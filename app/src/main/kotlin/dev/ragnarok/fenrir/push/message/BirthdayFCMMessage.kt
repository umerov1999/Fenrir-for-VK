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
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels.birthdaysChannelId
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels.getBirthdaysChannel
import dev.ragnarok.fenrir.longpoll.NotificationHelper
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace
import dev.ragnarok.fenrir.push.NotificationUtils.configOtherPushNotification
import dev.ragnarok.fenrir.settings.Settings.get
import dev.ragnarok.fenrir.settings.theme.ThemesController.toastColor
import dev.ragnarok.fenrir.util.Utils.hasOreo
import dev.ragnarok.fenrir.util.Utils.makeMutablePendingIntent

class BirthdayFCMMessage {
    private var user_id = 0
    private var body: String? = null
    private var title: String? = null
    fun notify(context: Context, account_id: Int) {
        if (!get()
                .notifications()
                .isBirthdayNotifyEnabled
        ) {
            return
        }
        val nManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (hasOreo()) {
            nManager?.createNotificationChannel(getBirthdaysChannel(context))
        }
        val builder = NotificationCompat.Builder(context, birthdaysChannelId)
            .setSmallIcon(R.drawable.cake)
            .setContentTitle(title)
            .setContentText(body)
            .setColor(toastColor(false))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(Extra.PLACE, getOwnerWallPlace(account_id, user_id, null))
        intent.action = MainActivity.ACTION_OPEN_PLACE
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val contentIntent = PendingIntent.getActivity(
            context,
            user_id,
            intent,
            makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
        )
        builder.setContentIntent(contentIntent)
        val notification = builder.build()
        configOtherPushNotification(notification)
        nManager?.notify(user_id.toString(), NotificationHelper.NOTIFICATION_BIRTHDAY, notification)
    }

    private class BirthdayContext {
        @SerializedName("user_id")
        var user_id = 0
    }

    companion object {
        private val GSON = Gson()

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
        fun fromRemoteMessage(remote: RemoteMessage): BirthdayFCMMessage? {
            val message = BirthdayFCMMessage()
            val data = remote.data
            val context = GSON.fromJson(data["context"], BirthdayContext::class.java)
            message.user_id = context.user_id
            if (context.user_id == 0) {
                return null
            }
            message.body = data["body"]
            message.title = data["title"]
            return message
        }
    }
}