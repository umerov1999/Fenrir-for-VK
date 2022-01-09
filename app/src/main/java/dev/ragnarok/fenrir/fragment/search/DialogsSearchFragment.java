package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.adapter.DialogPreviewAdapter;
import dev.ragnarok.fenrir.fragment.search.criteria.DialogsSearchCriteria;
import dev.ragnarok.fenrir.model.Conversation;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.search.DialogsSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.IDialogsSearchView;

public class DialogsSearchFragment extends AbsSearchFragment<DialogsSearchPresenter, IDialogsSearchView, Conversation, DialogPreviewAdapter>
        implements IDialogsSearchView, DialogPreviewAdapter.ActionListener {

    public static DialogsSearchFragment newInstance(int accountId, DialogsSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        DialogsSearchFragment fragment = new DialogsSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public IPresenterFactory<DialogsSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            DialogsSearchCriteria criteria = requireArguments().getParcelable(Extra.CRITERIA);
            return new DialogsSearchPresenter(accountId, criteria, saveInstanceState);
        };
    }

    @Override
    void setAdapterData(DialogPreviewAdapter adapter, List<Conversation> data) {
        adapter.setData(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    DialogPreviewAdapter createAdapter(List<Conversation> data) {
        return new DialogPreviewAdapter(data, this);
    }

    @Override
    RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity());
    }

    @Override
    public void onEntryClick(Conversation o) {
        callPresenter(p -> p.fireEntryClick(o));
    }
}