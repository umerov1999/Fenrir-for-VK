package dev.ragnarok.fenrir.model.menu.options;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({CommentsPhotoOption.go_to_photo_item_comment,
        CommentsPhotoOption.copy_item_comment,
        CommentsPhotoOption.report_item_comment,
        CommentsPhotoOption.like_item_comment,
        CommentsPhotoOption.dislike_item_comment,
        CommentsPhotoOption.who_like_item_comment,
        CommentsPhotoOption.send_to_friend_item_comment})
@Retention(RetentionPolicy.SOURCE)
public @interface CommentsPhotoOption {
    int go_to_photo_item_comment = 1;
    int copy_item_comment = 2;
    int report_item_comment = 3;
    int like_item_comment = 4;
    int dislike_item_comment = 5;
    int who_like_item_comment = 6;
    int send_to_friend_item_comment = 7;
}
