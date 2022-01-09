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
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Accounts;
import dev.ragnarok.fenrir.util.Utils;

public class AudioSearchTabsFragment extends Fragment {

    public static final int TAB_MUSIC = 0;
    public static final int TAB_AUDIO_PLAYLISTS = 1;
    public static final int TAB_ARTISTS = 2;

    public static Bundle buildArgs(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static AudioSearchTabsFragment newInstance(Bundle args) {
        AudioSearchTabsFragment fragment = new AudioSearchTabsFragment();
        fragment.setArguments(args);
        return fragment;
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
                case TAB_MUSIC:
                    tab.setText(R.string.music);
                    break;
                case TAB_AUDIO_PLAYLISTS:
                    tab.setText(R.string.playlists);
                    break;
                case TAB_ARTISTS:
                    tab.setText(R.string.artists);
                    break;
            }
        }).attach();
        mViewPager.setCurrentItem(TAB_MUSIC);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);

        ActivityUtils.setToolbarTitle(this, R.string.search);
        ActivityUtils.setToolbarSubtitle(this, null); //

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_AUDIOS);
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
                case TAB_MUSIC:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.AUDIOS, new AudioSearchCriteria("", false, true));
                    break;

                case TAB_AUDIO_PLAYLISTS:
                    fragment = SingleTabSearchFragment.newInstance(accountId, SearchContentType.AUDIO_PLAYLISTS);
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
            return 3;
        }
    }
}
