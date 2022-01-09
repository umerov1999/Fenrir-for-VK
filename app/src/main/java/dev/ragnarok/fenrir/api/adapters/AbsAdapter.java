package dev.ragnarok.fenrir.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Constants;

public class AbsAdapter {

    public static String optString(JsonObject json, String name) {
        return optString(json, name, null);
    }

    public static boolean checkObject(JsonElement element) {
        return element instanceof JsonObject;
    }

    public static boolean checkPrimitive(JsonElement element) {
        return element instanceof JsonPrimitive;
    }

    public static boolean checkArray(JsonElement element) {
        return element instanceof JsonArray && ((JsonArray) element).size() > 0;
    }

    public static boolean hasPrimitive(JsonObject object, String name) {
        if (object.has(name)) {
            JsonElement element = object.get(name);
            return element.isJsonPrimitive();
        }
        return false;
    }

    public static boolean hasObject(JsonObject object, String name) {
        if (object.has(name)) {
            JsonElement element = object.get(name);
            return element.isJsonObject();
        }
        return false;
    }

    public static boolean hasArray(JsonObject object, String name) {
        if (object.has(name)) {
            JsonElement element = object.get(name);
            return element.isJsonArray() && element.getAsJsonArray().size() > 0;
        }
        return false;
    }

    public static String optString(JsonObject json, String name, String fallback) {
        try {
            JsonElement element = json.get(name);
            return element instanceof JsonPrimitive ? element.getAsString() : fallback;
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static boolean optBoolean(JsonObject json, String name) {
        try {
            JsonElement element = json.get(name);
            if (!checkPrimitive(element)) {
                return false;
            }
            JsonPrimitive prim = element.getAsJsonPrimitive();
            try {
                return prim.isBoolean() && prim.getAsBoolean() || prim.getAsInt() == 1;
            } catch (Exception e) {
                return prim.getAsBoolean();
            }
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static int optInt(JsonObject json, String name) {
        return optInt(json, name, 0);
    }

    public static int optInt(JsonArray array, int index) {
        return optInt(array, index, 0);
    }

    public static int getFirstInt(JsonObject json, int fallback, String... names) {
        try {
            for (String name : names) {
                JsonElement element = json.get(name);
                if (element instanceof JsonPrimitive) {
                    return element.getAsInt();
                }
            }
            return fallback;
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static long optLong(JsonArray array, int index) {
        return optLong(array, index, 0L);
    }

    public static long optLong(JsonArray array, int index, long fallback) {
        try {
            JsonElement opt = opt(array, index);
            return opt instanceof JsonPrimitive ? opt.getAsLong() : fallback;
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static int optInt(JsonArray array, int index, int fallback) {
        try {
            JsonElement opt = opt(array, index);
            return opt instanceof JsonPrimitive ? opt.getAsInt() : fallback;
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static JsonElement opt(JsonArray array, int index) {
        if (index < 0 || index >= array.size()) {
            return null;
        }

        return array.get(index);
    }

    public static String optString(JsonArray array, int index) {
        return optString(array, index, null);
    }

    public static String optString(JsonArray array, int index, String fallback) {
        try {
            JsonElement opt = opt(array, index);
            return opt instanceof JsonPrimitive ? opt.getAsString() : fallback;
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static int optInt(JsonObject json, String name, int fallback) {
        try {
            JsonElement element = json.get(name);
            return (element instanceof JsonPrimitive) ? element.getAsInt() : fallback;
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    public static long optLong(JsonObject json, String name) {
        return optLong(json, name, 0L);
    }

    public static long optLong(JsonObject json, String name, long fallback) {
        try {
            JsonElement element = json.get(name);
            return element instanceof JsonPrimitive ? element.getAsLong() : fallback;
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    protected static <T> List<T> parseArray(JsonElement array, Class<? extends T> type, JsonDeserializationContext context, List<T> fallback) {
        if (!checkArray(array)) {
            return fallback;
        }

        try {
            List<T> list = new ArrayList<>();
            for (int i = 0; i < array.getAsJsonArray().size(); i++) {
                list.add(context.deserialize(array.getAsJsonArray().get(i), type));
            }

            return list;
        } catch (JsonParseException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    protected static <T> List<T> parseArray(JsonArray array, Class<? extends T> type, JsonDeserializationContext context, List<T> fallback) {
        if (array == null) {
            return fallback;
        }

        try {
            List<T> list = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                list.add(context.deserialize(array.get(i), type));
            }

            return list;
        } catch (JsonParseException e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    protected static String[] optStringArray(JsonObject root, String name, String[] fallback) {
        try {
            JsonElement element = root.get(name);
            if (!checkArray(element)) {
                return fallback;
            }
            return parseStringArray(element.getAsJsonArray());
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    protected static int[] optIntArray(JsonObject root, String name, int[] fallback) {
        try {
            JsonElement element = root.get(name);
            if (!checkArray(element)) {
                return fallback;
            }
            return parseIntArray(element.getAsJsonArray());
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    protected static int[] optIntArray(JsonArray array, int index, int[] fallback) {
        try {
            if (index < 0 || index >= array.size()) {
                return fallback;
            }

            JsonElement array_r = array.get(index);
            if (!checkArray(array_r)) {
                return fallback;
            }
            return parseIntArray(array_r.getAsJsonArray());
        } catch (Exception e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return fallback;
        }
    }

    private static int[] parseIntArray(JsonArray array) {
        int[] list = new int[array.size()];
        for (int i = 0; i < array.size(); i++) {
            list[i] = array.get(i).getAsInt();
        }

        return list;
    }

    private static String[] parseStringArray(JsonArray array) {
        String[] list = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            list[i] = array.get(i).getAsString();
        }

        return list;
    }
}