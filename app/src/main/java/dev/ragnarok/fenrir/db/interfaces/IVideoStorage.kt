package dev.ragnarok.fenrir.db.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.db.model.entity.VideoEntity
import dev.ragnarok.fenrir.model.VideoCriteria
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IVideoStorage : IStorage {
    @CheckResult
    fun findByCriteria(criteria: VideoCriteria): Single<List<VideoEntity>>

    @CheckResult
    fun insertData(
        accountId: Int,
        ownerId: Int,
        albumId: Int,
        videos: List<VideoEntity>,
        invalidateBefore: Boolean
    ): Completable
}