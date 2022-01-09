package dev.ragnarok.fenrir.model;

import android.os.Parcel;

public class Event extends AbsModel {

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    private final int id;
    private String button_text;
    private String text;
    private Owner subject;

    public Event(int id) {
        this.id = id;
    }

    protected Event(Parcel in) {
        super(in);
        id = in.readInt();
        button_text = in.readString();
        text = in.readString();
        subject = in.readParcelable(id > 0 ?
                User.class.getClassLoader() : Community.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeString(button_text);
        dest.writeString(text);
        dest.writeParcelable(subject, flags);
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Event setText(String text) {
        this.text = text;
        return this;
    }

    public String getButton_text() {
        return button_text;
    }

    public Event setButton_text(String button_text) {
        this.button_text = button_text;
        return this;
    }

    public Owner getSubject() {
        return subject;
    }

    public Event setSubject(Owner subject) {
        this.subject = subject;
        return this;
    }

    public String getSubjectPhoto() {
        return subject == null ? null : subject.getMaxSquareAvatar();
    }

    public String getSubjectName() {
        return subject == null ? null : subject.getFullName();
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
