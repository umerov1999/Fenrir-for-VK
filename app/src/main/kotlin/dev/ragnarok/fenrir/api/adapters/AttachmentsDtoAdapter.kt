package dev.ragnarok.fenrir.api.adapters

import com.google.gson.*
import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils
import java.lang.reflect.Type

class AttachmentsDtoAdapter : AbsAdapter(), JsonDeserializer<VKApiAttachments> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VKApiAttachments {
        if (!checkArray(json)) {
            return VKApiAttachments()
        }
        val array = json.asJsonArray
        val dto = VKApiAttachments(array.size())
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
            if (attachment != null && type.nonNullNoEmpty()) {
                dto.entries.add(VKApiAttachments.Entry(type, attachment))
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
            return when {
                VKApiAttachment.TYPE_PHOTO == type -> {
                    context.deserialize(o, VKApiPhoto::class.java)
                }
                VKApiAttachment.TYPE_VIDEO == type -> {
                    context.deserialize(o, VKApiVideo::class.java)
                }
                VKApiAttachment.TYPE_AUDIO == type -> {
                    context.deserialize(o, VKApiAudio::class.java)
                }
                VKApiAttachment.TYPE_DOC == type -> {
                    val doc: VKApiDoc = context.deserialize(o, VKApiDoc::class.java)
                    if ("lottie" == doc.ext) {
                        val sticker = VKApiSticker()
                        sticker.sticker_id = doc.id
                        sticker.animation_url = doc.url
                        return sticker
                    }
                    doc
                }
                VKApiAttachment.TYPE_POST == type || VKApiAttachment.TYPE_FAVE_POST == type -> {
                    context.deserialize(o, VKApiPost::class.java)
                    //} else if (VKApiAttachments.TYPE_POSTED_PHOTO.equals(type)) {
                    //    return context.deserialize(o, VKApiPostedPhoto.class);
                }
                VKApiAttachment.TYPE_LINK == type -> {
                    context.deserialize(o, VKApiLink::class.java)
                    //} else if (VKApiAttachments.TYPE_NOTE.equals(type)) {
                    //    return context.deserialize(o, VKApiNote.class);
                    //} else if (VKApiAttachments.TYPE_APP.equals(type)) {
                    //    return context.deserialize(o, VKApiApplicationContent.class);
                }
                VKApiAttachment.TYPE_ARTICLE == type -> {
                    context.deserialize(o, VKApiArticle::class.java)
                }
                VKApiAttachment.TYPE_POLL == type -> {
                    context.deserialize(o, VKApiPoll::class.java)
                }
                VKApiAttachment.TYPE_WIKI_PAGE == type -> {
                    context.deserialize(o, VKApiWikiPage::class.java)
                }
                VKApiAttachment.TYPE_ALBUM == type -> {
                    context.deserialize(o, VKApiPhotoAlbum::class.java)
                }
                VKApiAttachment.TYPE_STICKER == type -> {
                    context.deserialize(o, VKApiSticker::class.java)
                }
                VKApiAttachment.TYPE_AUDIO_MESSAGE == type -> {
                    context.deserialize(o, VKApiAudioMessage::class.java)
                }
                VKApiAttachment.TYPE_GIFT == type -> {
                    context.deserialize(o, VKApiGiftItem::class.java)
                }
                VKApiAttachment.TYPE_GRAFFITI == type -> {
                    context.deserialize(o, VKApiGraffiti::class.java)
                }
                VKApiAttachment.TYPE_STORY == type -> {
                    context.deserialize(o, VKApiStory::class.java)
                }
                VKApiAttachment.TYPE_CALL == type -> {
                    context.deserialize(o, VKApiCall::class.java)
                }
                VKApiAttachment.TYPE_AUDIO_PLAYLIST == type -> {
                    context.deserialize(o, VKApiAudioPlaylist::class.java)
                }
                VKApiAttachment.TYPE_WALL_REPLY == type -> {
                    context.deserialize(o, VKApiWallReply::class.java)
                }
                VKApiAttachment.TYPE_EVENT == type -> {
                    context.deserialize(o, VKApiEvent::class.java)
                }
                VKApiAttachment.TYPE_MARKET_ALBUM == type -> {
                    context.deserialize(o, VKApiMarketAlbum::class.java)
                }
                VKApiAttachment.TYPE_ARTIST == type -> {
                    context.deserialize(o, VKApiAudioArtist::class.java)
                }
                VKApiAttachment.TYPE_MARKET == type || VKApiAttachment.TYPE_PRODUCT == type -> {
                    context.deserialize(o, VKApiMarket::class.java)
                }
                !Utils.isValueAssigned(
                    type,
                    VKApiAttachment.IGNORE_ATTACHMENTS
                ) -> {
                    VKApiNotSupported(type, o.toString())
                }
                else -> {
                    null
                }
            }
        }
    }
}