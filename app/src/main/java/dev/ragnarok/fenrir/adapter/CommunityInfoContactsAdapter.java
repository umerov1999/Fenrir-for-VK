package dev.ragnarok.fenrir.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.OnlineView;

public class CommunityInfoContactsAdapter extends RecyclerView.Adapter<CommunityInfoContactsAdapter.Holder> {

    private final Transformation transformation;
    private List<Manager> users;
    private ActionListener actionListener;

    public CommunityInfoContactsAdapter(List<Manager> users) {
        this.users = users;
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_manager, parent, false));
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Manager manager = users.get(position);
        User user = manager.getUser();

        holder.name.setText(user.getFullName());

        ViewUtils.displayAvatar(holder.avatar, transformation, user.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        Integer onlineRes = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if (Objects.nonNull(onlineRes)) {
            holder.onlineView.setIcon(onlineRes);
            holder.onlineView.setVisibility(View.VISIBLE);
        } else {
            holder.onlineView.setVisibility(View.GONE);
        }

        @StringRes
        int roleTextRes;

        if (manager.getContactInfo() != null && manager.getContactInfo().getDescriprion() != null)
            holder.role.setText(manager.getContactInfo().getDescriprion());
        else {
            roleTextRes = R.string.role_unknown;
            holder.role.setText(roleTextRes);
        }
        holder.itemView.setOnClickListener(v -> {
            if (Objects.nonNull(actionListener)) {
                actionListener.onManagerClick(user);
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setData(List<Manager> data) {
        users = data;
        notifyDataSetChanged();
    }

    public interface ActionListener {
        void onManagerClick(User manager);
    }

    static class Holder extends RecyclerView.ViewHolder {

        final ImageView avatar;
        final OnlineView onlineView;
        final TextView name;
        final TextView role;

        Holder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            onlineView = itemView.findViewById(R.id.online);
            name = itemView.findViewById(R.id.name);
            role = itemView.findViewById(R.id.role);
        }
    }
}
