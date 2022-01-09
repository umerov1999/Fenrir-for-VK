package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.DualTabPhotoActivity;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.adapter.DocsUploadAdapter;
import dev.ragnarok.fenrir.adapter.VideosAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.LocalVideo;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.selection.FileManagerSelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalVideosSelectableSource;
import dev.ragnarok.fenrir.model.selection.Sources;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.VideosListPresenter;
import dev.ragnarok.fenrir.mvp.view.IVideosListView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.place.PlaceUtil;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class VideosFragment extends BaseMvpFragment<VideosListPresenter, IVideosListView>
        implements IVideosListView, DocsUploadAdapter.ActionListener, VideosAdapter.VideoOnClickListener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    public static final String EXTRA_ALBUM_TITLE = "album_title";
    private final ActivityResultLauncher<Intent> requestFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    String file = result.getData().getStringExtra(FileManagerFragment.returnFileParameter);
                    LocalVideo vid = result.getData().getParcelableExtra(Extra.VIDEO);

                    if (nonEmpty(file)) {
                        callPresenter(p -> p.fireFileForUploadSelected(file));
                    } else if (nonNull(vid)) {
                        callPresenter(p -> p.fireFileForUploadSelected(vid.getData().toString()));
                    }
                }
            });
    private final AppPerms.doRequestPermissions requestReadPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(VideosListPresenter::fireReadPermissionResolved));
    /**
     * True - если фрагмент находится внутри TabLayout
     */
    private boolean inTabsContainer;
    private VideosAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DocsUploadAdapter mUploadAdapter;
    private View mUploadRoot;
    private TextView mEmpty;

    public static Bundle buildArgs(int accountId, int ownerId, int albumId, String action, @Nullable String albumTitle) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.ALBUM_ID, albumId);
        args.putInt(Extra.OWNER_ID, ownerId);
        if (albumTitle != null) {
            args.putString(EXTRA_ALBUM_TITLE, albumTitle);
        }

        args.putString(Extra.ACTION, action);
        return args;
    }

    public static VideosFragment newInstance(int accountId, int ownerId, int albumId, String action, @Nullable String albumTitle) {
        return newInstance(buildArgs(accountId, ownerId, albumId, action, albumTitle));
    }

    public static VideosFragment newInstance(Bundle args) {
        VideosFragment fragment = new VideosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public IPresenterFactory<VideosListPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int albumId = requireArguments().getInt(Extra.ALBUM_ID);
            int ownerId = requireArguments().getInt(Extra.OWNER_ID);

            String optAlbumTitle = requireArguments().getString(EXTRA_ALBUM_TITLE);
            String action = requireArguments().getString(Extra.ACTION);
            return new VideosListPresenter(accountId, ownerId, albumId, action, optAlbumTitle, requireActivity(), saveInstanceState);
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
    }

    @Override
    public void setToolbarTitle(String title) {
        if (!inTabsContainer) {
            super.setToolbarTitle(title);
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        if (!inTabsContainer) {
            super.setToolbarSubtitle(subtitle);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setToolbarTitle(getString(R.string.videos));

        if (!inTabsContainer) {
            if (requireActivity() instanceof OnSectionResumeCallback) {
                ((OnSectionResumeCallback) requireActivity()).onClearSelection();
            }

            new ActivityFeatures.Builder()
                    .begin()
                    .setHideNavigationMenu(false)
                    .setBarsColored(requireActivity(), true)
                    .build()
                    .apply(requireActivity());
        }
    }

    @Override
    public void requestReadExternalStoragePermission() {
        requestReadPermission.launch();
    }

    @Override
    public void startSelectUploadFileActivity(int accountId) {
        Sources sources = new Sources()
                .with(new LocalVideosSelectableSource())
                .with(new FileManagerSelectableSource());

        Intent intent = DualTabPhotoActivity.createIntent(requireActivity(), 1, sources);
        requestFile.launch(intent);
    }

    @Override
    public void setUploadDataVisible(boolean visible) {
        if (nonNull(mUploadRoot)) {
            mUploadRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayUploads(List<Upload> data) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.setData(data);
        }
    }

    @Override
    public void notifyUploadDataChanged() {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyUploadItemsAdded(int position, int count) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void notifyUploadItemChanged(int position) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void notifyUploadItemRemoved(int position) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void notifyUploadProgressChanged(int position, int progress, boolean smoothly) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.changeUploadProgress(position, progress, smoothly);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_videos, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);

        Toolbar toolbar = root.findViewById(R.id.toolbar);

        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        MySearchView mySearchView = root.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callPresenter(p -> p.fireSearchRequestChanged(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callPresenter(p -> p.fireSearchRequestChanged(newText));
                return false;
            }
        });

        FloatingActionButton Add = root.findViewById(R.id.add_button);

        if (Add != null) {
            if (callPresenter(p -> p.getAccountId() != p.getOwnerId(), true))
                Add.setVisibility(View.GONE);
            else {
                Add.setVisibility(View.VISIBLE);
                Add.setOnClickListener(v -> callPresenter(VideosListPresenter::doUpload));
            }
        }

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(VideosListPresenter::fireRefresh));

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mEmpty = root.findViewById(R.id.empty);

        RecyclerView uploadRecyclerView = root.findViewById(R.id.uploads_recycler_view);
        uploadRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

        int columns = requireActivity().getResources().getInteger(R.integer.videos_column_count);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(VideosListPresenter::fireScrollToEnd);
            }
        });

        mAdapter = new VideosAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setVideoOnClickListener(this);
        mUploadAdapter = new DocsUploadAdapter(Collections.emptyList(), this);
        uploadRecyclerView.setAdapter(mUploadAdapter);
        mUploadRoot = root.findViewById(R.id.uploads_root);
        recyclerView.setAdapter(mAdapter);


        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onVideoClick(int position, Video video) {
        callPresenter(p -> p.fireVideoClick(video));
    }

    @Override
    public boolean onVideoLongClick(int position, Video video) {
        callPresenter(p -> p.fireOnVideoLongClick(position, video));
        return true;
    }

    @Override
    public void displayData(@NonNull List<Video> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(data);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
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
    public void notifyItemRemoved(int position) {
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void notifyItemChanged(int position) {
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void returnSelectionToParent(Video video) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, Utils.singletonArrayList(video));
        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    @Override
    public void showVideoPreview(int accountId, Video video) {
        PlaceFactory.getVideoPreviewPlace(accountId, video).tryOpenWith(requireActivity());
    }

    @Override
    public void onRemoveClick(Upload upload) {
        callPresenter(p -> p.fireRemoveClick(upload));
    }

    @Override
    public void onUploaded(Video upload) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, Utils.singletonArrayList(upload));
        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    @Override
    public void doVideoLongClick(int accountId, int ownerId, boolean isMy, int position, @NonNull Video video) {
        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
        if (!isMy) {
            if (video.isCanAdd()) {
                menus.add(new OptionRequest(R.id.action_add_to_my_videos, getString(R.string.add_to_my_videos), R.drawable.plus, false));
            }
        } else {
            menus.add(new OptionRequest(R.id.action_delete_from_my_videos, getString(R.string.delete), R.drawable.ic_outline_delete, true));
        }
        if (video.isCanEdit()) {
            menus.add(new OptionRequest(R.id.action_edit, getString(R.string.edit), R.drawable.pencil, true));
        }
        menus.add(new OptionRequest(R.id.action_copy_url, getString(R.string.copy_url), R.drawable.content_copy, false));
        menus.add(new OptionRequest(R.id.share_button, getString(R.string.share), R.drawable.share, true));
        menus.add(new OptionRequest(R.id.check_show_author, getString(R.string.author), R.drawable.person, true));
        menus.add(new OptionRequest(R.id.album_container, getString(R.string.videos_albums), R.drawable.album_photo, true));

        menus.header(video.getTitle(), R.drawable.video, video.getImage());
        menus.columns(2);
        menus.show(requireActivity().getSupportFragmentManager(), "video_options", option -> {
            if (option.getId() == R.id.action_copy_url) {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.link), "https://vk.com/video" + video.getOwnerId() + "_" + video.getId());
                clipboard.setPrimaryClip(clip);
                CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.copied_url);
            } else if (option.getId() == R.id.check_show_author) {
                PlaceFactory.getOwnerWallPlace(accountId, video.getOwnerId(), null).tryOpenWith(requireActivity());
            } else if (option.getId() == R.id.album_container) {
                PlaceFactory.getAlbumsByVideoPlace(accountId, ownerId, video.getOwnerId(), video.getId()).tryOpenWith(requireActivity());
            } else {
                callPresenter(p -> p.fireVideoOption(option.getId(), video, position, requireActivity()));
            }
        });
    }

    @Override
    public void displayShareDialog(int accountId, @NonNull Video video, boolean canPostToMyWall) {
        String[] items;
        if (canPostToMyWall) {
            if (!video.getPrivate()) {
                items = new String[]{getString(R.string.share_link), getString(R.string.repost_send_message), getString(R.string.repost_to_wall)};
            } else {
                items = new String[]{getString(R.string.repost_send_message), getString(R.string.repost_to_wall)};
            }
        } else {
            if (!video.getPrivate()) {
                items = new String[]{getString(R.string.share_link), getString(R.string.repost_send_message)};
            } else {
                items = new String[]{getString(R.string.repost_send_message)};
            }
        }

        new MaterialAlertDialogBuilder(requireActivity())
                .setItems(items, (dialogInterface, i) -> {
                    if (video.getPrivate()) {
                        switch (i) {
                            case 0:
                                SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, video);
                                break;
                            case 1:
                                PlaceUtil.goToPostCreation(requireActivity(), accountId, accountId, EditingPostType.TEMP, Collections.singletonList(video));
                                break;
                        }
                    } else {
                        switch (i) {
                            case 0:
                                Utils.shareLink(requireActivity(), "https://vk.com/video" + video.getOwnerId() + "_" + video.getId(), video.getTitle());
                                break;
                            case 1:
                                SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, video);
                                break;
                            case 2:
                                PlaceUtil.goToPostCreation(requireActivity(), accountId, accountId, EditingPostType.TEMP, Collections.singletonList(video));
                                break;
                        }
                    }
                })
                .setCancelable(true)
                .setTitle(R.string.repost_title)
                .show();
    }

    @Override
    public void showSuccessToast() {
        Toast.makeText(getContext(), R.string.success, Toast.LENGTH_SHORT).show();
    }
}
