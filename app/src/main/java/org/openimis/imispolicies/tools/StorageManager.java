package org.openimis.imispolicies.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import android.util.Log;

public class StorageManager {
    protected static final String LOG_TAG = "STORAGEMANAGER";

    protected final Context context;

    public static StorageManager of(Context context) {
        return new StorageManager(context);
    }

    protected StorageManager(Context context) {
        this.context = context;
    }

    public void requestOpenDirectory(int requestCode, Uri initialUri) {
        if (!(this.context instanceof Activity)) {
            Log.e(LOG_TAG, "Provided context in not an Activity. Activity is required to start a request.");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (initialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }

        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    public void requestOpenFile(int requestCode, @NonNull String mimeType, Uri initialUri) {
        if (!(this.context instanceof Activity)) {
            Log.e(LOG_TAG, "Provided context in not an Activity. Activity is required to start a request.");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
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
}
