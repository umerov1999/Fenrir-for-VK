package dev.ragnarok.fenrir.fragment.theme

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
import dev.ragnarok.fenrir.fragment.base.compat.AbsMvpFragment
import dev.ragnarok.fenrir.settings.Settings.get
import dev.ragnarok.fenrir.settings.theme.ThemeValue
import kotlin.math.max

class ThemeFragment : AbsMvpFragment<ThemePresenter, IThemeView>(), IThemeView,
    ThemeAdapter.ClickListener {
    private var mAdapter: ThemeAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_theme, container, false)
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

        mAdapter = ThemeAdapter(emptyList(), requireActivity())
        mAdapter?.setClickListener(this)
        recyclerView.adapter = mAdapter
        return root
    }

    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        actionBar?.setTitle(R.string.theme_edit_title)
        actionBar?.subtitle = null
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .build()
            .apply(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = ThemePresenter()

    override fun displayData(data: Array<ThemeValue>) {
        mAdapter?.setData(data)
    }

    override fun onClick(index: Int, value: ThemeValue?) {
        if ((value ?: return).disabled) {
            return
        }
        get().ui().setMainTheme(value.id)
        mAdapter?.updateCurrentId(value.id)
        requireActivity().recreate()
        mAdapter?.notifyDataSetChanged()
    }
}