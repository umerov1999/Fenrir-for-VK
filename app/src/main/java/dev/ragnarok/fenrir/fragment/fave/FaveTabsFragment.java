package dev.ragnarok.fenrir.fragment.fave;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import dev.ragnarok.fenrir.fragment.AbsNavigationFragment;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.link.types.FaveLink;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class FaveTabsFragment extends BaseFragment {

    public static final int TAB_UNKNOWN = -1;
    public static final int TAB_PAGES = 0;
    public static final int TAB_GROUPS = 1;
    public static final int TAB_POSTS = 2;
    public static final int TAB_LINKS = 3;
    public static final int TAB_ARTICLES = 4;
    public static final int TAB_PRODUCTS = 5;
    public static final int TAB_PHOTOS = 6;
    public static final int TAB_VIDEOS = 7;
    private int mAccountId;

    public static Bundle buildArgs(int accountId, int tab) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.TAB, tab);
        return args;
    }

    public static FaveTabsFragment newInstance(int accountId, int tab) {
        return newInstance(buildArgs(accountId, tab));
    }

    public static FaveTabsFragment newInstance(Bundle args) {
        FaveTabsFragment faveTabsFragment = new FaveTabsFragment();
        faveTabsFragment.setArguments(args);
        return faveTabsFragment;
    }

    public static int getTabByLinkSection(String linkSection) {
        if (TextUtils.isEmpty(linkSection)) {
            return TAB_PHOTOS;
        }

        switch (linkSection) {
            case FaveLink.SECTION_PHOTOS:
                return TAB_PHOTOS;
            case FaveLink.SECTION_VIDEOS:
                return TAB_VIDEOS;
            case FaveLink.SECTION_POSTS:
                return TAB_POSTS;
            case FaveLink.SECTION_PAGES:
                return TAB_PAGES;
            case FaveLink.SECTION_LINKS:
                return TAB_LINKS;
            case FaveLink.SECTION_ARTICLES:
                return TAB_ARTICLES;
            case FaveLink.SECTION_PRODUCTS:
                return TAB_PRODUCTS;
            default:
                return TAB_UNKNOWN;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID);
    }

    public int getAccountId() {
        return mAccountId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_fave_tabs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ViewPager2 viewPager = view.findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setPageTransformer(Utils.createPageTransform(Settings.get().main().getViewpager_page_transform()));
        setupViewPager(viewPager, view);

        if (requireArguments().containsKey(Extra.TAB)) {
            int tab = requireArguments().getInt(Extra.TAB);
            requireArguments().remove(Extra.TAB);
            viewPager.setCurrentItem(tab);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.BOOKMARKS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);

        if (actionBar != null) {
            actionBar.setTitle(R.string.bookmarks);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_BOOKMARKS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    private void setupViewPager(ViewPager2 viewPager, @NonNull View view) {
        List<ITab> tabs = new ArrayList<>();
        tabs.add(new Tab(() -> FavePagesFragment.newInstance(getAccountId(), true), getString(R.string.pages)));
        tabs.add(new Tab(() -> FavePagesFragment.newInstance(getAccountId(), false), getString(R.string.groups)));
        tabs.add(new Tab(() -> FavePostsFragment.newInstance(getAccountId()), getString(R.string.posts)));
        tabs.add(new Tab(() -> FaveLinksFragment.newInstance(getAccountId()), getString(R.string.links)));
        tabs.add(new Tab(() -> FaveArticlesFragment.newInstance(getAccountId()), getString(R.string.articles)));
        tabs.add(new Tab(() -> FaveProductsFragment.newInstance(getAccountId()), getString(R.string.products)));
        tabs.add(new Tab(() -> FavePhotosFragment.newInstance(getAccountId()), getString(R.string.photos)));
        tabs.add(new Tab(() -> FaveVideosFragment.newInstance(getAccountId()), getString(R.string.videos)));
        Adapter adapter = new Adapter(tabs, this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(view.findViewById(R.id.tablayout), viewPager, (tab, position) ->
                tab.setText(tabs.get(position).getTabTitle())).attach();
    }

    private interface ITab {
        String getTabTitle();

        IFragmentCreator getFragmentCreator();
    }

    private interface IFragmentCreator {
        Fragment create();
    }

    private static class Tab implements ITab {

        final String title;
        final IFragmentCreator creator;

        private Tab(IFragmentCreator creator, String title) {
            this.title = title;
            this.creator = creator;
        }

        @Override
        public String getTabTitle() {
            return title;
        }

        @Override
        public IFragmentCreator getFragmentCreator() {
            return creator;
        }
    }

    static class Adapter extends FragmentStateAdapter {

        private final List<ITab> tabs;

        public Adapter(List<ITab> tabs, @NonNull Fragment fragmentActivity) {
            super(fragmentActivity);
            this.tabs = tabs;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return tabs.get(position).getFragmentCreator().create();
        }

        @Override
        public int getItemCount() {
            return tabs.size();
        }
    }
}
