package dev.ragnarok.fenrir.api.adapters

import com.google.gson.*
import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.api.model.feedback.*
import java.lang.reflect.Type

class FeedbackDtoAdapter : AbsAdapter(), JsonDeserializer<VkApiBaseFeedback> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VkApiBaseFeedback {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val root = json.asJsonObject
        return when (val type = optString(root, "type")) {
            "follow", "friend_accepted" -> USERS_PARSER.parse(
                root,
                context
            )
            "mention" -> MENTION_WALL_PARSER.parse(root, context)
            "wall", "wall_publish" -> WALL_PARSER.parse(root, context)
            "comment_photo", "comment_post", "comment_video" -> CREATE_COMMENT_PARSER.parse(
                root,
                context
            )
            "reply_comment", "reply_comment_photo", "reply_comment_video", "reply_topic" -> REPLY_COMMENT_PARSER.parse(
                root,
                context
            )
            "like_video", "like_photo", "like_post" -> LIKE_PARSER.parse(
                root,
                context
            )
            "like_comment_photo", "like_comment_video", "like_comment_topic", "like_comment" -> LIKE_COMMENT_PARSER.parse(
                root,
                context
            )
            "copy_post", "copy_photo", "copy_video" -> COPY_PARSER.parse(
                root,
                context
            )
            "mention_comment_photo", "mention_comment_video", "mention_comments" -> MENTION_COMMENT_PARSER.parse(
                root,
                context
            )
            else -> throw UnsupportedOperationException("unsupported type $type")
        }
    }

    private abstract class Parser<T : VkApiBaseFeedback?> {
        protected abstract fun createDto(): T
        open fun parse(root: JsonObject, context: JsonDeserializationContext): T {
            val dto = createDto()
            dto?.type = optString(root, "type")
            dto?.date = optLong(root, "date")
            if (root.has("reply")) {
                dto?.reply = context.deserialize(root["reply"], VKApiComment::class.java)
            }
            return dto
        }
    }

    private class BaseCopyParser : Parser<VkApiCopyFeedback>() {
        override fun createDto(): VkApiCopyFeedback {
            return VkApiCopyFeedback()
        }

        override fun parse(
            root: JsonObject,
            context: JsonDeserializationContext
        ): VkApiCopyFeedback {
            val dto = super.parse(root, context)
            dto.copies = context.deserialize(root["feedback"], Copies::class.java)
            val copyClass: Type = when (dto.type) {
                "copy_post" -> VKApiPost::class.java
                "copy_photo" -> VKApiPhoto::class.java
                "copy_video" -> VKApiVideo::class.java
                else -> throw UnsupportedOperationException("Unsupported feedback type: " + dto.type)
            }
            dto.what = context.deserialize(root["parent"], copyClass)
            return dto
        }
    }

    private class BaseCreateCommentParser : Parser<VkApiCommentFeedback>() {
        override fun createDto(): VkApiCommentFeedback {
            return VkApiCommentFeedback()
        }

        override fun parse(
            root: JsonObject,
            context: JsonDeserializationContext
        ): VkApiCommentFeedback {
            val dto = super.parse(root, context)
            dto.comment = context.deserialize(root["feedback"], VKApiComment::class.java)
            val commentableClass: Type = when (dto.type) {
                "comment_post" -> VKApiPost::class.java
                "comment_photo" -> VKApiPhoto::class.java
                "comment_video" -> VKApiVideo::class.java
                else -> throw UnsupportedOperationException("Unsupported feedback type: " + dto.type)
            }
            dto.comment_of = context.deserialize(root["parent"], commentableClass)
            return dto
        }
    }

    private class BaseReplyCommentParser : Parser<VkApiReplyCommentFeedback>() {
        override fun createDto(): VkApiReplyCommentFeedback {
            return VkApiReplyCommentFeedback()
        }

        override fun parse(
            root: JsonObject,
            context: JsonDeserializationContext
        ): VkApiReplyCommentFeedback {
            val dto = super.parse(root, context)
            dto.feedback_comment = context.deserialize(root["feedback"], VKApiComment::class.java)
            if ("reply_topic" == dto.type) {
                dto.own_comment = null
                dto.comments_of = context.deserialize(root["parent"], VKApiTopic::class.java)
                return dto
            }
            dto.own_comment = context.deserialize(root["parent"], VKApiComment::class.java)
            val commentableClass: Type
            val parentCommentableField: String
            when (dto.type) {
                "reply_comment" -> {
                    commentableClass = VKApiPost::class.java
                    parentCommentableField = "post"
                }
                "reply_comment_photo" -> {
                    commentableClass = VKApiPhoto::class.java
                    parentCommentableField = "photo"
                }
                "reply_comment_video" -> {
                    commentableClass = VKApiVideo::class.java
                    parentCommentableField = "video"
                }
                else -> throw UnsupportedOperationException("Unsupported feedback type: " + dto.type)
            }
            dto.comments_of = context.deserialize(
                root.getAsJsonObject("parent")[parentCommentableField],
                commentableClass
            )
            return dto
        }
    }

    private class BaseUsersParser : Parser<VkApiUsersFeedback>() {
        override fun createDto(): VkApiUsersFeedback {
            return VkApiUsersFeedback()
        }

        override fun parse(
            root: JsonObject,
            context: JsonDeserializationContext
        ): VkApiUsersFeedback {
            val dto = super.parse(root, context)
            dto.users = context.deserialize(root["feedback"], UserArray::class.java)
            return dto
        }
    }

    private class LikeParser : Parser<VkApiLikeFeedback>() {
        override fun createDto(): VkApiLikeFeedback {
            return VkApiLikeFeedback()
        }

        override fun parse(
            root: JsonObject,
            context: JsonDeserializationContext
        ): VkApiLikeFeedback {
            val dto = super.parse(root, context)
            val likedClass: Type = when (dto.type) {
                "like_photo" -> VKApiPhoto::class.java
                "like_post" -> VKApiPost::class.java
                "like_video" -> VKApiVideo::class.java
                else -> throw UnsupportedOperationException("Unsupported feedback type: " + dto.type)
            }
            dto.liked = context.deserialize(root["parent"], likedClass)
            dto.users = context.deserialize(root["feedback"], UserArray::class.java)
            return dto
        }
    }

    private class BaseLikeCommentParser : Parser<VkApiLikeCommentFeedback>() {
        override fun createDto(): VkApiLikeCommentFeedback {
            return VkApiLikeCommentFeedback()
        }

        override fun parse(
            root: JsonObject,
            context: JsonDeserializationContext
        ): VkApiLikeCommentFeedback {
            val dto = super.parse(root, context)
            val commentableClass: Type
            val parentJsonField: String
            when (dto.type) {
                "like_comment" -> {
                    commentableClass = VKApiPost::class.java
                    parentJsonField = "post"
                }
                "like_comment_photo" -> {
                    commentableClass = VKApiPhoto::class.java
                    parentJsonField = "photo"
                }
                "like_comment_video" -> {
                    commentableClass = VKApiVideo::class.java
                    parentJsonField = "video"
                }
                "like_comment_topic" -> {
                    commentableClass = VKApiTopic::class.java
                    parentJsonField = "topic"
                }
                else -> throw UnsupportedOperationException("Unsupported feedback type: " + dto.type)
            }
            dto.users = context.deserialize(root["feedback"], UserArray::class.java)
            dto.comment = context.deserialize(root["parent"], VKApiComment::class.java)
            dto.commented = context.deserialize(
                root.getAsJsonObject("parent")[parentJsonField],
                commentableClass
            )
            return dto
        }
    }

    private class BaseMentionWallParser : Parser<VkApiMentionWallFeedback>() {
        override fun createDto(): VkApiMentionWallFeedback {
            return VkApiMentionWallFeedback()
        }

        override fun parse(
            root: JsonObject,
            context: JsonDeserializationContext
        ): VkApiMentionWallFeedback {
            val dto = super.parse(root, context)
            dto.post = context.deserialize(root["feedback"], VKApiPost::class.java)
            return dto
        }
    }

    private class BaseWallParser : Parser<VkApiWallFeedback>() {
        override fun createDto(): VkApiWallFeedback {
            return VkApiWallFeedback()
        }

        override fun parse(
            root: JsonObject,
            context: JsonDeserializationContext
        ): VkApiWallFeedback {
            val dto = super.parse(root, context)
            dto.post = context.deserialize(root["feedback"], VKApiPost::class.java)
            return dto
        }
    }

    private class BaseMentionCommentParser : Parser<VkApiMentionCommentFeedback>() {
        override fun createDto(): VkApiMentionCommentFeedback {
            return VkApiMentionCommentFeedback()
        }

        override fun parse(
            root: JsonObject,
            context: JsonDeserializationContext
        ): VkApiMentionCommentFeedback {
            val dto = super.parse(root, context)
            dto.where = context.deserialize(root["feedback"], VKApiComment::class.java)
            val commentableClass: Type = when (dto.type) {
                "mention_comments" -> VKApiPost::class.java
                "mention_comment_photo" -> VKApiPhoto::class.java
                "mention_comment_video" -> VKApiVideo::class.java
                else -> throw UnsupportedOperationException("Unsupported feedback type: " + dto.type)
            }
            dto.comment_of = context.deserialize(root["parent"], commentableClass)
            return dto
        }
    }

    companion object {
        private val TAG = FeedbackDtoAdapter::class.java.simpleName
        private val MENTION_COMMENT_PARSER = BaseMentionCommentParser()
        private val LIKE_COMMENT_PARSER = BaseLikeCommentParser()
        private val COPY_PARSER = BaseCopyParser()
        private val CREATE_COMMENT_PARSER = BaseCreateCommentParser()
        private val REPLY_COMMENT_PARSER = BaseReplyCommentParser()
        private val LIKE_PARSER = LikeParser()
        private val USERS_PARSER = BaseUsersParser()
        private val MENTION_WALL_PARSER = BaseMentionWallParser()
        private val WALL_PARSER = BaseWallParser()
    }
}