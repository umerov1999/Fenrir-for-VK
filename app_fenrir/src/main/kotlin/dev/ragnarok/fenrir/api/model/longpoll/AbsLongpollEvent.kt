package dev.ragnarok.fenrir.api.model.longpoll

import dev.ragnarok.fenrir.api.adapters.LongpollUpdateDtoAdapter
import kotlinx.serialization.Serializable

@Serializable(with = LongpollUpdateDtoAdapter::class)
open class AbsLongpollEvent(val action: Int) {
    companion object {
        const val ACTION_MESSAGES_FLAGS_SET = 2
        const val ACTION_MESSAGES_FLAGS_RESET = 3
        const val ACTION_MESSAGE_ADDED = 4
        const val ACTION_MESSAGE_EDITED = 5
        const val ACTION_SET_INPUT_MESSAGES_AS_READ = 6
        const val ACTION_SET_OUTPUT_MESSAGES_AS_READ = 7
        const val ACTION_MESSAGE_CHANGED = 18
        const val ACTION_USER_TYPING_TEXT_IN_DIALOG = 63
        const val ACTION_USER_RECORDING_VOICE_IN_DIALOG = 64
        const val ACTION_USER_UPLOADING_PHOTO_IN_DIALOG = 65
        const val ACTION_USER_UPLOADING_VIDEO_IN_DIALOG = 66
        const val ACTION_USER_UPLOADING_FILE_IN_DIALOG = 67
        const val ACTION_COUNTER_UNREAD_WAS_CHANGED = 80
        const val ACTION_MESSAGE_REACTION_CHANGE = 601
    }
}
