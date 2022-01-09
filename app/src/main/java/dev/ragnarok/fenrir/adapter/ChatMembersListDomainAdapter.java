package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso3.Transformation;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.AppChatUser;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserPlatform;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.OnlineView;
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter;

public class ChatMembersListDomainAdapter extends RecyclerView.Adapter<ChatMembersListDomainAdapter.ViewHolder> {

    private final Transformation transformation;
    private final int paddingForFirstLast;
    private List<AppChatUser> data;
    private ActionListener actionListener;

    public ChatMembersListDomainAdapter(Context context, List<AppChatUser> users) {
        data = users;
        transformation = CurrentTheme.createTransformationForAvatar();
        paddingForFirstLast = Utils.is600dp(context) ? (int) Utils.dpToPx(16, context) : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_list_second, viewGroup, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppChatUser item = data.get(position);
        Owner user = item.getMember();

        boolean online = false;
        boolean onlineMobile = false;

        @UserPlatform
        int platform = UserPlatform.UNKNOWN;
        int app = 0;

        if (user instanceof User) {
            User interlocuter = (User) user;
            online = interlocuter.isOnline();
            onlineMobile = interlocuter.isOnlineMobile();
            platform = interlocuter.getPlatform();
            app = interlocuter.getOnlineApp();
        }

        Integer iconRes = ViewUtils.getOnlineIcon(online, onlineMobile, platform, app);
        holder.vOnline.setIcon(iconRes != null ? iconRes : 0);
        holder.vOnline.setVisibility(online ? View.VISIBLE : View.GONE);

        String userAvatarUrl = user.getMaxSquareAvatar();

        if (isEmpty(userAvatarUrl)) {
            PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(transformation)
                    .into(holder.ivAvatar);
        } else {
            PicassoInstance.with()
                    .load(userAvatarUrl)
                    .transform(transformation)
                    .into(holder.ivAvatar);
        }

        holder.tvName.setText(user.getFullName());

        if (nonEmpty(user.getDomain())) {
            holder.tvDomain.setText("@" + user.getDomain());
        } else {
            holder.tvDomain.setText("@id" + user.getOwnerId());
        }

        if (Objects.nonNull(actionListener)) {
            holder.itemView.setOnClickListener(view -> {
                actionListener.onUserClick(item);
                holder.startSomeAnimation();
            });
            holder.itemView.setOnLongClickListener(v -> actionListener.onUserLongClick(item));
        }

        View view = holder.itemView;

        view.setPadding(view.getPaddingLeft(),
                position == 0 ? paddingForFirstLast : 0,
                view.getPaddingRight(),
                position == getItemCount() - 1 ? paddingForFirstLast : 0);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<AppChatUser> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface ActionListener {
        void onUserClick(AppChatUser user);

        boolean onUserLongClick(AppChatUser user);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final OnlineView vOnline;
        final ImageView ivAvatar;
        final TextView tvName;
        final TextView tvDomain;
        final MaterialCardView selectionView;
        final Animator.AnimatorListener animationAdapter;
        ObjectAnimator animator;

        ViewHolder(View root) {
            super(root);
            vOnline = root.findViewById(R.id.item_user_online);
            ivAvatar = root.findViewById(R.id.item_user_avatar);
            tvName = root.findViewById(R.id.item_user_name);
            tvDomain = root.findViewById(R.id.item_user_domain);
            selectionView = root.findViewById(R.id.item_user_selection);

            animationAdapter = new WeakViewAnimatorAdapter<View>(selectionView) {
                @Override
                public void onAnimationEnd(View view) {
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(View view) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationCancel(View view) {
                    view.setVisibility(View.GONE);
                }
            };
        }

        void startSomeAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorSecondary(selectionView.getContext()));
            selectionView.setAlpha(0.5f);

            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f);
            animator.setDuration(500);
            animator.addListener(animationAdapter);
            animator.start();
        }
    }
}
