package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.ragnarok.fenrir.db.DBHelper
import dev.ragnarok.fenrir.db.interfaces.IStorage
import dev.ragnarok.fenrir.db.interfaces.IStorages
import dev.ragnarok.fenrir.db.model.entity.AttachmentsEntity
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper
import dev.ragnarok.fenrir.db.serialize.AttachmentsDboAdapter
import dev.ragnarok.fenrir.db.serialize.EntityWrapperAdapter
import dev.ragnarok.fenrir.db.serialize.UriSerializer
import dev.ragnarok.fenrir.nonNullNoEmpty

open class AbsStorage(private val mRepositoryContext: AppStorages) : IStorage {
    override val stores: IStorages
        get() = mRepositoryContext

    val context: Context
        get() = mRepositoryContext.applicationContext

    fun helper(accountId: Int): DBHelper {
        return DBHelper.getInstance(context, accountId)
    }

    protected val contentResolver: ContentResolver
        get() = mRepositoryContext.contentResolver

    companion object {
        @JvmField
        val GSON: Gson = GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriSerializer())
            .registerTypeAdapter(AttachmentsEntity::class.java, AttachmentsDboAdapter())
            .registerTypeAdapter(EntityWrapper::class.java, EntityWrapperAdapter())
            .serializeSpecialFloatingPointValues() // for test
            .create()


        fun serializeJson(o: Any?): String? {
            return if (o == null) null else GSON.toJson(o)
        }


        fun <T> deserializeJson(cursor: Cursor, column: String?, clazz: Class<T>?): T? {
            val json = cursor.getString(cursor.getColumnIndexOrThrow(column))
            return if (json.nonNullNoEmpty()) {
                GSON.fromJson(json, clazz)
            } else {
                null
            }
        }


        fun extractId(result: ContentProviderResult): Int {
            result.uri ?: return 0
            return result.uri!!.pathSegments[1].toInt()
        }


        fun <T> addToListAndReturnIndex(target: MutableList<T>, item: T): Int {
            target.add(item)
            return target.size - 1
        }
    }
}