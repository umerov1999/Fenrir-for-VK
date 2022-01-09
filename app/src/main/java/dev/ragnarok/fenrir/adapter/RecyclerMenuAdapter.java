package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Icon;
import dev.ragnarok.fenrir.model.menu.AdvancedItem;
import dev.ragnarok.fenrir.model.menu.Section;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.view.ColorFilterImageView;

public class RecyclerMenuAdapter extends RecyclerView.Adapter<RecyclerMenuAdapter.MenuItemHolder> {

    @LayoutRes
    private final int itemRes;
    private List<AdvancedItem> items;
    private ActionListener actionListener;

    public RecyclerMenuAdapter(@LayoutRes int itemLayout, @NonNull List<AdvancedItem> items) {
        itemRes = itemLayout;
        this.items = items;
    }

    public RecyclerMenuAdapter(List<AdvancedItem> items) {
        this.items = items;
        itemRes = R.layout.item_advanced_menu;
    }

    public void setItems(List<AdvancedItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public MenuItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuItemHolder(LayoutInflater.from(parent.getContext()).inflate(itemRes, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemHolder holder, int position) {
        onBindMenuItemHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private AdvancedItem getItem(int position) {
        return items.get(position);
    }

    private void onBindMenuItemHolder(MenuItemHolder holder, int position) {
        Context context = holder.itemView.getContext();

        AdvancedItem item = getItem(position);

        AssertUtils.requireNonNull(item);

        Section section = item.getSection();

        boolean headerVisible;
        if (isNull(section)) {
            headerVisible = false;
        } else if (position == 0) {
            headerVisible = true;
        } else {
            AdvancedItem previous = getItem(position - 1);
            AssertUtils.requireNonNull(previous);

            headerVisible = section != previous.getSection();
        }

        holder.headerRoot.setOnClickListener(v -> {/*dummy*/});

        if (headerVisible) {
            holder.headerRoot.setVisibility(View.VISIBLE);
            holder.headerText.setText(section.getTitle().getText(context));

            if (nonNull(section.getIcon())) {
                holder.headerIcon.setVisibility(View.VISIBLE);
                holder.headerIcon.setImageResource(section.getIcon());
            } else {
                holder.headerIcon.setVisibility(View.GONE);
            }
        } else {
            holder.headerRoot.setVisibility(View.GONE);
        }

        holder.itemOffsetView.setVisibility(nonNull(section) ? View.VISIBLE : View.GONE);

        bindIcon(holder.itemIcon, item.getIcon());

        holder.itemTitle.setText(item.getTitle().getText(context));
        holder.itemSubtitle.setVisibility(isNull(item.getSubtitle()) ? View.GONE : View.VISIBLE);
        holder.itemSubtitle.setText(item.getSubtitle() == null ? null : item.getSubtitle().getText(context));

        boolean last = position == getItemCount() - 1;

        boolean dividerVisible;

        if (last) {
            dividerVisible = false;
        } else {
            AdvancedItem next = getItem(position + 1);
            AssertUtils.requireNonNull(next);

            dividerVisible = next.getSection() != section;
        }

        holder.divider.setVisibility(dividerVisible ? View.VISIBLE : View.GONE);

        holder.itemRoot.setOnClickListener(v -> {
            if (nonNull(actionListener)) {
                actionListener.onClick(item);
            }
        });

        holder.itemRoot.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onLongClick(item);
            }
            return true;
        });
    }

    private void bindIcon(ColorFilterImageView imageView, Icon icon) {
        if (nonNull(icon)) {
            imageView.setVisibility(View.VISIBLE);

            if (icon.isRemote()) {
                imageView.setColorFilterEnabled(false);
                PicassoInstance.with()
                        .load(icon.getUrl())
                        .transform(CurrentTheme.createTransformationForAvatar())
                        .into(imageView);
            } else {
                imageView.setColorFilterEnabled(true);
                PicassoInstance.with().cancelRequest(imageView);
                imageView.setImageResource(icon.getRes());
            }
        } else {
            PicassoInstance.with().cancelRequest(imageView);
            imageView.setVisibility(View.GONE);
        }
    }

    public interface ActionListener {
        void onClick(AdvancedItem item);

        void onLongClick(AdvancedItem item);
    }

    static class MenuItemHolder extends RecyclerView.ViewHolder {

        final View headerRoot;
        final ImageView headerIcon;
        final TextView headerText;

        final View itemOffsetView;
        final ColorFilterImageView itemIcon;
        final TextView itemTitle;
        final TextView itemSubtitle;
        final View itemRoot;
        final View divider;

        MenuItemHolder(View itemView) {
            super(itemView);
            headerRoot = itemView.findViewById(R.id.header_root);
            headerIcon = itemView.findViewById(R.id.header_icon);
            headerText = itemView.findViewById(R.id.header_text);
            itemRoot = itemView.findViewById(R.id.item_root);
            itemOffsetView = itemView.findViewById(R.id.item_offset);
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemSubtitle = itemView.findViewById(R.id.item_subtitle);

            divider = itemView.findViewById(R.id.divider);
        }
    }
}