package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.db.interfaces.ILogsStorage;
import dev.ragnarok.fenrir.model.LogEvent;
import dev.ragnarok.fenrir.model.LogEventType;
import dev.ragnarok.fenrir.model.LogEventWrapper;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.ILogsView;
import dev.ragnarok.fenrir.util.DisposableHolder;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;


public class LogsPresenter extends RxSupportPresenter<ILogsView> {

    private final List<LogEventType> types;

    private final List<LogEventWrapper> events;

    private final ILogsStorage store;
    private final DisposableHolder<Integer> disposableHolder = new DisposableHolder<>();
    private boolean loadingNow;

    public LogsPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);

        store = Injection.provideLogsStore();
        types = createTypes();
        events = new ArrayList<>();

        loadAll();
    }

    private static List<LogEventType> createTypes() {
        List<LogEventType> types = new ArrayList<>();
        types.add(new LogEventType(LogEvent.Type.ERROR, R.string.log_type_error).setActive(true));
        return types;
    }

    private void resolveEmptyTextVisibility() {
        callView(v -> v.setEmptyTextVisible(events.isEmpty()));
    }

    private void setLoading(boolean loading) {
        loadingNow = loading;
        resolveRefreshingView();
    }

    @Override
    public void onGuiCreated(@NonNull ILogsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(events);
        viewHost.displayTypes(types);

        resolveEmptyTextVisibility();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.showRefreshing(loadingNow));
    }

    public void fireClear() {
        store.Clear();
        loadAll();
    }

    private void loadAll() {
        int type = getSelectedType();

        setLoading(true);
        disposableHolder.append(store.getAll(type)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, throwable -> onDataReceiveError(Utils.getCauseIfRuntime(throwable))));
    }

    private void onDataReceiveError(Throwable throwable) {
        setLoading(false);
        callView(v -> v.showError(throwable.getMessage()));
    }

    private void onDataReceived(List<LogEvent> events) {
        setLoading(false);

        this.events.clear();

        for (LogEvent event : events) {
            this.events.add(new LogEventWrapper(event));
        }

        callView(ILogsView::notifyEventDataChanged);
        resolveEmptyTextVisibility();
    }

    private int getSelectedType() {
        int type = LogEvent.Type.ERROR;
        for (LogEventType t : types) {
            if (t.isActive()) {
                type = t.getType();
            }
        }

        return type;
    }

    @Override
    public void onDestroyed() {
        disposableHolder.dispose();
        super.onDestroyed();
    }

    public void fireTypeClick(LogEventType entry) {
        if (getSelectedType() == entry.getType()) {
            return;
        }

        for (LogEventType t : types) {
            t.setActive(t.getType() == entry.getType());
        }

        callView(ILogsView::notifyTypesDataChanged);
        loadAll();
    }

    public void fireRefresh() {
        loadAll();
    }
}
