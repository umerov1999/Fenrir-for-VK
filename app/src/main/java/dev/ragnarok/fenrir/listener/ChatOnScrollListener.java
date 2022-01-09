package dev.ragnarok.fenrir.listener;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChatOnScrollListener extends RecyclerView.OnScrollListener {

    private final FloatingActionButton fab;

    public ChatOnScrollListener(@NonNull FloatingActionButton fab) {
        this.fab = fab;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (!fab.isShown()) {
                fab.show();
            }
        } else {
            if (fab.isShown()) {
                fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        fab.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }
}
