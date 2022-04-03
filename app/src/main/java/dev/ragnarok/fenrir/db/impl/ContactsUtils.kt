package dev.ragnarok.fenrir.db.impl

import android.content.Context
import android.provider.BaseColumns
import android.provider.ContactsContract
import dev.ragnarok.fenrir.util.Utils.join
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

object ContactsUtils {

    fun getAllContacts(context: Context): Single<String> {
        return Single.create { o: SingleEmitter<String> ->
            val contacts: MutableList<String> = ArrayList()
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            if (cursor == null) {
                o.onError(Throwable("Can't collect contact list!"))
                return@create
            }
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    if (cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            .toInt() > 0
                    ) {
                        val pCur = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id),
                            null
                        )
                        while (pCur?.moveToNext() == true) {
                            val phone = pCur.getString(
                                pCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                            contacts.add(phone)
                        }
                        pCur?.close()
                    }
                }
                cursor.close()
            }
            if (contacts.isNullOrEmpty()) {
                o.onError(Throwable("Can't collect contact list!"))
            }
            o.onSuccess(join(",", contacts))
        }
    }
}