package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.adapter.ArtistSearchAdapter;
import dev.ragnarok.fenrir.api.model.VkApiArtist;
import dev.ragnarok.fenrir.fragment.search.criteria.ArtistSearchCriteria;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.search.ArtistSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.IArtistSearchView;

public class ArtistSearchFragment extends AbsSearchFragment<ArtistSearchPresenter, IArtistSearchView, VkApiArtist, ArtistSearchAdapter>
        implements ArtistSearchAdapter.ClickListener, IArtistSearchView {

    public static ArtistSearchFragment newInstance(int accountId, @Nullable ArtistSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        ArtistSearchFragment fragment = new ArtistSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ArtistSearchFragment newInstanceSelect(int accountId, @Nullable ArtistSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        ArtistSearchFragment fragment = new ArtistSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(ArtistSearchAdapter adapter, List<VkApiArtist> data) {
        adapter.setData(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    ArtistSearchAdapter createAdapter(List<VkApiArtist> data) {
        ArtistSearchAdapter ret = new ArtistSearchAdapter(data, requireActivity());
        ret.setClickListener(this);
        return ret;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity());
    }

    @NonNull
    @Override
    public IPresenterFactory<ArtistSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ArtistSearchPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }

    @Override
    public void onArtistClick(String id) {
        callPresenter(p -> p.fireArtistClick(new AudioArtist(id)));
    }
}
