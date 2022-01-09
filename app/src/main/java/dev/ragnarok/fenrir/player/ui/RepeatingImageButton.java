package dev.ragnarok.fenrir.player.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import androidx.appcompat.widget.AppCompatImageButton;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.player.MusicPlaybackController;

/**
 * A {@link ImageButton} that will repeatedly call a 'listener' method as long
 * as the button is pressed, otherwise functions like a typical
 * {@link ImageButton}
 */
public class RepeatingImageButton extends AppCompatImageButton implements OnClickListener {

    private static final long sInterval = 400;
    private long mStartTime;
    private int mRepeatCount;
    private RepeatListener mListener;
    private final Runnable mRepeater = new Runnable() {
        @Override
        public void run() {
            doRepeat(false);
            if (isPressed()) {
                postDelayed(this, sInterval);
            }
        }
    };

    public RepeatingImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setLongClickable(true);
        setOnClickListener(this);
        updateState();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.action_button_previous) {
            MusicPlaybackController.previous(getContext());
        } else if (view.getId() == R.id.action_button_next) {
            MusicPlaybackController.next();
        }
    }

    public void setRepeatListener(RepeatListener l) {
        mListener = l;
    }

    @Override
    public boolean performLongClick() {
        mStartTime = SystemClock.elapsedRealtime();
        mRepeatCount = 0;
        post(mRepeater);
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            /* Remove the repeater, but call the hook one more time */
            removeCallbacks(mRepeater);
            if (mStartTime != 0) {
                doRepeat(true);
                mStartTime = 0;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                /*
                 * Need to call super to make long press work, but return true
                 * so that the application doesn't get the down event
                 */
                super.onKeyDown(keyCode, event);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                /* Remove the repeater, but call the hook one more time */
                removeCallbacks(mRepeater);
                if (mStartTime != 0) {
                    doRepeat(true);
                    mStartTime = 0;
                }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * @param shouldRepeat If True the repeat count stops at -1, false if to add
     *                     incrementally add the repeat count
     */
    private void doRepeat(boolean shouldRepeat) {
        long now = SystemClock.elapsedRealtime();
        if (mListener != null) {
            mListener.onRepeat(this, now - mStartTime, shouldRepeat ? -1 : mRepeatCount++);
        }
    }

    public void updateState() {
        if (getId() == R.id.action_button_next) {
            setImageResource(R.drawable.page_last);
        } else if (getId() == R.id.action_button_previous) {
            setImageResource(R.drawable.page_first);
        }
    }

    public interface RepeatListener {

        /**
         * @param v           View to be set
         * @param duration    Duration of the long press
         * @param repeatcount The number of repeat counts
         */
        void onRepeat(View v, long duration, int repeatcount);
    }

}
