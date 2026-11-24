package dev.ragnarok.fenrir.api.model.longpoll

import dev.ragnarok.fenrir.api.adapters.LongpollUpdateDtoAdapter
import kotlinx.serialization.Serializable

@Serializable(with = LongpollUpdateDtoAdapter::class)
open class AbsLongpollEvent(val action: Int) {
    companion object {
        const val ACTION_MESSAGES_FLAGS_SET = 10002
        const val ACTION_MESSAGES_FLAGS_RESET = 10003
        const val ACTION_MESSAGE_ADDED = 10004
        const val ACTION_MESSAGE_EDITED = 10005
        const val ACTION_SET_INPUT_MESSAGES_AS_READ = 10006
        const val ACTION_SET_OUTPUT_MESSAGES_AS_READ = 10007
        const val ACTION_MESSAGE_CHANGED = 10018
        const val ACTION_PIN_CONVERSATION = 20
        const val ACTION_CHANGE_MINOR_ID = 21
        const val ACTION_USER_TYPING_TEXT_IN_CONVERSATION = 63
        const val ACTION_USER_RECORDING_VOICE_IN_CONVERSATION = 64
        const val ACTION_USER_UPLOADING_PHOTO_IN_CONVERSATION = 65
        const val ACTION_USER_UPLOADING_VIDEO_IN_CONVERSATION = 66
        const val ACTION_USER_UPLOADING_FILE_IN_CONVERSATION = 67
        const val ACTION_COUNTER_UNREAD_WAS_CHANGED = 80
        const val ACTION_MESSAGE_REACTION_CHANGE = 601
        const val ACTION_UPDATE_UNREAD_REACTIONS_IN_CONVERSATION = 602
    }
}