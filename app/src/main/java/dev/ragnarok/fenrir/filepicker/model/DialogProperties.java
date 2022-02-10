package dev.ragnarok.fenrir.filepicker.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.StringRes;

public class DialogProperties implements Parcelable {
    public static final Parcelable.Creator<DialogProperties> CREATOR = new Parcelable.Creator<DialogProperties>() {
        @Override
        public DialogProperties createFromParcel(Parcel in) {
            return new DialogProperties(in);
        }

        @Override
        public DialogProperties[] newArray(int size) {
            return new DialogProperties[size];
        }
    };
    public int selection_mode;
    public int selection_type;
    public String root;
    public String error_dir;
    public String offset;
    public String[] extensions;
    public boolean show_hidden_files;
    public @StringRes
    int tittle;
    public String request;

    public DialogProperties() {
        selection_mode = DialogConfigs.SINGLE_MODE;
        selection_type = DialogConfigs.FILE_SELECT;
        root = DialogConfigs.DEFAULT_DIR;
        error_dir = DialogConfigs.DEFAULT_DIR;
        offset = DialogConfigs.DEFAULT_DIR;
        extensions = null;
        show_hidden_files = false;
        tittle = -1;
        request = null;
    }

    public DialogProperties(Parcel in) {
        selection_mode = in.readInt();
        selection_type = in.readInt();
        root = in.readString();
        error_dir = in.readString();
        offset = in.readString();
        in.readStringArray(extensions);
        show_hidden_files = in.readByte() != 0;
        tittle = in.readInt();
        request = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(selection_mode);
        dest.writeInt(selection_type);
        dest.writeString(root);
        dest.writeString(error_dir);
        dest.writeString(offset);
        dest.writeStringArray(extensions);
        dest.writeByte((byte) (show_hidden_files ? 1 : 0));
        dest.writeInt(tittle);
        dest.writeString(request);
    }
}