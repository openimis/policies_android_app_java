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

package cm.ynote.educash;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

<<<<<<< HEAD:app/src/main/java/cm/ynote/educash/Global.java
=======
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.openimis.imispolicies.BuildConfig.APP_DIR;

>>>>>>> repo/main:app/src/main/java/org/openimis/imispolicies/Global.java
public class Global extends Application {
    private static Global GlobalContext;
    public static final String PREF_NAME = "CMPref";
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
    private List<String> ProtectedDirectories;

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
        ProtectedDirectories = Arrays.asList("Authentications", "Database", "Images");
        initSharedPrefsInts();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};
        } else {
            permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE};
        }

    }

    private void initSharedPrefsInts() {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        try {
            String text = getInputStreamText(getAssets().open("JSON/default_ints.json"));
            JSONObject intDefaults = new JSONObject(text);

            for (Iterator<String> it = intDefaults.keys(); it.hasNext(); ) {
                String key = it.next();

                if (!sp.contains(key)) {
                    editor.putInt(key, intDefaults.getInt(key));
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

    public void changeLanguage(Context ctx, String Language) {
        Resources res = ctx.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        config.locale = new Locale(Language.toLowerCase());
        res.updateConfiguration(config, dm);
    }

    private String createOrCheckDirectory(String path) {
        File dir = new File(path);

        if (dir.exists() || dir.mkdir()) {
            return path;
        } else {
            return "";
        }
    }

    public String getMainDirectory() {
        if (MainDirectory == null || "".equals(MainDirectory)) {
            String documentsDir = createOrCheckDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString());
            MainDirectory = createOrCheckDirectory(documentsDir + File.separator + APP_DIR);

            if ("".equals(documentsDir) || "".equals(MainDirectory)) {
                Log.w(FILE_IO_LOG_TAG, "Main directory could not be created");
            }
        }
        return MainDirectory;
    }

    public String getAppDirectory() {
        if (AppDirectory == null || "".equals(AppDirectory)) {
            AppDirectory = createOrCheckDirectory(getApplicationInfo().dataDir);

            if ("".equals(AppDirectory)) {
                Log.w(FILE_IO_LOG_TAG, "App directory could not be created");
            }
        }
        return AppDirectory;
    }

    public String getSubdirectory(String subdirectory) {
        if (!SubDirectories.containsKey(subdirectory) || "".equals(SubDirectories.get(subdirectory))) {
            String directory;

            if (ProtectedDirectories.contains(subdirectory)) {
                directory = getAppDirectory();
            } else {
                directory = getMainDirectory();
            }

            String subDirPath = createOrCheckDirectory(directory + File.separator + subdirectory);

            if ("".equals(subDirPath)) {
                Log.w(FILE_IO_LOG_TAG, String.format("%s directory could not be created", subdirectory));
                return null;
            } else {
                SubDirectories.put(subdirectory, subDirPath);
            }
        }
        return SubDirectories.get(subdirectory);
    }

    public String getFileText(String path) {
        String result = "";
        try {
            InputStream inputStream = new FileInputStream(path);
            result = getInputStreamText(inputStream);
        } catch (IOException e) {
            Log.e(FILE_IO_LOG_TAG, String.format("Creating input stream for path: %s failed", path), e);
        }
        return result;
    }

    public String getInputStreamText(InputStream inputStream) {
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
            Log.e(FILE_IO_LOG_TAG, "Reading text from input stream failed", e);
        }
        return stringBuilder.toString();
    }

    public int getIntSetting(String key, int defaultValue) {
        try {
            return getSharedPreferences(PREF_NAME, MODE_PRIVATE).getInt(key, defaultValue);
        } catch (ClassCastException e) {
            Log.e(PREF_LOG_TAG, String.format("%s key is not an int", key), e);
        }
        return defaultValue;
    }

    public String getStringSetting(String key, String defaultValue) {
        try {
            return getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(key, defaultValue);
        } catch (ClassCastException e) {
            Log.e(PREF_LOG_TAG, String.format("%s key is not a string", key), e);
        }
        return defaultValue;
    }
}
