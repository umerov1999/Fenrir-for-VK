package dev.ragnarok.fenrir.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.view.emoji.section.Emojicon;

class EmojiAdapter extends ArrayAdapter<Emojicon> {

    private EmojiconsPopup.OnEmojiconClickedListener emojiClickListener;

    public EmojiAdapter(Context context, Emojicon[] data) {
        super(context, R.layout.emojicon_item, data);
    }

    public void setEmojiClickListener(@NonNull EmojiconsPopup.OnEmojiconClickedListener listener) {
        emojiClickListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.emojicon_item, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.icon = v.findViewById(R.id.emojicon_icon);
            v.setTag(holder);
        }

        Emojicon emoji = getItem(position);
        ViewHolder holder = (ViewHolder) v.getTag();

        holder.icon.setText(emoji.getEmoji(), TextView.BufferType.SPANNABLE);
        holder.icon.setOnClickListener(v1 -> emojiClickListener.onEmojiconClicked(getItem(position)));
        return v;
    }

    static class ViewHolder {
        TextView icon;
    }
}