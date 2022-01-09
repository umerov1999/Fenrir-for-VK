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

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.ViewUtils;

public class MarketAlbumAdapter extends RecyclerView.Adapter<MarketAlbumAdapter.Holder> {

    private final Context context;
    private List<MarketAlbum> data;
    private ClickListener clickListener;

    public MarketAlbumAdapter(List<MarketAlbum> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_market_album, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        MarketAlbum market = data.get(position);
        if (!Objects.isNull(market.getPhoto())) {
            String url = market.getPhoto().getUrlForSize(PhotoSize.X, true);
            ViewUtils.displayAvatar(holder.thumb, null, url, Constants.PICASSO_TAG);
        } else
            holder.thumb.setImageResource(R.drawable.ic_market_colored_stack);
        holder.title.setText(market.getTitle());
        if (market.getCount() == 0)
            holder.count.setVisibility(View.GONE);
        else {
            holder.count.setVisibility(View.VISIBLE);
            holder.count.setText(context.getString(R.string.markets_count, market.getCount()));
        }
        if (market.getUpdated_time() == 0)
            holder.time.setVisibility(View.GONE);
        else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(AppTextUtils.getDateFromUnixTime(context, market.getUpdated_time()));
        }
        holder.market_container.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onOpenClick(holder.getBindingAdapterPosition(), market);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<MarketAlbum> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onOpenClick(int index, MarketAlbum market_album);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final ImageView thumb;
        final TextView title;
        final TextView count;
        final TextView time;
        final View market_container;

        public Holder(View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.item_thumb);
            title = itemView.findViewById(R.id.item_title);
            market_container = itemView.findViewById(R.id.market_container);
            count = itemView.findViewById(R.id.item_count);
            time = itemView.findViewById(R.id.item_time);
        }
    }
}
