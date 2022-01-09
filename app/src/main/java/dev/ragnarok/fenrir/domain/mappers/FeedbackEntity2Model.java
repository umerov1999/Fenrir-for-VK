package dev.ragnarok.fenrir.domain.mappers;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dev.ragnarok.fenrir.db.model.IdPairEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.CopyEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.FeedbackEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.LikeCommentEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.LikeEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.MentionCommentEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.MentionEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.NewCommentEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.PostFeedbackEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.ReplyCommentEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.UsersEntity;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.IOwnersBundle;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.feedback.CommentFeedback;
import dev.ragnarok.fenrir.model.feedback.CopyFeedback;
import dev.ragnarok.fenrir.model.feedback.Feedback;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;
import dev.ragnarok.fenrir.model.feedback.LikeCommentFeedback;
import dev.ragnarok.fenrir.model.feedback.LikeFeedback;
import dev.ragnarok.fenrir.model.feedback.MentionCommentFeedback;
import dev.ragnarok.fenrir.model.feedback.MentionFeedback;
import dev.ragnarok.fenrir.model.feedback.PostPublishFeedback;
import dev.ragnarok.fenrir.model.feedback.ReplyCommentFeedback;
import dev.ragnarok.fenrir.model.feedback.UsersFeedback;

public class FeedbackEntity2Model {

    public static Feedback buildFeedback(FeedbackEntity entity, IOwnersBundle owners) {
        Feedback feedback;

        switch (entity.getType()) {
            case FeedbackType.FOLLOW:
            case FeedbackType.FRIEND_ACCEPTED:
                feedback = buildUsersFeedback((UsersEntity) entity, owners);
                break;

            case FeedbackType.MENTION:
                feedback = buildMentionFeedback((MentionEntity) entity, owners);
                break;

            case FeedbackType.MENTION_COMMENT_POST:
            case FeedbackType.MENTION_COMMENT_PHOTO:
            case FeedbackType.MENTION_COMMENT_VIDEO:
                feedback = buildMentionCommentFeedback((MentionCommentEntity) entity, owners);
                break;

            case FeedbackType.WALL:
            case FeedbackType.WALL_PUBLISH:
                feedback = buildPostPublishFeedback((PostFeedbackEntity) entity, owners);
                break;

            case FeedbackType.COMMENT_POST:
            case FeedbackType.COMMENT_PHOTO:
            case FeedbackType.COMMENT_VIDEO:
                feedback = buildCommentFeedback((NewCommentEntity) entity, owners);
                break;

            case FeedbackType.REPLY_COMMENT:
            case FeedbackType.REPLY_COMMENT_PHOTO:
            case FeedbackType.REPLY_COMMENT_VIDEO:
            case FeedbackType.REPLY_TOPIC:
                feedback = buildReplyCommentFeedback((ReplyCommentEntity) entity, owners);
                break;

            case FeedbackType.LIKE_POST:
            case FeedbackType.LIKE_PHOTO:
            case FeedbackType.LIKE_VIDEO:
                feedback = buildLikeFeedback((LikeEntity) entity, owners);
                break;

            case FeedbackType.LIKE_COMMENT_POST:
            case FeedbackType.LIKE_COMMENT_PHOTO:
            case FeedbackType.LIKE_COMMENT_VIDEO:
            case FeedbackType.LIKE_COMMENT_TOPIC:
                feedback = buildLikeCommentFeedback((LikeCommentEntity) entity, owners);
                break;

            case FeedbackType.COPY_POST:
            case FeedbackType.COPY_PHOTO:
            case FeedbackType.COPY_VIDEO:
                feedback = buildCopyFeedback((CopyEntity) entity, owners);
                break;

            default:
                throw new UnsupportedOperationException("Unsupported feedback type, type: " + entity.getType());
        }

        if (nonNull(entity.getReply())) {
            feedback.setReply(Entity2Model.buildCommentFromDbo(entity.getReply(), owners));
        }

        feedback.setDate(entity.getDate());
        return feedback;
    }

    private static UsersFeedback buildUsersFeedback(UsersEntity entity, IOwnersBundle owners) {
        return new UsersFeedback(entity.getType())
                .setOwners(buildUserArray(entity.getOwners(), owners));
    }

    private static List<Owner> buildUserArray(@NonNull int[] usersIds, @NonNull IOwnersBundle owners) {
        List<Owner> data = new ArrayList<>(usersIds.length);

        for (int id : usersIds) {
            data.add(owners.getById(id));
        }

        return data;
    }

    private static CommentFeedback buildCommentFeedback(NewCommentEntity entity, IOwnersBundle owners) {
        return new CommentFeedback(entity.getType())
                .setCommentOf(Entity2Model.buildAttachmentFromDbo(entity.getCommented(), owners))
                .setComment(Entity2Model.buildCommentFromDbo(entity.getComment(), owners));
    }

