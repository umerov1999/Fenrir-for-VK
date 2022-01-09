package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Keyboard implements Parcelable {

    public static final Creator<Keyboard> CREATOR = new Creator<Keyboard>() {
        @Override
        public Keyboard createFromParcel(Parcel in) {
            return new Keyboard(in);
        }

        @Override
        public Keyboard[] newArray(int size) {
            return new Keyboard[size];
        }
    };
    private boolean one_time;
    private boolean inline;
    private int author_id;
    private List<List<Button>> buttons;

    public Keyboard() {
    }

    protected Keyboard(Parcel in) {
        one_time = in.readByte() != 0;
        inline = in.readByte() != 0;
        author_id = in.readInt();

        int size = in.readInt();
        buttons = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            buttons.add(i, in.createTypedArrayList(Button.CREATOR));
        }
    }

    public boolean getOne_time() {
        return one_time;
    }

    public Keyboard setOne_time(boolean one_time) {
        this.one_time = one_time;
        return this;
    }

    public boolean getInline() {
        return inline;
    }

    public Keyboard setInline(boolean inline) {
        this.inline = inline;
        return this;
    }

    public int getAuthor_id() {
        return author_id;
    }

    public Keyboard setAuthor_id(int author_id) {
        this.author_id = author_id;
        return this;
    }

    public List<List<Button>> getButtons() {
        return buttons;
    }

    public Keyboard setButtons(List<List<Button>> buttons) {
        this.buttons = buttons;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (one_time ? 1 : 0));
        dest.writeByte((byte) (inline ? 1 : 0));
        dest.writeInt(author_id);

        dest.writeInt(buttons.size());
        for (List<Button> i : buttons) {
            dest.writeTypedList(i);
        }
    }

    public static class Button implements Parcelable {

        public static final Creator<Button> CREATOR = new Creator<Button>() {
            @Override
            public Button createFromParcel(Parcel in) {
                return new Button(in);
            }

            @Override
            public Button[] newArray(int size) {
                return new Button[size];
            }
        };
        private String color;
        private String type;
        private String label;
        private String link;
        private String payload;

        public Button() {
        }

        protected Button(Parcel in) {
            color = in.readString();
            type = in.readString();
            label = in.readString();
            link = in.readString();
            payload = in.readString();
        }

        public String getColor() {
            return color;
        }

        public Button setColor(String color) {
            this.color = color;
            return this;
        }

        public String getType() {
            return type;
        }

        public Button setType(String type) {
            this.type = type;
            return this;
        }

        public String getLabel() {
            return label;
        }

        public Button setLabel(String label) {
            this.label = label;
            return this;
        }

        public String getLink() {
            return link;
        }

        public Button setLink(String link) {
            this.link = link;
            return this;
        }

        public String getPayload() {
            return payload;
        }

        public Button setPayload(String payload) {
            this.payload = payload;
            return this;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(color);
            dest.writeString(type);
            dest.writeString(label);
            dest.writeString(link);
            dest.writeString(payload);
        }
    }
}
