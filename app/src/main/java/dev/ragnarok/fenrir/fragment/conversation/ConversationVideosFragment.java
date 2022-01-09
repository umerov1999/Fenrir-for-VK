package dev.ragnarok.fenrir.fragment.conversation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.VideosAdapter;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.conversations.ChatAttachmentVideoPresenter;
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentVideoView;

public class ConversationVideosFragment extends AbsChatAttachmentsFragment<Video, ChatAttachmentVideoPresenter, IChatAttachmentVideoView>
        implements VideosAdapter.VideoOnClickListener, IChatAttachmentVideoView {

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columns = getResources().getInteger(R.integer.videos_column_count);
        return new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public RecyclerView.Adapter<?> createAdapter() {
        VideosAdapter adapter = new VideosAdapter(requireActivity(), Collections.emptyList());
        adapter.setVideoOnClickListener(this);
        return adapter;
    }

    @Override
    public void onVideoClick(int position, Video video) {
        callPresenter(p -> p.fireVideoClick(video));
    }

    @Override
    public boolean onVideoLongClick(int position, Video video) {
        callPresenter(p -> p.fireGoToMessagesLookup(video.getMsgPeerId(), video.getMsgId()));
        return true;
    }

    @Override
    public void displayAttachments(List<Video> data) {
        VideosAdapter adapter = (VideosAdapter) getAdapter();
        adapter.setData(data);
    }

    @NonNull
    @Override
    public IPresenterFactory<ChatAttachmentVideoPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = requireArguments().getInt(Extra.PEER_ID);
            return new ChatAttachmentVideoPresenter(peerId, accountId, saveInstanceState);
        };
    }
}