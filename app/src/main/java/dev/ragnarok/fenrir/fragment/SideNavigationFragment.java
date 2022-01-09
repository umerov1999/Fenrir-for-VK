package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.model.SideSwitchableCategory.BOOKMARKS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.DIALOGS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.DOCS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.FEED;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.FEEDBACK;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.FRIENDS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.GROUPS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.MUSIC;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.NEWSFEED_COMMENTS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.PHOTOS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.SEARCH;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.VIDEOS;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.RxUtils.ignore;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.MenuListAdapter;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.SideSwitchableCategory;
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem;
import dev.ragnarok.fenrir.model.drawer.DividerMenuItem;
import dev.ragnarok.fenrir.model.drawer.RecentChat;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.BlurTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.NightMode;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SideNavigationFragment extends AbsNavigationFragment implements MenuListAdapter.ActionListener {

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private NavigationDrawerCallbacks mCallbacks;
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private ImageView ivHeaderAvatar;
    private ImageView ivVerified;
    private RLottieImageView bDonate;
    private TextView tvUserName;
    private TextView tvDomain;
    private List<RecentChat> mRecentChats;
    private MenuListAdapter mAdapter;
    private List<AbsMenuItem> mDrawerItems;
    private ImageView backgroundImage;
    private int mAccountId;

    private IOwnersRepository ownersRepository;

    private static AbsMenuItem getItemBySideSwitchableCategory(@SideSwitchableCategory int type) {
        switch (type) {
            case FRIENDS:
                return SECTION_ITEM_FRIENDS;
            case DIALOGS:
                return SECTION_ITEM_DIALOGS;
            case FEED:
                return SECTION_ITEM_FEED;
            case FEEDBACK:
                return SECTION_ITEM_FEEDBACK;
            case NEWSFEED_COMMENTS:
                return SECTION_ITEM_NEWSFEED_COMMENTS;
            case GROUPS:
                return SECTION_ITEM_GROUPS;
            case PHOTOS:
                return SECTION_ITEM_PHOTOS;
            case VIDEOS:
                return SECTION_ITEM_VIDEOS;
            case MUSIC:
                return SECTION_ITEM_AUDIOS;
            case DOCS:
                return SECTION_ITEM_DOCS;
            case BOOKMARKS:
                return SECTION_ITEM_BOOKMARKS;
            case SEARCH:
                return SECTION_ITEM_SEARCH;
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ownersRepository = Repository.INSTANCE.getOwners();

        mAccountId = Settings.get()
                .accounts()
                .getCurrent();

        mCompositeDisposable.add(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAccountChange));

        mRecentChats = Settings.get()
                .recentChats()
                .get(mAccountId);

        mDrawerItems = new ArrayList<>();
        mDrawerItems.addAll(generateNavDrawerItems());

        mCompositeDisposable.add(Settings.get().sideDrawerSettings()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(o -> refreshNavigationItems()));
    }

    @Override
    public void onUnreadDialogsCountChange(int count) {
        if (SECTION_ITEM_DIALOGS.getCount() != count) {
            SECTION_ITEM_DIALOGS.setCount(count);
            safellyNotifyDataSetChanged();
        }
    }

    @Override
    public void onUnreadNotificationsCountChange(int count) {
        if (SECTION_ITEM_FEEDBACK.getCount() != count) {
            SECTION_ITEM_FEEDBACK.setCount(count);
            safellyNotifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_side_navigation_drawer, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        View vHeader = inflater.inflate(R.layout.side_header_navi_menu, recyclerView, false);
        if (!Settings.get().ui().isShow_profile_in_additional_page())
            vHeader.setVisibility(View.GONE);
        else
            vHeader.setVisibility(View.VISIBLE);
        backgroundImage = vHeader.findViewById(R.id.header_navi_menu_background);

        ivHeaderAvatar = vHeader.findViewById(R.id.header_navi_menu_avatar);
        tvUserName = vHeader.findViewById(R.id.header_navi_menu_username);
        tvDomain = vHeader.findViewById(R.id.header_navi_menu_usernick);
        ivVerified = vHeader.findViewById(R.id.item_verified);
        bDonate = vHeader.findViewById(R.id.donated_anim);

        ImageView ivHeaderDayNight = vHeader.findViewById(R.id.header_navi_menu_day_night);
        ImageView ivHeaderNotifications = vHeader.findViewById(R.id.header_navi_menu_notifications);
        ImageView ivHeaderThemes = vHeader.findViewById(R.id.header_navi_menu_themes);

        ivHeaderDayNight.setOnClickListener(v -> {
            if (Settings.get().ui().getNightMode() == NightMode.ENABLE || Settings.get().ui().getNightMode() == NightMode.AUTO ||
                    Settings.get().ui().getNightMode() == NightMode.FOLLOW_SYSTEM) {
                Settings.get().ui().switchNightMode(NightMode.DISABLE);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                Settings.get().ui().switchNightMode(NightMode.ENABLE);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        ivHeaderThemes.setOnClickListener(v -> PlaceFactory.getSettingsThemePlace().tryOpenWith(requireActivity()));

        ivHeaderNotifications.setOnClickListener(v -> {
            boolean rs = !Settings.get().other().isDisable_notifications();
            Settings.get().other().setDisable_notifications(rs);
            ivHeaderNotifications.setImageResource(rs ? R.drawable.notification_disable : R.drawable.feed);
        });
        ivHeaderNotifications.setImageResource(Settings.get().other().isDisable_notifications() ? R.drawable.notification_disable : R.drawable.feed);

        ivHeaderDayNight.setOnLongClickListener(v -> {
            PlaceFactory.getSettingsThemePlace().tryOpenWith(requireActivity());
            return true;
        });

        ivHeaderDayNight.setImageResource((Settings.get().ui().getNightMode() == NightMode.ENABLE || Settings.get().ui().getNightMode() == NightMode.AUTO ||
                Settings.get().ui().getNightMode() == NightMode.FOLLOW_SYSTEM) ? R.drawable.ic_outline_nights_stay : R.drawable.ic_outline_wb_sunny);

        mAdapter = new MenuListAdapter(requireActivity(), mDrawerItems, this, false);
        mAdapter.addHeader(vHeader);

        recyclerView.setAdapter(mAdapter);

        refreshUserInfo();

        ivHeaderAvatar.setOnClickListener(v -> {
            closeSheet();
            openMyWall();
        });

        return root;
    }

    private void refreshUserInfo() {
        if (mAccountId != ISettings.IAccountsSettings.INVALID_ID) {
            mCompositeDisposable.add(ownersRepository.getBaseOwnerInfo(mAccountId, mAccountId, IOwnersRepository.MODE_ANY)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(owner -> refreshHeader(owner), ignore()));
        }
    }

    private void openMyWall() {
        if (mAccountId == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        PlaceFactory.getOwnerWallPlace(mAccountId, mAccountId, null).tryOpenWith(requireActivity());
    }

    @Override
    public void refreshNavigationItems() {
        mDrawerItems.clear();
        mDrawerItems.addAll(generateNavDrawerItems());

        safellyNotifyDataSetChanged();
        backupRecentChats();
    }

    private ArrayList<AbsMenuItem> generateNavDrawerItems() {
        ISettings.ISideDrawerSettings settings = Settings.get().sideDrawerSettings();

        @SideSwitchableCategory
        int[] categories = settings.getCategoriesOrder();

        ArrayList<AbsMenuItem> items = new ArrayList<>();

        for (int category : categories) {
            if (settings.isCategoryEnabled(category)) {
                try {
                    items.add(getItemBySideSwitchableCategory(category));
                } catch (Exception ignored) {
                }
            }
        }
        items.add(new DividerMenuItem());

        if (nonEmpty(mRecentChats) && Settings.get().other().isEnable_show_recent_dialogs()) {
            items.addAll(mRecentChats);
            items.add(new DividerMenuItem());
        }

        items.add(SECTION_ITEM_SETTINGS);
        items.add(SECTION_ITEM_ACCOUNTS);
        return items;
    }

    /**
     * Добавить новый "недавний чат" в боковую панель
     * Если там уже есть более 4-х елементов, то удаляем последний
     *
     * @param recentChat новый чат
     */
    @Override
    public void appendRecentChat(@NonNull RecentChat recentChat) {
        if (mRecentChats == null) {
            mRecentChats = new ArrayList<>(1);
        }

        int index = mRecentChats.indexOf(recentChat);
        if (index != -1) {
            RecentChat old = mRecentChats.get(index);

            // если вдруг мы дабавляем чат без иконки или названия, то сохраним эти
            // значения из пердыдущего (c тем же peer_id) елемента
            recentChat.setIconUrl(firstNonEmptyString(recentChat.getIconUrl(), old.getIconUrl()));
            recentChat.setTitle(firstNonEmptyString(recentChat.getTitle(), old.getTitle()));

            mRecentChats.set(index, recentChat);
        } else {
            while (mRecentChats.size() >= Constants.MAX_RECENT_CHAT_COUNT) {
                mRecentChats.remove(mRecentChats.size() - 1);
            }

            mRecentChats.add(0, recentChat);
        }

        refreshNavigationItems();
    }

    private void refreshHeader(Owner user) {
        if (!isAdded()) return;

        String avaUrl = user.getMaxSquareAvatar();

        Transformation transformation = CurrentTheme.createTransformationForAvatar();
        if (nonNull(avaUrl)) {
            PicassoInstance.with()
                    .load(avaUrl)
                    .transform(transformation)
                    .into(ivHeaderAvatar);
            PicassoInstance.with()
                    .load(avaUrl)
                    .transform(new BlurTransformation(6f, requireActivity()))
                    .into(backgroundImage);
        } else {
            PicassoInstance.with().cancelRequest(ivHeaderAvatar);
            PicassoInstance.with().cancelRequest(backgroundImage);
            ivHeaderAvatar.setImageResource(R.drawable.ic_avatar_unknown);
        }

        String domailText = "@" + user.getDomain();
        tvDomain.setText(domailText);
        tvUserName.setText(user.getFullName());

        tvUserName.setTextColor(Utils.getVerifiedColor(requireActivity(), user.isVerified()));
        tvDomain.setTextColor(Utils.getVerifiedColor(requireActivity(), user.isVerified()));

        int donate_anim = Settings.get().other().getDonate_anim_set();
        if (donate_anim > 0 && user.isDonated()) {
            bDonate.setVisibility(View.VISIBLE);
            bDonate.setAutoRepeat(true);
            if (donate_anim == 2) {
                String cur = Settings.get().ui().getMainThemeKey();
                if ("fire".equals(cur) || "orange".equals(cur) || "orange_gray".equals(cur) || "yellow_violet".equals(cur)) {
                    tvUserName.setTextColor(Color.parseColor("#df9d00"));
                    tvDomain.setTextColor(Color.parseColor("#df9d00"));
                    Utils.setBackgroundTint(ivVerified, Color.parseColor("#df9d00"));
                    bDonate.fromRes(R.raw.donater_fire, Utils.dp(100), Utils.dp(100), null);
                } else {
                    tvUserName.setTextColor(CurrentTheme.getColorPrimary(requireActivity()));
                    tvDomain.setTextColor(CurrentTheme.getColorPrimary(requireActivity()));
                    Utils.setBackgroundTint(ivVerified, CurrentTheme.getColorPrimary(requireActivity()));
                    bDonate.fromRes(R.raw.donater_fire, Utils.dp(100), Utils.dp(100), new int[]{0xFF812E, CurrentTheme.getColorPrimary(requireActivity())}, true);
                }
            } else {
                bDonate.fromRes(R.raw.donater, Utils.dp(100), Utils.dp(100), new int[]{0xffffff, CurrentTheme.getColorPrimary(requireActivity()), 0x777777, CurrentTheme.getColorSecondary(requireActivity())});
            }
            bDonate.playAnimation();
        } else {
            bDonate.setImageDrawable(null);
            bDonate.setVisibility(View.GONE);
        }
        ivVerified.setVisibility(user.isVerified() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isSheetOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    @Override
    public void openSheet() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }
    }

    @Override
    public void closeSheet() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    @Override
    public void unblockSheet() {
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public void blockSheet() {
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    @Override
    public void setUp(@IdRes int fragmentId, @NonNull DrawerLayout drawerLayout) {
        mFragmentContainerView = requireActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (mCallbacks != null) {
                    mCallbacks.onSheetClosed();
                }
            }
        });
    }

    private void selectItem(AbsMenuItem item, boolean longClick) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }

        if (mCallbacks != null) {
            mCallbacks.onSheetItemSelected(item, longClick);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mCallbacks = (NavigationDrawerCallbacks) context;
        } catch (ClassCastException ignored) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void selectPage(AbsMenuItem item) {
        for (AbsMenuItem i : mDrawerItems) {
            i.setSelected(i == item);
        }
        safellyNotifyDataSetChanged();
    }

    private void backupRecentChats() {
        List<RecentChat> chats = new ArrayList<>(5);
        for (AbsMenuItem item : mDrawerItems) {
            if (item instanceof RecentChat) {
                chats.add((RecentChat) item);
            }
        }

        Settings.get()
                .recentChats()
                .store(mAccountId, chats);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    private void onAccountChange(int newAccountId) {
        backupRecentChats();

        mAccountId = newAccountId;
//        SECTION_ITEM_DIALOGS.setCount(Stores.getInstance()
//                .dialogs()
//                .getUnreadDialogsCount(mAccountId));

        mRecentChats = Settings.get()
                .recentChats()
                .get(mAccountId);

        refreshNavigationItems();

        if (mAccountId != ISettings.IAccountsSettings.INVALID_ID) {
            refreshUserInfo();
        }
    }

    private void safellyNotifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            try {
                mAdapter.notifyDataSetChanged();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onDrawerItemClick(AbsMenuItem item) {
        selectItem(item, false);
    }

    @Override
    public void onDrawerItemLongClick(AbsMenuItem item) {
        selectItem(item, true);
    }
}
