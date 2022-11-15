package org.openimis.imispolicies.util;

import android.text.TextUtils;

import java.util.List;
import java.util.function.Function;

public class StringUtils {
    public interface StringAccessor<T> {
        /**
         * Return any string accessed from object. That can any String field, any String representation of
         * a field or toString() of a whole object
         *
         * @return string accessed from object of type T
         */
        <t> String apply(T object);
    }

    /**
     * @param string String to be checked
     * @return is string null or empty
     */
    public static boolean isEmpty(CharSequence string) {
        return isEmpty(string, false);
    }

    /**
     * @param string          String to be checked
     * @param checkNullString Should "null" string be considered empty (case insensitive)
     * @return is string null or empty
     */
    public static boolean isEmpty(CharSequence string, boolean checkNullString) {
        return string == null
                || string.equals("")
                || (string.toString().equalsIgnoreCase("null") && checkNullString);
    }

    /**
     * Null-safe equality check (true if both are null)
     */
    public static boolean equals(CharSequence s1, CharSequence s2) {
        return TextUtils.equals(s1, s2);
    }

    public static <T> String join(String delimiter, Iterable<T> collection, StringAccessor<T> accessor) {
        StringBuilder builder = new StringBuilder();
        for (T item : collection) {
            if (builder.length() == 0) {
                builder.append(accessor.apply(item));
            } else {
                builder.append(delimiter).append(accessor.apply(item));
            }
        }
        return builder.toString();
    }
}
