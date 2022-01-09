package dev.ragnarok.fenrir.settings;

import android.content.Context;
import android.content.pm.PackageManager;

public class AppPrefs {

    public static boolean isCoubInstalled(Context context) {
        return isPackageIntalled(context, "com.coub.android");
    }

    public static boolean isNewPipeInstalled(Context context) {
        return isPackageIntalled(context, "org.schabi.newpipe");
    }

    public static boolean isYoutubeInstalled(Context context) {
        return isPackageIntalled(context, "com.google.android.youtube");
    }

    public static boolean isVancedYoutubeInstalled(Context context) {
        return isPackageIntalled(context, "com.vanced.android.youtube");
    }

    private static boolean isPackageIntalled(Context context, String name) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(name, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
}