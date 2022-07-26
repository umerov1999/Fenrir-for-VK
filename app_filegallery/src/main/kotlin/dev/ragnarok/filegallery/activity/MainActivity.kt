package dev.ragnarok.filegallery.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import dev.ragnarok.filegallery.R
import dev.ragnarok.filegallery.activity.EnterPinActivity.Companion.getClass
import dev.ragnarok.filegallery.fragment.*
import dev.ragnarok.filegallery.fromIOToMain
import dev.ragnarok.filegallery.listener.*
import dev.ragnarok.filegallery.media.music.MusicPlaybackController
import dev.ragnarok.filegallery.media.music.MusicPlaybackController.ServiceToken
import dev.ragnarok.filegallery.media.music.MusicPlaybackService
import dev.ragnarok.filegallery.model.SectionItem
import dev.ragnarok.filegallery.place.Place
import dev.ragnarok.filegallery.place.PlaceFactory.getFileManagerPlace
import dev.ragnarok.filegallery.place.PlaceFactory.getLocalMediaServerPlace
import dev.ragnarok.filegallery.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.filegallery.place.PlaceFactory.getPreferencesPlace
import dev.ragnarok.filegallery.place.PlaceFactory.getTagsPlace
import dev.ragnarok.filegallery.place.PlaceProvider
import dev.ragnarok.filegallery.settings.CurrentTheme
import dev.ragnarok.filegallery.settings.NightMode
import dev.ragnarok.filegallery.settings.Settings
import dev.ragnarok.filegallery.settings.theme.ThemesController.currentStyle
import dev.ragnarok.filegallery.settings.theme.ThemesController.nextRandom
import dev.ragnarok.filegallery.util.AppPerms
import dev.ragnarok.filegallery.util.AppPerms.requestPermissionsResultAbs
import dev.ragnarok.filegallery.util.Logger
import dev.ragnarok.filegallery.util.Utils
import dev.ragnarok.filegallery.util.ViewUtils.keyboardHide
import dev.ragnarok.filegallery.util.rxutils.RxUtils
import dev.ragnarok.filegallery.util.toast.CustomToast
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.io.File


