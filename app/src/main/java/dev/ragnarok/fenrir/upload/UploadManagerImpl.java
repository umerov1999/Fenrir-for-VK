package dev.ragnarok.fenrir.upload;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.ApiException;
import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.server.UploadServer;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.domain.IAttachmentsRepository;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.service.ErrorLocalizer;
import dev.ragnarok.fenrir.upload.impl.AudioUploadable;
import dev.ragnarok.fenrir.upload.impl.ChatPhotoUploadable;
import dev.ragnarok.fenrir.upload.impl.DocumentUploadable;
import dev.ragnarok.fenrir.upload.impl.OwnerPhotoUploadable;
import dev.ragnarok.fenrir.upload.impl.Photo2AlbumUploadable;
import dev.ragnarok.fenrir.upload.impl.Photo2MessageUploadable;
import dev.ragnarok.fenrir.upload.impl.Photo2WallUploadable;
import dev.ragnarok.fenrir.upload.impl.RemoteAudioPlayUploadable;
import dev.ragnarok.fenrir.upload.impl.StoryUploadable;
import dev.ragnarok.fenrir.upload.impl.Video2WallUploadable;
import dev.ragnarok.fenrir.upload.impl.VideoToMessageUploadable;
import dev.ragnarok.fenrir.upload.impl.VideoUploadable;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UploadManagerImpl implements IUploadManager {

    private static final int PROGRESS_LOOKUP_DELAY = 500;
    private static final String NOTIFICATION_CHANNEL_ID = "upload_files";

    private final Context context;
    private final INetworker networker;
    private final IStorages storages;
    private final IAttachmentsRepository attachmentsRepository;
    private final IWallsRepository walls;
    private final List<Upload> queue = new ArrayList<>();
    private final Scheduler scheduler;

    private final PublishProcessor<List<Upload>> addingProcessor = PublishProcessor.create();
    private final PublishProcessor<int[]> deletingProcessor = PublishProcessor.create();
    private final PublishProcessor<Pair<Upload, UploadResult<?>>> completeProcessor = PublishProcessor.create();
    private final PublishProcessor<Upload> statusProcessor = PublishProcessor.create();

    private final Flowable<Long> timer;
    private final CompositeDisposable notificationUpdateDisposable = new CompositeDisposable();
    private final Map<String, UploadServer> serverMap = Collections.synchronizedMap(new HashMap<>());
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private volatile Upload current;
    private boolean needCreateChannel = true;

    public UploadManagerImpl(Context context, INetworker networker, IStorages storages, IAttachmentsRepository attachmentsRepository,
                             IWallsRepository walls) {
        this.context = context.getApplicationContext();
        this.networker = networker;
        this.storages = storages;
        this.attachmentsRepository = attachmentsRepository;
        this.walls = walls;
        scheduler = Schedulers.from(Executors.newSingleThreadExecutor());
        timer = Flowable.interval(PROGRESS_LOOKUP_DELAY, PROGRESS_LOOKUP_DELAY, TimeUnit.MILLISECONDS).onBackpressureBuffer();
    }

    private static Upload intent2Upload(UploadIntent intent) {
        return new Upload(intent.getAccountId())
                .setAutoCommit(intent.isAutoCommit())
                .setDestination(intent.getDestination())
                .setFileId(intent.getFileId())
                .setFileUri(intent.getFileUri())
                .setStatus(Upload.STATUS_QUEUE)
                .setSize(intent.getSize());
    }

    private static String createServerKey(Upload upload) {
        UploadDestination dest = upload.getDestination();

        StringBuilder builder = new StringBuilder();
        builder.append(Extra.ACCOUNT_ID).append(upload.getAccountId());
        builder.append(Extra.METHOD).append(dest.getMethod());

        switch (upload.getDestination().getMethod()) {
            case Method.DOCUMENT:
            case Method.VIDEO:
            case Method.AUDIO:
            case Method.TO_COMMENT:
            case Method.TO_WALL:
                if (dest.getOwnerId() < 0) {
                    builder.append(Extra.GROUP_ID).append(Math.abs(dest.getOwnerId()));
                }
                break;
            case Method.PHOTO_TO_ALBUM:
                builder.append(Extra.ALBUM_ID).append(dest.getId());
                if (dest.getOwnerId() < 0) {
                    builder.append(Extra.GROUP_ID).append(Math.abs(dest.getOwnerId()));
                }
                break;
            case Method.STORY:
            case Method.REMOTE_PLAY_AUDIO:
            case Method.TO_MESSAGE:
                //do nothink
                break;
            case Method.PHOTO_TO_PROFILE:
            case Method.PHOTO_TO_CHAT:
                builder.append(Extra.OWNER_ID).append(dest.getOwnerId());
                break;
        }

        return builder.toString();
    }

    @Override
    public Single<List<Upload>> get(int accountId, @NonNull UploadDestination destination) {
        return Single.fromCallable(() -> getByDestination(accountId, destination));
    }

    private List<Upload> getByDestination(int accountId, @NonNull UploadDestination destination) {
        synchronized (this) {
            List<Upload> data = new ArrayList<>();
            for (Upload upload : queue) {
                if (accountId == upload.getAccountId() && destination.compareTo(upload.getDestination())) {
                    data.add(upload);
                }
            }
            return data;
        }
    }

    private void startWithNotification() {
        updateNotification(Collections.emptyList());

        notificationUpdateDisposable.add(observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::updateNotification));
    }

    private void updateNotification(List<IProgressUpdate> updates) {
        if (nonEmpty(updates)) {
            int progress = updates.get(0).getProgress();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (isNull(notificationManager)) {
                return;
            }

            NotificationCompat.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (needCreateChannel) {
                    NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.channel_upload_files), NotificationManager.IMPORTANCE_LOW);
                    notificationManager.createNotificationChannel(channel);
                    needCreateChannel = false;
                }

                builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            } else {
                builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).setPriority(Notification.PRIORITY_LOW);
            }

            builder.setContentTitle(context.getString(R.string.files_uploading_notification_title))
                    .setSmallIcon(R.drawable.ic_notification_upload)
                    .setOngoing(true)
                    .setProgress(100, progress, false)
                    .build();

            notificationManager.notify(NotificationHelper.NOTIFICATION_UPLOAD, builder.build());
        }
    }

    private void stopNotification() {
        notificationUpdateDisposable.clear();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nonNull(notificationManager)) {
            notificationManager.cancel(NotificationHelper.NOTIFICATION_UPLOAD);
        }
    }

    @Override
    public void enqueue(@NonNull List<UploadIntent> intents) {
        synchronized (this) {
            List<Upload> all = new ArrayList<>(intents.size());

            for (UploadIntent intent : intents) {
                Upload upload = intent2Upload(intent);
                all.add(upload);
                queue.add(upload);
            }

            addingProcessor.onNext(all);
            startIfNotStarted();
        }
    }

    private void startIfNotStarted() {
        compositeDisposable.add(Completable.complete()
                .observeOn(scheduler)
                .subscribe(this::startIfNotStartedInternal));
    }

    private Upload findFirstQueue() {
        Upload first = null;
        for (Upload u : queue) {
            if (u.getStatus() == Upload.STATUS_QUEUE) {
                first = u;
                break;
            }
        }
        return first;
    }

    private void startIfNotStartedInternal() {
        synchronized (this) {
            if (current != null) {
                return;
            }

            Upload first = findFirstQueue();
            if (first == null) {
                stopNotification();
                return;
            }

            startWithNotification();

            current = first;

            first.setStatus(Upload.STATUS_UPLOADING).setErrorText(null);
            statusProcessor.onNext(first);

            IUploadable<?> uploadable = createUploadable(first);
            UploadServer server = serverMap.get(createServerKey(first));

            compositeDisposable.add(uploadable.doUpload(first, server, new WeakProgressPublisgher(first))
                    .subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe(result -> onUploadComplete(first, result), t -> onUploadFail(first, t)));
        }
    }

    private void onUploadComplete(Upload upload, UploadResult<?> result) {
        synchronized (this) {
            queue.remove(upload);

            if (current == upload) {
                current = null;
            }

            //final int accountId = upload.getAccountId();
            //final UploadDestination destination = upload.getDestination();
            //if (destination.getMethod() == Method.TO_MESSAGE && getByDestination(accountId, destination).isEmpty()) {
            //    sendMessageIfWaitForUpload(accountId, destination.getId());
            //}

            UploadDestination destination = upload.getDestination();
            if (destination.getMessageMethod() != MessageMethod.VIDEO && destination.getMethod() != Method.VIDEO && destination.getMethod() != Method.STORY)
                serverMap.put(createServerKey(upload), result.getServer());

            completeProcessor.onNext(Pair.Companion.create(upload, result));
            startIfNotStartedInternal();
        }
    }

    private void onUploadFail(Upload upload, Throwable t) {
        synchronized (this) {
            if (current == upload) {
                current = null;

                Throwable cause = getCauseIfRuntime(t);
                String message;
                if (cause instanceof ApiException) {
                    message = ErrorLocalizer.localizeThrowable(context, cause);
                } else {
                    message = firstNonEmptyString(cause.getMessage(), cause.toString());
                }
                compositeDisposable.add(Completable.complete()
                        .observeOn(Injection.provideMainThreadScheduler())
                        .subscribe(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show()));

            }
            String errorMessage;
            if (t instanceof ApiException) {
                errorMessage = ErrorLocalizer.localizeThrowable(context, t);
            } else {
                errorMessage = firstNonEmptyString(t.getMessage(), t.toString());
            }
            upload.setStatus(Upload.STATUS_ERROR).setErrorText(errorMessage);
            statusProcessor.onNext(upload);

            startIfNotStartedInternal();
        }
    }

    @Override
    public void cancel(int id) {
        synchronized (this) {
            if (current != null && current.getId() == id) {
                compositeDisposable.clear();
                current = null;
            }

            int index = Utils.findIndexById(queue, id);
            if (index != -1) {
                queue.remove(index);
                deletingProcessor.onNext(new int[]{id});
            }

            startIfNotStarted();
        }
    }

    @Override
    public void cancelAll(int accountId, @NonNull UploadDestination destination) {
        synchronized (this) {
            if (current != null && accountId == current.getAccountId() && destination.compareTo(current.getDestination())) {
                compositeDisposable.clear();
                current = null;
            }

            List<Upload> target = new ArrayList<>();

            Iterator<Upload> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Upload next = iterator.next();
                if (accountId == next.getAccountId() && destination.compareTo(next.getDestination())) {
                    iterator.remove();
                    target.add(next);
                }
            }

            if (!target.isEmpty()) {
                int[] ids = new int[target.size()];
                for (int i = 0; i < target.size(); i++) {
                    ids[i] = target.get(i).getId();
                }
                deletingProcessor.onNext(ids);
            }

            startIfNotStarted();
        }
    }

    @Override
    public Optional<Upload> getCurrent() {
        synchronized (this) {
            return Optional.wrap(current);
        }
    }

    @Override
    public Flowable<int[]> observeDeleting(boolean includeCompleted) {
        if (includeCompleted) {
            Flowable<int[]> completeIds = completeProcessor.onBackpressureBuffer()
                    .map(pair -> new int[]{pair.getFirst().getId()});

            return Flowable.merge(deletingProcessor.onBackpressureBuffer(), completeIds);
        }

        return deletingProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<List<Upload>> observeAdding() {
        return addingProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<Upload> obseveStatus() {
        return statusProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<Pair<Upload, UploadResult<?>>> observeResults() {
        return completeProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<List<IProgressUpdate>> observeProgress() {
        return timer.map(ignored -> {
            synchronized (this) {
                if (current == null) {
                    return Collections.emptyList();
                }

                IProgressUpdate update = new ProgressUpdate(current.getId(), current.getProgress());
                return Collections.singletonList(update);
            }
        });
    }

    private IUploadable<?> createUploadable(Upload upload) {
        UploadDestination destination = upload.getDestination();

        switch (destination.getMethod()) {
            case Method.VIDEO:
                return new VideoUploadable(context, networker);
            case Method.STORY:
                return new StoryUploadable(context, networker);
            case Method.AUDIO:
                return new AudioUploadable(context, networker);
            case Method.REMOTE_PLAY_AUDIO:
                return new RemoteAudioPlayUploadable(context, networker);
            case Method.TO_MESSAGE:
                if (destination.getMessageMethod() == MessageMethod.PHOTO)
                    return new Photo2MessageUploadable(context, networker, attachmentsRepository, storages.messages());
                else if (destination.getMessageMethod() == MessageMethod.VIDEO)
                    return new VideoToMessageUploadable(context, networker, attachmentsRepository, storages.messages());
                else
                    throw new UnsupportedOperationException();
            case Method.PHOTO_TO_ALBUM:
                return new Photo2AlbumUploadable(context, networker, storages.photos());
            case Method.DOCUMENT:
                return new DocumentUploadable(context, networker, storages.docs());
            case Method.TO_COMMENT:
            case Method.TO_WALL:
                if (destination.getMessageMethod() == MessageMethod.PHOTO)
                    return new Photo2WallUploadable(context, networker, attachmentsRepository);
                else if (destination.getMessageMethod() == MessageMethod.VIDEO)
                    return new Video2WallUploadable(context, networker, attachmentsRepository);
                else
                    throw new UnsupportedOperationException();
            case Method.PHOTO_TO_PROFILE:
                return new OwnerPhotoUploadable(context, networker, walls);
            case Method.PHOTO_TO_CHAT:
                return new ChatPhotoUploadable(context, networker);
        }

        throw new UnsupportedOperationException();
    }

    private static final class WeakProgressPublisgher implements PercentagePublisher {

        final WeakReference<Upload> reference;

        WeakProgressPublisgher(Upload upload) {
            reference = new WeakReference<>(upload);
        }

        @Override
        public void onProgressChanged(int percentage) {
            Upload upload = reference.get();
            if (upload != null) {
                upload.setProgress(percentage);
            }
        }
    }

    private static final class ProgressUpdate implements IProgressUpdate {

        final int id;
        final int progress;

        private ProgressUpdate(int id, int progress) {
            this.id = id;
            this.progress = progress;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int getProgress() {
            return progress;
        }
    }
}