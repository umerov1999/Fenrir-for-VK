package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface IProxyManagerView extends IMvpView, IErrorView {
    void displayData(List<ProxyConfig> configs, ProxyConfig active);

    void notifyItemAdded(int position);

    void notifyItemRemoved(int position);

    void setActiveAndNotifyDataSetChanged(ProxyConfig config);

    void goToAddingScreen();
}