package dev.ragnarok.fenrir.fragment.messages.conversationattachments.conversationaudios

import dev.ragnarok.fenrir.fragment.messages.conversationattachments.abschatattachments.IBaseChatAttachmentsView
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio

interface IChatAttachmentAudiosView : IBaseChatAttachmentsView<Audio> {
    fun onOpenMenu(accountId: Long, position: Int, audio: Audio)
    fun onAudioLyricsReceived(lyricText: String, audio: Audio)
    fun onDownloadAudioNextStage()
    fun onStartAudioDownload(accountId: Long, position: Int, audio: Audio)
    fun onShowBitrate(duration: Int, size: Long)
    fun onSelectToSendAttachment(audios: ArrayList<Audio>)
    fun onServiceBindEvent(@PlayerStatus status: Int)
}