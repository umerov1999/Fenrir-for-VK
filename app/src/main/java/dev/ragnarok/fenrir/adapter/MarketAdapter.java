package dev.ragnarok.fenrir.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.Holder> {

    private final Context context;
    private List<Market> data;
    private ClickListener clickListener;

    public MarketAdapter(List<Market> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_market, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Market market = data.get(position);
        if (!Utils.isEmpty(market.getThumb_photo()))
            ViewUtils.displayAvatar(holder.thumb, null, market.getThumb_photo(), Constants.PICASSO_TAG);
        else
            holder.thumb.setImageResource(R.drawable.ic_market_colored_outline);
        holder.title.setText(market.getTitle());
        if (Utils.isEmpty(market.getDescription()))
            holder.description.setVisibility(View.GONE);
        else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(market.getDescription());
        }
        if (Utils.isEmpty(market.getPrice()))
            holder.price.setVisibility(View.GONE);
        else {
            holder.price.setVisibility(View.VISIBLE);
            holder.price.setText(market.getPrice());
        }
        if (market.getDate() == 0)
            holder.time.setVisibility(View.GONE);
        else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(AppTextUtils.getDateFromUnixTime(context, market.getDate()));
        }
        switch (market.getAvailability()) {
            case 0:
                holder.available.setTextColor(CurrentTheme.getColorOnSurface(context));
                holder.available.setText(R.string.markets_available);
                break;
            case 2:
                holder.available.setTextColor(Color.parseColor("#ffaa00"));
                holder.available.setText(R.string.markets_not_available);
                break;
            default:
                holder.available.setTextColor(Color.parseColor("#ff0000"));
                holder.available.setText(R.string.markets_deleted);
                break;
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

    public void setData(List<Market> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onOpenClick(int index, Market market);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final ImageView thumb;
        final TextView price;
        final TextView title;
        final TextView description;
        final TextView available;
        final TextView time;
        final View market_container;

        public Holder(View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.item_thumb);
            price = itemView.findViewById(R.id.item_price);
            title = itemView.findViewById(R.id.item_title);
            market_container = itemView.findViewById(R.id.market_container);
            description = itemView.findViewById(R.id.item_description);
            available = itemView.findViewById(R.id.item_available);
            time = itemView.findViewById(R.id.item_time);
        }
    }
}
