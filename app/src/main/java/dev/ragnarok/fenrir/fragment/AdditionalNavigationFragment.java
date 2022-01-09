package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.model.SwitchableCategory.BOOKMARKS;
import static dev.ragnarok.fenrir.model.SwitchableCategory.DOCS;
import static dev.ragnarok.fenrir.model.SwitchableCategory.FRIENDS;
import static dev.ragnarok.fenrir.model.SwitchableCategory.GROUPS;
import static dev.ragnarok.fenrir.model.SwitchableCategory.MUSIC;
import static dev.ragnarok.fenrir.model.SwitchableCategory.NEWSFEED_COMMENTS;
import static dev.ragnarok.fenrir.model.SwitchableCategory.PHOTOS;
import static dev.ragnarok.fenrir.model.SwitchableCategory.VIDEOS;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.RxUtils.ignore;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
import dev.ragnarok.fenrir.model.SwitchableCategory;
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem;
import dev.ragnarok.fenrir.model.drawer.RecentChat;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.NightMode;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AdditionalNavigationFragment extends AbsNavigationFragment implements MenuListAdapter.ActionListener {

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private NavigationDrawerCallbacks mCallbacks;
    private BottomSheetBehavior<View> mBottomSheetBehavior;
    private ImageView ivHeaderAvatar;
    private TextView tvUserName;
    private TextView tvDomain;
    private List<RecentChat> mRecentChats;
    private MenuListAdapter mAdapter;
    private List<AbsMenuItem> mDrawerItems;
    private int mAccountId;

    private IOwnersRepository ownersRepository;

    protected static AbsMenuItem getItemBySwitchableCategory(@SwitchableCategory int type) {
        switch (type) {
            case FRIENDS:
                return SECTION_ITEM_FRIENDS;
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

        mCompositeDisposable.add(Settings.get().drawerSettings()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(o -> refreshNavigationItems()));
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2));

        ViewGroup vgProfileContainer = root.findViewById(R.id.content_root);
        if (!Settings.get().ui().isShow_profile_in_additional_page())
            root.findViewById(R.id.profile_view).setVisibility(View.GONE);
        else
            root.findViewById(R.id.profile_view).setVisibility(View.VISIBLE);
        ivHeaderAvatar = root.findViewById(R.id.header_navi_menu_avatar);
        tvUserName = root.findViewById(R.id.header_navi_menu_username);
        tvDomain = root.findViewById(R.id.header_navi_menu_usernick);
        ImageView ivHeaderDayNight = root.findViewById(R.id.header_navi_menu_day_night);
        ImageView ivHeaderNotifications = root.findViewById(R.id.header_navi_menu_notifications);

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

        mAdapter = new MenuListAdapter(requireActivity(), mDrawerItems, this, true);

        mBottomSheetBehavior = BottomSheetBehavior.from(root.findViewById(R.id.bottom_sheet));
        mBottomSheetBehavior.setSkipCollapsed(true);
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset == -1) {
                    mCallbacks.onSheetClosed();
                }
            }
        });
        closeSheet();

        recyclerView.setAdapter(mAdapter);

        refreshUserInfo();

        vgProfileContainer.setOnClickListener(v -> {
            closeSheet();
            openMyWall();
        });

        return root;
    }

    @Override
    public void refreshNavigationItems() {
        mDrawerItems.clear();
        mDrawerItems.addAll(generateNavDrawerItems());

        safellyNotifyDataSetChanged();
        backupRecentChats();
    }

    private ArrayList<AbsMenuItem> generateNavDrawerItems() {
        ISettings.IDrawerSettings settings = Settings.get().drawerSettings();

        @SwitchableCategory
        int[] categories = settings.getCategoriesOrder();

        ArrayList<AbsMenuItem> items = new ArrayList<>();

        for (int category : categories) {
            if (settings.isCategoryEnabled(category)) {
                try {
                    items.add(getItemBySwitchableCategory(category));
                } catch (Exception ignored) {
                }
            }
        }

//        items.add(new DividerMenuItem());

        items.add(SECTION_ITEM_SETTINGS);
        items.add(SECTION_ITEM_ACCOUNTS);

        if (nonEmpty(mRecentChats) && Settings.get().other().isEnable_show_recent_dialogs()) {
            items.addAll(mRecentChats);
        }
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
        } else {
            ivHeaderAvatar.setImageResource(R.drawable.ic_avatar_unknown);
        }

        String domailText = "@" + user.getDomain();
        tvDomain.setText(domailText);
        tvUserName.setText(user.getFullName());
    }

    @Override
    public boolean isSheetOpen() {
        return mBottomSheetBehavior != null && mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    @Override
    public void openSheet() {
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void closeSheet() {
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void unblockSheet() {
        if (getView() != null) {
            getView().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void blockSheet() {
        if (getView() != null) {
            getView().setVisibility(View.GONE);
        }
    }

    @Override
    public void setUp(@IdRes int fragmentId, @NonNull DrawerLayout drawerLayout) {

    }

    @Override
    public void onUnreadDialogsCountChange(int count) {

    }

    @Override
    public void onUnreadNotificationsCountChange(int count) {

    }

    private void selectItem(AbsMenuItem item, boolean longClick) {
        closeSheet();

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
