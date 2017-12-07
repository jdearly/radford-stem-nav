package com.amazonaws.mobile.util;


import android.os.Handler;
import android.os.Looper;

public class ThreadUtils {
    private ThreadUtils() {
    }

    /**
     * Run a runnable on the Main (UI) Thread.
     *
     * @param runnable the runnable
     */
    public static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(runnable);
        } else {
            runnable.run();
        }
    }
}
