package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.EventListener;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Topic;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class FeedbackLinkAdapter extends RecyclerView.Adapter<FeedbackLinkAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Object> mData;

    private final Transformation transformation;

    private final ActionListener mActionListener;

    public FeedbackLinkAdapter(Context context, List<Object> objects, @NonNull ActionListener actionListener) {
        mContext = context;
        mActionListener = actionListener;
        mData = objects;
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback_link, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = mData.get(position);

        String title = null;
        if (item instanceof User) {
            User user = (User) item;
            title = user.getFullName();

            holder.mSubtitle.setText(R.string.open_profile);
            holder.ivImage.setVisibility(View.VISIBLE);

            ViewUtils.displayAvatar(holder.ivImage, transformation, user.getMaxSquareAvatar(), null);
        } else if (item instanceof Post) {
            Post post = (Post) item;
            title = post.getTextCopiesInclude();
            holder.mSubtitle.setText(R.string.open_post);

            String imageUrl = post.findFirstImageCopiesInclude(PhotoSize.M, false);
            if (TextUtils.isEmpty(imageUrl)) {
                holder.ivImage.setVisibility(View.GONE);
            } else {
                holder.ivImage.setVisibility(View.VISIBLE);
                PicassoInstance.with()
                        .load(imageUrl)
                        .into(holder.ivImage);
            }
        } else if (item instanceof Comment) {
            Comment comment = (Comment) item;
            title = comment.getText();
            holder.mSubtitle.setText(R.string.jump_to_comment);
            String senderAvatar = comment.getMaxAuthorAvaUrl();
            holder.ivImage.setVisibility(TextUtils.isEmpty(senderAvatar) ? View.GONE : View.VISIBLE);
            ViewUtils.displayAvatar(holder.ivImage, transformation, senderAvatar, null);
        } else if (item instanceof Photo) {
            Photo photo = (Photo) item;
            title = photo.getText();
            holder.mSubtitle.setText(R.string.show_photo);
            String imgUrl = photo.getUrlForSize(PhotoSize.M, false);

            if (TextUtils.isEmpty(imgUrl)) {
                holder.ivImage.setVisibility(View.GONE);
            } else {
                holder.ivImage.setVisibility(View.VISIBLE);
                PicassoInstance.with()
                        .load(imgUrl)
                        .into(holder.ivImage);
            }
        } else if (item instanceof Video) {
            Video video = (Video) item;
            String imgUrl = video.getImage();
            title = video.getTitle();
            holder.mSubtitle.setText(R.string.show_video);

            if (TextUtils.isEmpty(imgUrl)) {
                holder.ivImage.setVisibility(View.GONE);
            } else {
                holder.ivImage.setVisibility(View.VISIBLE);
                PicassoInstance.with()
                        .load(imgUrl)
                        .into(holder.ivImage);
            }
        } else if (item instanceof Topic) {
            Topic topic = (Topic) item;
            title = topic.getTitle();
            holder.mSubtitle.setText(R.string.open_topic);

            holder.ivImage.setVisibility(View.GONE);
        }

        Spannable spannableTitle = OwnerLinkSpanFactory.withSpans(title, true, true, null);
        holder.mTitle.setText(spannableTitle, TextView.BufferType.SPANNABLE);
        holder.mTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public interface ActionListener extends EventListener {
        void onPostClick(@NonNull Post post);

        void onCommentClick(@NonNull Comment comment);

        void onTopicClick(@NonNull Topic topic);

        void onPhotoClick(@NonNull Photo photo);

        void onVideoClick(@NonNull Video video);

        void onUserClick(@NonNull User user);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTitle;
        private final TextView mSubtitle;
        private final ImageView ivImage;

        ViewHolder(View root) {
            super(root);
            mTitle = root.findViewById(R.id.item_feedback_link_text);
            mSubtitle = root.findViewById(R.id.item_feedback_link_text2);
            ivImage = root.findViewById(R.id.item_feedback_link_image);
            ImageView ivForward = root.findViewById(R.id.item_feedback_link_forward);
            Utils.setColorFilter(ivForward, CurrentTheme.getColorPrimary(mContext));

            root.setOnClickListener(v -> {
                Object item = mData.get(getBindingAdapterPosition());
                if (item instanceof User) {
                    User user = (User) item;
                    mActionListener.onUserClick(user);
                } else if (item instanceof Post) {
                    Post post = (Post) item;
                    mActionListener.onPostClick(post);
                } else if (item instanceof Comment) {
                    Comment comment = (Comment) item;
                    mActionListener.onCommentClick(comment);
                } else if (item instanceof Photo) {
                    Photo photo = (Photo) item;
                    mActionListener.onPhotoClick(photo);
                } else if (item instanceof Video) {
                    Video video = (Video) item;
                    mActionListener.onVideoClick(video);
                } else if (item instanceof Topic) {
                    Topic topic = (Topic) item;
                    mActionListener.onTopicClick(topic);
                }
            });
        }
    }
}
