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
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.push.NotificationScheduler;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class GroupInviteFCMMessage {

    //collapseKey: group_invite, extras: Bundle[{from_id=175895893, from=376771982493, name=Pianoбой,
    // type=group_invite, group_id=1583008, sandbox=0, collapse_key=group_invite}]

    private int from_id;
    //public long from;
    //public String name;
    //public String type;
    private int group_id;

    public static GroupInviteFCMMessage fromRemoteMessage(@NonNull RemoteMessage remote) {
        GroupInviteFCMMessage message = new GroupInviteFCMMessage();
        message.from_id = Integer.parseInt(remote.getData().get("from_id"));
        //message.name = bundle.getString("name");
        message.group_id = Integer.parseInt(remote.getData().get("group_id"));
        //message.from = FriendFCMMessage.optLong(bundle, "from");
        //message.type = bundle.getString("type");
        return message;
    }

    public void notify(Context context, int accountId) {
        if (!Settings.get()
                .notifications()
                .isGroupInvitedNotifEnabled()) {
            return;
        }

        Context app = context.getApplicationContext();
        Single<OwnerInfo> group = OwnerInfo.getRx(app, accountId, -Math.abs(group_id));
        Single<OwnerInfo> user = OwnerInfo.getRx(app, accountId, from_id);

        Single.zip(group, user, Pair::new)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(pair -> {
                    OwnerInfo userInfo = pair.getSecond();
                    OwnerInfo groupInfo = pair.getFirst();
                    notifyImpl(app, userInfo.getUser(), groupInfo.getAvatar(), groupInfo.getCommunity());
                }, throwable -> {/*ignore*/});
    }

    private void notifyImpl(Context context, @NonNull User user, Bitmap groupBitmap, @NonNull Community community) {
        String contentText = context.getString(R.string.invites_you_to_join_community, user.getFullName());
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getGroupInvitesChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getGroupInvitesChannelId())
                .setSmallIcon(R.drawable.groups)
                .setLargeIcon(groupBitmap)
                .setContentTitle(community.getFullName())
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getOwnerWallPlace(aid, community));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, group_id, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);
        nManager.notify(String.valueOf(group_id), NotificationHelper.NOTIFICATION_GROUP_INVITE_ID, notification);
    }
}
