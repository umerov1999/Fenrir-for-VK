package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Utils.safeLenghtOf;

import android.content.Context;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.adapter.holder.IdentificableHolder;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.News;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.CircleCounterButton;

public class FeedAdapter extends RecyclerBindableAdapter<News, FeedAdapter.PostHolder> {

    private final Context context;
    private final AttachmentsViewBinder attachmentsViewBinder;
    private final Transformation transformation;
    private ClickListener clickListener;
    private int nextHolderId;
    private RecyclerView recyclerView;

    public FeedAdapter(Context context, List<News> data, AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback) {
        super(data);
        this.context = context;
        attachmentsViewBinder = new AttachmentsViewBinder(context, attachmentsActionCallback);
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    @Override
    protected void onBindItemViewHolder(PostHolder holder, int position, int type) {
        News item = getItem(position);

        attachmentsViewBinder.displayAttachments(item.getAttachments(), holder.attachmentsHolder, false, null);
        attachmentsViewBinder.displayCopyHistory(item.getCopyHistory(), holder.attachmentsHolder.getVgPosts(), true, R.layout.item_copy_history_post);
        attachmentsViewBinder.displayFriendsPost(item.getFriends(), holder.attachmentsHolder.getVgFriends(), R.layout.item_catalog_link);

        holder.tvOwnerName.setText(item.getOwnerName());

        String result = AppTextUtils.reduceStringForPost(item.getText());
        holder.tvText.setText(OwnerLinkSpanFactory.withSpans(result, true, false, new LinkActionAdapter() {
            @Override
            public void onOwnerClick(int ownerId) {
                if (clickListener != null) {
                    clickListener.onAvatarClick(ownerId);
                }
            }
        }));

        boolean force = false;
        if (TextUtils.isEmpty(item.getText())) {
            switch (item.getType()) {
                case "photo":
                    force = true;
                    holder.tvText.setText(R.string.public_photo);
                    break;
                case "wall_photo":
                    force = true;
                    holder.tvText.setText(R.string.public_photo_wall);
                    break;
                case "photo_tag":
                    force = true;
                    holder.tvText.setText(R.string.public_photo_tag);
                    break;
                case "friend":
                    force = true;
                    holder.tvText.setText(R.string.public_friends);
                    break;
                case "audio":
                    force = true;
                    holder.tvText.setText(R.string.public_audio);
                    break;
                case "video":
                    force = true;
                    holder.tvText.setText(R.string.public_video);
                    break;
            }
        }
        holder.bottomActionsContainer.setVisibility(item.getType().equals("post") ? View.VISIBLE : View.GONE);

        holder.tvShowMore.setVisibility(safeLenghtOf(item.getText()) > 400 ? View.VISIBLE : View.GONE);

        /*
        if (item.getSource() != null){
            switch (item.getSource().data){
                case PROFILE_ACTIVITY:
                    postSubtitle = context.getString(R.string.updated_status_at) + SPACE + AppTextUtils.getDateFromUnixTime(context, item.getDate());
                    break;
                case PROFILE_PHOTO:
                    postSubtitle = context.getString(R.string.updated_profile_photo_at) + SPACE + AppTextUtils.getDateFromUnixTime(context, item.getDate());
                    break;
            }
        }
        */

        String postTime = AppTextUtils.getDateFromUnixTime(context, item.getDate());
        holder.tvTime.setText(postTime);

        holder.tvText.setVisibility(TextUtils.isEmpty(item.getText()) && !force ? View.GONE : View.VISIBLE);

        String ownerAvaUrl = item.getOwnerMaxSquareAvatar();
        ViewUtils.displayAvatar(holder.ivOwnerAvatar, transformation, ownerAvaUrl, Constants.PICASSO_TAG);

        holder.ivOwnerAvatar.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAvatarClick(item.getSourceId());
            }
        });

        fillCounters(holder, item);

        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPostClick(item);
            }
        });

        holder.viewsCounter.setVisibility(item.getViewCount() > 0 ? View.VISIBLE : View.GONE);
        //holder.viewsCounter.setText(String.valueOf(item.getViewCount()));

        ViewUtils.setCountText(holder.viewsCounter, item.getViewCount(), false);
    }

    @Override
    protected PostHolder viewHolder(View view, int type) {
        return new PostHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_feed;
    }

    private int genereateHolderId() {
        nextHolderId++;
        return nextHolderId;
    }

    private void fillCounters(PostHolder holder, News news) {
        int targetLikeRes = news.isUserLike() ? R.drawable.heart_filled : R.drawable.heart;
        holder.likeButton.setIcon(targetLikeRes);

        holder.likeButton.setActive(news.isUserLike());
        holder.likeButton.setCount(news.getLikeCount());

        holder.likeButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onLikeClick(news, !news.isUserLike());
            }
        });

        holder.likeButton.setOnLongClickListener(v -> clickListener != null && clickListener.onLikeLongClick(news));

        holder.commentsButton.setVisibility(news.isCommentCanPost() || news.getCommentCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.commentsButton.setCount(news.getCommentCount());
        holder.commentsButton.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onCommentButtonClick(news);
            }
        });

        holder.shareButton.setActive(news.isUserReposted());
        holder.shareButton.setCount(news.getRepostsCount());
        holder.shareButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRepostClick(news);
            }
        });

        holder.shareButton.setOnLongClickListener(v -> clickListener != null && clickListener.onShareLongClick(news));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onAvatarClick(int ownerId);

        void onRepostClick(News news);

        void onPostClick(News news);

        void onBanClick(News news);

        void onIgnoreClick(News news);

        void onFaveClick(News news);

        void onCommentButtonClick(News news);

        void onLikeClick(News news, boolean add);

        boolean onLikeLongClick(News news);

        boolean onShareLongClick(News news);
    }

    class PostHolder extends RecyclerView.ViewHolder implements IdentificableHolder, View.OnCreateContextMenuListener {

        final TextView tvOwnerName;
        final ImageView ivOwnerAvatar;
        final TextView tvText;
        final TextView tvShowMore;
        final TextView tvTime;
        final Group bottomActionsContainer;
        final CircleCounterButton likeButton;
        final CircleCounterButton shareButton;
        final CircleCounterButton commentsButton;
        final AttachmentsHolder attachmentsHolder;
        final TextView viewsCounter;
        private final View cardView;

        PostHolder(View root) {
            super(root);
            itemView.setOnCreateContextMenuListener(this);
            cardView = root.findViewById(R.id.card_view);
            cardView.setTag(genereateHolderId());

            ivOwnerAvatar = root.findViewById(R.id.item_post_avatar);
            tvOwnerName = root.findViewById(R.id.item_post_owner_name);
            tvText = root.findViewById(R.id.item_post_text);
            tvShowMore = root.findViewById(R.id.item_post_show_more);
            tvTime = root.findViewById(R.id.item_post_time);
            bottomActionsContainer = root.findViewById(R.id.buttons_bar);
            likeButton = root.findViewById(R.id.like_button);
            commentsButton = root.findViewById(R.id.comments_button);
            shareButton = root.findViewById(R.id.share_button);

            attachmentsHolder = AttachmentsHolder.forPost((ViewGroup) root);
            viewsCounter = itemView.findViewById(R.id.post_views_counter);
        }

        @Override
        public int getHolderId() {
            return (int) cardView.getTag();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = getItemRawPosition(recyclerView.getChildAdapterPosition(v));
            News feed = getItems().get(position);
            if ("post".equals(feed.getType())) {
                menu.add(0, v.getId(), 0, R.string.add_to_bookmarks).setOnMenuItemClickListener(item -> {
                    if (clickListener != null) {
                        clickListener.onFaveClick(feed);
                    }
                    return true;
                });
            }
            menu.add(0, v.getId(), 0, R.string.ban_author).setOnMenuItemClickListener(item -> {
                if (clickListener != null) {
                    clickListener.onBanClick(feed);
                }
                return true;
            });
            menu.add(0, v.getId(), 0, R.string.not_interested).setOnMenuItemClickListener(item -> {
                if (clickListener != null) {
                    clickListener.onIgnoreClick(feed);
                }
                return true;
            });

        }
    }
}
