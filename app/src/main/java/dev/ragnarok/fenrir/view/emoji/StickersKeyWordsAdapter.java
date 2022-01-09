package dev.ragnarok.fenrir.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Callback;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class StickersKeyWordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final boolean isNightStiker;
    private List<Sticker> stickers;
    private EmojiconsPopup.OnStickerClickedListener stickerClickedListener;

    public StickersKeyWordsAdapter(Context context, List<Sticker> stickers) {
        this.context = context;
        this.stickers = stickers;
        isNightStiker = Settings.get().ui().isStickers_by_theme() && Settings.get().ui().isDarkModeEnabled(context);
    }

    public void setStickerClickedListener(EmojiconsPopup.OnStickerClickedListener listener) {
        stickerClickedListener = listener;
    }

    public void setData(List<Sticker> data) {
        if (Utils.isEmpty(data)) {
            stickers = Collections.emptyList();
        } else {
            stickers = data;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StickerHolder(LayoutInflater.from(context).inflate(R.layout.sticker_keyword_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Sticker item = stickers.get(position);
        StickerHolder normalHolder = (StickerHolder) holder;
        normalHolder.root.setVisibility(View.VISIBLE);
        String url = item.getImage(256, isNightStiker).getUrl();

        if (Utils.isEmpty(url)) {
            PicassoInstance.with().cancelRequest(normalHolder.root);
            normalHolder.root.setImageResource(R.drawable.ic_avatar_unknown);
        } else {
            PicassoInstance.with()
                    .load(url)
                    //.networkPolicy(NetworkPolicy.OFFLINE)
                    .tag(Constants.PICASSO_TAG)
                    .into(normalHolder.root, new LoadOnErrorCallback(normalHolder.root, url));
            normalHolder.root.setOnClickListener(v -> stickerClickedListener.onStickerClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    private static class LoadOnErrorCallback implements Callback {

        final WeakReference<ImageView> ref;
        final String link;

        private LoadOnErrorCallback(ImageView view, String link) {
            ref = new WeakReference<>(view);
            this.link = link;
        }

        @Override
        public void onSuccess() {
            // do nothink
        }

        @Override
        public void onError(@NonNull Throwable t) {
            ImageView view = ref.get();
            try {
                if (view != null) {
                    PicassoInstance.with()
                            .load(link)
                            .into(view);
                }
            } catch (Exception ignored) {

            }
        }
    }

    static final class StickerHolder extends RecyclerView.ViewHolder {
        final ImageView root;

        StickerHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.sticker);
        }
    }
}
