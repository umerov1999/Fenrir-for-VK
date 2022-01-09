package dev.ragnarok.fenrir.activity.slidr.model;

import androidx.customview.widget.ViewDragHelper;

/**
 * This listener interface is for receiving events from the sliding panel such as state changes
 * and slide progress
 */
public interface SlidrListener {

    /**
     * This is called when the {@link ViewDragHelper} calls it's
     * state change callback.
     *
     * @param state the {@link ViewDragHelper} state
     * @see ViewDragHelper#STATE_IDLE
     * @see ViewDragHelper#STATE_DRAGGING
     * @see ViewDragHelper#STATE_SETTLING
     */
    void onSlideStateChanged(int state);

    void onSlideChange(float percent);

    void onSlideOpened();

    /**
     * @return <code>true</code> than event was processed in the callback.
     */
    boolean onSlideClosed();
}
