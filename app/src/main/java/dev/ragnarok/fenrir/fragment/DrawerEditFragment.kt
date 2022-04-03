package dev.ragnarok.fenrir.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.adapter.DrawerCategotiesAdapter
import dev.ragnarok.fenrir.model.DrawerCategory
import dev.ragnarok.fenrir.mvp.compat.AbsMvpFragment
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.DrawerEditPresenter
import dev.ragnarok.fenrir.mvp.view.IDrawerEditView

class DrawerEditFragment : AbsMvpFragment<DrawerEditPresenter, IDrawerEditView>(),
    IDrawerEditView {
    private var mAdapter: DrawerCategotiesAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dialog_drawers_categories, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
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
        mAdapter = DrawerCategotiesAdapter(emptyList())
        recyclerView.adapter = mAdapter
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.drawer_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            presenter?.fireSaveClick()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.drawer_edit_title)
            actionBar.subtitle = null
        }
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<DrawerEditPresenter> {
        return object : IPresenterFactory<DrawerEditPresenter> {
            override fun create(): DrawerEditPresenter {
                return DrawerEditPresenter(saveInstanceState)
            }
        }
    }

    override fun displayData(data: List<DrawerCategory>) {
        mAdapter?.setData(data)
    }

    override fun goBackAndApplyChanges() {
        requireActivity().onBackPressed()
    }

    companion object {
        fun newInstance(): DrawerEditFragment {
            val args = Bundle()
            val fragment = DrawerEditFragment()
            fragment.arguments = args
            return fragment
        }
    }
}