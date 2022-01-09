package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LongSparseArray;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.ref.WeakReference;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.model.selection.AbsSelectableSource;
import dev.ragnarok.fenrir.model.selection.FileManagerSelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalGallerySelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalPhotosSelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalVideosSelectableSource;
import dev.ragnarok.fenrir.model.selection.Sources;
import dev.ragnarok.fenrir.model.selection.Types;
import dev.ragnarok.fenrir.model.selection.VkPhotosSelectableSource;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class DualTabPhotosFragment extends BaseFragment implements BackPressCallback {

    private Sources mSources;
    private Adapter mPagerAdapter;
    private int mCurrentTab;

    public static DualTabPhotosFragment newInstance(Sources sources) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.SOURCES, sources);

        DualTabPhotosFragment fragment = new DualTabPhotosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSources = requireArguments().getParcelable(Extra.SOURCES);

        if (nonNull(savedInstanceState)) {
            mCurrentTab = savedInstanceState.getInt("mCurrentTab");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mCurrentTab", mCurrentTab);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_dual_tab_photos, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        ViewPager2 viewPager = root.findViewById(R.id.view_pager);

        mPagerAdapter = new Adapter(this, mSources);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageTransformer(Utils.createPageTransform(Settings.get().main().getViewpager_page_transform()));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentTab = position;
            }
        });

        new TabLayoutMediator(root.findViewById(R.id.tablayout), viewPager, (tab, position) ->
                tab.setText(mPagerAdapter.getPageTitle(position))).attach();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(R.string.multiply_poll);
            actionBar.setSubtitle(null);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (nonNull(mPagerAdapter)) {
            Fragment fragment = mPagerAdapter.findFragmentByPosition(mCurrentTab);

            return !(fragment instanceof BackPressCallback) || ((BackPressCallback) fragment).onBackPressed();
        }

        return true;
    }

    private class Adapter extends FragmentStateAdapter {

        private final Sources mSources;
        private final LongSparseArray<WeakReference<Fragment>> fragments;

        public Adapter(@NonNull Fragment fm, Sources mSources) {
            super(fm);
            this.mSources = mSources;
            fragments = new LongSparseArray<>();
        }

        public CharSequence getPageTitle(int position) {
            @Types
            int tabtype = mSources.get(position).getType();

            switch (tabtype) {
                case Types.LOCAL_PHOTOS:
                    return getString(R.string.local_photos_tab_title);

                case Types.LOCAL_GALLERY:
                    return getString(R.string.local_gallery_tab_title);

                case Types.VIDEOS:
                    return getString(R.string.videos);

                case Types.VK_PHOTOS:
                    return getString(R.string.vk_photos_tab_title);

                case Types.FILES:
                    return getString(R.string.files_tab_title);
            }

            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            AbsSelectableSource source = mSources.get(position);

            if (source instanceof LocalPhotosSelectableSource) {
                Bundle args = new Bundle();
                args.putBoolean(BaseMvpFragment.EXTRA_HIDE_TOOLBAR, true);
                LocalImageAlbumsFragment fragment = new LocalImageAlbumsFragment();
                fragment.setArguments(args);
                fragments.put(position, new WeakReference<>(fragment));
                return fragment;
            }

            if (source instanceof LocalGallerySelectableSource) {
                LocalPhotosFragment fragment = LocalPhotosFragment.newInstance(10, null, true);
                fragments.put(position, new WeakReference<>(fragment));
                return fragment;
            }

            if (source instanceof LocalVideosSelectableSource) {
                Bundle args = new Bundle();
                args.putBoolean(BaseMvpFragment.EXTRA_HIDE_TOOLBAR, true);
                LocalVideosFragment fragment = LocalVideosFragment.newInstance();
                fragment.setArguments(args);
                fragments.put(position, new WeakReference<>(fragment));
                return fragment;
            }

            if (source instanceof VkPhotosSelectableSource) {
                VkPhotosSelectableSource vksource = (VkPhotosSelectableSource) source;
                VKPhotoAlbumsFragment fragment = VKPhotoAlbumsFragment.newInstance(vksource.getAccountId(), vksource.getOwnerId(), null, null, true);
                fragments.put(position, new WeakReference<>(fragment));
                return fragment;
            }

            if (source instanceof FileManagerSelectableSource) {
                Bundle args = new Bundle();
                args.putInt(Extra.ACTION, FileManagerFragment.SELECT_FILE);
                args.putBoolean(FileManagerFragment.EXTRA_SHOW_CANNOT_READ, true);

                FileManagerFragment fileManagerFragment = new FileManagerFragment();
                fileManagerFragment.setArguments(args);
                fragments.put(position, new WeakReference<>(fileManagerFragment));
                return fileManagerFragment;
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public int getItemCount() {
            return mSources.count();
        }

        public Fragment findFragmentByPosition(int position) {
            WeakReference<Fragment> weak = fragments.get(position);
            return Objects.isNull(weak) ? null : weak.get();
        }
    }
}