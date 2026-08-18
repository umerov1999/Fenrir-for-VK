package dev.ragnarok.fenrir.fragment.navigationedit

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.model.DrawerCategory
import dev.ragnarok.fenrir.model.DrawerType

class DrawerEditFragment : BaseMvpFragment<DrawerEditPresenter, IDrawerEditView>(),
    IDrawerEditView, MenuProvider {
    private var mAdapter: DrawerCategoriesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dialog_drawers_categories, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val callback: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                h1: RecyclerView.ViewHolder,
                h2: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = h1.bindingAdapterPosition
                val toPosition = h2.bindingAdapterPosition
                presenter?.fireItemMoved(
                    fromPosition,
                    toPosition
                )
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {}
            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

            override fun isItemViewSwipeEnabled(): Boolean {
                return false
            }
        }
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)
        mAdapter = DrawerCategoriesAdapter(emptyList())
        recyclerView.adapter = mAdapter
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.drawer_edit, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_save -> {
                presenter?.fireSaveClick()
                true
            }

            R.id.action_reset -> {
                presenter?.fireResetClick()
                true
            }

            else -> false
        }
    }

    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(
                when (requireArguments().getInt(Extra.TYPE)) {
                    DrawerType.BOTTOM -> R.string.bottom_drawer_edit_title
                    DrawerType.SIDE -> R.string.side_drawer_edit_title
                    else -> R.string.drawer_edit_title
                }
            )
            actionBar.subtitle = null
        }
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        DrawerEditPresenter(requireArguments().getInt(Extra.TYPE))

    @SuppressLint("NotifyDataSetChanged")
    override fun notifyDataSetChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun displayData(data: List<DrawerCategory>) {
        mAdapter?.setData(data)
    }

    override fun goBackAndApplyChanges() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    companion object {
        fun newInstance(@DrawerType type: Int): DrawerEditFragment {
            val args = Bundle()
            val fragment = DrawerEditFragment()
            args.putInt(Extra.TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }
}