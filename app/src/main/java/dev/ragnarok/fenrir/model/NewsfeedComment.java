package dev.ragnarok.fenrir.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class NewsfeedComment {

    private final Object model;

    private Comment comment;

    public NewsfeedComment(Object model) {
        this.model = model;
    }

    /**
     * @return Photo, Video, Topic or Post
     */
    @NonNull
    public Object getModel() {
        return model;
    }

    @Nullable
    public Comment getComment() {
        return comment;
    }

    public NewsfeedComment setComment(Comment comment) {
        this.comment = comment;
        return this;
    }
}
