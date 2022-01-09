package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.LogEventType;
import dev.ragnarok.fenrir.model.LogEventWrapper;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface ILogsView extends IMvpView, IErrorView {

    void displayTypes(List<LogEventType> types);

    void displayData(List<LogEventWrapper> events);

    void showRefreshing(boolean refreshing);

    void notifyEventDataChanged();

    void notifyTypesDataChanged();

    void setEmptyTextVisible(boolean visible);
}
