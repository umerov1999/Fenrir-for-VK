package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.AppTextUtils.getDateFromUnixTime;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.ViewUtils.displayAvatar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.AbsRecyclerViewAdapter;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.NewsfeedComment;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.PhotoWithOwner;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Topic;
import dev.ragnarok.fenrir.model.TopicWithOwner;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.VideoWithOwner;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.view.AspectRatioImageView;
import dev.ragnarok.fenrir.view.VideoServiceIcons;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;

public class NewsfeedCommentsAdapter extends AbsRecyclerViewAdapter<NewsfeedCommentsAdapter.AbsHolder> {

    private static final int VTYPE_POST = 1;
    private static final int VTYPE_VIDEO = 2;
    private static final int VTYPE_PHOTO = 3;
    private static final int VTYPE_TOPIC = 4;
    private final Context context;
    private final AttachmentsViewBinder attachmentsViewBinder;
    private final Transformation transformation;
    private final LinkActionAdapter linkActionAdapter;
    private final int colorTextSecondary;
    private final int iconColorActive;
    private List<NewsfeedComment> data;
    private ActionListener actionListener;

    public NewsfeedCommentsAdapter(Context context, List<NewsfeedComment> data,
                                   AttachmentsViewBinder.OnAttachmentsActionCallback callback) {
        this.context = context;
        this.data = data;
        transformation = CurrentTheme.createTransformationForAvatar();
        attachmentsViewBinder = new AttachmentsViewBinder(context, callback);

        colorTextSecondary = CurrentTheme.getSecondaryTextColorCode(context);
        iconColorActive = CurrentTheme.getColorPrimary(context);

        linkActionAdapter = new LinkActionAdapter() {
            // do nothing
        };
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public AbsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VTYPE_POST:
                return new PostHolder(inflater.inflate(R.layout.item_newsfeed_comment_post, parent, false));
            case VTYPE_VIDEO:
                return new VideoHolder(inflater.inflate(R.layout.item_newsfeed_comment_video, parent, false));
            case VTYPE_PHOTO:
                return new PhotoHolder(inflater.inflate(R.layout.item_newsfeed_comment_photo, parent, false));
            case VTYPE_TOPIC:
                return new TopicHolder(inflater.inflate(R.layout.item_newsfeed_comment_topic, parent, false));
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(@NonNull AbsHolder holder, int position) {
        bindBase(holder, position);

        switch (getItemViewType(position)) {
            case VTYPE_POST:
                bindPost((PostHolder) holder, position);
                break;
            case VTYPE_VIDEO:
                bindVideo((VideoHolder) holder, position);
                break;
            case VTYPE_PHOTO:
                bindPhoto((PhotoHolder) holder, position);
                break;
            case VTYPE_TOPIC:
                bindTopic((TopicHolder) holder, position);
                break;
        }
    }

    private void bindTopic(TopicHolder holder, int position) {
        TopicWithOwner wrapper = (TopicWithOwner) data.get(position).getModel();
        Topic topic = wrapper.getTopic();
        Owner owner = wrapper.getOwner();

        displayAvatar(holder.ownerAvatar, transformation, owner.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        if (nonNull(topic.getCreator())) {
            holder.creatorAvatar.setVisibility(View.VISIBLE);
            displayAvatar(holder.creatorAvatar, transformation, topic.getCreator().get100photoOrSmaller(), Constants.PICASSO_TAG);
        } else {
            holder.creatorAvatar.setVisibility(View.GONE);
            PicassoInstance.with().cancelRequest(holder.creatorAvatar);
        }

        addOwnerAvatarClickHandling(holder.ownerAvatar, topic.getOwnerId());

        holder.ownerName.setText(owner.getFullName());
        holder.commentsCounter.setText(String.valueOf(topic.getCommentsCount()));
        holder.title.setText(topic.getTitle());
    }

    private void bindPhoto(PhotoHolder holder, int position) {
        PhotoWithOwner wrapper = (PhotoWithOwner) data.get(position).getModel();
        Photo photo = wrapper.getPhoto();
        Owner owner = wrapper.getOwner();

        displayAvatar(holder.ownerAvatar, transformation, owner.getMaxSquareAvatar(), Constants.PICASSO_TAG);
        addOwnerAvatarClickHandling(holder.ownerAvatar, photo.getOwnerId());

        holder.ownerName.setText(owner.getFullName());
        holder.dateTime.setText(getDateFromUnixTime(context, photo.getDate()));

        holder.title.setVisibility(nonEmpty(photo.getText()) ? View.VISIBLE : View.GONE);
        holder.title.setText(photo.getText());

        if (photo.getWidth() > photo.getHeight()) {
            holder.image.setAspectRatio(photo.getWidth(), photo.getHeight());
            holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            holder.image.setAspectRatio(1, 1);
            holder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        String photoUrl = photo.getUrlForSize(PhotoSize.X, true);

        if (nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl)
                    .into(holder.image);
        }
    }

    private void bindVideo(VideoHolder holder, int position) {
        VideoWithOwner wrapper = (VideoWithOwner) data.get(position).getModel();
        Video video = wrapper.getVideo();
        Owner owner = wrapper.getOwner();

        holder.title.setText(video.getTitle());
        holder.viewsCounter.setText(String.valueOf(video.getViews()));
        holder.datitime.setText(getDateFromUnixTime(context, video.getDate()));

        Integer serviceIcon = VideoServiceIcons.getIconByType(video.getPlatform());
        if (nonNull(serviceIcon)) {
            holder.service.setVisibility(View.VISIBLE);
            holder.service.setImageResource(serviceIcon);
        } else {
            holder.service.setVisibility(View.GONE);
        }

        if (nonEmpty(video.getImage())) {
            PicassoInstance.with()
                    .load(video.getImage())
                    .into(holder.image);
        } else {
            PicassoInstance.with()
                    .cancelRequest(holder.image);
        }

        holder.duration.setText(AppTextUtils.getDurationString(video.getDuration()));
        holder.ownerName.setText(owner.getFullName());

        displayAvatar(holder.avatar, transformation, owner.getMaxSquareAvatar(), Constants.PICASSO_TAG);
        addOwnerAvatarClickHandling(holder.avatar, video.getOwnerId());
    }

    private void bindBase(AbsHolder holder, int position) {
        NewsfeedComment newsfeedComment = data.get(position);
        Comment comment = newsfeedComment.getComment();

        if (isNull(comment)) {
            holder.commentRoot.setVisibility(View.GONE);
            return;
        }

        holder.commentRoot.setVisibility(View.VISIBLE);
        holder.commentRoot.setOnClickListener(v -> {
            if (nonNull(actionListener)) {
                actionListener.onCommentBodyClick(newsfeedComment);
            }
        });

        holder.commentAttachmentRoot.setVisibility(comment.hasAttachments() ? View.VISIBLE : View.GONE);
        attachmentsViewBinder.displayAttachments(comment.getAttachments(), holder.commentAttachmentHolder, true, null);

        displayAvatar(holder.commentAvatar, transformation, comment.getMaxAuthorAvaUrl(), Constants.PICASSO_TAG);

        holder.commentAuthorName.setText(comment.getFullAuthorName());
        holder.commentDatetime.setText(getDateFromUnixTime(context, comment.getDate()));

        Spannable text = OwnerLinkSpanFactory.withSpans(comment.getText(), true, true, linkActionAdapter);
        holder.commentText.setText(text, TextView.BufferType.SPANNABLE);
        holder.commentText.setVisibility(isEmpty(comment.getText()) ? View.GONE : View.VISIBLE);

        holder.commentLikeCounter.setVisibility(comment.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
        holder.commentLikeCounter.setText(String.valueOf(comment.getLikesCount()));
        TextViewCompat.setCompoundDrawableTintList(holder.commentLikeCounter, ColorStateList.valueOf(comment.isUserLikes() ? iconColorActive : colorTextSecondary));

        if (comment.getFromId() != 0) {
            addOwnerAvatarClickHandling(holder.commentAvatar, comment.getFromId());
        }
    }

    private void bindPost(PostHolder holder, int position) {
        NewsfeedComment comment = data.get(position);
        Post post = (Post) comment.getModel();

        attachmentsViewBinder.displayAttachments(post.getAttachments(), holder.postAttachmentsHolder, false, null);
        attachmentsViewBinder.displayCopyHistory(post.getCopyHierarchy(), holder.postAttachmentsHolder.getVgPosts(), true, R.layout.item_copy_history_post);

        holder.ownerName.setText(post.getAuthorName());
        holder.postDatetime.setText(getDateFromUnixTime(context, post.getDate()));

        displayAvatar(holder.ownerAvatar, transformation, post.getAuthorPhoto(), Constants.PICASSO_TAG);
        addOwnerAvatarClickHandling(holder.ownerAvatar, post.getOwnerId());

        String reduced = AppTextUtils.reduceStringForPost(post.getText());
        holder.postText.setText(OwnerLinkSpanFactory.withSpans(reduced, true, false, linkActionAdapter));
        holder.buttonShowMore.setVisibility(post.hasText() && post.getText().length() > 400 ? View.VISIBLE : View.GONE);
        holder.postTextRoot.setVisibility(post.hasText() ? View.VISIBLE : View.GONE);

        holder.signerRoot.setVisibility(isNull(post.getCreator()) ? View.GONE : View.VISIBLE);
        addOwnerAvatarClickHandling(holder.signerRoot, post.getSignerId());

        if (nonNull(post.getCreator())) {
            holder.signerName.setText(post.getCreator().getFullName());
            displayAvatar(holder.signerAvatar, transformation, post.getCreator().getPhoto50(), Constants.PICASSO_TAG);
        }

        holder.viewsCounter.setText(String.valueOf(post.getViewCount()));
        holder.viewsCounter.setVisibility(post.getViewCount() > 0 ? View.VISIBLE : View.GONE);

        holder.friendsOnlyIcon.setVisibility(post.isFriendsOnly() ? View.VISIBLE : View.GONE);

        holder.topDivider.setVisibility(WallAdapter.needToShowTopDivider(post) ? View.VISIBLE : View.GONE);

        View.OnClickListener postOpenClickListener = v -> {
            if (nonNull(actionListener)) {
                actionListener.onPostBodyClick(comment);
            }
        };

        holder.postRoot.setOnClickListener(postOpenClickListener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        NewsfeedComment comment = data.get(position);
        if (comment.getModel() instanceof Post) {
            return VTYPE_POST;
        }

        if (comment.getModel() instanceof VideoWithOwner) {
            return VTYPE_VIDEO;
        }

        if (comment.getModel() instanceof PhotoWithOwner) {
            return VTYPE_PHOTO;
        }

        if (comment.getModel() instanceof TopicWithOwner) {
            return VTYPE_TOPIC;
        }

        throw new IllegalStateException("Unsupported view type");
    }

    public void setData(List<NewsfeedComment> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public interface ActionListener {
        void onPostBodyClick(NewsfeedComment comment);

        void onCommentBodyClick(NewsfeedComment comment);
    }

    private static final class TopicHolder extends AbsHolder {

        final ImageView ownerAvatar;
        final ImageView creatorAvatar;
        final TextView commentsCounter;
        final TextView ownerName;
        final TextView title;

        TopicHolder(View itemView) {
            super(itemView);
            ownerAvatar = itemView.findViewById(R.id.owner_avatar);
            creatorAvatar = itemView.findViewById(R.id.creator_avatar);
            commentsCounter = itemView.findViewById(R.id.comments_counter);
            ownerName = itemView.findViewById(R.id.owner_name);
            title = itemView.findViewById(R.id.title);
        }
    }

    abstract static class AbsHolder extends RecyclerView.ViewHolder {

        final View commentRoot;

        final ImageView commentAvatar;
        final TextView commentAuthorName;
        final EmojiconTextView commentText;
        final TextView commentDatetime;

        final TextView commentLikeCounter;

        final ViewGroup commentAttachmentRoot;
        final AttachmentsHolder commentAttachmentHolder;

        AbsHolder(View itemView) {
            super(itemView);

            commentRoot = itemView.findViewById(R.id.comment_root);
            commentAvatar = itemView.findViewById(R.id.item_comment_owner_avatar);
            commentAuthorName = itemView.findViewById(R.id.item_comment_owner_name);
            commentText = itemView.findViewById(R.id.item_comment_text);
            commentDatetime = itemView.findViewById(R.id.item_comment_time);

            commentLikeCounter = itemView.findViewById(R.id.item_comment_like_counter);

            commentAttachmentRoot = commentRoot.findViewById(R.id.item_comment_attachments_root);
            commentAttachmentHolder = AttachmentsHolder.forComment(commentAttachmentRoot);
        }
    }

    private static class PhotoHolder extends AbsHolder {

        final ImageView ownerAvatar;
        final TextView ownerName;
        final TextView dateTime;
        final TextView title;
        final AspectRatioImageView image;

        PhotoHolder(View itemView) {
            super(itemView);
            ownerAvatar = itemView.findViewById(R.id.photo_owner_avatar);
            ownerName = itemView.findViewById(R.id.photo_owner_name);
            dateTime = itemView.findViewById(R.id.photo_datetime);
            image = itemView.findViewById(R.id.photo_image);
            title = itemView.findViewById(R.id.photo_title);
        }
    }

    private static class VideoHolder extends AbsHolder {

        final TextView title;
        final TextView datitime;
        final TextView viewsCounter;

        final ImageView service;
        final ImageView image;
        final TextView duration;

        final ImageView avatar;
        final TextView ownerName;

        VideoHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.video_owner_avatar);
            ownerName = itemView.findViewById(R.id.video_owner_name);

            title = itemView.findViewById(R.id.video_title);
            datitime = itemView.findViewById(R.id.video_datetime);
            viewsCounter = itemView.findViewById(R.id.video_views_counter);

            service = itemView.findViewById(R.id.video_service);
            image = itemView.findViewById(R.id.video_image);
            duration = itemView.findViewById(R.id.video_lenght);
        }
    }

    private static class PostHolder extends AbsHolder {

        final ImageView ownerAvatar;
        final TextView ownerName;
        final TextView postDatetime;

        final View postTextRoot;
        final EmojiconTextView postText;
        final View buttonShowMore;

        final View signerRoot;
        final ImageView signerAvatar;
        final TextView signerName;

        final AttachmentsHolder postAttachmentsHolder;

        final TextView viewsCounter;

        final View friendsOnlyIcon;

        final View topDivider;

        final View postRoot;

        PostHolder(View itemView) {
            super(itemView);

            topDivider = itemView.findViewById(R.id.top_divider);

            ownerAvatar = itemView.findViewById(R.id.item_post_avatar);
            ownerName = itemView.findViewById(R.id.item_post_owner_name);
            postDatetime = itemView.findViewById(R.id.item_post_time);

            postTextRoot = itemView.findViewById(R.id.item_text_container);
            postText = itemView.findViewById(R.id.item_post_text);
            buttonShowMore = itemView.findViewById(R.id.item_post_show_more);

            ViewGroup postAttachmentRoot = itemView.findViewById(R.id.item_post_attachments);
            postAttachmentsHolder = AttachmentsHolder.forPost(postAttachmentRoot);

            signerRoot = itemView.findViewById(R.id.item_post_signer_root);
            signerAvatar = itemView.findViewById(R.id.item_post_signer_icon);
            signerName = itemView.findViewById(R.id.item_post_signer_name);

            viewsCounter = itemView.findViewById(R.id.post_views_counter);

            friendsOnlyIcon = itemView.findViewById(R.id.item_post_friends_only);

            postRoot = itemView.findViewById(R.id.post_root);
        }
    }
}
