package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.module.StringHash;

public class FileItem implements Parcelable {

    public static final Creator<FileItem> CREATOR = new Creator<FileItem>() {
        @Override
        public FileItem createFromParcel(Parcel in) {
            return new FileItem(in);
        }

        @Override
        public FileItem[] newArray(int size) {
            return new FileItem[size];
        }
    };
    private final boolean isDirectory;
    private final String file_name;
    private final String file_path;
    private final String parent_name;
    private final String parent_path;
    private final long modification;
    private final long size;

    public FileItem(boolean isDirectory, String file_name, String file_path, String parent_name, String parent_path, long modification, long size) {
        this.isDirectory = isDirectory;
        this.file_name = file_name;
        this.file_path = file_path;
        this.parent_name = parent_name;
        this.parent_path = parent_path;
        this.size = size;
        this.modification = modification;
    }

    protected FileItem(Parcel in) {
        isDirectory = in.readByte() != 0;
        file_name = in.readString();
        file_path = in.readString();
        parent_name = in.readString();
        parent_path = in.readString();
        modification = in.readLong();
        size = in.readLong();
    }


    public boolean getIsDir() {
        return isDirectory;
    }

    public String getFile_name() {
        return file_name;
    }

    public int getFileNameHash() {
        return StringHash.INSTANCE.calculateCRC32(file_name);
    }

    public int getFilePathHash() {
        return StringHash.INSTANCE.calculateCRC32(file_path);
    }

    public String getFile_path() {
        return file_path;
    }

    public String getParent_name() {
        return parent_name;
    }

    public String getParent_path() {
        return parent_path;
    }

    public long getModification() {
        return modification;
    }

    public long getSize() {
        return size;
    }

    @NonNull
    @Override
    public String toString() {
        return file_path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isDirectory ? 1 : 0));
        dest.writeString(file_name);
        dest.writeString(file_path);
        dest.writeString(parent_name);
        dest.writeString(parent_path);
        dest.writeLong(modification);
        dest.writeLong(size);
    }
}
