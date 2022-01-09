package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

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
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class PeopleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int STATUS_COLOR_OFFLINE = Color.parseColor("#999999");
    private static final int TYPE_USER = 0;
    private static final int TYPE_COMMUNITY = 1;
    private final Context mContext;
    private final Transformation transformation;
    private List<? extends Owner> mData;
    private ClickListener mClickListener;
    private LongClickListener longClickListener;

    public PeopleAdapter(Context context, List<? extends Owner> data) {
        mContext = context;
        mData = data;
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_USER:
                return new PeopleHolder(LayoutInflater.from(mContext).inflate(R.layout.item_people, parent, false));
            case TYPE_COMMUNITY:
                return new CommunityHolder(LayoutInflater.from(mContext).inflate(R.layout.item_group, parent, false));
        }
        throw new RuntimeException("OwnersAdapter.onCreateViewHolder");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_USER:
                bindUserHolder((PeopleHolder) holder, (User) mData.get(position));
                break;
            case TYPE_COMMUNITY:
                bindCommunityHolder((CommunityHolder) holder, (Community) mData.get(position));
                break;
        }
    }

    private void bindCommunityHolder(CommunityHolder holder, Community community) {
        holder.tvName.setText(community.getName());
        holder.tvName.setTextColor(Utils.getVerifiedColor(mContext, community.isVerified()));
        holder.ivVerified.setVisibility(community.isVerified() ? View.VISIBLE : View.GONE);
        holder.tvType.setText(CommunitiesAdapter.getCommunityType(mContext, community));
        holder.tvMembersCount.setText(mContext.getString(R.string.members_count, (community.getMembersCount() > 0 ? (AppTextUtils.getCounterWithK(community.getMembersCount())) : "-")));

        PicassoInstance.with()
                .load(community.getMaxSquareAvatar())
                .tag(Constants.PICASSO_TAG)
                .transform(transformation)
                .into(holder.ivAvatar);

        holder.itemView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onOwnerClick(community);
            }
        });
    }

    private void bindUserHolder(PeopleHolder holder, User user) {
        holder.name.setText(user.getFullName());

        holder.subtitle.setText(UserInfoResolveUtil.getUserActivityLine(mContext, user, true));
        holder.subtitle.setTextColor(user.isOnline() ? CurrentTheme.getColorPrimary(mContext) : STATUS_COLOR_OFFLINE);

        holder.name.setTextColor(Utils.getVerifiedColor(mContext, user.isVerified()));
        holder.ivVerified.setVisibility(user.isVerified() ? View.VISIBLE : View.GONE);
        holder.blacklisted.setVisibility(user.getBlacklisted() ? View.VISIBLE : View.GONE);

        holder.online.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
        Integer onlineIcon = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if (onlineIcon != null) {
            holder.online.setImageResource(onlineIcon);
        } else {
            holder.online.setImageDrawable(null);
        }

        String avaUrl = user.getMaxSquareAvatar();
        ViewUtils.displayAvatar(holder.avatar, transformation, avaUrl, Constants.PICASSO_TAG);

        holder.itemView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onOwnerClick(user);
            }
        });

        holder.itemView.setOnLongClickListener(v -> nonNull(longClickListener) && longClickListener.onOwnerLongClick(user));

        SelectionUtils.addSelectionProfileSupport(mContext, holder.avatarRoot, user);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setItems(List<? extends Owner> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) instanceof User ? TYPE_USER : TYPE_COMMUNITY;
    }

    public PeopleAdapter setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
        return this;
    }

    public void setClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public interface ClickListener {
        void onOwnerClick(Owner owner);
    }

    public interface LongClickListener {
        boolean onOwnerLongClick(Owner owner);
    }

    private static class CommunityHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvType;
        private final TextView tvMembersCount;
        private final ImageView ivAvatar;
        private final ImageView ivVerified;

        CommunityHolder(View root) {
            super(root);
            tvName = root.findViewById(R.id.item_group_name);
            tvType = root.findViewById(R.id.item_group_type);
            tvMembersCount = root.findViewById(R.id.item_group_members);
            ivAvatar = root.findViewById(R.id.item_group_avatar);
            ivVerified = itemView.findViewById(R.id.item_verified);
        }
    }

    private class PeopleHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final TextView subtitle;
        private final ImageView avatar;
        private final ImageView online;
        private final ImageView blacklisted;
        private final ImageView ivVerified;
        private final ViewGroup avatarRoot;

        PeopleHolder(View itemView) {
            super(itemView);
            avatarRoot = itemView.findViewById(R.id.avatar_root);
            name = itemView.findViewById(R.id.item_people_name);
            subtitle = itemView.findViewById(R.id.item_people_subtitle);
            avatar = itemView.findViewById(R.id.item_people_avatar);
            online = itemView.findViewById(R.id.item_people_online);
            ivVerified = itemView.findViewById(R.id.item_verified);
            blacklisted = itemView.findViewById(R.id.item_blacklisted);
            Utils.setColorFilter(online, CurrentTheme.getColorPrimary(mContext));
        }
    }
}
