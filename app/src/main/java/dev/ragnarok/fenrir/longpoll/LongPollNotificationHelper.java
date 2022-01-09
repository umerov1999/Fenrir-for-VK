package dev.ragnarok.fenrir.longpoll;

import static dev.ragnarok.fenrir.util.Utils.hasFlag;

import android.content.Context;

import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Logger;

public class LongPollNotificationHelper {

    public static final String TAG = LongPollNotificationHelper.class.getSimpleName();

    /**
     * Действие при добавлении нового сообщения в диалог или чат
     *
     * @param message нотификация с сервера
     */
    public static void notifyAbountNewMessage(Context context, Message message) {
        if (message.isOut()) {
            return;
        }

        //if (message.isRead()) {
        //    return;
        //}

        //boolean needSendNotif = needNofinicationFor(message.getAccountId(), message.getPeerId());
        //if(!needSendNotif){
        //    return;
        //}

        notifyAbountNewMessage(context, message.getAccountId(), message);
    }

    private static void notifyAbountNewMessage(Context context, int accountId, Message message) {
        int mask = Settings.get().notifications().getNotifPref(accountId, message.getPeerId());
        if (!hasFlag(mask, ISettings.INotificationSettings.FLAG_SHOW_NOTIF)) {
            return;
        }

        if (Settings.get().accounts().getCurrent() != accountId) {
            Logger.d(TAG, "notifyAbountNewMessage, Attempting to send a notification does not in the current account!!!");
            return;
        }

        NotificationHelper.notifyNewMessage(context, accountId, message);
    }
}