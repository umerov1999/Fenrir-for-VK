package dev.ragnarok.fenrir.mvp.presenter.photo;

import static dev.ragnarok.fenrir.util.Utils.findIndexById;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.squareup.picasso3.BitmapTarget;
import com.squareup.picasso3.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.App;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiPhotoTags;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IPhotosInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.model.AccessIdPair;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.FunctionSource;
import dev.ragnarok.fenrir.model.IOwnersBundle;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IPhotoPagerView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;

public class PhotoPagerPresenter extends AccountDependencyPresenter<IPhotoPagerView> {

    private static final String SAVE_INDEX = "save-index";
    private static final String SAVE_DATA = "save-data";
    final IPhotosInteractor photosInteractor;
    private final boolean read_only;
    private final Context context;
    ArrayList<Photo> mPhotos;
    private int mCurrentIndex;
    private boolean mLoadingNow;
    private boolean mFullScreen;

    PhotoPagerPresenter(@NonNull ArrayList<Photo> initialData, int accountId, boolean read_only, Context context, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        photosInteractor = InteractorFactory.createPhotosInteractor();
        this.read_only = read_only;
        this.context = context;

        if (Objects.nonNull(savedInstanceState)) {
            mCurrentIndex = savedInstanceState.getInt(SAVE_INDEX);
        }

        initPhotosData(initialData, savedInstanceState);

        AssertUtils.requireNonNull(mPhotos, "'mPhotos' not initialized");
    }

