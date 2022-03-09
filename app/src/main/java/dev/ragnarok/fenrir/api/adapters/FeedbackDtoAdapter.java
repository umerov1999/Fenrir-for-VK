package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.Commentable;
import dev.ragnarok.fenrir.api.model.Copyable;
import dev.ragnarok.fenrir.api.model.Likeable;
import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiTopic;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.feedback.Copies;
import dev.ragnarok.fenrir.api.model.feedback.UserArray;
import dev.ragnarok.fenrir.api.model.feedback.VkApiBaseFeedback;
import dev.ragnarok.fenrir.api.model.feedback.VkApiCommentFeedback;
import dev.ragnarok.fenrir.api.model.feedback.VkApiCopyFeedback;
import dev.ragnarok.fenrir.api.model.feedback.VkApiLikeCommentFeedback;
import dev.ragnarok.fenrir.api.model.feedback.VkApiLikeFeedback;
import dev.ragnarok.fenrir.api.model.feedback.VkApiMentionCommentFeedback;
import dev.ragnarok.fenrir.api.model.feedback.VkApiMentionWallFeedback;
import dev.ragnarok.fenrir.api.model.feedback.VkApiReplyCommentFeedback;
import dev.ragnarok.fenrir.api.model.feedback.VkApiUsersFeedback;
import dev.ragnarok.fenrir.api.model.feedback.VkApiWallFeedback;

public class FeedbackDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiBaseFeedback> {
    private static final String TAG = FeedbackDtoAdapter.class.getSimpleName();
    private static final BaseMentionCommentParser MENTION_COMMENT_PARSER = new BaseMentionCommentParser();
    private static final BaseLikeCommentParser LIKE_COMMENT_PARSER = new BaseLikeCommentParser();
    private static final BaseCopyParser COPY_PARSER = new BaseCopyParser();
    private static final BaseCreateCommentParser CREATE_COMMENT_PARSER = new BaseCreateCommentParser();
    private static final BaseReplyCommentParser REPLY_COMMENT_PARSER = new BaseReplyCommentParser();
    private static final LikeParser LIKE_PARSER = new LikeParser();
    private static final BaseUsersParser USERS_PARSER = new BaseUsersParser();
    private static final BaseMentionWallParser MENTION_WALL_PARSER = new BaseMentionWallParser();
    private static final BaseWallParser WALL_PARSER = new BaseWallParser();

    @Override
    public VkApiBaseFeedback deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        JsonObject root = json.getAsJsonObject();

        String type = optString(root, "type");

