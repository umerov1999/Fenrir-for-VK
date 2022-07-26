package dev.ragnarok.fenrir.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes.provideMainThreadScheduler
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.adapter.MenuListAdapter
import dev.ragnarok.fenrir.domain.IOwnersRepository
import dev.ragnarok.fenrir.domain.Repository.owners
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.model.SwitchableCategory
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem
import dev.ragnarok.fenrir.model.drawer.RecentChat
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace
import dev.ragnarok.fenrir.place.PlaceFactory.settingsThemePlace
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.ISettings
import dev.ragnarok.fenrir.settings.NightMode
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils.firstNonEmptyString
import dev.ragnarok.fenrir.util.rxutils.RxUtils.ignore
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AdditionalNavigationFragment : AbsNavigationFragment(), MenuListAdapter.ActionListener {
    private val mCompositeDisposable = CompositeDisposable()
    private var mCallbacks: NavigationDrawerCallbacks? = null
    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var ivHeaderAvatar: ImageView? = null
    private var tvUserName: TextView? = null
    private var tvDomain: TextView? = null
    private var mRecentChats: MutableList<RecentChat>? = null
    private var mAdapter: MenuListAdapter? = null
    private var mDrawerItems: ArrayList<AbsMenuItem>? = null
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
            Settings.get().drawerSettings()
                .observeChanges()
                .observeOn(provideMainThreadScheduler())
                .subscribe { refreshNavigationItems() })
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_navigation_drawer, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(requireActivity(), 2)
        val vgProfileContainer = root.findViewById<ViewGroup>(R.id.content_root)
        if (!Settings.get()
                .ui().isShow_profile_in_additional_page
        ) root.findViewById<View>(R.id.profile_view).visibility =
            View.GONE else root.findViewById<View>(R.id.profile_view).visibility = View.VISIBLE
        ivHeaderAvatar = root.findViewById(R.id.header_navi_menu_avatar)
        tvUserName = root.findViewById(R.id.header_navi_menu_username)
        tvDomain = root.findViewById(R.id.header_navi_menu_usernick)
        val ivHeaderDayNight = root.findViewById<ImageView>(R.id.header_navi_menu_day_night)
        val ivHeaderNotifications =
            root.findViewById<ImageView>(R.id.header_navi_menu_notifications)
        ivHeaderDayNight.setOnClickListener {
            if (Settings.get().ui().nightMode == NightMode.ENABLE || Settings.get()
                    .ui().nightMode == NightMode.AUTO || Settings.get()
                    .ui().nightMode == NightMode.FOLLOW_SYSTEM
            ) {
                Settings.get().ui().switchNightMode(NightMode.DISABLE)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                Settings.get().ui().switchNightMode(NightMode.ENABLE)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
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
            if (Settings.get().ui().nightMode == NightMode.ENABLE || Settings.get()
                    .ui().nightMode == NightMode.AUTO || Settings.get()
                    .ui().nightMode == NightMode.FOLLOW_SYSTEM
            ) R.drawable.ic_outline_nights_stay else R.drawable.ic_outline_wb_sunny
        )
        mAdapter = MenuListAdapter(requireActivity(), mDrawerItems ?: mutableListOf(), this, true)
        mBottomSheetBehavior = BottomSheetBehavior.from(root.findViewById(R.id.bottom_sheet))
        mBottomSheetBehavior?.skipCollapsed = true
        mBottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset == -1f) {
                    mCallbacks?.onSheetClosed()
                }
            }
        })
        closeSheet()
        recyclerView.adapter = mAdapter
        refreshUserInfo()
        vgProfileContainer.setOnClickListener {
            closeSheet()
            openMyWall()
        }
        return root
    }

    override fun refreshNavigationItems() {
        mDrawerItems?.clear()
        mDrawerItems?.addAll(generateNavDrawerItems())
        safellyNotifyDataSetChanged()
        backupRecentChats()
    }

    private fun generateNavDrawerItems(): ArrayList<AbsMenuItem> {
        val settings = Settings.get().drawerSettings()
        @SwitchableCategory val categories = settings.categoriesOrder
        val items = ArrayList<AbsMenuItem>()
        for (category in categories) {
            if (settings.isCategoryEnabled(category)) {
                try {
                    items.add(getItemBySwitchableCategory(category))
                } catch (ignored: Exception) {
                }
            }
        }

//        items.add(new DividerMenuItem());
        items.add(SECTION_ITEM_SETTINGS)
        items.add(SECTION_ITEM_ACCOUNTS)
        if (mRecentChats.nonNullNoEmpty() && Settings.get().other().isEnable_show_recent_dialogs) {
            mRecentChats?.let { items.addAll(it) }
        }
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
        } else {
            ivHeaderAvatar?.setImageResource(R.drawable.ic_avatar_unknown)
        }
        val domailText = "@" + user.domain.nonNullNoEmpty({
            it
        }, { user.ownerId.toString() })
        tvDomain?.text = domailText
        tvUserName?.text = user.fullName
    }

    override val isSheetOpen: Boolean
        get() = mBottomSheetBehavior != null && mBottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED

    override fun openSheet() {
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun closeSheet() {
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun unblockSheet() {
        view?.visibility = View.VISIBLE
    }

    override fun blockSheet() {
        view?.visibility = View.GONE
    }

    override fun setUp(@IdRes fragmentId: Int, drawerLayout: DrawerLayout) {}
    override fun onUnreadDialogsCountChange(count: Int) {}
    override fun onUnreadNotificationsCountChange(count: Int) {}
    private fun selectItem(item: AbsMenuItem, longClick: Boolean) {
        closeSheet()
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
            safellyNotifyDataSetChanged()
        }
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

    private fun getItemBySwitchableCategory(@SwitchableCategory type: Int): AbsMenuItem {
        when (type) {
            SwitchableCategory.FRIENDS -> return SECTION_ITEM_FRIENDS
            SwitchableCategory.NEWSFEED_COMMENTS -> return SECTION_ITEM_NEWSFEED_COMMENTS
            SwitchableCategory.GROUPS -> return SECTION_ITEM_GROUPS
            SwitchableCategory.PHOTOS -> return SECTION_ITEM_PHOTOS
            SwitchableCategory.VIDEOS -> return SECTION_ITEM_VIDEOS
            SwitchableCategory.MUSIC -> return SECTION_ITEM_AUDIOS
            SwitchableCategory.DOCS -> return SECTION_ITEM_DOCS
            SwitchableCategory.BOOKMARKS -> return SECTION_ITEM_BOOKMARKS
        }
        throw UnsupportedOperationException()
    }
}