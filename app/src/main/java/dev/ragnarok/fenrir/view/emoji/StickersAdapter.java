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

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.StickerSet;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class StickersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_ANIMATED = 1;

    private final Context context;
    private final StickerSet stickers;
    private final boolean isNightStiker;
    private EmojiconsPopup.OnStickerClickedListener stickerClickedListener;

    public StickersAdapter(Context context, StickerSet stickers) {
        this.context = context;
        this.stickers = stickers;
        isNightStiker = Settings.get().ui().isStickers_by_theme() && Settings.get().ui().isDarkModeEnabled(context);
    }

//    @Override
//    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
//        holder.setIsRecyclable(false);
//        super.onViewAttachedToWindow(holder);
//    }
//
//    @Override
//    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
//        holder.setIsRecyclable(true);
//        super.onViewDetachedFromWindow(holder);
//    }

    public void setStickerClickedListener(EmojiconsPopup.OnStickerClickedListener listener) {
        stickerClickedListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_IMAGE:
                return new StickerHolder(LayoutInflater.from(context).inflate(R.layout.sticker_grid_item, parent, false));
            case TYPE_ANIMATED:
                return new StickerAnimatedHolder(LayoutInflater.from(context).inflate(R.layout.sticker_grid_item_animated, parent, false));
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int getItemViewType(int position) {
        //return stickers.getStickers().get(position).isAnimated() ? TYPE_ANIMATED : TYPE_IMAGE;
        return TYPE_IMAGE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Sticker item = stickers.getStickers().get(position);
        switch (getItemViewType(position)) {
            default:
            case TYPE_ANIMATED:
                StickerAnimatedHolder animatedHolder = (StickerAnimatedHolder) holder;
                animatedHolder.animation.fromNet(item.getAnimationByType(isNightStiker ? "dark" : "light"), Utils.createOkHttp(5), Utils.dp(128), Utils.dp(128));
                animatedHolder.root.setOnClickListener(v -> stickerClickedListener.onStickerClick(item));
                animatedHolder.root.setOnLongClickListener(v -> {
                    animatedHolder.animation.playAnimation();
                    return true;
                });

                break;
            case TYPE_IMAGE:
                StickerHolder normalHolder = (StickerHolder) holder;
                normalHolder.image.setVisibility(View.VISIBLE);
                String url = item.getImage(256, isNightStiker).getUrl();
                if (Utils.isEmpty(url)) {
                    PicassoInstance.with().cancelRequest(normalHolder.image);
                    normalHolder.image.setImageResource(R.drawable.ic_avatar_unknown);
                } else {
                    PicassoInstance.with()
                            .load(url)
                            //.networkPolicy(NetworkPolicy.OFFLINE)
                            .tag(Constants.PICASSO_TAG)
                            .into(normalHolder.image, new LoadOnErrorCallback(normalHolder.image, url));
                    normalHolder.root.setOnClickListener(v -> stickerClickedListener.onStickerClick(item));
                }
                break;
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof StickerAnimatedHolder) {
            StickerAnimatedHolder animatedHolder = (StickerAnimatedHolder) holder;
            animatedHolder.animation.clearAnimationDrawable();
        }
    }

    @Override
    public int getItemCount() {
        return stickers.getStickers().size();
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
        final View root;
        final ImageView image;

        StickerHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.getRootView();
            image = itemView.findViewById(R.id.sticker);
        }
    }

    static final class StickerAnimatedHolder extends RecyclerView.ViewHolder {
        final View root;
        final RLottieImageView animation;

        StickerAnimatedHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.getRootView();
            animation = itemView.findViewById(R.id.sticker_animated);
        }
    }
}