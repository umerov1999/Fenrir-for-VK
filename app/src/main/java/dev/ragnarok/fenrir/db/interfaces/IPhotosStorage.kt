package dev.ragnarok.fenrir.db.interfaces

import dev.ragnarok.fenrir.db.model.PhotoPatch
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity
import dev.ragnarok.fenrir.model.criteria.PhotoCriteria
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IPhotosStorage : IStorage {
    fun insertPhotosRx(
        accountId: Int,
        ownerId: Int,
        albumId: Int,
        photos: List<PhotoEntity>,
        clearBefore: Boolean
    ): Completable

    fun findPhotosByCriteriaRx(criteria: PhotoCriteria): Single<List<PhotoEntity>>
    fun applyPatch(accountId: Int, ownerId: Int, photoId: Int, patch: PhotoPatch): Completable
}