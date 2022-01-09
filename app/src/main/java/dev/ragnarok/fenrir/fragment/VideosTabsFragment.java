package dev.ragnarok.fenrir.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.db.OwnerHelper;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.VideoSearchCriteria;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.mvp.view.IVideosListView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class VideosTabsFragment extends BaseFragment {

    public static final int LOCAL_SERVER = -1;
    public static final int VIDEOS = 0;
    public static final int ALBUMS = 1;

    private int accountId;
    private int ownerId;
    private String action;

    public static Bundle buildArgs(int accountId, int ownerId, String action) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putString(Extra.ACTION, action);
        return args;
    }

    public static VideosTabsFragment newInstance(int accountId, int ownerId, String action) {
        return newInstance(buildArgs(accountId, ownerId, action));
    }

    public static VideosTabsFragment newInstance(Bundle args) {
        VideosTabsFragment fragment = new VideosTabsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        ownerId = requireArguments().getInt(Extra.OWNER_ID);
        action = requireArguments().getString(Extra.ACTION);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_videos_tabs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ViewPager2 viewPager = view.findViewById(R.id.fragment_videos_pager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageTransformer(Utils.createPageTransform(
                Settings.get().main().getViewpager_page_transform()
        ));
        Adapter adapter = new Adapter(this);
        viewPager.setAdapter(adapter);
        adapter.addFragment(VIDEOS);
        adapter.addFragment(ALBUMS);

        if (accountId == ownerId && Settings.get().other().getLocalServer().enabled && !IVideosListView.ACTION_SELECT.equalsIgnoreCase(action)) {
            adapter.addFragment(LOCAL_SERVER);
        }

        new TabLayoutMediator(view.findViewById(R.id.fragment_videos_tabs), viewPager, (tab, position) -> {
            Integer fid = adapter.mFragments.get(position);
            switch (fid) {
                case LOCAL_SERVER:
                    tab.setText(R.string.on_server);
                    break;
                case VIDEOS:
                    tab.setText(R.string.videos_my);
                    break;
                case ALBUMS:
                    tab.setText(R.string.videos_albums);
                    break;
            }
        }).attach();
    }

    public int getAccountId() {
        return accountId;
    }

    private boolean isMy() {
        return getAccountId() == ownerId;
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.VIDEOS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.videos);
            actionBar.setSubtitle(isMy() ? null : OwnerHelper.loadOwnerFullName(requireActivity(), getAccountId(), ownerId));
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            if (isMy()) {
                ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_VIDEOS);
            } else {
                ((OnSectionResumeCallback) requireActivity()).onClearSelection();
            }
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            VideoSearchCriteria criteria = new VideoSearchCriteria("", true);
            PlaceFactory.getSingleTabSearchPlace(getAccountId(), SearchContentType.VIDEOS, criteria).tryOpenWith(requireActivity());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Fragment CreateVideosFragment(int option_menu) {
        switch (option_menu) {
            case LOCAL_SERVER:
                return VideosLocalServerFragment.newInstance(getAccountId());
            case VIDEOS:
                VideosFragment fragment = VideosFragment.newInstance(getAccountId(), ownerId, 0, action, null);
                fragment.requireArguments().putBoolean(VideosFragment.EXTRA_IN_TABS_CONTAINER, true);
                return fragment;
            case ALBUMS:
                return VideoAlbumsFragment.newInstance(getAccountId(), ownerId, action);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_video_main, menu);
    }

    class Adapter extends FragmentStateAdapter {
        private final List<Integer> mFragments = new ArrayList<>();

        public Adapter(@NonNull Fragment fm) {
            super(fm);
        }

        void addFragment(Integer fragment) {
            mFragments.add(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return CreateVideosFragment(mFragments.get(position));
        }

        @Override
        public int getItemCount() {
            return mFragments.size();
        }
    }
}
