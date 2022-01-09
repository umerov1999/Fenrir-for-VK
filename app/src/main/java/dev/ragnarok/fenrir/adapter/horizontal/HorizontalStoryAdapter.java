package dev.ragnarok.fenrir.adapter.horizontal;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class HorizontalStoryAdapter extends RecyclerBindableAdapter<Story, HorizontalStoryAdapter.Holder> {

    private Listener listener;

    public HorizontalStoryAdapter(List<Story> data) {
        super(data);
    }

    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        Story item = getItem(position);

        Context context = holder.itemView.getContext();
        holder.name.setText(item.getOwner().getFullName());
        if (item.getExpires() <= 0)
            holder.expires.setVisibility(View.INVISIBLE);
        else {
            if (item.isIs_expired()) {
                holder.expires.setVisibility(View.VISIBLE);
                holder.expires.setText(R.string.is_expired);
            } else {
                long exp = (item.getExpires() - Calendar.getInstance().getTime().getTime() / 1000) / 3600;
                if (exp <= 0) {
                    holder.expires.setVisibility(View.INVISIBLE);
                } else {
                    holder.expires.setVisibility(View.VISIBLE);
                    holder.expires.setText(context.getString(R.string.expires, String.valueOf(exp), context.getString(Utils.declOfNum(exp, new int[]{R.string.hour, R.string.hour_sec, R.string.hours}))));
                }
            }
        }

        if (Objects.isNull(item.getOwner())) {
            ViewUtils.displayAvatar(holder.story_image, new RoundTransformation(), null, Constants.PICASSO_TAG);
        } else {
            ViewUtils.displayAvatar(holder.story_image, new RoundTransformation(), item.getOwner().getMaxSquareAvatar(), Constants.PICASSO_TAG);
        }

        holder.itemView.setOnClickListener(v -> listener.onOptionClick(item, position));
    }

    @Override
    protected Holder viewHolder(View view, int type) {
        return new Holder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_story;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onOptionClick(Story item, int pos);
    }

    static class Holder extends RecyclerView.ViewHolder {

        final ImageView story_image;
        final TextView name;
        final TextView expires;

        Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_story_name);
            expires = itemView.findViewById(R.id.item_story_expires);
            story_image = itemView.findViewById(R.id.item_story_pic);
        }
    }
}
