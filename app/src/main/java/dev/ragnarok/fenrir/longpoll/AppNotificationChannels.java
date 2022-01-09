package dev.ragnarok.fenrir.longpoll;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.settings.Settings;

public class AppNotificationChannels {
    public static final String KEY_EXCHANGE_CHANNEL_ID = "key_exchange_channel";
    public static final String AUDIO_CHANNEL_ID = "audio_channel";
    public static final String DOWNLOAD_CHANNEL_ID = "download_channel";
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static final AudioAttributes ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
            .build();

    public static String getChatMessageChannelId() {
        return makeChannelId("chat_message_channel");
    }

    public static String getGroupChatMessageChannelId() {
        return makeChannelId("group_chat_message_channel");
    }

    public static String getLikesChannelId() {
        return makeChannelId("likes_channel");
    }

    public static String getCommentsChannelId() {
        return makeChannelId("comments_channel");
    }

    public static String getNewPostChannelId() {
        return makeChannelId("new_post_channel");
    }

    public static String getMentionChannelId() {
        return makeChannelId("mention_channel");
    }

    public static String getGroupInvitesChannelId() {
        return makeChannelId("group_invites_channel");
    }

    public static String getFriendRequestsChannelId() {
        return makeChannelId("friend_requests_channel");
    }

    public static String getBirthdaysChannelId() {
        return makeChannelId("birthdays_channel");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getChatMessageChannel(Context context) {
        String channelName = context.getString(R.string.message_channel);

        NotificationChannel channel = new NotificationChannel(getChatMessageChannelId(), channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(NotificationHelper.findNotificationSound(), ATTRIBUTES);
        channel.enableLights(true);
        channel.enableVibration(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            channel.setAllowBubbles(true);
        }
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getGroupChatMessageChannel(Context context) {
        String channelName = context.getString(R.string.group_message_channel);
        NotificationChannel channel = new NotificationChannel(getGroupChatMessageChannelId(), channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(NotificationHelper.findNotificationSound(), ATTRIBUTES);
        channel.enableLights(true);
        channel.enableVibration(true);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getKeyExchangeChannel(Context context) {
        String channelName = context.getString(R.string.key_exchange_channel);
        NotificationChannel channel = new NotificationChannel(KEY_EXCHANGE_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(false);
        channel.enableVibration(false);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getLikesChannel(Context context) {
        String channelName = context.getString(R.string.likes_channel);
        NotificationChannel channel = new NotificationChannel(getLikesChannelId(), channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.setSound(Settings.get().notifications().getFeedbackRingtoneUri(), ATTRIBUTES);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getAudioChannel(Context context) {
        String channelName = context.getString(R.string.audio_channel);
        NotificationChannel channel = new NotificationChannel(AUDIO_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(false);
        channel.enableVibration(false);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getCommentsChannel(Context context) {
        String channelName = context.getString(R.string.comment_channel);
        NotificationChannel channel = new NotificationChannel(getCommentsChannelId(), channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setSound(Settings.get().notifications().getFeedbackRingtoneUri(), ATTRIBUTES);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getNewPostChannel(Context context) {
        String channelName = context.getString(R.string.new_posts_channel);
        NotificationChannel channel = new NotificationChannel(getNewPostChannelId(), channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setSound(Settings.get().notifications().getNewPostRingtoneUri(), ATTRIBUTES);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getMentionChannel(Context context) {
        String channelName = context.getString(R.string.mentions);
        NotificationChannel channel = new NotificationChannel(getMentionChannelId(), channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setSound(Settings.get().notifications().getFeedbackRingtoneUri(), ATTRIBUTES);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getDownloadChannel(Context context) {
        String channelName = context.getString(R.string.downloading);
        NotificationChannel channel = new NotificationChannel(DOWNLOAD_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.enableVibration(false);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getGroupInvitesChannel(Context context) {
        String channelName = context.getString(R.string.group_invites_channel);
        NotificationChannel channel = new NotificationChannel(getGroupInvitesChannelId(), channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setSound(Settings.get().notifications().getFeedbackRingtoneUri(), ATTRIBUTES);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getFriendRequestsChannel(Context context) {
        String channelName = context.getString(R.string.friend_requests_channel);
        NotificationChannel channel = new NotificationChannel(getFriendRequestsChannelId(), channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setSound(Settings.get().notifications().getFeedbackRingtoneUri(), ATTRIBUTES);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getBirthdaysChannel(Context context) {
        String channelName = context.getString(R.string.birthdays);
        NotificationChannel channel = new NotificationChannel(getGroupInvitesChannelId(), channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setSound(Settings.get().notifications().getFeedbackRingtoneUri(), ATTRIBUTES);
        return channel;
    }

    private static @NonNull
    String makeChannelId(@NonNull String id) {
        int ch = Settings.get().other().getCustomChannelNotif();
        if (ch == 0) {
            return id;
        }
        return id + "_" + ch;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void invalidateSoundChannels(Context context) {
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int ch = Settings.get().other().getCustomChannelNotif();

        nManager.deleteNotificationChannel(getChatMessageChannelId());
        nManager.deleteNotificationChannel(getGroupChatMessageChannelId());
        nManager.deleteNotificationChannel(getLikesChannelId());
        nManager.deleteNotificationChannel(getCommentsChannelId());
        nManager.deleteNotificationChannel(getNewPostChannelId());
        nManager.deleteNotificationChannel(getMentionChannelId());
        nManager.deleteNotificationChannel(getGroupInvitesChannelId());
        nManager.deleteNotificationChannel(getFriendRequestsChannelId());
        nManager.deleteNotificationChannel(getBirthdaysChannelId());

        Settings.get().other().nextCustomChannelNotif();
    }
}
