package org.openimis.imispolicies.util;

import java.util.Locale;

public class LocaleUtil {
    public static String getLanguageTag(Locale locale) {
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

    public static Locale getLocaleFromTag(String languageTag) {
        String[] segments = languageTag.split("-");
        if (segments.length == 1) {
            return new Locale(segments[0]);
        } else {
            return new Locale(segments[0], segments[1]);
        }
    }
}
