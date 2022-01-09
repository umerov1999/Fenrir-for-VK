package dev.ragnarok.fenrir;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.push.IPushRegistrationResolver;
import dev.ragnarok.fenrir.push.PushType;
import dev.ragnarok.fenrir.push.message.BirthdayFCMMessage;
import dev.ragnarok.fenrir.push.message.CommentFCMMessage;
import dev.ragnarok.fenrir.push.message.FCMMessage;
import dev.ragnarok.fenrir.push.message.FriendAcceptedFCMMessage;
import dev.ragnarok.fenrir.push.message.FriendFCMMessage;
import dev.ragnarok.fenrir.push.message.GroupInviteFCMMessage;
import dev.ragnarok.fenrir.push.message.LikeFCMMessage;
import dev.ragnarok.fenrir.push.message.MentionMessage;
import dev.ragnarok.fenrir.push.message.NewPostPushMessage;
import dev.ragnarok.fenrir.push.message.ReplyFCMMessage;
import dev.ragnarok.fenrir.push.message.WallPostFCMMessage;
import dev.ragnarok.fenrir.push.message.WallPublishFCMMessage;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.PersistentLogger;
import dev.ragnarok.fenrir.util.RxUtils;

public class FcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "FcmListenerService";

    @SuppressLint("CheckResult")
    @WorkerThread
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Injection.providePushRegistrationResolver()
                .resolvePushRegistration()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), RxUtils.ignore());
    }

    @Override
    @WorkerThread
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Context context = getApplicationContext();
        String pushType = message.getData().get("type");

        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        if (accountId == ISettings.IAccountsSettings.INVALID_ID || isEmpty(pushType) || Settings.get().other().isSettings_no_push()) {
            return;
        }

        IPushRegistrationResolver registrationResolver = Injection.providePushRegistrationResolver();

        if (!registrationResolver.canReceivePushNotification()) {
            Logger.d(TAG, "Invalid push registration on VK");
            return;
        }

        if (Settings.get().other().isDump_fcm() && !PushType.ERASE.equals(pushType)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            if (Constants.IS_DEBUG) {
                Logger.d(TAG, "onMessage, from: " + message.getFrom() + ", pushType: " + pushType + ", data: " + gson.toJson(message.getData()));
            }
            PersistentLogger.logThrowable("Push received", new Exception("Key: " + pushType + ", Dump: " + gson.toJson(message.getData())));
        }

        try {
            switch (pushType) {
                case PushType.MSG:
                case PushType.CHAT:
                    FCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.POST:
                    WallPostFCMMessage.fromRemoteMessage(message).nofify(context, accountId);
                    break;
                case PushType.COMMENT:
                    CommentFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.FRIEND:
                    FriendFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.NEW_POST:
                    new NewPostPushMessage(accountId, message).notifyIfNeed(context);
                    break;
                case PushType.LIKE:
                    new LikeFCMMessage(accountId, message).notifyIfNeed(context);
                    break;
                case PushType.REPLY:
                    ReplyFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.WALL_PUBLISH:
                    WallPublishFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.FRIEND_ACCEPTED:
                    FriendAcceptedFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.GROUP_INVITE:
                    GroupInviteFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.BIRTHDAY:
                    BirthdayFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.VALIDATE_DEVICE:
                    NotificationHelper.showSimpleNotification(context, message.getData().get("body"), message.getData().get("title"), null, message.getData().get("url"));
                    break;
                case PushType.SHOW_MESSAGE:
                    NotificationHelper.showSimpleNotification(context, message.getData().get("body"), message.getData().get("title"), null, null);
                    break;
                case PushType.MENTION:
                    MentionMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            PersistentLogger.logThrowable("Push issues", e);
            e.printStackTrace();
        }
    }
}