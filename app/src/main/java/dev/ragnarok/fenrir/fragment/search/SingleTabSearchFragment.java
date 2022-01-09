package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.fragment.search.criteria.BaseSearchCriteria;
import dev.ragnarok.fenrir.listener.AppStyleable;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.view.MySearchView;

public class SingleTabSearchFragment extends Fragment implements MySearchView.OnQueryTextListener, MySearchView.OnAdditionalButtonClickListener {
    @SearchContentType
    private int mContentType;
    private int mAccountId;
    private BaseSearchCriteria mInitialCriteria;
    private boolean attachedChild;

    public static Bundle buildArgs(int accountId, @SearchContentType int contentType, @Nullable BaseSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.TYPE, contentType);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        return args;
    }

    public static SingleTabSearchFragment newInstance(Bundle args) {
        SingleTabSearchFragment fragment = new SingleTabSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static SingleTabSearchFragment newInstance(int accountId, @SearchContentType int contentType) {
        Bundle args = new Bundle();
        args.putInt(Extra.TYPE, contentType);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        SingleTabSearchFragment fragment = new SingleTabSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static SingleTabSearchFragment newInstance(int accountId, @SearchContentType int contentType, @Nullable BaseSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.TYPE, contentType);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        SingleTabSearchFragment fragment = new SingleTabSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentType = requireArguments().getInt(Extra.TYPE);
        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        mInitialCriteria = requireArguments().getParcelable(Extra.CRITERIA);

        if (Objects.nonNull(savedInstanceState)) {
            attachedChild = savedInstanceState.getBoolean("attachedChild");
        }
    }

    private void resolveLeftButton(MySearchView searchView) {
        int count = requireActivity().getSupportFragmentManager().getBackStackEntryCount();
        if (searchView != null) {
            searchView.setLeftIcon(count == 1 && requireActivity() instanceof AppStyleable ?
                    R.drawable.magnify : R.drawable.arrow_left);
            searchView.setLeftIconTint(CurrentTheme.getColorPrimary(requireActivity()));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_single, container, false);

        MySearchView searchView = root.findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(this);
        searchView.setOnBackButtonClickListener(this::onBackButtonClick);
        searchView.setOnAdditionalButtonClickListener(this);
        searchView.setQuery(getInitialCriteriaText(), true);

        resolveLeftButton(searchView);

        if (!attachedChild) {
            attachChildFragment();
            attachedChild = true;
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("attachedChild", attachedChild);
    }

    private void fireNewQuery(String query) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child_container);

        // MVP
        if (fragment instanceof AbsSearchFragment) {
            ((AbsSearchFragment<?, ?, ?, ?>) fragment).fireTextQueryEdit(query);
        }
    }

    private void attachChildFragment() {
        Fragment fragment = SearchFragmentFactory.create(mContentType, mAccountId, mInitialCriteria);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.child_container, fragment)
                .commit();
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

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }
    }

    private String getInitialCriteriaText() {
        return Objects.isNull(mInitialCriteria) ? "" : mInitialCriteria.getQuery();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        fireNewQuery(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        fireNewQuery(newText);
        return false;
    }

    public void onBackButtonClick() {
        if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() == 1
                && requireActivity() instanceof AppStyleable) {
            ((AppStyleable) requireActivity()).openMenu(true);
        } else {
            requireActivity().onBackPressed();
        }
    }

    @Override
    public void onAdditionalButtonClick() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child_container);
        if (fragment instanceof AbsSearchFragment) {
            ((AbsSearchFragment<?, ?, ?, ?>) fragment).openSearchFilter();
        }
    }
}
