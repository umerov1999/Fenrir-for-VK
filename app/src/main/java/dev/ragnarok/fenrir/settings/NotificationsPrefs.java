package dev.ragnarok.fenrir.settings;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.hasFlag;
import static dev.ragnarok.fenrir.util.Utils.removeFlag;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Peer;

public class NotificationsPrefs implements ISettings.INotificationSettings {

    private static final String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
    private static final String KEY_VIBRO_LENGTH = "vibration_length";
    private static final String KEY_PEERS_UIDS = "notif_peer_uids";

    private final Context app;
    private final Set<String> notification_peers;
    private final Map<String, Integer> types;

    NotificationsPrefs(Context context) {
        app = context.getApplicationContext();

        notification_peers = Collections.synchronizedSet(new HashSet<>(1));
        types = Collections.synchronizedMap(new HashMap<>(1));
        reloadNotifSettings(false);
    }

    private static String keyFor(int aid, int peerId) {
        return "notif_peer_" + aid + "_" + peerId;
    }

    private static String keyForAccount(int aid) {
        return "notif_peer_" + aid;
    }

    @NonNull
    @Override
    public Map<String, Integer> getChatsNotif() {
        return new HashMap<>(types);
    }

    @NonNull
    @Override
    public Set<String> getChatsNotifKeys() {
        return new HashSet<>(notification_peers);
    }

