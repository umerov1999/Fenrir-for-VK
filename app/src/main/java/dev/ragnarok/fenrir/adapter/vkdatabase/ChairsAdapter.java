package dev.ragnarok.fenrir.adapter.vkdatabase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.database.Chair;

public class ChairsAdapter extends RecyclerView.Adapter<ChairsAdapter.Holder> {

    private final Context mContext;
    private final List<Chair> mData;
    private Listener mListener;

    public ChairsAdapter(Context mContext, List<Chair> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_country, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Chair chair = mData.get(position);

        holder.name.setText(chair.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick(chair);
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
        void onClick(Chair chair);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final TextView name;

        public Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }
}
