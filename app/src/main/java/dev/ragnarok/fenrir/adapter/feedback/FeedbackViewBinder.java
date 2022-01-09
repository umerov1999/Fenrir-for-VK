package dev.ragnarok.fenrir.adapter.feedback;

import static dev.ragnarok.fenrir.util.AppTextUtils.getDateFromUnixTime;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.squareup.picasso3.Transformation;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.AttachmentsHolder;
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Topic;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.feedback.CommentFeedback;
import dev.ragnarok.fenrir.model.feedback.CopyFeedback;
import dev.ragnarok.fenrir.model.feedback.LikeCommentFeedback;
import dev.ragnarok.fenrir.model.feedback.LikeFeedback;
import dev.ragnarok.fenrir.model.feedback.MentionCommentFeedback;
import dev.ragnarok.fenrir.model.feedback.MentionFeedback;
import dev.ragnarok.fenrir.model.feedback.PostPublishFeedback;
import dev.ragnarok.fenrir.model.feedback.ReplyCommentFeedback;
import dev.ragnarok.fenrir.model.feedback.UsersFeedback;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.ViewUtils;

public class FeedbackViewBinder {

    private static final String SPACE = " ";
    private final Activity context;
    private final Transformation transformation;
    private final int linkColor;
    private final AttachmentsViewBinder attachmentsViewBinder;
    private final AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback;
    private final LinkActionAdapter mLinkActionAdapter;

    public FeedbackViewBinder(Activity context, AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback) {
        this.context = context;
        transformation = CurrentTheme.createTransformationForAvatar();
        linkColor = new TextView(context).getLinkTextColors().getDefaultColor();
        attachmentsViewBinder = new AttachmentsViewBinder(context, attachmentsActionCallback);
        this.attachmentsActionCallback = attachmentsActionCallback;
        mLinkActionAdapter = new LinkActionAdapter() {
            @Override
            public void onOwnerClick(int ownerId) {
                openOwner(ownerId);
            }
        };
    }

