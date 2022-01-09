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

public class BirthdayFCMMessage {
    private static final Gson GSON = new Gson();
    private int user_id;
    private String body;
    private String title;

    /*
    public static class tst {
        public Map<String, String> data;
    }
    public static void test(Context ctx) {
        BirthdayFCMMessage message = new BirthdayFCMMessage();
        Map<String, String> data = new ArrayMap<>();
        data = new Gson().fromJson("{ \"data\": {\n" +
                "  \"subtype\": \"birthday\",\n" +
                "  \"image_type\": \"user\",\n" +
                "  \"need_track_interaction\": \"1\",\n" +
                "  \"from_id\": \"647737194\",\n" +
                "  \"id\": \"friend_647737194\",\n" +
                "  \"url\": \"https://vk.com/id647737194\",\n" +
                "  \"body\": \"Отправьте подарок сейчас, чтобы точно не забыть поздравить\",\n" +
                "  \"icon\": \"gift_24\",\n" +
                "  \"stat\": \"time_sent\\u003d1626588192317\\u0026provider\\u003dfcm\\u0026is_feedback\\u003d1\\u0026subtype\\u003dbirthday\\u0026notify_sent_time\\u003d1626587947\",\n" +
                "  \"time\": \"1626588192\",\n" +
                "  \"type\": \"birthday\",\n" +
                "  \"category\": \"birthday\",\n" +
                "  \"image\": \"[{\\\"width\\\":200,\\\"url\\\":\\\"https:\\\\/\\\\/sun9-4.userapi.com\\\\/bz_33e8I8PLH1GiuInIZf1NNm11hYV5r4eWCzQ\\\\/3SypfdXM0-E.jpg\\\",\\\"height\\\":200},{\\\"width\\\":100,\\\"url\\\":\\\"https:\\\\/\\\\/sun9-74.userapi.com\\\\/rWpMl1okQy4AfL60WiGl_oIMmb-VGg1aCZOqNw\\\\/LEwSkGEPC1A.jpg\\\",\\\"height\\\":100},{\\\"width\\\":50,\\\"url\\\":\\\"https:\\\\/\\\\/sun9-72.userapi.com\\\\/SkPJ3NVl9pSDgEmC6QYvffqlOCsz8FeHF_Maiw\\\\/Vlt-jsdxha0.jpg\\\",\\\"height\\\":50}]\",\n" +
                "  \"sound\": \"0\",\n" +
                "  \"title\": \"День рождения у Бибы\",\n" +
                "  \"to_id\": \"581662705\",\n" +
                "  \"group_id\": \"birthdays\",\n" +
                "  \"context\": \"{\\\"feedback\\\":true,\\\"user_id\\\":647737194}\",\n" +
                "  \"notify_sent_time\": \"1626587947\"\n" +
                "}}", tst.class).data;
        BirthdayContext context = GSON.fromJson(data.get("context"), BirthdayContext.class);
        message.user_id = context.user_id;
        message.body = data.get("body");
        message.title = data.get("title");
        message.notify(ctx, Settings.get().accounts().getCurrent());
    }
     */

    public static BirthdayFCMMessage fromRemoteMessage(RemoteMessage remote) {
        BirthdayFCMMessage message = new BirthdayFCMMessage();
        Map<String, String> data = remote.getData();

        BirthdayContext context = GSON.fromJson(data.get("context"), BirthdayContext.class);
        message.user_id = context.user_id;
        message.body = data.get("body");
        message.title = data.get("title");
        return message;
    }

    public void notify(Context context, int account_id) {
        if (!Settings.get()
                .notifications()
                .isBirthdayNotifyEnabled()) {
            return;
        }
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getBirthdaysChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getBirthdaysChannelId())
                .setSmallIcon(R.drawable.cake)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(ThemesController.INSTANCE.toastColor(false))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getOwnerWallPlace(account_id, user_id, null));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, user_id, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);

        nManager.notify(String.valueOf(user_id), NotificationHelper.NOTIFICATION_BIRTHDAY, notification);
    }

    private static final class BirthdayContext {
        @SerializedName("user_id")
        int user_id;

        @SerializedName("feedback")
        boolean feedback;
    }
}
