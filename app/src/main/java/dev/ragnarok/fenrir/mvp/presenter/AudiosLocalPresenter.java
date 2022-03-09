package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.Includes.provideMainThreadScheduler;
import static dev.ragnarok.fenrir.util.Utils.findIndexById;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Includes;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.media.music.MusicPlaybackService;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudiosLocalView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.IUploadManager;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadIntent;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AudiosLocalPresenter extends AccountDependencyPresenter<IAudiosLocalView> {

    private final ArrayList<Audio> origin_audios;
    private final ArrayList<Audio> audios;
    private final CompositeDisposable audioListDisposable = new CompositeDisposable();
    private final IUploadManager uploadManager;
    private final List<Upload> uploadsData;
    private final UploadDestination destination;
    private final UploadDestination remotePlay;
    private boolean actualReceived;
    private boolean loadingNow;
    private String query;
    private boolean errorPermissions;
    private boolean doAudioLoadTabs;
    private int bucket_id;

    public AudiosLocalPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        destination = UploadDestination.forAudio(accountId);
        remotePlay = UploadDestination.forRemotePlay();
        uploadManager = Includes.getUploadManager();
        uploadsData = new ArrayList<>(0);
        audios = new ArrayList<>();
        origin_audios = new ArrayList<>();
    }

    public void fireBucketSelected(int bucket_id) {
        this.bucket_id = bucket_id;
        fireRefresh();
    }

    public void firePrepared() {
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
                .filter(pair -> destination.compareTo(pair.getFirst().getDestination()) || remotePlay.compareTo(pair.getFirst().getDestination()))
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadResults));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onProgressUpdates));

        fireRefresh();
    }

    public void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    private boolean checkTittleArtists(@NonNull Audio data, @NonNull String q) {
        String[] r = q.split("( - )|( )|( {2})", 2);
        if (r.length >= 2) {
            return Utils.safeCheck(data.getArtist(), () -> data.getArtist().toLowerCase().contains(r[0].toLowerCase()))
                    && Utils.safeCheck(data.getTitle(), () -> data.getTitle().toLowerCase().contains(r[1].toLowerCase()));
        }
        return false;
    }

    public void updateCriteria() {
        setLoadingNow(true);
        audios.clear();
        if (Utils.isEmpty(query)) {
            audios.addAll(origin_audios);
            setLoadingNow(false);
            callView(IAudiosLocalView::notifyListChanged);
            return;
        }
        for (Audio i : origin_audios) {
            if (Utils.safeCheck(i.getTitle(), () -> i.getTitle().toLowerCase().contains(query.toLowerCase()))
                    || Utils.safeCheck(i.getArtist(), () -> i.getArtist().toLowerCase().contains(query.toLowerCase())) || checkTittleArtists(i, query)) {
                audios.add(i);
            }
        }
        setLoadingNow(false);
        callView(IAudiosLocalView::notifyListChanged);
    }

    public void fireQuery(String q) {
        if (Utils.isEmpty(q))
            query = null;
        else {
            query = q;
        }
        updateCriteria();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        if (doAudioLoadTabs) {
            return;
        } else {
            doAudioLoadTabs = true;
        }
        callView(IAudiosLocalView::checkPermission);
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayRefreshing(loadingNow));
    }

    public void requestList() {
        setLoadingNow(true);
        if (bucket_id == 0) {
            audioListDisposable.add(Stores.getInstance()
                    .localMedia()
                    .getAudios(getAccountId())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onListReceived, this::onListGetError));
        } else {
            audioListDisposable.add(Stores.getInstance()
                    .localMedia()
                    .getAudios(getAccountId(), bucket_id)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onListReceived, this::onListGetError));
        }
    }

    private void onListReceived(List<Audio> data) {
        if (Utils.isEmpty(data)) {
            actualReceived = true;
            setLoadingNow(false);
            return;
        }
        origin_audios.clear();
        actualReceived = true;
        origin_audios.addAll(data);
        updateCriteria();
        setLoadingNow(false);
    }

    public void playAudio(Context context, int position) {
        MusicPlaybackService.startForPlayList(context, audios, position, false);
        if (!Settings.get().other().isShow_mini_player())
            PlaceFactory.getPlayerPlace(getAccountId()).tryOpenWith(context);
    }

    public void fireDelete(int position) {
        audios.remove(position);
        callView(v -> v.notifyItemRemoved(position));
    }

    @Override
    public void onDestroyed() {
        audioListDisposable.dispose();
        super.onDestroyed();
    }

    public void fireRemoveClick(Upload upload) {
        uploadManager.cancel(upload.getId());
    }

    private void onListGetError(Throwable t) {
        setLoadingNow(false);
        callResumedView(v -> showError(v, Utils.getCauseIfRuntime(t)));
    }

    public void fireFileForUploadSelected(String file) {
        UploadIntent intent = new UploadIntent(getAccountId(), destination)
                .setAutoCommit(true)
                .setFileUri(Uri.parse(file));

        uploadManager.enqueue(Collections.singletonList(intent));
    }

    public void fireFileForRemotePlaySelected(String file) {
        UploadIntent intent = new UploadIntent(getAccountId(), remotePlay)
                .setAutoCommit(true)
                .setFileUri(Uri.parse(file));

        uploadManager.enqueue(Collections.singletonList(intent));
    }

    public int getAudioPos(Audio audio) {
        if (!Utils.isEmpty(audios) && audio != null) {
            int pos = 0;
            for (Audio i : audios) {
                if (i.getId() == audio.getId() && i.getOwnerId() == audio.getOwnerId()) {
                    i.setAnimationNow(true);
                    int finalPos = pos;
                    callView(v -> v.notifyItemChanged(finalPos));
                    return pos;
                }
                pos++;
            }
        }
        return -1;
    }

    public void firePermissionsCanceled() {
        errorPermissions = true;
    }

    public void fireRefresh() {
        if (errorPermissions) {
            errorPermissions = false;
            callView(IAudiosLocalView::checkPermission);
            return;
        }
        audioListDisposable.clear();
        requestList();
    }

    public void fireScrollToEnd() {
        if (actualReceived) {
            requestList();
        }
    }

    @Override
    public void onGuiCreated(@NonNull IAudiosLocalView view) {
        super.onGuiCreated(view);
        view.displayList(audios);
        view.displayUploads(uploadsData);
        resolveUploadDataVisibility();
    }

    private void onUploadsDataReceived(List<Upload> data) {
        uploadsData.clear();
        uploadsData.addAll(data);

        resolveUploadDataVisibility();
    }

    private void onUploadResults(Pair<Upload, UploadResult<?>> pair) {
        Audio obj = (Audio) pair.getSecond().getResult();
        if (obj.getId() == 0)
            callView(v -> v.getCustomToast().showToastError(R.string.error));
        else {
            callView(v -> v.getCustomToast().showToast(R.string.uploaded));
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

}
