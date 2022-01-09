package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.DualTabPhotoActivity;
import dev.ragnarok.fenrir.adapter.DocsAdapter;
import dev.ragnarok.fenrir.adapter.DocsAsImagesAdapter;
import dev.ragnarok.fenrir.adapter.DocsUploadAdapter;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalOptionsAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.DocFilter;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.selection.FileManagerSelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalPhotosSelectableSource;
import dev.ragnarok.fenrir.model.selection.Sources;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.DocsListPresenter;
import dev.ragnarok.fenrir.mvp.view.IDocListView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.ViewUtils;

public class DocsFragment extends BaseMvpFragment<DocsListPresenter, IDocListView>
        implements IDocListView, DocsAdapter.ActionListener, DocsUploadAdapter.ActionListener, DocsAsImagesAdapter.ActionListener {

    private final ActivityResultLauncher<Intent> requestFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    String file = result.getData().getStringExtra(FileManagerFragment.returnFileParameter);
                    ArrayList<LocalPhoto> photos = result.getData().getParcelableArrayListExtra(Extra.PHOTOS);
                    if (nonEmpty(file)) {
                        callPresenter(p -> p.fireFileForUploadSelected(file));
                    } else if (nonEmpty(photos)) {
                        callPresenter(p -> p.fireLocalPhotosForUploadSelected(photos));
                    }
                }
            });
    private final AppPerms.doRequestPermissions requestReadPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(DocsListPresenter::fireReadPermissionResolved));
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerBindableAdapter<Document, ?> mDocsAdapter;
    private DocsUploadAdapter mUploadAdapter;
    private HorizontalOptionsAdapter<DocFilter> mFiltersAdapter;
    private View mHeaderView;
    private RecyclerView mRecyclerView;
    private View mUploadRoot;
    private boolean mImagesOnly;

    public static Bundle buildArgs(int accountId, int ownerId, String action) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putString(Extra.ACTION, action);
        return args;
    }

    public static DocsFragment newInstance(Bundle args) {
        DocsFragment fragment = new DocsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DocsFragment newInstance(int accountId, int ownerId, String action) {
        return newInstance(buildArgs(accountId, ownerId, action));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_docs, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(DocsListPresenter::fireRefresh));

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mRecyclerView = root.findViewById(R.id.recycler_view);

        // тут, значит, некая многоходовочка
        // Так как мы не знаем, какой тип данных мы показываем (фото или просто документы),
        // то при создании view мы просим presenter уведомить об этом типе.
        // Предполагается, что presenter НЕЗАМЕДЛИТЕЛЬНО вызовет у view метод setAdapterType(boolean imagesOnly)
        callPresenter(DocsListPresenter::pleaseNotifyViewAboutAdapterType);
        // и мы дальше по коду можем использовать переменную mImagesOnly

        mRecyclerView.setLayoutManager(createLayoutManager(mImagesOnly));

        mDocsAdapter = createAdapter(mImagesOnly, Collections.emptyList());

        FloatingActionButton buttonAdd = root.findViewById(R.id.add_button);
        buttonAdd.setOnClickListener(v -> callPresenter(DocsListPresenter::fireButtonAddClick));

        RecyclerView uploadRecyclerView = root.findViewById(R.id.uploads_recycler_view);
        uploadRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

        mUploadAdapter = new DocsUploadAdapter(Collections.emptyList(), this);

        uploadRecyclerView.setAdapter(mUploadAdapter);

        mHeaderView = View.inflate(requireActivity(), R.layout.header_feed, null);

        RecyclerView headerRecyclerView = mHeaderView.findViewById(R.id.header_list);
        headerRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

        mFiltersAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
        mFiltersAdapter.setListener(entry -> callPresenter(p -> p.fireFilterClick(entry)));

        headerRecyclerView.setAdapter(mFiltersAdapter);

        mDocsAdapter.addHeader(mHeaderView);
        mRecyclerView.setAdapter(mDocsAdapter);

        mUploadRoot = root.findViewById(R.id.uploads_root);

        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        return root;
    }

    private RecyclerView.LayoutManager createLayoutManager(boolean asImages) {
        if (asImages) {
            int columnCount = getResources().getInteger(R.integer.local_gallery_column_count);
            return new GridLayoutManager(requireActivity(), columnCount);
        } else {
            return new LinearLayoutManager(requireActivity());
        }
    }

    @Override
    public void displayData(List<Document> documents, boolean asImages) {
        mImagesOnly = asImages;

        if (isNull(mRecyclerView)) {
            return;
        }

        if (asImages && mDocsAdapter instanceof DocsAsImagesAdapter) {
            mDocsAdapter.setItems(documents);
            return;
        }

        if (!asImages && mDocsAdapter instanceof DocsAdapter) {
            mDocsAdapter.setItems(documents);
            return;
        }

        if (asImages) {
            DocsAsImagesAdapter docsAsImagesAdapter = new DocsAsImagesAdapter(documents);
            docsAsImagesAdapter.setActionListener(this);
            mDocsAdapter = docsAsImagesAdapter;
        } else {
            DocsAdapter docsAdapter = new DocsAdapter(documents);
            docsAdapter.setActionListener(this);
            mDocsAdapter = docsAdapter;
        }

        mRecyclerView.setLayoutManager(createLayoutManager(asImages));

        mDocsAdapter = createAdapter(asImages, documents);
        mDocsAdapter.addHeader(mHeaderView);

        mRecyclerView.setAdapter(mDocsAdapter);
    }

    private RecyclerBindableAdapter<Document, ?> createAdapter(boolean asImages, List<Document> documents) {
        if (asImages) {
            DocsAsImagesAdapter docsAsImagesAdapter = new DocsAsImagesAdapter(documents);
            docsAsImagesAdapter.setActionListener(this);
            return docsAsImagesAdapter;
        } else {
            DocsAdapter docsAdapter = new DocsAdapter(documents);
            docsAdapter.setActionListener(this);
            return docsAdapter;
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mDocsAdapter)) {
            mDocsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdd(int position, int count) {
        if (nonNull(mDocsAdapter)) {
            mDocsAdapter.notifyItemBindableRangeInserted(position, count);
        }
    }

    @Override
    public void notifyDataRemoved(int position) {
        if (nonNull(mDocsAdapter)) {
            mDocsAdapter.notifyItemBindableRemoved(position);
        }
    }

    @Override
    public void openDocument(int accountId, @NonNull Document document) {
        PlaceFactory.getDocPreviewPlace(accountId, document).tryOpenWith(requireActivity());
    }

    @Override
    public void returnSelection(ArrayList<Document> docs) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, docs);
        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    @Override
    public void goToGifPlayer(int accountId, @NonNull ArrayList<Document> gifs, int selected) {
        PlaceFactory.getGifPagerPlace(accountId, gifs, selected).tryOpenWith(requireActivity());
    }

    @Override
    public void requestReadExternalStoragePermission() {
        requestReadPermission.launch();
    }

    @Override
    public void startSelectUploadFileActivity(int accountId) {
        Sources sources = new Sources()
                .with(new FileManagerSelectableSource())
                .with(new LocalPhotosSelectableSource());

        Intent intent = DualTabPhotoActivity.createIntent(requireActivity(), 10, sources);
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
    public void displayFilterData(List<DocFilter> filters) {
        if (nonNull(mFiltersAdapter)) {
            mFiltersAdapter.setItems(filters);
        }
    }

    @Override
    public void notifyFiltersChanged() {
        if (nonNull(mFiltersAdapter)) {
            mFiltersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setAdapterType(boolean imagesOnly) {
        mImagesOnly = imagesOnly;
    }

    @NonNull
    @Override
    public IPresenterFactory<DocsListPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new DocsListPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.OWNER_ID),
                requireArguments().getString(Extra.ACTION),
                saveInstanceState
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.DOCS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.documents);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_DOCS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onDocClick(int index, @NonNull Document doc) {
        callPresenter(p -> p.fireDocClick(doc));
    }

    @Override
    public boolean onDocLongClick(int index, @NonNull Document doc) {
        callPresenter(p -> p.fireMenuClick(requireActivity(), index, doc));
        return true;
    }

    @Override
    public void onRemoveClick(Upload upload) {
        callPresenter(p -> p.fireRemoveClick(upload));
    }
}
