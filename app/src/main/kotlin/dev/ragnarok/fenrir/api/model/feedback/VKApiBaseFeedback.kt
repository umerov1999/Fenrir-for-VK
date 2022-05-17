package dev.ragnarok.fenrir.api.model.feedback

import dev.ragnarok.fenrir.api.model.VKApiComment

abstract class VKApiBaseFeedback {
    var type: String? = null
    var date: Long = 0
    var reply: VKApiComment? = null
}