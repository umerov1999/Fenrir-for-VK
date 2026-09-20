package dev.ragnarok.fenrir.db.interfaces

import dev.ragnarok.fenrir.db.model.entity.CommentEntity
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.model.Comment
import dev.ragnarok.fenrir.model.CommentIntent
import dev.ragnarok.fenrir.model.CommentUpdate
import dev.ragnarok.fenrir.model.Commented
import dev.ragnarok.fenrir.model.DraftComment
import dev.ragnarok.fenrir.model.criteria.CommentsCriteria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface ICommentsStorage : IStorage {
    fun insert(
        accountId: Long,
        sourceId: Int,
        sourceOwnerId: Long,
        sourceType: Int,
        dbos: List<CommentEntity>,
        owners: OwnerEntities?,
        clearBefore: Boolean
    ): Flow<IntArray>

    fun getDbosByCriteria(criteria: CommentsCriteria): Flow<List<CommentEntity>>

    fun findEditingComment(accountId: Long, commented: Commented): Flow<DraftComment?>

    fun saveDraftComment(
        accountId: Long,
        commented: Commented,
        text: String?,
        replyToUser: Long,
        replyToComment: Int
    ): Flow<Int>

    fun commitMinorUpdate(update: CommentUpdate): Flow<Boolean>
    fun observeMinorUpdates(): SharedFlow<CommentUpdate>
    fun deleteByDbId(accountId: Long, dbId: Int): Flow<Boolean>
    fun updateDraftCommentAndGet(
        accountId: Long,
        dbId: Int,
        newCommentId: Int,
        intent: CommentIntent
    ): Flow<Comment>

    fun getByDbId(accountId: Long, dbId: Int): Flow<CommentEntity>
    fun commitEditComment(
        accountId: Long,
        commentId: Int,
        commented: Commented,
        text: String?,
        attachments: List<AbsModel>?
    ): Flow<Comment>
}