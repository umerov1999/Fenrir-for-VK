package dev.ragnarok.fenrir.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import dev.ragnarok.fenrir.api.model.VKApiPhotoAlbum;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.util.Objects;

public final class PhotoAlbumsColumns implements BaseColumns {

    public static final String TABLENAME = "photo_albums";
    //Columns
    public static final String ALBUM_ID = "album_id";
    public static final String OWNER_ID = "owner_id";
    public static final String TITLE = "title";
    public static final String SIZE = "size";
    public static final String PRIVACY_VIEW = "privacy_view";
    public static final String PRIVACY_COMMENT = "privacy_comment";
    public static final String DESCRIPTION = "description";
    public static final String CAN_UPLOAD = "can_upload";
    public static final String UPDATED = "updated";
    public static final String CREATED = "created";
    public static final String SIZES = "sizes";
    public static final String UPLOAD_BY_ADMINS = "upload_by_admins";
    public static final String COMMENTS_DISABLED = "comments_disabled";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_ALBUM_ID = TABLENAME + "." + ALBUM_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_SIZE = TABLENAME + "." + SIZE;
    public static final String FULL_PRIVACY_VIEW = TABLENAME + "." + PRIVACY_VIEW;
    public static final String FULL_PRIVACY_COMMENT = TABLENAME + "." + PRIVACY_COMMENT;
    public static final String FULL_DESCRIPTION = TABLENAME + "." + DESCRIPTION;
    public static final String FULL_CAN_UPLOAD = TABLENAME + "." + CAN_UPLOAD;
    public static final String FULL_UPDATED = TABLENAME + "." + UPDATED;
    public static final String FULL_CREATED = TABLENAME + "." + CREATED;
    public static final String FULL_SIZES = TABLENAME + "." + SIZES;
    public static final String FULL_UPLOAD_BY_ADMINS = TABLENAME + "." + UPLOAD_BY_ADMINS;
    public static final String FULL_COMMENTS_DISABLED = TABLENAME + "." + COMMENTS_DISABLED;
    private static final Gson GSON = new Gson();

    private PhotoAlbumsColumns() {
    }

    public static ContentValues getCV(@NonNull VKApiPhotoAlbum p) {
        ContentValues cv = new ContentValues();
        cv.put(ALBUM_ID, p.id);
        cv.put(OWNER_ID, p.owner_id);
        cv.put(TITLE, p.title);
        cv.put(SIZE, p.size);
        cv.put(PRIVACY_VIEW, p.privacy_view == null ? null : p.privacy_view.toString());
        cv.put(PRIVACY_COMMENT, p.privacy_comment == null ? null : p.privacy_comment.toString());
        cv.put(DESCRIPTION, p.description);
        cv.put(CAN_UPLOAD, p.can_upload);
        cv.put(UPDATED, p.updated);
        cv.put(CREATED, p.created);

        if (Objects.nonNull(p.photo)) {
            cv.put(SIZES, GSON.toJson(Dto2Model.transform(p.photo)));
        } else {
            cv.putNull(SIZES);
        }

        cv.put(UPLOAD_BY_ADMINS, p.upload_by_admins_only);
        cv.put(COMMENTS_DISABLED, p.comments_disabled);
        return cv;
    }
}
