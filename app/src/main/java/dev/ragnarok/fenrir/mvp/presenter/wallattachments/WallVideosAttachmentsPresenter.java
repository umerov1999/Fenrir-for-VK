package dev.ragnarok.fenrir.mvp.presenter.wallattachments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.criteria.WallCriteria;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.reflect.OnGuiCreated;
import dev.ragnarok.fenrir.mvp.view.wallattachments.IWallVideosAttachmentsView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

public class WallVideosAttachmentsPresenter extends PlaceSupportPresenter<IWallVideosAttachmentsView> {

    private final ArrayList<Video> mVideos;
    private final IWallsRepository fInteractor;
    private final int owner_id;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private int loaded;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;

    public WallVideosAttachmentsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        owner_id = ownerId;
        mVideos = new ArrayList<>();
        fInteractor = Repository.INSTANCE.getWalls();
        loadActualData(0);
    }

    @Override
    public void onGuiCreated(@NonNull IWallVideosAttachmentsView view) {
        super.onGuiCreated(view);
        view.displayData(mVideos);
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.getWallNoCache(accountId, owner_id, offset, 100, WallCriteria.MODE_ALL)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void update(List<Post> data) {
        for (Post i : data) {
            if (i.hasAttachments() && !isEmpty(i.getAttachments().getVideos()))
                mVideos.addAll(i.getAttachments().getVideos());
            if (i.hasCopyHierarchy())
                update(i.getCopyHierarchy());
        }
    }

    private void onActualDataReceived(int offset, List<Post> data) {

        actualDataLoading = false;
        endOfContent = data.isEmpty();
        actualDataReceived = true;
        if (endOfContent)
            callResumedView(v -> v.onSetLoadingStatus(2));

        if (offset == 0) {
            loaded = data.size();
            mVideos.clear();
            update(data);
            resolveToolbar();
            callView(IWallVideosAttachmentsView::notifyDataSetChanged);
        } else {
            int startSize = mVideos.size();
            loaded += data.size();
            update(data);
            resolveToolbar();
            callView(view -> view.notifyDataAdded(startSize, mVideos.size() - startSize));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.showRefreshing(actualDataLoading));
        if (!endOfContent)
            callResumedView(v -> v.onSetLoadingStatus(actualDataLoading ? 1 : 0));
    }

    @OnGuiCreated
    private void resolveToolbar() {
        callView(v -> v.setToolbarTitle(getString(R.string.attachments_in_wall)));
        callView(v -> v.setToolbarSubtitle(getString(R.string.videos_count, safeCountOf(mVideos)) + " " + getString(R.string.posts_analized, loaded)));
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (!endOfContent && actualDataReceived && !actualDataLoading) {
            loadActualData(loaded);
            return false;
        }
        return true;
    }

    public void fireRefresh() {

        actualDataDisposable.clear();
        actualDataLoading = false;

        loadActualData(0);
    }
}
