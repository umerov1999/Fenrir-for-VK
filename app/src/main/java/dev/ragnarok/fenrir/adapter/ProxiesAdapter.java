package dev.ragnarok.fenrir.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.util.Utils;


public class ProxiesAdapter extends RecyclerBindableAdapter<ProxyConfig, ProxiesAdapter.Holder> {

    private final ActionListener actionListener;
    private ProxyConfig active;

    public ProxiesAdapter(List<ProxyConfig> data, ActionListener actionListener) {
        super(data);
        this.actionListener = actionListener;
    }

    @Override
    protected void onBindItemViewHolder(Holder holder, int position, int type) {
        ProxyConfig config = getItem(position);

        boolean isActive = config.equals(active);

        holder.address.setText(config.getAddress());
        holder.port.setText(String.valueOf(config.getPort()));
        holder.username.setText(config.getUser());

        StringBuilder pass = new StringBuilder();
        if (Utils.nonEmpty(config.getPass())) {
            for (int i = 0; i < config.getPass().length(); i++) {
                pass.append("*");
            }
        }

        holder.pass.setText(pass.toString());

        holder.setAsActive.setOnClickListener(v -> actionListener.onSetAtiveClick(config));
        holder.delete.setOnClickListener(v -> actionListener.onDeleteClick(config));
        holder.disable.setOnClickListener(v -> actionListener.onDisableClick(config));

        holder.disable.setVisibility(isActive ? View.VISIBLE : View.GONE);
        holder.setAsActive.setVisibility(isActive ? View.GONE : View.VISIBLE);
        holder.delete.setVisibility(isActive ? View.GONE : View.VISIBLE);
    }

    public void setActive(ProxyConfig active) {
        this.active = active;
        notifyDataSetChanged();
    }

    public void setData(List<ProxyConfig> data, ProxyConfig config) {
        setItems(data, false);
        active = config;
        notifyDataSetChanged();
    }

    @Override
    protected Holder viewHolder(View view, int type) {
        return new Holder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_proxy;
    }

    public interface ActionListener {
        void onDeleteClick(ProxyConfig config);

        void onSetAtiveClick(ProxyConfig config);

        void onDisableClick(ProxyConfig config);
    }

    static class Holder extends RecyclerView.ViewHolder {

        final TextView address;
        final TextView port;
        final TextView username;
        final TextView pass;

        final View delete;
        final View setAsActive;
        final View disable;

        Holder(View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.address);
            port = itemView.findViewById(R.id.port);
            username = itemView.findViewById(R.id.username);
            pass = itemView.findViewById(R.id.password);

            delete = itemView.findViewById(R.id.button_delete);
            setAsActive = itemView.findViewById(R.id.button_set_as_active);
            disable = itemView.findViewById(R.id.button_disable);
        }
    }
}
