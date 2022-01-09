package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Objects;

public class CommunityInfoLinksAdapter extends RecyclerView.Adapter<CommunityInfoLinksAdapter.Holder> {

    private List<VKApiCommunity.Link> links;
    private ActionListener actionListener;

    public CommunityInfoLinksAdapter(List<VKApiCommunity.Link> links) {
        this.links = links;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_link_info, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        VKApiCommunity.Link link = links.get(position);

        holder.title.setText(link.name);
        holder.subtitle.setText(link.desc);

        holder.itemView.setOnClickListener(v -> {
            if (Objects.nonNull(actionListener)) {
                actionListener.onClick(link);
            }
        });

        String photoUrl = link.photo_100;

        if (nonEmpty(photoUrl)) {
            holder.icon.setVisibility(View.VISIBLE);
            PicassoInstance.with()
                    .load(photoUrl)
                    .transform(CurrentTheme.createTransformationForAvatar())
                    .into(holder.icon);
        } else {
            PicassoInstance.with()
                    .cancelRequest(holder.icon);
            holder.icon.setVisibility(View.GONE);
        }
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @Override
    public int getItemCount() {
        return links.size();
    }

    public void setData(List<VKApiCommunity.Link> data) {
        links = data;
        notifyDataSetChanged();
    }

    public interface ActionListener {
        void onClick(VKApiCommunity.Link link);
    }

    static class Holder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView subtitle;
        final ImageView icon;

        Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
