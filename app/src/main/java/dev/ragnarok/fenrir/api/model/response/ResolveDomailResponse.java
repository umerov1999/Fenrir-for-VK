package dev.ragnarok.fenrir.api.model.response;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class ResolveDomailResponse implements Parcelable {

    public static final String TYPE_USER = "user";
    public static final String TYPE_GROUP = "group";
    public static final Creator<ResolveDomailResponse> CREATOR = new Creator<ResolveDomailResponse>() {
        @Override
        public ResolveDomailResponse createFromParcel(Parcel in) {
            return new ResolveDomailResponse(in);
        }

        @Override
        public ResolveDomailResponse[] newArray(int size) {
            return new ResolveDomailResponse[size];
        }
    };
    public String type;
    public String object_id;

    public ResolveDomailResponse() {

    }

    protected ResolveDomailResponse(Parcel in) {
        type = in.readString();
        object_id = in.readString();
    }

    public ResolveDomailResponse parse(JSONObject jsonObject) {
        type = jsonObject.optString("type");
        object_id = jsonObject.optString("object_id");
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(object_id);
    }

    @NonNull
    @Override
    public String toString() {
        return "ResolveDomailResponse{" +
                "type='" + type + '\'' +
                ", object_id='" + object_id + '\'' +
                '}';
    }
}
