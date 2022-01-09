package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.api.model.VkApiPostSource.Data.PROFILE_ACTIVITY;
import static dev.ragnarok.fenrir.api.model.VkApiPostSource.Data.PROFILE_PHOTO;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.db.model.PostUpdate;
import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentedType;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IWallPostView;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.core.Completable;

public class WallPostPresenter extends PlaceSupportPresenter<IWallPostView> {
    private static final String SAVE_POST = "save-post";
    private static final String SAVE_OWNER = "save-owner";

    private final int postId;
    private final int ownerId;
    private final IWallsRepository wallInteractor;
    private final IOwnersRepository ownersRepository;
    private final Context context;
    private final IFaveInteractor faveInteractor;
    private Post post;
    private Owner owner;
    private boolean loadingPostNow;

    public WallPostPresenter(int accountId, int postId, int ownerId, @Nullable Post post,
                             @Nullable Owner owner, Context context, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.postId = postId;
        this.ownerId = ownerId;
        ownersRepository = Repository.INSTANCE.getOwners();
        wallInteractor = Repository.INSTANCE.getWalls();
        faveInteractor = InteractorFactory.createFaveInteractor();

        this.context = context;

        if (nonNull(savedInstanceState)) {
            ParcelableOwnerWrapper wrapper = savedInstanceState.getParcelable(SAVE_OWNER);
            AssertUtils.requireNonNull(wrapper);

            this.post = savedInstanceState.getParcelable(SAVE_POST);
            this.owner = wrapper.get();
        } else {
            this.post = post;
            this.owner = owner;

            loadActualPostInfo();
        }

        loadOwnerInfoIfNeed();

        appendDisposable(wallInteractor.observeMinorChanges()
                .filter(event -> event.getOwnerId() == ownerId && event.getPostId() == postId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostUpdate));

        appendDisposable(wallInteractor.observeChanges()
                .filter(p -> postId == p.getVkid() && p.getOwnerId() == ownerId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostChanged));
    }

    private void onPostChanged(Post post) {
        this.post = post;

        resolveCommentsView();
        resolveLikesView();
        resolveToolbarView();
        resolveCommentsView();
        resolveRepostsView();
    }

    private void onPostUpdate(PostUpdate update) {
        if (isNull(post)) {
            return;
        }

        if (nonNull(update.getLikeUpdate())) {
            post.setLikesCount(update.getLikeUpdate().getCount());

            if (update.getAccountId() == getAccountId()) {
                post.setUserLikes(update.getLikeUpdate().isLiked());
            }

            resolveLikesView();
        }

        if (nonNull(update.getPinUpdate())) {
            post.setPinned(update.getPinUpdate().isPinned());
        }

        if (nonNull(update.getDeleteUpdate())) {
            post.setDeleted(update.getDeleteUpdate().isDeleted());
            resolveContentRootView();
        }
    }

