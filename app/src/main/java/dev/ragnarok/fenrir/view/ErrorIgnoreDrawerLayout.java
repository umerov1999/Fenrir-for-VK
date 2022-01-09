package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.drawerlayout.widget.DrawerLayout;

public class ErrorIgnoreDrawerLayout extends DrawerLayout {

    public ErrorIgnoreDrawerLayout(Context context) {
        super(context);
    }

    public ErrorIgnoreDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ignored) {
        }
        return false;
    }
}