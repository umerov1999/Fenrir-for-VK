package dev.ragnarok.fenrir.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VkApiArtist;
import dev.ragnarok.fenrir.util.Utils;

public class ArtistSearchAdapter extends RecyclerView.Adapter<ArtistSearchAdapter.Holder> {

    private final Context context;
    private List<VkApiArtist> data;
    private ClickListener clickListener;

    public ArtistSearchAdapter(List<VkApiArtist> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_artist_search, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        VkApiArtist item = data.get(position);
        if (Utils.isEmpty(item.name))
            holder.tvTitle.setVisibility(View.GONE);
        else {
            holder.tvTitle.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(item.name);
        }
        if (Utils.isEmpty(item.domain))
            holder.tvDomain.setVisibility(View.GONE);
        else {
            holder.tvDomain.setVisibility(View.VISIBLE);
            holder.tvDomain.setText("@" + item.domain);
        }
        if (Utils.isEmpty(item.id))
            holder.tvId.setVisibility(View.GONE);
        else {
            holder.tvId.setVisibility(View.VISIBLE);
            holder.tvId.setText("id" + item.id);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onArtistClick(item.id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<VkApiArtist> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onArtistClick(String id);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final TextView tvTitle;
        final TextView tvDomain;
        final TextView tvId;

        public Holder(View root) {
            super(root);

            tvTitle = root.findViewById(R.id.item_artist_search_title);
            tvDomain = root.findViewById(R.id.item_artist_search_domain);
            tvId = root.findViewById(R.id.item_artist_search_id);
        }
    }
}
