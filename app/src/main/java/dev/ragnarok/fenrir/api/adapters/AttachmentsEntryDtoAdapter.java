package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.ragnarok.fenrir.api.model.VKApiAttachment;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;
import dev.ragnarok.fenrir.util.Objects;

public class AttachmentsEntryDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiAttachments.Entry> {

    @Override
    public VkApiAttachments.Entry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!checkObject(json)) {
            return null;
        }
        JsonObject o = json.getAsJsonObject();

        String type = optString(o, "type");
        VKApiAttachment attachment;
        try {
            attachment = AttachmentsDtoAdapter.parse(type, o, context);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        VkApiAttachments.Entry entry = null;
        if (Objects.nonNull(attachment)) {
            entry = new VkApiAttachments.Entry(type, attachment);
        }

        return entry;
    }
}
