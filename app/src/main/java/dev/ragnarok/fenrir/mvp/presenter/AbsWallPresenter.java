package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.RxUtils.dummy;
import static dev.ragnarok.fenrir.util.RxUtils.ignore;
import static dev.ragnarok.fenrir.util.Utils.findIndexByPredicate;
import static dev.ragnarok.fenrir.util.Utils.findInfoByPredicate;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.intValueNotIn;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VkApiProfileInfo;
import dev.ragnarok.fenrir.db.model.PostUpdate;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.criteria.WallCriteria;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IWallView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public abstract class AbsWallPresenter<V extends IWallView> extends PlaceSupportPresenter<V> {

    private static final int COUNT = 20;
    private static final Comparator<Post> COMPARATOR = (rhs, lhs) -> {
        if (rhs.isPinned() == lhs.isPinned()) {
            return Integer.compare(lhs.getVkid(), rhs.getVkid());
        }

        return Boolean.compare(lhs.isPinned(), rhs.isPinned());
    };
    protected final int ownerId;
    protected final List<Post> wall;
    protected final List<Story> stories;
    private final IOwnersRepository ownersRepository;
    private final IWallsRepository walls;
    private final CompositeDisposable cacheCompositeDisposable = new CompositeDisposable();
    private final CompositeDisposable netCompositeDisposable = new CompositeDisposable();
    protected boolean endOfContent;
    private int wallFilter;
    private boolean requestNow;
    private int nowRequestOffset;
    private int nextOffset;
    private boolean actualDataReady;

    AbsWallPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.ownerId = ownerId;
        wall = new ArrayList<>();
        stories = new ArrayList<>();
        wallFilter = WallCriteria.MODE_ALL;
        walls = Repository.INSTANCE.getWalls();
        ownersRepository = Repository.INSTANCE.getOwners();

        loadWallCachedData();
        requestWall(0);

        if (!Settings.get().other().isDisable_history()) {
            appendDisposable(ownersRepository.getStory(accountId, accountId == ownerId ? null : ownerId)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(data -> {
                        if (!isEmpty(data)) {
                            stories.clear();
                            stories.addAll(data);
                            callView(view -> view.updateStory(stories));
                        }
                    }, t -> {
                    }));
        }

        appendDisposable(walls
                .observeMinorChanges()
                .filter(update -> update.getAccountId() == getAccountId() && update.getOwnerId() == getOwnerId())
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostChange));

        appendDisposable(walls
                .observeChanges()
                .filter(post -> post.getOwnerId() == ownerId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostChange));

        appendDisposable(walls
                .observePostInvalidation()
                .filter(pair -> pair.getOwnerId() == ownerId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(pair -> onPostInvalid(pair.getId())));
    }

    private static boolean isMatchFilter(Post post, int filter) {
        switch (filter) {
            case WallCriteria.MODE_ALL:
                return intValueNotIn(post.getPostType(), VKApiPost.Type.POSTPONE, VKApiPost.Type.SUGGEST);

            case WallCriteria.MODE_OWNER:
                return post.getAuthorId() == post.getOwnerId()
                        && intValueNotIn(post.getPostType(), VKApiPost.Type.POSTPONE, VKApiPost.Type.SUGGEST);

            case WallCriteria.MODE_SCHEDULED:
                return post.getPostType() == VKApiPost.Type.POSTPONE;

            case WallCriteria.MODE_SUGGEST:
                return post.getPostType() == VKApiPost.Type.SUGGEST;
        }

        throw new IllegalArgumentException("Unknown filter");
    }

    public void searchStory(boolean ByName) {
        throw new IllegalArgumentException("Unknown story search");
    }

    public List<Story> getStories() {
        return stories;
    }

    private void onPostInvalid(int postVkid) {
        int index = findIndexByPredicate(wall, post -> post.getVkid() == postVkid);

        if (index != -1) {
            wall.remove(index);
            callView(v -> v.notifyWallItemRemoved(index));
        }
    }

    private void onPostChange(Post post) {
        Pair<Integer, Post> found = findInfoByPredicate(wall, p -> p.getVkid() == post.getVkid());

        if (!isMatchFilter(post, wallFilter)) {
            // например, при публикации предложенной записи. Надо ли оно тут ?

            /*if (nonNull(found)) {
                int index = found.getFirst();
                wall.remove(index);

                if(isGuiReady()){
                    callView(v -> v.notifyWallItemRemoved(index);
                }
            }*/

            return;
        }

        if (nonNull(found)) {
            int index = found.getFirst();
            wall.set(index, post);
            callView(view -> view.notifyWallItemChanged(index));
        } else {
            int targetIndex;

            if (!post.isPinned() && wall.size() > 0 && wall.get(0).isPinned()) {
                targetIndex = 1;
            } else {
                targetIndex = 0;
            }

            wall.add(targetIndex, post);
            callView(view -> view.notifyWallDataAdded(targetIndex, 1));
        }
    }

    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void onGuiCreated(@NonNull V viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayWallData(wall);
        viewHost.updateStory(stories);
        resolveLoadMoreFooterView();
    }

    private void loadWallCachedData() {
        int accountId = getAccountId();

        cacheCompositeDisposable.add(walls.getCachedWall(accountId, ownerId, wallFilter)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, Analytics::logUnexpectedError));
    }

    private void onCachedDataReceived(List<Post> posts) {
        wall.clear();
        wall.addAll(posts);
        actualDataReady = false;

        callView(IWallView::notifyWallDataSetChanged);
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    @Override
    public void onDestroyed() {
        cacheCompositeDisposable.dispose();
        super.onDestroyed();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(requestNow && nowRequestOffset == 0));
    }

    private void safeNotifyWallDataSetChanged() {
        callView(IWallView::notifyWallDataSetChanged);
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;

        resolveRefreshingView();
        resolveLoadMoreFooterView();
    }

    private void setNowLoadingOffset(int offset) {
        nowRequestOffset = offset;
    }

    private void requestWall(int offset) {
        setNowLoadingOffset(offset);
        setRequestNow(true);

        int accountId = getAccountId();
        int nextOffset = offset + COUNT;
        boolean append = offset > 0;

        netCompositeDisposable.add(walls.getWall(accountId, ownerId, offset, COUNT, wallFilter)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(posts -> onActualDataReceived(nextOffset, posts, append), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable throwable) {
        setRequestNow(false);
        callView(v -> showError(v, getCauseIfRuntime(throwable)));
    }

    private boolean isExist(@NonNull Post post) {
        for (Post i : wall) {
            if (i.getOwnerId() == post.getOwnerId() && i.getVkid() == post.getVkid())
                return true;
        }
        return false;
    }

    private int addAll(@NonNull List<Post> posts) {
        int s = 0;
        for (Post i : posts) {
            if (!isExist(i)) {
                wall.add(i);
                s++;
            }
        }
        return s;
    }

    private void onActualDataReceived(int nextOffset, List<Post> posts, boolean append) {
        cacheCompositeDisposable.clear();

        actualDataReady = true;
        this.nextOffset = nextOffset;
        endOfContent = posts.isEmpty();

        if (nonEmpty(posts)) {
            if (append) {
                int sizeBefore = wall.size();
                int sz = addAll(posts);
                callView(view -> view.notifyWallDataAdded(sizeBefore, sz));
            } else {
                wall.clear();
                addAll(posts);
                callView(IWallView::notifyWallDataSetChanged);
            }
        }

        setRequestNow(false);
    }

    private void resolveLoadMoreFooterView() {
        @LoadMoreState
        int state;

        if (requestNow) {
            if (nowRequestOffset == 0) {
                state = LoadMoreState.INVISIBLE;
            } else {
                state = LoadMoreState.LOADING;
            }
        } else if (endOfContent) {
            state = LoadMoreState.END_OF_LIST;
        } else {
            state = LoadMoreState.CAN_LOAD_MORE;
        }

        callView(v -> v.setupLoadMoreFooter(state));
    }

    private boolean canLoadMore() {
        return !endOfContent && actualDataReady && nonEmpty(wall) && !requestNow;
    }

    private void requestNext() {
        requestWall(nextOffset);
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }

    public void fireLoadMoreClick() {
        if (canLoadMore()) {
            requestNext();
        }
    }

    public void fireOptionViewCreated(IWallView.IOptionView view) {
        view.setIsMy(getAccountId() == getOwnerId());
    }

    public void fireCreateClick() {
        callView(v -> v.goToPostCreation(getAccountId(), ownerId, EditingPostType.DRAFT));
    }

    private void fireEdit(Context context, VkApiProfileInfo p) {
        View root = View.inflate(context, R.layout.entry_info, null);
        ((TextInputEditText) root.findViewById(R.id.edit_first_name)).setText(p.first_name);
        ((TextInputEditText) root.findViewById(R.id.edit_last_name)).setText(p.last_name);
        ((TextInputEditText) root.findViewById(R.id.edit_maiden_name)).setText(p.maiden_name);
        ((TextInputEditText) root.findViewById(R.id.edit_screen_name)).setText(p.screen_name);
        ((TextInputEditText) root.findViewById(R.id.edit_bdate)).setText(p.bdate);
        ((TextInputEditText) root.findViewById(R.id.edit_home_town)).setText(p.home_town);
        ((Spinner) root.findViewById(R.id.sex)).setSelection(p.sex - 1);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.edit)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> appendDisposable(InteractorFactory.createAccountInteractor().saveProfileInfo(getAccountId(),
                        Utils.checkEditInfo(((TextInputEditText) root.findViewById(R.id.edit_first_name)).getEditableText().toString().trim(), p.first_name),
                        Utils.checkEditInfo(((TextInputEditText) root.findViewById(R.id.edit_last_name)).getEditableText().toString().trim(), p.last_name),
                        Utils.checkEditInfo(((TextInputEditText) root.findViewById(R.id.edit_maiden_name)).getEditableText().toString().trim(), p.maiden_name),
                        Utils.checkEditInfo(((TextInputEditText) root.findViewById(R.id.edit_screen_name)).getEditableText().toString().trim(), p.screen_name),
                        Utils.checkEditInfo(((TextInputEditText) root.findViewById(R.id.edit_bdate)).getEditableText().toString().trim(), p.bdate),
                        Utils.checkEditInfo(((TextInputEditText) root.findViewById(R.id.edit_home_town)).getEditableText().toString().trim(), p.home_town),
                        Utils.checkEditInfo(((Spinner) root.findViewById(R.id.sex)).getSelectedItemPosition() + 1, p.sex))
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(t -> {
                            switch (t) {
                                case 0:
                                    CustomToast.CreateCustomToast(context).showToastError(R.string.not_changed);
                                    break;
                                case 1:
                                    CustomToast.CreateCustomToast(context).showToastSuccessBottom(R.string.success);
                                    break;
                                case 2:
                                    CustomToast.CreateCustomToast(context).showToastBottom(R.string.later);
                                    break;
                            }
                        }, t -> callView(v -> showError(v, t)))))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    public final void fireEdit(Context context) {
        appendDisposable(InteractorFactory.createAccountInteractor().getProfileInfo(getAccountId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> fireEdit(context, t), v -> {
                }));
    }

    public final void fireRefresh() {
        netCompositeDisposable.clear();
        cacheCompositeDisposable.clear();

        requestWall(0);
        if (!Settings.get().other().isDisable_history()) {
            appendDisposable(ownersRepository.getStory(getAccountId(), getAccountId() == ownerId ? null : ownerId)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(data -> {
                        if (!isEmpty(data)) {
                            stories.clear();
                            stories.addAll(data);
                            callView(view -> view.updateStory(stories));
                        }
                    }, t -> {
                    }));
        }

        onRefresh();
    }

    public void fireShowQR(@NonNull Context context) {
        Bitmap qr = Utils.generateQR("https://vk.com/" + (ownerId < 0 ? "club" : "id") + Math.abs(ownerId), context);
        View view = LayoutInflater.from(context).inflate(R.layout.qr, null);
        ShapeableImageView imageView = view.findViewById(R.id.qr);
        imageView.setImageBitmap(qr);
        new MaterialAlertDialogBuilder(context)
                .setCancelable(true)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.save, (dialogInterface, i) -> {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    OutputStream fOutputStream;
                    File file = new File(path, "qr_fenrir_" + (ownerId < 0 ? "club" : "id") + Math.abs(ownerId) + ".png");
                    try {
                        fOutputStream = new FileOutputStream(file);
                        assert qr != null;
                        qr.compress(Bitmap.CompressFormat.PNG, 100, fOutputStream);

                        fOutputStream.flush();
                        fOutputStream.close();
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        CustomToast.CreateCustomToast(context).showToast(R.string.success);
                    } catch (IOException e) {
                        e.printStackTrace();
                        CustomToast.CreateCustomToast(context).showToastError("Save Failed");
                    }
                })
                .setIcon(R.drawable.qr_code)
                .setTitle(R.string.show_qr)
                .setView(view)
                .show();
    }

    protected void onRefresh() {

    }

    public void firePostBodyClick(Post post) {
        if (Utils.intValueIn(post.getPostType(), VKApiPost.Type.SUGGEST, VKApiPost.Type.POSTPONE)) {
            callView(v -> v.openPostEditor(getAccountId(), post));
            return;
        }

        firePostClick(post);
    }

    public void firePostRestoreClick(Post post) {
        appendDisposable(walls.restore(getAccountId(), post.getOwnerId(), post.getVkid())
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
        if (Settings.get().other().isDisable_likes() || Utils.isHiddenAccount(getAccountId())) {
            return;
        }
        int accountId = getAccountId();

        appendDisposable(walls.like(accountId, post.getOwnerId(), post.getVkid(), !post.isUserLikes())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(ignore(), t -> callView(v -> showError(v, t))));
    }

    int getWallFilter() {
        return wallFilter;
    }

    boolean changeWallFilter(int mode) {
        boolean changed = mode != wallFilter;

        wallFilter = mode;

        if (changed) {
            cacheCompositeDisposable.clear();
            netCompositeDisposable.clear();

            loadWallCachedData();
            requestWall(0);
        }

        return changed;
    }

    boolean isMyWall() {
        return getAccountId() == ownerId;
    }

    private void onPostChange(PostUpdate update) {
        boolean pinStateChanged = nonNull(update.getPinUpdate());

        int index = findByVkid(update.getOwnerId(), update.getPostId());

        if (index != -1) {
            Post post = wall.get(index);

            if (nonNull(update.getLikeUpdate())) {
                post.setLikesCount(update.getLikeUpdate().getCount());
                post.setUserLikes(update.getLikeUpdate().isLiked());
            }

            if (nonNull(update.getDeleteUpdate())) {
                post.setDeleted(update.getDeleteUpdate().isDeleted());
            }

            if (nonNull(update.getPinUpdate())) {
                for (Post p : wall) {
                    p.setPinned(false);
                }

                post.setPinned(update.getPinUpdate().isPinned());
            }

            if (pinStateChanged) {
                Collections.sort(wall, COMPARATOR);
                safeNotifyWallDataSetChanged();
            } else {
                callView(v -> v.notifyWallItemChanged(index));
            }
        }
    }

    private int findByVkid(int ownerId, int vkid) {
        return Utils.indexOf(wall, post -> post.getOwnerId() == ownerId && post.getVkid() == vkid);
    }

    public void fireCopyUrlClick() {
        callView(v -> v.copyToClipboard(getString(R.string.link), (isCommunity() ? "vk.com/club" : "vk.com/id") + Math.abs(ownerId)));
    }

    public void fireCopyIdClick() {
        callView(v -> v.copyToClipboard(getString(R.string.id), String.valueOf(ownerId)));
    }

    public abstract void fireAddToShortcutClick();

    public boolean isCommunity() {
        return ownerId < 0;
    }

    public void fireSearchClick() {
        callView(v -> v.goToWallSearch(getAccountId(), getOwnerId()));
    }

    public void openConversationAttachments() {
        callView(v -> v.goToConversationAttachments(getAccountId(), getOwnerId()));
    }

    public void fireButtonRemoveClick(Post post) {
        appendDisposable(walls.delete(getAccountId(), ownerId, post.getVkid())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(dummy(), t -> callView(v -> showError(v, t))));
    }
}
