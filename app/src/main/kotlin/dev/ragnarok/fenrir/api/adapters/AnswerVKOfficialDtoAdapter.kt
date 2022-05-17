package dev.ragnarok.fenrir.api.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import dev.ragnarok.fenrir.api.model.VKApiPhoto
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.model.AnswerVKOfficial
import dev.ragnarok.fenrir.model.AnswerVKOfficial.*
import dev.ragnarok.fenrir.model.AnswerVKOfficialList
import dev.ragnarok.fenrir.model.AnswerVKOfficialList.AnswerField
import dev.ragnarok.fenrir.nonNullNoEmpty
import java.lang.reflect.Type

class AnswerVKOfficialDtoAdapter : AbsAdapter(), JsonDeserializer<AnswerVKOfficialList> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AnswerVKOfficialList {
        if (!checkObject(json)) {
            throw JsonParseException("$TAG error parse object")
        }
        val dtolist = AnswerVKOfficialList()
        val root = json.asJsonObject
        dtolist.items = ArrayList()
        dtolist.fields = ArrayList()
        val photos: MutableList<VKApiPhoto> = ArrayList()
        if (hasArray(root, "photos")) {
            val temp = root.getAsJsonArray("photos")
            for (i in temp) {
                if (!checkObject(i)) {
                    continue
                }
                photos.add(context.deserialize(i, VKApiPhoto::class.java))
            }
        }
        if (hasArray(root, "profiles")) {
            val temp = root.getAsJsonArray("profiles")
            for (i in temp) {
                if (!checkObject(i)) {
                    continue
                }
                val obj = i.asJsonObject
                val id = optInt(obj, "id")
                if (obj.has("photo_200")) {
                    val url = optString(obj, "photo_200")
                    url?.let { AnswerField(id, it) }?.let { dtolist.fields?.add(it) }
                } else if (obj.has("photo_200_orig")) {
                    val url = optString(obj, "photo_200_orig")
                    url?.let { AnswerField(id, it) }?.let { dtolist.fields?.add(it) }
                }
            }
        }
        if (hasArray(root, "groups")) {
            val temp = root.getAsJsonArray("groups")
            for (i in temp) {
                if (!checkObject(i)) {
                    continue
                }
                val obj = i.asJsonObject
                val id = optInt(obj, "id") * -1
                if (obj.has("photo_200")) {
                    val url = optString(obj, "photo_200")
                    url?.let { AnswerField(id, it) }?.let { dtolist.fields?.add(it) }
                } else if (obj.has("photo_200_orig")) {
                    val url = optString(obj, "photo_200_orig")
                    url?.let { AnswerField(id, it) }?.let { dtolist.fields?.add(it) }
                }
            }
        }
        if (!hasArray(root, "items")) return dtolist
        for (i in root.getAsJsonArray("items")) {
            if (!checkObject(i)) {
                continue
            }
            val root_item = i.asJsonObject
            val dto = AnswerVKOfficial()
            if (hasObject(root_item, "action")) {
                val action_item = root_item["action"].asJsonObject
                if ("authorize" == optString(action_item, "type")) {
                    dto.action = ActionURL(optString(action_item, "url"))
                } else if ("message_open" == optString(
                        action_item,
                        "type"
                    ) && hasObject(action_item, "context")
                ) {
                    val context_item = action_item["context"].asJsonObject
                    dto.action = ActionMessage(
                        optInt(context_item, "peer_id", 0),
                        optInt(context_item, "id", 0)
                    )
                }
            }
            try {
                if (hasObject(root_item, "action_buttons")) {
                    val action_buttons = root_item["action_buttons"].asJsonObject
                    for (ss1 in action_buttons.keySet()) {
                        if (checkArray(action_buttons[ss1])) {
                            for (ss2 in action_buttons.getAsJsonArray(ss1)) {
                                if (checkObject(ss2)) {
                                    val act = ss2.asJsonObject
                                    if (hasObject(act, "action")) {
                                        val actu = act.getAsJsonObject("action")
                                        if ("hide_item" == optString(
                                                actu,
                                                "type"
                                            ) && hasObject(actu, "context")
                                        ) {
                                            val actctx = actu.getAsJsonObject("context")
                                            dto.hide_query = optString(actctx, "query")
                                            if (dto.hide_query.nonNullNoEmpty()) {
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
            dto.iconType = optString(root_item, "icon_type")
            dto.header = optString(root_item, "header")
            dto.header.nonNullNoEmpty {
                dto.header = it.replace("{date}", "")
                    .replace("'''(((?!''').)*)'''".toRegex(), "<b>$1</b>")
                    .replace("\\[vk(ontakte)?://[A-Za-z0-9/?=]+\\|([^]]+)]".toRegex(), "$2")
            }
            dto.text = optString(root_item, "text")
            dto.text.nonNullNoEmpty {
                dto.text =
                    it.replace("{date}", "")
                        .replace("'''(((?!''').)*)'''".toRegex(), "<b>$1</b>")
                        .replace("\\[vk(ontakte)?://[A-Za-z0-9/?=]+\\|([^]]+)]".toRegex(), "$2")
            }
            dto.footer = optString(root_item, "footer")
            dto.footer.nonNullNoEmpty {
                dto.footer = it.replace("{date}", "")
                    .replace("'''(((?!''').)*)'''".toRegex(), "<b>$1</b>")
                    .replace("\\[vk(ontakte)?://[A-Za-z0-9/?=]+\\|([^]]+)]".toRegex(), "$2")
            }
            dto.time = optLong(root_item, "date")
            dto.iconURL = optString(root_item, "icon_url")
            val attachments: MutableList<Attachment> = ArrayList()
            if (hasObject(root_item, "main_item")) {
                val main_item = root_item["main_item"].asJsonObject
                if (hasArray(main_item, "image_object")) {
                    val jsonPhotos2 = main_item["image_object"].asJsonArray
                    dto.iconURL = jsonPhotos2[jsonPhotos2.size() - 1].asJsonObject["url"].asString
                }
                if ("photo" == optString(main_item, "type")) {
                    attachments.add(context.deserialize(main_item, Attachment::class.java))
                }
            }
            if (hasObject(root_item, "additional_item")) {
                val additional_item = root_item["additional_item"].asJsonObject
                if (hasArray(additional_item, "image_object")) {
                    val arrt = additional_item.getAsJsonArray("image_object")
                    dto.images = ArrayList()
                    for (s in arrt) {
                        if (!checkObject(s)) {
                            continue
                        }
                        val imgh: ImageAdditional =
                            context.deserialize(s, ImageAdditional::class.java)
                        dto.images?.add(imgh)
                    }
                }
                if ("photo" == optString(additional_item, "type")) {
                    attachments.add(context.deserialize(additional_item, Attachment::class.java))
                }
            }
            if (hasArray(root_item, "attachments")) {
                val temp = root_item.getAsJsonArray("attachments")
                for (a in temp) {
                    if (!checkObject(a)) {
                        continue
                    }
                    attachments.add(context.deserialize(a, Attachment::class.java))
                }
            }
            for (s in attachments) {
                if (s.type.isNullOrEmpty() || s.object_id.isNullOrEmpty() || s.type != "photo") {
                    continue
                }
                for (v in photos) {
                    if (v.owner_id.toString() + "_" + v.id == s.object_id) {
                        if (dto.attachments == null) {
                            dto.attachments = ArrayList()
                        }
                        dto.attachments?.add(Dto2Model.transform(v))
                        break
                    }
                }
            }
            dtolist.items?.add(dto)
        }
        return dtolist
    }

    companion object {
        private val TAG = AnswerVKOfficialDtoAdapter::class.java.simpleName
    }
}