package dev.ragnarok.fenrir.fragment.messages.fwds

import android.os.Bundle
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.fragment.messages.AbsMessageListPresenter
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.hiddenIO

class FwdsPresenter(accountId: Long, messages: List<Message>, savedInstanceState: Bundle?) :
    AbsMessageListPresenter<IFwdsView>(accountId, savedInstanceState) {
    fun fireTranscript(voiceMessageId: String?, peerId: Long, cmId: Int) {
        appendJob(
            messages.recogniseAudioMessage(accountId, peerId, cmId, voiceMessageId)
                .hiddenIO()
        )
    }

    init {
        if (messages.isNotEmpty()) {
            data.addAll(messages)
        }
    }
}