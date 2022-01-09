package dev.ragnarok.fenrir.media.gif;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.settings.IProxySettings;

public class AppGifPlayerFactory implements IGifPlayerFactory {

    private final IProxySettings proxySettings;

    public AppGifPlayerFactory(IProxySettings proxySettings) {
        this.proxySettings = proxySettings;
    }

    @Override
    public IGifPlayer createGifPlayer(@NonNull String url, boolean isRepeat) {
        ProxyConfig config = proxySettings.getActiveProxy();
        return new ExoGifPlayer(url, config, isRepeat);
    }
}