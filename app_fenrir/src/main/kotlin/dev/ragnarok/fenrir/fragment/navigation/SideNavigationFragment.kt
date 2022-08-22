package dev.ragnarok.fenrir.fragment.navigation

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes.provideMainThreadScheduler
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.IOwnersRepository
import dev.ragnarok.fenrir.domain.Repository.owners
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.SideSwitchableCategory
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem
import dev.ragnarok.fenrir.model.drawer.DividerMenuItem
import dev.ragnarok.fenrir.model.drawer.RecentChat
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.picasso.transforms.BlurTransformation
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace
import dev.ragnarok.fenrir.place.PlaceFactory.settingsThemePlace
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.ISettings
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils.dp
import dev.ragnarok.fenrir.util.Utils.firstNonEmptyString
import dev.ragnarok.fenrir.util.Utils.getVerifiedColor
import dev.ragnarok.fenrir.util.Utils.setBackgroundTint
import dev.ragnarok.fenrir.util.rxutils.RxUtils.ignore
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView
import io.reactivex.rxjava3.disposables.CompositeDisposable

class SideNavigationFragment : AbsNavigationFragment(), MenuListAdapter.ActionListener {
    private val mCompositeDisposable = CompositeDisposable()
    private var mCallbacks: NavigationDrawerCallbacks? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var mFragmentContainerView: View? = null
    private var ivHeaderAvatar: ImageView? = null
    private var ivVerified: ImageView? = null
    private var bDonate: RLottieImageView? = null
    private var tvUserName: TextView? = null
    private var tvDomain: TextView? = null
    private var mRecentChats: MutableList<RecentChat>? = null
    private var mAdapter: MenuListAdapter? = null
    private var mDrawerItems: MutableList<AbsMenuItem>? = null
    private var backgroundImage: ImageView? = null
    private var mAccountId = 0
    private var ownersRepository: IOwnersRepository = owners
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAccountId = Settings.get()
            .accounts()
            .current
        mCompositeDisposable.add(
            Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(provideMainThreadScheduler())
                .subscribe { onAccountChange(it) })
        mRecentChats = Settings.get()
            .recentChats()[mAccountId]
        mDrawerItems = ArrayList()
        mDrawerItems?.addAll(generateNavDrawerItems())
        mCompositeDisposable.add(
            Settings.get().sideDrawerSettings()
                .observeChanges()
                .observeOn(provideMainThreadScheduler())
                .subscribe { refreshNavigationItems() })
    }

    override fun onUnreadDialogsCountChange(count: Int) {
        if (SECTION_ITEM_DIALOGS.count != count) {
            SECTION_ITEM_DIALOGS.count = count
            safellyNotifyDataSetChanged()
        }
    }

    override fun onUnreadNotificationsCountChange(count: Int) {
        if (SECTION_ITEM_FEEDBACK.count != count) {
            SECTION_ITEM_FEEDBACK.count = count
            safellyNotifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_side_navigation_drawer, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val vHeader = inflater.inflate(R.layout.side_header_navi_menu, recyclerView, false)
        if (!Settings.get().ui().isShow_profile_in_additional_page) vHeader.visibility =
            View.GONE else vHeader.visibility = View.VISIBLE
        backgroundImage = vHeader.findViewById(R.id.header_navi_menu_background)
        ivHeaderAvatar = vHeader.findViewById(R.id.header_navi_menu_avatar)
        tvUserName = vHeader.findViewById(R.id.header_navi_menu_username)
        tvDomain = vHeader.findViewById(R.id.header_navi_menu_usernick)
        ivVerified = vHeader.findViewById(R.id.item_verified)
        bDonate = vHeader.findViewById(R.id.donated_anim)
        val ivHeaderDayNight = vHeader.findViewById<ImageView>(R.id.header_navi_menu_day_night)
        val ivHeaderNotifications =
            vHeader.findViewById<ImageView>(R.id.header_navi_menu_notifications)
        val ivHeaderThemes = vHeader.findViewById<ImageView>(R.id.header_navi_menu_themes)
        ivHeaderDayNight.setOnClickListener {
            if (Settings.get().ui().nightMode == AppCompatDelegate.MODE_NIGHT_YES || Settings.get()
                    .ui().nightMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY || Settings.get()
                    .ui().nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ) {
                Settings.get().ui().switchNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                Settings.get().ui().switchNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        ivHeaderThemes.setOnClickListener {
            settingsThemePlace.tryOpenWith(
                requireActivity()
            )
        }
        ivHeaderNotifications.setOnClickListener {
            val rs = !Settings.get().other().isDisable_notifications
            Settings.get().other().isDisable_notifications = rs
            ivHeaderNotifications.setImageResource(if (rs) R.drawable.notification_disable else R.drawable.feed)
        }
        ivHeaderNotifications.setImageResource(
            if (Settings.get()
                    .other().isDisable_notifications
            ) R.drawable.notification_disable else R.drawable.feed
        )
        ivHeaderDayNight.setOnLongClickListener {
            settingsThemePlace.tryOpenWith(requireActivity())
            true
        }
        ivHeaderDayNight.setImageResource(
            if (Settings.get().ui().nightMode == AppCompatDelegate.MODE_NIGHT_YES || Settings.get()
                    .ui().nightMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY || Settings.get()
                    .ui().nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ) R.drawable.ic_outline_nights_stay else R.drawable.ic_outline_wb_sunny
        )
        mAdapter = MenuListAdapter(requireActivity(), mDrawerItems ?: mutableListOf(), this, false)
        mAdapter?.addHeader(vHeader)
        recyclerView.adapter = mAdapter
        refreshUserInfo()
        ivHeaderAvatar?.setOnClickListener {
            closeSheet()
            openMyWall()
        }
        return root
    }

    private fun refreshUserInfo() {
        if (mAccountId != ISettings.IAccountsSettings.INVALID_ID) {
            mCompositeDisposable.add(
                ownersRepository.getBaseOwnerInfo(
                    mAccountId,
                    mAccountId,
                    IOwnersRepository.MODE_ANY
                )
                    .fromIOToMain()
                    .subscribe({ user -> refreshHeader(user) }, ignore())
            )
        }
    }

    private fun openMyWall() {
        if (mAccountId == ISettings.IAccountsSettings.INVALID_ID) {
            return
        }
        getOwnerWallPlace(mAccountId, mAccountId, null).tryOpenWith(requireActivity())
    }

    override fun refreshNavigationItems() {
        mDrawerItems?.clear()
        mDrawerItems?.addAll(generateNavDrawerItems())
        safellyNotifyDataSetChanged()
        backupRecentChats()
    }

    private fun generateNavDrawerItems(): ArrayList<AbsMenuItem> {
        val settings = Settings.get().sideDrawerSettings()
        @SideSwitchableCategory val categories = settings.categoriesOrder
        val items = ArrayList<AbsMenuItem>()
        for (category in categories) {
            if (settings.isCategoryEnabled(category)) {
                try {
                    items.add(getItemBySideSwitchableCategory(category))
                } catch (ignored: Exception) {
                }
            }
        }
        items.add(DividerMenuItem())
        if (mRecentChats.nonNullNoEmpty() && Settings.get().other().isEnable_show_recent_dialogs) {
            mRecentChats?.let { items.addAll(it) }
            items.add(DividerMenuItem())
        }
        items.add(SECTION_ITEM_SETTINGS)
        items.add(SECTION_ITEM_ACCOUNTS)
        return items
    }

    /**
     * Добавить новый "недавний чат" в боковую панель
     * Если там уже есть более 4-х елементов, то удаляем последний
     *
     * @param recentChat новый чат
     */
    override fun appendRecentChat(recentChat: RecentChat) {
        if (mRecentChats == null) {
            mRecentChats = ArrayList(1)
        }
        mRecentChats?.let {
            val index = it.indexOf(recentChat)
            if (index != -1) {
                val old = it[index]

                // если вдруг мы дабавляем чат без иконки или названия, то сохраним эти
                // значения из пердыдущего (c тем же peer_id) елемента
                recentChat.iconUrl = firstNonEmptyString(recentChat.iconUrl, old.iconUrl)
                recentChat.title = firstNonEmptyString(recentChat.title, old.title)
                it[index] = recentChat
            } else {
                while (it.size >= Constants.MAX_RECENT_CHAT_COUNT) {
                    it.removeAt(it.size - 1)
                }
                it.add(0, recentChat)
            }
            refreshNavigationItems()
        }
    }

    private fun refreshHeader(user: Owner) {
        if (!isAdded) return
        val avaUrl = user.maxSquareAvatar
        val transformation = CurrentTheme.createTransformationForAvatar()
        if (avaUrl != null) {
            ivHeaderAvatar?.let {
                with()
                    .load(avaUrl)
                    .transform(transformation)
                    .into(it)
            }
            backgroundImage?.let {
                with()
                    .load(avaUrl)
                    .transform(BlurTransformation(6f, requireActivity()))
                    .into(it)
            }
        } else {
            ivHeaderAvatar?.let { with().cancelRequest(it) }
            backgroundImage?.let { with().cancelRequest(it) }
            ivHeaderAvatar?.setImageResource(R.drawable.ic_avatar_unknown)
        }
        val domailText = "@" + user.domain
        tvDomain?.text = domailText
        tvUserName?.text = user.fullName
        tvUserName?.setTextColor(getVerifiedColor(requireActivity(), user.isVerified))
        tvDomain?.setTextColor(getVerifiedColor(requireActivity(), user.isVerified))
        val donate_anim = Settings.get().other().donate_anim_set
        if (donate_anim > 0 && user.isDonated) {
            bDonate?.visibility = View.VISIBLE
            bDonate?.setAutoRepeat(true)
            if (donate_anim == 2) {
                val cur = Settings.get().ui().mainThemeKey
                if ("fire" == cur || "orange" == cur || "orange_gray" == cur || "yellow_violet" == cur) {
                    tvUserName?.setTextColor(Color.parseColor("#df9d00"))
                    tvDomain?.setTextColor(Color.parseColor("#df9d00"))
                    setBackgroundTint(ivVerified, Color.parseColor("#df9d00"))
                    bDonate?.fromRes(
                        dev.ragnarok.fenrir_common.R.raw.donater_fire,
                        dp(100f),
                        dp(100f),
                        null
                    )
                } else {
                    tvUserName?.setTextColor(CurrentTheme.getColorPrimary(requireActivity()))
                    tvDomain?.setTextColor(CurrentTheme.getColorPrimary(requireActivity()))
                    setBackgroundTint(ivVerified, CurrentTheme.getColorPrimary(requireActivity()))
                    bDonate?.fromRes(
                        dev.ragnarok.fenrir_common.R.raw.donater_fire,
                        dp(100f),
                        dp(100f),
                        intArrayOf(0xFF812E, CurrentTheme.getColorPrimary(requireActivity())),
                        true
                    )
                }
            } else {
                bDonate?.fromRes(
                    dev.ragnarok.fenrir_common.R.raw.donater,
                    dp(100f),
                    dp(100f),
                    intArrayOf(
                        0xffffff,
                        CurrentTheme.getColorPrimary(requireActivity()),
                        0x777777,
                        CurrentTheme.getColorSecondary(requireActivity())
                    )
                )
            }
            bDonate?.playAnimation()
        } else {
            bDonate?.setImageDrawable(null)
            bDonate?.visibility = View.GONE
        }
        ivVerified?.visibility = if (user.isVerified) View.VISIBLE else View.GONE
    }

    override val isSheetOpen: Boolean
        get() = mFragmentContainerView?.let { mDrawerLayout?.isDrawerOpen(it) } == true

    override fun openSheet() {
        mFragmentContainerView?.let { mDrawerLayout?.openDrawer(it) }
    }

    override fun closeSheet() {
        mFragmentContainerView?.let { mDrawerLayout?.closeDrawer(it) }
    }

    override fun unblockSheet() {
        mDrawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun blockSheet() {
        mDrawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    override fun setUp(@IdRes fragmentId: Int, drawerLayout: DrawerLayout) {
        mFragmentContainerView = requireActivity().findViewById(fragmentId)
        mDrawerLayout = drawerLayout
        mDrawerLayout?.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                mCallbacks?.onSheetClosed()
            }
        })
    }

    private fun selectItem(item: AbsMenuItem, longClick: Boolean) {
        mFragmentContainerView?.let { mDrawerLayout?.closeDrawer(it) }
        mCallbacks?.onSheetItemSelected(item, longClick)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallbacks = try {
            context as NavigationDrawerCallbacks
        } catch (ignored: ClassCastException) {
            throw ClassCastException("Activity must implement NavigationDrawerCallbacks.")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }

    override fun selectPage(item: AbsMenuItem?) {
        mDrawerItems?.let {
            for (i in it) {
                i.isSelected = (i == item)
            }
        }
        safellyNotifyDataSetChanged()
    }

    private fun backupRecentChats() {
        val chats: MutableList<RecentChat> = ArrayList(5)
        mDrawerItems?.let {
            for (item in it) {
                if (item is RecentChat) {
                    chats.add(item)
                }
            }
        }
        Settings.get()
            .recentChats()
            .store(mAccountId, chats)
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        super.onDestroy()
    }

    private fun onAccountChange(newAccountId: Int) {
        backupRecentChats()
        mAccountId = newAccountId
        //        SECTION_ITEM_DIALOGS.setCount(Stores.getInstance()
//                .dialogs()
//                .getUnreadDialogsCount(mAccountId));
        mRecentChats = Settings.get()
            .recentChats()[mAccountId]
        refreshNavigationItems()
        if (mAccountId != ISettings.IAccountsSettings.INVALID_ID) {
            refreshUserInfo()
        }
    }

    private fun safellyNotifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun onDrawerItemClick(item: AbsMenuItem) {
        selectItem(item, false)
    }

    override fun onDrawerItemLongClick(item: AbsMenuItem) {
        selectItem(item, true)
    }

    companion object {
        internal fun getItemBySideSwitchableCategory(@SideSwitchableCategory type: Int): AbsMenuItem {
            when (type) {
                SideSwitchableCategory.FRIENDS -> return SECTION_ITEM_FRIENDS
                SideSwitchableCategory.DIALOGS -> return SECTION_ITEM_DIALOGS
                SideSwitchableCategory.FEED -> return SECTION_ITEM_FEED
                SideSwitchableCategory.FEEDBACK -> return SECTION_ITEM_FEEDBACK
                SideSwitchableCategory.NEWSFEED_COMMENTS -> return SECTION_ITEM_NEWSFEED_COMMENTS
                SideSwitchableCategory.GROUPS -> return SECTION_ITEM_GROUPS
                SideSwitchableCategory.PHOTOS -> return SECTION_ITEM_PHOTOS
                SideSwitchableCategory.VIDEOS -> return SECTION_ITEM_VIDEOS
                SideSwitchableCategory.MUSIC -> return SECTION_ITEM_AUDIOS
                SideSwitchableCategory.DOCS -> return SECTION_ITEM_DOCS
                SideSwitchableCategory.BOOKMARKS -> return SECTION_ITEM_BOOKMARKS
                SideSwitchableCategory.SEARCH -> return SECTION_ITEM_SEARCH
            }
            throw UnsupportedOperationException()
        }
    }
}