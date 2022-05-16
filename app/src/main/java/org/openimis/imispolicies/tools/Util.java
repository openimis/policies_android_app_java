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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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

        /**
         * Copy byte content of the file to location described by the uri. The uri must be openable and
         * allow writing. The file must be openable and readable.
         *
         * @param file File to be read
         * @param uri  Uri pointing to writable location
         */
        public static void copyFileToUri(@NonNull Context context, @NonNull File file, @NonNull Uri uri) {
            try (InputStream is = new FileInputStream(file)) {
                Util.UriUtil.writeToUri(context, uri, is);
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
    }

    public static class StreamUtil {
        private static final int buffSize = 8192;
        private static final String LOG_TAG = "StreamUtil";

        /**
         * Only use this method if you can be sure the content of the input stream
         * can be read, is a UTF-8 text and will fit in memory (as String).
         *
         * @param is input stream to be read
         * @return content of the input stream or null if IO exception occurs
         */
        public static String readInputStreamAsUTF8String(@NonNull InputStream is) {
            try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder stringBuilder = null;
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    if(stringBuilder != null) {
                        stringBuilder.append("\n");
                        stringBuilder.append(inputStr);
                    } else {
                        stringBuilder = new StringBuilder(inputStr);
                    }
                }
                return stringBuilder != null? stringBuilder.toString(): null;
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
                if (!file.delete()) {
                    Log.w(LOG_TAG, "Delete file failed: " + file.getAbsolutePath());
                }
            }
        }

        public static void writeToFile(@NonNull File file, @NonNull InputStream is) {
            try (OutputStream os = new FileOutputStream(file)) {
                StreamUtil.bufferedStreamCopy(is, os);
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
                return StreamUtil.readInputStreamAsUTF8String(is);
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

        public static File zipDirectories(File outputFile, String zipPassword, File... directories) {
            ArrayList<File> filesToAdd = new ArrayList<>();
            for (File directory : directories) {
                if (!directory.exists() ||  !directory.isDirectory()) {
                    Log.w(LOG_TAG, "Provided file is not a directory: " + directory);
                    continue;
                }

                File[] files = directory.listFiles();
                if (files == null) {
                    Log.w(LOG_TAG, "Reading a directory filed: " + directory);
                    continue;
                }

                Collections.addAll(filesToAdd, files);
            }

            return Compressor.zip(filesToAdd, outputFile, zipPassword);
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
    }
}
