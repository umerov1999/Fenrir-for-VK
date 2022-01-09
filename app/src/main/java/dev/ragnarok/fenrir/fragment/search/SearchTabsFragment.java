package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.fragment.AbsNavigationFragment;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Accounts;
import dev.ragnarok.fenrir.util.Utils;

public class SearchTabsFragment extends Fragment {

    public static final int TAB_PEOPLE = 0;
    public static final int TAB_COMMUNITIES = 1;
    public static final int TAB_NEWS = 2;
    public static final int TAB_MUSIC = 3;
    public static final int TAB_AUDIO_PLAYLISTS = 4;
    public static final int TAB_VIDEOS = 5;
    public static final int TAB_MESSAGES = 6;
    public static final int TAB_DOCUMENTS = 7;
    public static final int TAB_PHOTOS = 8;
    public static final int TAB_ARTISTS = 9;
    private static final String SAVE_CURRENT_TAB = "save_current_tab";
    private int mCurrentTab;

    public static Bundle buildArgs(int accountId, int tab) {
        Bundle args = new Bundle();
        args.putInt(Extra.TAB, tab);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static SearchTabsFragment newInstance(Bundle args) {
        SearchTabsFragment fragment = new SearchTabsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentTab = savedInstanceState.getInt(SAVE_CURRENT_TAB);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_tabs, container, false);
        ViewPager2 mViewPager = root.findViewById(R.id.viewpager);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        Adapter mAdapter = new Adapter(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setPageTransformer(Utils.createPageTransform(Settings.get().main().getViewpager_page_transform()));

        new TabLayoutMediator(root.findViewById(R.id.tablayout), mViewPager, (tab, position) -> {
            switch (position) {
                case TAB_PEOPLE:
                    tab.setText(R.string.people);
                    break;
                case TAB_COMMUNITIES:
                    tab.setText(R.string.communities);
                    break;
                case TAB_MUSIC:
                    tab.setText(R.string.music);
                    break;
                case TAB_VIDEOS:
                    tab.setText(R.string.videos);
                    break;
                case TAB_DOCUMENTS:
                    tab.setText(R.string.documents);
                    break;
                case TAB_PHOTOS:
                    tab.setText(R.string.photos);
                    break;
                case TAB_NEWS:
                    tab.setText(R.string.feed);
                    break;
                case TAB_MESSAGES:
                    tab.setText(R.string.messages);
                    break;
                case TAB_AUDIO_PLAYLISTS:
                    tab.setText(R.string.playlists);
                    break;
                case TAB_ARTISTS:
                    tab.setText(R.string.artists);
                    break;
            }
        }).attach();

        if (requireArguments().containsKey(Extra.TAB)) {
            mCurrentTab = requireArguments().getInt(Extra.TAB);

            requireArguments().remove(Extra.TAB);
            mViewPager.setCurrentItem(mCurrentTab);
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_CURRENT_TAB, mCurrentTab);
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.SEARCH);

        ActivityUtils.setToolbarTitle(this, R.string.search);
        ActivityUtils.setToolbarSubtitle(this, null); //

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_SEARCH);
        }
    }

    private class Adapter extends FragmentStateAdapter {

        public Adapter(@NonNull Fragment fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            int accountId = Accounts.fromArgs(getArguments());

            Fragment fragment;

            switch (position) {
                case TAB_PEOPLE:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.PEOPLE);
                    break;

                case TAB_COMMUNITIES:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.COMMUNITIES);
                    break;

                case TAB_MUSIC:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.AUDIOS);
                    break;

                case TAB_AUDIO_PLAYLISTS:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.AUDIO_PLAYLISTS);
                    break;

                case TAB_VIDEOS:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.VIDEOS);
                    break;

                case TAB_DOCUMENTS:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.DOCUMENTS);
                    break;

                case TAB_PHOTOS:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.PHOTOS);
                    break;

                case TAB_NEWS:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.NEWS);
                    break;

                case TAB_MESSAGES:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.MESSAGES);
                    break;

                case TAB_ARTISTS:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.ARTISTS);
                    break;

                default:
                    throw new IllegalArgumentException();
            }
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 10;
        }
    }
}
