package dev.ragnarok.fenrir.fragment.shortcutsview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.activity.MainActivity
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.model.ShortcutStored
import kotlin.math.max

class ShortcutsViewFragment : BaseMvpFragment<ShortcutsViewPresenter, IShortcutsView>(),
    IShortcutsView, ShortcutsListAdapter.ActionListener {
    private var mAdapter: ShortcutsListAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_shortcuts, container, false) as ViewGroup
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        val columns = resources.getInteger(R.integer.photos_column_count)
        val gridLayoutManager = GridLayoutManager(requireActivity(), columns)
        recyclerView.layoutManager = gridLayoutManager
        if (requireActivity() is MainActivity) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                val insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
                WindowInsetsCompat.CONSUMED
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                val insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                val imeFixedBottom =
                    if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                        windowInsets.getInsets(
                            WindowInsetsCompat.Type.ime()
                        ).bottom, insets.bottom
                    ) else insets.bottom
                root.findViewById<View>(R.id.actionbar)
                    ?.setPadding(insets.left, 0, insets.right, 0)
                root.findViewById<View>(R.id.toolbar)
                    ?.setPadding(0, insets.top, 0, 0)
                recyclerView.setPadding(insets.left, 0, insets.right, imeFixedBottom)
                WindowInsetsCompat.CONSUMED
            }
        }

        PicassoPauseOnScrollListener.addListener(recyclerView)
        mAdapter = ShortcutsListAdapter(emptyList())
        mAdapter?.setActionListener(this)
        recyclerView.adapter = mAdapter
        return root
    }

    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.add_to_launcher_shortcuts)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .build()
            .apply(requireActivity())
    }

    override fun displayData(shortcuts: List<ShortcutStored>) {
        mAdapter?.setData(shortcuts)
    }

    override fun notifyItemRemoved(position: Int) {
        mAdapter?.notifyItemRemoved(position)
    }

    override fun notifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = ShortcutsViewPresenter(
        saveInstanceState
    )

    override fun onShortcutClick(shortcutStored: ShortcutStored) {
        presenter?.fireShortcutClick(requireActivity(), shortcutStored)
    }

    override fun onShortcutRemoved(pos: Int, shortcutStored: ShortcutStored) {
        presenter?.fireShortcutDeleted(pos, shortcutStored)
    }
}
