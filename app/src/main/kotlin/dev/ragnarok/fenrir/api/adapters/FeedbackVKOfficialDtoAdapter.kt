package dev.ragnarok.fenrir.api.adapters

import dev.ragnarok.fenrir.api.model.VKApiPhoto
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.kJson
import dev.ragnarok.fenrir.model.FeedbackVKOfficial
import dev.ragnarok.fenrir.model.FeedbackVKOfficial.*
import dev.ragnarok.fenrir.model.FeedbackVKOfficialList
import dev.ragnarok.fenrir.model.FeedbackVKOfficialList.AnswerField
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.serializeble.json.*

class FeedbackVKOfficialDtoAdapter : AbsAdapter<FeedbackVKOfficialList>("FeedbackVKOfficialList") {
    @Throws(Exception::class)
    override fun deserialize(
        json: JsonElement
    ): FeedbackVKOfficialList {
        if (!checkObject(json)) {
            throw Exception("$TAG error parse object")
        }
        val dtolist = FeedbackVKOfficialList()
        val root = json.jsonObject
        dtolist.items = ArrayList()
        dtolist.fields = ArrayList()
        val photos: MutableList<VKApiPhoto> = ArrayList()
        if (hasArray(root, "photos")) {
            val temp = root["photos"]?.jsonArray
            for (i in temp.orEmpty()) {
                if (!checkObject(i)) {
                    continue
                }
                photos.add(kJson.decodeFromJsonElement(i))
            }
        }
        if (hasArray(root, "profiles")) {
            val temp = root["profiles"]?.jsonArray
            for (i in temp.orEmpty()) {
                if (!checkObject(i)) {
                    continue
                }
                val obj = i.jsonObject
                val id = optInt(obj, "id")
                if (obj.containsKey("photo_200")) {
                    val url = optString(obj, "photo_200")
                    url?.let { AnswerField(id, it) }?.let { dtolist.fields?.add(it) }
                } else if (obj.containsKey("photo_200_orig")) {
                    val url = optString(obj, "photo_200_orig")
                    url?.let { AnswerField(id, it) }?.let { dtolist.fields?.add(it) }
                }
            }
        }
        if (hasArray(root, "groups")) {
            val temp = root["groups"]?.jsonArray
            for (i in temp.orEmpty()) {
                if (!checkObject(i)) {
                    continue
                }
                val obj = i.jsonObject
                val id = optInt(obj, "id") * -1
                if (obj.containsKey("photo_200")) {
                    val url = optString(obj, "photo_200")
                    url?.let { AnswerField(id, it) }?.let { dtolist.fields?.add(it) }
                } else if (obj.containsKey("photo_200_orig")) {
                    val url = optString(obj, "photo_200_orig")
                    url?.let { AnswerField(id, it) }?.let { dtolist.fields?.add(it) }
                }
            }
        }
        if (!hasArray(root, "items")) return dtolist
        for (i in root["items"]?.jsonArray.orEmpty()) {
            if (!checkObject(i)) {
                continue
            }
            val root_item = i.jsonObject
            val dto = FeedbackVKOfficial()
            if (hasObject(root_item, "action")) {
                val action_item = root_item["action"]?.jsonObject
                if ("authorize" == optString(action_item, "type")) {
                    dto.action = ActionURL(optString(action_item, "url"))
                } else if ("message_open" == optString(
                        action_item,
                        "type"
                    ) && hasObject(action_item, "context")
                ) {
                    val context_item = action_item["context"]?.jsonObject
                    dto.action = ActionMessage(
                        optInt(context_item, "peer_id", 0),
                        optInt(context_item, "id", 0)
                    )
                }
            }
            try {
                if (hasObject(root_item, "action_buttons")) {
                    val action_buttons = root_item["action_buttons"]?.jsonObject
                    for (ss1 in action_buttons.orEmpty().keys) {
                        if (checkArray(action_buttons?.get(ss1))) {
                            for (ss2 in action_buttons?.get(ss1)?.jsonArray.orEmpty()) {
                                if (checkObject(ss2)) {
                                    val act = ss2.jsonObject
                                    if (hasObject(act, "action")) {
                                        val actu = act["action"]?.jsonObject
                                        if ("hide_item" == optString(
                                                actu,
                                                "type"
                                            ) && hasObject(actu, "context")
                                        ) {
                                            val actctx = actu["context"]?.jsonObject
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
                val main_item = root_item["main_item"]?.jsonObject
                if (hasArray(main_item, "image_object")) {
                    val jsonPhotos2 = main_item["image_object"]?.jsonArray
                    dto.iconURL =
                        jsonPhotos2?.get(jsonPhotos2.size - 1)?.jsonObject?.get("url")?.jsonPrimitive?.content
                }
                if ("photo" == optString(main_item, "type")) {
                    attachments.add(kJson.decodeFromJsonElement(main_item!!))
                }
            }
            if (hasObject(root_item, "additional_item")) {
                val additional_item = root_item["additional_item"]?.jsonObject
                if (hasArray(additional_item, "image_object")) {
                    val arrt = additional_item["image_object"]?.jsonArray
                    dto.images = ArrayList()
                    for (s in arrt.orEmpty()) {
                        if (!checkObject(s)) {
                            continue
                        }
                        val imgh: ImageAdditional =
                            kJson.decodeFromJsonElement(s)
                        dto.images?.add(imgh)
                    }
                }
                if ("photo" == optString(additional_item, "type")) {
                    attachments.add(kJson.decodeFromJsonElement(additional_item!!))
                }
            }
            if (hasArray(root_item, "attachments")) {
                val temp = root_item["attachments"]?.jsonArray
                for (a in temp.orEmpty()) {
                    if (!checkObject(a)) {
                        continue
                    }
                    attachments.add(kJson.decodeFromJsonElement(a))
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
        private val TAG = FeedbackVKOfficialDtoAdapter::class.java.simpleName
    }
}