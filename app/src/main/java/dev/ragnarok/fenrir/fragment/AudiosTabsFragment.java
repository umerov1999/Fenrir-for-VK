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

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class AudiosTabsFragment extends BaseFragment {

    public static final int LOCAL_SERVER = -6;
    public static final int LOCAL = -5;
    public static final int CATALOG = -4;
    public static final int PLAYLISTS = -3;
    public static final int MY_RECOMMENDATIONS = -2;
    public static final int MY_AUDIO = -1;
    private int accountId;
    private int ownerId;

    public static Bundle buildArgs(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        return args;
    }

    public static AudiosTabsFragment newInstance(int accountId, int ownerId) {
        return newInstance(buildArgs(accountId, ownerId));
    }

    public static AudiosTabsFragment newInstance(Bundle args) {
        AudiosTabsFragment fragment = new AudiosTabsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        ownerId = requireArguments().getInt(Extra.OWNER_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_audios_tabs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ViewPager2 viewPager = view.findViewById(R.id.fragment_audios_pager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageTransformer(Utils.createPageTransform(Settings.get().main().getViewpager_page_transform()));
        Adapter adapter = new Adapter(this);
        setupViewPager(viewPager, adapter);

        new TabLayoutMediator(view.findViewById(R.id.fragment_audios_tabs), viewPager, (tab, position) -> {
            Integer fid = adapter.mFragments.get(position);
            if (fid == MY_AUDIO)
                tab.setText(getString(R.string.my_saved));
            else if (fid == PLAYLISTS)
                tab.setText(getString(R.string.playlists));
            else if (fid == MY_RECOMMENDATIONS)
                tab.setText(getString(R.string.recommendation));
            else if (fid == CATALOG)
                tab.setText(getString(R.string.audio_catalog));
            else if (fid == LOCAL)
                tab.setText(getString(R.string.local_audios));
            else if (fid == LOCAL_SERVER)
                tab.setText(getString(R.string.on_server));
            else
                tab.setText(VKApiAudio.Genre.getTitleByGenre(requireActivity(), fid));
        }).attach();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Integer fid = adapter.mFragments.get(position);
                viewPager.setUserInputEnabled(fid != CATALOG);
            }
        });
    }

    public int getAccountId() {
        return accountId;
    }

    private Fragment CreateAudiosFragment(int option_menu) {
        if (option_menu == PLAYLISTS) {
            AudioPlaylistsFragment fragment = AudioPlaylistsFragment.newInstance(getAccountId(), ownerId);
            fragment.requireArguments().putBoolean(AudiosFragment.EXTRA_IN_TABS_CONTAINER, true);
            return fragment;
        } else if (option_menu == CATALOG)
            return AudioCatalogFragment.newInstance(getAccountId(), null, true);
        else if (option_menu == LOCAL) {
            AudiosLocalFragment fragment = AudiosLocalFragment.newInstance(getAccountId());
            fragment.requireArguments().putBoolean(AudiosFragment.EXTRA_IN_TABS_CONTAINER, true);
            return fragment;
        } else if (option_menu == LOCAL_SERVER) {
            return AudiosLocalServerFragment.newInstance(getAccountId());
        } else if (option_menu == MY_AUDIO) {
            Bundle args = AudiosFragment.buildArgs(getAccountId(), ownerId, null, null);
            args.putBoolean(AudiosFragment.EXTRA_IN_TABS_CONTAINER, true);
            return AudiosFragment.newInstance(args);
        } else if (option_menu == MY_RECOMMENDATIONS) {
            AudiosRecommendationFragment fragment = AudiosRecommendationFragment.newInstance(getAccountId(), ownerId, false, 0);
            fragment.requireArguments().putBoolean(AudiosFragment.EXTRA_IN_TABS_CONTAINER, true);
            return fragment;
        } else {
            AudiosRecommendationFragment fragment = AudiosRecommendationFragment.newInstance(getAccountId(), ownerId, true, option_menu);
            fragment.requireArguments().putBoolean(AudiosFragment.EXTRA_IN_TABS_CONTAINER, true);
            return fragment;
        }
    }

    private void setupViewPager(ViewPager2 viewPager, Adapter adapter) {
        adapter.addFragment(MY_AUDIO);
        adapter.addFragment(PLAYLISTS);
        if (ownerId >= 0) {
            if (getAccountId() == ownerId) {
                adapter.addFragment(LOCAL);
                if (Settings.get().other().getLocalServer().enabled) {
                    adapter.addFragment(LOCAL_SERVER);
                }
                if (Settings.get().accounts().getType(Settings.get().accounts().getCurrent()) == AccountType.VK_ANDROID || Settings.get().accounts().getType(Settings.get().accounts().getCurrent()) == AccountType.VK_ANDROID_HIDDEN) {
                    adapter.addFragment(CATALOG);
                }
            }
            adapter.addFragment(MY_RECOMMENDATIONS);
        }
        if (getAccountId() == ownerId && Settings.get().other().isEnable_show_audio_top()) {
            adapter.addFragment(VKApiAudio.Genre.TOP_ALL);
            adapter.addFragment(VKApiAudio.Genre.ETHNIC);
            adapter.addFragment(VKApiAudio.Genre.INSTRUMENTAL);
            adapter.addFragment(VKApiAudio.Genre.ACOUSTIC_AND_VOCAL);
            adapter.addFragment(VKApiAudio.Genre.ALTERNATIVE);
            adapter.addFragment(VKApiAudio.Genre.CLASSICAL);
            adapter.addFragment(VKApiAudio.Genre.DANCE_AND_HOUSE);
            adapter.addFragment(VKApiAudio.Genre.DRUM_AND_BASS);
            adapter.addFragment(VKApiAudio.Genre.EASY_LISTENING);
            adapter.addFragment(VKApiAudio.Genre.ELECTROPOP_AND_DISCO);
            adapter.addFragment(VKApiAudio.Genre.INDIE_POP);
            adapter.addFragment(VKApiAudio.Genre.METAL);
            adapter.addFragment(VKApiAudio.Genre.OTHER);
            adapter.addFragment(VKApiAudio.Genre.POP);
            adapter.addFragment(VKApiAudio.Genre.REGGAE);
            adapter.addFragment(VKApiAudio.Genre.ROCK);
            adapter.addFragment(VKApiAudio.Genre.TRANCE);
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.music);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_AUDIOS);
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
            PlaceFactory.getAudiosTabsSearchPlace(getAccountId()).tryOpenWith(requireActivity());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_audio_main, menu);
    }

    private class Adapter extends FragmentStateAdapter {
        private final List<Integer> mFragments = new ArrayList<>();

        public Adapter(@NonNull Fragment fragmentActivity) {
            super(fragmentActivity);
        }

        void addFragment(Integer fragment) {
            mFragments.add(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return CreateAudiosFragment(mFragments.get(position));
        }

        @Override
        public int getItemCount() {
            return mFragments.size();
        }
    }
}