    public void close() {
        callView(IPhotoPagerView::closeOnly);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_INDEX, mCurrentIndex);
        savePhotosState(outState);
    }

    void savePhotosState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(SAVE_DATA, mPhotos);
    }

    void initPhotosData(@NonNull ArrayList<Photo> initialData, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mPhotos = initialData;
        } else {
            mPhotos = savedInstanceState.getParcelableArrayList(SAVE_DATA);
        }
    }

    void changeLoadingNowState(boolean loading) {
        mLoadingNow = loading;
        resolveLoadingView();
    }

    private void resolveLoadingView() {
        callView(v -> v.displayPhotoListLoading(mLoadingNow));
    }

    void refreshPagerView() {
        callView(v -> v.displayPhotos(mPhotos, mCurrentIndex));
    }

    @NonNull
    protected ArrayList<Photo> getData() {
        return mPhotos;
    }

    protected void setData(List<Photo> Photos) {
        mPhotos = (ArrayList<Photo>) Photos;
    }

    @Override
    public void onViewHostAttached(@NonNull IPhotoPagerView viewHost) {
        super.onViewHostAttached(viewHost);
        resolveOptionMenu();
    }

    private void resolveOptionMenu() {
        callView(v -> v.setupOptionMenu(canSaveYourself(), canDelete()));
    }

    private boolean canDelete() {
        return hasPhotos() && getCurrent().getOwnerId() == getAccountId();
    }

    private boolean canSaveYourself() {
        return hasPhotos() && getCurrent().getOwnerId() != getAccountId();
    }

    @Override
    public void onGuiCreated(@NonNull IPhotoPagerView viewHost) {
        super.onGuiCreated(viewHost);
        callView(v -> v.displayPhotos(mPhotos, mCurrentIndex));

        refreshInfoViews(true);
        resolveRestoreButtonVisibility();
        resolveToolbarVisibility();
        resolveButtonsBarVisible();
        resolveLoadingView();
    }

    public final void firePageSelected(int position) {
        int old = mCurrentIndex;
        changePageTo(position);
        afterPageChangedFromUi(old, position);
    }

    protected void afterPageChangedFromUi(int oldPage, int newPage) {

    }

    void changePageTo(int position) {
        if (mCurrentIndex == position) return;

        mCurrentIndex = position;
        onPositionChanged();
    }

    private void resolveLikeView() {
        if (hasPhotos()) {
            if (read_only) {
                callView(v -> v.setupLikeButton(false, false, 0));
                return;
            }
            Photo photo = getCurrent();
            callView(v -> v.setupLikeButton(true, photo.isUserLikes(), photo.getLikesCount()));
        }
    }

    private void resolveWithUserView() {
        if (hasPhotos()) {
            Photo photo = getCurrent();
            callView(v -> v.setupWithUserButton(photo.getTagsCount()));
        }
    }

    private void resolveShareView() {
        if (hasPhotos()) {
            callView(v -> v.setupShareButton(!read_only));
        }
    }

    private void resolveCommentsView() {
        if (hasPhotos()) {
            Photo photo = getCurrent();
            if (read_only) {
                callView(v -> v.setupCommentsButton(false, 0));
                return;
            }
            //boolean visible = photo.isCanComment() || photo.getCommentsCount() > 0;
            callView(v -> v.setupCommentsButton(true, photo.getCommentsCount()));
        }
    }

    int count() {
        return mPhotos.size();
    }

    void resolveToolbarTitleSubtitleView() {
        if (!hasPhotos()) return;

        String title = context.getString(R.string.image_number, mCurrentIndex + 1, count());
        callView(v -> v.setToolbarTitle(title));
        callView(v -> v.setToolbarSubtitle(getCurrent().getText()));
    }

    @NonNull
    private Photo getCurrent() {
        return mPhotos.get(mCurrentIndex);
    }

    private void onPositionChanged() {
        refreshInfoViews(true);
        resolveRestoreButtonVisibility();
        resolveOptionMenu();
    }

    private void showPhotoInfo(@NonNull Photo photo, PhotoAlbum album, IOwnersBundle bundle) {
        if (photo.getAlbumId() == -311) {
            return;
        }
        String album_info = (album == null ? context.getString(R.string.open_photo_album) : album.getDisplayTitle(context));
        String user = (photo.getOwnerId() >= 0 ? context.getString(R.string.goto_user) : context.getString(R.string.goto_community));
        if (bundle != null) {
            user = bundle.getById(photo.getOwnerId()).getFullName();
        }

        List<FunctionSource> buttons = new ArrayList<>(2);
        buttons.add(new FunctionSource(album_info, R.drawable.photo_album,
                () -> PlaceFactory.getVKPhotosAlbumPlace(getAccountId(), photo.getOwnerId(), photo.getAlbumId(), null).tryOpenWith(context)));

        buttons.add(new FunctionSource(user, R.drawable.person,
                () -> PlaceFactory.getOwnerWallPlace(getAccountId(), photo.getOwnerId(), null).tryOpenWith(context)));

        ButtonAdapter adapter = new ButtonAdapter(context, buttons);

        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.uploaded) + " " + AppTextUtils.getDateFromUnixTime(photo.getDate()))
                .setView(Utils.createAlertRecycleFrame(context, adapter, Utils.isEmpty(photo.getText()) ? null : context.getString(R.string.description_hint) + ": " + photo.getText(), getAccountId()))
                .setPositiveButton(R.string.button_ok, null)
                .setCancelable(true)
                .show();
    }

    private void getOwnerForPhoto(@NonNull Photo photo, PhotoAlbum album) {
        appendDisposable(Repository.INSTANCE.getOwners().findBaseOwnersDataAsBundle(getAccountId(), Collections.singleton(photo.getOwnerId()), IOwnersRepository.MODE_ANY)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> showPhotoInfo(photo, album, t), i -> showPhotoInfo(photo, album, null)));
    }

    public void fireInfoButtonClick() {
        Photo photo = getCurrent();
        appendDisposable(photosInteractor.getAlbumById(getAccountId(), photo.getOwnerId(), photo.getAlbumId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> getOwnerForPhoto(photo, t), i -> getOwnerForPhoto(photo, null)));
    }

    public void fireShareButtonClick() {
        Photo current = getCurrent();
        callView(v -> v.sharePhoto(getAccountId(), current));
    }

    public void firePostToMyWallClick() {
        Photo photo = getCurrent();
        callView(v -> v.postToMyWall(photo, getAccountId()));
    }

    void refreshInfoViews(boolean need_update) {
        resolveToolbarTitleSubtitleView();
        resolveLikeView();
        resolveWithUserView();
        resolveShareView();
        resolveCommentsView();
        resolveOptionMenu();
        if (need_update && need_update_info() && hasPhotos()) {
            Photo photo = getCurrent();
            if (photo.getAlbumId() != -311) {
                appendDisposable(photosInteractor.getPhotosByIds(getAccountId(),
                        Collections.singleton(new AccessIdPair(photo.getId(), photo.getOwnerId(), photo.getAccessKey())))
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(t -> {
                            if (t.get(0).getId() == photo.getId()) {
                                Photo ne = t.get(0);
                                if (ne.getAccessKey() == null) {
                                    ne.setAccessKey(photo.getAccessKey());
                                }
                                mPhotos.set(getCurrentIndex(), ne);
                                refreshInfoViews(false);
                            }
                        }, throwable -> {
                        }));
            }
        }
    }

    protected boolean need_update_info() {
        return false;
    }

    public void fireLikeClick() {
        addOrRemoveLike();
    }

    private void addOrRemoveLike() {
        if (Settings.get().other().isDisable_likes() || Utils.isHiddenAccount(getAccountId())) {
            return;
        }
        Photo photo = getCurrent();

        int ownerId = photo.getOwnerId();
        int photoId = photo.getId();
        int accountId = getAccountId();
        boolean add = !photo.isUserLikes();

        appendDisposable(photosInteractor.like(accountId, ownerId, photoId, add, photo.getAccessKey())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(count -> interceptLike(ownerId, photoId, count, add), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onDeleteOrRestoreResult(int photoId, int ownerId, boolean deleted) {
        int index = findIndexById(mPhotos, photoId, ownerId);

        if (index != -1) {
            Photo photo = mPhotos.get(index);
            photo.setDeleted(deleted);

            if (mCurrentIndex == index) {
                resolveRestoreButtonVisibility();
            }
        }
    }

    private void interceptLike(int ownerId, int photoId, int count, boolean userLikes) {
        for (Photo photo : mPhotos) {
            if (photo.getId() == photoId && photo.getOwnerId() == ownerId) {
                photo.setLikesCount(count);
                photo.setUserLikes(userLikes);
                resolveLikeView();
                break;
            }
        }
    }

    public void fireSaveOnDriveClick() {
        if (!AppPerms.hasReadWriteStoragePermission(App.getInstance())) {
            callView(IPhotoPagerView::requestWriteToExternalStoragePermission);
            return;
        }

        doSaveOnDrive();
    }

    private void doSaveOnDrive() {
        File dir = new File(Settings.get().other().getPhotoDir());
        if (!dir.isDirectory()) {
            boolean created = dir.mkdirs();
            if (!created) {
                callView(v -> v.showError("Can't create directory " + dir));
                return;
            }
        } else
            dir.setLastModified(Calendar.getInstance().getTime().getTime());

        Photo photo = getCurrent();

        if (photo.getAlbumId() == -311) {
            String path = photo.getText();
            int ndx = path.indexOf('/');
            if (ndx != -1) {
                path = path.substring(0, ndx);
            }
            DownloadResult(path, dir, photo);
        } else {
            appendDisposable(OwnerInfo.getRx(context, getAccountId(), photo.getOwnerId())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(userInfo -> DownloadResult(DownloadWorkUtils.makeLegalFilename(userInfo.getOwner().getFullName(), null), dir, photo), throwable -> DownloadResult(null, dir, photo)));
        }
    }

    private String transform_owner(int owner_id) {
        if (owner_id < 0)
            return "club" + Math.abs(owner_id);
        else
            return "id" + owner_id;
    }

    private void DownloadResult(String Prefix, File dir, Photo photo) {
        if (Prefix != null && Settings.get().other().isPhoto_to_user_dir()) {
            File dir_final = new File(dir.getAbsolutePath() + "/" + Prefix);
            if (!dir_final.isDirectory()) {
                boolean created = dir_final.mkdirs();
                if (!created) {
                    callView(v -> v.showError("Can't create directory " + dir_final));
                    return;
                }
            } else
                dir_final.setLastModified(Calendar.getInstance().getTime().getTime());
            dir = dir_final;
        }
        String url = photo.getUrlForSize(PhotoSize.W, true);
        DownloadWorkUtils.doDownloadPhoto(context, url, dir.getAbsolutePath(), (Prefix != null ? (Prefix + "_") : "") + transform_owner(photo.getOwnerId()) + "_" + photo.getId());
    }

    public void fireSaveYourselfClick() {
        Photo photo = getCurrent();
        if (photo.getAlbumId() == -311) {
            return;
        }
        int accountId = getAccountId();

        appendDisposable(photosInteractor.copy(accountId, photo.getOwnerId(), photo.getId(), photo.getAccessKey())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(ignored -> onPhotoCopied(), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    public void fireDetectQRClick(Activity context) {
        PicassoInstance.with().load(getCurrent().getUrlForSize(PhotoSize.W, false))
                .into(new BitmapTarget() {
                    @Override
                    public void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull Picasso.LoadedFrom from) {
                        String data = IntentIntegrator.decodeFromBitmap(bitmap);
                        new MaterialAlertDialogBuilder(context)
                                .setIcon(R.drawable.qr_code)
                                .setMessage(data)
                                .setTitle(getString(R.string.scan_qr))
                                .setPositiveButton(R.string.open, (dialog, which) -> LinkHelper.openUrl(context, getAccountId(), data))
                                .setNeutralButton(R.string.copy_text, (dialog, which) -> {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("response", data);
                                    clipboard.setPrimaryClip(clip);
                                    CustomToast.CreateCustomToast(context).showToast(R.string.copied_to_clipboard);
                                })
                                .setCancelable(true)
                                .show();
                    }

                    @Override
                    public void onBitmapFailed(@NonNull Exception t, Drawable errorDrawable) {
                        CustomToast.CreateCustomToast(context).showToastError(t.getLocalizedMessage());
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    private void onPhotoCopied() {
        callView(v -> v.showToast(R.string.photo_saved_yourself, true));
    }

    public void fireDeleteClick() {
        delete();
    }

    public void fireWriteExternalStoragePermissionResolved() {
        if (AppPerms.hasReadWriteStoragePermission(App.getInstance())) {
            doSaveOnDrive();
        }
    }

    public void fireButtonRestoreClick() {
        restore();
    }

    private void resolveRestoreButtonVisibility() {
        callView(v -> v.setButtonRestoreVisible(hasPhotos() && getCurrent().isDeleted()));
    }

    private void restore() {
        deleteOrRestore(false);
    }

    private void deleteOrRestore(boolean detele) {
        Photo photo = getCurrent();
        if (photo.getAlbumId() == -311) {
            return;
        }
        int photoId = photo.getId();
        int ownerId = photo.getOwnerId();
        int accountId = getAccountId();

        Completable completable;
        if (detele) {
            completable = photosInteractor.deletePhoto(accountId, ownerId, photoId);
        } else {
            completable = photosInteractor.restorePhoto(accountId, ownerId, photoId);
        }

        appendDisposable(completable.compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onDeleteOrRestoreResult(photoId, ownerId, detele), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void delete() {
        deleteOrRestore(true);
    }

    public void fireCommentsButtonClick() {
        Photo photo = getCurrent();
        callView(v -> v.goToComments(getAccountId(), Commented.from(photo)));
    }

    public void fireWithUserClick() {
        Photo photo = getCurrent();
        appendDisposable(
                InteractorFactory.createPhotosInteractor().getTags(getAccountId(), photo.getOwnerId(), photo.getId(), photo.getAccessKey())
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(userInfo -> {
                            List<FunctionSource> buttons = new ArrayList<>(userInfo.size());
                            for (VKApiPhotoTags i : userInfo) {
                                if (i.user_id != 0) {
                                    buttons.add(new FunctionSource(i.tagged_name, R.drawable.person, () -> PlaceFactory.getOwnerWallPlace(getAccountId(), i.user_id, null).tryOpenWith(context)));
                                } else {
                                    buttons.add(new FunctionSource(i.tagged_name, R.drawable.pencil, () -> {
                                    }));
                                }
                            }
                            ButtonAdapter adapter = new ButtonAdapter(context, buttons);
                            new MaterialAlertDialogBuilder(context)
                                    .setTitle(R.string.has_tags)
                                    .setPositiveButton(R.string.button_ok, null)
                                    .setCancelable(true)
                                    .setView(Utils.createAlertRecycleFrame(context, adapter, null, getAccountId()))
                                    .show();
                        }, throwable -> callView(v -> showError(v, throwable))));
    }

    private boolean hasPhotos() {
        return !Utils.safeIsEmpty(mPhotos);
    }

    public void firePhotoTap() {
        if (!hasPhotos()) return;

        mFullScreen = !mFullScreen;

        resolveToolbarVisibility();
        resolveButtonsBarVisible();
    }

    void resolveButtonsBarVisible() {
        callView(v -> v.setButtonsBarVisible(hasPhotos() && !mFullScreen));
    }

    void resolveToolbarVisibility() {
        callView(v -> v.setToolbarVisible(hasPhotos() && !mFullScreen));
    }

    int getCurrentIndex() {
        return mCurrentIndex;
    }

    void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
    }

    public void fireLikeLongClick() {
        if (!hasPhotos()) return;

        Photo photo = getCurrent();
        callView(v -> v.goToLikesList(getAccountId(), photo.getOwnerId(), photo.getId()));
    }

    private static class ButtonAdapter extends RecyclerView.Adapter<ButtonHolder> {
        private final Context context;
        private final List<FunctionSource> items;

        public ButtonAdapter(Context context, List<FunctionSource> items) {
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public ButtonHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ButtonHolder(LayoutInflater.from(context).inflate(R.layout.item_button, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ButtonHolder holder, int position) {
            FunctionSource source = items.get(position);
            holder.button.setText(source.getTitle(context));
            holder.button.setIconResource(source.getIcon());
            holder.button.setOnClickListener(v -> source.Do());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class ButtonHolder extends RecyclerView.ViewHolder {
        final MaterialButton button;

        public ButtonHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.item_button_function);
        }
    }
}
