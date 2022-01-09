package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.graphics.Color;
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
import dev.ragnarok.fenrir.activity.SelectionUtils;
import dev.ragnarok.fenrir.fragment.UserInfoResolveUtil;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UsersPart;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class FriendsRecycleAdapter extends RecyclerView.Adapter<FriendsRecycleAdapter.Holder> {

    private static final int STATUS_COLOR_OFFLINE = Color.parseColor("#999999");
    private final Context context;
    private final Transformation transformation;
    private List<UsersPart> data;
    private boolean group;
    private Listener listener;

    public FriendsRecycleAdapter(List<UsersPart> data, Context context) {
        this.data = data;
        this.context = context;
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_new_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ItemInfo itemInfo = get(position);
        User user = itemInfo.user;

        boolean headerVisible = group && itemInfo.first;
        holder.header.setVisibility(headerVisible ? View.VISIBLE : View.GONE);

        if (headerVisible) {
            holder.headerCount.setText(String.valueOf(itemInfo.fullSectionCount));
            holder.headerTitle.setText(itemInfo.sectionTitleRes);
        }

        holder.name.setText(user.getFullName());
        holder.name.setTextColor(Utils.getVerifiedColor(context, user.isVerified()));

        holder.status.setText(UserInfoResolveUtil.getUserActivityLine(context, user, true));
        holder.status.setTextColor(user.isOnline() ? CurrentTheme.getColorPrimary(context) : STATUS_COLOR_OFFLINE);

        holder.online.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
        Integer onlineIcon = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if (onlineIcon != null) {
            holder.online.setImageResource(onlineIcon);
        }
        ViewUtils.displayAvatar(holder.avatar, transformation, user.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });

        SelectionUtils.addSelectionProfileSupport(context, holder.avatarRoot, user);

        holder.ivVerified.setVisibility(user.isVerified() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (UsersPart pair : data) {
            if (!pair.enable) {
                continue;
            }

            count = count + pair.users.size();
        }

        return count;
    }

    private ItemInfo get(int position) throws IllegalArgumentException {
        int offset = 0;
        for (UsersPart pair : data) {
            if (!pair.enable) {
                continue;
            }

            int newOffset = offset + pair.users.size();
            if (position < newOffset) {
                int internalPosition = position - offset;
                boolean first = internalPosition == 0;
                int displayCount = pair.displayCount == null ? pair.users.size() : pair.displayCount;
                return new ItemInfo(pair.users.get(internalPosition), first, displayCount, pair.titleResId);
            }

            offset = newOffset;
        }

        throw new IllegalArgumentException("Invalid adapter position");
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public void setData(List<UsersPart> data, boolean grouping) {
        this.data = data;
        group = grouping;
        notifyDataSetChanged();
    }

    public interface Listener {
        void onUserClick(User user);
    }

    private static class ItemInfo {

        final User user;
        final boolean first;
        final int fullSectionCount;
        final int sectionTitleRes;

        ItemInfo(User user, boolean first, int fullSectionCount, int sectionTitleRes) {
            this.user = user;
            this.first = first;
            this.fullSectionCount = fullSectionCount;
            this.sectionTitleRes = sectionTitleRes;
        }
    }

    public class Holder extends RecyclerView.ViewHolder {

        final View header;
        final TextView headerTitle;
        final TextView headerCount;

        final TextView name;
        final TextView status;
        final ViewGroup avatarRoot;
        final ImageView avatar;
        final ImageView online;
        final ImageView ivVerified;

        public Holder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            headerTitle = itemView.findViewById(R.id.title);
            headerCount = itemView.findViewById(R.id.count);
            name = itemView.findViewById(R.id.item_friend_name);
            status = itemView.findViewById(R.id.item_friend_status);
            avatar = itemView.findViewById(R.id.item_friend_avatar);
            avatarRoot = itemView.findViewById(R.id.item_friend_avatar_container);
            online = itemView.findViewById(R.id.item_friend_online);
            ivVerified = itemView.findViewById(R.id.item_verified);
            Utils.setColorFilter(online, CurrentTheme.getColorPrimary(context));
        }
    }
}
