package dev.ragnarok.fenrir.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.ShortLink;
import dev.ragnarok.fenrir.util.AppTextUtils;

public class ShortedLinksAdapter extends RecyclerView.Adapter<ShortedLinksAdapter.Holder> {

    private final Context context;
    private List<ShortLink> data;
    private ClickListener clickListener;

    public ShortedLinksAdapter(List<ShortLink> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_short_link, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ShortLink link = data.get(position);
        holder.time.setText(AppTextUtils.getDateFromUnixTime(context, link.getTimestamp()));
        holder.views.setText(String.valueOf(link.getViews()));
        holder.short_link.setText(link.getShort_url());
        holder.original.setText(link.getUrl());
        holder.delete.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDelete(holder.getBindingAdapterPosition(), link);
            }
        });
        holder.copy.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCopy(holder.getBindingAdapterPosition(), link);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<ShortLink> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onCopy(int index, ShortLink link);

        void onDelete(int index, ShortLink link);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        final TextView short_link;
        final TextView original;
        final TextView time;
        final TextView views;
        final ImageView copy;
        final ImageView delete;

        public Holder(View itemView) {
            super(itemView);
            short_link = itemView.findViewById(R.id.item_short_link);
            original = itemView.findViewById(R.id.item_link);
            time = itemView.findViewById(R.id.item_time);
            views = itemView.findViewById(R.id.item_views);
            copy = itemView.findViewById(R.id.item_copy);
            delete = itemView.findViewById(R.id.item_delete);
        }
    }
}
