package dev.ragnarok.fenrir.filepicker.controller.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.filepicker.controller.NotifyItemChecked;
import dev.ragnarok.fenrir.filepicker.model.DialogConfigs;
import dev.ragnarok.fenrir.filepicker.model.DialogProperties;
import dev.ragnarok.fenrir.filepicker.model.FileListItem;
import dev.ragnarok.fenrir.filepicker.model.MarkedItemList;
import dev.ragnarok.fenrir.filepicker.widget.MaterialCheckbox;

/**
 * @author akshay sunil masram
 */
public class FileListAdapter extends BaseAdapter {

    private final ArrayList<FileListItem> listItem;
    private final Context context;
    private final DialogProperties properties;
    private NotifyItemChecked notifyItemChecked;

    public FileListAdapter(ArrayList<FileListItem> listItem, Context context,
                           DialogProperties properties) {
        this.listItem = listItem;
        this.context = context;
        this.properties = properties;
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public FileListItem getItem(int i) {
        return listItem.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.file_picker_dialog_file_list_item,
                    viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        FileListItem item = listItem.get(i);
        Animation animation;
        if (MarkedItemList.hasItem(item.getLocation())) {
            animation = AnimationUtils.loadAnimation(context,
                    R.anim.marked_item_animation);
        } else {
            animation = AnimationUtils.loadAnimation(context,
                    R.anim.unmarked_item_animation);
        }
        view.setAnimation(animation);
        if (item.isDirectory()) {
            holder.type_icon.setImageResource(R.drawable.ic_type_folder);
            if (properties.selection_type == DialogConfigs.FILE_SELECT) {
                holder.checkbox.setVisibility(View.INVISIBLE);
            } else {
                holder.checkbox.setVisibility(View.VISIBLE);
            }
        } else {
            holder.type_icon.setImageResource(R.drawable.ic_type_file);
            if (properties.selection_type == DialogConfigs.DIR_SELECT) {
                holder.checkbox.setVisibility(View.INVISIBLE);
            } else {
                holder.checkbox.setVisibility(View.VISIBLE);
            }
        }
        holder.type_icon.setContentDescription(item.getFilename());
        holder.name.setText(item.getFilename());
        DateFormat dateFormatter = android.text.format.DateFormat.getMediumDateFormat(context);
        DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        Date date = new Date(item.getTime());
        if (i == 0 && item.getFilename().startsWith(context.getString(R.string.label_parent_dir))) {
            holder.type.setText(R.string.label_parent_directory);
        } else {
            holder.type.setText(String.format(context.getString(R.string.last_edit),
                    dateFormatter.format(date), timeFormatter.format(date)));
        }
        if (holder.checkbox.getVisibility() == View.VISIBLE) {
            if (i == 0 && item.getFilename().startsWith(context.getString(R.string.label_parent_dir))) {
                holder.checkbox.setVisibility(View.INVISIBLE);
            }
            holder.checkbox.setChecked(MarkedItemList.hasItem(item.getLocation()));
        }

        holder.checkbox.setOnCheckedChangedListener((checkbox, isChecked) -> {
            item.setMarked(isChecked);
            if (item.isMarked()) {
                if (properties.selection_mode == DialogConfigs.MULTI_MODE) {
                    MarkedItemList.addSelectedItem(item);
                } else {
                    MarkedItemList.addSingleFile(item);
                }
            } else {
                MarkedItemList.removeSelectedItem(item.getLocation());
            }
            notifyItemChecked.notifyCheckBoxIsClicked();
        });
        return view;
    }

    public void setNotifyItemCheckedListener(NotifyItemChecked notifyItemChecked) {
        this.notifyItemChecked = notifyItemChecked;
    }

    private static class ViewHolder {
        ImageView type_icon;
        TextView name, type;
        MaterialCheckbox checkbox;

        ViewHolder(View itemView) {
            name = itemView.findViewById(R.id.fname);
            type = itemView.findViewById(R.id.ftype);
            type_icon = itemView.findViewById(R.id.image_type);
            checkbox = itemView.findViewById(R.id.file_mark);
        }
    }
}
