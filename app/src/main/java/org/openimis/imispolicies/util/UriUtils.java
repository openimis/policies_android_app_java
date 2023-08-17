package org.openimis.imispolicies.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import org.openimis.imispolicies.BuildConfig;
import org.openimis.imispolicies.tools.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UriUtils {
    private static final String LOG_TAG = "URIUTIL";
    public static final String FILE_PROVIDER_NAME = String.format("%s.fileprovider", BuildConfig.APPLICATION_ID);

    public static String getDisplayName(@NonNull Context context, @NonNull Uri uri) {
        try (Cursor c = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            c.moveToFirst();
            c.moveToFirst();
            return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Reading file name from URI failed", e);
        }
        return null;
    }


    public static void writeToUri(@NonNull Context context, @NonNull Uri uri, @NonNull InputStream is) {
        try (OutputStream os = context.getContentResolver().openOutputStream(uri, "w")) {
            StreamUtils.bufferedStreamCopy(is, os);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Writing to uri failed: " + uri, e);
        }
    }

    /**
     * Copy byte content of the file to location described by the uri. The uri must be openable and
     * allow writing. The file must be openable and readable.
     *
     * @param file File to be read
     * @param uri  Uri pointing to writable location
     */
    public static void copyFileToUri(@NonNull Context context, @NonNull File file, @NonNull Uri uri) {
        try (InputStream is = new FileInputStream(file)) {
            writeToUri(context, uri, is);
        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Error while opening streams", e);
        }
    }

    /**
     * Copy byte content of the file to location described by the uri. The uri must be openable and
     * allow writing. The file must be openable and readable.
     *
     * @param filePath Path to file to be read
     * @param uri      Uri pointing to writable location
     */
    public static void copyFileToUri(@NonNull Context context, @NonNull String filePath, @NonNull Uri uri) {
        copyFileToUri(context, new File(filePath), uri);
    }

    /**
     * Copy the content of the provided uri to provided file. The file location must allow creating
     * a file and writing to a file.
     *
     * @param uri        Uri of the file to copy. The Uri should allow read and should be openable
     * @param targetFile File to be used as output stream
     * @return File object pointing to created file
     */
    public static File copyUriContentToFile(@NonNull Context context, @NonNull Uri uri, @NonNull File targetFile) {
        if (!targetFile.exists()) {
            FileUtils.createFileWithSubdirectories(targetFile);
        }
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            FileUtils.writeToFile(targetFile, is);
        } catch (IOException e) {
            android.util.Log.e(LOG_TAG, "Error while opening streams", e);
        }

        return targetFile;
    }


    /**
     * Create a content URI for a file owned by the app. The directory path have to be specified in
     * res/xml/paths.xml to be eligible to provide content. Uri returned by this method can be shared
     * with other apps with intents.
     *
     * @param file File to be specified by generated content URI
     * @return Content URI for a given file
     */
    public static Uri createUriForFile(@NonNull Context context, @NonNull File file) {
        Uri uri = FileProvider.getUriForFile(context, FILE_PROVIDER_NAME, file);
        if (uri == null) {
            org.openimis.imispolicies.tools.Log.w(LOG_TAG, "Failed to create temp photo URI");
        }
        return uri;
    }

    /**
     * Copy the content of the provided uri to cache directory under provided path. The caller is
     * responsible to delete the file after use. The file can be deleted by Android or the user
     * without notifying the app.
     *
     * @param uri        Uri of the file to copy. The Uri should allow read and should be openable
     * @param targetPath Path inside cache directory to save the file (cache location will be appended)
     * @return File object pointing to created file
     */
    public static File copyUriContentToCache(@NonNull Context context, @NonNull Uri uri, @NonNull String targetPath) {
        File outputFile = FileUtils.createTempFile(context, targetPath, false);
        if (outputFile != null) {
            return copyUriContentToFile(context, uri, outputFile);
        } else {
            Log.e(LOG_TAG, "Creating temp file failed for: " + targetPath);
            return null;
        }


    }
}
