package dev.ragnarok.fenrir.push.message;

import static java.lang.Integer.parseInt;
import static dev.ragnarok.fenrir.push.NotificationUtils.configOtherPushNotification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.MainActivity;
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels;
import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.push.NotificationScheduler;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Utils;

public class NewPostPushMessage {
    private static final Pattern PATTERN_WALL_POST = Pattern.compile("vk.com/(?:[\\w.\\d]+\\?(?:[\\w=&]+)?w=)?wall(-?\\d*)_(\\d*)");

    private final int accountId;
    private final String url;
    private final String body;
    private final String title;

    public NewPostPushMessage(int accountId, RemoteMessage remote) {
        this.accountId = accountId;
        Map<String, String> data = remote.getData();
        url = data.get("url");
        body = data.get("body");
        title = data.get("title");
    }

    @SuppressLint("CheckResult")
    public void notifyIfNeed(Context context) {
        if (url == null) {
            Logger.wtf("NewPostPushMessage", "url is NULL!!!");
            return;
        }

        if (!Settings.get()
                .notifications()
                .isNewPostsNotificationEnabled()) {
            return;
        }

        Matcher matcher = PATTERN_WALL_POST.matcher(url);
        if (matcher.find()) {
            Context app = context.getApplicationContext();
            OwnerInfo.getRx(app, accountId, parseInt(matcher.group(1)))
                    .subscribeOn(NotificationScheduler.INSTANCE)
                    .subscribe(ownerInfo -> notifyImpl(app, ownerInfo.getAvatar()), throwable -> notifyImpl(app, null));
        } else {
            notifyImpl(context, null);
        }
    }

    private void notifyImpl(Context context, Bitmap bitmap) {
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getNewPostChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getNewPostChannelId())
                .setSmallIcon(R.drawable.client_round)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(ThemesController.INSTANCE.toastColor(false))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true);
        if (bitmap != null) {
            builder.setLargeIcon(bitmap);
        }

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, url.hashCode(), intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);

        nManager.notify(url, NotificationHelper.NOTIFICATION_NEW_POSTS_ID, notification);
    }
}
