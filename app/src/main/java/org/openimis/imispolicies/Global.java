//Copyright (c) 2016-%CurrentYear% Swiss Agency for Development and Cooperation (SDC)
//
//The program users must agree to the following terms:
//
//Copyright notices
//This program is free software: you can redistribute it and/or modify it under the terms of the GNU AGPL v3 License as published by the 
//Free Software Foundation, version 3 of the License.
//This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU AGPL v3 License for more details www.gnu.org.
//
//Disclaimer of Warranty
//There is no warranty for the program, to the extent permitted by applicable law; except when otherwise stated in writing the copyright 
//holders and/or other parties provide the program "as is" without warranty of any kind, either expressed or implied, including, but not 
//limited to, the implied warranties of merchantability and fitness for a particular purpose. The entire risk as to the quality and 
//performance of the program is with you. Should the program prove defective, you assume the cost of all necessary servicing, repair or correction.
//
//Limitation of Liability 
//In no event unless required by applicable law or agreed to in writing will any copyright holder, or any other party who modifies and/or 
//conveys the program as permitted above, be liable to you for damages, including any general, special, incidental or consequential damages 
//arising out of the use or inability to use the program (including but not limited to loss of data or data being rendered inaccurate or losses 
//sustained by you or third parties or a failure of the program to operate with any other programs), even if such holder or other party has been 
//advised of the possibility of such damages.
//
//In case of dispute arising out or in relation to the use of the program, it is subject to the public law of Switzerland. The place of jurisdiction is Berne.

