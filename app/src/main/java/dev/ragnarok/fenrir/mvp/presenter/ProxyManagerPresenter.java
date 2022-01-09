package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IProxyManagerView;
import dev.ragnarok.fenrir.settings.IProxySettings;
import dev.ragnarok.fenrir.util.Utils;


public class ProxyManagerPresenter extends RxSupportPresenter<IProxyManagerView> {

    private final IProxySettings settings;

    private final List<ProxyConfig> configs;

    public ProxyManagerPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        settings = Injection.provideProxySettings();

        configs = settings.getAll();

        appendDisposable(settings.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onProxyAdded));

        appendDisposable(settings.observeRemoving()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onProxyDeleted));

        appendDisposable(settings.observeActive()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(optional -> onActiveChanged(optional.get())));
    }

    private void onActiveChanged(ProxyConfig config) {
        callView(view -> view.setActiveAndNotifyDataSetChanged(config));
    }

    private void onProxyDeleted(ProxyConfig config) {
        int index = Utils.findIndexById(configs, config.getId());
        if (index != -1) {
            configs.remove(index);
            callView(view -> view.notifyItemRemoved(index));
        }
    }

    private void onProxyAdded(ProxyConfig config) {
        configs.add(0, config);
        callView(view -> view.notifyItemAdded(0));
    }

    @Override
    public void onGuiCreated(@NonNull IProxyManagerView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(configs, settings.getActiveProxy());

    }

    public void fireDeleteClick(ProxyConfig config) {
        if (config.equals(settings.getActiveProxy())) {
            callView(v -> showError(v, new Exception("Proxy is active. First, disable the proxy")));
            return;
        }

        settings.delete(config);
    }

    public void fireActivateClick(ProxyConfig config) {
        settings.setActive(config);
    }

    @SuppressWarnings("unused")
    public void fireDisableClick(ProxyConfig config) {
        settings.setActive(null);
    }

    public void fireAddClick() {
        callView(IProxyManagerView::goToAddingScreen);
    }
}