package dev.ragnarok.fenrir.db.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.db.model.entity.PhotoAlbumEntity
import dev.ragnarok.fenrir.model.criteria.PhotoAlbumsCriteria
import dev.ragnarok.fenrir.util.Optional
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IPhotoAlbumsStorage : IStorage {
    @CheckResult
    fun findAlbumById(
        accountId: Int,
        ownerId: Int,
        albumId: Int
    ): Single<Optional<PhotoAlbumEntity>>

    @CheckResult
    fun findAlbumsByCriteria(criteria: PhotoAlbumsCriteria): Single<List<PhotoAlbumEntity>>

    @CheckResult
    fun store(
        accountId: Int,
        ownerId: Int,
        albums: List<PhotoAlbumEntity>,
        clearBefore: Boolean
    ): Completable

    @CheckResult
    fun removeAlbumById(accountId: Int, ownerId: Int, albumId: Int): Completable
}