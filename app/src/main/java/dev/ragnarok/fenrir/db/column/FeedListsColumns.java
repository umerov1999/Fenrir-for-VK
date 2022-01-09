package dev.ragnarok.fenrir.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.db.model.entity.FeedListEntity;
import dev.ragnarok.fenrir.util.Objects;

public class FeedListsColumns implements BaseColumns {

    public static final String TABLENAME = "feed_sources";
    public static final String TITLE = "title";
    public static final String NO_REPOSTS = "no_reposts";
    public static final String SOURCE_IDS = "source_ids";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_NO_REPOSTS = TABLENAME + "." + NO_REPOSTS;
    public static final String FULL_SOURCE_IDS = TABLENAME + "." + SOURCE_IDS;

    private FeedListsColumns() {
    }

    public static ContentValues getCV(@NonNull FeedListEntity entity) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, entity.getId());
        cv.put(TITLE, entity.getTitle());
        cv.put(NO_REPOSTS, entity.isNoReposts());

        String sources = null;
        int[] ids = entity.getSourceIds();

        if (Objects.nonNull(ids)) {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < ids.length; i++) {
                builder.append(ids[i]);

                if (i != ids.length - 1) {
                    builder.append(",");
                }
            }

            sources = builder.toString();
        }

        cv.put(SOURCE_IDS, sources);
        return cv;
    }

}