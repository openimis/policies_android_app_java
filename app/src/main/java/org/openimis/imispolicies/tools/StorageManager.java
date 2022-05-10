package org.openimis.imispolicies.tools;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import org.openimis.imispolicies.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StorageManager {
    protected static final String LOG_TAG = "STORAGEMANAGER";
    protected static final String imisDirectory = BuildConfig.APP_DIR;

    protected final Context context;
    protected final ContentResolver contentResolver;

    public static StorageManager of(Context context) {
        return new StorageManager(context);
    }

    protected StorageManager(Context context) {
        this.context = context;
        this.contentResolver = this.context.getContentResolver();
    }

    public void requestOpenFile(int requestCode, @NonNull String mimeType, Uri initialUri) {
        if (!(this.context instanceof Activity)) {
            Log.e(LOG_TAG, "Provided context in not an Activity. Activity is required to start a request.");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        if (initialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }

        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    public void requestOpenFile(int requestCode, @NonNull String mimeType) {
        requestOpenFile(requestCode, mimeType, null);
    }

    public void requestOpenFile(int requestCode) {
        requestOpenFile(requestCode, "*/*", null);
    }

    public void requestCreateFile(int requestCode, @NonNull String mimeType, String initialFileName, Uri initialUri) {
        if (!(this.context instanceof Activity)) {
            Log.e(LOG_TAG, "Provided context in not an Activity. Activity is required to start a request.");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType(mimeType);
        if (initialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }
        if (initialFileName != null) {
            intent.putExtra(Intent.EXTRA_TITLE, initialFileName);
        }

        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    public void requestCreateFile(int requestCode, @NonNull String mimeType, String initialFileName) {
        requestCreateFile(requestCode, mimeType, initialFileName, null);
    }

    /**
     * Copy the content of the provided uri to provided file. The file location must allow creating
     * a file and writing to a file.
     *
     * @param uri        Uri of the file to copy. The Uri should allow read and should be openable
     * @param targetFile File to be used as output stream
     * @return File object pointing to created file
     */
    public File copyUriContentToFile(@NonNull Uri uri, @NonNull File targetFile) {
        if (!targetFile.exists()) {
            Util.FileUtil.createFileWithSubdirectories(targetFile);
        }
        try (InputStream is = contentResolver.openInputStream(uri)) {
            Util.FileUtil.writeToFile(targetFile, is);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while opening streams", e);
        }

        return targetFile;
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
    public File copyUriContentToCache(@NonNull Uri uri, @NonNull String targetPath) {
        return copyUriContentToFile(uri, new File(context.getCacheDir(), targetPath));
    }

    /**
     * Copy byte content of the file to location described by the uri. The uri must be openable and
     * allow writing. The file must be openable and readable.
     *
     * @param file File to be read
     * @param uri  Uri pointing to writable location
     */
    public void copyFileToUri(@NonNull File file, @NonNull Uri uri) {
        try (InputStream is = new FileInputStream(file)) {
            Util.UriUtil.writeToUri(context, uri, is);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while opening streams", e);
        }
    }

    /**
     * Copy byte content of the file to location described by the uri. The uri must be openable and
     * allow writing. The file must be openable and readable.
     *
     * @param filePath Path to file to be read
     * @param uri      Uri pointing to writable location
     */
    public void copyFileToUri(@NonNull String filePath, @NonNull Uri uri) {
        copyFileToUri(new File(filePath), uri);
    }
}
