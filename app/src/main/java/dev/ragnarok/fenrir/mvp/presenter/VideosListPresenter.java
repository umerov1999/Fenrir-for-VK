package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.Injection.provideMainThreadScheduler;
import static dev.ragnarok.fenrir.util.Utils.findIndexById;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IVideosInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IVideosListView;
import dev.ragnarok.fenrir.upload.IUploadManager;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadIntent;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.FindAtWithContent;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class VideosListPresenter extends AccountDependencyPresenter<IVideosListView> {

    private static final int COUNT = 50;
    private static final int SEARCH_VIEW_COUNT = 15;
    private static final int SEARCH_COUNT = 100;
    private static final int WEB_SEARCH_DELAY = 1000;

    private final int ownerId;
    private final int albumId;
    private final String action;
    private final List<Video> data;
    private final IVideosInteractor interactor;
    private final IUploadManager uploadManager;
    private final String albumTitle;
    private final UploadDestination destination;
    private final List<Upload> uploadsData;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final FindVideo searcher;
    private Disposable sleepDataDisposable = Disposable.disposed();
    private boolean endOfContent;
    private IntNextFrom intNextFrom;
    private boolean hasActualNetData;
    private boolean requestNow;
    private boolean cacheNowLoading;

    public VideosListPresenter(int accountId, int ownerId, int albumId, String action,
                               @Nullable String albumTitle, Context context, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        interactor = InteractorFactory.createVideosInteractor();
        uploadManager = Injection.provideUploadManager();
        destination = UploadDestination.forVideo(IVideosListView.ACTION_SELECT.equalsIgnoreCase(action) ? 0 : 1, ownerId);
        uploadsData = new ArrayList<>(0);
        searcher = new FindVideo(netDisposable);

        this.ownerId = ownerId;
        this.albumId = albumId;
        this.action = action;
        this.albumTitle = albumTitle;

        intNextFrom = new IntNextFrom(0);

        data = new ArrayList<>();

        appendDisposable(uploadManager.get(getAccountId(), destination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUploadsDataReceived));

        appendDisposable(uploadManager.observeAdding()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadsAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadDeleted));

        appendDisposable(uploadManager.observeResults()
                .filter(pair -> destination.compareTo(pair.getFirst().getDestination()))
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadResults));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onProgressUpdates));


        loadAllFromCache();
        request(false);
        if (IVideosListView.ACTION_SELECT.equalsIgnoreCase(action)) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.confirmation)
                    .setMessage(R.string.do_upload_video)
                    .setPositiveButton(R.string.button_yes, (dialog, which) -> doUpload())
                    .setNegativeButton(R.string.button_no, null)
                    .show();
        }
    }

    private void sleep_search(String q) {
        if (requestNow || cacheNowLoading) return;

        sleepDataDisposable.dispose();
        if (Utils.isEmpty(q)) {
            searcher.cancel();
        } else {
            if (!searcher.isSearchMode()) {
                searcher.insertCache(data, intNextFrom.getOffset());
            }
            sleepDataDisposable = (Single.just(new Object())
                    .delay(WEB_SEARCH_DELAY, TimeUnit.MILLISECONDS)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(videos -> searcher.do_search(q), this::onListGetError));
        }
    }

    public void fireSearchRequestChanged(String q) {
        sleep_search(q == null ? null : q.trim());
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public Integer getAlbumId() {
        return albumId;
    }

    private void onUploadsDataReceived(List<Upload> data) {
        uploadsData.clear();
        uploadsData.addAll(data);

        callView(IVideosListView::notifyDataSetChanged);
        resolveUploadDataVisibility();
    }

    private void onUploadResults(Pair<Upload, UploadResult<?>> pair) {
        Video obj = (Video) pair.getSecond().getResult();
        if (obj.getId() == 0)
            callView(v -> v.getCustomToast().showToastError(R.string.error));
        else {
            callView(v -> v.getCustomToast().showToast(R.string.uploaded));
            if (IVideosListView.ACTION_SELECT.equalsIgnoreCase(action)) {
                callView(v -> v.onUploaded(obj));
            } else
                fireRefresh();
        }

    }

    private void onProgressUpdates(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            int index = findIndexById(uploadsData, update.getId());
            if (index != -1) {
                callView(view -> view.notifyUploadProgressChanged(index, update.getProgress(), true));
            }
        }
    }

    private void onUploadStatusUpdate(Upload upload) {
        int index = findIndexById(uploadsData, upload.getId());
        if (index != -1) {
            callView(view -> view.notifyUploadItemChanged(index));
        }
    }

    private void onUploadsAdded(List<Upload> added) {
        for (Upload u : added) {
            if (destination.compareTo(u.getDestination())) {
                int index = uploadsData.size();
                uploadsData.add(u);
                callView(view -> view.notifyUploadItemsAdded(index, 1));
            }
        }

        resolveUploadDataVisibility();
    }

    private void onUploadDeleted(int[] ids) {
        for (int id : ids) {
            int index = findIndexById(uploadsData, id);
            if (index != -1) {
                uploadsData.remove(index);
                callView(view -> view.notifyUploadItemRemoved(index));
            }
        }

        resolveUploadDataVisibility();
    }

    private void resolveUploadDataVisibility() {
        callView(v -> v.setUploadDataVisible(!uploadsData.isEmpty()));
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayLoading(requestNow));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    public void doUpload() {
        if (AppPerms.hasReadStoragePermission(getApplicationContext())) {
            callView(v -> v.startSelectUploadFileActivity(getAccountId()));
        } else {
            callView(IVideosListView::requestReadExternalStoragePermission);
        }
    }

    public void fireRemoveClick(Upload upload) {
        uploadManager.cancel(upload.getId());
    }

    public void fireReadPermissionResolved() {
        if (AppPerms.hasReadStoragePermission(getApplicationContext())) {
            callView(v -> v.startSelectUploadFileActivity(getAccountId()));
        }
    }

    private void request(boolean more) {
        if (requestNow) return;

        setRequestNow(true);

        int accountId = getAccountId();

        IntNextFrom startFrom = more ? intNextFrom : new IntNextFrom(0);

        netDisposable.add(interactor.get(accountId, ownerId, albumId, COUNT, startFrom.getOffset())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> {
                    IntNextFrom nextFrom = new IntNextFrom(startFrom.getOffset() + COUNT);
                    onRequestResposnse(videos, startFrom, nextFrom);
                }, this::onListGetError));
    }

    private void onListGetError(Throwable throwable) {
        setRequestNow(false);
        callView(v -> showError(v, throwable));
    }

    private void onRequestResposnse(List<Video> videos, IntNextFrom startFrom, IntNextFrom nextFrom) {
        cacheDisposable.clear();
        cacheNowLoading = false;

        hasActualNetData = true;
        intNextFrom = nextFrom;
        endOfContent = videos.isEmpty();

        if (startFrom.getOffset() == 0) {
            data.clear();
            data.addAll(videos);

            callView(IVideosListView::notifyDataSetChanged);
        } else {
            if (nonEmpty(videos)) {
                int startSize = data.size();
                data.addAll(videos);
                callView(view -> view.notifyDataAdded(startSize, videos.size()));
            }
        }

        setRequestNow(false);
    }

    @Override
    public void onGuiCreated(@NonNull IVideosListView view) {
        super.onGuiCreated(view);
        view.displayData(data);
        view.displayUploads(uploadsData);
        view.setToolbarSubtitle(albumTitle);

        resolveUploadDataVisibility();
    }

    public void fireFileForUploadSelected(String file) {
        UploadIntent intent = new UploadIntent(getAccountId(), destination)
                .setAutoCommit(true)
                .setFileUri(Uri.parse(file));

        uploadManager.enqueue(Collections.singletonList(intent));
    }

    private void loadAllFromCache() {
        cacheNowLoading = true;
        int accountId = getAccountId();

        cacheDisposable.add(interactor.getCachedVideos(accountId, ownerId, albumId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, Analytics::logUnexpectedError));
    }

    private void onCachedDataReceived(List<Video> videos) {
        data.clear();
        data.addAll(videos);

        callView(IVideosListView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        sleepDataDisposable.dispose();
        super.onDestroyed();
    }

    public void fireRefresh() {
        if (requestNow || cacheNowLoading) {
            return;
        }

        if (searcher.isSearchMode()) {
            searcher.reset();
        } else {
            request(false);
        }
    }

    public void fireOnVideoLongClick(int position, @NonNull Video video) {
        callView(v -> v.doVideoLongClick(getAccountId(), ownerId, ownerId == getAccountId(), position, video));
    }

    public void fireScrollToEnd() {
        if (nonEmpty(data) && hasActualNetData && !cacheNowLoading && !requestNow) {
            if (searcher.isSearchMode()) {
                searcher.do_search();
            } else if (!endOfContent) {
                request(true);
            }
        }
    }

    public void fireVideoClick(Video video) {
        if (IVideosListView.ACTION_SELECT.equalsIgnoreCase(action)) {
            callView(v -> v.returnSelectionToParent(video));
        } else {
            callView(v -> v.showVideoPreview(getAccountId(), video));
        }
    }

    private void fireEditVideo(Context context, int position, @NonNull Video video) {
        View root = View.inflate(context, R.layout.entry_video_info, null);
        ((TextInputEditText) root.findViewById(R.id.edit_title)).setText(video.getTitle());
        ((TextInputEditText) root.findViewById(R.id.edit_description)).setText(video.getDescription());
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.edit)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    String title = ((TextInputEditText) root.findViewById(R.id.edit_title)).getText().toString();
                    String description = ((TextInputEditText) root.findViewById(R.id.edit_description)).getText().toString();
                    appendDisposable(interactor.edit(getAccountId(), video.getOwnerId(), video.getId(),
                            title, description).compose(RxUtils.applyCompletableIOToMainSchedulers())
                            .subscribe(() -> {
                                data.get(position).setTitle(title).setDescription(description);
                                callView(v -> v.notifyItemChanged(position));
                            }, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void onAddComplete() {
        callView(IVideosListView::showSuccessToast);
    }

    public void fireVideoOption(int id, @NonNull Video video, int position, Context context) {
        if (id == R.id.action_add_to_my_videos) {
            netDisposable.add(interactor.addToMy(getAccountId(), getAccountId(), video.getOwnerId(), video.getId())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(this::onAddComplete, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
        } else if (id == R.id.action_edit) {
            fireEditVideo(context, position, video);
        } else if (id == R.id.action_delete_from_my_videos) {
            netDisposable.add(interactor.delete(getAccountId(), video.getId(), video.getOwnerId(), getAccountId())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> {
                        data.remove(position);
                        callView(v -> v.notifyItemRemoved(position));
                    }, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
        } else if (id == R.id.share_button) {
            callView(v -> v.displayShareDialog(getAccountId(), video, getAccountId() != ownerId));
        }
    }

    private class FindVideo extends FindAtWithContent<Video> {
        public FindVideo(CompositeDisposable disposable) {
            super(disposable, SEARCH_VIEW_COUNT, SEARCH_COUNT);
        }

        @Override
        protected Single<List<Video>> search(int offset, int count) {
            return interactor.get(getAccountId(), ownerId, albumId, count, offset);
        }

        @Override
        protected void onError(@NonNull Throwable e) {
            onListGetError(e);
        }

        @Override
        protected void onResult(@NonNull List<Video> data) {
            hasActualNetData = true;
            int startSize = VideosListPresenter.this.data.size();
            VideosListPresenter.this.data.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        @Override
        protected void updateLoading(boolean loading) {
            setRequestNow(loading);
        }

        @Override
        protected void clean() {
            data.clear();
            callView(IVideosListView::notifyDataSetChanged);
        }

        @Override
        protected boolean compare(@NonNull Video data, @NonNull String q) {
            return Utils.safeCheck(data.getTitle(), () -> data.getTitle().toLowerCase().contains(q.toLowerCase()))
                    || Utils.safeCheck(data.getDescription(), () -> data.getDescription().toLowerCase().contains(q.toLowerCase()));
        }

        @Override
        protected void onReset(@NonNull List<Video> data, int offset, boolean isEnd) {
            if (Utils.isEmpty(data)) {
                fireRefresh();
            } else {
                VideosListPresenter.this.data.clear();
                VideosListPresenter.this.data.addAll(data);
                intNextFrom.setOffset(offset);
                endOfContent = isEnd;
                callView(IVideosListView::notifyDataSetChanged);
            }
        }
    }
}
