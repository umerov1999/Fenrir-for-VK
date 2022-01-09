package dev.ragnarok.fenrir.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;

public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.Holder> {

    private final Context mContext;
    private final List<AbsSection> data;
    private Listener listener;

    public SectionsAdapter(List<AbsSection> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.emoji_section_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AbsSection section = data.get(position);
        switch (section.type) {
            case AbsSection.TYPE_EMOJI:
                EmojiSection emojiSection = (EmojiSection) section;

                PicassoInstance.with()
                        .cancelRequest(holder.icon);

                holder.icon.setImageDrawable(emojiSection.drawable);
                holder.icon.getDrawable().setTint(CurrentTheme.getColorOnSurface(mContext));
                break;

            case AbsSection.TYPE_STICKER:
                StickerSection stickerSection = (StickerSection) section;
                if (stickerSection.stickerSet.getTitle() != null && stickerSection.stickerSet.getTitle().equals("recent")) {
                    holder.icon.setImageResource(R.drawable.pin);
                    holder.icon.getDrawable().setTint(CurrentTheme.getColorPrimary(mContext));
                } else {
                    PicassoInstance.with()
                            .load(stickerSection.stickerSet.getImageUrl(128))
                            .placeholder(R.drawable.sticker_pack_with_alpha)
                            .into(holder.icon);
                    holder.icon.setColorFilter(null);
                }
                break;
            case AbsSection.TYPE_PHOTO_ALBUM:
                PicassoInstance.with()
                        .cancelRequest(holder.icon);

                holder.icon.setImageResource(R.drawable.image);
                holder.icon.getDrawable().setTint(CurrentTheme.getColorOnSurface(mContext));
                break;
        }

        if (section.active) {
            holder.root.setBackgroundResource(R.drawable.circle_back_white);
            holder.root.getBackground().setTint(CurrentTheme.getMessageBackgroundSquare(mContext));
        } else {
            holder.root.setBackground(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onClick(int position);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final View root;
        final ImageView icon;

        public Holder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
