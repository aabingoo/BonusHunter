package com.bonushunter.utils;

import android.util.Log;

public class LogUtils {
    private static final String TAG = "BonusHunterLog";
    private static boolean bDebug = true;

    public static void d(String className, String msg) {
        if (bDebug) {
            Log.d(TAG, "[ " + className + " ] - " + msg);
        }
    }

    public static void i(String className, String msg) {
        Log.i(TAG, "[ " + className + " ] - " + msg);
    }

    public static void w(String className, String msg) {
        Log.w(TAG, "[ " + className + " ] - " + msg);
    }

    public static void e(String className, String msg) {
        Log.e(TAG, "[ " + className + " ] - " + msg);
    }

    public static void e(String className, String msg, Throwable tr) {
        Log.e(TAG, "[ " + className + " ] - " + msg, tr);
    }

    public static void v(String className, String msg) {
        Log.v(TAG, "[ " + className + " ] - " + msg);
    }
}
