package dev.ragnarok.fenrir.listener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dev.ragnarok.fenrir.picasso.PicassoInstance;

public class PicassoPauseOnScrollListener extends RecyclerView.OnScrollListener {

    private final String tag;

    public PicassoPauseOnScrollListener(String tag) {
        this.tag = tag;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            PicassoInstance.with().resumeTag(tag);
        } else {
            PicassoInstance.with().pauseTag(tag);
        }
    }
}