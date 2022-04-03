package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.mvp.view.IFwdsView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers

class FwdsPresenter(accountId: Int, messages: List<Message>, savedInstanceState: Bundle?) :
    AbsMessageListPresenter<IFwdsView>(accountId, savedInstanceState) {
    fun fireTranscript(voiceMessageId: String?, messageId: Int) {
        appendDisposable(
            messages.recogniseAudioMessage(accountId, messageId, voiceMessageId)
                .compose(applySingleIOToMainSchedulers())
                .subscribe({ }) { })
    }

    init {
        if (!messages.isNullOrEmpty()) {
            data.addAll(messages)
        }
    }
}