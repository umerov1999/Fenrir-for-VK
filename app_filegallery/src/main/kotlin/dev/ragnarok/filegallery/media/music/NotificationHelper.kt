package dev.ragnarok.filegallery.media.music

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.ragnarok.filegallery.activity.MainActivity
import dev.ragnarok.filegallery.util.Utils

object NotificationHelper {
    fun getAudioPlayerPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = MainActivity.ACTION_OPEN_AUDIO_PLAYER
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            Utils.makeImmutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
        )
    }

    const val FILE_GALLERY_MUSIC_SERVICE = 1
    const val NOTIFICATION_UPLOAD = 73
    const val NOTIFICATION_DOWNLOADING = 74
    const val NOTIFICATION_DOWNLOAD = 75
    const val NOTIFICATION_DOWNLOAD_MANAGER = 76
    const val NOTIFICATION_DOWNLOADING_GROUP = 77
    const val NOTIFICATION_UPLOAD_FAIL = 78
}