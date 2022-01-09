package dev.ragnarok.fenrir.link.types;

public class WallCommentLink extends AbsLink {

    private final int ownerId;

    private final int postId;

    private final int commentId;

    public WallCommentLink(int ownerId, int postId, int commentId) {
        super(WALL_COMMENT);
        this.ownerId = ownerId;
        this.postId = postId;
        this.commentId = commentId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getCommentId() {
        return commentId;
    }

    public int getPostId() {
        return postId;
    }

    @Override
    public boolean isValid() {
        return ownerId != 0 && postId > 0 && commentId > 0;
    }
}