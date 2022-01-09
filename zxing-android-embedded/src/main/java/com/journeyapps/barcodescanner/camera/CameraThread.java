package com.journeyapps.barcodescanner.camera;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Singleton thread that is started and stopped on demand.
 * <p>
 * Any access to Camera / CameraManager should happen on this thread, through CameraInstance.
 */
class CameraThread {
    private static final String TAG = CameraThread.class.getSimpleName();

    private static CameraThread instance;
    private final Object LOCK = new Object();
    private Handler handler;
    private HandlerThread thread;

    private int openCount;

    private CameraThread() {
    }

    public static CameraThread getInstance() {
        if (instance == null) {
            instance = new CameraThread();
        }
        return instance;
    }

    /**
     * Call from main thread or camera thread.
     * <p>
     * Enqueues a task on the camera thread.
     *
     * @param runnable the task to enqueue
     */
    protected void enqueue(Runnable runnable) {
        synchronized (LOCK) {
            checkRunning();
            handler.post(runnable);
        }
    }

    /**
     * Call from main thread or camera thread.
     * <p>
     * Enqueues a task on the camera thread.
     *
     * @param runnable    the task to enqueue
     * @param delayMillis the delay in milliseconds before executing the runnable
     */
    protected void enqueueDelayed(Runnable runnable, long delayMillis) {
        synchronized (LOCK) {
            checkRunning();
            handler.postDelayed(runnable, delayMillis);
        }
    }

    private void checkRunning() {
        synchronized (LOCK) {
            if (handler == null) {
                if (openCount <= 0) {
                    throw new IllegalStateException("CameraThread is not open");
                }
                thread = new HandlerThread("CameraThread");
                thread.start();
                handler = new Handler(thread.getLooper());
            }
        }
    }

    /**
     * Call from camera thread.
     */
    private void quit() {
        synchronized (LOCK) {
            thread.quit();
            thread = null;
            handler = null;
        }
    }

    /**
     * Call from camera thread
     */
    protected void decrementInstances() {
        synchronized (LOCK) {
            openCount -= 1;
            if (openCount == 0) {
                quit();
            }
        }
    }

    /**
     * Call from main thread.
     *
     * @param runner The {@link Runnable} to be enqueued
     */
    protected void incrementAndEnqueue(Runnable runner) {
        synchronized (LOCK) {
            openCount += 1;
            enqueue(runner);
        }
    }
}
