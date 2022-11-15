package org.openimis.imispolicies.util;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {
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
                    || StringUtils.isEmpty(object.getString(field), checkNullString);
        } catch (JSONException e) {
            return true;
        }
    }

    /**
     * @param object          Json object
     * @param field           field to be checked
     * @param defaultValue    default value to be used if requested value does not exists
     * @param checkNullString Should "null" string be considered empty
     * @return value of the specified field if exists, default value otherwise
     */
    public static String getStringOrDefault(@NonNull JSONObject object, @NonNull String field, String defaultValue, boolean checkNullString) {
        try {
            if (!isStringEmpty(object, field, checkNullString)) {
                return object.getString(field);
            } else {
                return defaultValue;
            }
        } catch (JSONException e) {
            return defaultValue;
        }
    }
}
