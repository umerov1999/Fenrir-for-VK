package dev.ragnarok.fenrir.api.model.response

import dev.ragnarok.fenrir.api.model.VKApiOwner

class LikesListResponse {
    var count = 0
    var owners: ArrayList<VKApiOwner>? = null
}