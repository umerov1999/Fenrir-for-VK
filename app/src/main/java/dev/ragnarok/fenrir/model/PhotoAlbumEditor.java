package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PhotoAlbumEditor implements Parcelable {

    public static final Creator<PhotoAlbumEditor> CREATOR = new Creator<PhotoAlbumEditor>() {
        @Override
        public PhotoAlbumEditor createFromParcel(Parcel in) {
            return new PhotoAlbumEditor(in);
        }

        @Override
        public PhotoAlbumEditor[] newArray(int size) {
            return new PhotoAlbumEditor[size];
        }
    };
    private String title;
    private String description;
    private Privacy privacyView;
    private Privacy privacyComment;
    private boolean commentsDisabled;
    private boolean uploadByAdminsOnly;

    private PhotoAlbumEditor() {

    }

    protected PhotoAlbumEditor(Parcel in) {
        title = in.readString();
        description = in.readString();
        privacyView = in.readParcelable(Privacy.class.getClassLoader());
        privacyComment = in.readParcelable(Privacy.class.getClassLoader());
        commentsDisabled = in.readByte() != 0;
        uploadByAdminsOnly = in.readByte() != 0;
    }

    public static PhotoAlbumEditor create() {
        PhotoAlbumEditor editor = new PhotoAlbumEditor();
        editor.setPrivacyComment(new Privacy());
        editor.setPrivacyView(new Privacy());
        return editor;
    }

    public String getTitle() {
        return title;
    }

    public PhotoAlbumEditor setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PhotoAlbumEditor setDescription(String description) {
        this.description = description;
        return this;
    }

    public Privacy getPrivacyView() {
        return privacyView;
    }

    public PhotoAlbumEditor setPrivacyView(Privacy privacyView) {
        this.privacyView = privacyView;
        return this;
    }

    public Privacy getPrivacyComment() {
        return privacyComment;
    }

    public PhotoAlbumEditor setPrivacyComment(Privacy privacyComment) {
        this.privacyComment = privacyComment;
        return this;
    }

    public boolean isCommentsDisabled() {
        return commentsDisabled;
    }

    public PhotoAlbumEditor setCommentsDisabled(boolean commentsDisabled) {
        this.commentsDisabled = commentsDisabled;
        return this;
    }

    public boolean isUploadByAdminsOnly() {
        return uploadByAdminsOnly;
    }

    public PhotoAlbumEditor setUploadByAdminsOnly(boolean uploadByAdminsOnly) {
        this.uploadByAdminsOnly = uploadByAdminsOnly;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeParcelable(privacyView, i);
        parcel.writeParcelable(privacyComment, i);
        parcel.writeByte((byte) (commentsDisabled ? 1 : 0));
        parcel.writeByte((byte) (uploadByAdminsOnly ? 1 : 0));
    }
}
