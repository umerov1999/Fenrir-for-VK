package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({DocType.TEXT, DocType.ARCHIVE, DocType.GIF, DocType.IMAGE,
        DocType.AUDIO, DocType.VIDEO, DocType.EBOOK, DocType.UNKNOWN})
@Retention(RetentionPolicy.SOURCE)
public @interface DocType {
    int TEXT = 1;
    int ARCHIVE = 2;
    int GIF = 3;
    int IMAGE = 4;
    int AUDIO = 5;
    int VIDEO = 6;
    int EBOOK = 7;
    int UNKNOWN = 8;
}
