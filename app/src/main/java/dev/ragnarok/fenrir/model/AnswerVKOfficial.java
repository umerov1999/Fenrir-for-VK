package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import dev.ragnarok.fenrir.util.Utils;

public class AnswerVKOfficial {
    public String footer;
    public String header;
    public String text;
    public String iconURL;
    public String iconType;
    public Long time;
    public List<ImageAdditional> images;
    public List<Photo> attachments;
    public Action action;

    public ImageAdditional getImage(int prefSize) {
        ImageAdditional result = null;
        if (Utils.isEmpty(images))
            return null;

        for (ImageAdditional image : images) {
            if (result == null) {
                result = image;
                continue;
            }
            if (Math.abs(image.calcAverageSize() - prefSize) < Math.abs(result.calcAverageSize() - prefSize)) {
                result = image;
            }
        }
        return result;
    }

    @IntDef({Action_Types.MESSAGE,
            Action_Types.URL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action_Types {
        int MESSAGE = 0;
        int URL = 1;
    }

    public static abstract class Action {
        public @Action_Types
        abstract int getType();
    }

    public static class ActionMessage extends Action {
        public int peerId;
        public int messageId;

        public ActionMessage(int peerId, int messageId) {
            this.messageId = messageId;
            this.peerId = peerId;
        }

        @Override
        public int getType() {
            return Action_Types.MESSAGE;
        }
    }

    public static class ActionURL extends Action {
        public String url;

        public ActionURL(String url) {
            this.url = url;
        }

        @Override
        public int getType() {
            return Action_Types.URL;
        }
    }

    public static final class ImageAdditional {
        @SerializedName("url")
        public String url;
        @SerializedName("width")
        public int width;
        @SerializedName("height")
        public int height;

        private int calcAverageSize() {
            return (width + height) / 2;
        }
    }

    public static final class Attachment {
        @SerializedName("type")
        public String type;
        @SerializedName("object_id")
        public String object_id;
    }
}