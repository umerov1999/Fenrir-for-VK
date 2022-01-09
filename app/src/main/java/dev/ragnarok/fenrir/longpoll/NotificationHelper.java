package dev.ragnarok.fenrir.longpoll;

import static dev.ragnarok.fenrir.util.Utils.hasFlag;
import static dev.ragnarok.fenrir.util.Utils.hasOreo;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ChatActivityBubbles;
import dev.ragnarok.fenrir.activity.MainActivity;
import dev.ragnarok.fenrir.activity.QuickAnswerActivity;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.push.ChatEntryFetcher;
import dev.ragnarok.fenrir.push.NotificationScheduler;
import dev.ragnarok.fenrir.service.QuickReplyService;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.ShortcutUtils;
import dev.ragnarok.fenrir.util.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotificationHelper {

    public static final int NOTIFICATION_MESSAGE = 62;
    public static final int NOTIFICATION_WALL_POST_ID = 63;
    public static final int NOTIFICATION_REPLY_ID = 64;
    public static final int NOTIFICATION_COMMENT_ID = 65;
    public static final int NOTIFICATION_WALL_PUBLISH_ID = 66;
    public static final int NOTIFICATION_FRIEND_ID = 67;
    public static final int NOTIFICATION_FRIEND_ACCEPTED_ID = 68;
    public static final int NOTIFICATION_GROUP_INVITE_ID = 69;
    public static final int NOTIFICATION_NEW_POSTS_ID = 70;
    public static final int NOTIFICATION_LIKE = 71;
    public static final int NOTIFICATION_BIRTHDAY = 72;
    public static final int NOTIFICATION_UPLOAD = 73;
    public static final int NOTIFICATION_DOWNLOADING = 74;
    public static final int NOTIFICATION_DOWNLOAD = 75;
    public static final int NOTIFICATION_DOWNLOAD_MANAGER = 76;
    public static final int NOTIFICATION_MENTION = 77;
    private static final Object bubbleLock = new Object();
    private static String bubbleOpened;

    public static @Nullable
    String getBubbleOpened() {
        synchronized (bubbleLock) {
            return bubbleOpened;
        }
    }

    public static void setBubbleOpened(int accountId, int peerId) {
        synchronized (bubbleLock) {
            bubbleOpened = createPeerTagFor(accountId, peerId);
        }
    }

    public static void resetBubbleOpened(Context context, boolean force) {
        synchronized (bubbleLock) {
            if (!force) {
                getService(context).cancel(bubbleOpened, NOTIFICATION_MESSAGE);
            }
            bubbleOpened = null;
        }
    }

    /**
     * Отображение уведомления в statusbar о новом сообщении.
     * Этот метод сначала в отдельном потоке получает всю необходимую информацию для отображения
     *
     * @param context контекст
     */

    @SuppressLint("CheckResult")
    public static void notifyNewMessage(Context context, int accountId, Message message) {
        if (Settings.get().other().isDisable_notifications())
            return;
        ChatEntryFetcher.getRx(context, accountId, accountId)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(account -> ChatEntryFetcher.getRx(context, accountId, message.getPeerId())
                        .subscribeOn(NotificationScheduler.INSTANCE)
                        .subscribe(info -> {
                            if (Settings.get().main().isLoad_history_notif()) {
                                Repository.INSTANCE.getMessages().getPeerMessages(accountId, message.getPeerId(), 10, 1, null, false, false)
                                        .subscribeOn(NotificationScheduler.INSTANCE)
                                        .subscribe(history -> doShowNotification(accountId, context, account, info, message, history), t -> doShowNotification(accountId, context, account, info, message, null));
                            } else {
                                doShowNotification(accountId, context, account, info, message, null);
                            }
                        }, e -> doShowNotification(accountId, context, account, onErrorChat(context), message, null)), e -> doShowNotification(accountId, context, onErrorChat(context), onErrorChat(context), message, null));
    }

    private static ChatEntryFetcher.DialogInfo onErrorChat(Context context) {
        ChatEntryFetcher.DialogInfo ret = new ChatEntryFetcher.DialogInfo();
        ret.icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_avatar_unknown);
        ret.title = context.getString(R.string.error);
        return ret;
    }

    private static void doShowNotification(int accountId, Context context, ChatEntryFetcher.DialogInfo account,
                                           ChatEntryFetcher.DialogInfo info, Message message, @Nullable List<Message> history) {
        Peer account_peer = new Peer(accountId).setTitle(account.title).setAvaUrl(account.img);
        Peer peer = new Peer(message.getPeerId()).setTitle(info.title).setAvaUrl(info.img);
        if (info.icon == null) {
            info.icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_avatar_unknown);
        }
        if (account.icon == null) {
            account.icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_avatar_unknown);
        }
        showNotification(context, accountId, peer, message, info.icon, history, account_peer, account.icon);
    }

    private static String getSenderName(Owner own, Context ctx) {
        if (own == null || isEmpty(own.getFullName()))
            return ctx.getString(R.string.error);
        return own.getFullName();
    }

    private static CharSequence getMessageContent(boolean hideBody, Message message, Context context) {
        String messageText = isEmpty(message.getDecryptedBody()) ? message.getBody() : message.getDecryptedBody();
        if (messageText == null)
            messageText = "";

        if (message.getForwardMessagesCount() > 0) {
            messageText += " " + context.getString(R.string.notif_forward, message.getForwardMessagesCount());
        }
        if (message.getAttachments() != null && message.getAttachments().size() > 0) {
            if (!isEmpty(message.getAttachments().getStickers()))
                messageText += " " + context.getString(R.string.notif_sticker);
            else if (!isEmpty(message.getAttachments().getVoiceMessages()))
                messageText += " " + context.getString(R.string.notif_voice);
            else if (!isEmpty(message.getAttachments().getPhotos()))
                messageText += " " + context.getString(R.string.notif_photos, message.getAttachments().getPhotos().size());
            else if (!isEmpty(message.getAttachments().getVideos()))
                messageText += " " + context.getString(R.string.notif_videos, message.getAttachments().getVideos().size());
            else
                messageText += " " + context.getString(R.string.notif_attach, message.getAttachments().size(), context.getString(Utils.declOfNum(message.getAttachments().size(), new int[]{R.string.attachment_notif, R.string.attacment_sec_notif, R.string.attachments_notif})));
        }

        String text = hideBody ? context.getString(R.string.message_text_is_not_available) : messageText;
        return OwnerLinkSpanFactory.withSpans(text, true, false, null);
    }

    private static void MakeMedia(Context context, NotificationCompat.MessagingStyle msgs, Message message, boolean hideBody, int accountId, Bitmap acc_avatar, Bitmap avatar) {
        if (hideBody || Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
            return;

        NotificationCompat.MessagingStyle.Message h_inbox = new NotificationCompat.MessagingStyle.Message(getMessageContent(hideBody, message, context),
                message.getDate() * 1000,
                new Person.Builder()
                        .setName(getSenderName(message.getSender(), context)).setIcon(IconCompat.createWithBitmap(message.getSenderId() == accountId ? acc_avatar : avatar))
                        .setKey(String.valueOf(message.getSenderId())).build());

        if (message.isHasAttachments() && message.getAttachments() != null) {
            if (!isEmpty(message.getAttachments().getStickers())) {
                String url = message.getAttachments().getStickers().get(0).getImageLight(256).getUrl();
                Content cont = doDownloadDataNotification(context, url, "notif_" + accountId + "_" + message.getId());
                if (cont != null) {
                    h_inbox.setData(cont.Mime, cont.uri_data);
                    msgs.addMessage(h_inbox);
                }
            } else if (!isEmpty(message.getAttachments().getPhotos())) {
                String url = message.getAttachments().getPhotos().get(0).getUrlForSize(PhotoSize.X, false);
                Content cont = doDownloadDataNotification(context, url, "notif_" + accountId + "_" + message.getId());
                if (cont != null) {
                    h_inbox.setData(cont.Mime, cont.uri_data);
                    msgs.addMessage(h_inbox);
                }
            } else if (!isEmpty(message.getAttachments().getVideos())) {
                String url = message.getAttachments().getVideos().get(0).getImage();
                Content cont = doDownloadDataNotification(context, url, "notif_" + accountId + "_" + message.getId());
                if (cont != null) {
                    h_inbox.setData(cont.Mime, cont.uri_data);
                    msgs.addMessage(h_inbox);
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private static void showNotification(Context context, int accountId, Peer peer, Message message, Bitmap avatar, @Nullable List<Message> History, Peer ich, Bitmap acc_avatar) {
        boolean hideBody = Settings.get()
                .security()
                .needHideMessagesBodyForNotif();

        CharSequence text = getMessageContent(hideBody, message, context);

        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Objects.isNull(nManager)) {
            return;
        }

        if (hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getChatMessageChannel(context));
            nManager.createNotificationChannel(AppNotificationChannels.getGroupChatMessageChannel(context));
        }

        String channelId = Peer.isGroupChat(message.getPeerId()) ? AppNotificationChannels.getGroupChatMessageChannelId() :
                AppNotificationChannels.getChatMessageChannelId();


        NotificationCompat.MessagingStyle msgs = new NotificationCompat.MessagingStyle(new Person.Builder()
                .setName(ich.getTitle()).setIcon(IconCompat.createWithBitmap(acc_avatar))
                .setKey(String.valueOf(accountId)).build());


        NotificationCompat.MessagingStyle.Message inbox = new NotificationCompat.MessagingStyle.Message(text,
                message.getDate() * 1000,
                new Person.Builder()
                        .setName(getSenderName(message.getSender(), context)).setIcon(IconCompat.createWithBitmap(message.getSenderId() == accountId ? acc_avatar : avatar))
                        .setKey(String.valueOf(message.getSenderId())).build());

        if (History != null) {
            Collections.reverse(History);
            for (Message i : History) {
                NotificationCompat.MessagingStyle.Message h_inbox = new NotificationCompat.MessagingStyle.Message(getMessageContent(hideBody, i, context),
                        i.getDate() * 1000,
                        new Person.Builder()
                                .setName(getSenderName(i.getSender(), context)).setIcon(IconCompat.createWithBitmap(i.getSenderId() == accountId ? acc_avatar : avatar))
                                .setKey(String.valueOf(i.getSenderId())).build());
                MakeMedia(context, msgs, i, hideBody, accountId, acc_avatar, avatar);
                msgs.addMessage(h_inbox);
            }
        }
        MakeMedia(context, msgs, message, hideBody, accountId, acc_avatar, avatar);
        msgs.addMessage(inbox);

        if (message.getPeerId() > VKApiMessage.CHAT_PEER) {
            msgs.setConversationTitle(peer.getTitle());
            msgs.setGroupConversation(Build.VERSION.SDK_INT >= 28);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.client_round)
                .setLargeIcon(avatar)
                .setContentText(text)
                .setStyle(msgs)
                .setColor(ThemesController.INSTANCE.toastColor(false))
                .setWhen(message.getDate() * 1000)
                .setShowWhen(true)
                .setSortKey("" + (Long.MAX_VALUE - message.getDate() * 1000))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true);

        //Our quickreply
        Intent intentQuick = QuickAnswerActivity.forStart(context, accountId, message, text != null ? text.toString() : context.getString(R.string.error), peer.getAvaUrl(), peer.getTitle());
        PendingIntent quickPendingIntent = PendingIntent.getActivity(context, message.getId(), intentQuick, Utils.makeMutablePendingIntent(PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationCompat.Action actionCustomReply = new NotificationCompat.Action(R.drawable.reply, context.getString(R.string.quick_answer_title), quickPendingIntent);

        //System reply. Works only on Wear (20 Api) and N+
        RemoteInput remoteInput = new RemoteInput.Builder(Extra.BODY)
                .setLabel(context.getResources().getString(R.string.reply))
                .build();

        Intent ReadIntent = QuickReplyService.intentForReadMessage(context, accountId, message.getPeerId(), message.getId());

        Intent directIntent = QuickReplyService.intentForAddMessage(context, accountId, message.getPeerId(), message);
        PendingIntent ReadPendingIntent = PendingIntent.getService(context, message.getId(), ReadIntent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_UPDATE_CURRENT));
        PendingIntent directPendingIntent = PendingIntent.getService(context, message.getId(), directIntent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationCompat.Action actionDirectReply = new NotificationCompat.Action.Builder
                (/*may be missing in some cases*/ R.drawable.reply,
                        context.getResources().getString(R.string.reply), directPendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        NotificationCompat.Action actionRead = new NotificationCompat.Action.Builder
                (/*may be missing in some cases*/ R.drawable.view,
                        context.getResources().getString(R.string.read), ReadPendingIntent)
                .build();

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);

        Place chatPlace = PlaceFactory.getChatPlace(accountId, accountId, peer);
        intent.putExtra(Extra.PLACE, chatPlace);

        PendingIntent contentIntent = PendingIntent.getActivity(context, message.getId(), intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);

        if (!Utils.hasNougat()) {
            builder.addAction(actionCustomReply);
        } else {
            builder.addAction(actionDirectReply);
        }

        builder.addAction(actionRead);

        //Для часов игнорирует все остальные action, тем самым убирает QuickReply

        NotificationCompat.WearableExtender War = new NotificationCompat.WearableExtender();
        War.addAction(actionDirectReply);
        War.addAction(actionRead);
        War.setStartScrollBottom(true);

        builder.extend(War);

        if (!hasOreo()) {
            int notificationMask = Settings.get()
                    .notifications()
                    .getNotifPref(accountId, message.getPeerId());

            if (hasFlag(notificationMask, ISettings.INotificationSettings.FLAG_HIGH_PRIORITY)) {
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }

            if (hasFlag(notificationMask, ISettings.INotificationSettings.FLAG_LED)) {
                builder.setLights(0xFF0000FF, 100, 1000);
            }

            if (hasFlag(notificationMask, ISettings.INotificationSettings.FLAG_VIBRO)) {
                builder.setVibrate(Settings.get()
                        .notifications()
                        .getVibrationLength());
            }

            if (hasFlag(notificationMask, ISettings.INotificationSettings.FLAG_SOUND)) {
                builder.setSound(findNotificationSound());
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Peer.isGroupChat(peer.getId()) && Settings.get().main().isNotification_bubbles_enabled()) {
            createNotificationShortcut(context, builder, new Person.Builder()
                    .setName(getSenderName(message.getSender(), context)).setIcon(IconCompat.createWithBitmap(message.getSenderId() == accountId ? acc_avatar : avatar))
                    .setKey(String.valueOf(message.getSenderId())).build(), peer, accountId, message, text);
        }

        nManager.notify(createPeerTagFor(accountId, message.getPeerId()), NOTIFICATION_MESSAGE, builder.build());

        if (Settings.get().notifications().isQuickReplyImmediately()) {
            Intent startQuickReply = QuickAnswerActivity.forStart(context, accountId, message, text != null ? text.toString() : context.getString(R.string.error), peer.getAvaUrl(), peer.getTitle());
            startQuickReply.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startQuickReply.putExtra(QuickAnswerActivity.EXTRA_FOCUS_TO_FIELD, false);
            startQuickReply.putExtra(QuickAnswerActivity.EXTRA_LIVE_DELAY, true);
            context.startActivity(startQuickReply);
        }
    }

    public static void showSimpleNotification(Context context, String body, String title, @Nullable String type, @Nullable String url) {
        boolean hideBody = Settings.get()
                .security()
                .needHideMessagesBodyForNotif();

        String text = hideBody ? context.getString(R.string.message_text_is_not_available) : body;

        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Objects.isNull(nManager)) {
            return;
        }

        if (hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getChatMessageChannel(context));
            nManager.createNotificationChannel(AppNotificationChannels.getGroupChatMessageChannel(context));
        }

        if (!isEmpty(type)) {
            title += ", Type: " + type;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getChatMessageChannelId())
                .setSmallIcon(R.drawable.client_round)
                .setContentText(text)
                .setContentTitle(title)
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        if (!isEmpty(url)) {
            int aid = Settings.get()
                    .accounts()
                    .getCurrent();
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(Extra.PLACE, PlaceFactory.getExternalLinkPlace(aid, url));
            intent.setAction(MainActivity.ACTION_OPEN_PLACE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(context, url.hashCode(), intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
            builder.setContentIntent(contentIntent);
        }
        Notification notification = builder.build();

        nManager.notify("simple " + Settings.get().accounts().getCurrent(), NOTIFICATION_MESSAGE, notification);
    }

    private static String createPeerTagFor(int aid, int peerId) {
        return aid + "_" + peerId;
    }

    public static Uri findNotificationSound() {
        try {
            return Uri.parse(Settings.get()
                    .notifications()
                    .getNotificationRingtone());
        } catch (Exception ignored) {
            return Uri.parse(Settings.get()
                    .notifications()
                    .getDefNotificationRingtone());
        }
    }

    private static NotificationManager getService(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void tryCancelNotificationForPeer(Context context, int accountId, int peerId) {
        //int mask = Settings.get()
        //        .notifications()
        //        .getNotifPref(accountId, peerId);

        //if (hasFlag(mask, ISettings.INotificationSettings.FLAG_SHOW_NOTIF)) {
        String peer = createPeerTagFor(accountId, peerId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || !peer.equals(getBubbleOpened())) {
            getService(context).cancel(peer, NOTIFICATION_MESSAGE);
        }
        //}
    }

    private static Content doDownloadDataNotification(Context mContext, String url, String prefix) {
        File cache = new File(mContext.getCacheDir(), "notif-cache");

        String MimeType = "image/jpeg";
        Uri urit;
        if (!cache.exists()) {
            cache.mkdirs();
        }
        File file = new File(cache.getAbsolutePath(), prefix);
        try {
            if (!file.exists()) {
                if (url == null || url.isEmpty())
                    throw new Exception(mContext.getString(R.string.null_image_link));
                OutputStream output = new FileOutputStream(file);

                OkHttpClient.Builder builder = Utils.createOkHttp(5);
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = builder.build().newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new Exception("Server return " + response.code() +
                            " " + response.message());
                }
                InputStream is = java.util.Objects.requireNonNull(response.body()).byteStream();
                BufferedInputStream input = new BufferedInputStream(is);
                byte[] data = new byte[80 * 1024];
                int bufferLength;

                while ((bufferLength = input.read(data)) != -1) {
                    output.write(data, 0, bufferLength);
                }
                //MimeType = response.header("Content-Type", "image/jpeg");

                output.flush();
                input.close();
            }
            urit = FileProvider.getUriForFile(mContext, Constants.FILE_PROVIDER_AUTHORITY, file);
            mContext.grantUriPermission("com.android.systemui", urit, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (urit != null)
            return new Content(MimeType, urit);
        return null;
    }

    @SuppressLint("RestrictedApi")
    private static void createNotificationShortcut(Context context, NotificationCompat.Builder builder, Person person, Peer peer, int accountId, Message message, CharSequence text) {
        try {
            String person_name = person.getName() != null ? person.getName().toString() : context.getString(R.string.error);
            String id = "fenrir_peer_" + peer.getId() + "_aid_" + accountId;
            ShortcutInfoCompat.Builder shortcutBuilder = new ShortcutInfoCompat.Builder(context, id)
                    .setShortLabel(person_name.trim())
                    .setLongLabel(person_name)
                    .setIntent(ShortcutUtils.chatOpenIntent(context, peer.getAvaUrl(), accountId, peer.getId(), peer.getTitle()))
                    .setLongLived(true);

            Bitmap avatar;
            shortcutBuilder.setPerson(person);
            shortcutBuilder.setIcon(person.getIcon());
            if (person.getIcon() != null) {
                avatar = person.getIcon().getBitmap();
            } else {
                avatar = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_avatar_unknown);
            }
            ArrayList<ShortcutInfoCompat> arrayList = new ArrayList<>(1);
            arrayList.add(shortcutBuilder.build());
            ShortcutManagerCompat.addDynamicShortcuts(context, arrayList);
            builder.setShortcutId(id);

            Intent intentQuick = ChatActivityBubbles.forStart(context, accountId, peer);
            PendingIntent bubbleIntent = PendingIntent.getActivity(context, 0, intentQuick, Utils.makeMutablePendingIntent(PendingIntent.FLAG_UPDATE_CURRENT));
            assert avatar != null;
            NotificationCompat.BubbleMetadata.Builder bubbleBuilder = new NotificationCompat.BubbleMetadata.Builder(bubbleIntent, IconCompat.createWithAdaptiveBitmap(avatar));

            bubbleBuilder.setSuppressNotification(true);
            bubbleBuilder.setAutoExpandBubble(false);
            bubbleBuilder.setDesiredHeight(640);
            builder.setBubbleMetadata(bubbleBuilder.build());
        } catch (Exception e) {
            //FileLog.e(e);
        }
    }

    private static class Content {
        public String Mime;
        public Uri uri_data;

        Content(String Mime, Uri uri_data) {
            this.Mime = Mime;
            this.uri_data = uri_data;
        }
    }
}
