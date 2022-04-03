package dev.ragnarok.fenrir.db.interfaces

import dev.ragnarok.fenrir.db.model.entity.VideoAlbumEntity
import dev.ragnarok.fenrir.model.VideoAlbumCriteria
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IVideoAlbumsStorage : IStorage {
    fun findByCriteria(criteria: VideoAlbumCriteria): Single<List<VideoAlbumEntity>>
    fun insertData(
        accountId: Int,
        ownerId: Int,
        data: List<VideoAlbumEntity>,
        invalidateBefore: Boolean
    ): Completable
}