package dev.ragnarok.fenrir.link.types;

public class WallCommentThreadLink extends AbsLink {

    private final int ownerId;

    private final int postId;

    private final int commentId;

    private final int threadId;

    public WallCommentThreadLink(int ownerId, int postId, int commentId, int threadId) {
        super(WALL_COMMENT_THREAD);
        this.ownerId = ownerId;
        this.postId = postId;
        this.commentId = commentId;
        this.threadId = threadId;
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

    public int getThreadId() {
        return threadId;
    }

    @Override
    public boolean isValid() {
        return ownerId != 0 && postId > 0 && commentId > 0 && threadId > 0;
    }
}
