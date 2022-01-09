package dev.ragnarok.fenrir.util;

import android.os.Parcel;

import java.util.HashMap;
import java.util.Map;

public class ParcelUtils {

    public static void writeIntStringMap(Parcel dest, Map<Integer, String> map) {
        boolean isNull = Objects.isNull(map);
        dest.writeByte(isNull ? (byte) 1 : (byte) 0);
        if (isNull) {
            return;
        }

        int size = map.size();
        dest.writeInt(size);
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            writeObjectInteger(dest, key);
            dest.writeString(value);
        }
    }

    public static Map<Integer, String> readIntStringMap(Parcel in) {
        boolean isNull = in.readByte() == (byte) 1;
        if (isNull) {
            return null;
        }

        int size = in.readInt();
        Map<Integer, String> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Integer key = readObjectInteger(in);
            String value = in.readString();
            map.put(key, value);
        }

        return map;
    }

    public static void writeObjectDouble(Parcel dest, Double value) {
        dest.writeByte(value == null ? (byte) 1 : (byte) 0);
        if (value != null) {
            dest.writeDouble(value);
        }
    }

    public static Double readObjectDouble(Parcel in) {
        boolean isNull = in.readByte() == (byte) 1;
        if (!isNull) {
            return in.readDouble();
        } else return null;
    }

    public static void writeObjectInteger(Parcel dest, Integer value) {
        dest.writeByte(value == null ? (byte) 1 : (byte) 0);
        if (value != null) {
            dest.writeInt(value);
        }
    }

    public static Integer readObjectInteger(Parcel in) {
        boolean isNull = in.readByte() == (byte) 1;
        if (!isNull) {
            return in.readInt();
        } else return null;
    }

    public static void writeObjectLong(Parcel dest, Long value) {
        dest.writeByte(value == null ? (byte) 1 : (byte) 0);
        if (value != null) {
            dest.writeLong(value);
        }
    }

    public static Long readObjectLong(Parcel in) {
        boolean isNull = in.readByte() == (byte) 1;
        if (!isNull) {
            return in.readLong();
        } else return null;
    }

}
