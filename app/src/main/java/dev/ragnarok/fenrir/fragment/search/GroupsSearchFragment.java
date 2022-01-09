package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.adapter.PeopleAdapter;
import dev.ragnarok.fenrir.fragment.search.criteria.GroupSearchCriteria;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.search.CommunitiesSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.ICommunitiesSearchView;
import dev.ragnarok.fenrir.place.PlaceFactory;

public class GroupsSearchFragment extends AbsSearchFragment<CommunitiesSearchPresenter, ICommunitiesSearchView, Community, PeopleAdapter>
        implements ICommunitiesSearchView, PeopleAdapter.ClickListener {

    public static GroupsSearchFragment newInstance(int accountId, @Nullable GroupSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        GroupsSearchFragment fragment = new GroupsSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(PeopleAdapter adapter, List<Community> data) {
        adapter.setItems(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    PeopleAdapter createAdapter(List<Community> data) {
        PeopleAdapter adapter = new PeopleAdapter(requireActivity(), data);
        adapter.setClickListener(this);
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @NonNull
    @Override
    public IPresenterFactory<CommunitiesSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunitiesSearchPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }

    @Override
    public void onOwnerClick(Owner owner) {
        callPresenter(p -> p.fireCommunityClick((Community) owner));
    }

    @Override
    public void openCommunityWall(int accountId, Community community) {
        PlaceFactory.getOwnerWallPlace(accountId, community).tryOpenWith(requireActivity());
    }
}