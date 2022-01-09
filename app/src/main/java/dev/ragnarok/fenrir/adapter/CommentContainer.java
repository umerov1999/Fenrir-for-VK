package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;

import com.squareup.picasso3.Transformation;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.link.internal.TopicLink;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;

public class CommentContainer extends LinearLayout {
    private Transformation transformation;
    private int colorTextSecondary;
    private int iconColorActive;

    public CommentContainer(Context context) {
        super(context);
        init();
    }

    public CommentContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommentContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CommentContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        transformation = CurrentTheme.createTransformationForAvatar();
        colorTextSecondary = CurrentTheme.getSecondaryTextColorCode(getContext());
        iconColorActive = CurrentTheme.getColorPrimary(getContext());
    }

    public void displayComments(List<Comment> commentsData, AttachmentsViewBinder binder, CommentsAdapter.OnCommentActionListener listener, EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
        List<Comment> comments = new ArrayList<>();
        if (!Utils.isEmpty(commentsData)) {
            for (Comment i : commentsData) {
                if (!i.isDeleted()) {
                    comments.add(i);
                }
            }
        }
        setVisibility(safeIsEmpty(comments) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(comments)) {
            return;
        }

        int i = comments.size() - getChildCount();
        for (int j = 0; j < i; j++) {
            addView(LayoutInflater.from(getContext()).inflate(R.layout.item_comment_container, this, false));
        }

        for (int g = 0; g < getChildCount(); g++) {
            ViewGroup root = (ViewGroup) getChildAt(g);
            if (g < comments.size()) {
                Comment comment = comments.get(g);
                CommentHolder check = (CommentHolder) root.getTag();
                if (check == null) {
                    check = new CommentHolder(root, onHashTagClickListener);
                    root.setTag(check);
                }
                CommentHolder holder = check;
                root.setOnLongClickListener(v -> {
                    if (listener != null) {
                        listener.populateCommentContextMenu(comment);
                    }
                    return true;
                });
                holder.cancelSelectionAnimation();

                if (comment.isAnimationNow()) {
                    holder.startSelectionAnimation();
                    comment.setAnimationNow(false);
                }
                holder.tvText.setOnLongClickListener(v -> {
                    if (listener != null) {
                        listener.populateCommentContextMenu(comment);
                    }
                    return true;
                });
                if (!comment.hasAttachments()) {
                    holder.vAttachmentsRoot.setVisibility(View.GONE);
                } else {
                    holder.vAttachmentsRoot.setVisibility(View.VISIBLE);
                    binder.displayAttachments(comment.getAttachments(), holder.attachmentContainers, true, null);
                }

                holder.tvOwnerName.setText(comment.getFullAuthorName());

                Spannable text = OwnerLinkSpanFactory.withSpans(comment.getText(), true, true, new LinkActionAdapter() {
                    @Override
                    public void onTopicLinkClicked(TopicLink link) {
                        if (listener != null) {
                            listener.onReplyToOwnerClick(link.replyToOwner, link.replyToCommentId);
                        }
                    }

                    @Override
                    public void onOwnerClick(int ownerId) {
                        if (listener != null) {
                            listener.onAvatarClick(ownerId);
                        }
                    }
                });

                if (Utils.isEmpty(text) && comment.getFromId() == 0) {
                    holder.tvText.setVisibility(View.VISIBLE);
                    holder.tvText.setText(R.string.deleted);
                } else {
                    holder.tvText.setText(text, TextView.BufferType.SPANNABLE);
                    holder.tvText.setVisibility(TextUtils.isEmpty(comment.getText()) ? View.GONE : View.VISIBLE);
                    holder.tvText.setMovementMethod(LinkMovementMethod.getInstance());
                }

                holder.tvLikeCounter.setVisibility(comment.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
                TextViewCompat.setCompoundDrawableTintList(holder.tvLikeCounter, ColorStateList.valueOf(comment.isUserLikes() ? iconColorActive : colorTextSecondary));
                holder.tvLikeCounter.setText(AppTextUtils.getCounterWithK(comment.getLikesCount()));
                holder.tvLikeCounter.setVisibility(comment.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
                holder.tvLikeCounter.setTextColor(comment.isUserLikes() ? iconColorActive : colorTextSecondary);

                holder.tvTime.setMovementMethod(LinkMovementMethod.getInstance());

                ViewUtils.displayAvatar(holder.ivOwnerAvatar, transformation, comment.getMaxAuthorAvaUrl(), Constants.PICASSO_TAG);

                holder.tvTime.setText(genTimeAndReplyText(comment, listener), TextView.BufferType.SPANNABLE);
                holder.tvTime.setTextColor(colorTextSecondary);

                holder.tvLikeCounter.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCommentLikeClick(comment, !comment.isUserLikes());
                    }
                });

                holder.ivOwnerAvatar.setOnClickListener(v -> {
                    if (comment.getFromId() == 0) {
                        return;
                    }
                    if (listener != null) {
                        listener.onAvatarClick(comment.getFromId());
                    }
                });

                root.setVisibility(View.VISIBLE);
            } else {
                root.setVisibility(View.GONE);
            }
        }
    }

    private Spannable genTimeAndReplyText(Comment comment, CommentsAdapter.OnCommentActionListener listener) {
        String time = AppTextUtils.getDateFromUnixTime(comment.getDate());
        if (comment.getReplyToUser() == 0) {
            return Spannable.Factory.getInstance().newSpannable(time);
        }

        String commentText = getContext().getString(R.string.comment).toLowerCase();
        String target = getContext().getString(R.string.in_response_to, time, commentText);

        int start = target.indexOf(commentText);

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(target);
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (listener != null) {
                    listener.onReplyToOwnerClick(comment.getReplyToUser(), comment.getReplyToComment());
                }
            }
        };

        spannable.setSpan(span, start, target.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private class CommentHolder {
        final TextView tvOwnerName;
        final ImageView ivOwnerAvatar;
        final EmojiconTextView tvText;
        final TextView tvTime;
        final TextView tvLikeCounter;
        final View vAttachmentsRoot;
        final View selectionView;

        final AttachmentsHolder attachmentContainers;
        final Animator.AnimatorListener animationAdapter;
        ObjectAnimator animator;

        CommentHolder(View root, EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
            ivOwnerAvatar = root.findViewById(R.id.item_comment_owner_avatar);
            tvOwnerName = root.findViewById(R.id.item_comment_owner_name);
            tvText = root.findViewById(R.id.item_comment_text);

            tvText.setOnHashTagClickListener(hashTag -> {
                if (nonNull(onHashTagClickListener)) {
                    onHashTagClickListener.onHashTagClicked(hashTag);
                }
            });

            tvTime = root.findViewById(R.id.item_comment_time);
            tvLikeCounter = root.findViewById(R.id.item_comment_like_counter);
            selectionView = root.findViewById(R.id.item_comment_selection);
            selectionView.setBackgroundColor(CurrentTheme.getColorPrimary(getContext()));
            TextViewCompat.setCompoundDrawableTintList(tvLikeCounter, ColorStateList.valueOf(CurrentTheme.getSecondaryTextColorCode(getContext())));
            vAttachmentsRoot = root.findViewById(R.id.item_comment_attachments_root);

            attachmentContainers = AttachmentsHolder.forComment((ViewGroup) vAttachmentsRoot);
            animationAdapter = new WeakViewAnimatorAdapter<View>(selectionView) {
                @Override
                public void onAnimationEnd(View view) {
                    view.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationStart(View view) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationCancel(View view) {
                    view.setVisibility(View.INVISIBLE);
                }
            };
        }

        void startSelectionAnimation() {
            selectionView.setAlpha(0.5f);

            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f);
            animator.setDuration(1500);
            animator.addListener(animationAdapter);
            animator.start();
        }

        void cancelSelectionAnimation() {
            if (animator != null) {
                animator.cancel();
                animator = null;
            }

            selectionView.setVisibility(View.INVISIBLE);
        }
    }
}
