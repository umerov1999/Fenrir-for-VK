package dev.ragnarok.fenrir.adapter;

import android.content.Context;
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
import dev.ragnarok.fenrir.adapter.multidata.MultyDataAdapter;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.DataWrapper;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class CommunitiesAdapter extends MultyDataAdapter<Community, CommunitiesAdapter.Holder> {

    private static final ItemInfo<Community> INFO = new ItemInfo<>();
    private final Transformation transformation;
    private final Context context;
    private ActionListener actionListener;

    public CommunitiesAdapter(Context context, List<DataWrapper<Community>> dataWrappers, Integer[] titles) {
        super(dataWrappers, titles);
        transformation = CurrentTheme.createTransformationForAvatar();
        this.context = context;
    }

    public static String getCommunityType(@NonNull Context context, @NonNull Community community) {
        switch (community.getType()) {
            case VKApiCommunity.Type.EVENT:
                return context.getString((community.getClosed() == VKApiCommunity.Status.OPEN ? R.string.type_opened : R.string.type_closed), context.getString(R.string.type_event));
            case VKApiCommunity.Type.PAGE:
                return context.getString(R.string.type_page);
        }
        return context.getString((community.getClosed() == VKApiCommunity.Status.OPEN ? R.string.type_opened : R.string.type_closed), context.getString(R.string.type_community));
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new Holder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_community, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        get(position, INFO);

        Community community = INFO.item;
        holder.headerRoot.setVisibility(INFO.internalPosition == 0 ? (INFO.sectionTitleRes == null ? View.GONE : View.VISIBLE) : View.GONE);
        if (INFO.sectionTitleRes != null) {
            holder.headerTitle.setText(INFO.sectionTitleRes);
        }

        ViewUtils.displayAvatar(holder.ivAvatar, transformation, community.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        holder.tvName.setText(community.getFullName());
        holder.tvName.setTextColor(Utils.getVerifiedColor(context, community.isVerified()));
        holder.ivVerified.setVisibility(community.isVerified() ? View.VISIBLE : View.GONE);
        holder.subtitle.setText(getCommunityType(context, community));

        SelectionUtils.addSelectionProfileSupport(context, holder.avatar_root, community);

        holder.contentRoot.setOnClickListener(view -> {
            if (Objects.nonNull(actionListener)) {
                actionListener.onCommunityClick(community);
            }
        });
        holder.contentRoot.setOnLongClickListener(view -> {
            if (Objects.nonNull(actionListener)) {
                return actionListener.onCommunityLongClick(community);
            }
            return false;
        });
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface ActionListener {
        void onCommunityClick(Community community);

        boolean onCommunityLongClick(Community community);
    }

    static class Holder extends RecyclerView.ViewHolder {

        private final ViewGroup avatar_root;
        private final View headerRoot;
        private final TextView headerTitle;

        private final View contentRoot;
        private final TextView tvName;
        private final ImageView ivAvatar;
        private final TextView subtitle;
        private final ImageView ivVerified;

        Holder(View root) {
            super(root);
            headerRoot = root.findViewById(R.id.header_root);
            headerTitle = root.findViewById(R.id.header_title);
            contentRoot = root.findViewById(R.id.content_root);
            tvName = root.findViewById(R.id.name);
            ivAvatar = root.findViewById(R.id.avatar);
            subtitle = root.findViewById(R.id.subtitle);
            avatar_root = itemView.findViewById(R.id.avatar_root);
            ivVerified = itemView.findViewById(R.id.item_verified);
        }
    }
}
