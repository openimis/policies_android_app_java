package org.openimis.imispolicies.tools;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.icu.text.LocaleDisplayNames;
import android.os.Build;
import android.os.LocaleList;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.util.DisplayMetrics;

import org.json.JSONArray;
import org.json.JSONException;
import org.openimis.imispolicies.BuildConfig;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.R;
import org.openimis.imispolicies.SQLHandler;
import org.openimis.imispolicies.util.AndroidUtils;
import org.openimis.imispolicies.util.LocaleUtils;
import org.openimis.imispolicies.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageManager {

    private static class LanguageContextWrapper extends ContextWrapper {
        public LanguageContextWrapper(Context base) {
            super(base);
        }
    }

    private static final String LOG_TAG = "LANGUAGEMANAGER";
    private final Global global;
    private final Context context;

    public LanguageManager(@NonNull Context context) {
        this.context = context;
        global = (Global) context.getApplicationContext();
    }

    /**
     * Check if current system language is supported as denoted in Master Data (tblLanguages)
     * If the current system language does not match to any of the supported languages then this method
     * will show a dialog telling the user what languages are supported
     */
    public void checkSystemLanguage() {
        List<Locale> supportedLocales = getSupportedLocales();
        if (!isSystemLanguageSupported(supportedLocales)) {
            showNotSupportedLanguagePrompt(supportedLocales);
        }
    }

    /**
     * Check if stored language is the current language. If not, then set stored language as current.
     */
    public void restoreLanguage() {
        restoreLanguage(true);
    }

    /**
     * Check if stored language is the current language. If not, then set stored language as current.
     *
     * @param withRestart should the current context be restarted (if it's Activity).
     */
    public void restoreLanguage(boolean withRestart) {
        String language = getStoredLanguage();
        setLanguage(language, withRestart);
    }

    /**
     * Set specified language as stored and current language.
     *
     * @param language    The language tag to be used.
     * @param withRestart should the current context be restarted (if it's Activity).
     */
    public void setLanguage(@NonNull String language, boolean withRestart) {
        if (!isCurrentLanguage(language)) {
            setStoredLanguage(language);
            setCurrentLanguage(language);
            if (withRestart && context instanceof Activity) {
                ((Activity) context).recreate();
            }
        }
    }

    /**
     * Set specified language as stored and current language.
     * The current context will be restarted (if it's Activity).
     *
     * @param language The language tag to be used.
     */
    public void setLanguage(@NonNull String language) {
        setLanguage(language, true);
    }

    public ContextWrapper getWrappedContext() {
        restoreLanguage(false);
        return new LanguageContextWrapper(context);
    }

    /**
     * Opens system language settings. This is a preferred method to change language
     */
    public void openLanguageSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings", "com.android.settings.LanguageSettings");
        context.startActivity(intent);
    }

    /**
     * Check if given language is current default language (as in Locale.getDefault()).
     *
     * @param language language tag to check
     * @return is the specified language tag current default
     */
    private boolean isCurrentLanguage(@NonNull String language) {
        String currentLanguage = LocaleUtils.getLanguageTag(getCurrentLocale());
        return language.toLowerCase(Locale.ROOT).equals(currentLanguage.toLowerCase());
    }

    /**
     * @return current system Locale object
     */
    private Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getSystemLocales().get(0);
        } else {
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    /**
     * @return LocaleList of all current system languages (sorted)
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private LocaleList getSystemLocales() {
        return context.getResources().getConfiguration().getLocales();
    }

    /**
     * Returns current resource language Locale object. The Locale is created from language tag stored
     * in "LanguageCode" resource key.
     *
     * @return current resource Locale object
     */
    private Locale getCurrentResourceLocale() {
        return LocaleUtils.getLocaleFromTag(context.getResources().getString(R.string.LanguageCode));
    }

    /**
     * Update locale of resources in current context to the selected language
     *
     * @param newLanguage language tag to use
     */
    private void setCurrentLanguage(@NonNull String newLanguage) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = LocaleUtils.getLocaleFromTag(newLanguage);
        resources.updateConfiguration(config, displayMetrics);
        Locale.setDefault(config.locale);
    }

    private void setStoredLanguage(String locale) {
        global.setStringKey(Global.PREF_LANGUAGE_KEY, locale);
    }

    private String getStoredLanguage() {
        String defaultLanguage = null;
        File database = global.getDatabasePath(SQLHandler.DBNAME);
        if (database.exists()) {
            defaultLanguage = new SQLHandler(context).getDefaultLanguage();
        } else {
            defaultLanguage = BuildConfig.DEFAULT_LANGUAGE_CODE;
        }
        return global.getStringKey(Global.PREF_LANGUAGE_KEY, defaultLanguage);
    }

    /**
     * @return List of supported languages from tblLanguages
     */
    private List<Locale> getSupportedLocales() {
        JSONArray languagesArray = new SQLHandler(context).getSupportedLanguages();
        List<Locale> supportedLocales = new ArrayList<>(languagesArray.length());

        try {
            for (int i = 0; i < languagesArray.length(); i++) {
                supportedLocales.add(LocaleUtils.getLocaleFromTag(languagesArray.getJSONObject(i).getString("LanguageCode")));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error while parsing supported languages", e);
        }

        return supportedLocales;
    }

    /**
     * This method check if system language (or any of the system languages on Android 7+) matches any of the supported languages
     * @param supportedLocales List of supported languages
     * @return does system language match any supported language
     */
    private boolean isSystemLanguageSupported(List<Locale> supportedLocales) {
        boolean isLocaleSupported = false;

        //System locale matches one of the supported locales if it is equal (language and country, or the supported locale is language only and system locale matches the language)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Android 7 new language matching system
            List<Locale> systemLocales = LocaleUtils.toList(getSystemLocales());
            isLocaleSupported = systemLocales.stream().anyMatch(systemLocale -> supportedLocales.stream().anyMatch(locale -> locale.getCountry().equals("") && locale.getLanguage().equals(systemLocale.getLanguage()) || systemLocale.equals(locale)));
        } else { // Legacy language matching
            Locale systemLocale = getCurrentLocale();
            for (Locale locale : supportedLocales) {
                if (locale.getCountry().equals("") && locale.getLanguage().equals(systemLocale.getLanguage()) || systemLocale.equals(locale)) {
                    isLocaleSupported = true;
                    break;
                }
            }
        }

        return isLocaleSupported;
    }

    private void showNotSupportedLanguagePrompt(List<Locale> supportedLocales) {
        String message = context.getResources().getString(R.string.SystemLanguageNotSupported);
        message += "\n\n" + context.getResources().getString(R.string.SupportedLanguages, StringUtils.join("\n", supportedLocales, this::getLanguageDisplayName));
        AndroidUtils.showDialog(context, message, context.getResources().getString(R.string.menu_settings), (d, i) -> openLanguageSettings());
    }

    private String getLanguageDisplayName(Locale locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return LocaleDisplayNames.getInstance(Locale.getDefault()).localeDisplayName(locale);
        } else {
            return locale.getDisplayName();
        }
    }
}
