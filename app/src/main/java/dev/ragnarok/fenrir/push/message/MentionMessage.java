package dev.ragnarok.fenrir.push.message;

import static dev.ragnarok.fenrir.push.NotificationUtils.configOtherPushNotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.MainActivity;
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels;
import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.Utils;

public class MentionMessage {
    private static final Gson GSON = new Gson();
    private int message_id;
    private int peerId;
    private String body;
    private String title;

    public static MentionMessage fromRemoteMessage(RemoteMessage remote) {
        MentionMessage message = new MentionMessage();
        Map<String, String> data = remote.getData();

        MentionContext context = GSON.fromJson(data.get("context"), MentionContext.class);
        message.message_id = context.msg_id;
        message.body = data.get("body");
        message.title = data.get("title");
        message.peerId = context.chat_id == 0 ? context.sender_id : context.chat_id;
        return message;
    }

    public void notify(Context context, int account_id) {
        if (!Settings.get()
                .notifications()
                .isMentionNotifyEnabled()) {
            return;
        }
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getMentionChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getMentionChannelId())
                .setSmallIcon(R.drawable.ic_mention)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(ThemesController.INSTANCE.toastColor(false))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getMessagesLookupPlace(account_id, peerId, message_id, null));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, message_id, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);

        nManager.notify(String.valueOf(message_id), NotificationHelper.NOTIFICATION_MENTION, notification);
    }

    private static final class MentionContext {
        @SerializedName("msg_id")
        int msg_id;

        @SerializedName("sender_id")
        int sender_id;

        @SerializedName("chat_id")
        int chat_id;
    }
}
