package dev.ragnarok.fenrir.adapter.fave;

import static dev.ragnarok.fenrir.api.model.VkApiPostSource.Data.PROFILE_ACTIVITY;
import static dev.ragnarok.fenrir.api.model.VkApiPostSource.Data.PROFILE_PHOTO;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.AttachmentsHolder;
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.CircleCounterButton;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;

public class FavePostAdapter extends RecyclerBindableAdapter<Post, RecyclerView.ViewHolder> {

    private static final int TYPE_NORMAL = 0;

    private final Context mContext;
    private final AttachmentsViewBinder attachmentsViewBinder;
    private final Transformation transformation;
    private final ClickListener clickListener;
    private final LinkActionAdapter mLinkActionAdapter;
    private RecyclerView recyclerView;
    private EmojiconTextView.OnHashTagClickListener mOnHashTagClickListener;

    public FavePostAdapter(Context context, List<Post> items, @NonNull AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback,
                           @NonNull ClickListener adapterListener) {
        super(items);
        mContext = context;
        attachmentsViewBinder = new AttachmentsViewBinder(context, attachmentsActionCallback);
        transformation = CurrentTheme.createTransformationForAvatar();
        clickListener = adapterListener;
        mLinkActionAdapter = new LinkActionAdapter() {
            @Override
            public void onOwnerClick(int ownerId) {
                clickListener.onAvatarClick(ownerId);
            }
        };
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position, int type) {
        Post item = getItem(position);
        if (type == TYPE_NORMAL) {
            NormalHolder normalHolder = (NormalHolder) viewHolder;
            configNormalPost(normalHolder, item);
            fillNormalPostButtonsBlock(normalHolder, item);
        }
    }

    private void configNormalPost(AbsPostHolder holder, Post post) {
        attachmentsViewBinder.displayAttachments(post.getAttachments(), holder.attachmentContainers, false, null);
        attachmentsViewBinder.displayCopyHistory(post.getCopyHierarchy(), holder.attachmentContainers.getVgPosts(), true, R.layout.item_copy_history_post);

        holder.tvOwnerName.setText(post.getAuthorName());

        String reduced = AppTextUtils.reduceStringForPost(post.getText());
        holder.tvText.setText(OwnerLinkSpanFactory.withSpans(reduced, true, false, mLinkActionAdapter));

        holder.tvShowMore.setVisibility(post.hasText() && post.getText().length() > 400 ? View.VISIBLE : View.GONE);

        holder.tvText.setVisibility(post.hasText() ? View.VISIBLE : View.GONE);
        holder.vTextContainer.setVisibility(post.hasText() ? View.VISIBLE : View.GONE);

        String ownerAvaUrl = post.getAuthorPhoto();
        ViewUtils.displayAvatar(holder.ivOwnerAvatar, transformation, ownerAvaUrl, Constants.PICASSO_TAG);

        holder.ivOwnerAvatar.setOnClickListener(v -> clickListener.onAvatarClick(post.getAuthorId()));

        holder.ivFriendOnly.setVisibility(post.isFriendsOnly() ? View.VISIBLE : View.GONE);

        boolean displaySigner = post.getSignerId() > 0 && nonNull(post.getCreator());

        holder.vSignerRoot.setVisibility(displaySigner ? View.VISIBLE : View.GONE);

        if (displaySigner) {
            holder.tvSignerName.setText(post.getCreator().getFullName());
            ViewUtils.displayAvatar(holder.ivSignerIcon, transformation, post.getCreator().get100photoOrSmaller(), Constants.PICASSO_TAG);

            holder.vSignerRoot.setOnClickListener(v -> clickListener.onAvatarClick(post.getSignerId()));
        }

        holder.root.setOnClickListener(v -> clickListener.onPostClick(post));

        holder.topDivider.setVisibility(View.GONE);

        if (holder.viewCounter != null) {
            holder.viewCounter.setVisibility(post.getViewCount() > 0 ? View.VISIBLE : View.GONE);
            holder.viewCounter.setText(String.valueOf(post.getViewCount()));
        }
    }

