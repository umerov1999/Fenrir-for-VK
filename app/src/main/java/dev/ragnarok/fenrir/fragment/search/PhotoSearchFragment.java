package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.SearchPhotosAdapter;
import dev.ragnarok.fenrir.fragment.search.criteria.PhotoSearchCriteria;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.search.PhotoSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.IPhotoSearchView;
import dev.ragnarok.fenrir.place.PlaceFactory;

public class PhotoSearchFragment extends AbsSearchFragment<PhotoSearchPresenter, IPhotoSearchView, Photo, SearchPhotosAdapter>
        implements SearchPhotosAdapter.PhotosActionListener, IPhotoSearchView {

    private static final String TAG = PhotoSearchFragment.class.getSimpleName();

    public static PhotoSearchFragment newInstance(int accountId, @Nullable PhotoSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        PhotoSearchFragment fragment = new PhotoSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(SearchPhotosAdapter adapter, List<Photo> data) {
        adapter.setData(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    SearchPhotosAdapter createAdapter(List<Photo> data) {
        SearchPhotosAdapter adapter = new SearchPhotosAdapter(requireActivity(), data, TAG);
        adapter.setPhotosActionListener(this);
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columnCount = getResources().getInteger(R.integer.local_gallery_column_count);
        return new GridLayoutManager(requireActivity(), columnCount);
    }

    @NonNull
    @Override
    public IPresenterFactory<PhotoSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new PhotoSearchPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }

    @Override
    public void displayGallery(int accountId, ArrayList<Photo> photos, int position) {
        PlaceFactory.getSimpleGalleryPlace(accountId, photos, position, false).tryOpenWith(requireActivity());
    }

    @Override
    public void onPhotoClick(SearchPhotosAdapter.PhotoViewHolder holder, Photo photo) {
        callPresenter(p -> p.firePhotoClick(photo));
    }
}
