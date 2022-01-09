package dev.ragnarok.fenrir.fragment.conversation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.adapter.DocsAdapter;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.conversations.ChatAttachmentDocsPresenter;
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentDocsView;

public class ConversationDocsFragment extends AbsChatAttachmentsFragment<Document, ChatAttachmentDocsPresenter, IChatAttachmentDocsView>
        implements DocsAdapter.ActionListener, IChatAttachmentDocsView {

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
    }

    @Override
    public RecyclerView.Adapter<?> createAdapter() {
        DocsAdapter simpleDocRecycleAdapter = new DocsAdapter(Collections.emptyList());
        simpleDocRecycleAdapter.setActionListener(this);
        return simpleDocRecycleAdapter;
    }

    @Override
    public void displayAttachments(List<Document> data) {
        if (getAdapter() instanceof DocsAdapter) {
            ((DocsAdapter) getAdapter()).setItems(data);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<ChatAttachmentDocsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ChatAttachmentDocsPresenter(
                requireArguments().getInt(Extra.PEER_ID),
                requireArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void onDocClick(int index, @NonNull Document doc) {
        callPresenter(p -> p.fireDocClick(doc));
    }

    @Override
    public boolean onDocLongClick(int index, @NonNull Document doc) {
        callPresenter(p -> p.fireGoToMessagesLookup(doc.getMsgPeerId(), doc.getMsgId()));
        return true;
    }
}
