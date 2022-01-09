package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.LogsAdapter;
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalOptionsAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.model.LogEvent;
import dev.ragnarok.fenrir.model.LogEventType;
import dev.ragnarok.fenrir.model.LogEventWrapper;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.LogsPresenter;
import dev.ragnarok.fenrir.mvp.view.ILogsView;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;


public class LogsFragement extends BaseMvpFragment<LogsPresenter, ILogsView>
        implements ILogsView, HorizontalOptionsAdapter.Listener<LogEventType>, LogsAdapter.ActionListener {

    private HorizontalOptionsAdapter<LogEventType> mTypesAdapter;
    private LogsAdapter mLogsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyText;

    public static LogsFragement newInstance() {
        Bundle args = new Bundle();
        LogsFragement fragment = new LogsFragement();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_logs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(LogsPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        View headerView = inflater.inflate(R.layout.header_logs, recyclerView, false);

        RecyclerView typesRecyclerView = headerView.findViewById(R.id.types_recycler_view);
        typesRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

        mTypesAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
        mTypesAdapter.setListener(this);
        typesRecyclerView.setAdapter(mTypesAdapter);

        mLogsAdapter = new LogsAdapter(Collections.emptyList(), this);
        mLogsAdapter.addHeader(headerView);

        recyclerView.setAdapter(mLogsAdapter);

        mEmptyText = root.findViewById(R.id.empty_text);
        return root;
    }

    @Override
    public void displayTypes(List<LogEventType> types) {
        if (nonNull(mTypesAdapter)) {
            mTypesAdapter.setItems(types);
        }
    }

    @Override
    public void displayData(List<LogEventWrapper> events) {
        if (nonNull(mLogsAdapter)) {
            mLogsAdapter.setItems(events);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(R.string.application_logs);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void notifyEventDataChanged() {
        if (nonNull(mLogsAdapter)) {
            mLogsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyTypesDataChanged() {
        if (nonNull(mTypesAdapter)) {
            mTypesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
        if (nonNull(mEmptyText)) {
            mEmptyText.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<LogsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LogsPresenter(saveInstanceState);
    }

    @Override
    public void onOptionClick(LogEventType entry) {
        callPresenter(p -> p.fireTypeClick(entry));
    }

    @Override
    public void onShareClick(LogEventWrapper wrapper) {
        LogEvent event = wrapper.getEvent();
        Utils.shareLink(requireActivity(), event.getBody(), event.getTag());
    }

    @Override
    public void onCopyClick(LogEventWrapper wrapper) {
        LogEvent event = wrapper.getEvent();
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(event.getTag(), event.getBody());
        clipboard.setPrimaryClip(clip);
        CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.copied_to_clipboard);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_menu) {
            callPresenter(LogsPresenter::fireClear);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.logs_menu, menu);
    }
}
