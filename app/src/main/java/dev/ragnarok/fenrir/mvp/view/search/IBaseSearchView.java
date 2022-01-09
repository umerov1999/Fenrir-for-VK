package dev.ragnarok.fenrir.mvp.view.search;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.fragment.search.options.BaseOption;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView;
import dev.ragnarok.fenrir.mvp.view.IErrorView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IBaseSearchView<T> extends IMvpView, IErrorView, IAccountDependencyView, IAttachmentsPlacesView {
    void displayData(List<T> data);

    void setEmptyTextVisible(boolean visible);

    void notifyDataSetChanged();

    void notifyItemChanged(int index);

    void notifyDataAdded(int position, int count);

    void showLoading(boolean loading);

    void displayFilter(int accountId, ArrayList<BaseOption> options);
}