package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.LocalPhotoAlbumsAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpBottomSheetDialogFragment;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.LocalAudioAlbumsPresenter;
import dev.ragnarok.fenrir.mvp.view.ILocalPhotoAlbumsView;
import dev.ragnarok.fenrir.picasso.Content_Local;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.view.MySearchView;

public class LocalAudioAlbumsFragment extends BaseMvpBottomSheetDialogFragment<LocalAudioAlbumsPresenter, ILocalPhotoAlbumsView>
        implements LocalPhotoAlbumsAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener, ILocalPhotoAlbumsView {

    private final AppPerms.doRequestPermissions requestReadPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(LocalAudioAlbumsPresenter::fireReadExternalStoregePermissionResolved));
    private RecyclerView mRecyclerView;
    private LocalPhotoAlbumsAdapter mAlbumsAdapter;
    private Listener listener;

    public static LocalAudioAlbumsFragment newInstance(Listener listener) {
        LocalAudioAlbumsFragment fragment = new LocalAudioAlbumsFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Override
    @NonNull
    public BottomSheetDialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireActivity(), getTheme());
        BottomSheetBehavior<FrameLayout> behavior = dialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        behavior.setSkipCollapsed(true);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_albums_audio, container, false);

        MySearchView mySearchView = view.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callPresenter(p -> p.fireSearchRequestChanged(query, false));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callPresenter(p -> p.fireSearchRequestChanged(newText, false));
                return false;
            }
        });

        int columnCount = getResources().getInteger(R.integer.photos_albums_column_count);
        RecyclerView.LayoutManager manager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(LocalPhotoAlbumsAdapter.PICASSO_TAG));

        mAlbumsAdapter = new LocalPhotoAlbumsAdapter(requireActivity(), Collections.emptyList(), Content_Local.AUDIO);
        mAlbumsAdapter.setClickListener(this);

        mRecyclerView.setAdapter(mAlbumsAdapter);
        return view;
    }

    @Override
    public void onClick(LocalImageAlbum album) {
        callPresenter(p -> p.fireAlbumClick(album));
    }

    @Override
    public void onRefresh() {
        callPresenter(LocalAudioAlbumsPresenter::fireRefresh);
    }

    @Override
    public void displayData(@NonNull List<LocalImageAlbum> data) {
        if (nonNull(mRecyclerView)) {
            mAlbumsAdapter.setData(data);
        }
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
    }

    @Override
    public void displayProgress(boolean loading) {
    }

    @Override
    public void openAlbum(@NonNull LocalImageAlbum album) {
        if (nonNull(listener)) {
            listener.onSelected(album.getId());
        }
        dismiss();
    }

    @Override
    public void notifyDataChanged() {
        if (nonNull(mAlbumsAdapter)) {
            mAlbumsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void requestReadExternalStoragePermission() {
        requestReadPermission.launch();
    }

    @NonNull
    @Override
    public IPresenterFactory<LocalAudioAlbumsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LocalAudioAlbumsPresenter(saveInstanceState);
    }

    public interface Listener {
        void onSelected(int bucket_id);
    }
}
