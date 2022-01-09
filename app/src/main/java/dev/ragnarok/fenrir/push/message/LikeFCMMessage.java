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

import java.util.ArrayList;
import java.util.Map;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.MainActivity;
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels;
import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentedType;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class LikeFCMMessage {

// key: id, value: like_216143660_photo280186075_456239045, class: class java.lang.String
// key: url, value: https://vk.com/photo280186075_456239045?access_key=d7d37c46854499dd3f, class: class java.lang.String
// key: body, value: Umerov Artem liked your photo, class: class java.lang.String
// key: icon, value: like_24, class: class java.lang.String
// key: time, value: 1529682146, class: class java.lang.String
// key: type, value: like, class: class java.lang.String
// key: badge, value: 1, class: class java.lang.String
// key: image, value: [{"width":200,"url":"https:\/\/pp.userapi.com\/c844520\/v844520706\/71a39\/nc5YPeh1yEI.jpg","height":200},{"width":100,"url":"https:\/\/pp.userapi.com\/c844520\/v844520706\/71a3a\/pZLtq6sleHo.jpg","height":100},{"width":50,"url":"https:\/\/pp.userapi.com\/c844520\/v844520706\/71a3b\/qoFJrYXVFdc.jpg","height":50}], class: class java.lang.String
// key: sound, value: 0, class: class java.lang.String
// key: title, value: Notification, class: class java.lang.String
// key: to_id, value: 280186075, class: class java.lang.String
// key: group_id, value: likes, class: class java.lang.String
// key: context, value: {"feedback":true,"item_id":"456239045","owner_id":"280186075","type":"photo"}, class: class java.lang.String

    private final int accountId;
    private final String id;
    private final String title;
    private final int from_id;
    private final int badge;
    private final int item_id;
    private final int owner_id;
    private final String like_type;
    private final int reply_id;

    public LikeFCMMessage(int accountId, RemoteMessage remote) {
        this.accountId = accountId;
        Map<String, String> data = remote.getData();
        long from = Long.parseLong(remote.getFrom());
        id = data.get("id");
        String url = data.get("url");
        long time = Long.parseLong(data.get("time"));
        boolean sound = Integer.parseInt(data.get("sound")) == 1;
        title = data.get("title");
        from_id = Integer.parseInt("from_id");
        String body = data.get("body");
        badge = Integer.parseInt(data.get("badge"));
        int to_id = Integer.parseInt(data.get("to_id"));
        String group_id = data.get("group_id");

        LikeContext context = new Gson().fromJson(data.get("context"), LikeContext.class);

        boolean is_feedback = context.feedback == 1;
        item_id = context.item_id;
        owner_id = context.owner_id;
        like_type = context.type;
        reply_id = context.reply_id;
    }

    //todo implement place
    private void notifyImpl(Context context) {
        Place place = null;

        if ("post".equals(like_type)) {
            place = PlaceFactory.getPostPreviewPlace(accountId, item_id, owner_id, null);
        } else if ("photo".equals(like_type)) {
            ArrayList<Photo> photos = Utils.singletonArrayList(
                    new Photo().setId(item_id).setOwnerId(owner_id)
            );

            place = PlaceFactory.getSimpleGalleryPlace(accountId, photos, 0, true);
        } else if ("video".equals(like_type)) {
            place = PlaceFactory.getVideoPreviewPlace(accountId, owner_id, item_id, null, null);
        } else if ("post_comment".equals(like_type)) {
            Commented commented = new Commented(item_id, owner_id, CommentedType.POST, null);
            place = PlaceFactory.getCommentsPlace(accountId, commented, reply_id);
        } else if ("photo_comment".equals(like_type)) {
            Commented commented = new Commented(item_id, owner_id, CommentedType.PHOTO, null);
            place = PlaceFactory.getCommentsPlace(accountId, commented, reply_id);
        } else if ("video_comment".equals(like_type)) {
            Commented commented = new Commented(item_id, owner_id, CommentedType.VIDEO, null);
            place = PlaceFactory.getCommentsPlace(accountId, commented, reply_id);
        }

        if (place == null) {
            return;
        }

//        VkPlace parsedPlace = VkPlace.parse(object);
//
//        if (isNull(parsedPlace)) {
//            PersistentLogger.logThrowable("Push issues", new Exception("LikeFCMMessage, UNKNOWN OBJECT: " + object));
//            return;
//        }
//
//        String userName = (stringEmptyIfNull(firstName) + " " + stringEmptyIfNull(lastName)).trim();
//
//        Place place = null;
//        String contentText = null;
//
//        if (parsedPlace instanceof VkPlace.Photo) {
//            VkPlace.Photo photo = (VkPlace.Photo) parsedPlace;
//
//            ArrayList<Photo> photos = singletonArrayList(
//                    new Photo().setId(photo.getPhotoId()).setOwnerId(photo.getOwnerId())
//            );
//
//            place = PlaceFactory.getSimpleGalleryPlace(accountId, photos, 0, true);
//            contentText = context.getString(R.string.push_user_liked_your_photo, userName);
//        } else if (parsedPlace instanceof VkPlace.PhotoComment) {
//            VkPlace.PhotoComment photoComment = (VkPlace.PhotoComment) parsedPlace;
//            Commented commented = new Commented(photoComment.getPhotoId(), photoComment.getOwnerId(), CommentedType.PHOTO, null);
//            place = PlaceFactory.getCommentsPlace(accountId, commented, replyId);
//            contentText = context.getString(R.string.push_user_liked_your_comment_on_the_photo, userName);
//        } else if (parsedPlace instanceof VkPlace.Video) {
//            VkPlace.Video video = (VkPlace.Video) parsedPlace;
//            place = PlaceFactory.getVideoPreviewPlace(accountId, video.getOwnerId(), video.getVideoId(), null);
//            contentText = context.getString(R.string.push_user_liked_your_video, userName);
//        } else if (parsedPlace instanceof VkPlace.VideoComment) {
//            VkPlace.VideoComment videoComment = (VkPlace.VideoComment) parsedPlace;
//            Commented commented = new Commented(videoComment.getVideoId(), videoComment.getOwnerId(), CommentedType.VIDEO, null);
//            place = PlaceFactory.getCommentsPlace(accountId, commented, replyId);
//            contentText = context.getString(R.string.push_user_liked_your_comment_on_the_video, userName);
//        } else if (parsedPlace instanceof VkPlace.WallPost) {
//            VkPlace.WallPost wallPost = (VkPlace.WallPost) parsedPlace;
//            place = PlaceFactory.getPostPreviewPlace(accountId, wallPost.getPostId(), wallPost.getOwnerId(), null);
//            contentText = context.getString(R.string.push_user_liked_your_post, userName);
//        } else if (parsedPlace instanceof VkPlace.WallComment) {
//            VkPlace.WallComment wallComment = (VkPlace.WallComment) parsedPlace;
//            Commented commented = new Commented(wallComment.getPostId(), wallComment.getOwnerId(), CommentedType.POST, null);
//            place = PlaceFactory.getCommentsPlace(accountId, commented, replyId);
//            contentText = context.getString(R.string.push_user_liked_your_comment_on_the_post, userName);
//        }
//
//        if (isNull(place)) {
//            PersistentLogger.logThrowable("Push issues", new Exception("LikeFCMMessage, UNKNOWN PLACE: " + object));
//            return;
//        }

        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()) {
            nManager.createNotificationChannel(AppNotificationChannels.getLikesChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.getLikesChannelId())
                .setSmallIcon(R.drawable.heart)
                .setContentTitle(context.getString(R.string.like_title))
                .setContentText(title)
                .setNumber(badge)
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, place);
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, from_id, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();

        configOtherPushNotification(notification);

        nManager.notify(id, NotificationHelper.NOTIFICATION_LIKE, notification);
    }

    public void notifyIfNeed(Context context) {
        if (!Settings.get()
                .notifications()
                .isLikeNotificationEnable()) {
            return;
        }

        notifyImpl(context);
    }

    static class LikeContext {
        @SerializedName("feedback")
        int feedback;

        @SerializedName("item_id")
        int item_id;

        @SerializedName("owner_id")
        int owner_id;

        @SerializedName("type")
        String type;

        @SerializedName("reply_id")
        int reply_id;
    }
}
