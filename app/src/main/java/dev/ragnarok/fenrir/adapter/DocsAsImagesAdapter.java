package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.EventListener;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.picasso.PicassoInstance;

public class DocsAsImagesAdapter extends RecyclerBindableAdapter<Document, DocsAsImagesAdapter.DocViewHolder> {

    private ActionListener mActionListener;

    public DocsAsImagesAdapter(List<Document> data) {
        super(data);
    }

    public void setData(List<Document> data) {
        setItems(data);
    }

    public void setActionListener(ActionListener listener) {
        mActionListener = listener;
    }

    @Override
    protected void onBindItemViewHolder(DocViewHolder holder, int position, int type) {
        Document item = getItem(position);

        holder.title.setText(item.getTitle());

        String previewUrl = item.getPreviewWithSize(PhotoSize.Q, false);
        boolean withImage = nonEmpty(previewUrl);

        if (withImage) {
            PicassoInstance.with()
                    .load(previewUrl)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.image);
        } else {
            PicassoInstance.with()
                    .cancelRequest(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (nonNull(mActionListener)) {
                mActionListener.onDocClick(holder.getBindingAdapterPosition(), item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> nonNull(mActionListener)
                && mActionListener.onDocLongClick(holder.getBindingAdapterPosition(), item));
    }

    @Override
    protected DocViewHolder viewHolder(View view, int type) {
        return new DocViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_doc_as_image;
    }

    public interface ActionListener extends EventListener {
        void onDocClick(int index, @NonNull Document doc);

        boolean onDocLongClick(int index, @NonNull Document doc);
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {

        final ImageView image;
        final TextView title;

        DocViewHolder(View root) {
            super(root);
            image = root.findViewById(R.id.image);
            title = root.findViewById(R.id.title);
        }
    }
}
