package dev.ragnarok.fenrir.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.SideDrawerCategory;

public class SideDrawerCategotiesAdapter extends RecyclerView.Adapter<SideDrawerCategotiesAdapter.ViewHolder> {

    private List<SideDrawerCategory> data;

    public SideDrawerCategotiesAdapter(List<SideDrawerCategory> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawer_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SideDrawerCategory category = data.get(position);

        holder.checkBox.setText(category.getTitle());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(category.isChecked());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> category.setChecked(isChecked));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<SideDrawerCategory> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.item_drawer_category_check);
        }
    }
}
