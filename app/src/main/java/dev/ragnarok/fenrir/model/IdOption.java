package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.api.model.Identificable;

public final class IdOption implements Parcelable, Identificable {

    public static final Creator<IdOption> CREATOR = new Creator<IdOption>() {
        @Override
        public IdOption createFromParcel(Parcel in) {
            return new IdOption(in);
        }

        @Override
        public IdOption[] newArray(int size) {
            return new IdOption[size];
        }
    };
    private final int id;
    private final String title;
    private final List<IdOption> childs;

    public IdOption(int id, String title, List<IdOption> childs) {
        this.id = id;
        this.title = title;
        this.childs = childs;
    }

    public IdOption(int id, String title) {
        this(id, title, Collections.emptyList());
    }

    private IdOption(Parcel in) {
        id = in.readInt();
        title = in.readString();
        childs = in.createTypedArrayList(CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeTypedList(childs);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<IdOption> getChilds() {
        return childs;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}