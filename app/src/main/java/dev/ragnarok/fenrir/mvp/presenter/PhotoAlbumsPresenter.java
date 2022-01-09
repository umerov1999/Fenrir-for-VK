package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.findIndexById;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IPhotosInteractor;
import dev.ragnarok.fenrir.domain.IUtilsInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.fragment.VKPhotoAlbumsFragment;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoAlbumEditor;
import dev.ragnarok.fenrir.model.SimplePrivacy;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IPhotoAlbumsView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class PhotoAlbumsPresenter extends AccountDependencyPresenter<IPhotoAlbumsView> {

    private final IPhotosInteractor photosInteractor;
    private final IOwnersRepository ownersRepository;
    private final IUtilsInteractor utilsInteractor;
    private final int mOwnerId;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private Owner mOwner;
    private String mAction;
    private ArrayList<PhotoAlbum> mData;
    private boolean endOfContent;
    private boolean netLoadingNow;

    public PhotoAlbumsPresenter(int accountId, int ownerId, @Nullable AdditionalParams params, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        ownersRepository = Repository.INSTANCE.getOwners();
        photosInteractor = InteractorFactory.createPhotosInteractor();
        utilsInteractor = InteractorFactory.createUtilsInteractor();

        mOwnerId = ownerId;

        //do restore this

        if (Objects.nonNull(params)) {
            mAction = params.getAction();
        }

        if (Objects.isNull(mOwner) && Objects.nonNull(params)) {
            mOwner = params.getOwner();
        }

        if (Objects.isNull(mData)) {
            mData = new ArrayList<>();

            loadAllFromDb();
            refreshFromNet(0);
        }

        if (Objects.isNull(mOwner) && !isMy()) {
            loadOwnerInfo();
        }
    }

    public void fireScrollToEnd() {
        if (!netLoadingNow && nonEmpty(mData) && !endOfContent) {
            int reserved = (Settings.get().other().getLocalServer().enabled && getAccountId() == mOwnerId) ? 2 : 1;
            refreshFromNet(Math.max(0, mData.size() - reserved));
        }
    }

    @Override
    public void onGuiCreated(@NonNull IPhotoAlbumsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mData);

        resolveDrawerPhotoSection();
        resolveProgressView();
        resolveSubtitleView();
        resolveCreateAlbumButtonVisibility();
    }

    private void resolveDrawerPhotoSection() {
        callView(v -> v.seDrawertPhotoSectionActive(isMy()));
    }

    private boolean isMy() {
        return mOwnerId == getAccountId();
    }

    private void loadOwnerInfo() {
        if (isMy()) {
            return;
        }

        int accountId = getAccountId();
        appendDisposable(ownersRepository.getBaseOwnerInfo(accountId, mOwnerId, IOwnersRepository.MODE_ANY)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onOwnerInfoReceived, this::onOwnerGetError));
    }

    private void onOwnerGetError(Throwable t) {
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onOwnerInfoReceived(Owner owner) {
        mOwner = owner;
        resolveSubtitleView();
        resolveCreateAlbumButtonVisibility();
    }

    private void refreshFromNet(int offset) {
        netLoadingNow = true;
        resolveProgressView();

        int accountId = getAccountId();
        netDisposable.add(photosInteractor.getActualAlbums(accountId, mOwnerId, 50, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(albums -> onActualAlbumsReceived(offset, albums), this::onActualAlbumsGetError));
    }

    private void onActualAlbumsGetError(Throwable t) {
        netLoadingNow = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveProgressView();
    }

    private void onActualAlbumsReceived(int offset, List<PhotoAlbum> albums) {
        // reset cache loading
        cacheDisposable.clear();
        endOfContent = albums.isEmpty() || mData.contains(albums.get(0)) && albums.size() < 50;

        netLoadingNow = false;

        if (offset == 0) {
            mData.clear();
            mData.addAll(albums);
            callView(IPhotoAlbumsView::notifyDataSetChanged);
        } else {
            int startSize = mData.size();
            int size = 0;
            for (PhotoAlbum i : albums) {
                if (mData.contains(i)) {
                    continue;
                }
                size++;
                mData.add(i);
            }
            int finalSize = size;
            callView(view -> view.notifyDataAdded(startSize, finalSize));
        }

        resolveProgressView();
    }

    private void loadAllFromDb() {

        int accountId = getAccountId();
        cacheDisposable.add(photosInteractor.getCachedAlbums(accountId, mOwnerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, t -> {/*ignored*/}));
    }

    private void onCachedDataReceived(List<PhotoAlbum> albums) {

        mData.clear();
        mData.addAll(albums);

        safeNotifyDatasetChanged();
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void resolveProgressView() {
        callView(v -> v.displayLoading(netLoadingNow));
    }


    private void safeNotifyDatasetChanged() {
        callView(IPhotoAlbumsView::notifyDataSetChanged);
    }

    private void resolveSubtitleView() {
        callView(v -> v.setToolbarSubtitle(Objects.isNull(mOwner) || isMy() ? null : mOwner.getFullName()));
    }

    private void doAlbumRemove(@NonNull PhotoAlbum album) {
        int accountId = getAccountId();
        int albumId = album.getId();
        int ownerId = album.getOwnerId();

        appendDisposable(photosInteractor.removedAlbum(accountId, album.getOwnerId(), album.getId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onAlbumRemoved(albumId, ownerId), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onAlbumRemoved(int albumId, int ownerId) {
        int index = findIndexById(mData, albumId, ownerId);
        if (index != -1) {
            callView(view -> view.notifyItemRemoved(index));
        }
    }

    public void fireCreateAlbumClick() {
        callView(v -> v.goToAlbumCreation(getAccountId(), mOwnerId));
    }

    public void fireAlbumClick(PhotoAlbum album) {
        if (VKPhotoAlbumsFragment.ACTION_SELECT_ALBUM.equals(mAction)) {
            callView(v -> v.doSelection(album));
        } else {
            callView(v -> v.openAlbum(getAccountId(), album, mOwner, mAction));
        }
    }

    public boolean fireAlbumLongClick(PhotoAlbum album) {
        if (canDeleteOrEdit(album)) {
            callView(v -> v.showAlbumContextMenu(album));
            return true;
        }

        return false;
    }

    private boolean isAdmin() {
        return mOwner instanceof Community && ((Community) mOwner).isAdmin();
    }

    private boolean canDeleteOrEdit(@NonNull PhotoAlbum album) {
        return !album.isSystem() && (isMy() || isAdmin());
    }

    private void resolveCreateAlbumButtonVisibility() {
        boolean mustBeVisible = isMy() || isAdmin();
        callView(v -> v.setCreateAlbumFabVisible(mustBeVisible));
    }

    public void fireAllComments() {
        callView(v -> v.goToPhotoComments(getAccountId(), mOwnerId));
    }

    public void fireRefresh() {
        cacheDisposable.clear();

        netDisposable.clear();
        netLoadingNow = false;

        refreshFromNet(0);
    }

    public void fireAlbumDeletingConfirmed(PhotoAlbum album) {
        doAlbumRemove(album);
    }

    public void fireAlbumDeleteClick(PhotoAlbum album) {
        callView(v -> v.showDeleteConfirmDialog(album));
    }

    public void fireAlbumEditClick(PhotoAlbum album) {
        @SuppressLint("UseSparseArrays")
        Map<Integer, SimplePrivacy> privacies = new HashMap<>();

        privacies.put(0, album.getPrivacyView());
        privacies.put(1, album.getPrivacyComment());

        int accountId = getAccountId();

        appendDisposable(utilsInteractor
                .createFullPrivacies(accountId, privacies)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(full -> {
                    PhotoAlbumEditor editor = PhotoAlbumEditor.create()
                            .setPrivacyView(full.get(0))
                            .setPrivacyComment(full.get(1))
                            .setTitle(album.getTitle())
                            .setDescription(album.getDescription())
                            .setCommentsDisabled(album.isCommentsDisabled())
                            .setUploadByAdminsOnly(album.isUploadByAdminsOnly());

                    callView(v -> v.goToAlbumEditing(getAccountId(), album, editor));
                }, Analytics::logUnexpectedError));
    }

    public static class AdditionalParams {

        private Owner owner;
        private String action;

        private Owner getOwner() {
            return owner;
        }

        public AdditionalParams setOwner(Owner owner) {
            this.owner = owner;
            return this;
        }

        private String getAction() {
            return action;
        }

        public AdditionalParams setAction(String action) {
            this.action = action;
            return this;
        }
    }
}