package org.openimis.imispolicies;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import org.openimis.imispolicies.tools.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.util.LocaleUtil;
import org.openimis.imispolicies.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Global extends Application {
    private static Global GlobalContext;
    public static final String PREF_NAME = "CMPref";
    public static final String PREF_LANGUAGE_KEY = "Language";
    public static final String PREF_LOG_TAG = "PREFS";
    public static final String FILE_IO_LOG_TAG = "FILEIO";

    private String OfficerCode;
    private String OfficerName;
    private int UserId;
    private int OfficerId;
    private String[] permissions;

    private String ImageFolder;

    private Token JWTToken;

    private String MainDirectory;
    private String AppDirectory;
    private Map<String, String> SubDirectories;

    public static Global getGlobal() {
        return GlobalContext;
    }

    public static Context getContext() {
        return GlobalContext.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalContext = this;
        SubDirectories = new HashMap<>();
        initSharedPrefsInts();

        permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE};
    }

    private void initSharedPrefsInts() {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        try {
            String text = StreamUtils.readInputStreamAsUTF8String(getAssets().open("JSON/default_ints.json"));

            if (text != null) {
                JSONObject intDefaults = new JSONObject(text);

                for (Iterator<String> it = intDefaults.keys(); it.hasNext(); ) {
                    String key = it.next();

                    if (!sp.contains(key)) {
                        editor.putInt(key, intDefaults.getInt(key));
                    }
                }
            }
        } catch (IOException e) {
            Log.e(FILE_IO_LOG_TAG, "Reading int defaults failed", e);
        } catch (JSONException e) {
            Log.e(FILE_IO_LOG_TAG, "Parsing int defaults failed", e);
        } finally {
            editor.apply();
        }
    }

    public Token getJWTToken() {
        if (JWTToken == null)
            JWTToken = new Token();
        return JWTToken;
    }

    public void setJWTToken(Token token) {
        JWTToken = token;
    }

    public boolean isLoggedIn() {
        return getJWTToken().isTokenValidJWT();
    }

    public String getOfficerCode() {
        return OfficerCode;
    }

    public void setOfficerCode(String officerCode) {
        OfficerCode = officerCode;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public int getOfficerId() {
        return OfficerId;
    }

    public void setOfficerId(int officerId) {
        OfficerId = officerId;
    }

    public String getImageFolder() {
        return ImageFolder;
    }

    public void setImageFolder(String imageFolder) {
        ImageFolder = imageFolder;
    }

    private String CurrentUrl;

    public String getCurrentUrl() {
        return CurrentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        CurrentUrl = currentUrl;
    }

    public String getOfficerName() {
        return OfficerName;
    }

    public void setOfficerName(String officerName) {
        OfficerName = officerName;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return (ni != null && ni.isConnected());
    }

    public int isSDCardAvailable() {
        String State = Environment.getExternalStorageState();
        if (State.equals("mounted_ro")) {
            return 0;
        } else {
            return !State.equals("mounted") ? -1 : 1;
        }
    }

    public boolean isCurrentLanguage(@NonNull String language) {
        String currentLanguage = LocaleUtil.getLanguageTag(Locale.getDefault()).toLowerCase(Locale.ROOT);
        return language.toLowerCase(Locale.ROOT).equals(currentLanguage);
    }

    public void setLanguage(@NonNull Context context, @NonNull String language) {
        String newLanguage = language.toLowerCase(Locale.ROOT);
        if(!isCurrentLanguage(language)) {
            changeLanguage(context, newLanguage);
            if (context instanceof Activity) {
                ((Activity) context).recreate();
            }
        }
    }

    private void changeLanguage(@NonNull Context context, @NonNull String newLanguage) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = LocaleUtil.getLocaleFromTag(newLanguage);
        resources.updateConfiguration(config, displayMetrics);
        Locale.setDefault(config.locale);
        onConfigurationChanged(config);
        setStoredLanguage(newLanguage);
    }


    public void setStoredLanguage(String locale) {
        setStringKey(PREF_LANGUAGE_KEY, locale);
    }

    public String getStoredLanguage() {
        String defaultLanguage = new SQLHandler(this).getDefaultLanguage();
        return getStringKey(PREF_LANGUAGE_KEY, defaultLanguage);
    }

    private String createOrCheckDirectory(String path) {
        File dir = new File(path);

        if (dir.exists() || dir.mkdir()) {
            return path;
        } else {
            return "";
        }
    }

    public String getAppDirectory() {
        if (AppDirectory == null || "".equals(AppDirectory)) {
            AppDirectory = createOrCheckDirectory(getApplicationInfo().dataDir + File.separator);

            if ("".equals(AppDirectory)) {
                Log.w(FILE_IO_LOG_TAG, "App directory could not be created");
            }
        }
        return AppDirectory;
    }

    public String getSubdirectory(String subdirectory) {
        if (!SubDirectories.containsKey(subdirectory) || "".equals(SubDirectories.get(subdirectory))) {
            String directory = getAppDirectory();
            String subDirPath = createOrCheckDirectory(directory + subdirectory + File.separator);

            if ("".equals(subDirPath)) {
                Log.w(FILE_IO_LOG_TAG, String.format("%s directory could not be created", subdirectory));
                return null;
            } else {
                SubDirectories.put(subdirectory, subDirPath);
            }
        }
        return SubDirectories.get(subdirectory);
    }

    public int getIntKey(String key, int defaultValue) {
        try {
            return getSharedPreferences(PREF_NAME, MODE_PRIVATE).getInt(key, defaultValue);
        } catch (ClassCastException e) {
            Log.e(PREF_LOG_TAG, String.format("%s key is not an int", key), e);
        }
        return defaultValue;
    }

    public void setIntKey(String key, int value) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putInt(key, value).apply();
    }

    public long getLongKey(String key, long defaultValue) {
        try {
            return getSharedPreferences(PREF_NAME, MODE_PRIVATE).getLong(key, defaultValue);
        } catch (ClassCastException e) {
            Log.e(PREF_LOG_TAG, String.format("%s key is not a long", key), e);
        }
        return defaultValue;
    }

    public void setLongKey(String key, long value) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putLong(key, value).apply();
    }

    public String getStringKey(String key, String defaultValue) {
        try {
            return getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(key, defaultValue);
        } catch (ClassCastException e) {
            Log.e(PREF_LOG_TAG, String.format("%s key is not a string", key), e);
        }
        return defaultValue;
    }

    public void setStringKey(String key, String value) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putString(key, value).apply();
    }

    public LinkedHashMap<String, String> getHFLevels() {
        LinkedHashMap<String, String> levels = new LinkedHashMap<>();
        try {
            String text = StreamUtils.readInputStreamAsUTF8String(getAssets().open("JSON/hflevels.json"));
            JSONArray root = new JSONArray(text);

            for (int i = 0; i < root.length(); i++) {
                JSONObject item = root.getJSONObject(i);
                String stringName = item.getString("string");
                int stringId = getResources().getIdentifier(stringName, "string",
                        getPackageName());
                String str;
                if (stringId == 0) {
                    str = stringName;
                } else {
                    try {
                        str = getResources().getString(stringId);
                    } catch (Resources.NotFoundException e) {
                        Log.e(PREF_LOG_TAG, "Resource not found for HFLevels " + stringName
                                + "(" + stringId + ")");
                        str = stringName;
                    }
                }
                levels.put(item.getString("code"), str);
            }
        } catch (IOException e) {
            Log.e(FILE_IO_LOG_TAG, "Reading int defaults failed", e);
        } catch (JSONException e) {
            Log.e(FILE_IO_LOG_TAG, "Parsing int defaults failed", e);
        }
        return levels;
    }

    public void grantUriPermissions(Context context, Uri uri, Intent intent, int permissionFlags) {
        intent.addFlags(permissionFlags);
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, permissionFlags);
        }
    }

    public void sendFile(Context context, Uri uri, String mimeType) {
        Intent shareExportIntent = new Intent(Intent.ACTION_SEND);
        shareExportIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareExportIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareExportIntent.setType(mimeType);
        Intent chooserIntent = Intent.createChooser(shareExportIntent, null);
        grantUriPermissions(this, uri, chooserIntent, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(chooserIntent);
    }
}
