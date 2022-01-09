package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.findIndexByPredicate;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.domain.IAttachmentsRepository;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.LocalVideo;
import dev.ragnarok.fenrir.model.ModelsBundle;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IMessageAttachmentsView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.IUploadManager;
import dev.ragnarok.fenrir.upload.MessageMethod;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadIntent;
import dev.ragnarok.fenrir.upload.UploadUtils;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.FileUtil;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Predicate;


public class MessageAttachmentsPresenter extends RxSupportPresenter<IMessageAttachmentsView> {

    private static final String SAVE_CAMERA_FILE_URI = "save_camera_file_uri";
    private static final String SAVE_ACCOMPANYING_ENTRIES = "save_accompanying_entries";
    private final int accountId;
    private final int messageOwnerId;
    private final int messageId;
    private final List<AttachmentEntry> entries;
    private final IAttachmentsRepository attachmentsRepository;
    private final UploadDestination destination;
    private final IUploadManager uploadManager;
    private final Context context;
    private Uri currentPhotoCameraUri;

    public MessageAttachmentsPresenter(int accountId, int messageOwnerId, int messageId, Context context, @Nullable ModelsBundle bundle, @Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.accountId = accountId;
        this.messageId = messageId;
        this.context = context;
        this.messageOwnerId = messageOwnerId;
        destination = UploadDestination.forMessage(messageId);
        entries = new ArrayList<>();
        attachmentsRepository = Injection.provideAttachmentsRepository();
        uploadManager = Injection.provideUploadManager();

        if (nonNull(savedInstanceState)) {
            currentPhotoCameraUri = savedInstanceState.getParcelable(SAVE_CAMERA_FILE_URI);
            ArrayList<AttachmentEntry> accompanying = savedInstanceState.getParcelableArrayList(SAVE_ACCOMPANYING_ENTRIES);
            AssertUtils.requireNonNull(accompanying);
            entries.addAll(accompanying);
        } else {
            handleInputModels(bundle);
        }

        Predicate<IAttachmentsRepository.IBaseEvent> predicate = event -> event.getAttachToType() == AttachToType.MESSAGE
                && event.getAttachToId() == messageId
                && event.getAccountId() == messageOwnerId;

        appendDisposable(attachmentsRepository
                .observeAdding()
                .filter(predicate)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(event -> onAttachmentsAdded(event.getAttachments())));

        appendDisposable(attachmentsRepository
                .observeRemoving()
                .filter(predicate)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(event -> onAttachmentRemoved(event.getGeneratedId())));

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsRemoved));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusChanges));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdates));

        loadData();
    }

    private static List<AttachmentEntry> entities2entries(List<Pair<Integer, AbsModel>> pairs) {
        List<AttachmentEntry> entries = new ArrayList<>(pairs.size());
        for (Pair<Integer, AbsModel> pair : pairs) {
            entries.add(new AttachmentEntry(true, pair.getSecond())
                    .setOptionalId(pair.getFirst()));
        }
        return entries;
    }

    private void handleInputModels(ModelsBundle bundle) {
        if (isNull(bundle)) {
            return;
        }

        for (AbsModel model : bundle) {
            entries.add(new AttachmentEntry(true, model).setAccompanying(true));
        }
    }

    private void resolveEmptyViewVisibility() {
        callView(v -> v.setEmptyViewVisible(entries.isEmpty()));
    }

    private void onUploadProgressUpdates(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            int index = findUploadObjectIndex(update.getId());
            if (index != -1) {
                int upId = entries.get(index).getId();
                Upload upload = (Upload) entries.get(index).getAttachment();
                if (upload.getStatus() != Upload.STATUS_UPLOADING) {
                    // for uploading only
                    continue;
                }

                upload.setProgress(update.getProgress());
                callView(view -> view.changePercentageSmoothly(upId, update.getProgress()));
            }
        }
    }

    private void onUploadStatusChanges(Upload upload) {
        int index = findUploadObjectIndex(upload.getId());
        if (index != -1) {
            ((Upload) entries.get(index).getAttachment())
                    .setStatus(upload.getStatus())
                    .setErrorText(upload.getErrorText());

            callView(view -> view.notifyItemChanged(index));
        }
    }

    private void onUploadsRemoved(int[] ids) {
        for (int id : ids) {
            int index = findUploadObjectIndex(id);
            if (index != -1) {
                entries.remove(index);
                callView(view -> view.notifyEntryRemoved(index));
                resolveEmptyViewVisibility();
            }
        }
    }

    private void onUploadsAdded(List<Upload> uploads) {
        int count = 0;
        for (int i = uploads.size() - 1; i >= 0; i--) {
            Upload upload = uploads.get(i);
            if (destination.compareTo(upload.getDestination())) {
                AttachmentEntry entry = new AttachmentEntry(true, upload);
                entries.add(0, entry);
                count++;
            }
        }

        int finalCount = count;
        callView(view -> view.notifyDataAdded(0, finalCount));
        resolveEmptyViewVisibility();
    }

    private int findUploadObjectIndex(int id) {
        return findIndexByPredicate(entries, entry -> {
            AbsModel model = entry.getAttachment();
            return model instanceof Upload && ((Upload) model).getId() == id;
        });
    }

    private void onAttachmentRemoved(int optionId) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getOptionalId() == optionId) {
                entries.remove(i);
                int finalI = i;
                callView(view -> view.notifyEntryRemoved(finalI));
                break;
            }
        }

        resolveEmptyViewVisibility();
    }

    private void onAttachmentsAdded(List<Pair<Integer, AbsModel>> pairs) {
        onDataReceived(entities2entries(pairs));
    }

    private void loadData() {
        appendDisposable(createLoadAllSingle()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, RxUtils.ignore()));
    }

    private Single<List<AttachmentEntry>> createLoadAllSingle() {
        return attachmentsRepository
                .getAttachmentsWithIds(messageOwnerId, AttachToType.MESSAGE, messageId)
                .map(MessageAttachmentsPresenter::entities2entries)
                .zipWith(uploadManager.get(messageOwnerId, destination), (atts, uploads) -> {
                    List<AttachmentEntry> data = new ArrayList<>(atts.size() + uploads.size());
                    for (Upload u : uploads) {
                        data.add(new AttachmentEntry(true, u));
                    }

                    data.addAll(atts);
                    return data;
                });
    }

    private void onDataReceived(List<AttachmentEntry> data) {
        if (data.isEmpty()) {
            return;
        }

        int startCount = entries.size();
        entries.addAll(data);

        resolveEmptyViewVisibility();
        callView(view -> view.notifyDataAdded(startCount, data.size()));
    }

    @Override
    public void onGuiCreated(@NonNull IMessageAttachmentsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayAttachments(entries);

        resolveEmptyViewVisibility();
    }

    public void fireAddPhotoButtonClick() {
        // Если сообщения группы - предлагать фотографии сообщества, а не группы
        callView(v -> v.addPhoto(accountId, messageOwnerId));
    }

    public void firePhotosSelected(ArrayList<Photo> photos, ArrayList<LocalPhoto> localPhotos, String file, LocalVideo video) {
        if (nonEmpty(file))
            doUploadFile(file);
        else if (nonEmpty(photos)) {
            fireAttachmentsSelected(photos);
        } else if (nonEmpty(localPhotos)) {
            doUploadPhotos(localPhotos);
        } else if (video != null) {
            doUploadVideo(video.getData().toString());
        }
    }

    private void doUploadFile(String file) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.select)
                .setNegativeButton(R.string.video, (dialog, which) -> doUploadFile(file, 0, true))
                .setPositiveButton(R.string.photo, (dialog, which) -> {
                    Integer size = Settings.get()
                            .main()
                            .getUploadImageSize();

                    if (isNull(size)) {
                        callView(v -> v.displaySelectUploadFileSizeDialog(file));
                    } else if (size == Upload.IMAGE_SIZE_CROPPING) {
                        callView(v -> v.displayCropPhotoDialog(Uri.fromFile(new File(file))));
                    } else {
                        doUploadFile(file, size, false);
                    }
                })
                .create().show();
    }

    private void doUploadPhotos(List<LocalPhoto> photos) {
        Integer size = Settings.get()
                .main()
                .getUploadImageSize();

        if (isNull(size)) {
            callView(v -> v.displaySelectUploadPhotoSizeDialog(photos));
        } else if (size == Upload.IMAGE_SIZE_CROPPING && photos.size() == 1) {
            Uri to_up = photos.get(0).getFullImageUri();
            if (new File(to_up.getPath()).isFile()) {
                to_up = Uri.fromFile(new File(to_up.getPath()));
            }
            Uri finalTo_up = to_up;
            callView(v -> v.displayCropPhotoDialog(finalTo_up));
        } else {
            doUploadPhotos(photos, size);
        }
    }

    public void doUploadFile(String file, int size, boolean isVideo) {
        List<UploadIntent> intents;
        if (isVideo) {
            intents = UploadUtils.createIntents(messageOwnerId, UploadDestination.forMessage(messageId, MessageMethod.VIDEO), file, size, true);
        } else {
            intents = UploadUtils.createIntents(messageOwnerId, destination, file, size, true);
        }
        uploadManager.enqueue(intents);
    }

    private void doUploadVideo(String file) {
        List<UploadIntent> intents = UploadUtils.createVideoIntents(messageOwnerId, UploadDestination.forMessage(messageId, MessageMethod.VIDEO), file, true);
        uploadManager.enqueue(intents);
    }

    private void doUploadPhotos(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(messageOwnerId, destination, photos, size, true);
        uploadManager.enqueue(intents);
    }

    public void fireRetryClick(AttachmentEntry entry) {
        fireRemoveClick(entry);
        if (entry.getAttachment() instanceof Upload) {
            Upload upl = ((Upload) entry.getAttachment());
            List<UploadIntent> intents = new ArrayList<>();
            intents.add(new UploadIntent(accountId, upl.getDestination())
                    .setSize(upl.getSize())
                    .setAutoCommit(upl.isAutoCommit())
                    .setFileId(upl.getFileId())
                    .setFileUri(upl.getFileUri()));
            uploadManager.enqueue(intents);
        }
    }

    public void fireRemoveClick(AttachmentEntry entry) {
        if (entry.getOptionalId() != 0) {
            RxUtils.subscribeOnIOAndIgnore(attachmentsRepository.remove(messageOwnerId, AttachToType.MESSAGE, messageId, entry.getOptionalId()));
            return;
        }

        if (entry.getAttachment() instanceof Upload) {
            uploadManager.cancel(((Upload) entry.getAttachment()).getId());
            return;
        }

        if (entry.isAccompanying()) {
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getId() == entry.getId()) {
                    entries.remove(i);
                    int finalI = i;
                    callView(v -> v.notifyEntryRemoved(finalI));
                    syncAccompanyingWithParent();
                    break;
                }
            }
        }
    }

    public void fireUploadPhotoSizeSelected(List<LocalPhoto> photos, int imageSize) {
        if (imageSize == Upload.IMAGE_SIZE_CROPPING && photos.size() == 1) {
            Uri to_up = photos.get(0).getFullImageUri();
            if (new File(to_up.getPath()).isFile()) {
                to_up = Uri.fromFile(new File(to_up.getPath()));
            }
            Uri finalTo_up = to_up;
            callView(v -> v.displayCropPhotoDialog(finalTo_up));
        } else {
            doUploadPhotos(photos, imageSize);
        }
    }

    public void fireUploadFileSizeSelected(String file, int imageSize) {
        if (imageSize == Upload.IMAGE_SIZE_CROPPING) {
            callView(v -> v.displayCropPhotoDialog(Uri.fromFile(new File(file))));
        } else {
            doUploadFile(file, imageSize, false);
        }
    }

    public void fireCameraPermissionResolved() {
        if (AppPerms.hasCameraPermission(getApplicationContext())) {
            makePhotoInternal();
        }
    }

    public void fireButtonCameraClick() {
        if (AppPerms.hasCameraPermission(getApplicationContext())) {
            makePhotoInternal();
        } else {
            callView(IMessageAttachmentsView::requestCameraPermission);
        }
    }

    public void fireCompressSettings(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.select_image_size_title))
                .setSingleChoiceItems(R.array.array_image_sizes_settings_names, Settings.get().main().getUploadImageSizePref(), (dialogInterface, j) -> {
                    Settings.get().main().setUploadImageSize(j);
                    dialogInterface.dismiss();
                })
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void makePhotoInternal() {
        try {
            File file = FileUtil.createImageFile();
            currentPhotoCameraUri = FileUtil.getExportedUriForFile(getApplicationContext(), file);
            callView(v -> v.startCamera(currentPhotoCameraUri));
        } catch (IOException e) {
            callView(v -> v.showError(e.getMessage()));
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_CAMERA_FILE_URI, currentPhotoCameraUri);

        // сохраняем в outState только неПерсистентные данные
        ArrayList<AttachmentEntry> accompanying = new ArrayList<>();
        for (AttachmentEntry entry : entries) {
            if (entry.isAccompanying()) {
                accompanying.add(entry);
            }
        }

        outState.putParcelableArrayList(SAVE_ACCOMPANYING_ENTRIES, accompanying);
    }

    private void syncAccompanyingWithParent() {
        ModelsBundle bundle = new ModelsBundle();
        for (AttachmentEntry entry : entries) {
            if (entry.isAccompanying()) {
                bundle.append(entry.getAttachment());
            }
        }

        callView(v -> v.syncAccompanyingWithParent(bundle));
    }

    public void firePhotoMaked() {
        Uri uri = currentPhotoCameraUri;
        currentPhotoCameraUri = null;

        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        getApplicationContext().sendBroadcast(scanIntent);

        LocalPhoto makedPhoto = new LocalPhoto().setFullImageUri(uri);
        doUploadPhotos(Collections.singletonList(makedPhoto));
    }

    public void fireButtonVideoClick() {
        callView(v -> v.startAddVideoActivity(accountId, messageOwnerId));
    }

    public void fireButtonAudioClick() {
        callView(v -> v.startAddAudioActivity(accountId));
    }

    public void fireButtonDocClick() {
        callView(v -> v.startAddDocumentActivity(accountId)); // TODO: 16.08.2017
    }

    public void fireAttachmentsSelected(ArrayList<? extends AbsModel> attachments) {
        RxUtils.subscribeOnIOAndIgnore(attachmentsRepository.attach(messageOwnerId, AttachToType.MESSAGE, messageId, attachments));
    }
}
