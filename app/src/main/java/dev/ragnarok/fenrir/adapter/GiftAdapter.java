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
import dev.ragnarok.fenrir.model.Gift;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.Holder> {

    private final Context context;
    private List<Gift> data;
    private ClickListener clickListener;

    public GiftAdapter(List<Gift> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_gift, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Gift gift = data.get(position);
        if (!Utils.isEmpty(gift.getThumb())) {
            ViewUtils.displayAvatar(holder.thumb, null, gift.getThumb(), Constants.PICASSO_TAG);
        } else {
            PicassoInstance.with().cancelRequest(holder.thumb);
        }
        if (!Utils.isEmpty(gift.getThumb())) {
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(gift.getMessage());
        } else {
            holder.message.setVisibility(View.GONE);
        }
        if (gift.getDate() == 0)
            holder.time.setVisibility(View.GONE);
        else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(AppTextUtils.getDateFromUnixTime(context, gift.getDate()));
        }
        holder.gift_container.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onOpenClick(holder.getBindingAdapterPosition(), gift);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Gift> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onOpenClick(int index, Gift gift);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final ImageView thumb;
        final TextView message;
        final TextView time;
        final View gift_container;

        public Holder(View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.item_thumb);
            message = itemView.findViewById(R.id.item_message);
            time = itemView.findViewById(R.id.item_time);
            gift_container = itemView.findViewById(R.id.gift_container);
        }
    }
}
