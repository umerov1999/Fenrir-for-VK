package dev.ragnarok.fenrir.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.FileItem;

public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.Holder> {

    private List<FileItem> data;
    private ClickListener clickListener;

    public FileManagerAdapter(List<FileItem> data) {
        this.data = data;
    }

    public void setItems(List<FileItem> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        FileItem item = data.get(position);
        holder.icon.setBackgroundResource(item.icon);
        holder.fileName.setText(item.file);
        holder.fileDetails.setText(item.details);
        holder.fileDetails.setVisibility(TextUtils.isEmpty(item.details) ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(holder.getBindingAdapterPosition(), item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(int position, FileItem item);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final TextView fileName;
        final TextView fileDetails;
        final ImageView icon;

        public Holder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.item_file_name);
            fileDetails = itemView.findViewById(R.id.item_file_details);
            icon = itemView.findViewById(R.id.item_file_icon);
        }
    }
}
