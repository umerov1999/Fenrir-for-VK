package dev.ragnarok.fenrir.fragment.search

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.adapter.MessagesAdapter
import dev.ragnarok.fenrir.adapter.MessagesAdapter.OnMessageActionListener
import dev.ragnarok.fenrir.fragment.search.criteria.MessageSearchCriteria
import dev.ragnarok.fenrir.model.Keyboard
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.search.MessagesSearchPresenter
import dev.ragnarok.fenrir.mvp.view.search.IMessagesSearchView
import dev.ragnarok.fenrir.place.PlaceFactory.getMessagesLookupPlace

class MessagesSearchFragment :
    AbsSearchFragment<MessagesSearchPresenter, IMessagesSearchView, Message, MessagesAdapter>(),
    OnMessageActionListener, IMessagesSearchView {
    override fun setAdapterData(adapter: MessagesAdapter, data: MutableList<Message>) {
        adapter.setItems(data)
    }

    override fun postCreate(root: View) {}
    override fun createAdapter(data: MutableList<Message>): MessagesAdapter {
        val adapter = MessagesAdapter(requireActivity(), data, this, true)
        //adapter.setOnHashTagClickListener(this);
        adapter.setOnMessageActionListener(this)
        return adapter
    }

    override fun createLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(requireActivity())
    }

    override fun onAvatarClick(message: Message, userId: Int, position: Int) {
        presenter?.fireOwnerClick(
            userId
        )
    }

    override fun onLongAvatarClick(message: Message, userId: Int, position: Int) {
        presenter?.fireOwnerClick(
            userId
        )
    }

    override fun onRestoreClick(message: Message, position: Int) {
        // delete is not supported
    }

    override fun onBotKeyboardClick(button: Keyboard.Button) {
        // is not supported
    }

    override fun onMessageLongClick(message: Message, position: Int): Boolean {
        return false
    }

    override fun onMessageClicked(message: Message, position: Int) {
        presenter?.fireMessageClick(
            message
        )
    }

    override fun onMessageDelete(message: Message) {}
    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<MessagesSearchPresenter> {
        return object : IPresenterFactory<MessagesSearchPresenter> {
            override fun create(): MessagesSearchPresenter {
                val accountId = requireArguments().getInt(Extra.ACCOUNT_ID)
                val c: MessageSearchCriteria? = requireArguments().getParcelable(Extra.CRITERIA)
                return MessagesSearchPresenter(accountId, c, saveInstanceState)
            }
        }
    }

    override fun goToMessagesLookup(accountId: Int, peerId: Int, messageId: Int) {
        getMessagesLookupPlace(accountId, peerId, messageId, null).tryOpenWith(requireActivity())
    }

    companion object {

        fun newInstance(
            accountId: Int,
            initialCriteria: MessageSearchCriteria?
        ): MessagesSearchFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putParcelable(Extra.CRITERIA, initialCriteria)
            val fragment = MessagesSearchFragment()
            fragment.arguments = args
            return fragment
        }
    }
}