    /**
     * Настройка отображения уведомления типа "mention_comment_video"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configMentionCommentsVideoFeedback(MentionCommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Comment feedback = notification.getWhere();

        Spannable spannable = OwnerLinkSpanFactory.withSpans(feedback.getText(), true, false, mLinkActionAdapter);

        holder.cOwnerName.setText(feedback.getFullAuthorName());
        holder.cOwnerText.setText(spannable, TextView.BufferType.SPANNABLE);

        String action = getDateFromUnixTime(notification.getDate());

        action = action + SPACE + context.getString(R.string.mentioned_in_comment_photo_video);
        Link actionLink = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.video_dative);
        actionLink.end(action.length());

        Spannable actionSpannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(actionSpannable, actionLink);

        holder.cOwnerTime.setText(actionSpannable, TextView.BufferType.SPANNABLE);
        holder.cChangable.setVisibility(View.GONE);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        setupAttachmentViewWithVideo((Video) notification.getCommentOf(), holder.ivRightAttachment);

        showUserAvatarOnImageView(feedback.getMaxAuthorAvaUrl(), holder.cOwnerAvatar);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());

        configReply(notification.getReply(), holder);
    }

    private void setupAttachmentViewWithVideo(Video video, ImageView imageView) {
        if (video == null || TextUtils.isEmpty(video.getImage())) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            PicassoInstance.with()
                    .load(video.getImage())
                    .tag(Constants.PICASSO_TAG)
                    .into(imageView);
        }
    }

    private void setupAttachmentViewWithPhoto(Photo photo, ImageView imageView) {
        String photoUrl = Objects.isNull(photo) ? null : photo.getUrlForSize(PhotoSize.X, false);

        if (TextUtils.isEmpty(photoUrl)) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);

            PicassoInstance.with()
                    .load(photoUrl)
                    .tag(Constants.PICASSO_TAG)
                    .into(imageView);
        }
    }

    /**
     * Настройка отображения уведомления типа "mention_comment_photo"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configMentionCommentsPhotoFeedback(MentionCommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Comment feedback = notification.getWhere();

        Spannable spannable = OwnerLinkSpanFactory.withSpans(feedback.getText(), true, false, mLinkActionAdapter);

        holder.cOwnerName.setText(feedback.getFullAuthorName());
        holder.cOwnerText.setText(spannable, TextView.BufferType.SPANNABLE);

        String action = getDateFromUnixTime(notification.getDate());
        action = action + SPACE + context.getString(R.string.mentioned_in_comment_photo_video);
        Link actionLink = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.photo_dative);
        actionLink.end(action.length());

        Spannable actionSpannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(actionSpannable, actionLink);

        holder.cOwnerTime.setText(actionSpannable, TextView.BufferType.SPANNABLE);
        holder.cChangable.setVisibility(View.GONE);

        PicassoInstance.with()
                .load(feedback.getMaxAuthorAvaUrl())
                .tag(Constants.PICASSO_TAG)
                .transform(transformation)
                .into(holder.cOwnerAvatar);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        setupAttachmentViewWithPhoto((Photo) notification.getCommentOf(), holder.ivRightAttachment);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());

        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "mention_comments"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configMentionCommentsFeedback(MentionCommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Post post = (Post) notification.getCommentOf();
        Comment feedback = notification.getWhere();

        Spannable spannable = OwnerLinkSpanFactory.withSpans(feedback.getText(), true, true, mLinkActionAdapter);

        holder.cOwnerName.setText(feedback.getFullAuthorName());
        holder.cOwnerText.setText(spannable, TextView.BufferType.SPANNABLE);

        String action = getDateFromUnixTime(notification.getDate());

        action = action + SPACE + context.getString(R.string.in_comments_for_post);
        Link actionLink = Link.startOf(action.length());
        String postText = getPostTextCopyIncluded(post);
        if (TextUtils.isEmpty(postText)) {
            action = action + SPACE + context.getString(R.string.from_date, getDateFromUnixTime(post.getDate()));
        } else {
            action = action + SPACE + reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(postText));
        }
        actionLink.end(action.length());
        Spannable actionSpannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(actionSpannable, actionLink);

        holder.cOwnerTime.setText(actionSpannable, TextView.BufferType.SPANNABLE);
        holder.cChangable.setVisibility(View.GONE);

        PicassoInstance.with()
                .load(feedback.getMaxAuthorAvaUrl())
                .tag(Constants.PICASSO_TAG)
                .transform(transformation)
                .into(holder.cOwnerAvatar);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        holder.ivRightAttachment.setVisibility(View.GONE);

        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());

        configReply(notification.getReply(), holder);
    }


    /**
     * Настройка отображения уведомления типа "mention"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configMentionFeedback(MentionFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Post feedback = (Post) notification.getWhere();

        Spannable spannable = OwnerLinkSpanFactory.withSpans(feedback.getText(), true, false, mLinkActionAdapter);

        holder.cOwnerName.setText(feedback.getAuthorName());
        holder.cOwnerText.setText(spannable, TextView.BufferType.SPANNABLE);

        String timeWithActionText = getDateFromUnixTime(notification.getDate()) + SPACE + context.getString(R.string.mentioned_in_post);
        holder.cOwnerTime.setText(timeWithActionText);
        holder.cChangable.setVisibility(View.GONE);

        PicassoInstance.with()
                .load(feedback.getAuthorPhoto())
                .tag(Constants.PICASSO_TAG)
                .transform(transformation)
                .into(holder.cOwnerAvatar);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        holder.ivRightAttachment.setVisibility(View.GONE);

        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getAuthorId());

        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "wall_publish"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configWallPublishFeedback(PostPublishFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Post feedback = notification.getPost();

        Spannable feedBackText = OwnerLinkSpanFactory.withSpans(feedback.getText(), true, false, mLinkActionAdapter);

        String action = getDateFromUnixTime(notification.getDate());
        action = action + SPACE + context.getString(R.string.postings_you_the_news);

        holder.cOwnerName.setText(feedback.getAuthorName());
        holder.cOwnerText.setText(feedBackText, TextView.BufferType.SPANNABLE);
        holder.cOwnerText.setVisibility(TextUtils.isEmpty(feedBackText) ? View.GONE : View.VISIBLE);

        holder.cOwnerTime.setText(action);
        holder.cChangable.setVisibility(View.GONE);

        PicassoInstance.with()
                .load(feedback.getAuthorPhoto())
                .tag(Constants.PICASSO_TAG)
                .transform(transformation)
                .into(holder.cOwnerAvatar);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        holder.ivRightAttachment.setVisibility(View.GONE);

        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getAuthorId());

        configReply(notification.getReply(), holder);
    }

    private void solveOwnerOpenByAvatar(ImageView ivSource, int ownerId) {
        ivSource.setOnClickListener(v -> openOwner(ownerId));
    }

    /**
     * Настройка отображения уведомления типа "wall"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configWallFeedback(PostPublishFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Post feedback = notification.getPost();

        Spannable feedBackText = OwnerLinkSpanFactory.withSpans(feedback.getText(), true, false, mLinkActionAdapter);

        String action = getDateFromUnixTime(notification.getDate());
        action = action + SPACE + context.getString(R.string.on_your_wall);

        holder.cOwnerName.setText(feedback.getAuthorName());
        holder.cOwnerText.setText(feedBackText, TextView.BufferType.SPANNABLE);
        holder.cOwnerText.setVisibility(TextUtils.isEmpty(feedBackText) ? View.GONE : View.VISIBLE);

        holder.cOwnerTime.setText(action);
        holder.cChangable.setVisibility(View.GONE);

        PicassoInstance.with()
                .load(feedback.getAuthorPhoto())
                .tag(Constants.PICASSO_TAG)
                .transform(transformation)
                .into(holder.cOwnerAvatar);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        holder.ivRightAttachment.setVisibility(View.GONE);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getAuthorId());

        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "comment_video"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configCommentVideoFeedback(CommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Comment feedback = notification.getComment();

        Spannable feedBackText = OwnerLinkSpanFactory.withSpans(feedback.getText(), true, false, mLinkActionAdapter);

        String action = getDateFromUnixTime(feedback.getDate());
        action = action + SPACE + context.getString(R.string.comment_your_video_without_video);

        Link parentLink = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.video_accusative);
        parentLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, parentLink);

        String ownername = feedback.getFullAuthorName() + SPACE + context.getString(R.string.commented);
        holder.cOwnerName.setText(ownername);
        holder.cOwnerText.setText(feedBackText, TextView.BufferType.SPANNABLE);
        holder.cOwnerText.setVisibility(TextUtils.isEmpty(feedBackText) ? View.GONE : View.VISIBLE);
        holder.cOwnerTime.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.cChangable.setVisibility(View.GONE);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        showUserAvatarOnImageView(feedback.getMaxAuthorAvaUrl(), holder.cOwnerAvatar);
        setupAttachmentViewWithVideo((Video) notification.getCommentOf(), holder.ivRightAttachment);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());
        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "comment_photo"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configCommentPhotoFeedback(CommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Comment feedback = notification.getComment();

        Spannable feedBackText = OwnerLinkSpanFactory.withSpans(feedback.getText(), true, false, mLinkActionAdapter);

        String action = getDateFromUnixTime(feedback.getDate());
        action = action + SPACE + context.getString(R.string.comment_your_photo_without_photo);

        Link parentLink = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.photo_accusative);
        parentLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, parentLink);

        holder.cOwnerName.setText(feedback.getFullAuthorName());
        holder.cOwnerText.setText(feedBackText, TextView.BufferType.SPANNABLE);
        holder.cOwnerText.setVisibility(TextUtils.isEmpty(feedBackText) ? View.GONE : View.VISIBLE);
        holder.cOwnerTime.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.cChangable.setVisibility(View.GONE);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        showUserAvatarOnImageView(feedback.getMaxAuthorAvaUrl(), holder.cOwnerAvatar);
        setupAttachmentViewWithPhoto((Photo) notification.getCommentOf(), holder.ivRightAttachment);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());
        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "comment_post"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configCommentPostFeedback(CommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Post post = (Post) notification.getCommentOf();
        Comment feedback = notification.getComment();

        String feedBackText = OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(feedback.getText());

        String action = getDateFromUnixTime(feedback.getDate());
        action = action + SPACE + context.getString(R.string.for_your_post);

        Link parentLink = Link.startOf(action.length());
        String parentText = OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(getPostTextCopyIncluded(post));
        if (TextUtils.isEmpty(parentText)) {
            action = action + SPACE + context.getString(R.string.from_date, getDateFromUnixTime(post.getDate()));
        } else {
            action = action + SPACE + parentText;
        }

        parentLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, parentLink);

        holder.cOwnerName.setText(feedback.getFullAuthorName());
        holder.cOwnerText.setText(feedBackText);
        holder.cOwnerText.setVisibility(TextUtils.isEmpty(feedBackText) ? View.GONE : View.VISIBLE);
        holder.cOwnerTime.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.cChangable.setVisibility(View.GONE);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        String postImage = post.findFirstImageCopiesInclude();
        if (TextUtils.isEmpty(postImage)) {
            holder.ivRightAttachment.setVisibility(View.GONE);
        } else {
            holder.ivRightAttachment.setVisibility(View.VISIBLE);
            PicassoInstance.with()
                    .load(postImage)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.ivRightAttachment);
        }

        showUserAvatarOnImageView(feedback.getMaxAuthorAvaUrl(), holder.cOwnerAvatar);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());

        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "reply_comment_video"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configReplyCommentVideoFeedback(ReplyCommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Comment parent = notification.getOwnComment();
        Comment feedback = notification.getFeedbackComment();

        String feedBackText = OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(feedback.getText());
        String action = getDateFromUnixTime(feedback.getDate());
        action = action + SPACE + context.getString(R.string.in_reply_to_your_comment);

        String parentText;
        if (TextUtils.isEmpty(parent.getText())) {
            parentText = context.getString(R.string.from_date, getDateFromUnixTime(parent.getDate()));
        } else {
            parentText = reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(parent.getText()));
        }

        Link parentLink = Link.startOf(action.length());
        action = action + SPACE + parentText;
        parentLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, parentLink);

        holder.cOwnerName.setText(feedback.getFullAuthorName());
        holder.cOwnerText.setText(feedBackText);
        holder.cOwnerText.setVisibility(TextUtils.isEmpty(feedBackText) ? View.GONE : View.VISIBLE);
        holder.cOwnerTime.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.cChangable.setVisibility(View.GONE);
        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        setupAttachmentViewWithVideo((Video) notification.getCommentsOf(), holder.ivRightAttachment);

        showUserAvatarOnImageView(feedback.getMaxAuthorAvaUrl(), holder.cOwnerAvatar);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());

        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "reply_comment_photo"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configReplyCommentPhotoFeedback(ReplyCommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Comment parent = notification.getOwnComment();
        Comment feedback = notification.getFeedbackComment();

        setupAttachmentViewWithPhoto((Photo) notification.getCommentsOf(), holder.ivRightAttachment);

        String feedBackText = OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(feedback.getText());

        String action = getDateFromUnixTime(feedback.getDate());
        action = action + SPACE + context.getString(R.string.in_reply_to_your_comment);

        String parentText;
        if (TextUtils.isEmpty(parent.getText())) {
            parentText = context.getString(R.string.from_date, getDateFromUnixTime(parent.getDate()));
        } else {
            parentText = reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(parent.getText()));
        }

        Link parentLink = Link.startOf(action.length());
        action = action + SPACE + parentText;
        parentLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, parentLink);

        holder.cOwnerName.setText(feedback.getFullAuthorName());
        holder.cOwnerText.setText(feedBackText);
        holder.cOwnerText.setVisibility(TextUtils.isEmpty(feedBackText) ? View.GONE : View.VISIBLE);
        holder.cOwnerTime.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.cChangable.setVisibility(View.GONE);
        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        showUserAvatarOnImageView(feedback.getMaxAuthorAvaUrl(), holder.cOwnerAvatar);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());

        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "reply_topic"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configReplyTopicFeedback(ReplyCommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Topic topic = (Topic) notification.getCommentsOf();

        Comment feedback = notification.getFeedbackComment();

        Spannable feedBackText = OwnerLinkSpanFactory.withSpans(feedback.getText(), true, true, mLinkActionAdapter);

        holder.cOwnerText.setText(feedBackText);
        holder.cOwnerText.setVisibility(TextUtils.isEmpty(feedBackText) ? View.GONE : View.VISIBLE);
        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        String action = getDateFromUnixTime(feedback.getDate());
        action = action + SPACE + context.getString(R.string.in_reply_to_your_message_in_topic);

        Link parentLink = Link.startOf(action.length());
        action = action + SPACE + topic.getTitle();
        parentLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, parentLink);

        holder.cOwnerName.setText(feedback.getFullAuthorName());
        holder.cOwnerTime.setText(spannable, TextView.BufferType.SPANNABLE);

        holder.cChangable.setVisibility(View.GONE);
        PicassoInstance.with()
                .load(feedback.getMaxAuthorAvaUrl())
                .tag(Constants.PICASSO_TAG)
                .transform(transformation)
                .into(holder.cOwnerAvatar);

        holder.ivRightAttachment.setVisibility(View.GONE);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());

        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "reply_comment"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configReplyCommentFeedback(ReplyCommentFeedback notification, FeedbackAdapter.CommentHolder holder) {
        Comment parent = notification.getOwnComment();
        Comment feedback = notification.getFeedbackComment();

        String feedBackText = OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(feedback.getText());

        String action = getDateFromUnixTime(notification.getDate());
        action = action + SPACE + context.getString(R.string.in_reply_to_your_comment);

        String parentText;
        if (TextUtils.isEmpty(parent.getText())) {
            parentText = context.getString(R.string.from_date, getDateFromUnixTime(parent.getDate()));
        } else {
            parentText = reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(parent.getText()));
        }

        Link parentLink = Link.startOf(action.length());
        action = action + SPACE + parentText;
        parentLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, parentLink);

        holder.cOwnerName.setText(feedback.getFullAuthorName());
        holder.cOwnerText.setText(feedBackText);
        holder.cOwnerText.setVisibility(TextUtils.isEmpty(feedBackText) ? View.GONE : View.VISIBLE);
        holder.cOwnerTime.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.cChangable.setVisibility(View.GONE);

        PicassoInstance.with()
                .load(feedback.getMaxAuthorAvaUrl())
                .tag(Constants.PICASSO_TAG)
                .transform(transformation)
                .into(holder.cOwnerAvatar);

        AttachmentsHolder containers = AttachmentsHolder.forFeedback(holder.vAttachmentsRoot);
        attachmentsViewBinder.displayAttachments(feedback.getAttachments(), containers, true, null);

        holder.ivRightAttachment.setVisibility(View.GONE);
        solveOwnerOpenByAvatar(holder.cOwnerAvatar, feedback.getFromId());

        configReply(notification.getReply(), holder);
    }

    /**
     * Настройка отображения уведомления типа "follow"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configFollowFeedback(UsersFeedback notification, FeedbackAdapter.UsersHolder holder) {
        List<Owner> users = notification.getOwners();
        Owner user = users.get(0);

        String action = genFullUsersString(users);
        action = action + SPACE + context.getString(R.string.subscribed_to_your_updates);

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);

        holder.uName.setText(spannable, TextView.BufferType.SPANNABLE);
        showFirstUserAvatarOnImageView(notification.getOwners(), holder.uAvatar);

        holder.uInfo.setVisibility(View.GONE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.plus);
        holder.ivAttachment.setVisibility(View.GONE);
        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    private void configReply(Comment reply, FeedbackAdapter.CommentHolder holder) {
        holder.cReplyContainer.setVisibility(reply == null ? View.GONE : View.VISIBLE);

        if (reply != null) {
            holder.cReplyName.setText(reply.getFullAuthorName());
            holder.cReplyTime.setText(getDateFromUnixTime(reply.getDate()));
            holder.cReplyText.setText(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(reply.getText()));

            String authorAvaUrl = reply.getMaxAuthorAvaUrl();
            ViewUtils.displayAvatar(holder.cReplyOwnerAvatar, transformation, authorAvaUrl, Constants.PICASSO_TAG);

            AttachmentsHolder replyContainers = AttachmentsHolder.forFeedback(holder.vReplyAttachmentsRoot);
            attachmentsViewBinder.displayAttachments(reply.getAttachments(), replyContainers, true, null);

            holder.cReplyOwnerAvatar.setOnClickListener(v -> openOwner(reply.getFromId()));
        }
    }

    /**
     * Настройка отображения уведомления типа "friend_accepted"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configFriendAcceptedFeedback(UsersFeedback notification, FeedbackAdapter.UsersHolder holder) {
        List<Owner> owners = notification.getOwners();
        Owner user = owners.get(0);

        String action = genFullUsersString(owners);
        action = action + SPACE + context.getString(R.string.accepted_friend_request);

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);

        holder.uName.setText(spannable, TextView.BufferType.SPANNABLE);
        showFirstUserAvatarOnImageView(owners, holder.uAvatar);
        holder.uInfo.setVisibility(View.GONE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.plus);
        holder.ivAttachment.setVisibility(View.GONE);

        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    /**
     * Настройка отображения уведомления типа "like_post"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configLikePostFeedback(LikeFeedback notification, FeedbackAdapter.UsersHolder holder) {
        Post post = (Post) notification.getLiked();
        List<Owner> owners = notification.getOwners();
        Owner user = owners.get(0);

        String action = genFullUsersString(owners);
        action = action + SPACE + context.getString(R.string.liked_your_post);
        String info;

        String postTitle = getPostTextCopyIncluded(post);
        if (TextUtils.isEmpty(postTitle)) {
            info = context.getString(R.string.from_date, getDateFromUnixTime(post.getDate()));
        } else {
            info = reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(postTitle));
        }
        Link postLink = Link.startOf(0);
        postLink.end(info.length());
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(info);
        showAsLink(spannable, postLink);

        holder.uName.setText(action);
        holder.uInfo.setVisibility(View.VISIBLE);
        holder.uInfo.setText(spannable, TextView.BufferType.SPANNABLE);

        showFirstUserAvatarOnImageView(owners, holder.uAvatar);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.heart_filled);

        String attachmentImg = post.findFirstImageCopiesInclude();
        holder.ivAttachment.setVisibility(TextUtils.isEmpty(attachmentImg) ? View.GONE : View.VISIBLE);
        if (nonEmpty(attachmentImg)) {
            PicassoInstance.with().load(attachmentImg).into(holder.ivAttachment);
        }

        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    /**
     * Настройка отображения уведомления типа "like_video"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configLikeVideoFeedback(LikeFeedback notification, FeedbackAdapter.UsersHolder holder) {
        List<Owner> users = notification.getOwners();
        Owner user = users.get(0);

        String action = genFullUsersString(users);
        action = action + SPACE + context.getString(R.string.liked_your_video_without_video);
        Link photoLink = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.video_accusative);
        photoLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, photoLink);

        holder.uName.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uInfo.setVisibility(View.GONE);
        holder.uChangable.setIcon(R.drawable.heart_filled);
        holder.ivAttachment.setVisibility(View.VISIBLE);

        showFirstUserAvatarOnImageView(notification.getOwners(), holder.uAvatar);
        setupAttachmentViewWithVideo((Video) notification.getLiked(), holder.ivAttachment);
        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    /**
     * Настройка отображения уведомления типа "copy_photo"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configCopyPhotoFeedback(CopyFeedback notification, FeedbackAdapter.UsersHolder holder) {
        List<Owner> users = notification.getOwners();

        Owner user = users.get(0);
        String action = genFullUsersString(users);
        action = action + SPACE + context.getString(R.string.copy_your_photo_without_photo);

        Link link = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.photo_ablative);
        link.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, link);

        holder.uName.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uInfo.setVisibility(View.GONE);
        holder.uChangable.setIcon(R.drawable.ic_vk_share);

        showFirstUserAvatarOnImageView(notification.getOwners(), holder.uAvatar);
        setupAttachmentViewWithPhoto((Photo) notification.getWhat(), holder.ivAttachment);
        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    /**
     * Настройка отображения уведомления типа "copy_video"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configCopyVideoFeedback(CopyFeedback notification, FeedbackAdapter.UsersHolder holder) {
        List<Owner> users = notification.getOwners();
        Owner user = users.get(0);

        String action = genFullUsersString(users);
        action = action + SPACE + context.getString(R.string.copy_your_video_without_video);

        Link link = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.video_ablative);
        link.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, link);

        holder.uName.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.uInfo.setVisibility(View.GONE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.ic_vk_share);

        showFirstUserAvatarOnImageView(notification.getOwners(), holder.uAvatar);
        setupAttachmentViewWithVideo((Video) notification.getWhat(), holder.ivAttachment);
        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    /**
     * Настройка отображения уведомления типа "copy_post"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configCopyPostFeedback(CopyFeedback notification, FeedbackAdapter.UsersHolder holder) {
        Post post = (Post) notification.getWhat();
        List<Owner> users = notification.getOwners();

        String action = genFullUsersString(users);
        action = action + SPACE + context.getString(R.string.shared_post);

        Link link = Link.startOf(0);
        String postText = getPostTextCopyIncluded(post);
        String info;
        if (TextUtils.isEmpty(postText)) {
            info = context.getString(R.string.from_date, getDateFromUnixTime(post.getDate()));
        } else {
            info = reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(postText));
        }
        link.end(info.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(info);
        showAsLink(spannable, link);

        holder.uName.setText(action);
        holder.uInfo.setVisibility(View.VISIBLE);
        holder.uInfo.setText(spannable, TextView.BufferType.SPANNABLE);

        showFirstUserAvatarOnImageView(notification.getOwners(), holder.uAvatar);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.ic_vk_share);

        String firstAttachmentUrl = post.findFirstImageCopiesInclude();

        holder.ivAttachment.setVisibility(isEmpty(firstAttachmentUrl) ? View.GONE : View.VISIBLE);

        if (nonEmpty(firstAttachmentUrl)) {
            PicassoInstance.with()
                    .load(firstAttachmentUrl)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.ivAttachment);
        }

        solveOwnerOpenByAvatar(holder.uAvatar, users.get(0).getOwnerId());
    }

    /**
     * Настройка отображения уведомления типа "like_photo"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configLikePhotoFeedback(LikeFeedback notification, FeedbackAdapter.UsersHolder holder) {
        List<Owner> owners = notification.getOwners();
        Owner firstUser = owners.get(0);

        String action = genFullUsersString(owners);
        action = action + SPACE + context.getString(R.string.liked_your_photo_without_photo);

        Link photoLink = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.photo_accusative);
        photoLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        showAsLink(spannable, photoLink);

        holder.uName.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.uInfo.setVisibility(View.GONE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.heart_filled);

        setupAttachmentViewWithPhoto((Photo) notification.getLiked(), holder.ivAttachment);
        showFirstUserAvatarOnImageView(owners, holder.uAvatar);
        solveOwnerOpenByAvatar(holder.uAvatar, firstUser.getOwnerId());
    }

    /**
     * Позволяет получить к примеру "Евгений Румянцев и ещё 6 человек оценили Вашу фотографию"
     * для заглавия ответа
     *
     * @param users массив пользователей, которые "ответили"
     * @return строка
     */
    private String genFullUsersString(List<Owner> users) {
        Owner owner = users.get(0);
        String action = owner.getFullName();
        if (users.size() > 1) {
            action = action + SPACE + context.getString(R.string.and_users_count_more, users.size() - 1);
        }
        return action;
    }

