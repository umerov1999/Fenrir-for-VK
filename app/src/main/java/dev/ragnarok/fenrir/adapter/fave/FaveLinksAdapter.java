package dev.ragnarok.fenrir.adapter.fave;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.view.ContextMenu;
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
import dev.ragnarok.fenrir.model.FaveLink;
import dev.ragnarok.fenrir.model.PhotoSizes;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class FaveLinksAdapter extends RecyclerView.Adapter<FaveLinksAdapter.Holder> {

    private final Context context;
    private List<FaveLink> data;
    private RecyclerView recyclerView;
    private ClickListener clickListener;

    public FaveLinksAdapter(List<FaveLink> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public String getImageUrl(FaveLink link) {
        if (nonNull(link.getPhoto()) && nonNull(link.getPhoto().getSizes())) {
            PhotoSizes sizes = link.getPhoto().getSizes();
            return sizes.getUrlForSize(Settings.get().main().getPrefPreviewImageSize(), true);
        }
        return null;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_fave_link, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        FaveLink item = data.get(position);
        if (Utils.isEmpty(item.getTitle()))
            holder.tvTitle.setVisibility(View.GONE);
        else {
            holder.tvTitle.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(item.getTitle());
        }
        if (Utils.isEmpty(item.getDescription()))
            holder.tvDescription.setVisibility(View.GONE);
        else {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(item.getDescription());
        }
        if (Utils.isEmpty(item.getUrl()))
            holder.tvURL.setVisibility(View.GONE);
        else {
            holder.tvURL.setVisibility(View.VISIBLE);
            holder.tvURL.setText(item.getUrl());
        }

        String imageUrl = getImageUrl(item);
        if (imageUrl != null) {
            holder.ivEmpty.setVisibility(View.GONE);
            holder.ivImage.setVisibility(View.VISIBLE);
            ViewUtils.displayAvatar(holder.ivImage, null, imageUrl, Constants.PICASSO_TAG);
        } else {
            PicassoInstance.with().cancelRequest(holder.ivImage);
            holder.ivImage.setVisibility(View.GONE);
            holder.ivEmpty.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onLinkClick(holder.getBindingAdapterPosition(), item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<FaveLink> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onLinkClick(int index, FaveLink link);

        void onLinkDelete(int index, FaveLink link);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        final ImageView ivImage;
        final ImageView ivEmpty;
        final TextView tvTitle;
        final TextView tvDescription;
        final TextView tvURL;

        public Holder(View root) {
            super(root);
            itemView.setOnCreateContextMenuListener(this);

            ivImage = root.findViewById(R.id.item_fave_link_image);
            ivEmpty = root.findViewById(R.id.item_fave_link_empty);
            tvTitle = root.findViewById(R.id.item_fave_link_title);
            tvDescription = root.findViewById(R.id.item_fave_link_description);
            tvURL = root.findViewById(R.id.item_fave_link_url);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = recyclerView.getChildAdapterPosition(v);
            FaveLink faveLink = data.get(position);
            menu.setHeaderTitle(faveLink.getTitle());

            menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
                if (clickListener != null) {
                    clickListener.onLinkDelete(position, faveLink);
                }
                return true;
            });
        }
    }
}
