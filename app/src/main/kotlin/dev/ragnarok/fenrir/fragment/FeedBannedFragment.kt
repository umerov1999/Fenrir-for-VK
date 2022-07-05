package dev.ragnarok.fenrir.fragment

import android.os.Bundle
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.Option
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.FeedBannedPresenter
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView

class FeedBannedFragment : AbsOwnersListFragment<FeedBannedPresenter, ISimpleOwnersView>() {
    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<FeedBannedPresenter> {
        return object : IPresenterFactory<FeedBannedPresenter> {
            override fun create(): FeedBannedPresenter {
                return FeedBannedPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    saveInstanceState
                )
            }
        }
    }

    override fun onLongClick(owner: Owner): Boolean {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                1,
                requireActivity().getString(R.string.delete),
                R.drawable.ic_outline_delete,
                true
            )
        )
        menus.header(
            owner.fullName,
            R.drawable.person,
            owner.get100photoOrSmaller()
        )
        menus.columns(1)
        menus.show(
            requireActivity().supportFragmentManager,
            "ban_options",
            object : ModalBottomSheetDialogFragment.Listener {
                override fun onModalOptionSelected(option: Option) {
                    if (option.id == 1) {
                        presenter?.fireRemove(owner)
                    }
                }
            })
        return true
    }

    override fun hasToolbar(): Boolean {
        return true
    }

    override fun needShowCount(): Boolean {
        return true
    }

    companion object {
        fun newInstance(args: Bundle?): FeedBannedFragment {
            val fragment = FeedBannedFragment()
            fragment.arguments = args
            return fragment
        }

        fun buildArgs(
            accountId: Int
        ): Bundle {
            val bundle = Bundle()
            bundle.putInt(Extra.ACCOUNT_ID, accountId)
            return bundle
        }
    }
}