    private void loadOwnerInfoIfNeed() {
        if (isNull(owner)) {
            int accountId = getAccountId();
            appendDisposable(ownersRepository.getBaseOwnerInfo(accountId, ownerId, IOwnersRepository.MODE_NET)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onOwnerInfoReceived, RxUtils.ignore()));
        }
    }

    private void onOwnerInfoReceived(Owner owner) {
        this.owner = owner;
    }

    private void setLoadingPostNow(boolean loadingPostNow) {
        this.loadingPostNow = loadingPostNow;
        resolveContentRootView();
    }

    private void loadActualPostInfo() {
        if (loadingPostNow) {
            return;
        }

        int accountId = getAccountId();

        setLoadingPostNow(true);

        appendDisposable(wallInteractor.getById(accountId, ownerId, postId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onActualPostReceived, this::onLoadPostInfoError));
    }

    private void onLoadPostInfoError(Throwable t) {
        setLoadingPostNow(false);
        callView(v -> showError(v, t));
    }

    private void onActualPostReceived(Post post) {
        this.post = post;

        setLoadingPostNow(false);

        resolveToolbarView();
        resolveCommentsView();
        resolveLikesView();
        resolveRepostsView();
    }

    @Override
    public void onGuiCreated(@NonNull IWallPostView view) {
        super.onGuiCreated(view);

        resolveRepostsView();
        resolveLikesView();
        resolveCommentsView();
        resolveContentRootView();
        resolveToolbarView();
    }

    private void resolveRepostsView() {
        if (nonNull(post)) {
            callView(v -> v.displayReposts(post.getRepostCount(), post.isUserReposted()));
        }
    }

    private void resolveLikesView() {
        if (nonNull(post)) {
            callView(v -> v.displayLikes(post.getLikesCount(), post.isUserLikes()));
        }
    }

    private void resolveCommentsView() {
        if (nonNull(post)) {
            callView(v -> v.displayCommentCount(post.getCommentsCount()));
            callView(v -> v.setCommentButtonVisible(post.isCanPostComment() || post.getCommentsCount() > 0));
        }
    }

    private void resolveContentRootView() {
        if (nonNull(post)) {
            callView(v -> v.displayPostInfo(post));
        } else if (loadingPostNow) {
            callView(IWallPostView::displayLoading);
        } else {
            callView(IWallPostView::displayLoadingFail);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_POST, post);
        outState.putParcelable(SAVE_OWNER, new ParcelableOwnerWrapper(owner));
    }

    public void fireOptionViewCreated(IWallPostView.IOptionView view) {
        view.setCanPin(nonNull(post) && !post.isPinned() && post.isCanPin() && !post.isDeleted());
        view.setCanUnpin(nonNull(post) && post.isPinned() && post.isCanPin() && !post.isDeleted());
        view.setCanDelete(canDelete());
        view.setCanRestore(nonNull(post) && post.isDeleted());
        view.setCanEdit(nonNull(post) && post.isCanEdit());
    }

    private boolean canDelete() {
        if (isNull(post) || post.isDeleted()) {
            return false;
        }

        int accountId = getAccountId();

        boolean canDeleteAsAdmin = owner instanceof Community && ((Community) owner).isAdmin();
        boolean canDeleteAsOwner = ownerId == accountId || post.getAuthorId() == accountId;
        return canDeleteAsAdmin || canDeleteAsOwner;
    }

    public void fireGoToOwnerClick() {
        fireOwnerClick(ownerId);
    }

    public void firePostEditClick() {
        if (isNull(post)) {
            callView(IWallPostView::showPostNotReadyToast);
            return;
        }

        callView(v -> v.goToPostEditing(getAccountId(), post));
    }

    public void fireCommentClick() {
        Commented commented = new Commented(postId, ownerId, CommentedType.POST, null);
        callView(v -> v.openComments(getAccountId(), commented, null));
    }

    public void fireRepostLongClick() {
        callView(v -> v.goToReposts(getAccountId(), "post", ownerId, postId));
    }

    public void fireLikeLongClick() {
        callView(v -> v.goToLikes(getAccountId(), "post", ownerId, postId));
    }

    public void fireTryLoadAgainClick() {
        loadActualPostInfo();
    }

    public void fireShareClick() {
        if (nonNull(post)) {
            callView(v -> v.repostPost(getAccountId(), post));
        } else {
            callView(IWallPostView::showPostNotReadyToast);
        }
    }

    public void fireLikeClick() {
        if (nonNull(post)) {
            appendDisposable(wallInteractor.like(getAccountId(), ownerId, postId, !post.isUserLikes())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(RxUtils.ignore(), t -> callView(v -> showError(v, t))));
        } else {
            callView(IWallPostView::showPostNotReadyToast);
        }
    }

    public void fireAddBookmark() {
        appendDisposable(faveInteractor.addPost(getAccountId(), ownerId, postId, null)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onPostAddedToBookmarks, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onPostAddedToBookmarks() {
        callView(IWallPostView::showSuccessToast);
    }

    public void fireDeleteClick() {
        deleteOrRestore(true);
    }

    public void fireRestoreClick() {
        deleteOrRestore(false);
    }

    private void deleteOrRestore(boolean delete) {
        int accountId = getAccountId();

        Completable completable = delete ? wallInteractor.delete(accountId, ownerId, postId)
                : wallInteractor.restore(accountId, ownerId, postId);

        appendDisposable(completable
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onDeleteOrRestoreComplete(delete), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onDeleteOrRestoreComplete(boolean deleted) {
        callView(view -> view.displayDeleteOrRestoreComplete(deleted));
    }

    public void firePinClick() {
        pinOrUnpin(true);
    }

    public void fireUnpinClick() {
        pinOrUnpin(false);
    }

    private void pinOrUnpin(boolean pin) {
        int accountId = getAccountId();

        appendDisposable(wallInteractor.pinUnpin(accountId, ownerId, postId, pin)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onPinOrUnpinComplete(pin), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onPinOrUnpinComplete(boolean pinned) {
        callView(view -> view.displayPinComplete(pinned));
    }

    public void fireRefresh() {
        loadActualPostInfo();
    }

    public void fireCopyLinkClink() {
        String link = String.format("vk.com/wall%s_%s", ownerId, postId);
        callView(v -> v.copyLinkToClipboard(link));
    }

    public void fireReport() {
        CharSequence[] items = {"Спам", "Детская порнография", "Экстремизм", "Насилие", "Пропаганда наркотиков", "Материал для взрослых", "Оскорбление", "Призывы к суициду"};
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.report)
                .setItems(items, (dialog, item) -> {
                    appendDisposable(wallInteractor.reportPost(getAccountId(), ownerId, postId, item)
                            .compose(RxUtils.applySingleIOToMainSchedulers())
                            .subscribe(p -> {
                                if (p == 1)
                                    callView(v -> v.getCustomToast().showToast(R.string.success));
                                else
                                    callView(v -> v.getCustomToast().showToast(R.string.error));
                            }, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
                    dialog.dismiss();
                })
                .show();
    }

    private void resolveToolbarView() {
        if (nonNull(post)) {
            int type = IWallPostView.SUBTITLE_NORMAL;

            if (nonNull(post.getSource())) {
                switch (post.getSource().getData()) {
                    case PROFILE_ACTIVITY:
                        type = IWallPostView.SUBTITLE_STATUS_UPDATE;
                        break;
                    case PROFILE_PHOTO:
                        type = IWallPostView.SUBTITLE_PHOTO_UPDATE;
                        break;
                }
            }

            int finalType = type;
            callView(v -> {
                v.displayToolbarTitle(post.getAuthorName());
                v.displayToolbatSubtitle(finalType, post.getDate());
            });
        } else {
            callView(v -> {
                v.displayDefaultToolbaTitle();
                v.displayDefaultToolbaSubitle();
            });
        }
    }

    public void fireCopyTextClick() {
        if (isNull(post)) {
            callView(IWallPostView::showPostNotReadyToast);
            return;
        }

        // Append post text
        StringBuilder builder = new StringBuilder();
        if (nonEmpty(post.getText())) {
            builder.append(post.getText()).append("\n");
        }

        // Append copies text if exists
        if (nonEmpty(post.getCopyHierarchy())) {
            for (Post copy : post.getCopyHierarchy()) {
                if (nonEmpty(copy.getText())) {
                    builder.append(copy.getText()).append("\n");
                }
            }
        }

        callView(v -> v.copyTextToClipboard(builder.toString()));
    }

    public void fireHasgTagClick(String hashTag) {
        callView(v -> v.goToNewsSearch(getAccountId(), hashTag));
    }
}