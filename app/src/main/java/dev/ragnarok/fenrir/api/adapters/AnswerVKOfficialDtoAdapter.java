package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.model.AnswerVKOfficial;
import dev.ragnarok.fenrir.model.AnswerVKOfficialList;
import dev.ragnarok.fenrir.util.Utils;

public class AnswerVKOfficialDtoAdapter extends AbsAdapter implements JsonDeserializer<AnswerVKOfficialList> {
    private static final String TAG = AnswerVKOfficialDtoAdapter.class.getSimpleName();

    @Override
    public AnswerVKOfficialList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            throw new JsonParseException(TAG + " error parse object");
        }
        AnswerVKOfficialList dtolist = new AnswerVKOfficialList();
        JsonObject root = json.getAsJsonObject();

        dtolist.items = new ArrayList<>();
        dtolist.fields = new ArrayList<>();
        List<VKApiPhoto> photos = new ArrayList<>();

        if (hasArray(root, "photos")) {
            JsonArray temp = root.getAsJsonArray("photos");
            for (JsonElement i : temp) {
                if (!checkObject(i)) {
                    continue;
                }
                photos.add(context.deserialize(i, VKApiPhoto.class));
            }
        }

        if (hasArray(root, "profiles")) {
            JsonArray temp = root.getAsJsonArray("profiles");
            for (JsonElement i : temp) {
                if (!checkObject(i)) {
                    continue;
                }
                JsonObject obj = i.getAsJsonObject();
                int id = optInt(obj, "id");
                if (obj.has("photo_200")) {
                    String url = optString(obj, "photo_200");
                    dtolist.fields.add(new AnswerVKOfficialList.AnswerField(id, url));
                } else if (obj.has("photo_200_orig")) {
                    String url = optString(obj, "photo_200_orig");
                    dtolist.fields.add(new AnswerVKOfficialList.AnswerField(id, url));
                }
            }
        }
        if (hasArray(root, "groups")) {
            JsonArray temp = root.getAsJsonArray("groups");
            for (JsonElement i : temp) {
                if (!checkObject(i)) {
                    continue;
                }
                JsonObject obj = i.getAsJsonObject();
                int id = optInt(obj, "id") * -1;
                if (obj.has("photo_200")) {
                    String url = optString(obj, "photo_200");
                    dtolist.fields.add(new AnswerVKOfficialList.AnswerField(id, url));
                } else if (obj.has("photo_200_orig")) {
                    String url = optString(obj, "photo_200_orig");
                    dtolist.fields.add(new AnswerVKOfficialList.AnswerField(id, url));
                }
            }
        }

        if (!hasArray(root, "items"))
            return dtolist;

        for (JsonElement i : root.getAsJsonArray("items")) {
            if (!checkObject(i)) {
                continue;
            }
            JsonObject root_item = i.getAsJsonObject();
            AnswerVKOfficial dto = new AnswerVKOfficial();

            if (hasObject(root_item, "action")) {
                JsonObject action_item = root_item.get("action").getAsJsonObject();
                if ("authorize".equals(optString(action_item, "type"))) {
                    dto.action = new AnswerVKOfficial.ActionURL(optString(action_item, "url"));
                } else if ("message_open".equals(optString(action_item, "type")) && hasObject(action_item, "context")) {
                    JsonObject context_item = action_item.get("context").getAsJsonObject();
                    dto.action = new AnswerVKOfficial.ActionMessage(optInt(context_item, "peer_id", 0), optInt(context_item, "id", 0));
                }
            }

            dto.iconType = optString(root_item, "icon_type");
            dto.header = optString(root_item, "header");
            if (dto.header != null) {
                dto.header = dto.header.replace("{date}", "").replaceAll("'''(((?!''').)*)'''", "<b>$1</b>").replaceAll("\\[vk(ontakte)?://[A-Za-z0-9/?=]+\\|([^]]+)]", "$2");
            }
            dto.text = optString(root_item, "text");
            if (dto.text != null)
                dto.text = dto.text.replace("{date}", "").replaceAll("'''(((?!''').)*)'''", "<b>$1</b>").replaceAll("\\[vk(ontakte)?://[A-Za-z0-9/?=]+\\|([^]]+)]", "$2");
            dto.footer = optString(root_item, "footer");
            if (dto.footer != null)
                dto.footer = dto.footer.replace("{date}", "").replaceAll("'''(((?!''').)*)'''", "<b>$1</b>").replaceAll("\\[vk(ontakte)?://[A-Za-z0-9/?=]+\\|([^]]+)]", "$2");
            dto.time = optLong(root_item, "date");
            dto.iconURL = optString(root_item, "icon_url");

            List<AnswerVKOfficial.Attachment> attachments = new ArrayList<>();
            if (hasObject(root_item, "main_item")) {
                JsonObject main_item = root_item.get("main_item").getAsJsonObject();
                if (hasArray(main_item, "image_object")) {
                    JsonArray jsonPhotos2 = main_item.get("image_object").getAsJsonArray();
                    dto.iconURL = jsonPhotos2.get(jsonPhotos2.size() - 1).getAsJsonObject().get("url").getAsString();
                }
                if ("photo".equals(optString(main_item, "type"))) {
                    attachments.add(context.deserialize(main_item, AnswerVKOfficial.Attachment.class));
                }
            }
            if (hasObject(root_item, "additional_item")) {
                JsonObject additional_item = root_item.get("additional_item").getAsJsonObject();
                if (hasArray(additional_item, "image_object")) {
                    JsonArray arrt = additional_item.getAsJsonArray("image_object");
                    dto.images = new ArrayList<>();
                    for (JsonElement s : arrt) {
                        if (!checkObject(s)) {
                            continue;
                        }
                        AnswerVKOfficial.ImageAdditional imgh = context.deserialize(s, AnswerVKOfficial.ImageAdditional.class);
                        if (imgh != null)
                            dto.images.add(imgh);
                    }
                }
                if ("photo".equals(optString(additional_item, "type"))) {
                    attachments.add(context.deserialize(additional_item, AnswerVKOfficial.Attachment.class));
                }
            }
            if (hasArray(root_item, "attachments")) {
                JsonArray temp = root_item.getAsJsonArray("attachments");
                for (JsonElement a : temp) {
                    if (!checkObject(a)) {
                        continue;
                    }
                    attachments.add(context.deserialize(a, AnswerVKOfficial.Attachment.class));
                }
            }
            for (AnswerVKOfficial.Attachment s : attachments) {
                if (Utils.isEmpty(s.type) || Utils.isEmpty(s.object_id) || !s.type.equals("photo")) {
                    continue;
                }
                for (VKApiPhoto v : photos) {
                    if ((v.owner_id + "_" + v.id).equals(s.object_id)) {
                        if (dto.attachments == null) {
                            dto.attachments = new ArrayList<>();
                        }
                        dto.attachments.add(Dto2Model.transform(v));
                        break;
                    }
                }
            }
            dtolist.items.add(dto);
        }
        return dtolist;
    }
}
