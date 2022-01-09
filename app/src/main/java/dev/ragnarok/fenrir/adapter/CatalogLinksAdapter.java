package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.EventListener;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.PhotoSizes;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;


public class CatalogLinksAdapter extends RecyclerBindableAdapter<Link, CatalogLinksAdapter.LinkViewHolder> {

    private final Transformation transformation;
    private ActionListener mActionListner;

    public CatalogLinksAdapter(List<Link> data) {
        super(data);
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    public void setActionListener(ActionListener listener) {
        mActionListner = listener;
    }

    public String getImageUrl(Link link) {

        if (link.getPhoto() == null && link.getPreviewPhoto() != null)
            return link.getPreviewPhoto();

        if (nonNull(link.getPhoto()) && nonNull(link.getPhoto().getSizes())) {
            PhotoSizes sizes = link.getPhoto().getSizes();
            return sizes.getUrlForSize(Settings.get().main().getPrefPreviewImageSize(), true);
        }

        return null;
    }

    @Override
    protected void onBindItemViewHolder(LinkViewHolder holder, int position, int type) {
        Link item = getItem(position);

        if (Utils.isEmpty(item.getTitle()))
            holder.tvTitle.setVisibility(View.INVISIBLE);
        else {
            holder.tvTitle.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(item.getTitle());
        }
        if (Utils.isEmpty(item.getDescription()))
            holder.tvDescription.setVisibility(View.INVISIBLE);
        else {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(item.getDescription());
        }

        String imageUrl = getImageUrl(item);
        if (imageUrl != null) {
            ViewUtils.displayAvatar(holder.ivImage, transformation, imageUrl, Constants.PICASSO_TAG);
        } else {
            PicassoInstance.with().cancelRequest(holder.ivImage);
            holder.ivImage.setImageResource(R.drawable.ic_avatar_unknown);
        }

        holder.itemView.setOnClickListener(v -> {
            if (nonNull(mActionListner)) {
                mActionListner.onLinkClick(holder.getBindingAdapterPosition(), item);
            }
        });
    }

    @Override
    protected LinkViewHolder viewHolder(View view, int type) {
        return new LinkViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_catalog_link;
    }

    public interface ActionListener extends EventListener {
        void onLinkClick(int index, @NonNull Link doc);
    }

    static class LinkViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivImage;
        final TextView tvTitle;
        final TextView tvDescription;

        private LinkViewHolder(View root) {
            super(root);
            ivImage = root.findViewById(R.id.item_link_pic);
            tvTitle = root.findViewById(R.id.item_link_name);
            tvDescription = root.findViewById(R.id.item_link_description);
        }
    }
}
