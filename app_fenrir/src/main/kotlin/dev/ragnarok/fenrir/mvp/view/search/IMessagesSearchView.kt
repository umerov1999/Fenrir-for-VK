package dev.ragnarok.fenrir.mvp.view.search

import dev.ragnarok.fenrir.model.Message

interface IMessagesSearchView : IBaseSearchView<Message> {
    fun goToMessagesLookup(accountId: Int, peerId: Int, messageId: Int)
    fun configNowVoiceMessagePlaying(
        id: Int,
        progress: Float,
        paused: Boolean,
        amin: Boolean,
        speed: Boolean
    )

    fun bindVoiceHolderById(
        holderId: Int,
        play: Boolean,
        paused: Boolean,
        progress: Float,
        amin: Boolean,
        speed: Boolean
    )

    fun disableVoicePlaying()
}