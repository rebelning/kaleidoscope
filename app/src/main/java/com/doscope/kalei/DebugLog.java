package com.doscope.kalei;

import android.util.Log;


/**
 * 调试log
 *
 * @author sxn
 */
public class DebugLog {

    public DebugLog() {
    }

    /**
     * Log.d
     *
     * @param tag LOGTAG
     * @param des msg
     */
    public static void d(String tag, String des) {
        if (BuildConfig.ENABLE_DEBUG)
            Log.d(tag, des);
    }

    /**
     * Log.w
     *
     * @param tag LOGTAG
     * @param des msg
     */
    public static void w(String tag, String des) {
        if (BuildConfig.ENABLE_DEBUG)
            Log.w(tag, des);
    }

    /**
     * Log.i
     *
     * @param tag LOGTAG
     * @param des msg
     */
    public static void i(String tag, String des) {
        if (BuildConfig.ENABLE_DEBUG)
            Log.i(tag, des);
    }

    /**
     * Log.e
     *
     * @param tag LOGTAG
     * @param des msg
     */
    public static void e(String tag, String des) {
        if (BuildConfig.ENABLE_DEBUG)
            Log.e(tag, des);
    }

    /**
     * log.e throwable t
     *
     * @param tag
     * @param des
     * @param t
     */
    public static void e(String tag, String des, Throwable t) {
        if (BuildConfig.ENABLE_DEBUG)
            Log.e(tag, des, t);
    }

    /**
     * Log.v
     *
     * @param tag LOGTAG
     * @param des msg
     */
    public static void v(String tag, String des) {
        if (BuildConfig.ENABLE_DEBUG)
            Log.v(tag, des);
    }


}