class MainActivity : AppCompatActivity(), OnSectionResumeCallback, AppStyleable, PlaceProvider,
    NavigationBarView.OnItemSelectedListener, UpdatableNavigation, ServiceConnection {
    private var mBottomNavigation: BottomNavigationView? = null
    private var isSelected = false

    @SectionItem
    private var mCurrentFrontSection: Int = SectionItem.NULL
    private var mToolbar: Toolbar? = null
    private var mViewFragment: FragmentContainerView? = null
    private var mLastBackPressedTime: Long = 0
    private val DOUBLE_BACK_PRESSED_TIMEOUT = 2000
    private var mDestroyed = false
    private var mAudioPlayServiceToken: ServiceToken? = null
    private val TAG = "MainActivity_LOG"
    private val mCompositeDisposable = CompositeDisposable()
    private val requestReadWritePermission = requestPermissionsResultAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        ), {
            handleIntent(null, true)
        }, {
            finish()
        })

    private val mOnBackStackChangedListener =
        FragmentManager.OnBackStackChangedListener {
            resolveToolbarNavigationIcon()
            keyboardHide(this)
        }

    private val requestEnterPin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode != RESULT_OK) {
            finish()
        } else {
            handleIntent(intent?.action, true)
        }
    }

    private fun startEnterPinActivity() {
        val intent = Intent(this, getClass(this))
        requestEnterPin.launch(intent)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        delegate.applyDayNight()
        savedInstanceState ?: nextRandom()
        setTheme(currentStyle())
        Utils.prepareDensity(this)
        super.onCreate(savedInstanceState)
        mDestroyed = false

        savedInstanceState ?: run {
            if (Settings.get().security().isUsePinForEntrance && Settings.get().security()
                    .hasPinHash()
            ) {
                startEnterPinActivity()
            } else {
                handleIntent(intent?.action, true)
            }
        }
        bindToAudioPlayService()
        setContentView(noMainContentView)
        mBottomNavigation = findViewById(R.id.bottom_navigation_menu)
        mBottomNavigation?.setOnItemSelectedListener(this)
        mViewFragment = findViewById(R.id.fragment)

        supportFragmentManager.addOnBackStackChangedListener(mOnBackStackChangedListener)
        resolveToolbarNavigationIcon()
        savedInstanceState ?: run {
            if (Settings.get().main().getLocalServer().enabled_audio_local_sync) {
                mCompositeDisposable.add(MusicPlaybackController.tracksExist.findAllAudios(
                    this
                )
                    .fromIOToMain()
                    .subscribe(
                        RxUtils.dummy()
                    ) { t: Throwable? ->
                        if (Settings.get().main().isDeveloper_mode()) {
                            CustomToast.createCustomToast(
                                this,
                                mViewFragment,
                                mBottomNavigation
                            )?.showToastThrowable(t)
                        }
                    })
            }
            mCompositeDisposable.add(MusicPlaybackController.tracksExist.findAllTags()
                .fromIOToMain()
                .subscribe(
                    RxUtils.dummy()
                ) { t: Throwable? ->
                    if (Settings.get().main().isDeveloper_mode()) {
                        CustomToast.createCustomToast(this, mViewFragment, mBottomNavigation)
                            ?.showToastThrowable(t)
                    }
                })
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Utils.updateActivityContext(newBase))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent?.action, false)
    }

    private fun resolveToolbarNavigationIcon() {
        mToolbar ?: return
        val manager: FragmentManager = supportFragmentManager
        if (manager.backStackEntryCount > 1 || frontFragment is CanBackPressedCallback && (frontFragment as CanBackPressedCallback).canBackPressed()) {
            mToolbar?.setNavigationIcon(R.drawable.arrow_left)
            mToolbar?.setNavigationOnClickListener { onBackPressed() }
        } else {
            mToolbar?.setNavigationIcon(R.drawable.client_round)
            mToolbar?.setNavigationOnClickListener {
                if (Settings.get().main().getNightMode() == NightMode.ENABLE || Settings.get()
                        .main().getNightMode() == NightMode.AUTO || Settings.get()
                        .main().getNightMode() == NightMode.FOLLOW_SYSTEM
                ) {
                    Settings.get().main().switchNightMode(NightMode.DISABLE)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    Settings.get().main().switchNightMode(NightMode.ENABLE)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
    }

    @get:LayoutRes
    private val noMainContentView: Int
        get() = R.layout.activity_main
    private val frontFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragment)

    @SuppressLint("ShowToast")
    override fun onBackPressed() {
        val front: Fragment? = frontFragment
        if (front is BackPressCallback) {
            if (!(front as BackPressCallback).onBackPressed()) {
                return
            }
        }
        if (supportFragmentManager.backStackEntryCount == 1) {
            if (mLastBackPressedTime < 0
                || mLastBackPressedTime + DOUBLE_BACK_PRESSED_TIMEOUT > System.currentTimeMillis()
            ) {
                supportFinishAfterTransition()
                return
            }
            mLastBackPressedTime = System.currentTimeMillis()
            mViewFragment?.let {
                CustomToast.createCustomToast(this, mViewFragment, mBottomNavigation)
                    ?.setDuration(Toast.LENGTH_SHORT)?.showToast(R.string.click_back_to_exit)
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onSectionResume(@SectionItem section: Int) {
        mCurrentFrontSection = section
        mBottomNavigation?.menu ?: return
        for (i in (mBottomNavigation?.menu ?: return).iterator()) {
            i.isChecked = false
        }

        when (section) {
            SectionItem.FILE_MANAGER -> {
                mBottomNavigation?.menu?.findItem(R.id.menu_files)?.isChecked = true
            }
            SectionItem.LOCAL_SERVER -> {
                mBottomNavigation?.menu?.findItem(R.id.menu_local_server)?.isChecked = true
            }
            SectionItem.NULL -> {

            }
            SectionItem.SETTINGS -> {
                mBottomNavigation?.menu?.findItem(R.id.menu_settings)?.isChecked = true
            }
            SectionItem.TAGS -> {
                mBottomNavigation?.menu?.findItem(R.id.menu_tags)?.isChecked = true
            }
        }
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

    @get:IdRes
    private val mainContainerViewId: Int
        get() = R.id.fragment

    private fun handleIntent(action: String?, main: Boolean) {
        if (main) {
            if (!AppPerms.hasReadWriteStoragePermission(this)) {
                requestReadWritePermission.launch()
                return
            }
            if (Intent.ACTION_GET_CONTENT.contentEquals(action, true)) {
                isSelected = true
                openNavigationPage(
                    SectionItem.FILE_MANAGER,
                    clearBackStack = false,
                    isSelect = true
                )
            } else if (Intent.ACTION_PICK.contentEquals(action, true)) {
                isSelected = true
                openNavigationPage(
                    SectionItem.FILE_MANAGER,
                    clearBackStack = false,
                    isSelect = true
                )
            } else {
                isSelected = false
                openNavigationPage(
                    SectionItem.FILE_MANAGER,
                    clearBackStack = false,
                    isSelect = false
                )
            }
        }
        if (ACTION_OPEN_AUDIO_PLAYER == action) {
            openPlace(getPlayerPlace())
        }
    }

    private fun attachToFront(fragment: Fragment, animate: Boolean = true) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (animate) fragmentTransaction.setCustomAnimations(
            R.anim.fragment_enter,
            R.anim.fragment_exit
        )
        fragmentTransaction
            .replace(mainContainerViewId, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    override fun openPlace(place: Place) {
        val args: Bundle = place.prepareArguments()
        when (place.type) {
            Place.FILE_MANAGER -> {
                attachToFront(FileManagerFragment.newInstance(args))
            }
            Place.PREFERENCES -> {
                attachToFront(PreferencesFragment())
            }
            Place.LOCAL_MEDIA_SERVER -> {
                attachToFront(LocalServerTabsFragment())
            }
            Place.SETTINGS_THEME -> {
                attachToFront(ThemeFragment())
            }
            Place.AUDIO_PLAYER -> {
                val player = supportFragmentManager.findFragmentByTag("audio_player")
                if (player is AudioPlayerFragment) {
                    player.dismiss()
                }
                AudioPlayerFragment().show(supportFragmentManager, "audio_player")
            }
            Place.PHOTO_LOCAL, Place.PHOTO_LOCAL_SERVER -> {
                PhotoPagerActivity.newInstance(this, place.type, args)?.let {
                    place.launchActivityForResult(
                        this,
                        it
                    )
                }
            }
            Place.VIDEO_PLAYER -> {
                val intent = Intent(this, VideoPlayerActivity::class.java)
                intent.putExtras(args)
                startActivity(intent)
            }
            Place.TAGS -> {
                attachToFront(TagOwnerFragment.newInstance(args))
            }
            Place.TAG_DIRS -> {
                attachToFront(TagDirFragment.newInstance(args))
            }
            Place.SECURITY -> attachToFront(SecurityPreferencesFragment())
        }
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        mToolbar?.setNavigationOnClickListener(null)
        mToolbar?.setOnMenuItemClickListener(null)
        super.setSupportActionBar(toolbar)
        mToolbar = toolbar
        resolveToolbarNavigationIcon()
    }

    override fun onUpdateNavigation() {
        resolveToolbarNavigationIcon()
    }

    private fun bindToAudioPlayService() {
        if (!isActivityDestroyed() && mAudioPlayServiceToken == null) {
            mAudioPlayServiceToken = MusicPlaybackController.bindToServiceWithoutStart(this, this)
        }
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        mDestroyed = true
        supportFragmentManager.removeOnBackStackChangedListener(mOnBackStackChangedListener)

        //if(!bNoDestroyServiceAudio)
        unbindFromAudioPlayService()
        super.onDestroy()
    }

    private fun unbindFromAudioPlayService() {
        if (mAudioPlayServiceToken != null) {
            MusicPlaybackController.unbindFromService(mAudioPlayServiceToken)
            mAudioPlayServiceToken = null
        }
    }

    private fun isActivityDestroyed(): Boolean {
        return mDestroyed
    }

    private fun clearBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun openNavigationPage(
        @SectionItem item: Int,
        clearBackStack: Boolean,
        isSelect: Boolean
    ) {
        if (item == mCurrentFrontSection) {
            return
        }
        if (clearBackStack) {
            clearBackStack()
        }
        mCurrentFrontSection = item
        when (item) {
            SectionItem.FILE_MANAGER -> {
                @Suppress("DEPRECATION")
                val path: File =
                    if (Environment.getExternalStorageDirectory().isDirectory && Environment.getExternalStorageDirectory()
                            .canRead()
                    ) {
                        Environment.getExternalStorageDirectory()
                    } else {
                        File("/")
                    }
                openPlace(getFileManagerPlace(path.absolutePath, false, isSelect))
            }
            SectionItem.LOCAL_SERVER -> {
                if (!Settings.get().main().getLocalServer().enabled) {
                    CustomToast.createCustomToast(this, mViewFragment, mBottomNavigation)
                        ?.setDuration(Toast.LENGTH_SHORT)
                        ?.showToastError(R.string.local_server_need_enable)
                    openPlace(getPreferencesPlace())
                } else {
                    openPlace(getLocalMediaServerPlace())
                }
            }
            SectionItem.NULL -> {
                throw UnsupportedOperationException()
            }
            SectionItem.SETTINGS -> {
                openPlace(getPreferencesPlace())
            }
            SectionItem.TAGS -> {
                openPlace(getTagsPlace(isSelect))
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_files -> {
                openNavigationPage(
                    SectionItem.FILE_MANAGER,
                    clearBackStack = true,
                    isSelect = isSelected
                )
                true
            }
            R.id.menu_settings -> {
                openNavigationPage(
                    SectionItem.SETTINGS,
                    clearBackStack = false,
                    isSelect = isSelected
                )
                true
            }
            R.id.menu_local_server -> {
                openNavigationPage(
                    SectionItem.LOCAL_SERVER,
                    clearBackStack = false,
                    isSelect = isSelected
                )
                true
            }
            R.id.menu_tags -> {
                openNavigationPage(SectionItem.TAGS, clearBackStack = false, isSelect = isSelected)
                true
            }
            else -> false
        }
    }

    companion object {
        const val ACTION_OPEN_AUDIO_PLAYER =
            "dev.ragnarok.filegallery.activity.MainActivity.openAudioPlayer"
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (name?.className.equals(MusicPlaybackService::class.java.name)) {
            Logger.d(TAG, "Connected to MusicPlaybackService")
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        if (isActivityDestroyed()) return

        if (name?.className.equals(MusicPlaybackService::class.java.name)) {
            Logger.d(TAG, "Disconnected from MusicPlaybackService")
            mAudioPlayServiceToken = null
            bindToAudioPlayService()
        }
    }
}
