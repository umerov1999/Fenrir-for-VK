package dev.ragnarok.fenrir.settings

import android.content.Context
import android.content.pm.PackageManager

object AppPrefs {
    fun isCoubInstalled(context: Context): Boolean {
        return isPackageIntalled(context, "com.coub.android")
    }

    fun isNewPipeInstalled(context: Context): Boolean {
        return isPackageIntalled(context, "org.schabi.newpipe")
    }

    fun isYoutubeInstalled(context: Context): Boolean {
        return isPackageIntalled(context, "com.google.android.youtube")
    }

    fun isVancedYoutubeInstalled(context: Context): Boolean {
        return isPackageIntalled(context, "com.vanced.android.youtube")
    }

    private fun isPackageIntalled(context: Context, name: String): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(name, PackageManager.GET_ACTIVITIES)
            true
        } catch (ignored: PackageManager.NameNotFoundException) {
            false
        }
    }
}