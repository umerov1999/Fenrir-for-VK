package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Icon;
import dev.ragnarok.fenrir.model.menu.Item;
import dev.ragnarok.fenrir.model.menu.Section;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.ColorFilterImageView;

public class MenuAdapter extends ArrayAdapter<Item> {
    private final boolean big;

    public MenuAdapter(@NonNull Context context, @NonNull List<Item> items, boolean big) {
        super(context, R.layout.item_custom_menu, items);
        this.big = big;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if (nonNull(convertView)) {
            view = convertView;
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(big ? R.layout.item_custom_menu_big : R.layout.item_custom_menu, parent, false);
            view.setTag(new Holder(view));
        }

        Holder holder = (Holder) view.getTag();
        Item item = getItem(position);

        AssertUtils.requireNonNull(item);

        Section section = item.getSection();

        boolean headerVisible;
        if (isNull(section)) {
            headerVisible = false;
        } else if (position == 0) {
            headerVisible = true;
        } else {
            Item previous = getItem(position - 1);
            AssertUtils.requireNonNull(previous);

            headerVisible = section != previous.getSection();
        }

        holder.headerRoot.setOnClickListener(v -> {/*dummy*/});

        if (headerVisible) {
            holder.headerRoot.setVisibility(View.VISIBLE);
            holder.headerText.setText(section.getTitle().getText(getContext()));

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
        if (item.getColor() != null) {
            Utils.setColorFilter(holder.itemIcon, item.getColor());
        } else {
            holder.itemIcon.clearColorFilter();
        }

        holder.itemText.setText(item.getTitle().getText(getContext()));

        boolean last = position == getCount() - 1;

        boolean dividerVisible;

        if (last) {
            dividerVisible = false;
        } else {
            Item next = getItem(position + 1);
            AssertUtils.requireNonNull(next);

            dividerVisible = next.getSection() != section;
        }

        holder.divider.setVisibility(dividerVisible ? View.VISIBLE : View.GONE);
        return view;
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

    static class Holder {

        final View headerRoot;
        final ImageView headerIcon;
        final TextView headerText;

        final View itemOffsetView;
        final ColorFilterImageView itemIcon;
        final TextView itemText;

        final View divider;

        Holder(View itemView) {
            headerRoot = itemView.findViewById(R.id.header_root);
            headerIcon = itemView.findViewById(R.id.header_icon);
            headerText = itemView.findViewById(R.id.header_text);

            itemOffsetView = itemView.findViewById(R.id.item_offset);
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemText = itemView.findViewById(R.id.item_text);

            divider = itemView.findViewById(R.id.divider);
        }
    }
}