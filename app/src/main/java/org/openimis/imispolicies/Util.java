package org.openimis.imispolicies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Util {
    public static class StringUtil {
        /**
         * @param string String to be checked
         * @return is string null or empty
         */
        public static boolean isEmpty(CharSequence string) {
            return isEmpty(string, false);
        }

        /**
         * @param string          String to be checked
         * @param checkNullString Should "null" string be considered empty
         * @return is string null or empty
         */
        public static boolean isEmpty(CharSequence string, boolean checkNullString) {
            return string == null
                    || string.equals("")
                    || (string.equals("null") && checkNullString);
        }

        public static boolean equals(CharSequence s1, CharSequence s2) {
            return TextUtils.equals(s1, s2);
        }
    }

    public static class JsonUtil {
        /**
         * @param object Json object
         * @param field  field to be checked
         * @return if the field does not exists, is null or empty
         */
        public static boolean isStringEmpty(JSONObject object, @NonNull String field) {
            return isStringEmpty(object, field, false);
        }

        /**
         * @param object          Json object
         * @param field           field to be checked
         * @param checkNullString Should "null" string be considered empty
         * @return if the field does not exists, is null or empty
         */
        public static boolean isStringEmpty(JSONObject object, @NonNull String field, boolean checkNullString) {
            try {
                return object == null
                        || !object.has(field)
                        || StringUtil.isEmpty(object.getString(field), checkNullString);
            } catch (JSONException e) {
                return true;
            }
        }
    }

    public static class AndroidUtil {
        public static ProgressDialog showProgressDialog(Context context, int titleResId, int messageResId) {
            return ProgressDialog.show(
                    context,
                    context.getResources().getString(titleResId),
                    context.getResources().getString(messageResId)
            );
        }

        public static void showToast(Context context, int messageResId) {
            Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
        }

        public static void showToast(Context context, CharSequence message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        public static AlertDialog showDialog(Context context, int messageResId) {
            return new AlertDialog.Builder(context)
                    .setMessage(messageResId)
                    .setCancelable(false)
                    .setPositiveButton(R.string.Ok, (dialogInterface, i) -> {
                    }).show();
        }

        public static AlertDialog showDialog(Context context, CharSequence message) {
            return new AlertDialog.Builder(context)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.Ok, (dialogInterface, i) -> {
                    }).show();
        }
    }

    public static class UriUtil {
        private static final String LOG_TAG = "UriUtil";

        public static String getDisplayName(Context context, Uri uri) {
            try (Cursor c = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                c.moveToFirst();
                c.moveToFirst();
                return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } catch (Exception e) {
                Log.e(LOG_TAG, "Reading file name from URI failed", e);
            }
            return null;
        }
    }

    public static class FileUtil {
        private static final String LOG_TAG = "FileUtil";

        /**
         * Only use this method if you can be sure the content of the input stream,
         * can be read, is a UTF-8 text and will fit in memory (as String).
         * There is a reason this is not a part of the standard libraries.
         *
         * @param is input stream to be read
         * @return content of the input stream or null if IO exception occurs
         */
        public static String readInputStreamAsUTF8String(InputStream is) {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();

            try {
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    stringBuilder.append(inputStr);
                return stringBuilder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while reading input stream", e);
            }

            return null;
        }

        /**
         * Replaces the extension of the file
         *
         * @param filename filename ending with extension
         * @param targetExtension target extension, including a dot (to protect from replacing
         *                        a substring inside filename that match current extension)
         * @return filename with replaced extension, or null if the filename does not end with
         * extension
         */
        public static String replaceFilenameExtension(String filename, String targetExtension) {
            try {
                String currentExtension = filename.substring(filename.lastIndexOf('.'));
                return filename.replace(currentExtension, targetExtension);
            } catch (IndexOutOfBoundsException e) {
                Log.e(LOG_TAG, "Filename does not have an extension at the end", e);
            }
            return null;
        }
    }
}
