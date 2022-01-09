package dev.ragnarok.fenrir.mvp.view;

import dev.ragnarok.fenrir.model.AnswerVKOfficialList;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IAnswerVKOfficialView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(AnswerVKOfficialList pages);

    void notifyFirstListReceived();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);
}
