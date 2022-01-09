package dev.ragnarok.fenrir.adapter.horizontal;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Utils;

public class HorizontalOptionsAdapter<T extends Entry> extends RecyclerBindableAdapter<T, HorizontalOptionsAdapter.Holder> {

    private Listener<T> listener;
    private CustomListener<T> delete_listener;

    public HorizontalOptionsAdapter(List<T> data) {
        super(data);
    }

    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        T item = getItem(position);

        String title = item.getTitle(holder.itemView.getContext());
        String targetTitle = title.startsWith("#") ? title : (item.isCustom() ? title : "#" + title);

        Context context = holder.itemView.getContext();
        holder.title.setText(targetTitle);
        holder.title.setTextColor(item.isActive() ?
                CurrentTheme.getColorOnPrimary(context) : CurrentTheme.getPrimaryTextColorCode(context));
        holder.background.setCardBackgroundColor(item.isActive() ?
                CurrentTheme.getColorPrimary(context) : CurrentTheme.getColorSurface(context));
        holder.background.setStrokeWidth(item.isActive() ? 0 : (int) Utils.dpToPx(1, context));

        holder.itemView.setOnClickListener(v -> listener.onOptionClick(item));
        holder.delete.setColorFilter(item.isActive() ?
                CurrentTheme.getColorOnPrimary(context) : CurrentTheme.getPrimaryTextColorCode(context));
        holder.delete.setVisibility(item.isCustom() ? View.VISIBLE : View.GONE);
        holder.delete.setOnClickListener(v -> {
            if (item.isCustom()) {
                if (delete_listener != null) {
                    delete_listener.onDeleteOptionClick(item, position);
                }
            }
        });
    }

    @Override
    protected Holder viewHolder(View view, int type) {
        return new Holder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_chip;
    }

    public void setListener(Listener<T> listener) {
        this.listener = listener;
    }

    public void setDeleteListener(CustomListener<T> listener) {
        delete_listener = listener;
    }

    public interface Listener<T extends Entry> {
        void onOptionClick(T entry);
    }

    public interface CustomListener<T extends Entry> {
        void onDeleteOptionClick(T entry, int position);
    }

    static class Holder extends RecyclerView.ViewHolder {

        final MaterialCardView background;
        final TextView title;
        final ImageView delete;

        Holder(View itemView) {
            super(itemView);
            background = itemView.findViewById(R.id.card_view);
            title = itemView.findViewById(R.id.title);
            delete = itemView.findViewById(R.id.delete);
        }
    }
}
