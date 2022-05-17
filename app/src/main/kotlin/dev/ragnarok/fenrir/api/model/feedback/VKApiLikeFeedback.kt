package dev.ragnarok.fenrir.api.model.feedback

import dev.ragnarok.fenrir.api.model.Likeable

class VKApiLikeFeedback : VKApiBaseFeedback() {
    var users: UserArray? = null
    var liked: Likeable? = null
}