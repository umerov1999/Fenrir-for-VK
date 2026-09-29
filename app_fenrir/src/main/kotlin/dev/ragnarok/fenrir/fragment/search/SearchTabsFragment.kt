package dev.ragnarok.fenrir.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.setToolbarSubtitle
import dev.ragnarok.fenrir.activity.ActivityUtils.setToolbarTitle
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Accounts.fromArgs
import dev.ragnarok.fenrir.util.Utils.createPageTransform
import dev.ragnarok.fenrir.view.navigation.AbsNavigationView

class SearchTabsFragment : Fragment() {
    private var mCurrentTab = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mCurrentTab = savedInstanceState.getInt(SAVE_CURRENT_TAB)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search_tabs, container, false)
        val mViewPager: ViewPager2 = root.findViewById(R.id.viewpager)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        val mAdapter = Adapter(this)
        mViewPager.adapter = mAdapter
        mViewPager.setPageTransformer(
            createPageTransform(
                Settings.get().main().viewpager_page_transform
            )
        )
        TabLayoutMediator(
            root.findViewById(R.id.tablayout),
            mViewPager
        ) { tab, position ->
            when (position) {
                TAB_PEOPLE -> tab.setText(R.string.people)
                TAB_COMMUNITIES -> tab.setText(R.string.communities)
                TAB_MUSIC -> tab.setText(R.string.music)
                TAB_VIDEOS -> tab.setText(R.string.videos)
                TAB_DOCUMENTS -> tab.setText(R.string.documents)
                TAB_PHOTOS -> tab.setText(R.string.photos)
                TAB_NEWS -> tab.setText(R.string.feed)
                TAB_MESSAGES -> tab.setText(R.string.messages)
                TAB_AUDIO_PLAYLISTS -> tab.setText(R.string.playlists)
                TAB_ARTISTS -> tab.setText(R.string.artists)
            }
        }.attach()
        if (requireArguments().containsKey(Extra.TAB)) {
            mCurrentTab = requireArguments().getInt(Extra.TAB)
            requireArguments().remove(Extra.TAB)
            mViewPager.currentItem = mCurrentTab
        }
        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_CURRENT_TAB, mCurrentTab)
    }

    override fun onResume() {
        super.onResume()
        Settings.get().ui().notifyPlaceResumed(Place.SEARCH)
        setToolbarTitle(this, R.string.search)
        setToolbarSubtitle(this, null) //
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .build()
            .apply(requireActivity())
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(AbsNavigationView.SECTION_ITEM_SEARCH)
        }
    }

    private inner class Adapter(fm: Fragment) : FragmentStateAdapter(fm) {
        override fun createFragment(position: Int): Fragment {
            val accountId = fromArgs(arguments)
            val fragment: Fragment = when (position) {
                TAB_PEOPLE -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.PEOPLE
                )

                TAB_COMMUNITIES -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.COMMUNITIES
                )

                TAB_MUSIC -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.AUDIOS
                )

                TAB_AUDIO_PLAYLISTS -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.AUDIO_PLAYLISTS
                )

                TAB_VIDEOS -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.VIDEOS
                )

                TAB_DOCUMENTS -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.DOCUMENTS
                )

                TAB_PHOTOS -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.PHOTOS
                )

                TAB_NEWS -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.NEWS
                )

                TAB_MESSAGES -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.MESSAGES
                )

                TAB_ARTISTS -> SearchFragmentFactory.create(
                    accountId,
                    SearchContentType.ARTISTS
                )

                else -> throw IllegalArgumentException()
            }
            return fragment
        }

        override fun getItemCount(): Int {
            return 10
        }
    }

    companion object {
        const val TAB_PEOPLE = 0
        const val TAB_COMMUNITIES = 1
        const val TAB_NEWS = 2
        const val TAB_MUSIC = 3
        const val TAB_AUDIO_PLAYLISTS = 4
        const val TAB_VIDEOS = 5
        const val TAB_MESSAGES = 6
        const val TAB_DOCUMENTS = 7
        const val TAB_PHOTOS = 8
        const val TAB_ARTISTS = 9
        private const val SAVE_CURRENT_TAB = "save_current_tab"
        fun buildArgs(accountId: Long, tab: Int): Bundle {
            val args = Bundle()
            args.putInt(Extra.TAB, tab)
            args.putLong(Extra.ACCOUNT_ID, accountId)
            return args
        }

        fun newInstance(args: Bundle?): SearchTabsFragment {
            val fragment = SearchTabsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}