package org.openimis.imispolicies;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Util {
    public static class StringUtil {
        /**
         * @param string String to be checked
         * @return is string null or empty
         */
        public static boolean isEmpty(String string) {
            return isEmpty(string, false);
        }

        /**
         * @param string          String to be checked
         * @param checkNullString Should "null" string be considered empty
         * @return is string null or empty
         */
        public static boolean isEmpty(String string, boolean checkNullString) {
            return string == null
                    || string.equals("")
                    || (string.equals("null") && checkNullString);
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
}