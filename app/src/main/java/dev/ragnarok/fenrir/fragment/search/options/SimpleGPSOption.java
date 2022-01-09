package dev.ragnarok.fenrir.fragment.search.options;

import android.os.Parcel;

import androidx.annotation.NonNull;

public class SimpleGPSOption extends BaseOption {

    public static final Creator<SimpleGPSOption> CREATOR = new Creator<SimpleGPSOption>() {
        @Override
        public SimpleGPSOption createFromParcel(Parcel in) {
            return new SimpleGPSOption(in);
        }

        @Override
        public SimpleGPSOption[] newArray(int size) {
            return new SimpleGPSOption[size];
        }
    };
    public double lat_gps;
    public double long_gps;

    public SimpleGPSOption(int key, int title, boolean active) {
        super(GPS, key, title, active);
    }

    protected SimpleGPSOption(Parcel in) {
        super(in);
        lat_gps = in.readDouble();
        long_gps = in.readDouble();
    }

    public String simpleGPS() {
        return "{ lat=" + lat_gps + ", long=" + long_gps + " }";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(lat_gps);
        dest.writeDouble(long_gps);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SimpleGPSOption that = (SimpleGPSOption) o;
        return lat_gps == that.lat_gps && long_gps == that.long_gps;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Double.valueOf(lat_gps).hashCode();
        result = 31 * result + Double.valueOf(long_gps).hashCode();
        return result;
    }

    @NonNull
    @Override
    public SimpleGPSOption clone() throws CloneNotSupportedException {
        return (SimpleGPSOption) super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
