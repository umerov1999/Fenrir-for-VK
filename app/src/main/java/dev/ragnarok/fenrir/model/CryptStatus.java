package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({CryptStatus.NO_ENCRYPTION, CryptStatus.ENCRYPTED, CryptStatus.DECRYPTED, CryptStatus.DECRYPT_FAILED})
@Retention(RetentionPolicy.SOURCE)
public @interface CryptStatus {
    int NO_ENCRYPTION = 0;
    int ENCRYPTED = 1;
    int DECRYPTED = 2;
    int DECRYPT_FAILED = 3;
}
