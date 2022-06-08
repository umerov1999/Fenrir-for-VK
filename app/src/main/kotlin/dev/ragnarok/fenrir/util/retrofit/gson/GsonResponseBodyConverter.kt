package dev.ragnarok.fenrir.util.retrofit.gson

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonToken
import dev.ragnarok.fenrir.api.model.response.VkResponse
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException
import java.lang.reflect.Type

internal class GsonResponseBodyConverter<T>(
    private val gson: Gson,
    private val adapter: TypeAdapter<T>,
    private val type: Type
) : Converter<ResponseBody, T> {
    @Throws(IOException::class)
    override fun convert(value: ResponseBody?): T? {
        val jsonReader = gson.newJsonReader(value?.charStream())
        return value.use {
            val result = adapter.read(jsonReader)
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw JsonIOException("JSON document was not fully consumed.")
            }
            if (result is VkResponse) {
                result.error?.let {
                    it.type = type
                }
            }
            result
        }
    }
}