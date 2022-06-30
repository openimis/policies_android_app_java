package org.openimis.imispolicies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
}
