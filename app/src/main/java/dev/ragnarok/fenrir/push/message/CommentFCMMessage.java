package dev.ragnarok.fenrir.push.message;

import static dev.ragnarok.fenrir.push.NotificationUtils.configOtherPushNotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.MainActivity;
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels;
import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentedType;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.push.NotificationScheduler;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class CommentFCMMessage {

    /**
     * Идентификатор пользователя
     */
    private int from_id;

    /**
     * Идентификатор комментария
     */
    private int reply_id;

    //private int sex;
    //public long from;
    private String text;

    private String type;

    private int item_id;

    private int owner_id;

    //extras: Bundle[{google.sent_time=1477925617791, from_id=175895893, reply_id=3686, sex=2,
    // text=да, type=comment, place=wall25651989_3499, google.message_id=0:1477925617795994%8c76e97a38a5ee5f, _genSrv=833239, sandbox=0, collapse_key=comment}]

    public static CommentFCMMessage fromRemoteMessage(@NonNull RemoteMessage remote) {
        CommentFCMMessage message = new CommentFCMMessage();
        message.from_id = Integer.parseInt(remote.getData().get("from_id"));
        message.text = remote.getData().get("body");

        PushContext context = new Gson().fromJson(remote.getData().get("context"), PushContext.class);
        message.reply_id = context.reply_id;
        message.type = context.type;
        message.item_id = context.item_id;
        message.owner_id = context.owner_id;
        return message;
    }

    public void notify(Context context, int accountId) {
        if (!Settings.get()
                .notifications()
                .isCommentsNotificationsEnabled()) {
            return;
        }

        Context app = context.getApplicationContext();
        OwnerInfo.getRx(context, accountId, from_id)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(ownerInfo -> notifyImpl(app, ownerInfo), RxUtils.ignore());
    }

    private void notifyImpl(Context context, OwnerInfo ownerInfo) {
        Commented commented = null;
        String title = null;

        switch (type) {
            case "photo_comment":
                title = context.getString(R.string.photo_comment_push_title);
                commented = new Commented(item_id, owner_id, CommentedType.PHOTO, null);
                break;
            case "video_comment":
                title = context.getString(R.string.video_comment_push_title);
                commented = new Commented(item_id, owner_id, CommentedType.VIDEO, null);
                break;
            case "comment":
                title = context.getString(R.string.wall_comment_push_title);
                commented = new Commented(item_id, owner_id, CommentedType.POST, null);
                break;
        }

        if (commented == null) {
            return;
        }

        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getCommentsChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getCommentsChannelId())
                .setSmallIcon(R.drawable.comment_thread)
                .setLargeIcon(ownerInfo.getAvatar())
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getCommentsPlace(aid, commented, reply_id));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, reply_id, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();

        configOtherPushNotification(notification);

        String tag = type + item_id + "_" + owner_id;
        nManager.notify(tag, NotificationHelper.NOTIFICATION_COMMENT_ID, notification);
    }

    private static final class PushContext {
        @SerializedName("feedback")
        boolean feedback;

        @SerializedName("reply_id")
        int reply_id;

        @SerializedName("user_id")
        int user_id;

        @SerializedName("item_id")
        int item_id;

        @SerializedName("owner_id")
        int owner_id;

        @SerializedName("type")
        String type;
    }
}
