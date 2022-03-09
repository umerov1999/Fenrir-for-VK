package dev.ragnarok.fenrir.settings;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Observable;

public interface IProxySettings {
    void put(String address, int port);

    void put(String address, int port, String username, String pass);

    Observable<ProxyConfig> observeAdding();

    Observable<ProxyConfig> observeRemoving();

    Observable<Optional<ProxyConfig>> observeActive();

    List<ProxyConfig> getAll();

    ProxyConfig getActiveProxy();

    void setActive(ProxyConfig config);

    void broadcastUpdate(@Nullable ProxyConfig config);

    void delete(ProxyConfig config);
}