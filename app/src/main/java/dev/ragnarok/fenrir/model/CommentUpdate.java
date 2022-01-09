package dev.ragnarok.fenrir.model;

public class CommentUpdate {

    private final int accountId;

    private final Commented commented;

    private final int commentId;

    private LikeUpdate likeUpdate;

    private DeleteUpdate deleteUpdate;

    private CommentUpdate(int accountId, Commented commented, int commentId) {
        this.accountId = accountId;
        this.commented = commented;
        this.commentId = commentId;
    }

    public static CommentUpdate create(int accountId, Commented commented, int commentId) {
        return new CommentUpdate(accountId, commented, commentId);
    }

    public int getAccountId() {
        return accountId;
    }

    public CommentUpdate withDeletion(boolean deleted) {
        deleteUpdate = new DeleteUpdate(deleted);
        return this;
    }

    public Commented getCommented() {
        return commented;
    }

    public int getCommentId() {
        return commentId;
    }

    public boolean hasDeleteUpdate() {
        return deleteUpdate != null;
    }

    public boolean hasLikesUpdate() {
        return likeUpdate != null;
    }

    public DeleteUpdate getDeleteUpdate() {
        return deleteUpdate;
    }

    public LikeUpdate getLikeUpdate() {
        return likeUpdate;
    }

    public CommentUpdate withLikes(boolean userLikes, int count) {
        likeUpdate = new LikeUpdate(userLikes, count);
        return this;
    }

    public static final class DeleteUpdate {

        private final boolean deleted;

        private DeleteUpdate(boolean deleted) {
            this.deleted = deleted;
        }

        public boolean isDeleted() {
            return deleted;
        }
    }

    public static final class LikeUpdate {

        private final boolean userLikes;

        private final int count;

        private LikeUpdate(boolean userLikes, int count) {
            this.userLikes = userLikes;
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public boolean isUserLikes() {
            return userLikes;
        }
    }

}
