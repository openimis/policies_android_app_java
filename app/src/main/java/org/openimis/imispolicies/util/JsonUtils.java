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
}
