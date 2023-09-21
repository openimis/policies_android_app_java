package org.openimis.imispolicies.util;

import android.os.Build;
import android.os.LocaleList;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocaleUtils {
    /**
     * @param locale Locale to parse
     * @return language tag for provided Locale object
     */
    @NonNull
    public static String getLanguageTag(@NonNull Locale locale) {
        String languageTag;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (language.contains("-") || StringUtils.isEmpty(country)) {
            languageTag = language;
        } else {
            languageTag = String.format("%s-%s", language, country);
        }
        return languageTag;
    }

    /**
     * Parse given language tag to Locale object. This method will parse "en" to Locale("en") and "en-US"
     * into Locale("en", "US"). Locale("en-US") in an incorrect declaration.
     *
     * @param languageTag language tag to parse
     * @return Locale object for given language tag
     */
    @NonNull
    public static Locale getLocaleFromTag(@NonNull String languageTag) {
        String[] segments = languageTag.split("-");
        if (segments.length == 1) {
            return new Locale(segments[0]);
        } else {
            return new Locale(segments[0], segments[1]);
        }
    }

    /**
     * This method helps convert LocaleList object into java collection of Locale objects. This allows to use
     * generic collection tools to be used with the LocaleList object.
     *
     * @param localeList System LocaleList object
     * @return List of Locale objects accessible in LocaleList object
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    public static List<Locale> toList(@NonNull LocaleList localeList) {
        List<Locale> result = new ArrayList<>(localeList.size());
        for (int i = 0; i < localeList.size(); i++) {
            result.add(localeList.get(i));
        }
        return result;
    }
}
