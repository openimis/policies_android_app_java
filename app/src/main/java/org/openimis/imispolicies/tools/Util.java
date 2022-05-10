package org.openimis.imispolicies.tools;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

        public static AlertDialog showConfirmDialog(Context context, int messageResId, DialogInterface.OnClickListener onPositive) {
            return new AlertDialog.Builder(context)
                    .setMessage(messageResId)
                    .setCancelable(false)
                    .setPositiveButton(R.string.Ok, onPositive)
                    .setNegativeButton(R.string.Cancel, (dialogInterface, i) -> {
                    }).show();
        }
    }

    public static class UriUtil {
        private static final String LOG_TAG = "UriUtil";

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
                StreamUtil.bufferedStreamCopy(is, os);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Writing to uri failed: " + uri, e);
            }
        }
    }

    public static class StreamUtil {
        private static final int buffSize = 8192;
        private static final String LOG_TAG = "StreamUtil";

        /**
         * Only use this method if you can be sure the content of the input stream,
         * can be read, is a UTF-8 text and will fit in memory (as String).
         * There is a reason this is not a part of the standard libraries.
         *
         * @param is input stream to be read
         * @return content of the input stream or null if IO exception occurs
         */
        public static String readInputStreamAsUTF8String(@NonNull InputStream is) {
            try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder stringBuilder = new StringBuilder();
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
         * Copy input stream to output stream using a byte buffer (8kb by default)
         *
         * @param is source stream
         * @param os target stream
         * @throws IOException thrown on read/write error
         */
        public static void bufferedStreamCopy(@NonNull InputStream is, @NonNull OutputStream os) throws IOException {
            byte[] buffer = new byte[buffSize];

            int read;
            while ((read = is.read(buffer)) >= 0) {
                os.write(buffer, 0, read);
            }
        }
    }

    public static class FileUtil {
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

        public static void createFileWithSubdirectories(@NonNull File file) {
            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.mkdirs()) {
                    Log.e(LOG_TAG, "Creating parent directories failed for file: " + file.getAbsolutePath());
                }
                if (!file.createNewFile()) {
                    Log.e(LOG_TAG, "Failed to create file: " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error while creating file: " + file.getAbsolutePath(), e);
            }
        }

        public static void writeToFile(@NonNull File file, @NonNull InputStream is) {
            try (OutputStream os = new FileOutputStream(file)) {
                StreamUtil.bufferedStreamCopy(is, os);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while writing to file: " + file.getAbsolutePath(), e);
            }
        }
    }
}
