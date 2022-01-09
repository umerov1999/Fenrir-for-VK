package dev.ragnarok.fenrir.db.model;

public class PostUpdate {

    private final int accountId;

    private final int postId;

    private final int ownerId;

    private PinUpdate pinUpdate;

    private DeleteUpdate deleteUpdate;

    private LikeUpdate likeUpdate;

    public PostUpdate(int accountId, int postId, int ownerId) {
        this.accountId = accountId;
        this.postId = postId;
        this.ownerId = ownerId;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getPostId() {
        return postId;
    }

    public LikeUpdate getLikeUpdate() {
        return likeUpdate;
    }

    public PostUpdate withDeletion(boolean deleted) {
        deleteUpdate = new DeleteUpdate(deleted);
        return this;
    }

    public PostUpdate withPin(boolean pinned) {
        pinUpdate = new PinUpdate(pinned);
        return this;
    }

    public PostUpdate withLikes(int count, boolean usesLikes) {
        likeUpdate = new LikeUpdate(usesLikes, count);
        return this;
    }

    public DeleteUpdate getDeleteUpdate() {
        return deleteUpdate;
    }


    public PinUpdate getPinUpdate() {
        return pinUpdate;
    }

    public static class PinUpdate {

        private final boolean pinned;

        private PinUpdate(boolean pinned) {
            this.pinned = pinned;
        }

        public boolean isPinned() {
            return pinned;
        }
    }

    public static class DeleteUpdate {

        private final boolean deleted;

        private DeleteUpdate(boolean deleted) {
            this.deleted = deleted;
        }

        public boolean isDeleted() {
            return deleted;
        }
    }

    public static class LikeUpdate {

        private final boolean liked;

        private final int count;

        private LikeUpdate(boolean liked, int count) {
            this.liked = liked;
            this.count = count;
        }

        public boolean isLiked() {
            return liked;
        }

        public int getCount() {
            return count;
        }
    }
}