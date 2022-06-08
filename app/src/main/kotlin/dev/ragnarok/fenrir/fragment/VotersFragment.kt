package dev.ragnarok.fenrir.fragment

import android.os.Bundle
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.VotersPresenter
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView

class VotersFragment : AbsOwnersListFragment<VotersPresenter, ISimpleOwnersView>() {
    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<VotersPresenter> {
        return object : IPresenterFactory<VotersPresenter> {
            override fun create(): VotersPresenter {
                return VotersPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getInt(Extra.OWNER_ID),
                    requireArguments().getInt(Extra.POLL),
                    requireArguments().getInt(Extra.ANSWER),
                    requireArguments().getBoolean(Extra.IS_BOARD),
                    saveInstanceState
                )
            }
        }
    }

    override fun hasToolbar(): Boolean {
        return true
    }

    override fun needShowCount(): Boolean {
        return true
    }

    companion object {
        fun newInstance(args: Bundle?): VotersFragment {
            val fragment = VotersFragment()
            fragment.arguments = args
            return fragment
        }

        fun buildArgs(
            accountId: Int,
            ownerId: Int,
            pollId: Int,
            board: Boolean,
            answer: Int
        ): Bundle {
            val bundle = Bundle()
            bundle.putInt(Extra.ACCOUNT_ID, accountId)
            bundle.putInt(Extra.OWNER_ID, ownerId)
            bundle.putInt(Extra.POLL, pollId)
            bundle.putInt(Extra.ANSWER, answer)
            bundle.putBoolean(Extra.IS_BOARD, board)
            return bundle
        }
    }
}