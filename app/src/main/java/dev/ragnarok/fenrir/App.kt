package dev.ragnarok.fenrir

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.internal.ConstructorConstructor
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.longpoll.NotificationHelper
import dev.ragnarok.fenrir.media.music.MusicPlaybackController
import dev.ragnarok.fenrir.model.PeerUpdate
import dev.ragnarok.fenrir.model.SentMsg
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.module.rlottie.RLottieDrawable
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.service.ErrorLocalizer
import dev.ragnarok.fenrir.service.KeepLongpollService
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.PersistentLogger
import dev.ragnarok.fenrir.util.RxUtils
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.existfile.FileExistJVM
import dev.ragnarok.fenrir.util.existfile.FileExistNative
import ealvatag.tag.TagOptionSingleton
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.plugins.RxJavaPlugins

class App : Application() {
    private val compositeDisposable = CompositeDisposable()

    @SuppressLint("UnsafeExperimentalUsageWarning")
    override fun onCreate() {
        sInstanse = this

        sApplicationHandler = Handler(mainLooper)
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(Settings.get().ui().nightMode)
        if (Settings.get().other().isEnable_native) {
            FenrirNative.loadNativeLibrary(object : FenrirNative.NativeOnException {
                override fun onException(e: Error) {
                    PersistentLogger.logThrowable("NativeError", e)
                }
            })
        }
        FenrirNative.updateAppContext(this)
        FenrirNative.updateDensity(object : FenrirNative.OnGetDensity {
            override fun get(): Float {
                return Utils.density
            }
        })
        ConstructorConstructor.setLogUnsafe(Settings.get().other().isDeveloper_mode)

        if (FenrirNative.isNativeLoaded) {
            MusicPlaybackController.tracksExist = FileExistNative()
        } else {
            MusicPlaybackController.tracksExist = FileExistJVM()
        }

        Utils.isCompressTraffic = Settings.get().other().isCompress_traffic
        RLottieDrawable.setCacheResourceAnimation(Settings.get().other().isEnable_cache_ui_anim)
        TagOptionSingleton.getInstance().isAndroid = true
        MusicPlaybackController.registerBroadcast(this)
        PicassoInstance.init(this, Includes.proxySettings)
        if (Settings.get().other().isKeepLongpoll) {
            KeepLongpollService.start(this)
        }
        compositeDisposable.add(messages
            .observePeerUpdates()
            .flatMap { source: List<PeerUpdate> -> Flowable.fromIterable(source) }
            .subscribe({ update: PeerUpdate ->
                if (update.readIn != null) {
                    NotificationHelper.tryCancelNotificationForPeer(
                        this,
                        update.accountId,
                        update.peerId
                    )
                }
            }, RxUtils.ignore())
        )
        compositeDisposable.add(
            messages
                .observeSentMessages()
                .subscribe({ sentMsg: SentMsg ->
                    NotificationHelper.tryCancelNotificationForPeer(
                        this,
                        sentMsg.accountId,
                        sentMsg.peerId
                    )
                }, RxUtils.ignore())
        )
        compositeDisposable.add(
            messages
                .observeMessagesSendErrors()
                .toMainThread()
                .subscribe({ throwable: Throwable ->
                    run {
                        CreateCustomToast(this).showToastError(
                            ErrorLocalizer.localizeThrowable(this, throwable)
                        ); throwable.printStackTrace()
                    }
                }, RxUtils.ignore())
        )
        RxJavaPlugins.setErrorHandler {
            Handler(mainLooper).post {
                if (Settings.get().other().isDeveloper_mode) {
                    CreateCustomToast(this).showToastError(
                        ErrorLocalizer.localizeThrowable(
                            this,
                            it
                        )
                    )
                }
            }
        }
    }

    companion object {
        @Volatile
        private var sInstanse: App? = null

        @Volatile
        private var sApplicationHandler: Handler? = null

        val applicationHandler: Handler?
            get() {
                return sApplicationHandler
            }

        val instance: App
            get() {
                checkNotNull(sInstanse) { "App instance is null!!! WTF???" }
                return sInstanse!!
            }
    }
}
