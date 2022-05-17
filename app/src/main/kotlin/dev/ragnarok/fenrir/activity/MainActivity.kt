package dev.ragnarok.fenrir.activity

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.view.*
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dev.ragnarok.fenrir.*
import dev.ragnarok.fenrir.Includes.networkInterfaces
import dev.ragnarok.fenrir.Includes.provideMainThreadScheduler
import dev.ragnarok.fenrir.Includes.proxySettings
import dev.ragnarok.fenrir.Includes.pushRegistrationResolver
import dev.ragnarok.fenrir.activity.ActivityUtils.checkInputExist
import dev.ragnarok.fenrir.activity.ActivityUtils.isMimeAudio
import dev.ragnarok.fenrir.activity.EnterPinActivity.Companion.getClass
import dev.ragnarok.fenrir.activity.PhotoPagerActivity.Companion.newInstance
import dev.ragnarok.fenrir.activity.qr.CameraScanActivity
import dev.ragnarok.fenrir.db.Stores
import dev.ragnarok.fenrir.dialog.ResolveDomainDialog
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.impl.CountersInteractor
import dev.ragnarok.fenrir.fragment.*
import dev.ragnarok.fenrir.fragment.AbsNavigationFragment.NavigationDrawerCallbacks
import dev.ragnarok.fenrir.fragment.ChatFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.PreferencesFragment.Companion.cleanCache
import dev.ragnarok.fenrir.fragment.PreferencesFragment.Companion.cleanUICache
import dev.ragnarok.fenrir.fragment.UserDetailsFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.attachments.*
import dev.ragnarok.fenrir.fragment.conversation.ConversationFragmentFactory
import dev.ragnarok.fenrir.fragment.fave.FaveTabsFragment
import dev.ragnarok.fenrir.fragment.friends.FriendsTabsFragment
import dev.ragnarok.fenrir.fragment.search.AudioSearchTabsFragment
import dev.ragnarok.fenrir.fragment.search.SearchTabsFragment
import dev.ragnarok.fenrir.fragment.search.SingleTabSearchFragment
import dev.ragnarok.fenrir.fragment.wallattachments.WallAttachmentsFragmentFactory
import dev.ragnarok.fenrir.fragment.wallattachments.WallSearchCommentsAttachmentsFragment
import dev.ragnarok.fenrir.link.LinkHelper
import dev.ragnarok.fenrir.listener.*
import dev.ragnarok.fenrir.media.music.MusicPlaybackController
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.ServiceToken
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.bindToServiceWithoutStart
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.isPlaying
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.stop
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.unbindFromService
import dev.ragnarok.fenrir.media.music.MusicPlaybackService
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.Option
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem
import dev.ragnarok.fenrir.model.drawer.RecentChat
import dev.ragnarok.fenrir.model.drawer.SectionMenuItem
import dev.ragnarok.fenrir.mvp.presenter.DocsListPresenter
import dev.ragnarok.fenrir.mvp.view.IVideosListView
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.place.PlaceProvider
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.ISettings
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.settings.SwipesChatMode
import dev.ragnarok.fenrir.settings.theme.ThemesController.currentStyle
import dev.ragnarok.fenrir.settings.theme.ThemesController.nextRandom
import dev.ragnarok.fenrir.upload.UploadUtils
import dev.ragnarok.fenrir.util.*
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.HelperSimple.needHelp
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.view.zoomhelper.ZoomHelper.Companion.getInstance
import io.reactivex.rxjava3.disposables.CompositeDisposable

