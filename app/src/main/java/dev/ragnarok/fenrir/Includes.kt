package dev.ragnarok.fenrir

import android.content.Context
import dev.ragnarok.fenrir.App.Companion.instance
import dev.ragnarok.fenrir.api.CaptchaProvider
import dev.ragnarok.fenrir.api.ICaptchaProvider
import dev.ragnarok.fenrir.api.impl.Networker
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.db.impl.AppStorages
import dev.ragnarok.fenrir.db.impl.LogsStorage
import dev.ragnarok.fenrir.db.interfaces.ILogsStorage
import dev.ragnarok.fenrir.db.interfaces.IStorages
import dev.ragnarok.fenrir.domain.IAttachmentsRepository
import dev.ragnarok.fenrir.domain.IBlacklistRepository
import dev.ragnarok.fenrir.domain.Repository.owners
import dev.ragnarok.fenrir.domain.Repository.walls
import dev.ragnarok.fenrir.domain.impl.AttachmentsRepository
import dev.ragnarok.fenrir.domain.impl.BlacklistRepository
import dev.ragnarok.fenrir.media.gif.AppGifPlayerFactory
import dev.ragnarok.fenrir.media.gif.IGifPlayerFactory
import dev.ragnarok.fenrir.media.voice.IVoicePlayerFactory
import dev.ragnarok.fenrir.media.voice.VoicePlayerFactory
import dev.ragnarok.fenrir.push.IPushRegistrationResolver
import dev.ragnarok.fenrir.push.PushRegistrationResolver
import dev.ragnarok.fenrir.settings.IProxySettings
import dev.ragnarok.fenrir.settings.ISettings
import dev.ragnarok.fenrir.settings.ProxySettingsImpl
import dev.ragnarok.fenrir.settings.SettingsImpl
import dev.ragnarok.fenrir.upload.IUploadManager
import dev.ragnarok.fenrir.upload.UploadManagerImpl
import dev.ragnarok.fenrir.util.Utils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler

object Includes {
    @JvmStatic
    val proxySettings: IProxySettings by lazy {
        ProxySettingsImpl(provideApplicationContext())
    }

    @JvmStatic
    val gifPlayerFactory: IGifPlayerFactory by lazy {
        AppGifPlayerFactory(proxySettings)
    }

    @JvmStatic
    val voicePlayerFactory: IVoicePlayerFactory by lazy {
        VoicePlayerFactory(
            provideApplicationContext(),
            proxySettings,
            settings.other()
        )
    }

    @JvmStatic
    val pushRegistrationResolver: IPushRegistrationResolver by lazy {
        PushRegistrationResolver(
            { Utils.getDeviceId(provideApplicationContext()) },
            settings,
            networkInterfaces
        )
    }

    @JvmStatic
    val uploadManager: IUploadManager by lazy {
        UploadManagerImpl(
            instance, networkInterfaces,
            stores, attachmentsRepository, walls
        )
    }

    @JvmStatic
    val captchaProvider: ICaptchaProvider by lazy {
        CaptchaProvider(provideApplicationContext(), provideMainThreadScheduler())
    }

    @JvmStatic
    val attachmentsRepository: IAttachmentsRepository by lazy {
        AttachmentsRepository(stores.attachments(), owners)
    }

    @JvmStatic
    val networkInterfaces: INetworker by lazy {
        Networker(proxySettings)
    }

    @JvmStatic
    val stores: IStorages by lazy {
        AppStorages.getInstance(instance)
    }

    @JvmStatic
    val blacklistRepository: IBlacklistRepository by lazy {
        BlacklistRepository()
    }

    @JvmStatic
    val settings: ISettings by lazy {
        SettingsImpl.getInstance(instance)
    }

    @JvmStatic
    val logsStore: ILogsStorage by lazy {
        LogsStorage(provideApplicationContext())
    }

    @JvmStatic
    fun provideMainThreadScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    @JvmStatic
    fun provideApplicationContext(): Context {
        return instance
    }
}