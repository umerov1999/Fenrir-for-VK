package dev.ragnarok.fenrir.settings.backup;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({SettingTypes.TYPE_BOOL,
        SettingTypes.TYPE_INT,
        SettingTypes.TYPE_STRING,
        SettingTypes.TYPE_STRING_SET})
@Retention(RetentionPolicy.SOURCE)
public @interface SettingTypes {
    int TYPE_BOOL = 0;
    int TYPE_INT = 1;
    int TYPE_STRING = 2;
    int TYPE_STRING_SET = 3;
}