    /**
     * Настройка отображения уведомления типа "like_comment_photo"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configLikeCommentForPhotoFeedback(LikeCommentFeedback notification, FeedbackAdapter.UsersHolder holder) {
        Comment comment = notification.getLiked();
        List<Owner> users = notification.getOwners();
        Owner user = users.get(0);

        String action = user.getFullName();
        if (users.size() > 1) {
            action = action + SPACE + context.getString(R.string.and_users_count_more, users.size() - 1);
        }

        action = action + SPACE + context.getString(R.string.liked_comment);

        String commentText;
        if (TextUtils.isEmpty(comment.getText())) {
            commentText = context.getString(R.string.from_date, getDateFromUnixTime(comment.getDate()));
        } else {
            commentText = reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(comment.getText()));
        }

        Link commentLink = Link.startOf(0);
        commentLink.end(commentText.length());

        action = action + SPACE + context.getString(R.string.keyword_for);

        Link photoLink = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.photo_dative);
        photoLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        Spannable commentSpan = Spannable.Factory.getInstance().newSpannable(commentText);
        showAsLink(commentSpan, commentLink);
        showAsLink(spannable, photoLink);

        holder.uName.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.uInfo.setVisibility(View.VISIBLE);
        holder.uInfo.setText(commentSpan, TextView.BufferType.SPANNABLE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.heart_filled);

        setupAttachmentViewWithPhoto((Photo) notification.getCommented(), holder.ivAttachment);
        showFirstUserAvatarOnImageView(users, holder.uAvatar);
        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    /**
     * Настройка отображения уведомления типа "like_comment_video"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configLikeCommentVideoFeedback(LikeCommentFeedback notification, FeedbackAdapter.UsersHolder holder) {
        Comment comment = notification.getLiked();
        List<Owner> users = notification.getOwners();
        Owner user = users.get(0);

        String action = user.getFullName();
        if (users.size() > 1) {
            action = action + SPACE + context.getString(R.string.and_users_count_more, users.size() - 1);
        }

        action = action + SPACE + context.getString(R.string.liked_comment);

        String commentText;
        if (TextUtils.isEmpty(comment.getText())) {
            commentText = context.getString(R.string.from_date, getDateFromUnixTime(comment.getDate()));
        } else {
            commentText = reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(comment.getText()));
        }

        Link commentLink = Link.startOf(0);
        commentLink.end(commentText.length());

        action = action + SPACE + context.getString(R.string.keyword_for);

        Link photoLink = Link.startOf(action.length());
        action = action + SPACE + context.getString(R.string.video_dative);
        photoLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        Spannable spannable1 = Spannable.Factory.getInstance().newSpannable(commentText);
        showAsLink(spannable1, commentLink);
        showAsLink(spannable, photoLink);

        holder.uName.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.uInfo.setVisibility(View.VISIBLE);
        holder.uInfo.setText(spannable1, TextView.BufferType.SPANNABLE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.heart_filled);

        showFirstUserAvatarOnImageView(users, holder.uAvatar);
        setupAttachmentViewWithVideo((Video) notification.getCommented(), holder.ivAttachment);
        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    /**
     * Настройка отображения уведомления типа "like_comment_topic"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configLikeCommentTopicFeedback(LikeCommentFeedback notification, FeedbackAdapter.UsersHolder holder) {
        Comment comment = notification.getLiked();
        List<Owner> users = notification.getOwners();
        Owner user = users.get(0);
        Topic topic = (Topic) notification.getCommented();

        holder.ivAttachment.setVisibility(View.GONE);

        String action = user.getFullName();
        if (users.size() > 1) {
            action = action + SPACE + context.getString(R.string.and_users_count_more, users.size() - 1);
        }

        action = action + SPACE + context.getString(R.string.liked_comment);

        String commentText;
        if (TextUtils.isEmpty(comment.getText())) {
            commentText = context.getString(R.string.from_date, getDateFromUnixTime(comment.getDate()));
        } else {
            commentText = reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(comment.getText()));
        }

        Link commentLink = Link.startOf(0);
        commentLink.end(commentText.length());

        action = action + SPACE + context.getString(R.string.in_topic);

        Link photoLink = Link.startOf(action.length());
        action = action + SPACE + topic.getTitle();
        photoLink.end(action.length());

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(action);
        Spannable spannable1 = Spannable.Factory.getInstance().newSpannable(commentText);
        showAsLink(spannable1, commentLink);
        showAsLink(spannable, photoLink);

        holder.uName.setText(spannable, TextView.BufferType.SPANNABLE);
        holder.uInfo.setVisibility(View.VISIBLE);
        holder.uInfo.setText(spannable1, TextView.BufferType.SPANNABLE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.heart_filled);

        showFirstUserAvatarOnImageView(users, holder.uAvatar);
        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    private void showAsLink(Spannable spannable, Link link) {
        spannable.setSpan(new ForegroundColorSpan(linkColor), link.start, link.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.ITALIC), link.start, link.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Настройка отображения уведомления типа "like_comment"
     *
     * @param notification уведомление
     * @param holder       контейнер в елементами интерфейса
     */
    public void configLikeCommentFeedback(LikeCommentFeedback notification, FeedbackAdapter.UsersHolder holder) {
        Comment comment = notification.getLiked();
        List<Owner> users = notification.getOwners();
        Post post = (Post) notification.getCommented();

        Owner user = users.get(0);

        String action = user.getFullName();
        if (users.size() > 1) {
            action = action + SPACE + context.getString(R.string.and_users_count_more, users.size() - 1);
        }

        action = action + SPACE + context.getString(R.string.liked_comment);

        String commentText;
        if (TextUtils.isEmpty(comment.getText())) {
            commentText = context.getString(R.string.from_date, getDateFromUnixTime(comment.getDate()));
        } else {
            commentText = reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(comment.getText()));
        }

