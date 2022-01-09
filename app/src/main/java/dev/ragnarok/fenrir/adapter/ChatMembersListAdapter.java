package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.AppChatUser;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserPlatform;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.OnlineView;

public class ChatMembersListAdapter extends RecyclerView.Adapter<ChatMembersListAdapter.ViewHolder> {

    private final Transformation transformation;
    private final int paddingForFirstLast;
    private List<AppChatUser> data;
    private ActionListener actionListener;
    private RecyclerView recyclerView;
    private boolean isOwner;

    public ChatMembersListAdapter(Context context, List<AppChatUser> users) {
        data = users;
        transformation = CurrentTheme.createTransformationForAvatar();
        paddingForFirstLast = Utils.is600dp(context) ? (int) Utils.dpToPx(16, context) : 0;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_list, viewGroup, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

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

        if (item.isOwner()) {
            holder.tvInvitedBy.setVisibility(View.GONE);
            holder.tvAdmin.setVisibility(View.VISIBLE);
            holder.tvAdmin.setText(R.string.creator_of_conversation);
            holder.tvInvitedDate.setVisibility(View.GONE);
        } else if (item.isAdmin()) {
            holder.tvAdmin.setVisibility(View.VISIBLE);
            holder.tvAdmin.setText(R.string.role_administrator);
            holder.tvInvitedBy.setVisibility(View.VISIBLE);
            holder.tvInvitedBy.setText(context.getString(R.string.invited_by, item.getInviter().getFullName()));
            if (item.getJoin_date() > 0) {
                holder.tvInvitedDate.setText(AppTextUtils.getDateFromUnixTime(context, item.getJoin_date()));
            } else {
                holder.tvInvitedDate.setVisibility(View.GONE);
            }
        } else {
            holder.tvInvitedBy.setVisibility(View.VISIBLE);
            holder.tvInvitedBy.setText(context.getString(R.string.invited_by, item.getInviter().getFullName()));
            holder.tvAdmin.setVisibility(View.GONE);
            if (item.getJoin_date() > 0) {
                holder.tvInvitedDate.setText(AppTextUtils.getDateFromUnixTime(context, item.getJoin_date()));
            } else {
                holder.tvInvitedDate.setVisibility(View.GONE);
            }
        }

        if (nonEmpty(user.getDomain())) {
            holder.tvDomain.setText("@" + user.getDomain());
        } else {
            holder.tvDomain.setText("@id" + user.getOwnerId());
        }

        holder.itemView.setOnClickListener(view -> {
            if (Objects.nonNull(actionListener)) {
                actionListener.onUserClick(item);
            }
        });

        holder.vRemove.setVisibility(item.isCanRemove() ? View.VISIBLE : View.GONE);
        holder.vRemove.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onRemoveClick(item);
            }
        });

        View view = holder.itemView;

        view.setPadding(view.getPaddingLeft(),
                position == 0 ? paddingForFirstLast : 0,
                view.getPaddingRight(),
                position == getItemCount() - 1 ? paddingForFirstLast : 0);
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
        void onRemoveClick(AppChatUser user);

        void onUserClick(AppChatUser user);

        void onAdminToggleClick(boolean isAdmin, int ownerId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        final OnlineView vOnline;
        final ImageView ivAvatar;
        final TextView tvName;
        final TextView tvDomain;
        final TextView tvInvitedBy;
        final TextView tvInvitedDate;
        final TextView tvAdmin;
        final View vRemove;

        ViewHolder(View root) {
            super(root);
            itemView.setOnCreateContextMenuListener(this);
            vOnline = root.findViewById(R.id.item_user_online);
            ivAvatar = root.findViewById(R.id.item_user_avatar);
            tvName = root.findViewById(R.id.item_user_name);
            tvInvitedBy = root.findViewById(R.id.item_user_invited_by);
            vRemove = root.findViewById(R.id.item_user_remove);
            tvDomain = root.findViewById(R.id.item_user_domain);
            tvInvitedDate = root.findViewById(R.id.item_user_invited_time);
            tvAdmin = root.findViewById(R.id.item_user_admin);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = recyclerView.getChildAdapterPosition(v);
            AppChatUser item = data.get(position);
            if (isOwner && !item.isOwner()) {
                menu.add(0, v.getId(), 0, item.isAdmin() ? R.string.disrate : R.string.assign_administrator).setOnMenuItemClickListener(it -> {
                    if (actionListener != null) {
                        actionListener.onAdminToggleClick(!item.isAdmin(), item.getId());
                    }
                    return true;
                });
            }
        }
    }
}
