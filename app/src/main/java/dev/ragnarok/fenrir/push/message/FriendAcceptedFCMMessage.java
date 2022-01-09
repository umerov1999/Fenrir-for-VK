package dev.ragnarok.fenrir.push.message;

import static dev.ragnarok.fenrir.push.NotificationUtils.configOtherPushNotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.MainActivity;
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels;
import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.push.NotificationScheduler;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class FriendAcceptedFCMMessage {

    // collapseKey: friend_accepted, extras: Bundle[{first_name=Андрей, uid=320891480, from=376771982493,
    // type=friend_accepted, sandbox=0, collapse_key=friend_accepted, last_name=Боталов}]

    //private String first_name;
    //private String last_name;
    private int uid;
    //private long from;
    //private String type;

// FCM
//key: image_type, value: user, class: class java.lang.String
//key: from_id, value: 339247963, class: class java.lang.String
//key: id, value: friend_339247963, class: class java.lang.String
//key: url, value: https://vk.com/id339247963, class: class java.lang.String
//key: icon, value: done_24, class: class java.lang.String
//key: time, value: 1540738607, class: class java.lang.String
//key: type, value: friend_accepted, class: class java.lang.String
//key: badge, value: 69, class: class java.lang.String
//key: image, value: [{"width":200,"url":"https:\/\/pp.userapi.com\/c844418\/v844418689\/110e79\/yMJ6_zsujQ8.jpg","height":200},{"width":100,"url":"https:\/\/pp.userapi.com\/c844418\/v844418689\/110e7a\/olNcuZZOXSU.jpg","height":100},{"width":50,"url":"https:\/\/pp.userapi.com\/c844418\/v844418689\/110e7b\/9yyASlXwnJs.jpg","height":50}], class: class java.lang.String
//key: sound, value: 1, class: class java.lang.String
//key: to_id, value: 25651989, class: class java.lang.String
//key: group_id, value: friend_accepted, class: class java.lang.String
//key: context, value: {"feedback":true,"user_id":339247963}, class: class java.lang.String

    public static FriendAcceptedFCMMessage fromRemoteMessage(@NonNull RemoteMessage remote) {
        FriendAcceptedFCMMessage message = new FriendAcceptedFCMMessage();
        //message.first_name = bundle.getString("first_name");
        //message.last_name = bundle.getString("last_name");
        message.uid = Integer.parseInt(remote.getData().get("from_id"));
        //message.from = FriendFCMMessage.optLong(bundle, "from");
        //message.type = bundle.getString("type");
        return message;
    }

    public void notify(Context context, int accountId) {
        if (!Settings.get()
                .notifications()
                .isFriendRequestAcceptationNotifEnabled()) {
            return;
        }

        Context app = context.getApplicationContext();
        OwnerInfo.getRx(app, accountId, uid)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(ownerInfo -> notifyImpl(app, ownerInfo.getUser(), ownerInfo.getAvatar()), throwable -> {/*ignore*/});
    }

    private void notifyImpl(Context context, @NonNull User user, Bitmap bitmap) {
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getFriendRequestsChannel(context));
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getFriendRequestsChannelId())
                .setSmallIcon(R.drawable.friends)
                .setLargeIcon(bitmap)
                .setContentTitle(user.getFullName())
                .setContentText(context.getString(R.string.accepted_friend_request))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getOwnerWallPlace(aid, uid, user));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, uid, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);
        nManager.notify(String.valueOf(uid), NotificationHelper.NOTIFICATION_FRIEND_ACCEPTED_ID, notification);
    }
}