open class MainActivity : AppCompatActivity(), NavigationDrawerCallbacks, OnSectionResumeCallback,
    AppStyleable, PlaceProvider, ServiceConnection, UpdatableNavigation,
    NavigationBarView.OnItemSelectedListener {
    private val mCompositeDisposable = CompositeDisposable()
    private val postResumeActions: MutableList<Action<MainActivity>> = ArrayList(0)
    private val requestEnterPin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode != RESULT_OK) {
            finish()
        }
    }
    protected var mAccountId = 0
    private val requestEnterPinZero = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode != RESULT_OK) {
            finish()
        } else {
            Settings.get().ui().getDefaultPage(mAccountId).tryOpenWith(this)
        }
    }
    private val requestQRScan = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanner = result.data?.extras?.getString(Extra.URL)
            if (scanner.nonNullNoEmpty()) {
                MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.qr_code)
                    .setMessage(scanner)
                    .setTitle(getString(R.string.scan_qr))
                    .setPositiveButton(R.string.open) { _: DialogInterface?, _: Int ->
                        LinkHelper.openUrl(
                            this,
                            mAccountId,
                            scanner, false
                        )
                    }
                    .setNeutralButton(R.string.copy_text) { _: DialogInterface?, _: Int ->
                        val clipboard = getSystemService(
                            CLIPBOARD_SERVICE
                        ) as ClipboardManager?
                        val clip = ClipData.newPlainText("response", scanner)
                        clipboard?.setPrimaryClip(clip)
                        CreateCustomToast(this).showToast(R.string.copied_to_clipboard)
                    }
                    .setCancelable(true)
                    .create().show()
            }
        }
    }
    private val requestLogin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        mAccountId = Settings.get()
            .accounts()
            .current
        if (mAccountId == ISettings.IAccountsSettings.INVALID_ID) {
            supportFinishAfterTransition()
        }
    }
    protected var mLayoutRes = if (Settings.get().main().isSnow_mode) snowLayout else normalLayout
    protected var mLastBackPressedTime: Long = 0

    /**
     * Атрибуты секции, которая на данный момент находится на главном контейнере экрана
     */
    private var mCurrentFrontSection: AbsMenuItem? = null
    private var mToolbar: Toolbar? = null
    private var mBottomNavigation: BottomNavigationView? = null
    private var mBottomNavigationContainer: ViewGroup? = null
    private var mViewFragment: FragmentContainerView? = null
    private val requestLoginZero = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        mAccountId = Settings.get()
            .accounts()
            .current
        if (mAccountId == ISettings.IAccountsSettings.INVALID_ID) {
            supportFinishAfterTransition()
        } else {
            Settings.get().ui().getDefaultPage(mAccountId).tryOpenWith(this)
            checkFCMRegistration(true)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP && needHelp(
                    HelperSimple.LOLLIPOP_21,
                    1
                )
            ) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.info)
                    .setMessage(R.string.lollipop21)
                    .setCancelable(false)
                    .setPositiveButton(R.string.button_ok, null)
                    .show()
            }
        }
    }
    private val mOnBackStackChangedListener = FragmentManager.OnBackStackChangedListener {
        resolveToolbarNavigationIcon()
        keyboardHide()
    }
    private var mAudioPlayServiceToken: ServiceToken? = null
    private var isActivityDestroyed = false

    /**
     * First - DrawerItem, second - Clear back stack before adding
     */
    private var mTargetPage: Pair<AbsMenuItem, Boolean>? = null
    private var resumed = false
    private var isZoomPhoto = false
    private val snowLayout: Int
        get() = if (Settings.get()
                .other().is_side_navigation()
        ) R.layout.activity_main_side_with_snow else R.layout.activity_main_with_snow
    private val normalLayout: Int
        get() = if (Settings.get()
                .other().is_side_navigation()
        ) R.layout.activity_main_side else R.layout.activity_main

    @MainActivityTransforms
    protected open fun getMainActivityTransform(): Int {
        return MainActivityTransforms.MAIN
    }

    private fun postResume(action: Action<MainActivity>) {
        if (resumed) {
            action.call(this)
        } else {
            postResumeActions.add(action)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Utils.updateActivityContext(newBase))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (!isZoomPhoto) {
            super.dispatchTouchEvent(ev)
        } else getInstance()?.dispatchTouchEvent(ev, this) == true || super.dispatchTouchEvent(ev)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null && getMainActivityTransform() == MainActivityTransforms.MAIN) {
            nextRandom()
        }
        setTheme(currentStyle())
        delegate.applyDayNight()
        Utils.prepareDensity(this)
        super.onCreate(savedInstanceState)
        isActivityDestroyed = false
        isZoomPhoto = Settings.get().other().isDo_zoom_photo
        mCompositeDisposable.add(
            Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(provideMainThreadScheduler())
                .subscribe { onCurrentAccountChange(it) })
        mCompositeDisposable.add(
            proxySettings
                .observeActive().observeOn(provideMainThreadScheduler())
                .subscribe { stop() })
        mCompositeDisposable.add(Stores.instance
            .dialogs()
            .observeUnreadDialogsCount()
            .filter { it.first == mAccountId }
            .toMainThread()
            .subscribe { updateMessagesBagde(it.second) })
        bindToAudioPlayService()
        setContentView(mLayoutRes)
        mAccountId = Settings.get()
            .accounts()
            .current
        setStatusbarColored(true, Settings.get().ui().isDarkModeEnabled(this))
        val mDrawerLayout = findViewById<DrawerLayout>(R.id.my_drawer_layout)
        if (mDrawerLayout != null) {
            if (Settings.get().other().is_side_navigation()) {
                val anim = ObjectAnimator.ofPropertyValuesHolder(
                    findViewById<View>(R.id.main_root), PropertyValuesHolder.ofFloat(
                        View.ALPHA, 1f, 1f, 0.6f
                    ),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f, 0f, 100f)
                )
                anim.interpolator = LinearInterpolator()
                mDrawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                            anim.currentPlayTime =
                                (slideOffset * anim.duration).toLong().coerceAtMost(anim.duration)
                        } else {
                            anim.setCurrentFraction(slideOffset)
                        }
                    }

                    override fun onDrawerOpened(drawerView: View) {
                        anim.start()
                    }

                    override fun onDrawerClosed(drawerView: View) {
                        anim.cancel()
                    }
                })
            }
            mDrawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerStateChanged(newState: Int) {
                    if (newState != DrawerLayout.STATE_IDLE || mDrawerLayout.isDrawerOpen(
                            GravityCompat.START
                        )
                    ) {
                        keyboardHide()
                    }
                }
            })
            navigationFragment?.setUp(R.id.additional_navigation_menu, mDrawerLayout)
        }
        mBottomNavigation = findViewById(R.id.bottom_navigation_menu)
        mBottomNavigation?.setOnItemSelectedListener(this)
        mBottomNavigationContainer = findViewById(R.id.bottom_navigation_menu_container)
        mViewFragment = findViewById(R.id.fragment)
        supportFragmentManager.addOnBackStackChangedListener(mOnBackStackChangedListener)
        resolveToolbarNavigationIcon()
        updateMessagesBagde(
            Stores.instance
                .dialogs()
                .getUnreadDialogsCount(mAccountId)
        )
        if (savedInstanceState == null) {
            val intentWasHandled = handleIntent(intent, true)
            if (!isAuthValid) {
                if (intentWasHandled) {
                    startAccountsActivity()
                } else {
                    startAccountsActivityZero()
                }
            } else {
                if (getMainActivityTransform() == MainActivityTransforms.MAIN) {
                    checkFCMRegistration(false)
                    mCompositeDisposable.add(MusicPlaybackController.tracksExist.findAllAudios(this)
                        .fromIOToMain()
                        .subscribe(RxUtils.dummy()) { t ->
                            if (Settings.get().other().isDeveloper_mode) {
                                Utils.showErrorInAdapter(this, t)
                            }
                        })
                    mCompositeDisposable.add(
                        InteractorFactory.createStickersInteractor().PlaceToStickerCache(this)
                            .fromIOToMain()
                            .subscribe(RxUtils.dummy(), RxUtils.ignore())
                    )
                    if (!Settings.get().other().appStoredVersionEqual()) {
                        cleanUICache(this, false)
                    }
                    if (Settings.get().other().isDelete_cache_images) {
                        cleanCache(this, false)
                    }
                }
                updateNotificationCount(mAccountId)
                val needPin = (Settings.get().security().isUsePinForEntrance
                        && !intent.getBooleanExtra(EXTRA_NO_REQUIRE_PIN, false) && !Settings.get()
                    .security().isDelayedAllow)
                if (needPin) {
                    if (!intentWasHandled) {
                        startEnterPinActivityZero()
                    } else {
                        startEnterPinActivity()
                    }
                } else {
                    if (!intentWasHandled) {
                        Settings.get().ui().getDefaultPage(mAccountId).tryOpenWith(this)
                    }
                }
            }
        }
        CurrentTheme.dumpDynamicColors(this)
    }

    private fun updateNotificationCount(account: Int) {
        mCompositeDisposable.add(CountersInteractor(networkInterfaces).getApiCounters(account)
            .fromIOToMain()
            .subscribe({ counters -> updateNotificationsBadge(counters) }) { removeNotificationsBadge() })
    }

    override fun onPause() {
        resumed = false
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        for (action in postResumeActions) {
            action.call(this)
        }
        postResumeActions.clear()
    }

    private fun startEnterPinActivity() {
        val intent = Intent(this, getClass(this))
        requestEnterPin.launch(intent)
    }

    private fun startEnterPinActivityZero() {
        val intent = Intent(this, getClass(this))
        requestEnterPinZero.launch(intent)
    }

    private fun checkFCMRegistration(onlyCheckGMS: Boolean) {
        if (!checkPlayServices(this)) {
            if (!Settings.get().other().isDisabledErrorFCM) {
                mViewFragment?.let {
                    Utils.ThemedSnack(
                        it,
                        getString(R.string.this_device_does_not_support_fcm),
                        BaseTransientBottomBar.LENGTH_LONG
                    )
                        .setAnchorView(mBottomNavigationContainer)
                        .setAction(R.string.button_access) {
                            Settings.get().other().setDisableErrorFCM(true)
                        }
                        .show()
                }
            }
            return
        }
        if (onlyCheckGMS) {
            return
        }
        val resolver = pushRegistrationResolver
        mCompositeDisposable.add(
            resolver.resolvePushRegistration()
                .fromIOToMain()
                .subscribe(RxUtils.dummy(), RxUtils.ignore())
        )

        //RequestHelper.checkPushRegistration(this);
    }

    private fun bindToAudioPlayService() {
        if (!isActivityDestroyed && mAudioPlayServiceToken == null) {
            mAudioPlayServiceToken = bindToServiceWithoutStart(this, this)
        }
    }

    private fun resolveToolbarNavigationIcon() {
        mToolbar ?: return
        val manager = supportFragmentManager
        if (manager.backStackEntryCount > 1 || frontFragment is CanBackPressedCallback && (frontFragment as CanBackPressedCallback?)?.canBackPressed() == true) {
            mToolbar?.setNavigationIcon(R.drawable.arrow_left)
            mToolbar?.setNavigationOnClickListener { onBackPressed() }
        } else {
            if (!isFragmentWithoutNavigation) {
                mToolbar?.setNavigationIcon(R.drawable.client_round)
                mToolbar?.setNavigationOnClickListener {
                    val menus = ModalBottomSheetDialogFragment.Builder()
                    menus.add(
                        OptionRequest(
                            R.id.button_ok,
                            getString(R.string.set_offline),
                            R.drawable.offline,
                            true
                        )
                    )
                    menus.add(
                        OptionRequest(
                            R.id.button_cancel,
                            getString(R.string.open_clipboard_url),
                            R.drawable.web,
                            false
                        )
                    )
                    menus.add(
                        OptionRequest(
                            R.id.action_preferences,
                            getString(R.string.settings),
                            R.drawable.preferences,
                            true
                        )
                    )
                    menus.add(
                        OptionRequest(
                            R.id.button_camera,
                            getString(R.string.scan_qr),
                            R.drawable.qr_code,
                            false
                        )
                    )
                    menus.show(
                        supportFragmentManager,
                        "left_options",
                        object : ModalBottomSheetDialogFragment.Listener {
                            override fun onModalOptionSelected(option: Option) {
                                if (option.id == R.id.button_ok) {
                                    mCompositeDisposable.add(InteractorFactory.createAccountInteractor()
                                        .setOffline(
                                            Settings.get().accounts().current
                                        )
                                        .fromIOToMain()
                                        .subscribe({ onSetOffline(it) }) {
                                            onSetOffline(
                                                false
                                            )
                                        })
                                } else if (option.id == R.id.button_cancel) {
                                    val clipBoard =
                                        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                                    if (clipBoard != null && clipBoard.primaryClip != null && (clipBoard.primaryClip?.itemCount
                                            ?: 0) > 0 && (clipBoard.primaryClip
                                            ?: return).getItemAt(0).text != null
                                    ) {
                                        val temp =
                                            clipBoard.primaryClip?.getItemAt(0)?.text.toString()
                                        LinkHelper.openUrl(
                                            this@MainActivity,
                                            mAccountId,
                                            temp,
                                            false
                                        )
                                    }
                                } else if (option.id == R.id.action_preferences) {
                                    PlaceFactory.getPreferencesPlace(mAccountId)
                                        .tryOpenWith(this@MainActivity)
                                } else if (option.id == R.id.button_camera) {
                                    val intent =
                                        Intent(this@MainActivity, CameraScanActivity::class.java)
                                    requestQRScan.launch(intent)
                                }
                            }
                        })
                }
            } else {
                mToolbar?.setNavigationIcon(R.drawable.arrow_left)
                if (getMainActivityTransform() != MainActivityTransforms.SWIPEBLE) {
                    mToolbar?.setNavigationOnClickListener {
                        openNavigationPage(
                            AbsNavigationFragment.SECTION_ITEM_FEED,
                            false
                        )
                    }
                } else {
                    mToolbar?.setNavigationOnClickListener { onBackPressed() }
                }
            }
        }
    }

    private fun onSetOffline(success: Boolean) {
        if (success) CreateCustomToast(this).showToast(R.string.succ_offline) else CreateCustomToast(
            this
        ).showToastError(R.string.err_offline)
    }

    private fun onCurrentAccountChange(newAccountId: Int) {
        mAccountId = newAccountId
        Accounts.showAccountSwitchedToast(this)
        updateNotificationCount(newAccountId)
        if (!Settings.get().other().isDeveloper_mode) {
            stop()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Logger.d(TAG, "onNewIntent, intent: $intent")
        handleIntent(intent, false)
    }

    private fun handleIntent(intent: Intent?, isMain: Boolean): Boolean {
        if (intent == null) {
            return false
        }
        if (ACTION_OPEN_WALL == intent.action) {
            val owner_id = intent.extras!!.getInt(Extra.OWNER_ID)
            PlaceFactory.getOwnerWallPlace(mAccountId, owner_id, null).tryOpenWith(this)
            return true
        }
        if (ACTION_SWITH_ACCOUNT == intent.action) {
            val newAccountId = intent.extras!!.getInt(Extra.ACCOUNT_ID)
            if (Settings.get().accounts().current != newAccountId) {
                Settings.get()
                    .accounts().current = newAccountId
                mAccountId = newAccountId
            }
            intent.action = ACTION_MAIN
        }
        if (ACTION_SHORTCUT_WALL == intent.action) {
            val newAccountId = intent.extras!!.getInt(Extra.ACCOUNT_ID)
            val ownerId = intent.extras!!.getInt(Extra.OWNER_ID)
            if (Settings.get().accounts().current != newAccountId) {
                Settings.get()
                    .accounts().current = newAccountId
                mAccountId = newAccountId
            }
            clearBackStack()
            openPlace(PlaceFactory.getOwnerWallPlace(newAccountId, ownerId, null))
            return true
        }
        val extras = intent.extras
        val action = intent.action
        Logger.d(TAG, "handleIntent, extras: $extras, action: $action")
        if (Intent.ACTION_SEND_MULTIPLE == action) {
            val mime = intent.type
            if (getMainActivityTransform() == MainActivityTransforms.MAIN && extras != null && mime.nonNullNoEmpty() && isMimeAudio(
                    mime
                ) && extras.containsKey(Intent.EXTRA_STREAM)
            ) {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (uris.nonNullNoEmpty()) {
                    val playlist = ArrayList<Audio>(
                        uris.size
                    )
                    for (i in uris) {
                        val track = UploadUtils.findFileName(this, i) ?: return false
                        var TrackName = track.replace(".mp3", "")
                        var Artist = ""
                        val arr = TrackName.split(" - ").toTypedArray()
                        if (arr.size > 1) {
                            Artist = arr[0]
                            TrackName = TrackName.replace("$Artist - ", "")
                        }
                        val tmp = Audio().setIsLocal().setThumb_image_big(
                            "share_$i"
                        ).setThumb_image_little("share_$i").setUrl(i.toString())
                            .setOwnerId(mAccountId).setArtist(Artist).setTitle(TrackName)
                            .setId(i.toString().hashCode())
                        playlist.add(tmp)
                    }
                    intent.removeExtra(Intent.EXTRA_STREAM)
                    startForPlayList(this, playlist, 0, false)
                    PlaceFactory.getPlayerPlace(mAccountId).tryOpenWith(this)
                }
            }
        }
        if (extras != null && checkInputExist(this)) {
            mCurrentFrontSection = AbsNavigationFragment.SECTION_ITEM_DIALOGS
            openNavigationPage(AbsNavigationFragment.SECTION_ITEM_DIALOGS, false)
            return true
        }
        if (ACTION_SEND_ATTACHMENTS == action) {
            mCurrentFrontSection = AbsNavigationFragment.SECTION_ITEM_DIALOGS
            openNavigationPage(AbsNavigationFragment.SECTION_ITEM_DIALOGS, false)
            return true
        }
        if (ACTION_OPEN_PLACE == action) {
            val place: Place = intent.getParcelableExtra(Extra.PLACE) ?: return false
            openPlace(place)
            return if (place.type == Place.CHAT) {
                Settings.get().ui().swipes_chat_mode != SwipesChatMode.SLIDR || Settings.get()
                    .ui().swipes_chat_mode == SwipesChatMode.DISABLED
            } else true
        }
        if (ACTION_OPEN_AUDIO_PLAYER == action) {
            openPlace(PlaceFactory.getPlayerPlace(mAccountId))
            return false
        }
        if (ACTION_CHAT_FROM_SHORTCUT == action) {
            val aid = intent.extras!!.getInt(Extra.ACCOUNT_ID)
            val prefsAid = Settings.get()
                .accounts()
                .current
            if (prefsAid != aid) {
                Settings.get()
                    .accounts().current = aid
            }
            val peerId = intent.extras!!.getInt(Extra.PEER_ID)
            val title = intent.getStringExtra(Extra.TITLE)
            val imgUrl = intent.getStringExtra(Extra.IMAGE)
            val peer = Peer(peerId).setTitle(title).setAvaUrl(imgUrl)
            PlaceFactory.getChatPlace(aid, aid, peer).tryOpenWith(this)
            return Settings.get().ui().swipes_chat_mode != SwipesChatMode.SLIDR || Settings.get()
                .ui().swipes_chat_mode == SwipesChatMode.DISABLED
        }
        if (Intent.ACTION_VIEW == action) {
            val data = intent.data
            val mime = intent.type ?: ""
            if (getMainActivityTransform() == MainActivityTransforms.MAIN && mime.nonNullNoEmpty() && isMimeAudio(
                    mime
                )
            ) {
                val track = UploadUtils.findFileName(this, data) ?: return false
                var TrackName = track.replace(".mp3", "")
                var Artist = ""
                val arr = TrackName.split(" - ").toTypedArray()
                if (arr.size > 1) {
                    Artist = arr[0]
                    TrackName = TrackName.replace("$Artist - ", "")
                }
                val tmp =
                    Audio().setIsLocal().setThumb_image_big("share_$data").setThumb_image_little(
                        "share_$data"
                    ).setUrl(data.toString()).setOwnerId(mAccountId).setArtist(Artist)
                        .setTitle(TrackName).setId(data.toString().hashCode())
                startForPlayList(this, ArrayList(listOf(tmp)), 0, false)
                PlaceFactory.getPlayerPlace(mAccountId).tryOpenWith(this)
                return false
            }
            LinkHelper.openUrl(this, mAccountId, data.toString(), isMain)
            return true
        }
        return false
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        mToolbar?.setNavigationOnClickListener(null)
        mToolbar?.setOnMenuItemClickListener(null)
        super.setSupportActionBar(toolbar)
        mToolbar = toolbar
        resolveToolbarNavigationIcon()
    }

    private fun openChat(accountId: Int, messagesOwnerId: Int, peer: Peer, closeMain: Boolean) {
        if (Settings.get().other().isEnable_show_recent_dialogs) {
            val recentChat = RecentChat(accountId, peer.id, peer.getTitle(), peer.avaUrl)
            navigationFragment?.appendRecentChat(recentChat)
            navigationFragment?.refreshNavigationItems()
            navigationFragment?.selectPage(recentChat)
        }
        if (Settings.get().ui().swipes_chat_mode == SwipesChatMode.DISABLED) {
            val chatFragment = newInstance(accountId, messagesOwnerId, peer)
            attachToFront(chatFragment)
        } else {
            if (Settings.get()
                    .ui().swipes_chat_mode == SwipesChatMode.SLIDR && getMainActivityTransform() == MainActivityTransforms.MAIN
            ) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.action = ChatActivity.ACTION_OPEN_PLACE
                intent.putExtra(
                    Extra.PLACE,
                    PlaceFactory.getChatPlace(accountId, messagesOwnerId, peer)
                )
                startActivity(intent)
                if (closeMain) {
                    finish()
                    overridePendingTransition(0, 0)
                }
            } else if (Settings.get()
                    .ui().swipes_chat_mode == SwipesChatMode.SLIDR && getMainActivityTransform() != MainActivityTransforms.MAIN
            ) {
                val chatFragment = newInstance(accountId, messagesOwnerId, peer)
                attachToFront(chatFragment)
            } else {
                throw UnsupportedOperationException()
            }
        }
    }

    private fun openRecentChat(chat: RecentChat) {
        val accountId = mAccountId
        val messagesOwnerId = mAccountId
        openChat(
            accountId,
            messagesOwnerId,
            Peer(chat.peerId).setAvaUrl(chat.iconUrl).setTitle(chat.title),
            false
        )
    }

    private fun openTargetPage() {
        if (mTargetPage == null) {
            return
        }
        val item = mTargetPage?.first ?: return
        val clearBackStack = mTargetPage?.second ?: false
        if (item == mCurrentFrontSection) {
            return
        }
        if (item.type == AbsMenuItem.TYPE_ICON) {
            openNavigationPage(item, clearBackStack, true)
        }
        if (item.type == AbsMenuItem.TYPE_RECENT_CHAT) {
            openRecentChat(item as RecentChat)
        }
        mTargetPage = null
    }

    private val navigationFragment: AbsNavigationFragment?
        get() {
            val fm = supportFragmentManager
            return fm.findFragmentById(R.id.additional_navigation_menu) as AbsNavigationFragment?
        }

    private fun openNavigationPage(item: AbsMenuItem, menu: Boolean) {
        openNavigationPage(item, true, menu)
    }

    private fun startAccountsActivity() {
        val intent = Intent(this, AccountsActivity::class.java)
        requestLogin.launch(intent)
    }

    private fun startAccountsActivityZero() {
        val intent = Intent(this, AccountsActivity::class.java)
        requestLoginZero.launch(intent)
    }

    private fun clearBackStack() {
        val manager = supportFragmentManager
        /*if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }*/manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // TODO: 13.12.2017 Exception java.lang.IllegalStateException:Can not perform this action after onSaveInstanceState
        Logger.d(TAG, "Back stack was cleared")
    }

    private fun openNavigationPage(item: AbsMenuItem, clearBackStack: Boolean, menu: Boolean) {
        var doClearBackStack = clearBackStack
        if (item.type == AbsMenuItem.TYPE_RECENT_CHAT) {
            openRecentChat(item as RecentChat)
            return
        }
        val sectionDrawerItem = item as SectionMenuItem
        if (sectionDrawerItem.section == AbsNavigationFragment.PAGE_ACCOUNTS) {
            startAccountsActivity()
            return
        }
        mCurrentFrontSection = item
        navigationFragment?.selectPage(item)
        if (Settings.get().other().isDo_not_clear_back_stack && menu && isPlaying) {
            doClearBackStack = !doClearBackStack
        }
        if (doClearBackStack) {
            clearBackStack()
        }
        val aid = mAccountId
        when (sectionDrawerItem.section) {
            AbsNavigationFragment.PAGE_DIALOGS -> openPlace(
                PlaceFactory.getDialogsPlace(
                    aid,
                    aid,
                    null
                )
            )
            AbsNavigationFragment.PAGE_FRIENDS -> openPlace(
                PlaceFactory.getFriendsFollowersPlace(
                    aid,
                    aid,
                    FriendsTabsFragment.TAB_ALL_FRIENDS,
                    null
                )
            )
            AbsNavigationFragment.PAGE_GROUPS -> openPlace(
                PlaceFactory.getCommunitiesPlace(
                    aid,
                    aid
                )
            )
            AbsNavigationFragment.PAGE_PREFERENSES -> openPlace(PlaceFactory.getPreferencesPlace(aid))
            AbsNavigationFragment.PAGE_MUSIC -> openPlace(PlaceFactory.getAudiosPlace(aid, aid))
            AbsNavigationFragment.PAGE_DOCUMENTS -> openPlace(
                PlaceFactory.getDocumentsPlace(
                    aid,
                    aid,
                    DocsListPresenter.ACTION_SHOW
                )
            )
            AbsNavigationFragment.PAGE_FEED -> openPlace(PlaceFactory.getFeedPlace(aid))
            AbsNavigationFragment.PAGE_NOTIFICATION -> openPlace(
                PlaceFactory.getNotificationsPlace(
                    aid
                )
            )
            AbsNavigationFragment.PAGE_PHOTOS -> openPlace(
                PlaceFactory.getVKPhotoAlbumsPlace(
                    aid,
                    aid,
                    IVkPhotosView.ACTION_SHOW_PHOTOS,
                    null
                )
            )
            AbsNavigationFragment.PAGE_VIDEOS -> openPlace(
                PlaceFactory.getVideosPlace(
                    aid,
                    aid,
                    IVideosListView.ACTION_SHOW
                )
            )
            AbsNavigationFragment.PAGE_BOOKMARKS -> openPlace(
                PlaceFactory.getBookmarksPlace(
                    aid,
                    FaveTabsFragment.TAB_PAGES
                )
            )
            AbsNavigationFragment.PAGE_SEARCH -> openPlace(
                PlaceFactory.getSearchPlace(
                    aid,
                    SearchTabsFragment.TAB_PEOPLE
                )
            )
            AbsNavigationFragment.PAGE_NEWSFEED_COMMENTS -> openPlace(
                PlaceFactory.getNewsfeedCommentsPlace(
                    aid
                )
            )
            else -> throw IllegalArgumentException("Unknown place!!! $item")
        }
    }

    override fun onSheetItemSelected(item: AbsMenuItem, longClick: Boolean) {
        if (mCurrentFrontSection != null && mCurrentFrontSection == item) {
            return
        }
        mTargetPage = create(item, !longClick)
        //после закрытия бокового меню откроется данная страница
    }

    override fun onSheetClosed() {
        postResume(object : Action<MainActivity> {
            override fun call(target: MainActivity) {
                target.openTargetPage()
            }
        })
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        isActivityDestroyed = true
        supportFragmentManager.removeOnBackStackChangedListener(mOnBackStackChangedListener)

        //if(!bNoDestroyServiceAudio)
        unbindFromAudioPlayService()
        super.onDestroy()
    }

    private fun unbindFromAudioPlayService() {
        if (mAudioPlayServiceToken != null) {
            unbindFromService(mAudioPlayServiceToken)
            mAudioPlayServiceToken = null
        }
    }

    private val isAuthValid: Boolean
        get() = mAccountId != ISettings.IAccountsSettings.INVALID_ID

    /*
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        SwipeTouchListener.getGestureDetector().onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
     */
    fun keyboardHide() {
        try {
            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            inputManager?.hideSoftInputFromWindow(
                window.decorView.rootView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        } catch (ignored: Exception) {
        }
    }

    private val frontFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragment)

    override fun onBackPressed() {
        if (navigationFragment?.isSheetOpen == true) {
            navigationFragment?.closeSheet()
            return
        }
        val front = frontFragment
        if (front is BackPressCallback) {
            if (!(front as BackPressCallback).onBackPressed()) {
                return
            }
        }
        if (supportFragmentManager.backStackEntryCount == 1) {
            if (getMainActivityTransform() != MainActivityTransforms.SWIPEBLE) {
                if (isFragmentWithoutNavigation) {
                    openNavigationPage(AbsNavigationFragment.SECTION_ITEM_FEED, false)
                    return
                }
                if (isChatFragment) {
                    openNavigationPage(AbsNavigationFragment.SECTION_ITEM_DIALOGS, false)
                    return
                }
            }
            if (mLastBackPressedTime < 0
                || mLastBackPressedTime + DOUBLE_BACK_PRESSED_TIMEOUT > System.currentTimeMillis()
            ) {
                supportFinishAfterTransition()
                return
            }
            mLastBackPressedTime = System.currentTimeMillis()
            mViewFragment?.let {
                val bar = Snackbar.make(
                    it,
                    getString(R.string.click_back_to_exit),
                    BaseTransientBottomBar.LENGTH_SHORT
                ).setAnchorView(mBottomNavigationContainer)
                bar.setOnClickListener { bar.dismiss() }.show()
            }
        } else {
            super.onBackPressed()
        }
    }

    private val isChatFragment: Boolean
        get() = frontFragment is ChatFragment
    private val isFragmentWithoutNavigation: Boolean
        get() = frontFragment is CommentsFragment ||
                frontFragment is PostCreateFragment ||
                frontFragment is GifPagerFragment

    override fun onNavigateUp(): Boolean {
        supportFragmentManager.popBackStack()
        return true
    }

    /* Убрать выделение в боковом меню */
    private fun resetNavigationSelection() {
        mCurrentFrontSection = null
        navigationFragment?.selectPage(null)
    }

    override fun onSectionResume(sectionDrawerItem: SectionMenuItem) {
        navigationFragment?.selectPage(sectionDrawerItem)
        if (mBottomNavigation != null) {
            when (sectionDrawerItem.section) {
                AbsNavigationFragment.PAGE_FEED -> mBottomNavigation?.menu?.getItem(0)?.isChecked =
                    true
                AbsNavigationFragment.PAGE_SEARCH -> mBottomNavigation?.menu?.getItem(1)?.isChecked =
                    true
                AbsNavigationFragment.PAGE_DIALOGS -> mBottomNavigation?.menu?.getItem(2)?.isChecked =
                    true
                AbsNavigationFragment.PAGE_NOTIFICATION -> mBottomNavigation?.menu?.getItem(3)?.isChecked =
                    true
                else -> mBottomNavigation?.menu?.getItem(4)?.isChecked = true
            }
        }
        mCurrentFrontSection = sectionDrawerItem
    }

    override fun onChatResume(accountId: Int, peerId: Int, title: String?, imgUrl: String?) {
        if (Settings.get().other().isEnable_show_recent_dialogs) {
            val recentChat = RecentChat(accountId, peerId, title, imgUrl)
            navigationFragment?.appendRecentChat(recentChat)
            navigationFragment?.refreshNavigationItems()
            navigationFragment?.selectPage(recentChat)
            mCurrentFrontSection = recentChat
        }
    }

    override fun onClearSelection() {
        resetNavigationSelection()
        mCurrentFrontSection = null
    }

    override fun readAllNotifications() {
        if (Utils.isHiddenAccount(mAccountId)) return
        mCompositeDisposable.add(
            InteractorFactory.createFeedbackInteractor()
                .maskAaViewed(mAccountId)
                .fromIOToMain()
                .subscribe({
                    mBottomNavigation?.removeBadge(R.id.menu_feedback)
                    navigationFragment?.onUnreadNotificationsCountChange(0)
                }, RxUtils.ignore())
        )
    }

    private fun attachToFront(fragment: Fragment, animate: Boolean = true) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (animate) fragmentTransaction.setCustomAnimations(
            R.anim.fragment_enter,
            R.anim.fragment_exit
        )
        fragmentTransaction
            .replace(R.id.fragment, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    @Suppress("DEPRECATION")
    override fun setStatusbarColored(colored: Boolean, invertIcons: Boolean) {
        val statusbarNonColored = CurrentTheme.getStatusBarNonColored(this)
        val statusbarColored = CurrentTheme.getStatusBarColor(this)
        val w = window
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        w.statusBarColor = if (colored) statusbarColored else statusbarNonColored
        @ColorInt val navigationColor =
            if (colored) CurrentTheme.getNavigationBarColor(this) else Color.BLACK
        w.navigationBarColor = navigationColor
        if (Utils.hasMarshmallow()) {
            var flags = window.decorView.systemUiVisibility
            flags = if (invertIcons) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            window.decorView.systemUiVisibility = flags
        }
        if (Utils.hasOreo()) {
            var flags = window.decorView.systemUiVisibility
            if (invertIcons) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                w.decorView.systemUiVisibility = flags
                w.navigationBarColor = Color.WHITE
            } else {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                w.decorView.systemUiVisibility = flags
            }
        }
    }

    override fun hideMenu(hide: Boolean) {
        if (hide) {
            navigationFragment?.closeSheet()
            navigationFragment?.blockSheet()
            mBottomNavigationContainer?.visibility = View.GONE
            if (Settings.get().other().is_side_navigation()) {
                findViewById<MaterialCardView>(R.id.miniplayer_side_root)?.visibility = View.GONE
            }
        } else {
            mBottomNavigationContainer?.visibility = View.VISIBLE
            if (Settings.get().other().is_side_navigation()) {
                findViewById<MaterialCardView>(R.id.miniplayer_side_root)?.visibility = View.VISIBLE
            }
            navigationFragment?.unblockSheet()
        }
    }

    override fun openMenu(open: Boolean) {
//        if (open) {
//            getNavigationFragment().openSheet();
//        } else {
//            getNavigationFragment().closeSheet();
//        }
    }

    override fun openPlace(place: Place) {
        val args = place.safeArguments()
        when (place.type) {
            Place.VIDEO_PREVIEW -> attachToFront(VideoPreviewFragment.newInstance(args))
            Place.STORY_PLAYER -> if (getMainActivityTransform() == MainActivityTransforms.SWIPEBLE) {
                attachToFront(StoryPagerFragment.newInstance(args))
            } else {
                Utils.openPlaceWithSwipebleActivity(this, place)
            }
            Place.FRIENDS_AND_FOLLOWERS -> attachToFront(FriendsTabsFragment.newInstance(args))
            Place.EXTERNAL_LINK -> attachToFront(BrowserFragment.newInstance(args))
            Place.DOC_PREVIEW -> {
                val document: Document? = args.getParcelable(Extra.DOC)
                if (document != null && document.hasValidGifVideoLink()) {
                    val aid = args.getInt(Extra.ACCOUNT_ID)
                    val documents = ArrayList(listOf(document))
                    val ph = Intent(this, PhotoFullScreenActivity::class.java)
                    ph.action = PhotoFullScreenActivity.ACTION_OPEN_PLACE
                    ph.putExtra(Extra.PLACE, PlaceFactory.getGifPagerPlace(aid, documents, 0))
                    place.launchActivityForResult(this, ph)
                } else {
                    attachToFront(DocPreviewFragment.newInstance(args))
                }
            }
            Place.WALL_POST -> attachToFront(WallPostFragment.newInstance(args))
            Place.COMMENTS -> attachToFront(CommentsFragment.newInstance(place))
            Place.WALL -> attachToFront(AbsWallFragment.newInstance(args))
            Place.CONVERSATION_ATTACHMENTS -> attachToFront(
                ConversationFragmentFactory.newInstance(
                    args
                )
            )
            Place.PLAYER -> {
                val player = supportFragmentManager.findFragmentByTag("audio_player")
                if (player is AudioPlayerFragment) player.dismiss()
                AudioPlayerFragment.newInstance(args).show(supportFragmentManager, "audio_player")
            }
            Place.CHAT -> {
                val peer: Peer = args.getParcelable(Extra.PEER) ?: return
                openChat(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.OWNER_ID),
                    peer,
                    place.isNeedFinishMain
                )
            }
            Place.SEARCH -> attachToFront(SearchTabsFragment.newInstance(args))
            Place.AUDIOS_SEARCH_TABS -> attachToFront(AudioSearchTabsFragment.newInstance(args))
            Place.GROUP_CHATS -> attachToFront(GroupChatsFragment.newInstance(args))
            Place.BUILD_NEW_POST -> {
                val postCreateFragment = PostCreateFragment.newInstance(args)
                attachToFront(postCreateFragment)
            }
            Place.EDIT_COMMENT -> {
                val comment: Comment? = args.getParcelable(Extra.COMMENT)
                val accountId = args.getInt(Extra.ACCOUNT_ID)
                val commemtId = args.getInt(Extra.COMMENT_ID)
                val commentEditFragment =
                    CommentEditFragment.newInstance(accountId, comment, commemtId)
                place.applyFragmentListener(commentEditFragment, supportFragmentManager)
                attachToFront(commentEditFragment)
            }
            Place.EDIT_POST -> {
                val postEditFragment = PostEditFragment.newInstance(args)
                attachToFront(postEditFragment)
            }
            Place.REPOST -> {
                val repostFragment = RepostFragment.newInstance(args)
                attachToFront(repostFragment)
            }
            Place.DIALOGS -> attachToFront(
                DialogsFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.OWNER_ID),
                    args.getString(Extra.SUBTITLE)
                )
            )
            Place.FORWARD_MESSAGES -> attachToFront(FwdsFragment.newInstance(args))
            Place.TOPICS -> attachToFront(TopicsFragment.newInstance(args))
            Place.CHAT_MEMBERS -> attachToFront(ChatUsersFragment.newInstance(args))
            Place.COMMUNITIES -> {
                val communitiesFragment = CommunitiesFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.USER_ID)
                )
                attachToFront(communitiesFragment)
            }
            Place.AUDIOS -> attachToFront(
                AudiosTabsFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID), args.getInt(
                        Extra.OWNER_ID
                    )
                )
            )
            Place.MENTIONS -> attachToFront(
                NewsfeedMentionsFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID), args.getInt(
                        Extra.OWNER_ID
                    )
                )
            )
            Place.AUDIOS_IN_ALBUM -> attachToFront(AudiosFragment.newInstance(args))
            Place.SEARCH_BY_AUDIO -> attachToFront(
                AudiosRecommendationFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    ), args.getInt(Extra.OWNER_ID), false, args.getInt(Extra.ID)
                )
            )
            Place.LOCAL_SERVER_PHOTO -> attachToFront(
                PhotosLocalServerFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    )
                )
            )
            Place.VIDEO_ALBUM -> attachToFront(VideosFragment.newInstance(args))
            Place.VIDEOS -> attachToFront(VideosTabsFragment.newInstance(args))
            Place.VK_PHOTO_ALBUMS -> attachToFront(
                VKPhotoAlbumsFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.OWNER_ID),
                    args.getString(Extra.ACTION),
                    args.getParcelable(Extra.OWNER), false
                )
            )
            Place.VK_PHOTO_ALBUM -> attachToFront(VKPhotosFragment.newInstance(args))
            Place.VK_PHOTO_ALBUM_GALLERY, Place.FAVE_PHOTOS_GALLERY, Place.SIMPLE_PHOTO_GALLERY, Place.VK_PHOTO_TMP_SOURCE, Place.VK_PHOTO_ALBUM_GALLERY_SAVED, Place.VK_PHOTO_ALBUM_GALLERY_NATIVE -> newInstance(
                this,
                place.type,
                args
            )?.let {
                place.launchActivityForResult(
                    this,
                    it
                )
            }
            Place.SINGLE_PHOTO, Place.GIF_PAGER -> {
                val ph = Intent(this, PhotoFullScreenActivity::class.java)
                ph.action = PhotoFullScreenActivity.ACTION_OPEN_PLACE
                ph.putExtra(Extra.PLACE, place)
                place.launchActivityForResult(this, ph)
            }
            Place.POLL -> attachToFront(PollFragment.newInstance(args))
            Place.BOOKMARKS -> attachToFront(FaveTabsFragment.newInstance(args))
            Place.DOCS -> attachToFront(DocsFragment.newInstance(args))
            Place.FEED -> attachToFront(FeedFragment.newInstance(args))
            Place.NOTIFICATIONS -> {
                if (Settings.get().accounts()
                        .getType(mAccountId) == AccountType.VK_ANDROID || Settings.get().accounts()
                        .getType(mAccountId) == AccountType.VK_ANDROID_HIDDEN
                ) {
                    attachToFront(
                        AnswerVKOfficialFragment.newInstance(
                            Settings.get().accounts().current
                        )
                    )
                } else {
                    attachToFront(FeedbackFragment.newInstance(args))
                }
            }
            Place.PREFERENCES -> attachToFront(PreferencesFragment.newInstance(args))
            Place.RESOLVE_DOMAIN -> {
                val domainDialog = ResolveDomainDialog.newInstance(args)
                domainDialog.show(supportFragmentManager, "resolve-domain")
            }
            Place.VK_INTERNAL_PLAYER -> {
                val intent = Intent(this, VideoPlayerActivity::class.java)
                intent.putExtras(args)
                startActivity(intent)
            }
            Place.NOTIFICATION_SETTINGS -> attachToFront(NotificationPreferencesFragment())
            Place.LIKES_AND_COPIES -> attachToFront(LikesFragment.newInstance(args))
            Place.CREATE_PHOTO_ALBUM, Place.EDIT_PHOTO_ALBUM -> {
                val createPhotoAlbumFragment = CreatePhotoAlbumFragment.newInstance(args)
                attachToFront(createPhotoAlbumFragment)
            }
            Place.MESSAGE_LOOKUP -> attachToFront(MessagesLookFragment.newInstance(args))
            Place.SEARCH_COMMENTS -> attachToFront(
                WallSearchCommentsAttachmentsFragment.newInstance(
                    args
                )
            )
            Place.UNREAD_MESSAGES -> attachToFront(NotReadMessagesFragment.newInstance(args))
            Place.SECURITY -> attachToFront(SecurityPreferencesFragment())
            Place.CREATE_POLL -> {
                val createPollFragment = CreatePollFragment.newInstance(args)
                place.applyFragmentListener(createPollFragment, supportFragmentManager)
                attachToFront(createPollFragment)
            }
            Place.COMMENT_CREATE -> openCommentCreatePlace(place)
            Place.LOGS -> attachToFront(LogsFragment.newInstance())
            Place.SINGLE_SEARCH -> {
                val singleTabSearchFragment = SingleTabSearchFragment.newInstance(args)
                attachToFront(singleTabSearchFragment)
            }
            Place.NEWSFEED_COMMENTS -> {
                val newsfeedCommentsFragment = NewsfeedCommentsFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    )
                )
                attachToFront(newsfeedCommentsFragment)
            }
            Place.COMMUNITY_CONTROL -> {
                val communityControlFragment = CommunityControlFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getParcelable(Extra.OWNER),
                    args.getParcelable(Extra.SETTINGS)
                )
                attachToFront(communityControlFragment)
            }
            Place.COMMUNITY_INFO -> {
                val communityInfoFragment = CommunityInfoContactsFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getParcelable(Extra.OWNER)
                )
                attachToFront(communityInfoFragment)
            }
            Place.COMMUNITY_INFO_LINKS -> {
                val communityLinksFragment = CommunityInfoLinksFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getParcelable(Extra.OWNER)
                )
                attachToFront(communityLinksFragment)
            }
            Place.SETTINGS_THEME -> {
                val themes = ThemeFragment()
                attachToFront(themes)
                if (navigationFragment?.isSheetOpen == true) {
                    navigationFragment?.closeSheet()
                    return
                }
            }
            Place.COMMUNITY_BAN_EDIT -> {
                val communityBanEditFragment = CommunityBanEditFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.GROUP_ID),
                    args.getParcelable<Parcelable>(Extra.BANNED) as Banned?
                )
                attachToFront(communityBanEditFragment)
            }
            Place.COMMUNITY_ADD_BAN -> attachToFront(
                CommunityBanEditFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.GROUP_ID),
                    args.getParcelableArrayList(Extra.USERS)
                )
            )
            Place.COMMUNITY_MANAGER_ADD -> attachToFront(
                CommunityManagerEditFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.GROUP_ID),
                    args.getParcelableArrayList(Extra.USERS)
                )
            )
            Place.COMMUNITY_MANAGER_EDIT -> attachToFront(
                CommunityManagerEditFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.GROUP_ID),
                    args.getParcelable<Parcelable>(Extra.MANAGER) as Manager?
                )
            )
            Place.REQUEST_EXECUTOR -> attachToFront(
                RequestExecuteFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    )
                )
            )
            Place.USER_BLACKLIST -> attachToFront(UserBannedFragment.newInstance(args.getInt(Extra.ACCOUNT_ID)))
            Place.DRAWER_EDIT -> attachToFront(DrawerEditFragment.newInstance())
            Place.SIDE_DRAWER_EDIT -> attachToFront(SideDrawerEditFragment.newInstance())
            Place.ARTIST -> {
                if (Settings.get().accounts()
                        .getType(mAccountId) == AccountType.VK_ANDROID || Settings.get().accounts()
                        .getType(mAccountId) == AccountType.VK_ANDROID_HIDDEN
                ) {
                    attachToFront(AudioCatalogFragment.newInstance(args))
                } else {
                    attachToFront(AudiosByArtistFragment.newInstance(args))
                }
            }
            Place.CATALOG_BLOCK_AUDIOS -> attachToFront(
                AudiosInCatalogFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    ), args.getString(Extra.ID), args.getString(Extra.TITLE)
                )
            )
            Place.CATALOG_BLOCK_PLAYLISTS -> attachToFront(
                PlaylistsInCatalogFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    ), args.getString(Extra.ID), args.getString(Extra.TITLE)
                )
            )
            Place.CATALOG_BLOCK_VIDEOS -> attachToFront(
                VideosInCatalogFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    ), args.getString(Extra.ID), args.getString(Extra.TITLE)
                )
            )
            Place.CATALOG_BLOCK_LINKS -> attachToFront(
                LinksInCatalogFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    ), args.getString(Extra.ID), args.getString(Extra.TITLE)
                )
            )
            Place.SHORT_LINKS -> attachToFront(ShortedLinksFragment.newInstance(args.getInt(Extra.ACCOUNT_ID)))

            Place.SHORTCUTS -> attachToFront(ShortcutsViewFragment())
            Place.IMPORTANT_MESSAGES -> attachToFront(
                ImportantMessagesFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    )
                )
            )
            Place.OWNER_ARTICLES -> attachToFront(
                OwnerArticlesFragment.newInstance(
                    args.getInt(
                        Extra.ACCOUNT_ID
                    ), args.getInt(Extra.OWNER_ID)
                )
            )
            Place.USER_DETAILS -> {
                val accountId = args.getInt(Extra.ACCOUNT_ID)
                val user: User = args.getParcelable(Extra.USER) ?: return
                val details: UserDetails = args.getParcelable("details") ?: return
                attachToFront(newInstance(accountId, user, details))
            }
            Place.WALL_ATTACHMENTS -> {
                val wall_attachments = WallAttachmentsFragmentFactory.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.OWNER_ID),
                    args.getString(Extra.TYPE)
                )
                    ?: throw IllegalArgumentException("wall_attachments cant bee null")
                attachToFront(wall_attachments)
            }
            Place.MARKET_ALBUMS -> attachToFront(
                ProductAlbumsFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.OWNER_ID)
                )
            )
            Place.NARRATIVES -> attachToFront(
                NarrativesFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.OWNER_ID)
                )
            )
            Place.MARKETS -> attachToFront(
                ProductsFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.OWNER_ID),
                    args.getInt(Extra.ALBUM_ID),
                    args.getBoolean(Extra.SERVICE)
                )
            )
            Place.PHOTO_ALL_COMMENT -> attachToFront(
                PhotoAllCommentFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.OWNER_ID)
                )
            )
            Place.GIFTS -> attachToFront(
                GiftsFragment.newInstance(
                    args.getInt(Extra.ACCOUNT_ID),
                    args.getInt(Extra.OWNER_ID)
                )
            )
            Place.MARKET_VIEW -> attachToFront(MarketViewFragment.newInstance(args))
            Place.ALBUMS_BY_VIDEO -> attachToFront(VideoAlbumsByVideoFragment.newInstance(args))
            Place.FRIENDS_BY_PHONES -> attachToFront(FriendsByPhonesFragment.newInstance(args))
            else -> throw IllegalArgumentException("Main activity can't open this place, type: " + place.type)
        }
    }

    private fun openCommentCreatePlace(place: Place) {
        val args = place.safeArguments()
        val fragment = CommentCreateFragment.newInstance(
            args.getInt(Extra.ACCOUNT_ID),
            args.getInt(Extra.COMMENT_ID),
            args.getInt(Extra.OWNER_ID),
            args.getString(Extra.BODY)
        )
        place.applyFragmentListener(fragment, supportFragmentManager)
        attachToFront(fragment)
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        if (name.className == MusicPlaybackService::class.java.name) {
            Logger.d(TAG, "Connected to MusicPlaybackService")
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        if (isActivityDestroyed) return
        if (name.className == MusicPlaybackService::class.java.name) {
            Logger.d(TAG, "Disconnected from MusicPlaybackService")
            mAudioPlayServiceToken = null
            bindToAudioPlayService()
        }
    }

    private fun openPageAndCloseSheet(item: AbsMenuItem) {
        if (navigationFragment?.isSheetOpen == true) {
            navigationFragment?.closeSheet()
            onSheetItemSelected(item, false)
        } else {
            openNavigationPage(item, true)
        }
    }

    private fun updateMessagesBagde(count: Int) {
        navigationFragment?.onUnreadDialogsCountChange(count)
        if (mBottomNavigation != null) {
            if (count > 0) {
                val badgeDrawable = mBottomNavigation?.getOrCreateBadge(R.id.menu_messages)
                badgeDrawable?.isBadgeNotSaveColor = true
                badgeDrawable?.number = count
            } else {
                mBottomNavigation?.removeBadge(R.id.menu_messages)
            }
        }
    }

    private fun updateNotificationsBadge(counters: SectionCounters) {
        navigationFragment?.onUnreadDialogsCountChange(counters.messages)
        navigationFragment?.onUnreadNotificationsCountChange(counters.notifications)
        if (mBottomNavigation != null) {
            if (counters.notifications > 0) {
                val badgeDrawable = mBottomNavigation?.getOrCreateBadge(R.id.menu_feedback)
                badgeDrawable?.isBadgeNotSaveColor = true
                badgeDrawable?.number = counters.notifications
            } else {
                mBottomNavigation?.removeBadge(R.id.menu_feedback)
            }
            if (counters.messages > 0) {
                val badgeDrawable = mBottomNavigation?.getOrCreateBadge(R.id.menu_messages)
                badgeDrawable?.isBadgeNotSaveColor = true
                badgeDrawable?.number = counters.messages
            } else {
                mBottomNavigation?.removeBadge(R.id.menu_messages)
            }
        }
    }

    private fun removeNotificationsBadge() {
        navigationFragment?.onUnreadDialogsCountChange(0)
        navigationFragment?.onUnreadNotificationsCountChange(0)
        mBottomNavigation?.removeBadge(R.id.menu_feedback)
        mBottomNavigation?.removeBadge(R.id.menu_messages)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_feed -> {
                openPageAndCloseSheet(AbsNavigationFragment.SECTION_ITEM_FEED)
                return true
            }
            R.id.menu_search -> {
                openPageAndCloseSheet(AbsNavigationFragment.SECTION_ITEM_SEARCH)
                return true
            }
            R.id.menu_messages -> {
                openPageAndCloseSheet(AbsNavigationFragment.SECTION_ITEM_DIALOGS)
                return true
            }
            R.id.menu_feedback -> {
                openPageAndCloseSheet(AbsNavigationFragment.SECTION_ITEM_FEEDBACK)
                return true
            }
            R.id.menu_other -> {
                if (navigationFragment?.isSheetOpen == true) {
                    navigationFragment?.closeSheet()
                } else {
                    navigationFragment?.openSheet()
                }
                return true
            }
            else -> return false
        }
    }

    override fun onUpdateNavigation() {
        resolveToolbarNavigationIcon()
    }

    protected val DOUBLE_BACK_PRESSED_TIMEOUT = 2000

    companion object {
        const val ACTION_MAIN = "android.intent.action.MAIN"
        const val ACTION_CHAT_FROM_SHORTCUT = "dev.ragnarok.fenrir.ACTION_CHAT_FROM_SHORTCUT"
        const val ACTION_OPEN_PLACE = "dev.ragnarok.fenrir.activity.MainActivity.openPlace"
        const val ACTION_OPEN_AUDIO_PLAYER =
            "dev.ragnarok.fenrir.activity.MainActivity.openAudioPlayer"
        const val ACTION_SEND_ATTACHMENTS = "dev.ragnarok.fenrir.ACTION_SEND_ATTACHMENTS"
        const val ACTION_SWITH_ACCOUNT = "dev.ragnarok.fenrir.ACTION_SWITH_ACCOUNT"
        const val ACTION_SHORTCUT_WALL = "dev.ragnarok.fenrir.ACTION_SHORTCUT_WALL"
        const val ACTION_OPEN_WALL = "dev.ragnarok.fenrir.ACTION_OPEN_WALL"
        const val EXTRA_NO_REQUIRE_PIN = "no_require_pin"

        /**
         * Extra with type [dev.ragnarok.fenrir.model.ModelsBundle] only
         */
        const val EXTRA_INPUT_ATTACHMENTS = "input_attachments"
        private const val TAG = "MainActivity_LOG"

        /**
         * Check the device to make sure it has the Google Play Services APK. If
         * it doesn't, display a dialog that allows users to download the APK from
         * the Google Play Store or enable it in the device's system settings.
         */
        fun checkPlayServices(context: Context): Boolean {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
            return resultCode == ConnectionResult.SUCCESS
        }
    }
}