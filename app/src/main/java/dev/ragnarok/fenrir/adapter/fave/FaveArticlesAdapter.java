package dev.ragnarok.fenrir.adapter.fave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.ViewUtils;

public class FaveArticlesAdapter extends RecyclerView.Adapter<FaveArticlesAdapter.Holder> {

    private final Context context;
    private List<Article> data;
    private ClickListener clickListener;

    public FaveArticlesAdapter(List<Article> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_article, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Article article = data.get(position);
        holder.btFave.setImageResource(R.drawable.favorite);
        holder.btFave.setOnClickListener(v -> clickListener.onDelete(position, article));
        if (article.getURL() != null) {
            holder.ivButton.setVisibility(View.VISIBLE);
            holder.ivButton.setOnClickListener(v -> clickListener.onUrlClick(article.getURL()));
        } else
            holder.ivButton.setVisibility(View.GONE);

        holder.btShare.setOnClickListener(v -> clickListener.onShare(article));

        String photo_url = null;
        if (article.getPhoto() != null) {
            photo_url = article.getPhoto().getUrlForSize(Settings.get().main().getPrefPreviewImageSize(), false);
        }

        if (photo_url != null) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            ViewUtils.displayAvatar(holder.ivPhoto, null, photo_url, Constants.PICASSO_TAG);
            holder.ivPhoto.setOnLongClickListener(v -> {
                clickListener.onPhotosOpen(article.getPhoto());
                return true;
            });
        } else
            holder.ivPhoto.setVisibility(View.GONE);

        if (article.getSubTitle() != null) {
            holder.ivSubTitle.setVisibility(View.VISIBLE);
            holder.ivSubTitle.setText(article.getSubTitle());
        } else
            holder.ivSubTitle.setVisibility(View.GONE);

        if (article.getTitle() != null) {
            holder.ivTitle.setVisibility(View.VISIBLE);
            holder.ivTitle.setText(article.getTitle());
        } else
            holder.ivTitle.setVisibility(View.GONE);

        if (article.getOwnerName() != null) {
            holder.ivName.setVisibility(View.VISIBLE);
            holder.ivName.setText(article.getOwnerName());
        } else
            holder.ivName.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Article> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onUrlClick(String url);

        void onPhotosOpen(Photo photo);

        void onDelete(int index, Article article);

        void onShare(Article article);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final ImageView ivPhoto;
        final ImageView btFave;
        final ImageView btShare;
        final TextView ivSubTitle;
        final TextView ivTitle;
        final TextView ivName;
        final Button ivButton;

        public Holder(View root) {
            super(root);

            ivPhoto = itemView.findViewById(R.id.item_article_image);
            ivSubTitle = itemView.findViewById(R.id.item_article_subtitle);
            ivTitle = itemView.findViewById(R.id.item_article_title);
            ivName = itemView.findViewById(R.id.item_article_name);
            ivButton = itemView.findViewById(R.id.item_article_read);
            btFave = itemView.findViewById(R.id.item_article_to_fave);
            btShare = itemView.findViewById(R.id.item_article_share);
        }
    }
}
