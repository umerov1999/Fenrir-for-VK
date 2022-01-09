package dev.ragnarok.fenrir.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.squareup.picasso3.Transformation;

import java.util.ArrayList;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.ViewUtils;

public class OwnersListAdapter extends ArrayAdapter<Owner> {

    private final ArrayList<Owner> data;
    private final Transformation transformation;

    public OwnersListAdapter(Activity context, ArrayList<Owner> owners) {
        super(context, R.layout.item_simple_owner, owners);
        data = owners;
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Owner getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_owner, parent, false);
            view.setTag(new ViewHolder(view));
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        Owner item = data.get(position);

        holder.tvName.setText(item.getFullName());
        ViewUtils.displayAvatar(holder.ivAvatar, transformation, item.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        holder.subtitle.setText(item instanceof User ? R.string.profile : R.string.community);
        return view;
    }

    private static class ViewHolder {

        final TextView tvName;
        final ImageView ivAvatar;
        final TextView subtitle;

        ViewHolder(View root) {
            tvName = root.findViewById(R.id.name);
            ivAvatar = root.findViewById(R.id.avatar);
            subtitle = root.findViewById(R.id.subtitle);
        }
    }
}
