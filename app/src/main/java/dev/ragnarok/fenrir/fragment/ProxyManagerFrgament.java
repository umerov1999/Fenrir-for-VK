package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.ProxiesAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.ProxyManagerPresenter;
import dev.ragnarok.fenrir.mvp.view.IProxyManagerView;
import dev.ragnarok.fenrir.place.PlaceFactory;

public class ProxyManagerFrgament extends BaseMvpFragment<ProxyManagerPresenter, IProxyManagerView>
        implements IProxyManagerView, ProxiesAdapter.ActionListener {

    private ProxiesAdapter mProxiesAdapter;

    public static ProxyManagerFrgament newInstance() {
        Bundle args = new Bundle();
        ProxyManagerFrgament fragment = new ProxyManagerFrgament();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_proxy_manager, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mProxiesAdapter = new ProxiesAdapter(Collections.emptyList(), this);
        recyclerView.setAdapter(mProxiesAdapter);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.proxies, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            callPresenter(ProxyManagerPresenter::fireAddClick);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public IPresenterFactory<ProxyManagerPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ProxyManagerPresenter(saveInstanceState);
    }

    @Override
    public void displayData(List<ProxyConfig> configs, ProxyConfig active) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.setData(configs, active);
        }
    }

    @Override
    public void notifyItemAdded(int position) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.notifyItemInserted(position + mProxiesAdapter.getHeadersCount());
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.notifyItemRemoved(position + mProxiesAdapter.getHeadersCount());
        }
    }

    @Override
    public void setActiveAndNotifyDataSetChanged(ProxyConfig config) {
        if (nonNull(mProxiesAdapter)) {
            mProxiesAdapter.setActive(config);
        }
    }

    @Override
    public void goToAddingScreen() {
        PlaceFactory.getProxyAddPlace().tryOpenWith(requireActivity());
    }

    @Override
    public void onDeleteClick(ProxyConfig config) {
        callPresenter(p -> p.fireDeleteClick(config));
    }

    @Override
    public void onSetAtiveClick(ProxyConfig config) {
        callPresenter(p -> p.fireActivateClick(config));
    }

    @Override
    public void onDisableClick(ProxyConfig config) {
        callPresenter(p -> p.fireDisableClick(config));
    }
}
