package dev.ragnarok.fenrir.model.selection;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({Types.LOCAL_PHOTOS, Types.VK_PHOTOS, Types.FILES, Types.VIDEOS, Types.LOCAL_GALLERY})
@Retention(RetentionPolicy.SOURCE)
public @interface Types {
    int LOCAL_PHOTOS = 0;
    int VK_PHOTOS = 1;
    int FILES = 2;
    int VIDEOS = 3;
    int LOCAL_GALLERY = 4;
}