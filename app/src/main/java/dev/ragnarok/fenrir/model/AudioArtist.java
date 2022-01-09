package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.util.Utils;

public class AudioArtist extends AbsModel {
    public static final Creator<AudioArtist> CREATOR = new Creator<AudioArtist>() {
        @Override
        public AudioArtist createFromParcel(Parcel in) {
            return new AudioArtist(in);
        }

        @Override
        public AudioArtist[] newArray(int size) {
            return new AudioArtist[size];
        }
    };

    private final String id;
    private String name;
    private List<AudioArtistImage> photo;

    public AudioArtist(String id) {
        this.id = id;
    }

    protected AudioArtist(Parcel in) {
        super(in);
        id = in.readString();
        name = in.readString();
        photo = in.createTypedArrayList(AudioArtistImage.CREATOR);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AudioArtist setName(String name) {
        this.name = name;
        return this;
    }

    public List<AudioArtistImage> getPhoto() {
        return photo;
    }

    public AudioArtist setPhoto(List<AudioArtistImage> photo) {
        this.photo = photo;
        return this;
    }

    public @Nullable
    String getMaxPhoto() {
        if (Utils.isEmpty(photo)) {
            return null;
        }
        int size = 0;
        String url = photo.get(0).url;
        for (AudioArtistImage i : photo) {
            if (i.width * i.height > size) {
                size = i.width * i.height;
                url = i.url;
            }
        }
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeTypedList(photo);
    }

    public static final class AudioArtistImage implements Parcelable {

        public static final Creator<AudioArtistImage> CREATOR = new Creator<AudioArtistImage>() {
            @Override
            public AudioArtistImage createFromParcel(Parcel in) {
                return new AudioArtistImage(in);
            }

            @Override
            public AudioArtistImage[] newArray(int size) {
                return new AudioArtistImage[size];
            }
        };

        private final String url;
        private final int width;
        private final int height;

        public AudioArtistImage(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        AudioArtistImage(Parcel in) {
            url = in.readString();
            width = in.readInt();
            height = in.readInt();
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeInt(width);
            dest.writeInt(height);
        }
    }
}
