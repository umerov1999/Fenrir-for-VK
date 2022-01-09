package dev.ragnarok.fenrir.adapter.base;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import dev.ragnarok.fenrir.adapter.listener.OwnerClickListener;

public abstract class AbsRecyclerViewAdapter<H extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<H> {

    private OwnerClickListener ownerClickListener;

    public void setOwnerClickListener(OwnerClickListener ownerClickListener) {
        this.ownerClickListener = ownerClickListener;
    }

    protected void addOwnerAvatarClickHandling(View view, int ownerId) {
        view.setOnClickListener(v -> {
            if (nonNull(ownerClickListener)) {
                ownerClickListener.onOwnerClick(ownerId);
            }
        });
    }
}