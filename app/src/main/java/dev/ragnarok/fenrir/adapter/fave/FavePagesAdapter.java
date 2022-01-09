package dev.ragnarok.fenrir.adapter.fave;

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

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SelectionUtils;
import dev.ragnarok.fenrir.model.FavePage;
import dev.ragnarok.fenrir.model.FavePageType;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.AspectRatioImageView;
import dev.ragnarok.fenrir.view.OnlineView;

public class FavePagesAdapter extends RecyclerView.Adapter<FavePagesAdapter.Holder> {

    private final Context context;
    private final Transformation transformation;
    private List<FavePage> data;
    private RecyclerView recyclerView;
    private ClickListener clickListener;

    public FavePagesAdapter(List<FavePage> data, Context context) {
        this.data = data;
        this.context = context;
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_fave_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        FavePage favePage = data.get(position);

        if (Settings.get().other().isMention_fave()) {
            holder.itemView.setOnLongClickListener(view -> {
                if (clickListener != null && favePage.getId() >= 0) {
                    clickListener.onMention(favePage.getOwner());
                }
                return true;
            });
        }

        holder.description.setText(favePage.getDescription());
        holder.name.setText(favePage.getOwner().getFullName());
        holder.name.setTextColor(Utils.getVerifiedColor(context, favePage.getOwner().isVerified()));
        holder.ivVerified.setVisibility(favePage.getOwner().isVerified() ? View.VISIBLE : View.GONE);
        ViewUtils.displayAvatar(holder.avatar, transformation, favePage.getOwner().getMaxSquareAvatar(), Constants.PICASSO_TAG);

        if (favePage.getType().equals(FavePageType.USER)) {
            holder.ivOnline.setVisibility(View.VISIBLE);
            User user = favePage.getUser();
            holder.blacklisted.setVisibility(user.getBlacklisted() ? View.VISIBLE : View.GONE);
            Integer onlineIcon = ViewUtils.getOnlineIcon(true, user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
            if (!user.isOnline())
                holder.ivOnline.setCircleColor(CurrentTheme.getColorFromAttrs(R.attr.icon_color_inactive, context, "#000000"));
            else
                holder.ivOnline.setCircleColor(CurrentTheme.getColorFromAttrs(R.attr.icon_color_active, context, "#000000"));

            if (onlineIcon != null) {
                holder.ivOnline.setIcon(onlineIcon);
            }
        } else {
            holder.name.setTextColor(Utils.getVerifiedColor(context, false));
            holder.ivOnline.setVisibility(View.GONE);
            holder.blacklisted.setVisibility(View.GONE);
        }

        SelectionUtils.addSelectionProfileSupport(context, holder.avatar_root, favePage);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPageClick(holder.getBindingAdapterPosition(), favePage.getOwner());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<FavePage> data) {
        this.data = data;
        notifyDataSetChanged();
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
        void onPageClick(int index, Owner owner);

        void onDelete(int index, Owner owner);

        void onPushFirst(int index, Owner owner);

        void onMention(@NonNull Owner owner);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        final ViewGroup avatar_root;
        final AspectRatioImageView avatar;
        final ImageView blacklisted;
        final TextView name;
        final TextView description;
        final OnlineView ivOnline;
        final ImageView ivVerified;

        public Holder(View itemView) {
            super(itemView);
            if (!Settings.get().other().isMention_fave()) {
                itemView.setOnCreateContextMenuListener(this);
            }
            ivOnline = itemView.findViewById(R.id.header_navi_menu_online);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            blacklisted = itemView.findViewById(R.id.item_blacklisted);
            ivVerified = itemView.findViewById(R.id.item_verified);
            avatar_root = itemView.findViewById(R.id.avatar_root);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = recyclerView.getChildAdapterPosition(v);
            FavePage favePage = data.get(position);
            menu.setHeaderTitle(favePage.getOwner().getFullName());

            menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
                if (clickListener != null) {
                    clickListener.onDelete(position, favePage.getOwner());
                }
                return true;
            });
            menu.add(0, v.getId(), 0, R.string.push_first).setOnMenuItemClickListener(item -> {
                if (clickListener != null) {
                    clickListener.onPushFirst(position, favePage.getOwner());
                }
                return true;
            });
        }
    }
}
