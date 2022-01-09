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
import dev.ragnarok.fenrir.model.GroupChats;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;

public class GroupChatsAdapter extends RecyclerBindableAdapter<GroupChats, GroupChatsAdapter.ViewHolder> {

    private final Transformation transformation;
    private final ActionListener mActionListener;
    private int firstLastPadding;

    public GroupChatsAdapter(Context context, List<GroupChats> chats, @NonNull ActionListener actionListener) {
        super(chats);
        mActionListener = actionListener;
        transformation = CurrentTheme.createTransformationForAvatar();

        if (Utils.is600dp(context)) {
            firstLastPadding = (int) Utils.dpToPx(16, context);
        }
    }

    @Override
    protected void onBindItemViewHolder(ViewHolder holder, int position, int type) {
        GroupChats item = getItem(position - getHeadersCount());
        Context context = holder.itemView.getContext();

        holder.title.setText(item.getTitle());
        holder.subtitle.setText(context.getString(R.string.group_chats_counter,
                AppTextUtils.getDateFromUnixTime(item.getLastUpdateTime()), item.getMembers_count()));

        holder.itemView.setOnLongClickListener(view -> {
            PopupMenu popup = new PopupMenu(context, holder.itemView);
            popup.inflate(R.menu.topics_item_menu);
            popup.setOnMenuItemClickListener(item1 -> {
                if (item1.getItemId() == R.id.copy_url) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(context.getString(R.string.link), item.getInvite_link());
                    clipboard.setPrimaryClip(clip);

                    CustomToast.CreateCustomToast(context).showToast(R.string.copied);
                    return true;
                }
                return false;
            });
            popup.show();
            return false;
        });

        String avaUrl = item.getPhoto();

        if (isEmpty(avaUrl)) {
            PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(transformation)
                    .into(holder.ava);
        } else {
            PicassoInstance.with()
                    .load(avaUrl)
                    .transform(transformation)
                    .into(holder.ava);
        }

        holder.itemView.setPadding(holder.itemView.getPaddingLeft(),
                position == 0 ? firstLastPadding : 0,
                holder.itemView.getPaddingRight(),
                position == getItemCount() - 1 ? firstLastPadding : 0);

        holder.itemView.setOnClickListener(view -> mActionListener.onGroupChatsClick(item));
    }

    @Override
    protected ViewHolder viewHolder(View view, int type) {
        return new ViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_group_chat;
    }

    public interface ActionListener extends EventListener {
        void onGroupChatsClick(@NonNull GroupChats chat);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView subtitle;
        private final ImageView ava;

        private ViewHolder(View root) {
            super(root);
            title = root.findViewById(R.id.item_group_chat_title);
            subtitle = root.findViewById(R.id.item_group_chat_subtitle);
            ava = root.findViewById(R.id.item_group_chat_avatar);
        }
    }
}
