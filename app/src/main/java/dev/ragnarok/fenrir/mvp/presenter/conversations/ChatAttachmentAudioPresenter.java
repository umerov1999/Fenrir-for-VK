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
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.response.AttachmentsHistoryResponse;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentAudiosView;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Single;

public class ChatAttachmentAudioPresenter extends BaseChatAttachmentsPresenter<Audio, IChatAttachmentAudiosView> {

    public ChatAttachmentAudioPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @Override
    Single<Pair<String, List<Audio>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, "audio", nextFrom, 0, 50, null)
                .map(response -> {
                    List<Audio> audios = new ArrayList<>(safeCountOf(response.items));

                    if (nonNull(response.items)) {
                        for (AttachmentsHistoryResponse.One one : response.items) {
                            if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VKApiAudio) {
                                VKApiAudio dto = (VKApiAudio) one.entry.attachment;
                                audios.add(Dto2Model.transform(dto));
                            }
                        }
                    }

                    return Pair.Companion.create(response.next_from, audios);
                });
    }

    @SuppressWarnings("unused")
    public void fireAudioPlayClick(int position, Audio audio) {
        fireAudioPlayClick(position, new ArrayList<>(data));
    }

    @Override
    public void onGuiCreated(@NonNull IChatAttachmentAudiosView view) {
        super.onGuiCreated(view);

        resolveToolbar();
    }

    private void resolveToolbar() {
        callView(v -> {
            v.setToolbarTitle(getString(R.string.attachments_in_chat));
            v.setToolbarSubtitle(getString(R.string.audios_count, safeCountOf(data)));
        });
    }
}