package dev.ragnarok.fenrir.adapter;

import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.PhotoSizes;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.view.mozaik.PostImagePosition;

public class PostImage {

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_GIF = 3;

    private final int type;
    private final AbsModel attachment;
    private PostImagePosition position;

    public PostImage(AbsModel model, int type) {
        attachment = model;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public AbsModel getAttachment() {
        return attachment;
    }

    public PostImagePosition getPosition() {
        return position;
    }

    public PostImage setPosition(PostImagePosition position) {
        this.position = position;
        return this;
    }

    public int getWidth() {
        switch (type) {
            case TYPE_IMAGE:
                Photo photo = (Photo) attachment;
                return photo.getWidth() == 0 ? 100 : photo.getWidth();
            case TYPE_VIDEO:
                return 640;
            case TYPE_GIF:
                Document document = (Document) attachment;
                PhotoSizes.Size max = document.getMaxPreviewSize(false);
                return max == null ? 640 : max.getW();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public String getPreviewUrl(@PhotoSize int photoPreviewSize) {
        switch (type) {
            case TYPE_IMAGE:
                Photo photo = (Photo) attachment;
                PhotoSizes.Size size = photo.getSizes().getSize(photoPreviewSize, true);
                return size == null ? null : size.getUrl();
            case TYPE_VIDEO:
                Video video = (Video) attachment;
                return video.getImage();
            case TYPE_GIF:
                Document document = (Document) attachment;
                return document.getPreviewWithSize(PhotoSize.Q, false);
        }

        throw new UnsupportedOperationException();
    }

    public int getHeight() {
        switch (type) {
            case TYPE_IMAGE:
                Photo photo = (Photo) attachment;
                return photo.getHeight() == 0 ? 100 : photo.getHeight();
            case TYPE_VIDEO:
                return 360;
            case TYPE_GIF:
                Document document = (Document) attachment;
                PhotoSizes.Size max = document.getMaxPreviewSize(false);
                return max == null ? 480 : max.getH();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public float getAspectRatio() {
        return (float) getWidth() / (float) getHeight();
    }
}