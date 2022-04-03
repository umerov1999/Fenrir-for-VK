package dev.ragnarok.fenrir.api.adapters

import com.google.gson.*
import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.util.Utils
import java.lang.reflect.Type

class AttachmentsDtoAdapter : AbsAdapter(), JsonDeserializer<VkApiAttachments> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VkApiAttachments {
        val dto = VkApiAttachments()
        if (!checkArray(json)) {
            return dto
        }
        val array = json.asJsonArray
        dto.entries = ArrayList(array.size())
        for (i in 0 until array.size()) {
            if (!checkObject(array[i])) {
                continue
            }
            val o = array[i].asJsonObject
            val type = optString(o, "type")
            val attachment: VKApiAttachment? = try {
                parse(type, o, context)
            } catch (e: Exception) {
                e.printStackTrace()
                continue
            }
            if (attachment != null) {
                dto.entries.add(VkApiAttachments.Entry(type, attachment))
            }
        }
        return dto
    }

    companion object {

        fun parse(
            type: String?,
            root: JsonObject,
            context: JsonDeserializationContext
        ): VKApiAttachment? {
            type ?: return null
            val o = root[type]

            //{"type":"photos_list","photos_list":["406536042_456239026"]}
            return if (VKApiAttachment.TYPE_PHOTO == type) {
                context.deserialize(o, VKApiPhoto::class.java)
            } else if (VKApiAttachment.TYPE_VIDEO == type) {
                context.deserialize(o, VKApiVideo::class.java)
            } else if (VKApiAttachment.TYPE_AUDIO == type) {
                context.deserialize(o, VKApiAudio::class.java)
            } else if (VKApiAttachment.TYPE_DOC == type) {
                val doc: VkApiDoc = context.deserialize(o, VkApiDoc::class.java)
                if ("lottie" == doc.ext) {
                    val sticker = VKApiSticker()
                    sticker.sticker_id = doc.id
                    sticker.animation_url = doc.url
                    return sticker
                }
                doc
            } else if (VKApiAttachment.TYPE_POST == type || VKApiAttachment.TYPE_FAVE_POST == type) {
                context.deserialize(o, VKApiPost::class.java)
                //} else if (VkApiAttachments.TYPE_POSTED_PHOTO.equals(type)) {
                //    return context.deserialize(o, VKApiPostedPhoto.class);
            } else if (VKApiAttachment.TYPE_LINK == type) {
                context.deserialize(o, VKApiLink::class.java)
                //} else if (VkApiAttachments.TYPE_NOTE.equals(type)) {
                //    return context.deserialize(o, VKApiNote.class);
                //} else if (VkApiAttachments.TYPE_APP.equals(type)) {
                //    return context.deserialize(o, VKApiApplicationContent.class);
            } else if (VKApiAttachment.TYPE_ARTICLE == type) {
                context.deserialize(o, VKApiArticle::class.java)
            } else if (VKApiAttachment.TYPE_POLL == type) {
                context.deserialize(o, VKApiPoll::class.java)
            } else if (VKApiAttachment.TYPE_WIKI_PAGE == type) {
                context.deserialize(o, VKApiWikiPage::class.java)
            } else if (VKApiAttachment.TYPE_ALBUM == type) {
                context.deserialize(o, VKApiPhotoAlbum::class.java)
            } else if (VKApiAttachment.TYPE_STICKER == type) {
                context.deserialize(o, VKApiSticker::class.java)
            } else if (VKApiAttachment.TYPE_AUDIO_MESSAGE == type) {
                context.deserialize(o, VkApiAudioMessage::class.java)
            } else if (VKApiAttachment.TYPE_GIFT == type) {
                context.deserialize(o, VKApiGiftItem::class.java)
            } else if (VKApiAttachment.TYPE_GRAFFITI == type) {
                context.deserialize(o, VKApiGraffiti::class.java)
            } else if (VKApiAttachment.TYPE_STORY == type) {
                context.deserialize(o, VKApiStory::class.java)
            } else if (VKApiAttachment.TYPE_CALL == type) {
                context.deserialize(o, VKApiCall::class.java)
            } else if (VKApiAttachment.TYPE_AUDIO_PLAYLIST == type) {
                context.deserialize(o, VKApiAudioPlaylist::class.java)
            } else if (VKApiAttachment.TYPE_WALL_REPLY == type) {
                context.deserialize(o, VKApiWallReply::class.java)
            } else if (VKApiAttachment.TYPE_EVENT == type) {
                context.deserialize(o, VkApiEvent::class.java)
            } else if (VKApiAttachment.TYPE_MARKET_ALBUM == type) {
                context.deserialize(o, VkApiMarketAlbum::class.java)
            } else if (VKApiAttachment.TYPE_ARTIST == type) {
                context.deserialize(o, VKApiAudioArtist::class.java)
            } else if (VKApiAttachment.TYPE_MARKET == type || VKApiAttachment.TYPE_PRODUCT == type) {
                context.deserialize(o, VkApiMarket::class.java)
            } else if (!Utils.isValueAssigned(
                    type,
                    VKApiAttachment.IGNORE_ATTACHMENTS
                )
            ) {
                VKApiNotSupported(type, o.toString())
            } else {
                null
            }
        }
    }
}