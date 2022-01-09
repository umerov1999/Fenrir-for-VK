package dev.ragnarok.fenrir.fragment.conversation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.adapter.LinksAdapter;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.conversations.ChatAttachmentPostsPresenter;
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentPostsView;

public class ConversationPostsFragment extends AbsChatAttachmentsFragment<Link, ChatAttachmentPostsPresenter, IChatAttachmentPostsView>
        implements LinksAdapter.ActionListener, LinksAdapter.LinkConversationListener, IChatAttachmentPostsView {

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
    }

    @Override
    public RecyclerView.Adapter<?> createAdapter() {
        LinksAdapter simpleDocRecycleAdapter = new LinksAdapter(Collections.emptyList());
        simpleDocRecycleAdapter.setActionListener(this);
        return simpleDocRecycleAdapter;
    }

    @Override
    public void displayAttachments(List<Link> data) {
        if (getAdapter() instanceof LinksAdapter) {
            ((LinksAdapter) getAdapter()).setItems(data);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<ChatAttachmentPostsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ChatAttachmentPostsPresenter(
                requireArguments().getInt(Extra.PEER_ID),
                requireArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void onLinkClick(int index, @NonNull Link link) {
        callPresenter(p -> p.fireLinkClick(link));
    }

    @Override
    public void onGoLinkConversation(@NonNull Link doc) {
        callPresenter(p -> p.fireGoToMessagesLookup(doc.getMsgPeerId(), doc.getMsgId()));
    }
}
