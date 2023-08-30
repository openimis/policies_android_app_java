package org.openimis.imispolicies.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

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

    @Nullable
    public static String getStringOrDefault(@NonNull JSONObject object, @NonNull String field) {
        return getStringOrDefault(object, field, null, true);
    }

    /**
     * @param object          Json object
     * @param field           field to be checked
     * @param defaultValue    default value to be used if requested value does not exists
     * @param checkNullString Should "null" string be considered empty
     * @return value of the specified field if exists, default value otherwise
     */
    @Nullable
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

    @Nullable
    public static Date getDateOrDefault(@NonNull JSONObject object, @NonNull String field) {
        return getDateOrDefault(object, field, null);
    }

    @Nullable
    public static Date getDateOrDefault(@NonNull JSONObject object, @NonNull String field, @Nullable Date defaultValue) {
        return getDateOrDefault(object, field, defaultValue, true);
    }

    @Nullable
    public static Date getDateOrDefault(@NonNull JSONObject object, @NonNull String field, @Nullable Date defaultValue, boolean checkNullString) {
        String string = getStringOrDefault(object, field, null, checkNullString);
        if (string == null) {
            return defaultValue;
        }
        try {
            return DateUtils.dateFromString(string);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    @Nullable
    public static Integer getIntegerOrDefault(@NonNull JSONObject object, @NonNull String field) {
        return getIntegerOrDefault(object, field, null, true);
    }

    @Nullable
    public static Integer getIntegerOrDefault(@NonNull JSONObject object, @NonNull String field, Integer defaultValue) {
        return getIntegerOrDefault(object, field, defaultValue, true);
    }

    @Nullable
    public static Integer getIntegerOrDefault(@NonNull JSONObject object, @NonNull String field, Integer defaultValue, boolean checkNullString) {
        try {
            if (!isStringEmpty(object, field, checkNullString)) {
                return Integer.parseInt(object.getString(field));
            } else {
                return defaultValue;
            }
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static boolean getBooleanOrDefault(@NonNull JSONObject object, @NonNull String field, boolean defaultValue) {
        Boolean value = getBooleanOrDefault(object, field, null);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    @Nullable
    public static Boolean getBooleanOrDefault(@NonNull JSONObject object, @NonNull String field) {
        return getBooleanOrDefault(object, field, null, true);
    }

    @Nullable
    public static Boolean getBooleanOrDefault(@NonNull JSONObject object, @NonNull String field, Boolean defaultValue) {
        return getBooleanOrDefault(object, field, defaultValue, true);
    }

    @Nullable
    public static Boolean getBooleanOrDefault(@NonNull JSONObject object, @NonNull String field, Boolean defaultValue, boolean checkNullString) {
        try {
            if (!isStringEmpty(object, field, checkNullString)) {
                String value = object.getString(field);
                return "true".equalsIgnoreCase(value) || "1".equals(value);
            } else {
                return defaultValue;
            }
        } catch (JSONException e) {
            return defaultValue;
        }
    }
}
