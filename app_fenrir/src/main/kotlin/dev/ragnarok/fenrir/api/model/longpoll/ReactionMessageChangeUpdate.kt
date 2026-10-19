package dev.ragnarok.fenrir.api.model.longpoll

import dev.ragnarok.fenrir.api.model.VKApiReaction

class ReactionMessageChangeUpdate(
    @param:ReactionEventType val event_type_of_reaction: Int,
    val peerId: Long,
    val cmId: Int,
    val myReaction: Int,
    val myReactionChanged: Boolean,
    val arrayReactionList: ArrayList<VKApiReaction>
) : AbsLongpollEvent(ACTION_MESSAGE_REACTION_CHANGE)