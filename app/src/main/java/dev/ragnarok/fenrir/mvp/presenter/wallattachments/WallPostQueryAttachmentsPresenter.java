package dev.ragnarok.fenrir.mvp.presenter.wallattachments;

import static dev.ragnarok.fenrir.util.RxUtils.dummy;
import static dev.ragnarok.fenrir.util.RxUtils.ignore;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.criteria.WallCriteria;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.wallattachments.IWallPostQueryAttachmentsView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class WallPostQueryAttachmentsPresenter extends PlaceSupportPresenter<IWallPostQueryAttachmentsView> {

    private final ArrayList<Post> mPost;
    private final IWallsRepository fInteractor;
    private final int owner_id;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private int loaded;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;
    private String Query;

    public WallPostQueryAttachmentsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        owner_id = ownerId;
        mPost = new ArrayList<>();
        fInteractor = Repository.INSTANCE.getWalls();
    }

    @Override
    public void onGuiCreated(@NonNull IWallPostQueryAttachmentsView view) {
        super.onGuiCreated(view);
        view.displayData(mPost);

        resolveToolbar();
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.getWallNoCache(accountId, owner_id, offset, 100, WallCriteria.MODE_ALL)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    public void fireSearchRequestChanged(String q, boolean only_insert) {
        Query = q == null ? null : q.trim();
        if (only_insert) {
            return;
        }
        actualDataDisposable.clear();
        actualDataLoading = false;
        resolveRefreshingView();
        callView(v -> v.onSetLoadingStatus(0));
        fireRefresh();
    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private boolean check(String data, List<String> str) {
        for (String i : str) {
            if (data.toLowerCase().contains(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean doCompare(String data, List<String> str) {
        return Utils.safeCheck(data, () -> check(data, str));
    }

    private boolean checkDocs(ArrayList<Document> docs, List<String> str, List<Integer> ids) {
        if (Utils.isEmpty(docs)) {
            return false;
        }
        for (Document i : docs) {
            if (doCompare(i.getTitle(), str) || doCompare(i.getExt(), str) || ids.contains(i.getOwnerId())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPhotos(ArrayList<Photo> docs, List<String> str, List<Integer> ids) {
        if (Utils.isEmpty(docs)) {
            return false;
        }
        for (Photo i : docs) {
            if (doCompare(i.getText(), str) || ids.contains(i.getOwnerId())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkVideos(ArrayList<Video> docs, List<String> str, List<Integer> ids) {
        if (Utils.isEmpty(docs)) {
            return false;
        }
        for (Video i : docs) {
            if (doCompare(i.getTitle(), str) || doCompare(i.getDescription(), str) || ids.contains(i.getOwnerId())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAlbums(ArrayList<PhotoAlbum> docs, List<String> str, List<Integer> ids) {
        if (Utils.isEmpty(docs)) {
            return false;
        }
        for (PhotoAlbum i : docs) {
            if (doCompare(i.getTitle(), str) || doCompare(i.getDescription(), str) || ids.contains(i.getOwnerId())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkLinks(ArrayList<Link> docs, List<String> str) {
        if (Utils.isEmpty(docs)) {
            return false;
        }
        for (Link i : docs) {
            if (doCompare(i.getTitle(), str) || doCompare(i.getDescription(), str) || doCompare(i.getCaption(), str)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkArticles(ArrayList<Article> docs, List<String> str, List<Integer> ids) {
        if (Utils.isEmpty(docs)) {
            return false;
        }
        for (Article i : docs) {
            if (doCompare(i.getTitle(), str) || doCompare(i.getSubTitle(), str) || ids.contains(i.getOwnerId())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPoll(ArrayList<Poll> docs, List<String> str, List<Integer> ids) {
        if (Utils.isEmpty(docs)) {
            return false;
        }
        for (Poll i : docs) {
            if (doCompare(i.getQuestion(), str) || ids.contains(i.getOwnerId())) {
                return true;
            }
        }
        return false;
    }

    private void update(List<Post> data, List<String> str, List<Integer> ids) {

        for (Post i : data) {
            if ((i.hasText() && doCompare(i.getText(), str)) || ids.contains(i.getOwnerId()) || ids.contains(i.getSignerId()) || ids.contains(i.getAuthorId())) {
                mPost.add(i);
            } else if (i.getAuthor() != null && doCompare(i.getAuthor().getFullName(), str)) {
                mPost.add(i);
            } else if (i.hasAttachments() && (checkDocs(i.getAttachments().getDocs(), str, ids)
                    || checkAlbums(i.getAttachments().getPhotoAlbums(), str, ids) || checkArticles(i.getAttachments().getArticles(), str, ids)
                    || checkLinks(i.getAttachments().getLinks(), str) || checkPhotos(i.getAttachments().getPhotos(), str, ids)
                    || checkVideos(i.getAttachments().getVideos(), str, ids) || checkPoll(i.getAttachments().getPolls(), str, ids))) {
                mPost.add(i);
            }
            if (i.hasCopyHierarchy())
                update(i.getCopyHierarchy(), str, ids);
        }
    }

    private void onActualDataReceived(int offset, List<Post> data) {

        actualDataLoading = false;
        endOfContent = data.isEmpty();
        actualDataReceived = true;
        if (endOfContent)
            callResumedView(v -> v.onSetLoadingStatus(2));

        String[] str = Query.split("\\|");
        for (int i = 0; i < str.length; i++) {
            str[i] = str[i].trim().toLowerCase();
        }

        List<Integer> ids = new ArrayList<>();
        for (String cc : str) {
            if (cc.contains("*id")) {
                try {
                    ids.add(Integer.parseInt(cc.replace("*id", "")));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (offset == 0) {
            loaded = data.size();
            mPost.clear();
            update(data, Arrays.asList(str), ids);
            resolveToolbar();
            callView(IWallPostQueryAttachmentsView::notifyDataSetChanged);
        } else {
            int startSize = mPost.size();
            loaded += data.size();
            update(data, Arrays.asList(str), ids);
            resolveToolbar();
            callView(view -> view.notifyDataAdded(startSize, mPost.size() - startSize));
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

    private void resolveToolbar() {
        callView(v -> {
            v.setToolbarTitle(getString(R.string.attachments_in_wall));
            v.setToolbarSubtitle(getString(R.string.query, safeCountOf(mPost)) + " " + getString(R.string.posts_analized, loaded));
        });
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (Utils.isEmpty(Query)) {
            return true;
        }
        if (!endOfContent && actualDataReceived && !actualDataLoading) {
            loadActualData(loaded);
            return false;
        }
        return true;
    }

    public void fireRefresh() {
        if (Utils.isEmpty(Query)) {
            return;
        }
        actualDataDisposable.clear();
        actualDataLoading = false;

        loadActualData(0);
    }

    public void firePostBodyClick(Post post) {
        if (Utils.intValueIn(post.getPostType(), VKApiPost.Type.SUGGEST, VKApiPost.Type.POSTPONE)) {
            callView(v -> v.openPostEditor(getAccountId(), post));
            return;
        }

        firePostClick(post);
    }

    public void firePostRestoreClick(Post post) {
        appendDisposable(fInteractor.restore(getAccountId(), post.getOwnerId(), post.getVkid())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(dummy(), t -> callView(v -> showError(v, t))));
    }

    public void fireLikeLongClick(Post post) {
        callView(v -> v.goToLikes(getAccountId(), "post", post.getOwnerId(), post.getVkid()));
    }

    public void fireShareLongClick(Post post) {
        callView(v -> v.goToReposts(getAccountId(), "post", post.getOwnerId(), post.getVkid()));
    }

    public void fireLikeClick(Post post) {
        int accountId = getAccountId();

        appendDisposable(fInteractor.like(accountId, post.getOwnerId(), post.getVkid(), !post.isUserLikes())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(ignore(), t -> callView(v -> showError(v, t))));
    }
}
