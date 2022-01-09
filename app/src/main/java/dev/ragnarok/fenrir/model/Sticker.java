package dev.ragnarok.fenrir.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;


public class Sticker extends AbsModel {

    public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };
    private final int id;
    private List<Image> images;
    private List<Image> imagesWithBackground;
    private String animationUrl;
    private List<Animation> animations;

    public Sticker(int id) {
        this.id = id;
    }

    protected Sticker(Parcel in) {
        super(in);
        id = in.readInt();
        images = in.createTypedArrayList(Image.CREATOR);
        imagesWithBackground = in.createTypedArrayList(Image.CREATOR);
        animations = in.createTypedArrayList(Animation.CREATOR);
        animationUrl = in.readString();
    }

    public Image getImage(int prefSize, boolean isNight) {
        return isNight ? getImage(prefSize, imagesWithBackground) : getImage(prefSize, images);
    }

    public Image getImage(int prefSize, Context context) {
        return getImage(prefSize, Settings.get().ui().isStickers_by_theme() && Settings.get().ui().isDarkModeEnabled(context));
    }

    public Image getImageLight(int prefSize) {
        return getImage(prefSize, images);
    }

    private Image getImage(int prefSize, List<Image> images) {
        if (Utils.isEmpty(images)) {
            return new Image(null, 256, 256);
        }
        Image result = null;

        for (Image image : images) {
            if (result == null) {
                result = image;
                continue;
            }

            if (Math.abs(image.calcAverageSize() - prefSize) < Math.abs(result.calcAverageSize() - prefSize)) {
                result = image;
            }
        }

        if (result == null) {
            // default
            return images.get(0);
        }

        return result;
    }

    public String getAnimationUrl() {
        return animationUrl;
    }

    public Sticker setAnimationUrl(String animationUrl) {
        this.animationUrl = animationUrl;
        return this;
    }

    public String getAnimationByType(String type) {
        if (Utils.isEmpty(animations)) {
            return animationUrl;
        }
        for (Animation i : animations) {
            if (i.type.equals(type)) {
                return i.url;
            }
        }
        return animationUrl;
    }

    public String getAnimationByDayNight(Context context) {
        if (!Settings.get().ui().isStickers_by_theme()) {
            return getAnimationByType("light");
        }
        return getAnimationByType(Settings.get().ui().isDarkModeEnabled(context) ? "dark" : "light");
    }

    public boolean isAnimated() {
        if (Utils.isEmpty(animations)) {
            return animationUrl != null && !animationUrl.isEmpty();
        }
        return true;
    }

    public List<Image> getImages() {
        return images;
    }

    public Sticker setImages(List<Image> images) {
        this.images = images;
        return this;
    }

    public List<Animation> getAnimations() {
        return animations;
    }

    public Sticker setAnimations(List<Animation> animations) {
        this.animations = animations;
        return this;
    }

    public List<Image> getImagesWithBackground() {
        return imagesWithBackground;
    }

    public Sticker setImagesWithBackground(List<Image> imagesWithBackground) {
        this.imagesWithBackground = imagesWithBackground;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeTypedList(images);
        dest.writeTypedList(imagesWithBackground);
        dest.writeTypedList(animations);
        dest.writeString(animationUrl);
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final class Image implements Parcelable {

        public static final Creator<Image> CREATOR = new Creator<Image>() {
            @Override
            public Image createFromParcel(Parcel in) {
                return new Image(in);
            }

            @Override
            public Image[] newArray(int size) {
                return new Image[size];
            }
        };
        private final String url;
        private final int width;
        private final int height;

        public Image(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        Image(Parcel in) {
            url = in.readString();
            width = in.readInt();
            height = in.readInt();
        }

        public String getUrl() {
            return url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
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

        private int calcAverageSize() {
            return (width + height) / 2;
        }
    }

    public static final class Animation implements Parcelable {

        public static final Creator<Animation> CREATOR = new Creator<Animation>() {
            @Override
            public Animation createFromParcel(Parcel in) {
                return new Animation(in);
            }

            @Override
            public Animation[] newArray(int size) {
                return new Animation[size];
            }
        };
        private final String url;
        private final String type;

        public Animation(String url, String type) {
            this.url = url;
            this.type = type;
        }

        Animation(Parcel in) {
            url = in.readString();
            type = in.readString();
        }

        public String getUrl() {
            return url;
        }

        public String getType() {
            return type;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeString(type);
        }
    }

    public static final class LocalSticker {
        private final String path;
        private final boolean isAnimated;

        public LocalSticker(@NonNull String path, boolean isAnimated) {
            this.path = path;
            this.isAnimated = isAnimated;
        }

        public @NonNull
        String getPath() {
            return path;
        }

        public @NonNull
        String getPreviewPath() {
            return "file://" + path;
        }

        public boolean isAnimated() {
            return isAnimated;
        }

        public @NonNull
        String getAnimationName() {
            return new File(path).getName().replace(".json", ".lottie");
        }
    }
}