package dev.ragnarok.fenrir.fragment;

import static android.app.Activity.RESULT_OK;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.PhotosActivity;
import dev.ragnarok.fenrir.adapter.BigVkPhotosAdapter;
import dev.ragnarok.fenrir.dialog.ImageSizeAlertDialog;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.model.wrappers.SelectablePhotoWrapper;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.VkPhotosPresenter;
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.ViewUtils;

public class VKPhotosFragment extends BaseMvpFragment<VkPhotosPresenter, IVkPhotosView>
        implements BigVkPhotosAdapter.PhotosActionListener, BigVkPhotosAdapter.UploadActionListener, IVkPhotosView {

    private static final String TAG = VKPhotosFragment.class.getSimpleName();
    private final ActivityResultLauncher<Intent> requestUploadPhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    ArrayList<LocalPhoto> photos = result.getData().getParcelableArrayListExtra(Extra.PHOTOS);
                    if (nonEmpty(photos)) {
                        onPhotosForUploadSelected(photos);
                    }
                }
            });
    private final AppPerms.doRequestPermissions requestReadPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(VkPhotosPresenter::fireReadStoragePermissionChanged));
    private final AppPerms.doRequestPermissions requestReadPermissionForLoadDownload = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(VkPhotosPresenter::loadDownload));
    private final ActivityResultLauncher<Intent> requestPhotoUpdate = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    postPresenterReceive(p -> p.updateInfo(result.getData().getExtras().getInt(Extra.POSITION), result.getData().getExtras().getLong(Extra.PTR)));
                }
            });
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BigVkPhotosAdapter mAdapter;
    private TextView mEmptyText;
    private FloatingActionButton mFab;
    private String mAction;
    private RecyclerView mRecyclerView;

    public static Bundle buildArgs(int accountId, int ownerId, int albumId, String action) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ALBUM_ID, albumId);
        args.putString(Extra.ACTION, action);
        return args;
    }

    public static VKPhotosFragment newInstance(Bundle args) {
        VKPhotosFragment fragment = new VKPhotosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static VKPhotosFragment newInstance(int accountId, int ownerId, int albumId, String action) {
        return newInstance(buildArgs(accountId, ownerId, albumId, action));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAction = requireArguments().getString(Extra.ACTION, ACTION_SHOW_PHOTOS);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        int columnCount = getResources().getInteger(R.integer.local_gallery_column_count);
        RecyclerView.LayoutManager manager = new GridLayoutManager(requireActivity(), columnCount);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(VkPhotosPresenter::fireRefresh));

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(TAG));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(VkPhotosPresenter::fireScrollToEnd);
            }
        });

        mEmptyText = root.findViewById(R.id.empty);

        mFab = root.findViewById(R.id.fr_photo_gallery_attach);
        mFab.setOnClickListener(v -> onFabClicked());

        mAdapter = new BigVkPhotosAdapter(requireActivity(), Collections.emptyList(), Collections.emptyList(), TAG);
        mAdapter.setPhotosActionListener(this);
        mAdapter.setUploadActionListener(this);
        mRecyclerView.setAdapter(mAdapter);
        return root;
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmptyText) && nonNull(mAdapter)) {
            mEmptyText.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void resolveFabVisibility(boolean anim, boolean show) {
        if (!isAdded() || mFab == null) return;

        if (mFab.isShown() && !show) {
            mFab.hide();
        }

        if (!mFab.isShown() && show) {
            mFab.show();
        }
    }

    private void onFabClicked() {
        if (isSelectionMode()) {
            callPresenter(VkPhotosPresenter::fireSelectionCommitClick);
        } else {
            callPresenter(VkPhotosPresenter::fireAddPhotosClick);
        }
    }

    private boolean isSelectionMode() {
        return ACTION_SELECT_PHOTOS.equals(mAction);
    }

    private void onPhotosForUploadSelected(@NonNull List<LocalPhoto> photos) {
        ImageSizeAlertDialog.showUploadPhotoSizeIfNeed(requireActivity(), size -> doUploadPhotosToAlbum(photos, size));
    }

    private void doUploadPhotosToAlbum(@NonNull List<LocalPhoto> photos, int size) {
        callPresenter(p -> p.firePhotosForUploadSelected(photos, size));
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onDestroyView() {
        mAdapter.cleanup();
        super.onDestroyView();
    }

    @Override
    public void onPhotoClick(BigVkPhotosAdapter.PhotoViewHolder holder, SelectablePhotoWrapper wrapper) {
        if (isSelectionMode()) {
            callPresenter(p -> p.firePhotoSelectionChanged(wrapper));
            mAdapter.updatePhotoHoldersSelectionAndIndexes();
        } else {
            callPresenter(p -> p.firePhotoClick(wrapper));
        }
    }

    @Override
    public void onUploadRemoveClicked(Upload upload) {
        callPresenter(p -> p.fireUploadRemoveClick(upload));
    }

    @Override
    public void displayData(List<SelectablePhotoWrapper> photos, List<Upload> uploads) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(BigVkPhotosAdapter.DATA_TYPE_UPLOAD, uploads);
            mAdapter.setData(BigVkPhotosAdapter.DATA_TYPE_PHOTO, photos);
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyPhotosAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count, BigVkPhotosAdapter.DATA_TYPE_PHOTO);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void notifyUploadAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count, BigVkPhotosAdapter.DATA_TYPE_UPLOAD);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyUploadRemoved(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(index, BigVkPhotosAdapter.DATA_TYPE_UPLOAD);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void setButtonAddVisible(boolean visible, boolean anim) {
        if (nonNull(mFab)) {
            resolveFabVisibility(anim, visible);
        }
    }

    @Override
    public void notifyUploadItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index, BigVkPhotosAdapter.DATA_TYPE_UPLOAD);
        }
    }

    @Override
    public void notifyUploadProgressChanged(int id, int progress) {
        if (nonNull(mAdapter)) {
            mAdapter.updateUploadHoldersProgress(id, true, progress);
        }
    }

    @Override
    public void displayGallery(int accountId, int albumId, int ownerId, @NonNull TmpSource source, int position) {
        PlaceFactory.getPhotoAlbumGalleryPlace(accountId, albumId, ownerId, source, position, false, Settings.get().other().isInvertPhotoRev()).tryOpenWith(requireActivity());
    }

    @Override
    public void displayGalleryUnSafe(int accountId, int albumId, int ownerId, long parcelNativePointer, int position) {
        PlaceFactory.getPhotoAlbumGalleryPlace(accountId, albumId, ownerId, parcelNativePointer, position, false, Settings.get().other().isInvertPhotoRev()).setActivityResultLauncher(requestPhotoUpdate).tryOpenWith(requireActivity());
    }

    @Override
    public void displayDefaultToolbarTitle() {
        setToolbarTitle(getString(R.string.photos));
    }

    @Override
    public void displayToolbarSubtitle(@Nullable PhotoAlbum album, @NonNull String text) {
        if (nonNull(album)) {
            setToolbarSubtitle(album.getDisplayTitle(requireActivity()) + " " + text);
        } else {
            setToolbarSubtitle(text);
        }
    }

    @Override
    public void scrollTo(int position) {
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void setDrawerPhotosSelected(boolean selected) {
        if (requireActivity() instanceof OnSectionResumeCallback) {
            if (selected) {
                ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_PHOTOS);
            } else {
                ((OnSectionResumeCallback) requireActivity()).onClearSelection();
            }
        }
    }

    @Override
    public void returnSelectionToParent(List<Photo> selected) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, new ArrayList<>(selected));
        requireActivity().setResult(RESULT_OK, intent);
        requireActivity().finish();
    }

    @Override
    public void showSelectPhotosToast() {
        Toast.makeText(requireActivity(), getString(R.string.select_attachments), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startLocalPhotosSelection() {
        if (!AppPerms.hasReadStoragePermission(requireActivity())) {
            requestReadPermission.launch();
            return;
        }

        startLocalPhotosSelectionActibity();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_photos, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_toggle_rev).setTitle(Settings.get().other().isInvertPhotoRev() ? R.string.sort_new_to_old : R.string.sort_old_to_new);
        menu.findItem(R.id.action_show_date).setVisible(!callPresenter(VkPhotosPresenter::getIsShowBDate, false));
    }

    @Override
    public void onToggleShowDate(boolean isShow) {
        mAdapter.setIsShowDate(isShow);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_get_downloaded) {
            if (!AppPerms.hasReadWriteStoragePermission(requireActivity())) {
                requestReadPermissionForLoadDownload.launch();
                return true;
            }
            callPresenter(VkPhotosPresenter::loadDownload);
            return true;
        } else if (item.getItemId() == R.id.action_show_date) {
            callPresenter(VkPhotosPresenter::doToggleDate);
            requireActivity().invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.action_toggle_rev) {
            callPresenter(VkPhotosPresenter::togglePhotoInvert);
            requireActivity().invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startLocalPhotosSelectionActibity() {
        Intent intent = new Intent(requireActivity(), PhotosActivity.class);
        intent.putExtra(PhotosActivity.EXTRA_MAX_SELECTION_COUNT, Integer.MAX_VALUE);
        requestUploadPhoto.launch(intent);
    }

    @Override
    public void startLocalPhotosSelectionIfHasPermission() {
        if (AppPerms.hasReadStoragePermission(requireActivity())) {
            startLocalPhotosSelectionActibity();
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<VkPhotosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            ParcelableOwnerWrapper ownerWrapper = requireArguments().getParcelable(Extra.OWNER);
            Owner owner = nonNull(ownerWrapper) ? ownerWrapper.get() : null;
            PhotoAlbum album = requireArguments().getParcelable(Extra.ALBUM);

            return new VkPhotosPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getInt(Extra.OWNER_ID),
                    requireArguments().getInt(Extra.ALBUM_ID),
                    requireArguments().getString(Extra.ACTION, ACTION_SHOW_PHOTOS),
                    owner,
                    album,
                    saveInstanceState
            );
        };
    }
}
