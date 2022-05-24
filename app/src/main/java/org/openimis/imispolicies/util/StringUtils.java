package org.openimis.imispolicies.util;

import android.text.TextUtils;

public class StringUtils {
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
}
