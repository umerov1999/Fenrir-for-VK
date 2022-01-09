package dev.ragnarok.fenrir.model;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.model.Identificable;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class WallReply extends AbsModel implements Identificable {

    public static final Creator<WallReply> CREATOR = new Creator<WallReply>() {
        @Override
        public WallReply createFromParcel(Parcel in) {
            return new WallReply(in);
        }

        @Override
        public WallReply[] newArray(int size) {
            return new WallReply[size];
        }
    };
    private int id;

    private int from_id;

    private int post_id;

    private Owner author;

    private int owner_id;

    private String text;

    private Attachments attachments;

    protected WallReply(Parcel in) {
        super(in);
        id = in.readInt();
        from_id = in.readInt();
        post_id = in.readInt();
        owner_id = in.readInt();
        text = in.readString();
        attachments = in.readParcelable(Attachments.class.getClassLoader());
        author = ParcelableOwnerWrapper.readOwner(in);
    }

    public WallReply() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(from_id);
        dest.writeInt(post_id);
        dest.writeInt(owner_id);
        dest.writeString(text);
        dest.writeParcelable(attachments, flags);
        ParcelableOwnerWrapper.writeOwner(dest, flags, author);
    }

    public int getAttachmentsCount() {
        return Objects.isNull(attachments) ? 0 : attachments.size();
    }

    public WallReply buildFromComment(@NonNull Comment comment, @Nullable Commented commented) {
        id = comment.getId();
        from_id = comment.getFromId();
        author = comment.getAuthor();
        text = comment.getText();
        if (commented != null) {
            owner_id = commented.getSourceOwnerId();
        } else if (comment.getCommented() != null) {
            owner_id = comment.getCommented().getSourceOwnerId();
        }
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    public boolean hasStickerOnly() {
        return attachments != null && !Utils.safeIsEmpty(attachments.getStickers());
    }

    public int getId() {
        return id;
    }

    public WallReply setId(int id) {
        this.id = id;
        return this;
    }

    public int getOwnerId() {
        return owner_id;
    }

    public WallReply setOwnerId(int owner_id) {
        this.owner_id = owner_id;
        return this;
    }

    public Owner getAuthor() {
        return author;
    }

    public WallReply setAuthor(Owner author) {
        this.author = author;
        return this;
    }

    public String getAuthorPhoto() {
        return author == null ? null : author.getMaxSquareAvatar();
    }

    public String getAuthorName() {
        return author == null ? null : author.getFullName();
    }

    public int getPostId() {
        return post_id;
    }

    public WallReply setPostId(int post_id) {
        this.post_id = post_id;
        return this;
    }

    public int getFromId() {
        return from_id;
    }

    public WallReply setFromId(int from_id) {
        this.from_id = from_id;
        return this;
    }

    public String getText() {
        return text;
    }

    public WallReply setText(String text) {
        this.text = text;
        return this;
    }

    public Attachments getAttachments() {
        return attachments;
    }

    public WallReply setAttachments(Attachments attachments) {
        this.attachments = attachments;
        return this;
    }
}
