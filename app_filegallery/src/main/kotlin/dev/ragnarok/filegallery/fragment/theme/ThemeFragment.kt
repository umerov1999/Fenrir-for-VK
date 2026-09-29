package dev.ragnarok.filegallery.fragment.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.filegallery.R
import dev.ragnarok.filegallery.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.filegallery.fragment.base.compat.AbsMvpFragment
import dev.ragnarok.filegallery.settings.Settings.get
import dev.ragnarok.filegallery.settings.theme.ThemeValue

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

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        val columns = resources.getInteger(R.integer.photos_column_count)
        val gridLayoutManager = GridLayoutManager(requireActivity(), columns)
        recyclerView.layoutManager = gridLayoutManager
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
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = ThemePresenter()

    override fun displayData(data: Array<ThemeValue>) {
        mAdapter?.setData(data)
    }

    override fun onClick(index: Int, value: ThemeValue?) {
        if ((value ?: return).disabled) {
            return
        }
        get().main().setMainTheme(value.id)
        mAdapter?.updateCurrentId(value.id)
        requireActivity().recreate()
        mAdapter?.notifyDataSetChanged()
    }
}
