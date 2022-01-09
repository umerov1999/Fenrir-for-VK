package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Commented implements Parcelable {

    public static final Creator<Commented> CREATOR = new Creator<Commented>() {
        public Commented createFromParcel(Parcel source) {
            return new Commented(source);
        }

        public Commented[] newArray(int size) {
            return new Commented[size];
        }
    };
    private final int sourceId;
    private final int sourceOwnerId;
    @CommentedType
    private final int sourceType;
    private String accessKey;

    public Commented(int sourceId, int sourceOwnerId, @CommentedType int sourceType, String accessKey) {
        this.sourceId = sourceId;
        this.sourceOwnerId = sourceOwnerId;
        this.sourceType = sourceType;
        this.accessKey = accessKey;
    }

    public Commented(Parcel in) {
        sourceId = in.readInt();
        sourceOwnerId = in.readInt();
        //noinspection ResourceType
        sourceType = in.readInt();
        accessKey = in.readString();
    }

    public static Commented from(@NonNull AbsModel model) {
        if (model instanceof Post) {
            return new Commented(((Post) model).getVkid(), ((Post) model).getOwnerId(), CommentedType.POST, null);
        } else if (model instanceof Photo) {
            return new Commented(((Photo) model).getId(), ((Photo) model).getOwnerId(), CommentedType.PHOTO, ((Photo) model).getAccessKey());
        } else if (model instanceof Video) {
            return new Commented(((Video) model).getId(), ((Video) model).getOwnerId(), CommentedType.VIDEO, ((Video) model).getAccessKey());
        } else if (model instanceof Topic) {
            return new Commented(((Topic) model).getId(), ((Topic) model).getOwnerId(), CommentedType.TOPIC, null);
        } else {
            throw new IllegalArgumentException("Invalid model, class: " + model.getClass());
        }
    }

    @CommentedType
    public int getSourceType() {
        return sourceType;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getSourceOwnerId() {
        return sourceOwnerId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public Commented setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commented commented = (Commented) o;
        return sourceId == commented.sourceId
                && sourceOwnerId == commented.sourceOwnerId
                && sourceType == commented.sourceType;

    }

    /**
     * Тип комментируемого обьекта
     * Используется в процедуре http://vk.com/dev/execute.getComments
     */
    public String getTypeForStoredProcedure() {
        switch (sourceType) {
            case CommentedType.POST:
                return "post";
            case CommentedType.PHOTO:
                return "photo";
            case CommentedType.VIDEO:
                return "video";
            case CommentedType.TOPIC:
                return "topic";
            default:
                throw new IllegalArgumentException("Unknown source type: " + sourceType);
        }
    }

    @Override
    public int hashCode() {
        int result = sourceId;
        result = 31 * result + sourceOwnerId;
        result = 31 * result + sourceType;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "Commented{" +
                "sourceId=" + sourceId +
                ", sourceOwnerId=" + sourceOwnerId +
                ", sourceType=" + sourceType +
                ", accessKey='" + accessKey + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sourceId);
        dest.writeInt(sourceOwnerId);
        dest.writeInt(sourceType);
        dest.writeString(accessKey);
    }
}