    @Override
    public void reloadNotifSettings(boolean onlyRoot) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        notification_peers.clear();
        notification_peers.addAll(preferences.getStringSet(KEY_PEERS_UIDS, new HashSet<>(1)));
        if (onlyRoot) {
            return;
        }
        types.clear();
        for (String i : notification_peers) {
            types.put(i, preferences.getInt(i, getGlobalNotifPref(true)));
        }
    }

    @Override
    public void setNotifPref(int aid, int peerid, int mask) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        notification_peers.add(keyFor(aid, peerid));
        types.put(keyFor(aid, peerid), mask);
        preferences.edit()
                .putInt(keyFor(aid, peerid), mask)
                .putStringSet(KEY_PEERS_UIDS, notification_peers)
                .apply();
    }

    private boolean isOtherNotificationsEnable() {
        return hasFlag(getOtherNotificationMask(), FLAG_SHOW_NOTIF);
    }

    @Override
    public int getOtherNotificationMask() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        int mask = 0;
        if (preferences.getBoolean("other_notifications_enable", true)) {
            mask = mask + FLAG_SHOW_NOTIF;
        }

        if (preferences.getBoolean("other_notif_sound", true)) {
            mask = mask + FLAG_SOUND;
        }

        if (preferences.getBoolean("other_notif_vibration", true)) {
            mask = mask + FLAG_VIBRO;
        }

        if (preferences.getBoolean("other_notif_led", true)) {
            mask = mask + FLAG_LED;
        }
        return mask;
    }

    @Override
    public boolean isCommentsNotificationsEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("new_comment_notification", true);
    }

    @Override
    public boolean isFriendRequestAcceptationNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("friend_request_accepted_notification", true);
    }

    @Override
    public boolean isNewFollowerNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("new_follower_notification", true);
    }

    @Override
    public boolean isWallPublishNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("wall_publish_notification", true);
    }

    @Override
    public boolean isGroupInvitedNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("group_invited_notification", true);
    }

    @Override
    public boolean isReplyNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("reply_notification", true);
    }

    @Override
    public boolean isNewPostOnOwnWallNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("new_wall_post_notification", true);
    }

    @Override
    public boolean isNewPostsNotificationEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("new_posts_notification", true);
    }

    @Override
    public boolean isBirthdayNotifyEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("birtday_notification", true);
    }

    @Override
    public boolean isMentionNotifyEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("mention_notification", true);
    }

    @Override
    public boolean isSilentChat(int aid, int peerId) {
        if (types.containsKey(keyFor(aid, peerId))) {
            Integer v = types.get(keyFor(aid, peerId));
            if (nonNull(v)) {
                return !hasFlag(v, FLAG_SHOW_NOTIF);
            }
        }
        return false;
    }

    @Override
    public boolean isLikeNotificationEnable() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("likes_notification", true);
    }

    @Override
    public Uri getFeedbackRingtoneUri() {
        String path = "android.resource://" + app.getPackageName() + "/" + R.raw.feedback_sound;
        return Uri.parse(path);
    }

    @Override
    public Uri getNewPostRingtoneUri() {
        String path = "android.resource://" + app.getPackageName() + "/" + R.raw.new_post_sound;
        return Uri.parse(path);
    }

    @Override
    public String getDefNotificationRingtone() {
        return "android.resource://" + app.getPackageName() + "/" + R.raw.notification_sound;
    }

    @Override
    public String getNotificationRingtone() {
        return PreferenceManager.getDefaultSharedPreferences(app)
                .getString(KEY_NOTIFICATION_RINGTONE, getDefNotificationRingtone());
    }

    @Override
    public void setNotificationRingtoneUri(String path) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putString(KEY_NOTIFICATION_RINGTONE, path)
                .apply();
    }

    @Override
    public long[] getVibrationLength() {
        switch (PreferenceManager.getDefaultSharedPreferences(app)
                .getString(KEY_VIBRO_LENGTH, "4")) {
            case "0":
                return new long[]{0, 300};
            case "1":
                return new long[]{0, 400};
            case "2":
                return new long[]{0, 500};
            case "3":
                return new long[]{0, 300, 250, 300};
            case "5":
                return new long[]{0, 500, 250, 500};
            default:
                return new long[]{0, 400, 250, 400};
        }
    }

    @Override
    public boolean isQuickReplyImmediately() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("quick_reply_immediately", false);
    }

    @Override
    public void forceDisable(int aid, int peerId) {
        int mask = getGlobalNotifPref(Peer.isGroupChat(peerId));
        if (hasFlag(mask, FLAG_SHOW_NOTIF)) {
            mask = removeFlag(mask, FLAG_SHOW_NOTIF);
        }
        setNotifPref(aid, peerId, mask);
    }

    @Override
    public void setDefault(int aid, int peerId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        notification_peers.remove(keyFor(aid, peerId));
        types.remove(keyFor(aid, peerId));
        preferences.edit()
                .remove(keyFor(aid, peerId))
                .putStringSet(KEY_PEERS_UIDS, notification_peers)
                .apply();
    }

    @Override
    public void resetAccount(int aid) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        for (String i : new HashSet<>(notification_peers)) {
            if (i.contains(keyForAccount(aid))) {
                notification_peers.remove(i);
                types.remove(i);
                preferences.edit().remove(i).apply();
            }
        }
        preferences.edit()
                .putStringSet(KEY_PEERS_UIDS, notification_peers)
                .apply();
    }

    @Override
    public int getNotifPref(int aid, int peerid) {
        if (types.containsKey(keyFor(aid, peerid))) {
            Integer v = types.get(keyFor(aid, peerid));
            if (nonNull(v)) {
                return v;
            }
        }
        return getGlobalNotifPref(Peer.isGroupChat(peerid));
    }

    private int getGlobalNotifPref(boolean isGroup) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
        int value = sharedPreferences.getBoolean("high_notif_priority", false) ? FLAG_HIGH_PRIORITY : 0;

        if (!isGroup) {
            if (sharedPreferences.getBoolean("new_dialog_message_notif_enable", true)) {
                value += FLAG_SHOW_NOTIF;
            }

            if (sharedPreferences.getBoolean("new_dialog_message_notif_sound", true)) {
                value += FLAG_SOUND;
            }

            if (sharedPreferences.getBoolean("new_dialog_message_notif_vibration", true)) {
                value += FLAG_VIBRO;
            }

            if (sharedPreferences.getBoolean("new_dialog_message_notif_led", true)) {
                value += FLAG_LED;
            }
        } else {
            if (sharedPreferences.getBoolean("new_groupchat_message_notif_enable", true)) {
                value += FLAG_SHOW_NOTIF;
            }

            if (sharedPreferences.getBoolean("new_groupchat_message_notif_sound", true)) {
                value += FLAG_SOUND;
            }

            if (sharedPreferences.getBoolean("new_groupchat_message_notif_vibration", true)) {
                value += FLAG_VIBRO;
            }

            if (sharedPreferences.getBoolean("new_groupchat_message_notif_led", true)) {
                value += FLAG_LED;
            }
        }
        return value;
    }
}
