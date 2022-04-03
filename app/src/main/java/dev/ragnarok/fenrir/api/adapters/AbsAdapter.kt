package dev.ragnarok.fenrir.api.adapters

import com.google.gson.*
import dev.ragnarok.fenrir.Constants

open class AbsAdapter {
    companion object {

        fun checkObject(element: JsonElement?): Boolean {
            return element is JsonObject
        }


        fun checkPrimitive(element: JsonElement?): Boolean {
            return element is JsonPrimitive
        }


        fun checkArray(element: JsonElement?): Boolean {
            return element is JsonArray && element.size() > 0
        }


        fun hasPrimitive(obj: JsonObject, name: String?): Boolean {
            if (obj.has(name)) {
                return obj[name].isJsonPrimitive
            }
            return false
        }


        fun hasObject(obj: JsonObject, name: String?): Boolean {
            if (obj.has(name)) {
                return obj[name].isJsonObject
            }
            return false
        }


        fun hasArray(obj: JsonObject, name: String?): Boolean {
            if (obj.has(name)) {
                val element = obj[name]
                return element.isJsonArray && element.asJsonArray.size() > 0
            }
            return false
        }


        @JvmOverloads
        fun optString(json: JsonObject, name: String?, fallback: String? = null): String? {
            return try {
                val element = json[name]
                if (element is JsonPrimitive) element.getAsString() else fallback
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }


        fun optBoolean(json: JsonObject, name: String?): Boolean {
            return try {
                val element = json[name]
                if (!checkPrimitive(element)) {
                    return false
                }
                val prim = element.asJsonPrimitive
                try {
                    prim.isBoolean && prim.asBoolean || prim.asInt == 1
                } catch (e: Exception) {
                    prim.asBoolean
                }
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                false
            }
        }


        fun getFirstInt(json: JsonObject, fallback: Int, vararg names: String?): Int {
            return try {
                for (name in names) {
                    val element = json[name]
                    if (element is JsonPrimitive) {
                        return element.getAsInt()
                    }
                }
                fallback
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }


        @JvmOverloads
        fun optLong(array: JsonArray, index: Int, fallback: Long = 0L): Long {
            return try {
                val opt = opt(array, index)
                (opt as? JsonPrimitive)?.asLong ?: fallback
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }


        @JvmOverloads
        fun optInt(array: JsonArray, index: Int, fallback: Int = 0): Int {
            return try {
                val opt = opt(array, index)
                (opt as? JsonPrimitive)?.asInt ?: fallback
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }


        fun opt(array: JsonArray, index: Int): JsonElement? {
            return if (index < 0 || index >= array.size()) {
                null
            } else array[index]
        }


        @JvmOverloads
        fun optString(array: JsonArray, index: Int, fallback: String? = null): String? {
            return try {
                val opt = opt(array, index)
                if (opt is JsonPrimitive) opt.getAsString() else fallback
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }


        @JvmOverloads
        fun optInt(json: JsonObject, name: String?, fallback: Int = 0): Int {
            return try {
                val element = json[name]
                (element as? JsonPrimitive)?.asInt ?: fallback
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }


        @JvmOverloads
        fun optLong(json: JsonObject, name: String?, fallback: Long = 0L): Long {
            return try {
                val element = json[name]
                (element as? JsonPrimitive)?.asLong ?: fallback
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }


        fun <T> parseArray(
            array: JsonElement?,
            type: Class<out T>?,
            context: JsonDeserializationContext,
            fallback: List<T>?
        ): List<T>? {
            return if (array == null || !checkArray(array)) {
                fallback
            } else try {
                val list: MutableList<T> = ArrayList()
                for (i in 0 until array.asJsonArray.size()) {
                    list.add(context.deserialize(array.asJsonArray[i], type))
                }
                list
            } catch (e: JsonParseException) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }

        fun <T> parseArray(
            array: JsonArray?,
            type: Class<out T>?,
            context: JsonDeserializationContext,
            fallback: List<T>?
        ): List<T>? {
            return if (array == null) {
                fallback
            } else try {
                val list: MutableList<T> = ArrayList()
                for (i in 0 until array.size()) {
                    list.add(context.deserialize(array[i], type))
                }
                list
            } catch (e: JsonParseException) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }

        fun optStringArray(
            root: JsonObject,
            name: String?,
            fallback: Array<String?>?
        ): Array<String?>? {
            return try {
                val element = root[name]
                if (!checkArray(element)) {
                    fallback
                } else parseStringArray(element.asJsonArray)
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }


        fun optIntArray(root: JsonObject, name: String?, fallback: IntArray?): IntArray? {
            return try {
                val element = root[name]
                if (!checkArray(element)) {
                    fallback
                } else parseIntArray(element.asJsonArray)
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }


        fun optIntArray(array: JsonArray, index: Int, fallback: IntArray?): IntArray? {
            return try {
                if (index < 0 || index >= array.size()) {
                    return fallback
                }
                val array_r = array[index]
                if (!checkArray(array_r)) {
                    fallback
                } else parseIntArray(array_r.asJsonArray)
            } catch (e: Exception) {
                if (Constants.IS_DEBUG) {
                    e.printStackTrace()
                }
                fallback
            }
        }

        private fun parseIntArray(array: JsonArray): IntArray {
            val list = IntArray(array.size())
            for (i in 0 until array.size()) {
                list[i] = array[i].asInt
            }
            return list
        }

        private fun parseStringArray(array: JsonArray): Array<String?> {
            val list = arrayOfNulls<String>(array.size())
            for (i in 0 until array.size()) {
                list[i] = array[i].asString
            }
            return list
        }
    }
}