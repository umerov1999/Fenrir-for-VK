package dev.ragnarok.fenrir.activity.alias;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ToggleAlias {
    private final List<Class<?>> aliases = new ArrayList<>(10);

    public ToggleAlias() {
        aliases.add(DefaultFenrirAlias.class);
        aliases.add(BlueFenrirAlias.class);
        aliases.add(GreenFenrirAlias.class);
        aliases.add(VioletFenrirAlias.class);
        aliases.add(RedFenrirAlias.class);
        aliases.add(YellowFenrirAlias.class);
        aliases.add(BlackFenrirAlias.class);
        aliases.add(VKFenrirAlias.class);
        aliases.add(WhiteFenrirAlias.class);
        aliases.add(LineageFenrirAlias.class);
    }

    public void toggleTo(@NonNull Context context, @NonNull Class<?> v) {
        for (Class<?> i : aliases) {
            if (i == v) {
                continue;
            }
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, i), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, v), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
}