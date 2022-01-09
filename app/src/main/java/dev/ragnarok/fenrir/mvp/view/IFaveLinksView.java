package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.FaveLink;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IFaveLinksView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayLinks(List<FaveLink> links);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void displayRefreshing(boolean refreshing);

    void openLink(int accountId, FaveLink link);

    void notifyItemRemoved(int index);
}