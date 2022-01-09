package dev.ragnarok.fenrir.fragment.friends;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.fragment.AbsNavigationFragment;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.model.FriendsCounters;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.FriendsTabsPresenter;
import dev.ragnarok.fenrir.mvp.view.IFriendsTabsView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class FriendsTabsFragment extends BaseMvpFragment<FriendsTabsPresenter, IFriendsTabsView> implements IFriendsTabsView {

    public static final int TAB_ALL_FRIENDS = 0;
    public static final int TAB_ONLINE = 1;
    public static final int TAB_FOLLOWERS = 2;
    public static final int TAB_REQUESTS = 3;
    public static final int TAB_MUTUAL = 4;
    public static final int TAB_RECOMMENDATIONS = 5;

    private Adapter adapter;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public static Bundle buildArgs(int accountId, int userId, int tab, FriendsCounters counters) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.USER_ID, userId);
        args.putInt(Extra.TAB, tab);
        args.putParcelable(Extra.COUNTERS, counters);
        return args;
    }

    public static FriendsTabsFragment newInstance(Bundle args) {
        FriendsTabsFragment friendsFragment = new FriendsTabsFragment();
        friendsFragment.setArguments(args);
        return friendsFragment;
    }

    public static FriendsTabsFragment newInstance(int accountId, int userId, int tab, FriendsCounters counters) {
        return newInstance(buildArgs(accountId, userId, tab, counters));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        View root = inflater.inflate(R.layout.fragment_friends_tabs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        viewPager = root.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageTransformer(Utils.createPageTransform(Settings.get().main().getViewpager_page_transform()));

        tabLayout = root.findViewById(R.id.tablayout);
        return root;
    }

    private void setupTabCounterView(int id, int count) {
        try {
            adapter.updateCount(id, count);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.FRIENDS_AND_FOLLOWERS);

        ActivityUtils.setToolbarTitle(this, R.string.friends);

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @NonNull
    @Override
    public IPresenterFactory<FriendsTabsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FriendsTabsPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.USER_ID),
                requireArguments().getParcelable(Extra.COUNTERS),
                saveInstanceState
        );
    }

    @Override
    public void displayConters(FriendsCounters counters) {
        setupTabCounterView(TAB_ALL_FRIENDS, counters.getAll());
        setupTabCounterView(TAB_ONLINE, counters.getOnline());
        setupTabCounterView(TAB_FOLLOWERS, counters.getFollowers());
        setupTabCounterView(TAB_REQUESTS, 0);
        setupTabCounterView(TAB_MUTUAL, counters.getMutual());
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setText(adapter.getPageTitle(i));
        }
    }

    @Override
    public void configTabs(int accountId, int userId, boolean isNotMyPage) {
        adapter = new Adapter(requireActivity(), this, accountId, userId, isNotMyPage);

        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(adapter.getPageTitle(position))).attach();

        if (requireArguments().containsKey(Extra.TAB)) {
            int tab = requireArguments().getInt(Extra.TAB);
            requireArguments().remove(Extra.TAB);
            int pos = 0;
            boolean succ = false;
            for (FriendSource i : adapter.mFragmentTitles) {
                if (i.Id == tab) {
                    succ = true;
                    break;
                }
                pos++;
            }
            if (succ) {
                viewPager.setCurrentItem(pos);
            }
        }
    }

    @Override
    public void displayUserNameAtToolbar(String userName) {
        ActivityUtils.setToolbarSubtitle(this, userName);
    }

    @Override
    public void setDrawerFriendsSectionSelected(boolean selected) {
        if (requireActivity() instanceof OnSectionResumeCallback) {
            if (selected) {
                ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_FRIENDS);
            } else {
                ((OnSectionResumeCallback) requireActivity()).onClearSelection();
            }
        }
    }

    private interface CreateFriendsFragment {
        Fragment create();
    }

    private static class Adapter extends FragmentStateAdapter {

        private final boolean isNotMyPage;

        private final List<FriendSource> mFragmentTitles;

        public Adapter(Context context, @NonNull Fragment fm, int accountId, int userId, boolean isNotMyPage) {
            super(fm);
            this.isNotMyPage = isNotMyPage;

            mFragmentTitles = new ArrayList<>(getItemCount());
            mFragmentTitles.add(new FriendSource(TAB_ALL_FRIENDS, context.getString(R.string.all_friends), () -> AllFriendsFragment.newInstance(accountId, userId)));
            mFragmentTitles.add(new FriendSource(TAB_ONLINE, context.getString(R.string.online), () -> OnlineFriendsFragment.newInstance(accountId, userId)));
            mFragmentTitles.add(new FriendSource(TAB_FOLLOWERS, context.getString(R.string.counter_followers), () -> FollowersFragment.newInstance(accountId, userId)));
            if (isNotMyPage) {
                mFragmentTitles.add(new FriendSource(TAB_MUTUAL, context.getString(R.string.mutual_friends), () -> MutualFriendsFragment.newInstance(accountId, userId)));
            } else {
                mFragmentTitles.add(new FriendSource(TAB_REQUESTS, context.getString(R.string.counter_requests), () -> RequestsFragment.newInstance(accountId, userId)));
                mFragmentTitles.add(new FriendSource(TAB_RECOMMENDATIONS, context.getString(R.string.recommendation), () -> RecommendationsFriendsFragment.newInstance(accountId, userId)));
            }
        }

        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position).getTitle();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return mFragmentTitles.get(position).getFragment();
        }

        @Override
        public int getItemCount() {
            return isNotMyPage ? 4 : 5;
        }

        public void updateCount(int id, Integer count) {
            for (FriendSource i : mFragmentTitles) {
                if (i.isId(id)) {
                    i.updateCount(count);
                    break;
                }
            }
        }
    }

    private static class FriendSource {
        private final String Title;
        private final CreateFriendsFragment call;
        private final int Id;
        private Integer Count;

        public FriendSource(int Id, @NonNull String Title, @NonNull CreateFriendsFragment call) {
            Count = null;
            this.Title = Title;
            this.call = call;
            this.Id = Id;
        }

        public void updateCount(@Nullable Integer Count) {
            this.Count = Count;
        }

        public String getTitle() {
            if (Count != null && Count > 0) {
                return Title + " " + Count;
            }
            return Title;
        }

        public boolean isId(int Id) {
            return this.Id == Id;
        }

        public Fragment getFragment() {
            return call.create();
        }
    }
}