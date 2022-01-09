package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.EventListener;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.model.Topic;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class TopicsAdapter extends RecyclerBindableAdapter<Topic, TopicsAdapter.ViewHolder> {

    private final Transformation transformation;
    private final ActionListener mActionListener;
    private int firstLastPadding;

    public TopicsAdapter(Context context, List<Topic> topics, @NonNull ActionListener actionListener) {
        super(topics);
        mActionListener = actionListener;
        transformation = CurrentTheme.createTransformationForAvatar();

        if (Utils.is600dp(context)) {
            firstLastPadding = (int) Utils.dpToPx(16, context);
        }
    }

    @Override
    protected void onBindItemViewHolder(ViewHolder holder, int position, int type) {
        Topic item = getItem(position - getHeadersCount());
        Context context = holder.itemView.getContext();

        holder.title.setText(item.getTitle());
        holder.subtitle.setText(context.getString(R.string.topic_comments_counter,
                AppTextUtils.getDateFromUnixTime(item.getLastUpdateTime()), item.getCommentsCount()));

        holder.itemView.setOnLongClickListener(view -> {
            PopupMenu popup = new PopupMenu(context, holder.itemView);
            popup.inflate(R.menu.topics_item_menu);
            popup.setOnMenuItemClickListener(item1 -> {
                if (item1.getItemId() == R.id.copy_url) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(context.getString(R.string.link), "vk.com/topic" + item.getOwnerId() + "_" + item.getId());
                    clipboard.setPrimaryClip(clip);

                    CustomToast.CreateCustomToast(context).showToast(R.string.copied);
                    return true;
                }
                return false;
            });
            popup.show();
            return false;
        });

        String avaUrl = Objects.isNull(item.getUpdater()) ? null : item.getUpdater().getMaxSquareAvatar();

        if (isEmpty(avaUrl)) {
            PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(transformation)
                    .into(holder.creator);
        } else {
            PicassoInstance.with()
                    .load(avaUrl)
                    .transform(transformation)
                    .into(holder.creator);
        }

        holder.itemView.setPadding(holder.itemView.getPaddingLeft(),
                position == 0 ? firstLastPadding : 0,
                holder.itemView.getPaddingRight(),
                position == getItemCount() - 1 ? firstLastPadding : 0);

        holder.itemView.setOnClickListener(view -> mActionListener.onTopicClick(item));
    }

    @Override
    protected ViewHolder viewHolder(View view, int type) {
        return new ViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_topic;
    }

    public interface ActionListener extends EventListener {
        void onTopicClick(@NonNull Topic topic);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView subtitle;
        private final ImageView creator;

        private ViewHolder(View root) {
            super(root);
            title = root.findViewById(R.id.item_topic_title);
            subtitle = root.findViewById(R.id.item_topic_subtitle);
            creator = root.findViewById(R.id.item_topicstarter_avatar);
        }
    }
}
