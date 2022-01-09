package dev.ragnarok.fenrir.adapter.multidata;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.util.Utils;

public abstract class DifferentDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<List<?>> mData;

    public DifferentDataAdapter() {
        mData = new ArrayList<>(2);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (List<?> data : mData) {
            count = count + data.size();
        }

        return count;
    }

    public void setData(int type, List<?> data) {
        mData.add(type, data);
    }

    public void notifyItemChanged(int dataPosition, int dataType) {
        notifyItemChanged(getOffset(dataType) + dataPosition);
    }

    public void notifyItemRangeInserted(int dataPositionStart, int count, int dataType) {
        notifyItemRangeInserted(getOffset(dataType) + dataPositionStart, count);
    }

    public void notifyItemInserted(int dataPosition, int dataType) {
        notifyItemInserted(dataPosition + getOffset(dataType));
    }

    public void notifyItemRemoved(int dataPosition, int dataType) {
        notifyItemRemoved(dataToAdapterPosition(dataPosition, dataType));
    }

    protected int dataToAdapterPosition(int dataPosition, int dataType) {
        return dataPosition + getOffset(dataType);
    }

    protected int getOffset(int type) {
        int offset = 0;
        for (int i = 0; i < mData.size(); i++) {
            if (i < type) {
                offset = offset + Utils.safeCountOf(mData.get(i));
            }
        }

        return offset;
    }

    @SuppressWarnings("unchecked")
    public <T> T getItem(int adapterPosition) {
        int offset = 0;
        for (List<?> data : mData) {
            int newOffset = offset + data.size();
            if (adapterPosition < newOffset) {
                int internalPosition = adapterPosition - offset;
                return (T) data.get(internalPosition);
            }

            offset = newOffset;
        }

        throw new IllegalArgumentException("Invalid adapter position");
    }

    protected int getDataTypeByAdapterPosition(int adapterPosition) {
        int offset = 0;
        for (int i = 0; i < mData.size(); i++) {
            List<?> data = mData.get(i);

            int newOffset = offset + data.size();

            if (adapterPosition < newOffset) {
                return i;
            }

            offset = newOffset;
        }

        throw new IllegalArgumentException("Invalid adapter position");
    }
}