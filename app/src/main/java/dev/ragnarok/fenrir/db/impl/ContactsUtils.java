package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.util.Utils.join;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class ContactsUtils {
    public static Single<String> getAllContacts(@NonNull Context context) {
        return Single.create(o -> {
            List<String> contacts = new ArrayList<>();
            Cursor cursor = context.getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = context.getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id},
                                null);
                        while (pCur.moveToNext()) {
                            String phone = pCur.getString(
                                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contacts.add(phone);
                        }
                        pCur.close();
                    }
                }
                cursor.close();
            }
            if (Utils.isEmpty(contacts)) {
                o.onError(new Throwable("Can't collect contact list!"));
            }
            o.onSuccess(join(",", contacts));
        });
    }
}
