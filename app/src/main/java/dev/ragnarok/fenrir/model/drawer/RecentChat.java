package dev.ragnarok.fenrir.model.drawer;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

public class RecentChat extends AbsMenuItem {

    public static Creator<RecentChat> CREATOR = new Creator<RecentChat>() {
        public RecentChat createFromParcel(Parcel source) {
            return new RecentChat(source);
        }

        public RecentChat[] newArray(int size) {
            return new RecentChat[size];
        }
    };
    @SerializedName("aid")
    private int aid;
    @SerializedName("peerId")
    private int peerId;
    @SerializedName("title")
    private String title;
    @SerializedName("iconUrl")
    private String iconUrl;

    @SuppressWarnings("unused")
    public RecentChat() {
        super(TYPE_RECENT_CHAT);
    }

    public RecentChat(int aid, int peerId, String title, String iconUrl) {
        super(TYPE_RECENT_CHAT);
        this.aid = aid;
        this.peerId = peerId;
        this.title = title;
        this.iconUrl = iconUrl;
    }

    public RecentChat(Parcel in) {
        super(in);
        aid = in.readInt();
        peerId = in.readInt();
        title = in.readString();
        iconUrl = in.readString();
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getAid() {
        return aid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RecentChat that = (RecentChat) o;
        return aid == that.aid && peerId == that.peerId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + aid;
        result = 31 * result + peerId;
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(aid);
        dest.writeInt(peerId);
        dest.writeString(title);
        dest.writeString(iconUrl);
    }
}
