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

public class FriendFCMMessage {

    //collapseKey: friend, extras: Bundle[{first_name=Андрей, uid=320891480, from=376771982493,
    // type=friend, badge=1, common_count=0, sandbox=0, collapse_key=friend, last_name=Боталов}]

    //private String first_name;
    //private String last_name;
    private int from_id;
    //private long from;
    //private String type;
    //private int badge;
    //private int common_count;

    public static FriendFCMMessage fromRemoteMessage(@NonNull RemoteMessage remote) {
        FriendFCMMessage message = new FriendFCMMessage();
        //message.first_name = bundle.getString("first_name");
        //message.last_name = bundle.getString("last_name");
        message.from_id = Integer.parseInt(remote.getData().get("from_id"));
        //message.from = optLong(bundle, "from");
        //message.type = bundle.getString("type");
        //message.badge = optInt(bundle, "badge");
        //message.common_count = optInt(bundle, "common_count");
        return message;
    }

    public void notify(Context context, int accountId) {
        if (!Settings.get()
                .notifications()
                .isNewFollowerNotifEnabled()) {
            return;
        }

        Context app = context.getApplicationContext();
        OwnerInfo.getRx(app, accountId, from_id)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(ownerInfo -> notifyImpl(app, ownerInfo.getUser(), ownerInfo.getAvatar()), throwable -> {/*ignore*/});
    }

    private void notifyImpl(Context context, User user, Bitmap bitmap) {
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getFriendRequestsChannel(context));
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getFriendRequestsChannelId())
                .setSmallIcon(R.drawable.friends)
                .setLargeIcon(bitmap)
                .setContentTitle(user.getFullName())
                .setContentText(context.getString(R.string.subscribed_to_your_updates))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getOwnerWallPlace(aid, from_id, user));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, from_id, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);
        nManager.notify(String.valueOf(from_id), NotificationHelper.NOTIFICATION_FRIEND_ID, notification);
    }
}
