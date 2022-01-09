package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IVideosInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.VideoAlbum;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IVideoAlbumsByVideoView;
import dev.ragnarok.fenrir.util.RxUtils;


public class VideoAlbumsByVideoPresenter extends AccountDependencyPresenter<IVideoAlbumsByVideoView> {

    private final int ownerId;
    private final int videoOwnerId;
    private final int videoId;
    private final List<VideoAlbum> data;
    private final IVideosInteractor videosInteractor;
    private boolean netLoadingNow;

    public VideoAlbumsByVideoPresenter(int accountId, int ownerId, int owner, int video, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        videosInteractor = InteractorFactory.createVideosInteractor();
        this.ownerId = ownerId;
        videoOwnerId = owner;
        videoId = video;
        data = new ArrayList<>();

        requestActualData();
    }

    private void resolveRefreshingView() {
        callView(v -> v.displayLoading(netLoadingNow));
    }

    private void requestActualData() {
        netLoadingNow = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        appendDisposable(videosInteractor.getAlbumsByVideo(accountId, ownerId, videoOwnerId, videoId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onActualDataReceived, this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();

        callView(v -> showError(v, t));
    }

    private void onActualDataReceived(List<VideoAlbum> albums) {

        netLoadingNow = false;

        resolveRefreshingView();

        data.clear();
        data.addAll(albums);
        callView(IVideoAlbumsByVideoView::notifyDataSetChanged);
    }

    @Override
    public void onGuiCreated(@NonNull IVideoAlbumsByVideoView view) {
        super.onGuiCreated(view);
        view.displayData(data);

        resolveRefreshingView();
    }

    public void fireItemClick(VideoAlbum album) {
        callView(v -> v.openAlbum(getAccountId(), ownerId, album.getId(), null, album.getTitle()));
    }

    public void fireRefresh() {
        requestActualData();
    }
}
