package dev.ragnarok.fenrir.adapter.vkdatabase;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.City;

public class CitiesAdapter extends RecyclerView.Adapter<CitiesAdapter.Holder> {

    private final Context mContext;
    private final List<City> mData;
    private Listener mListener;

    public CitiesAdapter(Context mContext, List<City> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_city, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        City city = mData.get(position);
        holder.title.setText(city.getTitle());
        holder.title.setTypeface(null, city.isImportant() ? Typeface.BOLD : Typeface.NORMAL);

        holder.region.setText(city.getRegion());
        holder.region.setVisibility(TextUtils.isEmpty(city.getRegion()) ? View.GONE : View.VISIBLE);

        holder.area.setText(city.getArea());
        holder.area.setVisibility(TextUtils.isEmpty(city.getArea()) ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick(city);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClick(City country);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView area;
        final TextView region;

        public Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            area = itemView.findViewById(R.id.area);
            region = itemView.findViewById(R.id.region);
        }
    }
}