        Link commentLink = Link.startOf(0);
        commentLink.end(commentText.length());

        Link postLink = null;
        String info = commentText;
        if (post != null) {
            info = info + SPACE + context.getString(R.string.for_post);
            postLink = Link.startOf(info.length());
            if (TextUtils.isEmpty(post.getText())) {
                info = info + SPACE + context.getString(R.string.from_date, getDateFromUnixTime(post.getDate()));
            } else {
                info = info + SPACE + reduce(OwnerLinkSpanFactory.getTextWithCollapseOwnerLinks(post.getText()));
            }
            postLink.end(info.length());
        }

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(info);
        showAsLink(spannable, commentLink);
        if (postLink != null) {
            showAsLink(spannable, postLink);
        }

        holder.uName.setText(action);
        holder.uInfo.setVisibility(View.VISIBLE);
        holder.uInfo.setText(info, TextView.BufferType.SPANNABLE);
        holder.uTime.setText(getDateFromUnixTime(notification.getDate()));
        holder.uChangable.setIcon(R.drawable.heart_filled);
        holder.ivAttachment.setVisibility(View.GONE);

        showFirstUserAvatarOnImageView(users, holder.uAvatar);
        solveOwnerOpenByAvatar(holder.uAvatar, user.getOwnerId());
    }

    /**
     * Обрезать строку до 100 символов
     *
     * @param input строка, которую надо обрезать
     * @return обрезанная строка
     */
    private String reduce(String input) {
        return input.length() > 100 ? input.substring(0, 100) + "..." : input;
    }

    /**
     * Отобразить аватар пользователя на ImageView
     *
     * @param url       сслыка на аватра
     * @param imageView вьюв
     */
    private void showUserAvatarOnImageView(String url, ImageView imageView) {
        ViewUtils.displayAvatar(imageView, transformation, url, Constants.PICASSO_TAG);
    }

    /**
     * Отображение аватара первого в списке пользователя на ImageView.
     * Если у пользователя нет аватара, то будет отображено изображение
     * неизвестного пользователя
     *
     * @param owners    массив пользователей
     * @param imageView вьюв
     */
    private void showFirstUserAvatarOnImageView(List<Owner> owners, ImageView imageView) {
        if (owners == null || owners.isEmpty() || TextUtils.isEmpty(owners.get(0).getMaxSquareAvatar())) {
            PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .tag(Constants.PICASSO_TAG)
                    .into(imageView);
        } else {
            String url = owners.get(0).getMaxSquareAvatar();
            PicassoInstance.with()
                    .load(url)
                    .tag(Constants.PICASSO_TAG)
                    .transform(transformation)
                    .into(imageView);
        }
    }

    /**
     * Получить заглавие поста.
     * Если у поста нет заглавия, то проверяеться история репостов,
     * возвращается первое найденное заглавие
     *
     * @param post пост
     * @return заглавие
     */
    private String getPostTextCopyIncluded(@NonNull Post post) {
        if (TextUtils.isEmpty(post.getText()) && post.hasCopyHierarchy()) {
            for (Post copy : post.getCopyHierarchy()) {
                if (copy.hasText()) {
                    return copy.getText();
                }
            }
            return null;
        } else {
            return post.getText();
        }
    }

    private void openOwner(int userId) {
        if (attachmentsActionCallback != null) {
            attachmentsActionCallback.onOpenOwner(userId);
        }
    }

    private static class Link {
        private final int start;
        private int end;

        private Link(int start) {
            this.start = start;
        }

        private static Link startOf(int start) {
            return new Link(start);
        }

        private void end(int end) {
            this.end = end;
        }
    }
}
