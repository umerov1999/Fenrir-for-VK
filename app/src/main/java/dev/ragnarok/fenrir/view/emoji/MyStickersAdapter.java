package dev.ragnarok.fenrir.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class MyStickersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_ANIMATED = 1;
    private final Context context;
    private EmojiconsPopup.OnMyStickerClickedListener myStickerClickedListener;

    public MyStickersAdapter(Context context) {
        this.context = context;
    }

    public void setMyStickerClickedListener(EmojiconsPopup.OnMyStickerClickedListener listener) {
        myStickerClickedListener = listener;
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
        return Utils.getCachedMyStickers().get(position).isAnimated() ? TYPE_ANIMATED : TYPE_IMAGE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Sticker.LocalSticker item = Utils.getCachedMyStickers().get(position);

        switch (getItemViewType(position)) {
            default:
            case TYPE_ANIMATED:
                StickerAnimatedHolder animatedHolder = (StickerAnimatedHolder) holder;
                animatedHolder.animation.fromFile(new File(item.getPath()), Utils.dp(128), Utils.dp(128));
                animatedHolder.root.setOnClickListener(v -> myStickerClickedListener.onMyStickerClick(item));
                animatedHolder.root.setOnLongClickListener(v -> {
                    animatedHolder.animation.playAnimation();
                    return true;
                });

                break;
            case TYPE_IMAGE:
                StickerHolder normalHolder = (StickerHolder) holder;
                normalHolder.image.setVisibility(View.VISIBLE);
                String url = item.getPreviewPath();
                if (Utils.isEmpty(url)) {
                    PicassoInstance.with().cancelRequest(normalHolder.image);
                    normalHolder.image.setImageResource(R.drawable.ic_avatar_unknown);
                } else {
                    PicassoInstance.with()
                            .load(url)
                            //.networkPolicy(NetworkPolicy.OFFLINE)
                            .tag(Constants.PICASSO_TAG)
                            .into(normalHolder.image);
                    normalHolder.root.setOnClickListener(v -> myStickerClickedListener.onMyStickerClick(item));
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return Utils.getCachedMyStickers().size();
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
