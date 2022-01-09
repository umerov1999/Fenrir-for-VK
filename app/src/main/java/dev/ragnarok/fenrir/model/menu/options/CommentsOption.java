package dev.ragnarok.fenrir.model.menu.options;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({CommentsOption.copy_item_comment,
        CommentsOption.reply_item_comment,
        CommentsOption.report_item_comment,
        CommentsOption.delete_item_comment,
        CommentsOption.edit_item_comment,
        CommentsOption.block_author_item_comment,
        CommentsOption.like_item_comment,
        CommentsOption.dislike_item_comment,
        CommentsOption.who_like_item_comment,
        CommentsOption.send_to_friend_item_comment})
@Retention(RetentionPolicy.SOURCE)
public @interface CommentsOption {
    int copy_item_comment = 1;
    int reply_item_comment = 2;
    int report_item_comment = 3;
    int delete_item_comment = 4;
    int edit_item_comment = 5;
    int block_author_item_comment = 6;
    int like_item_comment = 7;
    int dislike_item_comment = 8;
    int who_like_item_comment = 9;
    int send_to_friend_item_comment = 10;
}
