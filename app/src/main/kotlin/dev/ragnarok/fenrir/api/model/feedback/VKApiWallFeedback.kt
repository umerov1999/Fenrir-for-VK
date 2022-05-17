package dev.ragnarok.fenrir.api.model.feedback

import dev.ragnarok.fenrir.api.model.VKApiPost

class VKApiWallFeedback : VKApiBaseFeedback() {
    var post: VKApiPost? = null
}