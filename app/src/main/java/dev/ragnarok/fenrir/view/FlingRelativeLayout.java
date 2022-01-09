package dev.ragnarok.fenrir.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class FlingRelativeLayout extends RelativeLayout {

    private static final int SINGLE_TOUCH = 1;
    private GestureDetector mGestureDetector;
    private OnSingleFlingListener mSingleFlingListener;

    public FlingRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public FlingRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (mSingleFlingListener != null) {
                    if (e1.getPointerCount() > SINGLE_TOUCH || e2.getPointerCount() > SINGLE_TOUCH) {
                        return false;
                    }

                    return mSingleFlingListener.onFling(e1, e2, velocityX, velocityY);
                }
                return false;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = false;
        // Check to see if the user double tapped
        if (null != mGestureDetector && mGestureDetector.onTouchEvent(ev)) {
            handled = true;
        }

        return handled || super.onTouchEvent(ev);
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        mSingleFlingListener = onSingleFlingListener;
    }

    /**
     * Interface definition for a callback to be invoked when the FlingRelativeLayout is fling with a single
     * touch
     *
     * @author tonyjs
     */
    public interface OnSingleFlingListener {

        /**
         * A callback to receive where the user flings on a FlingRelativeLayout. You will receive a callback if
         * the user flings anywhere on the view.
         *
         * @param e1        - MotionEvent the user first touch.
         * @param e2        - MotionEvent the user last touch.
         * @param velocityX - distance of user's horizontal fling.
         * @param velocityY - distance of user's vertical fling.
         */
        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
    }
}
