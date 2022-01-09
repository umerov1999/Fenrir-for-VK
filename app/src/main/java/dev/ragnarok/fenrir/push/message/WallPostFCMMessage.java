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
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.MainActivity;
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels;
import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.push.NotificationScheduler;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class WallPostFCMMessage {

    //from_id=175895893, first_name=Руслан, from=376771982493, text=Тест push-уведомлений, type=wall_post, place=wall25651989_2509, collapse_key=wall_post, last_name=Колбаса

    private int from_id;
    private int post_id;
    //public String first_name;
    //public String last_name;
    //public long from;
    private String body;
    //public String type;
    private String place;
    private int owner_id;
    private String title;

    /*2018-10-29 14:09:00.106 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: onMessage, from: 237327763482, pushType: post, data: {image_type=user, from_id=175895893, id=wall_post_25651989_4099, url=https://vk.com/wall25651989_4099, body=Руслан Колбаса: Дарова!, icon=write_24, time=1540814940, type=post, category=wall_posts, badge=64, image=[{"width":200,"url":"https:\/\/pp.userapi.com\/c626917\/v626917893\/f230\/KnzJyeBQr30.jpg","height":200},{"width":100,"url":"https:\/\/pp.userapi.com\/c626917\/v626917893\/f232\/A7dV0Aj_zHE.jpg","height":100},{"width":50,"url":"https:\/\/pp.userapi.com\/c626917\/v626917893\/f233\/wThqed0he9s.jpg","height":50}], sound=1, title=Новая запись на стене, to_id=25651989, group_id=posts, context={"feedback":true,"item_id":"4099","owner_id":"25651989","type":"post"}}
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: image_type, value: user, class: class java.lang.String
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: from_id, value: 175895893, class: class java.lang.String
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: id, value: wall_post_25651989_4099, class: class java.lang.String
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: url, value: https://vk.com/wall25651989_4099, class: class java.lang.String
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: body, value: Руслан Колбаса: Дарова!, class: class java.lang.String
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: icon, value: write_24, class: class java.lang.String
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: time, value: 1540814940, class: class java.lang.String
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: type, value: post, class: class java.lang.String
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: category, value: wall_posts, class: class java.lang.String
        2018-10-29 14:09:00.107 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: badge, value: 64, class: class java.lang.String
        2018-10-29 14:09:00.108 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: image, value: [{"width":200,"url":"https:\/\/pp.userapi.com\/c626917\/v626917893\/f230\/KnzJyeBQr30.jpg","height":200},{"width":100,"url":"https:\/\/pp.userapi.com\/c626917\/v626917893\/f232\/A7dV0Aj_zHE.jpg","height":100},{"width":50,"url":"https:\/\/pp.userapi.com\/c626917\/v626917893\/f233\/wThqed0he9s.jpg","height":50}], class: class java.lang.String
        2018-10-29 14:09:00.108 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: sound, value: 1, class: class java.lang.String
        2018-10-29 14:09:00.108 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: title, value: Новая запись на стене, class: class java.lang.String
        2018-10-29 14:09:00.108 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: to_id, value: 25651989, class: class java.lang.String
        2018-10-29 14:09:00.108 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: group_id, value: posts, class: class java.lang.String
        2018-10-29 14:09:00.108 18518-18893/dev.ragnarok.fenrir D/FcmListenerService: key: context, value: {"feedback":true,"item_id":"4099","owner_id":"25651989","type":"post"}, class: class java.lang.String
  */
    public static WallPostFCMMessage fromRemoteMessage(@NonNull RemoteMessage remote) {
        WallPostFCMMessage message = new WallPostFCMMessage();
        Map<String, String> data = remote.getData();
        message.from_id = Integer.parseInt(remote.getData().get("from_id"));

        message.body = data.get("body");
        message.place = data.get("url");
        message.title = data.get("title");

        PushContext context = new Gson().fromJson(data.get("context"), PushContext.class);
        message.post_id = context.itemId;
        message.owner_id = context.ownerId;
        return message;
    }

    public void nofify(Context context, int accountId) {
        if (accountId == owner_id) {
            notifyWallPost(context, accountId);
        } else {
            notifyNewPost(context, accountId);
        }
    }

    private void notifyWallPost(Context context, int accountId) {
        if (!Settings.get()
                .notifications()
                .isNewPostOnOwnWallNotifEnabled()) {
            return;
        }

        Context app = context.getApplicationContext();
        OwnerInfo.getRx(app, accountId, from_id)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(ownerInfo -> notifyImpl(app, ownerInfo.getOwner(), ownerInfo.getAvatar()), RxUtils.ignore());
    }

    private void notifyNewPost(Context context, int accountId) {
        if (!Settings.get()
                .notifications()
                .isNewPostsNotificationEnabled()) {
            return;
        }

        Context app = context.getApplicationContext();
        OwnerInfo.getRx(app, accountId, owner_id)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(info -> {
                    NotificationManager manager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Utils.hasOreo()) {
                        manager.createNotificationChannel(AppNotificationChannels.getNewPostChannel(app));
                    }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(app, AppNotificationChannels.getNewPostChannelId())
                            .setSmallIcon(R.drawable.client_round)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setLargeIcon(info.getAvatar())
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                            .setAutoCancel(true);

                    builder.setPriority(NotificationCompat.PRIORITY_HIGH);

                    Intent intent = new Intent(app, MainActivity.class);
                    intent.putExtra(Extra.PLACE, PlaceFactory.getPostPreviewPlace(accountId, post_id, owner_id));

                    intent.setAction(MainActivity.ACTION_OPEN_PLACE);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    PendingIntent contentIntent = PendingIntent.getActivity(app, owner_id, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
                    builder.setContentIntent(contentIntent);
                    Notification notification = builder.build();

                    configOtherPushNotification(notification);

                    manager.notify("new_post" + owner_id + "_" + post_id, NotificationHelper.NOTIFICATION_NEW_POSTS_ID, notification);
                }, RxUtils.ignore());
    }

    private void notifyImpl(Context context, @NonNull Owner owner, Bitmap avatar) {
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getNewPostChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getNewPostChannelId())
                .setSmallIcon(R.drawable.pencil)
                .setLargeIcon(avatar)
                .setContentTitle(owner.getFullName())
                .setContentText(context.getString(R.string.published_post_on_your_wall))
                .setSubText(body)
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getPostPreviewPlace(aid, post_id, owner_id));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, post_id, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);
        nManager.notify(place, NotificationHelper.NOTIFICATION_WALL_POST_ID, notification);
    }

    private static final class PushContext {
        @SerializedName("item_id")
        int itemId;
        @SerializedName("owner_id")
        int ownerId;
        @SerializedName("type")
        String type;
    }
}
