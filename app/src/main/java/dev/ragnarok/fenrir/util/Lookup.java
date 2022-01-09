package dev.ragnarok.fenrir.util;

import android.os.Handler;
import android.os.Looper;

public class Lookup {

    private static final int LOOKUP = 1540;

    private final Handler mHandler;

    private int mDelay;
    private Callback mCallback;

    public Lookup(int initialDelay) {
        mDelay = initialDelay;
        mHandler = new Handler(Looper.getMainLooper(), msg -> {
            onLookupHandle();
            return true;
        });
    }

    private void onLookupHandle() {
        mHandler.sendEmptyMessageDelayed(LOOKUP, mDelay);
        if (Objects.nonNull(mCallback)) {
            mCallback.onIterated();
        }
    }

    public void changeDelayTime(int delay, boolean startNow) {
        mDelay = delay;
        if (startNow) {
            mHandler.removeMessages(LOOKUP);
            mHandler.sendEmptyMessageDelayed(LOOKUP, mDelay);
        }
    }

    public void stop() {
        mHandler.removeMessages(LOOKUP);
    }

    public void start() {
        if (!mHandler.hasMessages(LOOKUP)) {
            mHandler.sendEmptyMessageDelayed(LOOKUP, mDelay);
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onIterated();
    }
}