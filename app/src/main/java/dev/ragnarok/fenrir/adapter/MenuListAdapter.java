package dev.ragnarok.fenrir.adapter;

import android.content.Context;
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
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem;
import dev.ragnarok.fenrir.model.drawer.IconMenuItem;
import dev.ragnarok.fenrir.model.drawer.NoIconMenuItem;
import dev.ragnarok.fenrir.model.drawer.RecentChat;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class MenuListAdapter extends RecyclerBindableAdapter<AbsMenuItem, RecyclerView.ViewHolder> {

    private final ActionListener actionListener;
    private final int colorPrimary;
    private final int colorSurface;
    private final int colorOnPrimary;
    private final int colorOnSurface;
    private final int dp;
    private final Transformation transformation;
    private final boolean paging;
    private final boolean noStroke;

    public MenuListAdapter(@NonNull Context context, @NonNull List<AbsMenuItem> pageItems, @NonNull ActionListener actionListener, boolean paging) {
        super(pageItems);
        colorPrimary = CurrentTheme.getColorPrimary(context);
        colorSurface = CurrentTheme.getColorSurface(context);
        colorOnPrimary = CurrentTheme.getColorOnPrimary(context);
        colorOnSurface = CurrentTheme.getColorOnSurface(context);
        dp = (int) Utils.dpToPx(1, context);
        transformation = CurrentTheme.createTransformationForAvatar();
        this.actionListener = actionListener;
        this.paging = paging;
        noStroke = Settings.get().other().is_side_no_stroke();
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position, int type) {
        AbsMenuItem item = getItem(position);
        holder.itemView.setSelected(item.isSelected());

        switch (type) {
            case AbsMenuItem.TYPE_ICON:
                if ((paging || !noStroke)) {
                    bindIconHolder((NormalHolder) holder, (IconMenuItem) item);
                } else {
                    bindIconNoStrokeHolder((NormalNoStrokeHolder) holder, (IconMenuItem) item);
                }
                break;
            case AbsMenuItem.TYPE_RECENT_CHAT:
                bindRecentChat((RecentChatHolder) holder, (RecentChat) item);
                break;
            case AbsMenuItem.TYPE_WITHOUT_ICON:
                bindWithoutIcon((NoIconHolder) holder, (NoIconMenuItem) item);
                break;
        }
    }

    private void bindWithoutIcon(NoIconHolder holder, NoIconMenuItem item) {
        holder.txTitle.setText(item.getTitle());
        holder.txTitle.setTextColor(item.isSelected() ? colorOnPrimary : colorOnSurface);
        holder.contentRoot.setOnClickListener(v -> actionListener.onDrawerItemClick(item));
        holder.contentRoot.setOnLongClickListener(view -> {
            actionListener.onDrawerItemLongClick(item);
            return true;
        });
    }

    private void bindIconNoStrokeHolder(NormalNoStrokeHolder holder, IconMenuItem item) {
        holder.txtTitle.setText(item.getTitle());
        holder.txtTitle.setTextColor(item.isSelected() ? colorOnPrimary : colorOnSurface);

        holder.tvCount.setVisibility(item.getCount() > 0 ? View.VISIBLE : View.GONE);
        holder.tvCount.setText(String.valueOf(item.getCount()));
        holder.tvCount.setTextColor(item.isSelected() ? colorOnPrimary : colorPrimary);

        holder.imgIcon.setImageResource(item.getIcon());
        holder.imgIcon.setColorFilter(item.isSelected() ? colorOnPrimary : colorOnSurface);

        holder.contentRoot.setBackgroundColor(item.isSelected() ? colorPrimary : colorSurface);
        holder.contentRoot.setOnClickListener(v -> actionListener.onDrawerItemClick(item));
        holder.contentRoot.setOnLongClickListener(view -> {
            actionListener.onDrawerItemLongClick(item);
            return true;
        });
    }

    private void bindIconHolder(NormalHolder holder, IconMenuItem item) {
        holder.txtTitle.setText(item.getTitle());
        holder.txtTitle.setTextColor(item.isSelected() ? colorOnPrimary : colorOnSurface);

        holder.tvCount.setVisibility(item.getCount() > 0 ? View.VISIBLE : View.GONE);
        holder.tvCount.setText(String.valueOf(item.getCount()));
        holder.tvCount.setTextColor(item.isSelected() ? colorOnPrimary : colorPrimary);

        holder.imgIcon.setImageResource(item.getIcon());
        holder.imgIcon.setColorFilter(item.isSelected() ? colorOnPrimary : colorOnSurface);

        holder.contentRoot.setCardBackgroundColor(item.isSelected() ? colorPrimary : colorSurface);
        holder.contentRoot.setStrokeWidth(item.isSelected() ? 0 : dp);
        holder.contentRoot.setOnClickListener(v -> actionListener.onDrawerItemClick(item));
        holder.contentRoot.setOnLongClickListener(view -> {
            actionListener.onDrawerItemLongClick(item);
            return true;
        });
    }

    private void bindRecentChat(RecentChatHolder holder, RecentChat item) {
        holder.tvChatTitle.setText(item.getTitle());
        holder.tvChatTitle.setTextColor(item.isSelected() ? colorOnPrimary : colorOnSurface);

        if (Utils.isEmpty(item.getIconUrl())) {
            PicassoInstance.with()
                    .load(R.drawable.ic_group_chat)
                    .transform(transformation)
                    .into(holder.ivChatImage);
        } else {
            PicassoInstance.with()
                    .load(item.getIconUrl())
                    .transform(transformation)
                    .into(holder.ivChatImage);
        }

        ((MaterialCardView) holder.contentRoot).setCardBackgroundColor(item.isSelected() ? colorPrimary : colorSurface);
        holder.contentRoot.setOnClickListener(v -> actionListener.onDrawerItemClick(item));
        holder.contentRoot.setOnLongClickListener(view -> {
            actionListener.onDrawerItemLongClick(item);
            return true;
        });
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        switch (type) {
            case AbsMenuItem.TYPE_DIVIDER:
                return new DividerHolder(view);
            case AbsMenuItem.TYPE_RECENT_CHAT:
                return new RecentChatHolder(view);
            case AbsMenuItem.TYPE_ICON:
                return (paging || !noStroke) ? new NormalHolder(view) : new NormalNoStrokeHolder(view);
            case AbsMenuItem.TYPE_WITHOUT_ICON:
                return new NoIconHolder(view);
        }
        throw new IllegalStateException();
    }

    @Override
    protected int layoutId(int type) {
        switch (type) {
            case AbsMenuItem.TYPE_DIVIDER:
                return (noStroke ? R.layout.drawer_list_item_divider_no_stroke : R.layout.drawer_list_item_divider);
            case AbsMenuItem.TYPE_RECENT_CHAT:
                return R.layout.item_navigation_recents;
            case AbsMenuItem.TYPE_ICON:
                return paging ? R.layout.item_navigation : (noStroke ? R.layout.drawer_list_item_no_stroke : R.layout.drawer_list_item);
            case AbsMenuItem.TYPE_WITHOUT_ICON:
                return R.layout.drawer_list_item_without_icon;
        }

        throw new IllegalStateException();
    }

    @Override
    protected int getItemType(int position) {
        return getItem(position - getHeadersCount()).getType();
    }

    public interface ActionListener {
        void onDrawerItemClick(AbsMenuItem item);

        void onDrawerItemLongClick(AbsMenuItem item);
    }

    private static class NormalHolder extends RecyclerView.ViewHolder {

        final ImageView imgIcon;
        final TextView txtTitle;
        final TextView tvCount;
        final MaterialCardView contentRoot;

        NormalHolder(View view) {
            super(view);
            contentRoot = view.findViewById(R.id.content_root);
            imgIcon = view.findViewById(R.id.icon);
            txtTitle = view.findViewById(R.id.title);
            tvCount = view.findViewById(R.id.counter);
        }
    }

    private static class NormalNoStrokeHolder extends RecyclerView.ViewHolder {

        final ImageView imgIcon;
        final TextView txtTitle;
        final TextView tvCount;
        final ViewGroup contentRoot;

        NormalNoStrokeHolder(View view) {
            super(view);
            contentRoot = view.findViewById(R.id.content_root);
            imgIcon = view.findViewById(R.id.icon);
            txtTitle = view.findViewById(R.id.title);
            tvCount = view.findViewById(R.id.counter);
        }
    }

    private static class DividerHolder extends RecyclerView.ViewHolder {

        DividerHolder(View itemView) {
            super(itemView);
        }
    }

    private static class RecentChatHolder extends RecyclerView.ViewHolder {

        final TextView tvChatTitle;
        final ImageView ivChatImage;
        final View contentRoot;

        RecentChatHolder(View itemView) {
            super(itemView);
            contentRoot = itemView.findViewById(R.id.content_root);
            tvChatTitle = itemView.findViewById(R.id.title);
            ivChatImage = itemView.findViewById(R.id.avatar);
        }
    }

    private static class NoIconHolder extends RecyclerView.ViewHolder {
        TextView txTitle;
        View contentRoot;

        NoIconHolder(View view) {
            super(view);
            contentRoot = view.findViewById(R.id.content_root);
            txTitle = view.findViewById(R.id.title);
        }
    }
}