        switch (type) {
            case "follow":
            case "friend_accepted":
                return USERS_PARSER.parse(root, context);

            case "mention":
                return MENTION_WALL_PARSER.parse(root, context);

            case "wall":
            case "wall_publish":
                return WALL_PARSER.parse(root, context);

            case "comment_photo":
            case "comment_post":
            case "comment_video":
                return CREATE_COMMENT_PARSER.parse(root, context);

            case "reply_comment":
            case "reply_comment_photo":
            case "reply_comment_video":
            case "reply_topic":
                return REPLY_COMMENT_PARSER.parse(root, context);

            case "like_video":
            case "like_photo":
            case "like_post":
                return LIKE_PARSER.parse(root, context);

            case "like_comment_photo":
            case "like_comment_video":
            case "like_comment_topic":
            case "like_comment":
                return LIKE_COMMENT_PARSER.parse(root, context);

            case "copy_post":
            case "copy_photo":
            case "copy_video":
                return COPY_PARSER.parse(root, context);

            case "mention_comment_photo":
            case "mention_comment_video":
            case "mention_comments":
                return MENTION_COMMENT_PARSER.parse(root, context);

            default:
                throw new UnsupportedOperationException("unsupported type " + type);
        }
    }

    private abstract static class Parser<T extends VkApiBaseFeedback> {
        protected abstract T createDto();

        T parse(JsonObject root, JsonDeserializationContext context) {
            T dto = createDto();
            dto.type = optString(root, "type");
            dto.date = optLong(root, "date");

            if (root.has("reply")) {
                dto.reply = context.deserialize(root.get("reply"), VKApiComment.class);
            }
            return dto;
        }
    }

    private static class BaseCopyParser extends Parser<VkApiCopyFeedback> {
        @Override
        protected VkApiCopyFeedback createDto() {
            return new VkApiCopyFeedback();
        }

        @Override
        VkApiCopyFeedback parse(JsonObject root, JsonDeserializationContext context) {
            VkApiCopyFeedback dto = super.parse(root, context);
            dto.copies = context.deserialize(root.get("feedback"), Copies.class);

            Class<? extends Copyable> copyClass;

            switch (dto.type) {
                case "copy_post":
                    copyClass = VKApiPost.class;
                    break;
                case "copy_photo":
                    copyClass = VKApiPhoto.class;
                    break;
                case "copy_video":
                    copyClass = VKApiVideo.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.what = context.deserialize(root.get("parent"), copyClass);
            return dto;
        }
    }

    private static class BaseCreateCommentParser extends Parser<VkApiCommentFeedback> {
        @Override
        protected VkApiCommentFeedback createDto() {
            return new VkApiCommentFeedback();
        }

        @Override
        VkApiCommentFeedback parse(JsonObject root, JsonDeserializationContext context) {
            VkApiCommentFeedback dto = super.parse(root, context);
            dto.comment = context.deserialize(root.get("feedback"), VKApiComment.class);

            Class<? extends Commentable> commentableClass;
            switch (dto.type) {
                case "comment_post":
                    commentableClass = VKApiPost.class;
                    break;
                case "comment_photo":
                    commentableClass = VKApiPhoto.class;
                    break;
                case "comment_video":
                    commentableClass = VKApiVideo.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.comment_of = context.deserialize(root.get("parent"), commentableClass);
            return dto;
        }
    }

    private static class BaseReplyCommentParser extends Parser<VkApiReplyCommentFeedback> {
        @Override
        protected VkApiReplyCommentFeedback createDto() {
            return new VkApiReplyCommentFeedback();
        }

        @Override
        VkApiReplyCommentFeedback parse(JsonObject root, JsonDeserializationContext context) {
            VkApiReplyCommentFeedback dto = super.parse(root, context);
            dto.feedback_comment = context.deserialize(root.get("feedback"), VKApiComment.class);

            if ("reply_topic".equals(dto.type)) {
                dto.own_comment = null;
                dto.comments_of = context.deserialize(root.get("parent"), VKApiTopic.class);
                return dto;
            }

            dto.own_comment = context.deserialize(root.get("parent"), VKApiComment.class);

            Class<? extends Commentable> commentableClass;
            String parentCommentableField;

            switch (dto.type) {
                case "reply_comment":
                    commentableClass = VKApiPost.class;
                    parentCommentableField = "post";
                    break;
                case "reply_comment_photo":
                    commentableClass = VKApiPhoto.class;
                    parentCommentableField = "photo";
                    break;
                case "reply_comment_video":
                    commentableClass = VKApiVideo.class;
                    parentCommentableField = "video";
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.comments_of = context.deserialize(root.getAsJsonObject("parent").get(parentCommentableField), commentableClass);
            return dto;
        }
    }

    private static class BaseUsersParser extends Parser<VkApiUsersFeedback> {
        @Override
        protected VkApiUsersFeedback createDto() {
            return new VkApiUsersFeedback();
        }

        @Override
        VkApiUsersFeedback parse(JsonObject root, JsonDeserializationContext context) {
            VkApiUsersFeedback dto = super.parse(root, context);
            dto.users = context.deserialize(root.get("feedback"), UserArray.class);
            return dto;
        }
    }

    private static class LikeParser extends Parser<VkApiLikeFeedback> {
        @Override
        protected VkApiLikeFeedback createDto() {
            return new VkApiLikeFeedback();
        }

        @Override
        VkApiLikeFeedback parse(JsonObject root, JsonDeserializationContext context) {
            VkApiLikeFeedback dto = super.parse(root, context);

            Class<? extends Likeable> likedClass;
            switch (dto.type) {
                case "like_photo":
                    likedClass = VKApiPhoto.class;
                    break;
                case "like_post":
                    likedClass = VKApiPost.class;
                    break;
                case "like_video":
                    likedClass = VKApiVideo.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.liked = context.deserialize(root.get("parent"), likedClass);
            dto.users = context.deserialize(root.get("feedback"), UserArray.class);
            return dto;
        }
    }

    private static class BaseLikeCommentParser extends Parser<VkApiLikeCommentFeedback> {
        @Override
        protected VkApiLikeCommentFeedback createDto() {
            return new VkApiLikeCommentFeedback();
        }

        @Override
        VkApiLikeCommentFeedback parse(JsonObject root, JsonDeserializationContext context) {
            VkApiLikeCommentFeedback dto = super.parse(root, context);

            Class<? extends Commentable> commentableClass;
            String parentJsonField;

            switch (dto.type) {
                case "like_comment":
                    commentableClass = VKApiPost.class;
                    parentJsonField = "post";
                    break;
                case "like_comment_photo":
                    commentableClass = VKApiPhoto.class;
                    parentJsonField = "photo";
                    break;
                case "like_comment_video":
                    commentableClass = VKApiVideo.class;
                    parentJsonField = "video";
                    break;
                case "like_comment_topic":
                    commentableClass = VKApiTopic.class;
                    parentJsonField = "topic";
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.users = context.deserialize(root.get("feedback"), UserArray.class);
            dto.comment = context.deserialize(root.get("parent"), VKApiComment.class);
            dto.commented = context.deserialize(root.getAsJsonObject("parent").get(parentJsonField), commentableClass);
            return dto;
        }
    }

    private static class BaseMentionWallParser extends Parser<VkApiMentionWallFeedback> {
        @Override
        protected VkApiMentionWallFeedback createDto() {
            return new VkApiMentionWallFeedback();
        }

        @Override
        VkApiMentionWallFeedback parse(JsonObject root, JsonDeserializationContext context) {
            VkApiMentionWallFeedback dto = super.parse(root, context);
            dto.post = context.deserialize(root.get("feedback"), VKApiPost.class);
            return dto;
        }
    }

    private static class BaseWallParser extends Parser<VkApiWallFeedback> {
        @Override
        protected VkApiWallFeedback createDto() {
            return new VkApiWallFeedback();
        }

        @Override
        VkApiWallFeedback parse(JsonObject root, JsonDeserializationContext context) {
            VkApiWallFeedback dto = super.parse(root, context);
            dto.post = context.deserialize(root.get("feedback"), VKApiPost.class);
            return dto;
        }
    }

    private static class BaseMentionCommentParser extends Parser<VkApiMentionCommentFeedback> {
        @Override
        protected VkApiMentionCommentFeedback createDto() {
            return new VkApiMentionCommentFeedback();
        }

        @Override
        VkApiMentionCommentFeedback parse(JsonObject root, JsonDeserializationContext context) {
            VkApiMentionCommentFeedback dto = super.parse(root, context);
            dto.where = context.deserialize(root.get("feedback"), VKApiComment.class);

            Class<? extends Commentable> commentableClass;

            switch (dto.type) {
                case "mention_comments":
                    commentableClass = VKApiPost.class;
                    break;
                case "mention_comment_photo":
                    commentableClass = VKApiPhoto.class;
                    break;
                case "mention_comment_video":
                    commentableClass = VKApiVideo.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.comment_of = context.deserialize(root.get("parent"), commentableClass);
            return dto;
        }
    }
}