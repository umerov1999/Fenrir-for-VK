package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
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
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.Banned;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.FormatUtil;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.OnlineView;

public class CommunityBannedAdapter extends RecyclerView.Adapter<CommunityBannedAdapter.Holder> {

    private final Transformation transformation;
    private final OwnerLinkSpanFactory.ActionListener ownerLinkActionListener = new LinkActionAdapter();
    private List<Banned> data;
    private ActionListener actionListener;

    public CommunityBannedAdapter(List<Banned> data) {
        this.data = data;
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_ban_info, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Context context = holder.itemView.getContext();

        Banned banned = data.get(position);

        Owner bannedOwner = banned.getBanned();
        User admin = banned.getAdmin();

        Banned.Info info = banned.getInfo();

        holder.name.setText(bannedOwner.getFullName());

        ViewUtils.displayAvatar(holder.avatar, transformation, bannedOwner.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        Integer onlineViewRes = null;
        if (bannedOwner instanceof User) {
            User user = (User) bannedOwner;
            onlineViewRes = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        }

        if (Objects.nonNull(onlineViewRes)) {
            holder.onlineView.setIcon(onlineViewRes);
            holder.onlineView.setVisibility(View.VISIBLE);
        } else {
            holder.onlineView.setVisibility(View.GONE);
        }

        String comment = info.getComment();

        if (Utils.nonEmpty(comment)) {
            holder.comment.setVisibility(View.VISIBLE);

            String commentText = context.getString(R.string.ban_comment_text, comment);
            holder.comment.setText(commentText);
        } else {
            holder.comment.setVisibility(View.GONE);
        }

        Spannable spannable = FormatUtil.formatCommunityBanInfo(context, admin.getId(),
                admin.getFullName(), info.getEndDate(), ownerLinkActionListener);

        holder.dateAndAdminInfo.setMovementMethod(LinkMovementMethod.getInstance());
        holder.dateAndAdminInfo.setText(spannable, TextView.BufferType.SPANNABLE);

        holder.itemView.setOnClickListener(v -> {
            if (Objects.nonNull(actionListener)) {
                actionListener.onBannedClick(banned);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (Objects.nonNull(actionListener)) {
                actionListener.onBannedLongClick(banned);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Banned> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface ActionListener {
        void onBannedClick(Banned banned);

        void onBannedLongClick(Banned banned);
    }

    static class Holder extends RecyclerView.ViewHolder {

        final ImageView avatar;
        final OnlineView onlineView;

        final TextView name;
        final TextView dateAndAdminInfo;
        final TextView comment;

        Holder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            onlineView = itemView.findViewById(R.id.online);
            name = itemView.findViewById(R.id.name);
            dateAndAdminInfo = itemView.findViewById(R.id.date_and_admin_info);
            comment = itemView.findViewById(R.id.comment_text);
        }
    }
}
