package dev.ragnarok.fenrir.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.SideDrawerCategotiesAdapter;
import dev.ragnarok.fenrir.model.SideDrawerCategory;
import dev.ragnarok.fenrir.mvp.compat.AbsMvpFragment;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.SideDrawerEditPresenter;
import dev.ragnarok.fenrir.mvp.view.ISideDrawerEditView;
import dev.ragnarok.fenrir.util.Objects;

public class SideDrawerEditFragment extends AbsMvpFragment<SideDrawerEditPresenter, ISideDrawerEditView> implements ISideDrawerEditView {

    private SideDrawerCategotiesAdapter mAdapter;

    public static SideDrawerEditFragment newInstance() {
        Bundle args = new Bundle();
        SideDrawerEditFragment fragment = new SideDrawerEditFragment();
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
        View root = inflater.inflate(R.layout.fragment_dialog_drawers_categories, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder h1, RecyclerView.ViewHolder h2) {
                int fromPosition = h1.getBindingAdapterPosition();
                int toPosition = h2.getBindingAdapterPosition();

                callPresenter(p -> p.fireItemMoved(fromPosition, toPosition));
                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        mAdapter = new SideDrawerCategotiesAdapter(Collections.emptyList());
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.drawer_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            callPresenter(SideDrawerEditPresenter::fireSaveClick);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (Objects.nonNull(actionBar)) {
            actionBar.setTitle(R.string.drawer_edit_title);
            actionBar.setSubtitle(null);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<SideDrawerEditPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new SideDrawerEditPresenter(saveInstanceState);
    }

    @Override
    public void displayData(List<SideDrawerCategory> data) {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.setData(data);
        }
    }

    @Override
    public void goBackAndApplyChanges() {
        requireActivity().onBackPressed();
    }
}
