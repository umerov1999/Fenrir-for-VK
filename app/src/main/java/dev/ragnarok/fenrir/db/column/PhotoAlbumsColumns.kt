package dev.ragnarok.fenrir.db.column

import android.content.ContentValues
import android.provider.BaseColumns
import com.google.gson.Gson
import dev.ragnarok.fenrir.api.model.VKApiPhotoAlbum
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transform

object PhotoAlbumsColumns : BaseColumns {
    const val TABLENAME = "photo_albums"

    //Columns
    const val ALBUM_ID = "album_id"
    const val OWNER_ID = "owner_id"
    const val TITLE = "title"
    const val SIZE = "size"
    const val PRIVACY_VIEW = "privacy_view"
    const val PRIVACY_COMMENT = "privacy_comment"
    const val DESCRIPTION = "description"
    const val CAN_UPLOAD = "can_upload"
    const val UPDATED = "updated"
    const val CREATED = "created"
    const val SIZES = "sizes"
    const val UPLOAD_BY_ADMINS = "upload_by_admins"
    const val COMMENTS_DISABLED = "comments_disabled"
    const val FULL_ID = TABLENAME + "." + BaseColumns._ID
    const val FULL_ALBUM_ID = "$TABLENAME.$ALBUM_ID"
    const val FULL_OWNER_ID = "$TABLENAME.$OWNER_ID"
    const val FULL_TITLE = "$TABLENAME.$TITLE"
    const val FULL_SIZE = "$TABLENAME.$SIZE"
    const val FULL_PRIVACY_VIEW = "$TABLENAME.$PRIVACY_VIEW"
    const val FULL_PRIVACY_COMMENT = "$TABLENAME.$PRIVACY_COMMENT"
    const val FULL_DESCRIPTION = "$TABLENAME.$DESCRIPTION"
    const val FULL_CAN_UPLOAD = "$TABLENAME.$CAN_UPLOAD"
    const val FULL_UPDATED = "$TABLENAME.$UPDATED"
    const val FULL_CREATED = "$TABLENAME.$CREATED"
    const val FULL_SIZES = "$TABLENAME.$SIZES"
    const val FULL_UPLOAD_BY_ADMINS = "$TABLENAME.$UPLOAD_BY_ADMINS"
    const val FULL_COMMENTS_DISABLED = "$TABLENAME.$COMMENTS_DISABLED"
    private val GSON = Gson()
    fun getCV(p: VKApiPhotoAlbum): ContentValues {
        val cv = ContentValues()
        cv.put(ALBUM_ID, p.id)
        cv.put(OWNER_ID, p.owner_id)
        cv.put(TITLE, p.title)
        cv.put(SIZE, p.size)
        cv.put(PRIVACY_VIEW, if (p.privacy_view == null) null else p.privacy_view.toString())
        cv.put(
            PRIVACY_COMMENT,
            if (p.privacy_comment == null) null else p.privacy_comment.toString()
        )
        cv.put(DESCRIPTION, p.description)
        cv.put(CAN_UPLOAD, p.can_upload)
        cv.put(UPDATED, p.updated)
        cv.put(CREATED, p.created)
        if (p.photo != null) {
            cv.put(SIZES, GSON.toJson(transform(p.photo)))
        } else {
            cv.putNull(SIZES)
        }
        cv.put(UPLOAD_BY_ADMINS, p.upload_by_admins_only)
        cv.put(COMMENTS_DISABLED, p.comments_disabled)
        return cv
    }
}