package dev.ragnarok.fenrir;

import static dev.ragnarok.fenrir.util.Objects.isNull;

import android.content.Context;

import dev.ragnarok.fenrir.api.CaptchaProvider;
import dev.ragnarok.fenrir.api.ICaptchaProvider;
import dev.ragnarok.fenrir.api.impl.Networker;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.db.impl.AppStorages;
import dev.ragnarok.fenrir.db.impl.LogsStorage;
import dev.ragnarok.fenrir.db.interfaces.ILogsStorage;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.domain.IAttachmentsRepository;
import dev.ragnarok.fenrir.domain.IBlacklistRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.domain.impl.AttachmentsRepository;
import dev.ragnarok.fenrir.domain.impl.BlacklistRepository;
import dev.ragnarok.fenrir.media.gif.AppGifPlayerFactory;
import dev.ragnarok.fenrir.media.gif.IGifPlayerFactory;
import dev.ragnarok.fenrir.media.voice.IVoicePlayerFactory;
import dev.ragnarok.fenrir.media.voice.VoicePlayerFactory;
import dev.ragnarok.fenrir.push.IDeviceIdProvider;
import dev.ragnarok.fenrir.push.IPushRegistrationResolver;
import dev.ragnarok.fenrir.push.PushRegistrationResolver;
import dev.ragnarok.fenrir.settings.IProxySettings;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.ProxySettingsImpl;
import dev.ragnarok.fenrir.settings.SettingsImpl;
import dev.ragnarok.fenrir.upload.IUploadManager;
import dev.ragnarok.fenrir.upload.UploadManagerImpl;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;

public class Injection {

    private static final Object UPLOADMANAGERLOCK = new Object();
    private static final IProxySettings proxySettings = new ProxySettingsImpl(provideApplicationContext());
    private static final INetworker networkerInstance = new Networker(proxySettings);
    private static volatile ICaptchaProvider captchaProvider;
    private static volatile IPushRegistrationResolver resolver;
    private static volatile IUploadManager uploadManager;
    private static volatile IAttachmentsRepository attachmentsRepository;
    private static volatile IBlacklistRepository blacklistRepository;
    private static volatile ILogsStorage logsStore;

    public static IProxySettings provideProxySettings() {
        return proxySettings;
    }

    public static IGifPlayerFactory provideGifPlayerFactory() {
        return new AppGifPlayerFactory(proxySettings);
    }

    public static IVoicePlayerFactory provideVoicePlayerFactory() {
        return new VoicePlayerFactory(provideApplicationContext(), provideProxySettings(), provideSettings().other());
    }

    public static IPushRegistrationResolver providePushRegistrationResolver() {
        if (isNull(resolver)) {
            synchronized (Injection.class) {
                if (isNull(resolver)) {
                    Context context = provideApplicationContext();
                    IDeviceIdProvider deviceIdProvider = () -> Utils.getDeviceId(context);
                    resolver = new PushRegistrationResolver(deviceIdProvider, provideSettings(), provideNetworkInterfaces());
                }
            }
        }

        return resolver;
    }

    public static IUploadManager provideUploadManager() {
        if (uploadManager == null) {
            synchronized (UPLOADMANAGERLOCK) {
                if (uploadManager == null) {
                    uploadManager = new UploadManagerImpl(App.getInstance(), provideNetworkInterfaces(),
                            provideStores(), provideAttachmentsRepository(), Repository.INSTANCE.getWalls());
                }
            }
        }

        return uploadManager;
    }

    public static ICaptchaProvider provideCaptchaProvider() {
        if (isNull(captchaProvider)) {
            synchronized (Injection.class) {
                if (isNull(captchaProvider)) {
                    captchaProvider = new CaptchaProvider(provideApplicationContext(), provideMainThreadScheduler());
                }
            }
        }
        return captchaProvider;
    }

    public static IAttachmentsRepository provideAttachmentsRepository() {
        if (isNull(attachmentsRepository)) {
            synchronized (Injection.class) {
                if (isNull(attachmentsRepository)) {
                    attachmentsRepository = new AttachmentsRepository(provideStores().attachments(), Repository.INSTANCE.getOwners());
                }
            }
        }

        return attachmentsRepository;
    }

    public static INetworker provideNetworkInterfaces() {
        return networkerInstance;
    }

    public static IStorages provideStores() {
        return AppStorages.getInstance(App.getInstance());
    }

    public static IBlacklistRepository provideBlacklistRepository() {
        if (isNull(blacklistRepository)) {
            synchronized (Injection.class) {
                if (isNull(blacklistRepository)) {
                    blacklistRepository = new BlacklistRepository();
                }
            }
        }
        return blacklistRepository;
    }

    public static ISettings provideSettings() {
        return SettingsImpl.getInstance(App.getInstance());
    }

    public static ILogsStorage provideLogsStore() {
        if (isNull(logsStore)) {
            synchronized (Injection.class) {
                if (isNull(logsStore)) {
                    logsStore = new LogsStorage(provideApplicationContext());
                }
            }
        }
        return logsStore;
    }

    public static Scheduler provideMainThreadScheduler() {
        return AndroidSchedulers.mainThread();
    }

    public static Context provideApplicationContext() {
        return App.getInstance();
    }
}