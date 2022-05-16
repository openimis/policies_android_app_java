package org.openimis.imispolicies.tools;

import android.net.Uri;
import android.support.v4.content.FileProvider;

import org.openimis.imispolicies.AppInformation;
import org.openimis.imispolicies.BuildConfig;
import org.openimis.imispolicies.Global;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * This class is a wrapper for the default android Log class, adding additional functionality while
 * keeping the android.util.Log api
 */
public class Log {
    public static final boolean isLoggingEnabled = BuildConfig.LOGGING_ENABLED;

    private static final String logFilePrefix = "log-";
    private static final String logExportFileName = "logs.zip";
    private static final String[] levelMapping = {"V", "D", "I", "W", "E", "A"};
    private static File logFile = null;

    public static void v(String tag, String msg) {
        log(tag, msg, android.util.Log.VERBOSE);
    }

    public static void v(String tag, String msg, Throwable thr) {
        log(tag, String.format("%s\n%s", msg, android.util.Log.getStackTraceString(thr)), android.util.Log.VERBOSE);
    }

    public static void d(String tag, String msg) {
        log(tag, msg, android.util.Log.DEBUG);
    }

    public static void d(String tag, String msg, Throwable thr) {
        log(tag, String.format("%s\n%s", msg, android.util.Log.getStackTraceString(thr)), android.util.Log.DEBUG);
    }

    public static void i(String tag, String msg) {
        log(tag, msg, android.util.Log.INFO);
    }

    public static void i(String tag, String msg, Throwable thr) {
        log(tag, String.format("%s\n%s", msg, android.util.Log.getStackTraceString(thr)), android.util.Log.INFO);
    }

    public static void w(String tag, String msg) {
        log(tag, msg, android.util.Log.WARN);
    }

    public static void w(String tag, String msg, Throwable thr) {
        log(tag, String.format("%s\n%s", msg, android.util.Log.getStackTraceString(thr)), android.util.Log.WARN);
    }

    public static void e(String tag, String msg) {
        log(tag, msg, android.util.Log.ERROR);
    }

    public static void e(String tag, String msg, Throwable thr) {
        log(tag, String.format("%s\n%s", msg, android.util.Log.getStackTraceString(thr)), android.util.Log.ERROR);
    }

    private static void log(String tag, String msg, int level) {
        if (level < 2 || level > 7) {
            throw new RuntimeException("Unknown log level: " + level);
        }

        if (isLoggingEnabled) {
            new Thread(() -> {

                if (level >= BuildConfig.CONSOLE_LOG_LEVEL) {
                    android.util.Log.println(level, tag, msg);
                }

                if (level >= BuildConfig.FILE_LOG_LEVEL) {
                    storeLog(tag, msg, level);
                }
            }).start();
        }
    }

    public static void zipLogFiles() {
        File cacheDir = Global.getContext().getExternalCacheDir();
        File[] logFiles = cacheDir.listFiles((dir, filename) -> filename.startsWith(logFilePrefix));
        File targetFile = new File(cacheDir, logExportFileName);

        if (logFiles != null) {
            ArrayList<File> filesToZip = new ArrayList<>(Arrays.asList(logFiles));
            Compressor.zip(filesToZip, targetFile, "");
        }

        Uri logExportUri = FileProvider.getUriForFile(Global.getContext(),
                String.format("%s.fileprovider", BuildConfig.APPLICATION_ID),
                targetFile);

        Global.getGlobal().sendFile(logExportUri, "application/zip");
    }

    public static void deleteLogFiles() {
        File cacheDir = Global.getContext().getExternalCacheDir();
        File[] logFiles = cacheDir.listFiles((dir, filename) -> filename.startsWith(logFilePrefix));
        if (logFiles != null) {
            for (File f : logFiles) {
                f.delete();
            }
        }
    }

    private synchronized static void storeLog(String tag, String msg, int level) {
        Date date = new Date();
        initializeLogFile(date);
        try {
            OutputStream os = new FileOutputStream(logFile, true);
            os.write(buildLogLine(tag, msg, level, date).getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (IOException e) {
            throw new RuntimeException("Writing to log file failed", e);
        }
    }

    private static void initializeLogFile(Date date) {
        if (logFile == null || !logFile.exists()) {
            File cacheDir = Global.getContext().getExternalCacheDir();

            String filename = String.format("%s%s.txt",logFilePrefix, AppInformation.DateTimeInfo.getDefaultFileDatetimeFormatter().format(date));
            logFile = new File(cacheDir, filename);

            try {
                if (!logFile.createNewFile())
                    throw new RuntimeException("Log file creation failed");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String buildLogLine(String tag, String msg, int level, Date date) {
        String dateString = AppInformation.DateTimeInfo.getDefaultIsoDatetimeFormatter().format(date);
        return String.format(Locale.US, "%s %s %s %s\n", dateString, levelMapping[level - 2], tag, msg);
    }
}
