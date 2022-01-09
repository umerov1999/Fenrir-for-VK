package dev.ragnarok.fenrir.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.ThemeAdapter;
import dev.ragnarok.fenrir.mvp.compat.AbsMvpFragment;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.ThemePresenter;
import dev.ragnarok.fenrir.mvp.view.IThemeView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemeValue;
import dev.ragnarok.fenrir.util.Objects;

public class ThemeFragment extends AbsMvpFragment<ThemePresenter, IThemeView> implements IThemeView, ThemeAdapter.ClickListener {

    private ThemeAdapter mAdapter;

    public static ThemeFragment newInstance() {
        Bundle args = new Bundle();
        ThemeFragment fragment = new ThemeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_theme, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        int columns = getResources().getInteger(R.integer.photos_column_count);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity(), columns);
        recyclerView.setLayoutManager(gridLayoutManager);

        mAdapter = new ThemeAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (Objects.nonNull(actionBar)) {
            actionBar.setTitle(R.string.theme_edit_title);
            actionBar.setSubtitle(null);
        }
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @NonNull
    @Override
    public IPresenterFactory<ThemePresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ThemePresenter(saveInstanceState);
    }

    @Override
    public void displayData(ThemeValue[] data) {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.setData(data);
        }
    }

    @Override
    public void onClick(int index, ThemeValue value) {
        if (value.getDisabled()) {
            return;
        }
        Settings.get().ui().setMainTheme(value.getId());
        requireActivity().recreate();
        mAdapter.notifyDataSetChanged();
    }
}
