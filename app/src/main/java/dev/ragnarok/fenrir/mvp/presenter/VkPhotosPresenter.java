package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.findIndexById;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.db.serialize.Serializers;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IPhotosInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.model.wrappers.SelectablePhotoWrapper;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.module.parcel.ParcelNative;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.IUploadManager;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadIntent;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.upload.UploadUtils;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class VkPhotosPresenter extends AccountDependencyPresenter<IVkPhotosView> {

    private static final String SAVE_ALBUM = "save-album";
    private static final String SAVE_OWNER = "save-owner";
    private static final int COUNT = 100;

    private final int ownerId;
    private final int albumId;

    private final IPhotosInteractor interactor;
    private final IOwnersRepository ownersRepository;
    private final IUploadManager uploadManager;

    private final List<SelectablePhotoWrapper> photos;
    private final List<Upload> uploads;

    private final UploadDestination destination;
    private final String action;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private PhotoAlbum album;
    private Owner owner;
    private List<String> mDownloads;
    private boolean requestNow;
    private boolean endOfContent;
    private boolean isShowBDate;
    private boolean invertPhotoRev;

    public VkPhotosPresenter(int accountId, int ownerId, int albumId, String action,
                             @Nullable Owner owner, @Nullable PhotoAlbum album, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.ownerId = ownerId;
        this.albumId = albumId;
        this.action = action;
        invertPhotoRev = Settings.get().other().isInvertPhotoRev();

        interactor = InteractorFactory.createPhotosInteractor();
        ownersRepository = Repository.INSTANCE.getOwners();
        uploadManager = Injection.provideUploadManager();

        destination = UploadDestination.forPhotoAlbum(albumId, ownerId);

        photos = new ArrayList<>();
        uploads = new ArrayList<>();

        if (isNull(savedInstanceState)) {
            this.album = album;
            this.owner = owner;
        } else {
            this.album = savedInstanceState.getParcelable(SAVE_ALBUM);
            ParcelableOwnerWrapper ownerWrapper = savedInstanceState.getParcelable(SAVE_OWNER);
            AssertUtils.requireNonNull(ownerWrapper);
            this.owner = ownerWrapper.get();
        }

        loadInitialData();

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadQueueAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsRemoved));

        appendDisposable(uploadManager.observeResults()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadResults));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        refreshOwnerInfoIfNeed();
        refreshAlbumInfoIfNeed();
    }

    private static List<SelectablePhotoWrapper> wrappersOf(List<Photo> photos) {
        List<SelectablePhotoWrapper> wrappers = new ArrayList<>(photos.size());
        for (Photo photo : photos) {
            wrappers.add(new SelectablePhotoWrapper(photo));
        }
        return wrappers;
    }

    public void togglePhotoInvert() {
        invertPhotoRev = !invertPhotoRev;
        Settings.get().other().setInvertPhotoRev(invertPhotoRev);
        fireRefresh();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_ALBUM, album);
        outState.putParcelable(SAVE_OWNER, new ParcelableOwnerWrapper(owner));
    }

    private void refreshOwnerInfoIfNeed() {
        int accountId = getAccountId();

        if (!isMy() && isNull(owner)) {
            appendDisposable(ownersRepository.getBaseOwnerInfo(accountId, ownerId, IOwnersRepository.MODE_NET)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onActualOwnerInfoReceived, RxUtils.ignore()));
        }
    }

    private void refreshAlbumInfoIfNeed() {
        int accountId = getAccountId();

        if (isNull(album)) {
            appendDisposable(interactor.getAlbumById(accountId, ownerId, albumId)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onAlbumInfoReceived, RxUtils.ignore()));
        }
    }

    private void onAlbumInfoReceived(PhotoAlbum album) {
        this.album = album;

        resolveToolbarView();

        if (!isSelectionMode()) {
            resolveButtonAddVisibility(true);
        }
    }

    private void onActualOwnerInfoReceived(Owner owner) {
        this.owner = owner;
        resolveButtonAddVisibility(true);
    }

    private void resolveToolbarView() {
        String ownerName = nonNull(owner) ? owner.getFullName() : null;

        callView(v -> {
            v.displayToolbarSubtitle(album, getString(R.string.photos_count, photos.size()));
            if (nonEmpty(ownerName)) {
                v.setToolbarTitle(ownerName);
            } else {
                v.displayDefaultToolbarTitle();
            }
        });
    }

    private void onUploadQueueAdded(List<Upload> added) {
        int startUploadSize = uploads.size();
        int count = 0;

        for (Upload upload : added) {
            if (destination.compareTo(upload.getDestination())) {
                uploads.add(upload);
                count++;
            }
        }

        if (count > 0) {
            int finalCount = count;
            callView(view -> view.notifyUploadAdded(startUploadSize, finalCount));
        }
    }

    private void onUploadsRemoved(int[] ids) {
        for (int id : ids) {
            int index = findIndexById(uploads, id);

            if (index != -1) {
                uploads.remove(index);
                callView(view -> view.notifyUploadRemoved(index));
            }
        }
    }

    private void onUploadResults(Pair<Upload, UploadResult<?>> pair) {
        if (destination.compareTo(pair.getFirst().getDestination())) {
            Photo photo = (Photo) pair.getSecond().getResult();
            photos.add(0, new SelectablePhotoWrapper(photo));
            callView(view -> view.notifyPhotosAdded(0, 1));
        }
    }

    private void onUploadStatusUpdate(Upload upload) {
        int index = findIndexById(uploads, upload.getId());
        if (index != -1) {
            callView(view -> view.notifyUploadItemChanged(index));
        }
    }

    private void onUploadProgressUpdate(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            int index = findIndexById(uploads, update.getId());
            if (index != -1) {
                callView(view -> view.notifyUploadProgressChanged(update.getId(), update.getProgress()));
            }
        }
    }

    public boolean getIsShowBDate() {
        return isShowBDate;
    }

    public void doToggleDate() {
        isShowBDate = !isShowBDate;
        callView(v -> v.onToggleShowDate(isShowBDate));
        callView(IVkPhotosView::notifyDataSetChanged);
    }

    @Override
    public void onGuiCreated(@NonNull IVkPhotosView view) {
        super.onGuiCreated(view);
        view.displayData(photos, uploads);
        view.onToggleShowDate(isShowBDate);
        resolveButtonAddVisibility(false);
        resolveToolbarView();
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayRefreshing(requestNow));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        callView(v -> v.setDrawerPhotosSelected(isMy()));
    }

    private void requestActualData(int offset) {
        setRequestNow(true);
        if (albumId != -9001 && albumId != -9000) {
            appendDisposable(interactor.get(getAccountId(), ownerId, albumId, COUNT, offset, !invertPhotoRev)
                    .map(t -> {
                        List<SelectablePhotoWrapper> wrap = wrappersOf(t);
                        if (!Utils.isEmpty(mDownloads)) {
                            for (SelectablePhotoWrapper i : wrap) {
                                i.setDownloaded(existPhoto(i.getPhoto()));
                            }
                        }
                        return wrap;
                    })
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(photos -> onActualPhotosReceived(offset, photos), this::onActualDataGetError));
        } else if (albumId == -9000) {
            appendDisposable(interactor.getUsersPhoto(getAccountId(), ownerId, 1, invertPhotoRev ? 1 : 0, offset, COUNT)
                    .map(t -> {
                        List<SelectablePhotoWrapper> wrap = wrappersOf(t);
                        if (!Utils.isEmpty(mDownloads)) {
                            for (SelectablePhotoWrapper i : wrap) {
                                i.setDownloaded(existPhoto(i.getPhoto()));
                            }
                        }
                        return wrap;
                    })
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(photos -> onActualPhotosReceived(offset, photos), this::onActualDataGetError));
        } else {
            appendDisposable(interactor.getAll(getAccountId(), ownerId, 1, 1, offset, COUNT)
                    .map(t -> {
                        List<SelectablePhotoWrapper> wrap = wrappersOf(t);
                        if (!Utils.isEmpty(mDownloads)) {
                            for (SelectablePhotoWrapper i : wrap) {
                                i.setDownloaded(existPhoto(i.getPhoto()));
                            }
                        }
                        return wrap;
                    })
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(photos -> onActualPhotosReceived(offset, photos), this::onActualDataGetError));
        }
    }

    private void onActualDataGetError(Throwable t) {
        callView(v -> showError(v, getCauseIfRuntime(t)));
        setRequestNow(false);
    }

    private void onActualPhotosReceived(int offset, List<SelectablePhotoWrapper> data) {
        cacheDisposable.clear();
        endOfContent = data.isEmpty();

        setRequestNow(false);

        if (offset == 0) {
            photos.clear();
            photos.addAll(data);
            callView(IVkPhotosView::notifyDataSetChanged);
        } else {
            int startSize = photos.size();
            photos.addAll(data);
            callView(view -> view.notifyPhotosAdded(startSize, data.size()));
        }
        resolveToolbarView();
    }

    private void loadInitialData() {
        int accountId = getAccountId();
        cacheDisposable.add(interactor.getAllCachedData(accountId, ownerId, albumId, invertPhotoRev)
                .zipWith(uploadManager.get(getAccountId(), destination), Pair.Companion::create)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onInitialDataReceived));
    }

    private String transform_owner(int owner_id) {
        if (owner_id < 0)
            return "club" + Math.abs(owner_id);
        else
            return "id" + owner_id;
    }

    private boolean existPhoto(Photo photo) {
        for (String i : mDownloads) {
            if (i.contains(transform_owner(photo.getOwnerId()) + "_" + photo.getId())) {
                return true;
            }
        }
        return false;
    }

    public void updateInfo(int position, long ptr) {
        List<Photo> p = ParcelNative.fromNative(ptr).readParcelableList(Photo.NativeCreator);
        photos.clear();
        photos.addAll(wrappersOf(p));
        photos.get(position).setCurrent(true);
        if (!Utils.isEmpty(mDownloads)) {
            for (SelectablePhotoWrapper i : photos) {
                i.setDownloaded(existPhoto(i.getPhoto()));
            }
        }
        callView(v -> {
            v.notifyDataSetChanged();
            v.scrollTo(position);
        });
    }

    private void onInitialDataReceived(Pair<List<Photo>, List<Upload>> data) {
        photos.clear();
        photos.addAll(wrappersOf(data.getFirst()));

        uploads.clear();
        uploads.addAll(data.getSecond());

        callView(IVkPhotosView::notifyDataSetChanged);
        resolveToolbarView();
        requestActualData(0);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        super.onDestroyed();
    }

    public void fireUploadRemoveClick(Upload o) {
        uploadManager.cancel(o.getId());
    }

    public void fireRefresh() {
        if (!requestNow) {
            requestActualData(0);
        }
    }

    public void fireScrollToEnd() {
        if (!requestNow && nonEmpty(photos) && !endOfContent) {
            requestActualData(photos.size());
        }
    }

    private boolean isMy() {
        return getAccountId() == ownerId;
    }

    private boolean isAdmin() {
        return owner instanceof Community && ((Community) owner).getAdminLevel() >= VKApiCommunity.AdminLevel.MODERATOR;
    }

    private boolean canUploadToAlbum() {
        // можно загружать,
        // 1 - альбом не системный ОБЯЗАТЕЛЬНО
        // 2 - если я админ группы
        // 3 - если альбом мой
        // 4 - если альбом принадлежит группе, но разрешено в него грузить
        return albumId >= 0 && (isAdmin() || isMy() || (nonNull(album) && album.isCanUpload()));
    }

    public void firePhotosForUploadSelected(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), destination, photos, size, true);
        uploadManager.enqueue(intents);
    }

    public void firePhotoSelectionChanged(SelectablePhotoWrapper wrapper) {
        wrapper.setSelected(!wrapper.isSelected());
        onPhotoSelected(wrapper);
    }

    private void onPhotoSelected(SelectablePhotoWrapper selectedPhoto) {
        if (selectedPhoto.isSelected()) {
            int targetIndex = 1;
            for (SelectablePhotoWrapper photo : photos) {
                if (photo.getIndex() >= targetIndex) {
                    targetIndex = photo.getIndex() + 1;
                }
            }

            selectedPhoto.setIndex(targetIndex);
        } else {
            for (int i = 0; i < photos.size(); i++) {
                SelectablePhotoWrapper photo = photos.get(i);
                if (photo.getIndex() > selectedPhoto.getIndex()) {
                    photo.setIndex(photo.getIndex() - 1);
                }
            }

            selectedPhoto.setIndex(0);
        }

        if (selectedPhoto.isSelected()) {
            callView(v -> v.setButtonAddVisible(true, true));
        } else {
            resolveButtonAddVisibility(true);
        }
    }

    private boolean isSelectionMode() {
        return IVkPhotosView.ACTION_SELECT_PHOTOS.equals(action);
    }

    private void resolveButtonAddVisibility(boolean anim) {
        if (isSelectionMode()) {
            boolean hasSelected = false;
            for (SelectablePhotoWrapper wrapper : photos) {
                if (wrapper.isSelected()) {
                    hasSelected = true;
                    break;
                }
            }

            boolean finalHasSelected = hasSelected;
            callView(v -> v.setButtonAddVisible(finalHasSelected, anim));
        } else {
            callView(v -> v.setButtonAddVisible(canUploadToAlbum(), anim));
        }
    }

    public void firePhotoClick(SelectablePhotoWrapper wrapper) {
        int Index = 0;
        boolean trig = false;
        if (!FenrirNative.isNativeLoaded() || !Settings.get().other().isNative_parcel_photo()) {
            ArrayList<Photo> photos_ret = new ArrayList<>(photos.size());
            for (int i = 0; i < photos.size(); i++) {
                SelectablePhotoWrapper photo = photos.get(i);
                photos_ret.add(photo.getPhoto());
                if (!trig && photo.getPhoto().getId() == wrapper.getPhoto().getId() && photo.getPhoto().getOwnerId() == wrapper.getPhoto().getOwnerId()) {
                    Index = i;
                    trig = true;
                }
            }
            int finalIndex = Index;
            TmpSource source = new TmpSource(getInstanceId(), 0);
            fireTempDataUsage();
            appendDisposable(Stores.getInstance()
                    .tempStore()
                    .put(source.getOwnerId(), source.getSourceId(), photos_ret, Serializers.PHOTOS_SERIALIZER)
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> callView(view -> view.displayGallery(getAccountId(), albumId, ownerId, source, finalIndex)), Analytics::logUnexpectedError));
        } else {
            ParcelNative mem = ParcelNative.create();
            mem.writeInt(photos.size());
            for (int i = 0; i < photos.size(); i++) {
                SelectablePhotoWrapper photo = photos.get(i);
                mem.writeParcelable(photo.getPhoto());
                if (!trig && photo.getPhoto().getId() == wrapper.getPhoto().getId() && photo.getPhoto().getOwnerId() == wrapper.getPhoto().getOwnerId()) {
                    Index = i;
                    trig = true;
                }
            }
            int finalIndex = Index;
            callView(view -> view.displayGalleryUnSafe(getAccountId(), albumId, ownerId, mem.getNativePointer(), finalIndex));
        }
    }

    public void fireSelectionCommitClick() {
        List<Photo> selected = getSelected();

        if (nonEmpty(selected)) {
            callView(v -> v.returnSelectionToParent(selected));
        } else {
            callView(IVkPhotosView::showSelectPhotosToast);
        }
    }

    private List<SelectablePhotoWrapper> getSelectedWrappers() {
        List<SelectablePhotoWrapper> result = Utils.getSelected(photos);
        Collections.sort(result);
        return result;
    }

    private List<Photo> getSelected() {
        List<SelectablePhotoWrapper> wrappers = getSelectedWrappers();
        List<Photo> photos = new ArrayList<>(wrappers.size());
        for (SelectablePhotoWrapper wrapper : wrappers) {
            photos.add(wrapper.getPhoto());
        }

        return photos;
    }

    public void fireAddPhotosClick() {
        if (canUploadToAlbum()) {
            callView(IVkPhotosView::startLocalPhotosSelection);
        }
    }

    public void fireReadStoragePermissionChanged() {
        callView(IVkPhotosView::startLocalPhotosSelectionIfHasPermission);
    }

    private void loadDownloadPath(String Path) {
        File temp = new File(Path);
        if (!temp.exists())
            return;
        File[] file_list = temp.listFiles();
        if (file_list == null || file_list.length <= 0)
            return;
        for (File u : file_list) {
            if (u.isFile())
                mDownloads.add(u.getName());
            else if (u.isDirectory()) {
                loadDownloadPath(u.getAbsolutePath());
            }
        }
    }

    public void loadDownload() {
        isShowBDate = true;
        setRequestNow(true);
        appendDisposable(loadLocalImages()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onCacheLoaded, t -> {/*TODO*/}));
    }

    private void onCacheLoaded() {
        callView(view -> view.onToggleShowDate(isShowBDate));
        callView(IVkPhotosView::notifyDataSetChanged);
        setRequestNow(false);
    }

    private Completable loadLocalImages() {
        return Completable.create(t -> {
            File temp = new File(Settings.get().other().getPhotoDir());
            if (!temp.exists()) {
                t.onComplete();
                return;
            }
            File[] file_list = temp.listFiles();
            if (file_list == null || file_list.length <= 0) {
                t.onComplete();
                return;
            }
            if (mDownloads == null) {
                mDownloads = new LinkedList<>();
            } else {
                mDownloads.clear();
            }
            for (File u : file_list) {
                if (u.isFile())
                    mDownloads.add(u.getName());
                else if (u.isDirectory()) {
                    loadDownloadPath(u.getAbsolutePath());
                }
            }
            for (SelectablePhotoWrapper i : photos) {
                i.setDownloaded(existPhoto(i.getPhoto()));
            }
            t.onComplete();
        });
    }
}
