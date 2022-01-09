package dev.ragnarok.fenrir.db.model;

public class PhotoPatch {

    private Like like;
    private Deletion deletion;

    public Deletion getDeletion() {
        return deletion;
    }

    public PhotoPatch setDeletion(Deletion deletion) {
        this.deletion = deletion;
        return this;
    }

    public Like getLike() {
        return like;
    }

    public PhotoPatch setLike(Like like) {
        this.like = like;
        return this;
    }

    public static final class Like {

        private final int count;

        private final boolean liked;

        public Like(int count, boolean liked) {
            this.count = count;
            this.liked = liked;
        }

        public boolean isLiked() {
            return liked;
        }

        public int getCount() {
            return count;
        }
    }

    public static final class Deletion {

        private final boolean deleted;

        public Deletion(boolean deleted) {
            this.deleted = deleted;
        }

        public boolean isDeleted() {
            return deleted;
        }
    }
}