package org.openimis.imispolicies.tools;

import org.openimis.imispolicies.BuildConfig;

/**
 * This class is a wrapper for the default android Log class, adding additional functionality while
 * keeping the android.util.Log api
 * Release version should not allow any logging
 */
public class Log {
    public static final boolean isLoggingEnabled = BuildConfig.LOGGING_ENABLED;

    public static void v(String tag, String msg) {
    }

    public static void v(String tag, String msg, Throwable thr) {
    }

    public static void d(String tag, String msg) {
    }

    public static void d(String tag, String msg, Throwable thr) {
    }

    public static void i(String tag, String msg) {
    }

    public static void i(String tag, String msg, Throwable thr) {
    }

    public static void w(String tag, String msg) {
    }

    public static void w(String tag, String msg, Throwable thr) {
    }

    public static void e(String tag, String msg) {
    }

    public static void e(String tag, String msg, Throwable thr) {
    }

    public static void zipLogFiles() {
    }

    public static void deleteLogFiles() {
    }
}
