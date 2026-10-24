package dev.ragnarok.fenrir.db.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.db.model.entity.PhotoAlbumDboEntity
import dev.ragnarok.fenrir.model.criteria.PhotoAlbumsCriteria
import kotlinx.coroutines.flow.Flow

interface IPhotoAlbumsStorage : IStorage {
    @CheckResult
    fun findAlbumById(
        accountId: Long,
        ownerId: Long,
        albumId: Int
    ): Flow<PhotoAlbumDboEntity?>

    @CheckResult
    fun findAlbumsByCriteria(criteria: PhotoAlbumsCriteria): Flow<List<PhotoAlbumDboEntity>>

    @CheckResult
    fun store(
        accountId: Long,
        ownerId: Long,
        albums: List<PhotoAlbumDboEntity>,
        clearBefore: Boolean
    ): Flow<Boolean>

    @CheckResult
    fun removeAlbumById(accountId: Long, ownerId: Long, albumId: Int): Flow<Boolean>
}