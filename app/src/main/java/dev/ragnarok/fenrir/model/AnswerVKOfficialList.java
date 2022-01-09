package dev.ragnarok.fenrir.model;

import java.util.List;

public class AnswerVKOfficialList {
    public List<AnswerVKOfficial> items;
    public List<AnswerField> fields;

    public String getAvatar(int id) {
        if (fields.isEmpty())
            return null;
        for (AnswerField i : fields) {
            if (i.id == id)
                return i.photo;
        }
        return null;
    }

    public static class AnswerField {
        public final int id;
        public final String photo;

        public AnswerField(int id, String photo) {
            this.id = id;
            this.photo = photo;
        }
    }
}
