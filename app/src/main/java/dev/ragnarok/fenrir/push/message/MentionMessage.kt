package dev.ragnarok.fenrir.push.message

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.MainActivity
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels.getMentionChannel
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels.mentionChannelId
import dev.ragnarok.fenrir.longpoll.NotificationHelper
import dev.ragnarok.fenrir.place.PlaceFactory.getMessagesLookupPlace
import dev.ragnarok.fenrir.push.NotificationUtils.configOtherPushNotification
import dev.ragnarok.fenrir.settings.Settings.get
import dev.ragnarok.fenrir.settings.theme.ThemesController.toastColor
import dev.ragnarok.fenrir.util.Utils.hasOreo
import dev.ragnarok.fenrir.util.Utils.makeMutablePendingIntent

class MentionMessage {
    private var message_id = 0
    private var peerId = 0
    private var body: String? = null
    private var title: String? = null
    fun notify(context: Context, account_id: Int) {
        if (!get()
                .notifications()
                .isMentionNotifyEnabled
        ) {
            return
        }
        val nManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (hasOreo()) {
            nManager?.createNotificationChannel(getMentionChannel(context))
        }
        val builder = NotificationCompat.Builder(context, mentionChannelId)
            .setSmallIcon(R.drawable.ic_mention)
            .setContentTitle(title)
            .setContentText(body)
            .setColor(toastColor(false))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(Extra.PLACE, getMessagesLookupPlace(account_id, peerId, message_id, null))
        intent.action = MainActivity.ACTION_OPEN_PLACE
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val contentIntent = PendingIntent.getActivity(
            context,
            message_id,
            intent,
            makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
        )
        builder.setContentIntent(contentIntent)
        val notification = builder.build()
        configOtherPushNotification(notification)
        nManager?.notify(
            message_id.toString(),
            NotificationHelper.NOTIFICATION_MENTION,
            notification
        )
    }

    private class MentionContext {
        @SerializedName("msg_id")
        var msg_id = 0

        @SerializedName("sender_id")
        var sender_id = 0

        @SerializedName("chat_id")
        var chat_id = 0
    }

    companion object {
        private val GSON = Gson()
        fun fromRemoteMessage(remote: RemoteMessage): MentionMessage {
            val message = MentionMessage()
            val data = remote.data
            val context = GSON.fromJson(data["context"], MentionContext::class.java)
            message.message_id = context.msg_id
            message.body = data["body"]
            message.title = data["title"]
            message.peerId = if (context.chat_id == 0) context.sender_id else context.chat_id
            return message
        }
    }
}