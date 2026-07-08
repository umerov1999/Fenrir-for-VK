package dev.ragnarok.fenrir.api.adapters

import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiPhoto
import dev.ragnarok.fenrir.api.model.VKApiUser
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.kJson
import dev.ragnarok.fenrir.link.internal.FeedbackLinkSpanFactory
import dev.ragnarok.fenrir.model.FeedbackVKOfficial
import dev.ragnarok.fenrir.model.FeedbackVKOfficial.ActionBrowserURL
import dev.ragnarok.fenrir.model.FeedbackVKOfficial.ActionMessage
import dev.ragnarok.fenrir.model.FeedbackVKOfficial.ActionURL
import dev.ragnarok.fenrir.model.FeedbackVKOfficial.Attachment
import dev.ragnarok.fenrir.model.FeedbackVKOfficial.ImageAdditional
import dev.ragnarok.fenrir.model.FeedbackVKOfficialList
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.util.Utils
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElementOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class FeedbackVKOfficialDtoAdapter :
    AbsDtoAdapter<FeedbackVKOfficialList>("FeedbackVKOfficialList") {
    @Throws(Exception::class)
    override fun deserialize(
        json: JsonElement
    ): FeedbackVKOfficialList {
        if (!checkObject(json)) {
            throw Exception("$TAG error parse object")
        }
        val dtoList = FeedbackVKOfficialList()
        val root = json.jsonObject
        dtoList.items = ArrayList()

        val profiles: List<VKApiUser> =
            if (hasArray(root, "profiles")) kJson.decodeFromJsonElementOrNull(
                ListSerializer(VKApiUser.serializer()), root["profiles"]
            ).orEmpty() else emptyList()
        val groups: List<VKApiCommunity> =
            if (hasArray(root, "groups")) kJson.decodeFromJsonElementOrNull(
                ListSerializer(VKApiCommunity.serializer()), root["groups"]
            ).orEmpty() else emptyList()

        val photos: MutableList<VKApiPhoto> = ArrayList()
        if (hasArray(root, "photos")) {
            val temp = root["photos"]?.jsonArray
            for (i in temp.orEmpty()) {
                if (!checkObject(i)) {
                    continue
                }
                photos.add(kJson.decodeFromJsonElement(VKApiPhoto.serializer(), i))
            }
        }
        if (!hasArray(root, "items")) return dtoList
        for (i in root["items"]?.jsonArray.orEmpty()) {
            if (!checkObject(i)) {
                continue
            }
            val rootItem = i.jsonObject
            val dto = FeedbackVKOfficial()
            if (hasObject(rootItem, "action")) {
                val actionItem = rootItem["action"]?.jsonObject
                if ("authorize" == optString(actionItem, "type")) {
                    dto.action = ActionBrowserURL(optString(actionItem, "url"))
                } else if ("custom" == optString(
                        actionItem,
                        "type"
                    ) && optString(
                        rootItem,
                        "icon_type"
                    ) == "friend_found" && optString(
                        actionItem,
                        "url"
                    )?.contains("friends_swipe") != true
                ) {
                    dto.action = ActionURL(optString(actionItem, "url"))
                } else if ("custom" == optString(
                        actionItem,
                        "type"
                    ) && (optString(
                        actionItem,
                        "url"
                    )?.contains("/story") == true || optString(
                        actionItem,
                        "url"
                    )?.contains("/wall") == true || optString(
                        actionItem,
                        "url"
                    )?.contains("/music") == true)
                ) {
                    dto.action = ActionURL(optString(actionItem, "url"))
                } else if ("message_open" == optString(
                        actionItem,
                        "type"
                    ) && hasObject(actionItem, "context")
                ) {
                    val contextItem = actionItem["context"]?.jsonObject
                    dto.action = ActionMessage(
                        optLong(contextItem, "peer_id", 0),
                        optInt(contextItem, "id", 0)
                    )
                }
            }
            try {
                if (hasObject(rootItem, "action_buttons")) {
                    val actionButtons = rootItem["action_buttons"]?.jsonObject
                    for (ss1 in actionButtons.orEmpty().keys) {
                        if (checkArray(actionButtons?.get(ss1))) {
                            for (ss2 in actionButtons[ss1]?.jsonArray.orEmpty()) {
                                if (checkObject(ss2)) {
                                    val act = ss2.jsonObject
                                    if (hasObject(act, "action")) {
                                        val action = act["action"]?.jsonObject
                                        if ("hide_item" == optString(
                                                action,
                                                "type"
                                            ) && hasObject(action, "context")
                                        ) {
                                            val actionContext = action["context"]?.jsonObject
                                            dto.hideQuery = optString(actionContext, "query")
                                            if (dto.hideQuery.nonNullNoEmpty()) {
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
            dto.iconType = optString(rootItem, "icon_type")
            dto.header = optString(rootItem, "header")
            dto.header.nonNullNoEmpty {
                dto.header = it.replace("{date}", "")
                    .replace("'''(((?!''').)*)'''".toRegex(), "<b>$1</b>")
                    .replace("\\[vk(ontakte)?://[A-Za-z\\d/?=]+\\|([^]]+)]".toRegex(), "$2")

                try {
                    val matcher = FeedbackLinkSpanFactory.MENTIONS_AVATAR_PATTERN.find(it)
                    matcher?.let { sm ->
                        val type = sm.groupValues.getOrNull(1)
                        sm.groupValues.getOrNull(2)?.toLong()?.let { lit ->
                            dto.headerOwnerId =
                                if (type == "event" || type == "club" || type == "public") -lit else lit
                            if (dto.headerOwnerId.orZero() >= 0) {
                                for (n in profiles) {
                                    if (n.id == dto.headerOwnerId) {
                                        dto.headerOwnerAvatarUrl =
                                            Utils.firstNonEmptyString(n.photo_200, n.photo_100)
                                        break
                                    }
                                }
                            } else {
                                for (n in groups) {
                                    if (-n.id == dto.headerOwnerId) {
                                        dto.headerOwnerAvatarUrl =
                                            Utils.firstNonEmptyString(n.photo_200, n.photo_100)
                                        break
                                    }
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }

            dto.text = optString(rootItem, "text")
            dto.text.nonNullNoEmpty {
                dto.text =
                    it.replace("{date}", "")
                        .replace("'''(((?!''').)*)'''".toRegex(), "<b>$1</b>")
                        .replace("\\[vk(ontakte)?://[A-Za-z\\d/?=]+\\|([^]]+)]".toRegex(), "$2")
            }
            dto.footer = optString(rootItem, "footer")
            dto.footer.nonNullNoEmpty {
                dto.footer = it.replace("{date}", "")
                    .replace("'''(((?!''').)*)'''".toRegex(), "<b>$1</b>")
                    .replace("\\[vk(ontakte)?://[A-Za-z\\d/?=]+\\|([^]]+)]".toRegex(), "$2")
            }
            dto.time = optLong(rootItem, "date")
            dto.iconURL = optString(rootItem, "icon_url")
            val attachments: MutableList<Attachment> = ArrayList()
            if (hasObject(rootItem, "main_item")) {
                val mainItem = rootItem["main_item"]?.jsonObject
                if (hasArray(mainItem, "image_object")) {
                    val jsonPhotos2 = mainItem["image_object"]?.jsonArray
                    dto.iconURL =
                        jsonPhotos2?.get(jsonPhotos2.size - 1)?.asJsonObjectSafe?.get("url")?.asPrimitiveSafe?.content
                }
                if ("photo" == optString(mainItem, "type")) {
                    attachments.add(
                        kJson.decodeFromJsonElement(
                            Attachment.serializer(),
                            mainItem!!
                        )
                    )
                }
            }
            if (hasObject(rootItem, "additional_item")) {
                val additionalItem = rootItem["additional_item"]?.jsonObject
                if (hasArray(additionalItem, "image_object")) {
                    val array = additionalItem["image_object"]?.jsonArray
                    dto.images = ArrayList()
                    for (s in array.orEmpty()) {
                        if (!checkObject(s)) {
                            continue
                        }
                        val img: ImageAdditional =
                            kJson.decodeFromJsonElement(ImageAdditional.serializer(), s)
                        dto.images?.add(img)
                    }

                    if (hasObject(additionalItem, "action")) {
                        val actionItem = additionalItem["action"]?.jsonObject
                        if ("custom" == optString(actionItem, "type")) {
                            dto.imagesAction = ActionURL(optString(actionItem, "url"))
                        }
                    }
                }
                if ("photo" == optString(additionalItem, "type")) {
                    attachments.add(
                        kJson.decodeFromJsonElement(
                            Attachment.serializer(),
                            additionalItem!!
                        )
                    )
                }
            }
            if (hasArray(rootItem, "attachments")) {
                val temp = rootItem["attachments"]?.jsonArray
                for (a in temp.orEmpty()) {
                    if (!checkObject(a)) {
                        continue
                    }
                    attachments.add(kJson.decodeFromJsonElement(Attachment.serializer(), a))
                }
            }
            for (s in attachments) {
                if (s.type.isNullOrEmpty() || s.objectId.isNullOrEmpty() || s.type != "photo") {
                    continue
                }
                for (v in photos) {
                    if (v.owner_id.toString() + "_" + v.id == s.objectId) {
                        if (dto.attachments == null) {
                            dto.attachments = ArrayList()
                        }
                        dto.attachments?.add(Dto2Model.transform(v))
                        break
                    }
                }
            }
            dtoList.items?.add(dto)
        }
        return dtoList
    }

    companion object {
        private val TAG = FeedbackVKOfficialDtoAdapter::class.simpleName.orEmpty()
    }
}