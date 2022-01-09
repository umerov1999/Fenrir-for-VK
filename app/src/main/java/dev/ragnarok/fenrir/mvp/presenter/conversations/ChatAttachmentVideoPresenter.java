package dev.ragnarok.fenrir.mvp.presenter.conversations;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.Apis;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.response.AttachmentsHistoryResponse;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentVideoView;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Single;

public class ChatAttachmentVideoPresenter extends BaseChatAttachmentsPresenter<Video, IChatAttachmentVideoView> {

    public ChatAttachmentVideoPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @Override
    Single<Pair<String, List<Video>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, "video", nextFrom, 1, 50, null)
                .map(response -> {
                    List<Video> videos = new ArrayList<>(safeCountOf(response.items));

                    if (nonNull(response.items)) {
                        for (AttachmentsHistoryResponse.One one : response.items) {
                            if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VKApiVideo) {
                                VKApiVideo dto = (VKApiVideo) one.entry.attachment;
                                videos.add(Dto2Model.transform(dto).setMsgId(one.messageId).setMsgPeerId(peerId));
                            }
                        }
                    }

                    return Pair.Companion.create(response.next_from, videos);
                });
    }

    @Override
    public void onGuiCreated(@NonNull IChatAttachmentVideoView view) {
        super.onGuiCreated(view);

        resolveToolbar();
    }

    private void resolveToolbar() {
        callView(v -> {
            v.setToolbarTitle(getString(R.string.attachments_in_chat));
            v.setToolbarSubtitle(getString(R.string.videos_count, safeCountOf(data)));
        });
    }
}