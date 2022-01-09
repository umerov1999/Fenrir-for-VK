package dev.ragnarok.fenrir.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.EventListener;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Conversation;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class DialogPreviewAdapter extends RecyclerView.Adapter<DialogPreviewAdapter.DialogPreviewHolder> {

    private final ActionListener actionListener;
    private final Transformation mTransformation;
    private List<Conversation> mData;

    public DialogPreviewAdapter(List<Conversation> items, ActionListener actionListener) {
        mData = items;
        mTransformation = CurrentTheme.createTransformationForAvatar();
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public DialogPreviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DialogPreviewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialog_preview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DialogPreviewHolder holder, int position) {
        Conversation item = mData.get(position);

        holder.mTitle.setText(item.getTitle());
        String url = item.getMaxSquareAvatar();
        if (!Utils.isEmpty(url)) {
            holder.EmptyAvatar.setVisibility(View.INVISIBLE);
            ViewUtils.displayAvatar(holder.mAvatar, mTransformation, item.getMaxSquareAvatar(), Constants.PICASSO_TAG);
        } else {
            PicassoInstance.with().cancelRequest(holder.mAvatar);
            if (!Utils.isEmpty(item.getTitle())) {
                holder.EmptyAvatar.setVisibility(View.VISIBLE);
                String name = item.getTitle();
                if (name.length() > 2)
                    name = name.substring(0, 2);
                name = name.trim();
                holder.EmptyAvatar.setText(name);
            } else {
                holder.EmptyAvatar.setVisibility(View.INVISIBLE);
            }
            holder.mAvatar.setImageBitmap(mTransformation.localTransform(Utils.createGradientChatImage(200, 200, item.getId())));
        }

        holder.itemView.setOnClickListener(v -> actionListener.onEntryClick(item));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Conversation> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public interface ActionListener extends EventListener {
        void onEntryClick(Conversation o);
    }

    static class DialogPreviewHolder extends RecyclerView.ViewHolder {

        private final ImageView mAvatar;
        private final TextView mTitle;
        private final TextView EmptyAvatar;

        DialogPreviewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.dialog_title);
            mAvatar = itemView.findViewById(R.id.item_chat_avatar);
            EmptyAvatar = itemView.findViewById(R.id.empty_avatar_text);
        }
    }
}