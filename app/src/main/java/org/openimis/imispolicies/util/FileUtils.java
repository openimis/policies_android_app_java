package org.openimis.imispolicies.util;

import android.content.Context;
import android.support.annotation.NonNull;

import org.openimis.imispolicies.tools.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class FileUtils {
    private static final String LOG_TAG = "FileUtil";

    /**
     * Replaces the extension of the file
     *
     * @param filename        filename ending with extension
     * @param targetExtension target extension, including a dot (to protect from replacing
     *                        a substring inside filename that match current extension)
     * @return filename with replaced extension, or null if the filename does not end with
     * extension
     */
    public static String replaceFilenameExtension(@NonNull String filename, @NonNull String targetExtension) {
        try {
            String currentExtension = filename.substring(filename.lastIndexOf('.'));
            return filename.replace(currentExtension, targetExtension);
        } catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, "Filename does not have an extension at the end", e);
        }
        return null;
    }

    public static boolean createFileWithSubdirectories(@NonNull File file) {
        try {
            File parent = file.getParentFile();

            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                Log.e(LOG_TAG, "Creating parent directories failed for path: " + file.getAbsolutePath());
            }

            if (!file.createNewFile()) {
                Log.e(LOG_TAG, "File already exists: " + file.getAbsolutePath());
            }

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while creating file: " + file.getAbsolutePath(), e);
        }

        return false;
    }

    public static boolean createDirectoryWithSubdirectories(@NonNull File directory) {
        try {
            if (!directory.exists() && !directory.mkdirs()) {
                Log.e(LOG_TAG, "Creating directory failed for path: " + directory.getAbsolutePath());
            }
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while creating directory: " + directory.getAbsolutePath(), e);
        }

        return false;
    }

    public static void deleteFiles(File[] files) {
        for (File file : files) {
            deleteFile(file);
        }
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            if (!file.delete()) {
                Log.w(LOG_TAG, "Delete file failed: " + file.getAbsolutePath());
            }
        } else {
            Log.w(LOG_TAG, "File does not exists: " + file.getAbsolutePath());
        }
    }

    public static void writeToFile(@NonNull File file, @NonNull InputStream is) {
        try (OutputStream os = new FileOutputStream(file)) {
            StreamUtils.bufferedStreamCopy(is, os);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while writing to file: " + file.getAbsolutePath(), e);
        }
    }

    public static File[] getFilesStartingWith(@NonNull File directory, @NonNull String filenamePrefix) {
        if (!directory.exists()) {
            android.util.Log.e(LOG_TAG, "Directory does not exists: " + directory);
            return null;
        }

        if (!directory.isDirectory()) {
            android.util.Log.e(LOG_TAG, "Provided path is not a directory: " + directory);
            return null;
        }

        return directory.listFiles((dir, filename) -> filename.startsWith(filenamePrefix));
    }

    public static File getNewestFileStartingWith(@NonNull File directory, @NonNull String filenamePrefix) {
        File[] files = getFilesStartingWith(directory, filenamePrefix);
        if (files == null || files.length == 0) {
            return null;
        }
        Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        return files[files.length - 1];
    }

    public static String readFileAsUTF8String(@NonNull File file) {
        try {
            InputStream is = new FileInputStream(file);
            return StreamUtils.readInputStreamAsUTF8String(is);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Opening input stream failed for file: " + file.getAbsolutePath(), e);
        }
        return null;
    }

    public static File createOrCheckDirectory(File directory) {
        if (directory.exists() || directory.mkdirs()) {
            return directory;
        } else {
            return null;
        }
    }

    public static int getFileCount(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            Log.e(LOG_TAG, "Not a directory: " + directory.getAbsolutePath());
            return -1;
        }

        File[] files = directory.listFiles(File::isFile);
        if (files != null) {
            return files.length;
        } else {
            Log.e(LOG_TAG, "Counting files failed: " + directory.getAbsolutePath());
            return -1;
        }
    }

    /**
     * Create a temporary file inside app's cache directory under specified path. This file should be
     * manually deleted, but the cache directory can be emptied by the user or by the Android to save
     * storage space. If the file exists, it will be deleted and recreated.
     *
     * @param targetPath Path to the temporary file inside cache, the cache root will be appended
     * @return Created temp file
     */
    public static File createTempFile(@NonNull Context context, @NonNull String targetPath) {
        return createTempFile(context, targetPath, false);
    }

    /**
     * Create a temporary file inside app's cache directory under specified path. This file should be
     * manually deleted, but the cache directory can be emptied by the user or by the Android to save
     * storage space. If the file exists, it will be deleted and recreated.
     *
     * @param targetPath     Path to the temporary file inside cache, the cache root will be appended
     * @param preparePathOly Set to true if the abstract path should point to non-existing file
     *                       (i.e. to forward the file object to a library to create the file)
     * @return Created temp file
     */
    public static File createTempFile(@NonNull Context context, @NonNull String targetPath, boolean preparePathOly) {
        File tempFile = new File(context.getCacheDir(), targetPath);

        if (tempFile.exists() && tempFile.delete()) {
            org.openimis.imispolicies.tools.Log.i(LOG_TAG, "Leftover temp file deleted: " + tempFile.getAbsolutePath());
        }

        if (preparePathOly) {
            File parentFile = tempFile.getParentFile();
            if (parentFile != null) {
                return FileUtils.createDirectoryWithSubdirectories(parentFile) ? tempFile : null;
            } else {
                return null;
            }
        } else {
            return FileUtils.createFileWithSubdirectories(tempFile) ? tempFile : null;
        }
    }

    /**
     * Convenience method for removing a temp file form specified path inside cache directory created
     * by createTempFile(String). The file can also be safely deleted with returned File object.
     *
     * @param targetPath Path to the temporary file inside cache, the cache root will be appended
     * @return true if file under targetPath does not exist or was successfully deleted, false if file
     * exists and delete failed
     */
    public static boolean removeTempFile(@NonNull Context context, @NonNull String targetPath) {
        File tempFile = new File(context.getCacheDir(), targetPath);
        return !tempFile.exists() || tempFile.delete();
    }
}
