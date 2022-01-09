package dev.ragnarok.fenrir;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AccountType.BY_TYPE,
        AccountType.VK_ANDROID,
        AccountType.VK_ANDROID_HIDDEN,
        AccountType.KATE,
        AccountType.KATE_HIDDEN})
@Retention(RetentionPolicy.SOURCE)
public @interface AccountType {
    int BY_TYPE = 0;
    int VK_ANDROID = 1;
    int VK_ANDROID_HIDDEN = 2;
    int KATE = 3;
    int KATE_HIDDEN = 4;
}