    private void fillNormalPostButtonsBlock(NormalHolder holder, Post post) {
        holder.pinRoot.setVisibility(post.isPinned() ? View.VISIBLE : View.GONE);

        String formattedDate = AppTextUtils.getDateFromUnixTime(mContext, post.getDate());
        String postSubtitle = formattedDate;

        if (post.getSource() != null) {
            switch (post.getSource().getData()) {
                case PROFILE_ACTIVITY:
                    postSubtitle = mContext.getString(R.string.updated_status_at, formattedDate);
                    break;
                case PROFILE_PHOTO:
                    postSubtitle = mContext.getString(R.string.updated_profile_photo_at, formattedDate);
                    break;
            }
        }
        holder.tvTime.setText(postSubtitle);

        holder.likeButton.setIcon(post.isUserLikes() ? R.drawable.heart_filled : R.drawable.heart);
        holder.likeButton.setActive(post.isUserLikes());
        holder.likeButton.setCount(post.getLikesCount());

        holder.likeButton.setOnClickListener(v -> clickListener.onLikeClick(post));

        holder.likeButton.setOnLongClickListener(v -> {
            clickListener.onLikeLongClick(post);
            return true;
        });

        holder.commentsButton.setVisibility(post.isCanPostComment() || post.getCommentsCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.commentsButton.setCount(post.getCommentsCount());
        holder.commentsButton.setOnClickListener(view -> clickListener.onCommentsClick(post));

        holder.shareButton.setActive(post.isUserReposted());
        holder.shareButton.setCount(post.getRepostCount());
        holder.shareButton.setOnClickListener(v -> clickListener.onShareClick(post));

        holder.shareButton.setOnLongClickListener(v -> {
            clickListener.onShareLongClick(post);
            return true;
        });
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        if (type == TYPE_NORMAL) {
            return new NormalHolder(view);
        }

        throw new IllegalArgumentException();
    }

    @Override
    protected int layoutId(int type) {
        if (type == TYPE_NORMAL) {
            return R.layout.item_post_normal;
        }

        throw new IllegalArgumentException();
    }

    public void setOnHashTagClickListener(EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
        mOnHashTagClickListener = onHashTagClickListener;
    }

    @Override
    protected int getItemType(int position) {
        return TYPE_NORMAL;
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

    public interface ClickListener {
        void onAvatarClick(int ownerId);

        void onShareClick(Post post);

        void onPostClick(Post post);

        void onCommentsClick(Post post);

        void onLikeLongClick(Post post);

        void onShareLongClick(Post post);

        void onLikeClick(Post post);

        void onDelete(int index, Post post);
    }

    private abstract class AbsPostHolder extends RecyclerView.ViewHolder {

        final View root;
        final View topDivider;
        final TextView tvOwnerName;
        final ImageView ivOwnerAvatar;
        final View vTextContainer;
        final EmojiconTextView tvText;
        final TextView tvShowMore;
        final TextView tvTime;
        final ImageView ivFriendOnly;
        final TextView viewCounter;
        final View vSignerRoot;
        final ImageView ivSignerIcon;
        final TextView tvSignerName;

        final AttachmentsHolder attachmentContainers;

        AbsPostHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.card_view);
            topDivider = itemView.findViewById(R.id.top_divider);
            ivOwnerAvatar = itemView.findViewById(R.id.item_post_avatar);
            tvOwnerName = itemView.findViewById(R.id.item_post_owner_name);
            vTextContainer = itemView.findViewById(R.id.item_text_container);
            tvText = itemView.findViewById(R.id.item_post_text);
            tvText.setOnHashTagClickListener(mOnHashTagClickListener);
            tvShowMore = itemView.findViewById(R.id.item_post_show_more);
            tvTime = itemView.findViewById(R.id.item_post_time);

            ivFriendOnly = itemView.findViewById(R.id.item_post_friends_only);

            vSignerRoot = itemView.findViewById(R.id.item_post_signer_root);
            ivSignerIcon = itemView.findViewById(R.id.item_post_signer_icon);
            tvSignerName = itemView.findViewById(R.id.item_post_signer_name);
            attachmentContainers = AttachmentsHolder.forPost((ViewGroup) itemView);

            viewCounter = itemView.findViewById(R.id.post_views_counter);
        }
    }

    private class NormalHolder extends AbsPostHolder implements View.OnCreateContextMenuListener {

        final View pinRoot;
        final CircleCounterButton likeButton;
        final CircleCounterButton shareButton;
        final CircleCounterButton commentsButton;

        NormalHolder(View view) {
            super(view);
            itemView.setOnCreateContextMenuListener(this);
            pinRoot = root.findViewById(R.id.item_post_normal_pin);
            likeButton = root.findViewById(R.id.like_button);
            commentsButton = root.findViewById(R.id.comments_button);
            shareButton = root.findViewById(R.id.share_button);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = recyclerView.getChildAdapterPosition(v);
            Post post = getItems().get(position);
            menu.setHeaderTitle(post.getAuthorName());

            menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
                if (clickListener != null) {
                    clickListener.onDelete(position, post);
                }
                return true;
            });
        }
    }
}