    private static CopyFeedback buildCopyFeedback(CopyEntity entity, IOwnersBundle owners) {
        CopyFeedback feedback = new CopyFeedback(entity.getType());
        feedback.setWhat(Entity2Model.buildAttachmentFromDbo(entity.getCopied(), owners));

        List<Owner> copyOwners = new LinkedList<>();
        for (IdPairEntity pair : entity.getCopies().getPairDbos()) {
            copyOwners.add(owners.getById(pair.getOwnerId()));
        }

        feedback.setOwners(copyOwners);
        return feedback;
    }

    private static LikeCommentFeedback buildLikeCommentFeedback(LikeCommentEntity entity, IOwnersBundle owners) {
        LikeCommentFeedback feedback = new LikeCommentFeedback(entity.getType());
        feedback.setOwners(buildUserArray(entity.getLikesOwnerIds(), owners));
        feedback.setLiked(Entity2Model.buildCommentFromDbo(entity.getLiked(), owners));
        feedback.setCommented(Entity2Model.buildAttachmentFromDbo(entity.getCommented(), owners));
        return feedback;
    }

    private static LikeFeedback buildLikeFeedback(LikeEntity entity, IOwnersBundle owners) {
        return new LikeFeedback(entity.getType())
                .setOwners(buildUserArray(entity.getLikesOwnerIds(), owners))
                .setLiked(Entity2Model.buildAttachmentFromDbo(entity.getLiked(), owners));
    }

    private static MentionCommentFeedback buildMentionCommentFeedback(MentionCommentEntity entity, IOwnersBundle owners) {
        return new MentionCommentFeedback(entity.getType())
                .setWhere(Entity2Model.buildCommentFromDbo(entity.getWhere(), owners))
                .setCommentOf(Entity2Model.buildAttachmentFromDbo(entity.getCommented(), owners));
    }

    private static MentionFeedback buildMentionFeedback(MentionEntity entity, IOwnersBundle owners) {
        return new MentionFeedback(entity.getType())
                .setWhere(Entity2Model.buildAttachmentFromDbo(entity.getWhere(), owners));
    }

    private static PostPublishFeedback buildPostPublishFeedback(PostFeedbackEntity entity, IOwnersBundle owners) {
        return new PostPublishFeedback(entity.getType())
                .setPost(Entity2Model.buildPostFromDbo(entity.getPost(), owners));
    }

    private static ReplyCommentFeedback buildReplyCommentFeedback(ReplyCommentEntity entity, IOwnersBundle owners) {
        Comment ownComment = null;

        if (nonNull(entity.getOwnComment())) {
            ownComment = Entity2Model.buildCommentFromDbo(entity.getOwnComment(), owners);
        }

        return new ReplyCommentFeedback(entity.getType())
                .setCommentsOf(Entity2Model.buildAttachmentFromDbo(entity.getCommented(), owners))
                .setFeedbackComment(Entity2Model.buildCommentFromDbo(entity.getFeedbackComment(), owners))
                .setOwnComment(ownComment);
    }

    @FeedbackType
    public static int transformType(String apitype) {
        switch (apitype) {
            case "follow":
                return FeedbackType.FOLLOW;
            case "friend_accepted":
                return FeedbackType.FRIEND_ACCEPTED;
            case "mention":
                return FeedbackType.MENTION;
            case "mention_comments":
                return FeedbackType.MENTION_COMMENT_POST;
            case "wall":
                return FeedbackType.WALL;
            case "wall_publish":
                return FeedbackType.WALL_PUBLISH;
            case "comment_post":
                return FeedbackType.COMMENT_POST;
            case "comment_photo":
                return FeedbackType.COMMENT_PHOTO;
            case "comment_video":
                return FeedbackType.COMMENT_VIDEO;
            case "reply_comment":
                return FeedbackType.REPLY_COMMENT;
            case "reply_comment_photo":
                return FeedbackType.REPLY_COMMENT_PHOTO;
            case "reply_comment_video":
                return FeedbackType.REPLY_COMMENT_VIDEO;
            case "reply_topic":
                return FeedbackType.REPLY_TOPIC;
            case "like_post":
                return FeedbackType.LIKE_POST;
            case "like_comment":
                return FeedbackType.LIKE_COMMENT_POST;
            case "like_photo":
                return FeedbackType.LIKE_PHOTO;
            case "like_video":
                return FeedbackType.LIKE_VIDEO;
            case "like_comment_photo":
                return FeedbackType.LIKE_COMMENT_PHOTO;
            case "like_comment_video":
                return FeedbackType.LIKE_COMMENT_VIDEO;
            case "like_comment_topic":
                return FeedbackType.LIKE_COMMENT_TOPIC;
            case "copy_post":
                return FeedbackType.COPY_POST;
            case "copy_photo":
                return FeedbackType.COPY_PHOTO;
            case "copy_video":
                return FeedbackType.COPY_VIDEO;
            case "mention_comment_photo":
                return FeedbackType.MENTION_COMMENT_PHOTO;
            case "mention_comment_video":
                return FeedbackType.MENTION_COMMENT_VIDEO;
            default:
                throw new UnsupportedOperationException();
        }
    }
}