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

import static android.provider.MediaStore.EXTRA_OUTPUT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.io.IOUtils;
import org.intellij.lang.annotations.Language;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.domain.entity.FeedbackRequest;
import org.openimis.imispolicies.domain.entity.PendingFeedback;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.exception.UserNotAuthenticatedException;
import org.openimis.imispolicies.tools.ImageManager;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.tools.StorageManager;
import org.openimis.imispolicies.usecase.DeletePolicyRenewal;
import org.openimis.imispolicies.usecase.FetchFamily;
import org.openimis.imispolicies.usecase.FetchMasterData;
import org.openimis.imispolicies.usecase.Login;
import org.openimis.imispolicies.usecase.PostFeedback;
import org.openimis.imispolicies.usecase.UpdateFamily;
import org.openimis.imispolicies.util.AndroidUtils;
import org.openimis.imispolicies.util.DateUtils;
import org.openimis.imispolicies.util.FileUtils;
import org.openimis.imispolicies.util.JsonUtils;
import org.openimis.imispolicies.util.StringUtils;
import org.openimis.imispolicies.util.UriUtils;
import org.openimis.imispolicies.util.ZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.HttpResponse;

public class ClientAndroidInterface {
    private static final String LOG_TAG_RENEWAL = "RENEWAL";
    public static String filePath = null;
    public static Uri tempPhotoUri = null;
    public static int RESULT_LOAD_IMG = 1;
    public static int RESULT_SCAN = 100;
    public static boolean inProgress = true;

    @NonNull
    private final Activity activity;
    @NonNull
    private final SQLHandler sqlHandler;
    @NonNull
    private final HashMap<String, String> controls = new HashMap<>();
    @NonNull
    private final ArrayList<String> myList = new ArrayList<>();
    @NonNull
    private final ArrayList<String> enrolMessages = new ArrayList<>();
    @NonNull
    private final Global global;
    @NonNull
    private final StorageManager storageManager;
    @NonNull
    private final Picasso picassoInstance;
    private int enrol_result;


    ClientAndroidInterface(@NonNull Activity activity) {
        this.activity = activity;
        global = (Global) activity.getApplicationContext();
        sqlHandler = new SQLHandler(activity);
        getControls();
        SQLiteDatabase database = sqlHandler.getReadableDatabase();
        filePath = database.getPath();
        storageManager = StorageManager.of(activity);
        picassoInstance = new Picasso.Builder(activity)
                .listener((picasso, path, exception) ->
                        Log.e("Images", String.format("Image load failed: %s", path.toString()), exception))
                .loggingEnabled(BuildConfig.LOGGING_ENABLED)
                .build();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void SetUrl(String Url) {
        global.setCurrentUrl(Url);
    }

    private void getControls() {
        String tableName = "tblControls";
        String[] columns = {"FieldName", "Adjustibility"};

        JSONArray ctls = sqlHandler.getResult(tableName, columns, null, null);

        for (int i = 0; i < ctls.length(); i++) {
            try {
                JSONObject object = ctls.getJSONObject(i);
                String FieldName = object.getString("FieldName");
                String Adjustibility = object.getString("Adjustibility");
                controls.put(FieldName, Adjustibility);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSpecificControl(String FieldName) {

        String tableName = "tblControls";
        String[] columns = {"Adjustibility"};
        String where = "FieldName = '" + FieldName + "'";
        String Adjustibility = "O";

        JSONArray ctls = sqlHandler.getResult(tableName, columns, where, null);

        for (int i = 0; i < ctls.length(); i++) {
            try {
                JSONObject object = ctls.getJSONObject(i);
                Adjustibility = object.getString("Adjustibility");
                controls.put(FieldName, Adjustibility);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Adjustibility;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getSpecificControlHtml(String FieldName) {
        String tableName = "tblControls";
        String[] columns = {"Adjustibility"};
        String where = "FieldName = '" + FieldName + "'";
        String Adjustibility = "O";

        JSONArray ctls = sqlHandler.getResult(tableName, columns, where, null);

        for (int i = 0; i < ctls.length(); i++) {
            try {
                JSONObject object = ctls.getJSONObject(i);
                Adjustibility = object.getString("Adjustibility");
                controls.put(FieldName, Adjustibility);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Adjustibility;
    }


    @JavascriptInterface
    @SuppressWarnings("unused")
    public String GetSystemImageFolder() {
        return global.getImageFolder();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getControl(String ctl) {
        if (controls.get(ctl) == null) {
            getControls();
        }
        return controls.get(ctl);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void ShowToast(String msg) {
        AndroidUtils.showToast(activity, msg);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void ShowDialog(String msg) {
        AndroidUtils.showDialog(activity, msg);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void ShowDialogYesNo(final int InsureId, final int FamilyId, final int isOffline) {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.ExceedThreshold)
                .setCancelable(false)
                .setNegativeButton(R.string.No, (dialog, which) -> {
                    SaveInsureePolicy(InsureId, FamilyId, false, isOffline);
                    try {
                        getFamilyPolicies(FamilyId);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                })
                .setPositiveButton(R.string.Yes, (dialogInterface, i) -> {
                    SaveInsureePolicy(InsureId, FamilyId, true, isOffline);
                    try {
                        getFamilyPolicies(FamilyId);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }).show();
    }

    @SuppressLint("DiscouragedApi")
    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getString(String str) {
        try {
            Resources resources = activity.getResources();
            return resources.getString(resources.getIdentifier(str, "string", activity.getPackageName()));
        } catch (Resources.NotFoundException e) {
            Log.e("RESOURCES", String.format("Resource \"%s\" not found", str), e);
        }
        return "";
    }

    @SuppressLint("DiscouragedApi")
    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getStringWithArgument(String str, String arg) {
        try {
            Resources resources = activity.getResources();
            return resources.getString(resources.getIdentifier(str, "string", activity.getPackageName()), arg);
        } catch (Resources.NotFoundException e) {
            Log.e("RESOURCES", String.format("Resource \"%s\" not found", str), e);
        }
        return "";
    }


    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean isValidInsuranceNumber(String InsuranceNumber) {
        Escape escape = new Escape();
        int validInsuranceNumber = escape.CheckInsuranceNumber(InsuranceNumber);
        if (validInsuranceNumber != 0) {
            ShowDialog(activity.getResources().getString(validInsuranceNumber));
            return false;
        }
        return true;
    }

    //get Region Without Officer
    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getRegionsWO() {
        @Language("SQL")
        String Query = "SELECT LocationId,LocationName FROM tblLocations WHERE LocationType = 'R'";
        return sqlHandler.getResult(Query, null).toString();
    }

    //get District Without Officer
    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getDistrictsWO(int RegionId) {
        @Language("SQL")
        String Query = "SELECT  LocationId,LocationName FROM tblLocations \n" +
                "WHERE LocationType = 'D' AND ParentLocationId = " + RegionId;
        return sqlHandler.getResult(Query, null).toString();
    }

    @Nullable
    private Integer getOfficerLocationId() {
        try {
            String officerCode = global.getOfficerCode();
            if (officerCode == null) {
                return null;
            }
            @Language("SQL")
            String query = "SELECT LocationId FROM tblOfficer WHERE Code = ?";
            JSONObject object = sqlHandler.getResult(query, new String[]{officerCode}).getJSONObject(0);
            if (!object.has("LocationId")) {
                return null;
            }
            return JsonUtils.getIntegerOrDefault(object, "LocationId");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getRegions() {
        Integer officerLocationId = getOfficerLocationId();
        @Language("SQL")
        String Query = "SELECT LocationId, LocationName FROM tblLocations WHERE LocationId = (SELECT L.ParentLocationId LocationId FROM tblLocations L";
        if (officerLocationId != null) {
            Query += " WHERE L.LocationId = " + officerLocationId;
        }
        Query += ")";
        return sqlHandler.getResult(Query, null).toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getDistricts(int RegionId) {
        Integer officerLocationId = getOfficerLocationId();
        @Language("SQL")
        String Query = "SELECT * FROM tblLocations L WHERE LocationType = 'D' AND ParentLocationId = " + RegionId;
        if (officerLocationId != null) {
            Query += " AND LocationId = " + officerLocationId;
        }
        return sqlHandler.getResult(Query, null).toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getDistricts() {
        String tableName = "tblLocations";
        String[] columns = {"LocationId", "LocationName"};
        String where = "LocationType = 'D'";

        return sqlHandler.getResult(tableName, columns, where, null).toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getWards(int DistrictId) {
        String tableName = "tblLocations";
        String[] columns = {"LocationId", "LocationName"};
        String where = "LocationType = 'W' AND ParentLocationId = " + DistrictId;

        return sqlHandler.getResult(tableName, columns, where, null).toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getVillages(int WardId) {
        String tableName = "tblLocations";
        String[] columns = {"LocationId", "LocationName"};
        String where = "LocationType = 'V' AND ParentLocationId = " + WardId;

        return sqlHandler.getResult(tableName, columns, where, null).toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getYesNo() {
        JSONArray YesNo = new JSONArray();
        try {
            JSONObject object = new JSONObject();
            object.put("key", activity.getResources().getString(R.string.Yes));
            object.put("value", 1);
            YesNo.put(object);

            object = new JSONObject();
            object.put("key", activity.getResources().getString(R.string.No));
            object.put("value", 2);
            YesNo.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return YesNo.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getConfirmationTypes() {
        String tableName = "tblConfirmationTypes";
        String[] columns = {"ConfirmationTypeCode", "ConfirmationType", "AltLanguage"};
        String OrderBy = "SortOrder";

        JSONArray ConfirmationTypes = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return ConfirmationTypes.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getGroupTypes() {
        String tableName = "tblFamilyTypes";
        String[] columns = {"FamilyTypeCode", "FamilyType", "AltLanguage"};
        String OrderBy = "SortOrder";

        JSONArray GroupTypes = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return GroupTypes.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getLanguagesOfSMS() {
        String tableName = "tblLanguages";
        String[] columns = {"LanguageCode", "LanguageName"};
        String OrderBy = "SortOrder";

        JSONArray GroupTypes = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return GroupTypes.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getVulnerability() {
        JSONArray selectJsonArray = new JSONArray();
        try {
            JSONObject object = new JSONObject();
            object.put("key", activity.getResources().getString(R.string.Yes));
            object.put("value", 1);
            selectJsonArray.put(object);

            object = new JSONObject();
            object.put("key", activity.getResources().getString(R.string.No));
            object.put("value", 0);
            selectJsonArray.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return selectJsonArray.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getApprovalOfSMS() {
        JSONArray selectJsonArray = new JSONArray();
        try {
            JSONObject object = new JSONObject();
            object.put("key", activity.getResources().getString(R.string.Yes));
            object.put("value", 1);
            selectJsonArray.put(object);

            object = new JSONObject();
            object.put("key", activity.getResources().getString(R.string.No));
            object.put("value", 0);
            selectJsonArray.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return selectJsonArray.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getGender() {
        String tableName = "tblGender";
        String[] columns = {"Code", "Gender", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Gender = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Gender.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getMaritalStatus() {
        JSONArray maritalStatus = new JSONArray();
        JSONObject object = new JSONObject();

        try {
            object.put("Code", "");
            object.put("Status", activity.getResources().getString(R.string.SelectMaritalStatus));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "M");
            object.put("Status", activity.getResources().getString(R.string.Married));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "S");
            object.put("Status", activity.getResources().getString(R.string.Single));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "D");
            object.put("Status", activity.getResources().getString(R.string.Divorced));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "W");
            object.put("Status", activity.getResources().getString(R.string.Widowed));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "N");
            object.put("Status", activity.getResources().getString(R.string.NotSpecified));
            maritalStatus.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return maritalStatus.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getProfessions() {
        String tableName = "tblProfessions";
        String[] columns = {"ProfessionId", "Profession", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Professions = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Professions.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getEducations() {
        String tableName = "tblEducations";
        String[] columns = {"EducationId", "Education", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Educations = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Educations.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getIdentificationTypes() {
        String tableName = "tblIdentificationTypes";
        String[] columns = {"IdentificationCode", "IdentificationTypes", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Educations = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Educations.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getRelationships() {
        String tableName = "tblRelations";
        String[] columns = {"RelationId", "Relation", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Relations = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Relations.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getHFLevels() {
        JSONArray jsonHFLevels = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        LinkedHashMap<String, String> hfLevels = global.getHFLevels();

        try {
            jsonObject.put("Code", "");
            jsonObject.put("HFLevel", activity.getResources().getString(R.string.SelectHFLevel));
            jsonHFLevels.put(jsonObject);
            for (String key : hfLevels.keySet()) {
                jsonObject = new JSONObject();
                jsonObject.put("Code", key);
                jsonObject.put("HFLevel", hfLevels.get(key));
                jsonHFLevels.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonHFLevels.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getHF(int DistrictId, String HFLevel) {
        JSONArray HFs;
        if (HFLevel != null) {
            @Language("SQL")
            String Query = "SELECT HFID,  HFCode ||\" : \"||  HFName HF FROM tblHF WHERE LocationId = ? AND HFLevel = ?";
            String[] args = {String.valueOf(DistrictId), HFLevel};

            HFs = sqlHandler.getResult(Query, args);
        } else {
            @Language("SQL")
            String Query = "SELECT HFID,  HFCode ||\" : \"||  HFName HF FROM tblHF WHERE LocationId = ?";
            String[] args = {String.valueOf(DistrictId)};

            HFs = sqlHandler.getResult(Query, args);
        }

        return HFs.toString();
    }

    private HashMap<String, String> jsonToTable(String jsonString) {
        HashMap<String, String> data = new HashMap<>();
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String ControlName = object.getString("id");
                String ControlValue;
                if (!"null".equals(object.getString("value"))) {
                    ControlValue = object.getString("value");
                } else {
                    ControlValue = null;
                }
                data.put(ControlName, ControlValue);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;

    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int SaveFamily(String FamilyData, String InsureeData) {

        int FamilyId = 0;
        int InsureeId = 0;

        try {
            int MaxFamilyId = getNextAvailableFamilyId();

            if (InsureeData.length() > 0) {
                int validation = isValidInsureeData(jsonToTable(InsureeData));
                if (validation > 0) {
                    throw new UserException(activity.getResources().getString(validation));
                }
            }

            //Insert Family
            //===============================================================================
            HashMap<String, String> data = jsonToTable(FamilyData);
            ContentValues values = new ContentValues();


            FamilyId = Integer.parseInt(data.get("hfFamilyId"));

            int LocationId = Integer.parseInt(data.get("ddlVillage"));

            Boolean Poverty = null;
            if (!TextUtils.isEmpty(data.get("ddlPovertyStatus"))) {
                Poverty = "1".equals(data.get("ddlPovertyStatus"));
            }

            String FamilyType = null;
            if (!TextUtils.isEmpty(data.get("ddlGroupType")) && !"0".equals(data.get("ddlGroupType")))
                FamilyType = data.get("ddlGroupType");

            String PermanentAddress = data.get("txtPermanentAddress");

            String Ethnicity = data.get("ddlEthnicity");

            String ConfirmationNo = data.get("txtConfirmationNo");
            String ConfirmationType = data.get("ddlConfirmationType");
            int isOffline = getFamilyStatus(FamilyId);

            values.put("LocationId", LocationId);
            values.put("Poverty", Poverty);
            values.put("FamilyType", FamilyType);
            values.put("FamilyAddress", PermanentAddress);
            values.put("Ethnicity", Ethnicity);
            values.put("ConfirmationNo", ConfirmationNo);
            values.put("ConfirmationType", ConfirmationType);

            if (FamilyId == 0) {
                values.put("isOffline", isOffline);
                values.put("FamilyId", MaxFamilyId);
                sqlHandler.insertData("tblFamilies", values);
                FamilyId = MaxFamilyId;
            } else {
                int Online = 2;
                if (isOffline == 0 || isOffline == 2) {
                    isOffline = 0;
                    values.put("isOffline", 2);
                }
                sqlHandler.updateData("tblFamilies", values, "FamilyId = ? AND (isOffline = ? OR isOffline = ?) ", new String[]{String.valueOf(FamilyId), String.valueOf(isOffline), String.valueOf(Online)}, false);
            }
            if (InsureeData.length() > 0) {
                //Insert Insuree
                //==========================================================================================
                InsureeId = SaveInsuree(InsureeData, FamilyId, 1, -1, 0);//herman new

                //Update insureeId in tblFamilies
                //==========================================================================================
                ContentValues cvUpdate = new ContentValues();
                cvUpdate.put("InsureeId", InsureeId);
                if (getFamilyStatus(FamilyId) == 1) {
                    cvUpdate.put("isOffline", 1);
                } else {
                    cvUpdate.put("isOffline", 2);
                }

                String[] whereArgs = {String.valueOf(FamilyId)};

                sqlHandler.updateData("tblFamilies", cvUpdate, "FamilyId= ?", whereArgs);
            }
            addOrUpdateFamilySmsFromDll(FamilyId, data);

            return FamilyId;

        } catch (UserException e) {
            e.printStackTrace();
            if (InsureeId != 0)
                sqlHandler.deleteData("tblInsuree", "InsureeId = ?", new String[]{String.valueOf(InsureeId)});
            if (FamilyId > 0 && InsureeData.length() > 0)
                sqlHandler.deleteData("tblFamilies", "FamilyId", new String[]{String.valueOf(FamilyId)});
            FamilyId = 0;
            ShowDialog(activity.getResources().getString(R.string.ErrorOccurred));

        } catch (Exception e) {
            e.printStackTrace();

            if (InsureeId != 0)
                sqlHandler.deleteData("tblInsuree", "InsureeId = ?", new String[]{String.valueOf(InsureeId)});

            if (FamilyId > 0 && InsureeData.length() > 0)
                sqlHandler.deleteData("tblFamilies", "FamilyId = ?", new String[]{String.valueOf(FamilyId)});

            FamilyId = 0;
            ShowDialog(e.getMessage());
        }

        return FamilyId;
    }

    private JSONObject getFamilySMS(String familyId) {
        // tblFamilySMS and tblFamily are in 1:1 relation, therefore only one record is returned
        @Language("SQL")
        String query = "SELECT * FROM tblFamilySMS where FamilyId = ? LIMIT 1;";
        String[] queryArgs = {familyId};
        try {
            JSONObject result = null;
            JSONArray array = sqlHandler.getResult(query, queryArgs);
            if (array.length() > 0) {
                result = array.getJSONObject(0);
            }
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject getFamilySMS(int familyId) throws JSONException {
        return getFamilySMS(String.valueOf(familyId));
    }

    private void addOrUpdateFamilySmsFromDll(int familyId, HashMap<String, String> familyFormData)
            throws UserException {
        Boolean approveSMS = "1".equals(familyFormData.get("ddlApprovalOfSMS"));
        String languageOfSMS =
                "".equals(familyFormData.get("ddlLanguageOfSMS")) ? null : familyFormData.get("ddlLanguageOfSMS");
        addOrUpdateFamilySms(familyId, approveSMS, languageOfSMS);
    }

    public void addOrUpdateFamilySms(int familyId, Boolean approve, String language) throws UserException {
        ContentValues familySmsValues = new ContentValues();
        familySmsValues.put("ApprovalOfSMS", approve);
        familySmsValues.put("LanguageOfSMS", language);
        familySmsValues.put("FamilyID", familyId);

        @Language("SQL")
        String query = "SELECT FamilyId FROM tblFamilySMS WHERE FamilyId = ?";
        String[] queryArgs = {String.valueOf(familyId)};
        if (sqlHandler.getResult(query, queryArgs).length() == 0) {
            sqlHandler.insertData("tblFamilySMS", familySmsValues);
        } else {
            sqlHandler.updateData("tblFamilySMS", familySmsValues,
                    "FamilyId= ?", queryArgs);
        }
    }

    private int isValidInsureeData(HashMap<String, String> data) {
        int Result;

        String InsuranceNumber = data.get("txtInsuranceNumber");
        String InsureeId = data.get("hfInsureeId");
        @Language("SQL")
        String Query = "SELECT InsureeId FROM tblInsuree WHERE Trim(CHFID) = ? AND InsureeId <> ?";
        String[] args = {InsuranceNumber, InsureeId};
        JSONArray returnData = sqlHandler.getResult(Query, args);
        if (returnData.length() > 0) {
            Result = R.string.InsuranceNumberExists;
        } else {
            Result = 0;
        }
        return Result;
    }


    @JavascriptInterface
    @SuppressWarnings("unused")
    public int SaveInsuree(String InsureeData, int FamilyId, int isHead, int ExceedThreshold, int PolicyId) throws Exception {
        inProgress = true;

        int InsureeId;
        int IsHeadSet;
        int isOffline;
        int insureeIsOffline;
        int MaxInsureeId;
        int rtInsureeId;
        try {
            HashMap<String, String> data = jsonToTable(InsureeData);

            int validation = isValidInsureeData(data);
            if (validation > 0) {
                ShowDialog(activity.getResources().getString(validation));
                return 7;
            }

            MaxInsureeId = getNextAvailableInsureeId();
            ContentValues values = new ContentValues();

            InsureeId = Integer.parseInt(data.get("hfInsureeId"));
            rtInsureeId = InsureeId;

            String s1 = data.get("hfisHead");
            if (Objects.equals(s1, "true") || Objects.equals(s1, "1")) IsHeadSet = 1;
            else if (s1.equals("false") || s1.equals("0")) IsHeadSet = 0;
            else IsHeadSet = Integer.parseInt(data.get("hfisHead"));

            String Marital = null;
            if (!Objects.equals(data.get("ddlMaritalStatus"), "") && data.get("ddlMaritalStatus") != null)
                Marital = data.get("ddlMaritalStatus");

            Boolean CardIssued = null;
            if (!TextUtils.isEmpty(data.get("ddlBeneficiaryCard"))) {
                if (data.get("ddlBeneficiaryCard").equals("1")) {
                    CardIssued = true;
                } else {
                    CardIssued = false;
                }
            }

            Integer Relation = null;
            if (!TextUtils.isEmpty(data.get("ddlRelationship")) && !data.get("ddlRelationship").equals("0"))
                Relation = Integer.valueOf(data.get("ddlRelationship"));

            Integer Profession = null;
            if (!TextUtils.isEmpty(data.get("ddlProfession")) && !data.get("ddlProfession").equals("0"))
                Profession = Integer.valueOf(data.get("ddlProfession"));

            Integer Education = null;
            if (!TextUtils.isEmpty(data.get("ddlEducation")) && !data.get("ddlEducation").equals("0"))
                Education = Integer.valueOf(data.get("ddlEducation"));

            String IdentificationType = "null";
            if (!TextUtils.isEmpty(data.get("ddlIdentificationType")) && !data.get("ddlIdentificationType").equals(""))
                IdentificationType = (data.get("ddlIdentificationType"));

            String PhotoPath = data.get("hfImagePath");
            String newPhotoPath = data.get("hfNewPhotoPath");

            if (!"".equals(newPhotoPath)) {
                PhotoPath = copyImageFromGalleryToApplication(newPhotoPath, data.get("txtInsuranceNumber"));
            }

            values.put("FamilyId", FamilyId);
            values.put("CHFID", data.get("txtInsuranceNumber"));
            values.put("LastName", data.get("txtLastName"));
            values.put("OtherNames", data.get("txtOtherNames"));
            values.put("DOB", data.get("txtBirthDate"));
            values.put("Gender", data.get("ddlGender"));
            values.put("Marital", Marital);
            if (IsHeadSet == -1) {
                values.put("isHead", isHead);
            } else {
                values.put("isHead", IsHeadSet);
                isHead = IsHeadSet;
            }
            isOffline = getFamilyStatus(FamilyId);
            insureeIsOffline = getInsureeStatus(InsureeId);
            //Integer.parseInt(data.get("hfIsOffline"));
            //isOffline = getInsureeStatus(InsureeId);
            values.put("IdentificationNumber", data.get("txtIdentificationNumber"));
            values.put("Phone", data.get("txtPhoneNumber"));
            if (isOffline == 0 || isOffline == 2)
                PhotoPath = PhotoPath.substring(PhotoPath.lastIndexOf("/") + 1);
            if (!"".equals(PhotoPath)) {
                values.put("PhotoPath", PhotoPath);
            }
            values.put("CardIssued", CardIssued);

            //values.put("isOffline", isOffline);
            values.put("Relationship", Relation);
            values.put("Profession", Profession);
            values.put("Education", Education);
            values.put("Email", data.get("txtEmail"));
            values.put("TypeOfId", IdentificationType);

            if (data.get("ddlVulnerability") != null && !data.get("ddlVulnerability").equals("")) {
                values.put("Vulnerability", data.get("ddlVulnerability"));
            } else {
                values.put("Vulnerability", data.get("0"));
            }

            if (data.get("ddlFSP") != null)
                values.put("HFID", Integer.valueOf(data.get("ddlFSP")));
            values.put("CurrentAddress", data.get("txtCurrentAddress"));
            values.put("GeoLocation", "");
            if (data.get("ddlCurrentVillage") != null)
                values.put("CurVillage", Integer.valueOf(data.get("ddlCurrentVillage")));
//            if(isOffline == 1 || isOffline)


            if (rtInsureeId == 0) {//New Insuaree
                values.put("isOffline", 1);
                if (isOffline == 0 || isOffline == 2) {
                    if (isOffline == 2) isOffline = 0;
                    if (global.isNetworkAvailable()) {
                        //if (isOffline == 0){
                        MaxInsureeId = -MaxInsureeId;
                        //}
                        values.put("InsureeId", MaxInsureeId);

                        sqlHandler.insertData("tblInsuree", values);
                        if (PolicyId > 0 && isHead == 0) {
                            getFamilyPolicies(FamilyId);
                        }
                        inProgress = false;
                        rtInsureeId = MaxInsureeId;
                        if (ExceedThreshold == 1) {
                            int InsId = -rtInsureeId;
                            ShowDialogYesNo(InsId, FamilyId, isOffline);
                        } else if (ExceedThreshold == 0) {
                            int InsId = -rtInsureeId;
                            SaveInsureePolicy(InsId, FamilyId, true, isOffline);
                        }
                    } else {
                        sqlHandler.insertData("tblInsuree", values);
                        if (PolicyId > 0 && isHead == 0) {
                            getFamilyPolicies(FamilyId);
                        }
                        rtInsureeId = MaxInsureeId;
                        if (ExceedThreshold == 1)
                            ShowDialogYesNo(rtInsureeId, FamilyId, isOffline);
                        else if (ExceedThreshold == 0)
                            SaveInsureePolicy(rtInsureeId, FamilyId, true, isOffline);
                    }
                } else {//New Family
                    values.put("InsureeId", MaxInsureeId);
                    sqlHandler.insertData("tblInsuree", values);
                    if (PolicyId > 0 && isHead == 0) {
                        getFamilyPolicies(FamilyId);
                    }
                    rtInsureeId = MaxInsureeId;
                    if (ExceedThreshold == 1)
                        ShowDialogYesNo(rtInsureeId, FamilyId, isOffline);
                    else if (ExceedThreshold == 0)
                        SaveInsureePolicy(rtInsureeId, FamilyId, true, isOffline);
                }

            } else {//Existing Insuree
                values.put("isOffline", insureeIsOffline);
                sqlHandler.updateData(
                        "tblInsuree", values, "InsureeId = ? AND (isOffline = ? OR isOffline = ?)",
                        new String[]{String.valueOf(InsureeId), String.valueOf(insureeIsOffline), insureeIsOffline == 1 ? "true" : "false"}
                );
            }
        } catch (NumberFormatException | UserException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return rtInsureeId;
    }

    private String copyImageFromGalleryToApplication(String selectedPath, String InsuranceNumber) {
        String result = "";

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            Calendar cal = Calendar.getInstance();
            String d = format.format(cal.getTime());

            File[] oldFiles = ImageManager.of(activity).getInsureeImages(InsuranceNumber);
            Runnable deleteOldFiles = () -> FileUtils.deleteFiles(oldFiles);

            String outputFileName = InsuranceNumber + "_" + global.getOfficerCode() + "_" + d + "_0_0.jpg";
            File outputFile = new File(global.getImageFolder(), outputFileName);

            if (!outputFile.createNewFile()) {
                Log.e("IMAGES", "Creating image copy failed");
            }

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            Target imageTarget = new OutputStreamImageTarget(outputStream, global.getIntKey("image_jpeg_quality", 40), deleteOldFiles);
            try {
                activity.runOnUiThread(() -> picassoInstance.load(selectedPath)
                        .resize(global.getIntKey("image_width_limit", 400),
                                global.getIntKey("image_height_limit", 400))
                        .centerInside()
                        .into(imageTarget));
            } catch (ClassCastException e) {
                Log.e("IMAGES", "copyImageFromGalleryToApplication can only be run in context of Activity");
            }
            result = outputFileName;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public AlertDialog deleteDialog(String msg, final int FamilyId) {
        return new AlertDialog.Builder(activity)
                .setTitle(R.string.TitleDelete)
                .setMessage(msg)
                .setIcon(R.drawable.ic_about)
                .setCancelable(false)
                .setPositiveButton(R.string.Yes, (dialogInterface, i) -> {
                    if (DeleteFamily(FamilyId) == 1) {
                        ShowDialog(activity.getResources().getString(R.string.FamilyDeleted));
                    }
                })
                .setNegativeButton(R.string.No, (dialog, which) -> {
                }).show();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void shutDownProgress() {
        inProgress = false;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int DeleteOnlineDataF(final int FamilyId) {
        int res = 0;
        if (DeleteFamily(FamilyId) == 1) {
            res = 1;
        }
        return res;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void informUser() {
        ShowDialog(activity.getResources().getString(R.string.DeleteFamilyOnlyOffline));
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getAllFamilies() {
        @Language("SQL")
        String Query = "SELECT F.FamilyId, I.CHFID, I.OtherNames ||\" \"||  I.LastName InsureeName, R.LocationName RegionName, D.LocationName DistrictName, W.LocationName WardName, V.LocationName VillageName, F.isOffline \n" +
                "FROM tblFamilies F\n" +
                "INNER JOIN tblInsuree I ON I.InsureeId = F.InsureeId\n" +
                "INNER JOIN tblLocations V ON V.LocationId = F.LocationId\n" +
                "INNER JOIN tblLocations W ON W.LocationId = V.ParentLocationId\n" +
                "INNER JOIN tblLocations D ON D.LocationId = W.ParentLocationId\n" +
                "INNER JOIN tblLocations R ON R.LocationId = D.ParentLocationId";

        JSONArray Families = sqlHandler.getResult(Query, null);

        return Families.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    //Get last segment value from url
    public String queryString(String url, String name) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter(name);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getInsuree(int InsureeId) {
        @Language("SQL")
        String Query = "SELECT InsureeId, FamilyId, CHFID, LastName, OtherNames, DOB, Gender, Marital, isHead, IdentificationNumber, Phone, isOffline , PhotoPath, CardIssued, Relationship, Profession, Education, Email, TypeOfId, I.HFID, CurrentAddress,R.LocationId CurRegion, D.LocationId CurDistrict, W.LocationId CurWard,  I.CurVillage, HFR.LocationId FSPRegion, HFD.LocationId FSPDistrict, HF.HFLevel FSPCategory, I.Vulnerability\n" +
                "FROM tblInsuree I\n" +
                "LEFT OUTER JOIN tblLocations V ON V.LocationId = I.CurVillage\n" +
                "LEFT OUTER JOIN tblLocations W ON W.LocationId = V.ParentLocationId\n" +
                "LEFT OUTER JOIN tblLocations D ON D.LocationId = W.ParentLocationId\n" +
                "LEFT OUTER JOIN tblLocations R ON R.LocationId = D.ParentLocationId\n" +
                "LEFT OUTER JOIN tblHF HF ON HF.HFID = I.HFID\n" +
                "LEFT OUTER JOIN tblLocations HFD ON HFD.LocationId = HF.LocationId\n" +
                "LEFT OUTER JOIN tblLocations HFR ON HFR.LocationId = HFD.ParentLocationId\n" +
                "WHERE I.InsureeId = ?";

        String[] args = {String.valueOf(InsureeId)};

        JSONArray Insuree = sqlHandler.getResult(Query, args);

        return Insuree.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getInsureesForFamily(int FamilyId) {
        @Language("SQL")
        String Query = "SELECT I.InsureeId, I.CHFID, I.Othernames ||\" \"|| I.LastName InsureeName, " +
                "CASE I.Gender WHEN 'M' THEN '" + activity.getResources().getString(R.string.Male) + "' WHEN 'F' THEN '" + activity.getResources().getString(R.string.Female) + "' ELSE '" + activity.getResources().getString(R.string.Other) + "' END Gender, " +
                "I.DOB , I.isHead, isOffline, I.Vulnerability FROM tblInsuree I WHERE FamilyId = ? ORDER BY I.isHead DESC, I.InsureeId ASC";
        String[] arg = {String.valueOf(FamilyId)};
        JSONArray Insurees = sqlHandler.getResult(Query, arg);
        return Insurees.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getFamilyHeader(int FamilyId) {
        @Language("SQL")
        String Query = "SELECT R.LocationName RegionName, R.LocationId RegionId,D.LocationId DistrictId,  D.LocationName DistrictName,  W.LocationName WardName,  V.LocationName VillageName, D.LocationId, isOffline FROM tblFamilies F \n" +
                "INNER JOIN tblLocations V ON V.LocationId = F.LocationId\n" +
                "INNER JOIN tblLocations W ON W.LocationId = V.ParentLocationId\n" +
                "INNER JOIN tblLocations D ON D.LocationId = W.ParentLocationId\n" +
                "INNER JOIN tblLocations R ON R.LocationId = D.ParentLocationId\n" +
                "WHERE F.FamilyID= ? ";
        String[] arg = {String.valueOf(FamilyId)};
        JSONArray HeadOfFamily = sqlHandler.getResult(Query, arg);
        return HeadOfFamily.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getFamily(int FamilyId) {
        @Language("SQL")
        String sSQL = "SELECT R.LocationId RegionId, D.LocationId DistrictId, W.LocationId WardId, V.LocationId VillageId, F.FamilyId, F.InsureeId, F.Poverty, F.isOffline, F.FamilyType, F.FamilyAddress, F.Ethnicity, F.ConfirmationNo, F.ConfirmationType, isOffline \n" +
                "FROM tblFamilies F\n" +
                "INNER JOIN tblLocations V ON V.LocationId= F.LocationId\n" +
                "INNER JOIN tblLocations W ON W.LocationId = V.ParentLocationId\n" +
                "INNER JOIN tblLocations D ON D.LocationId = W.ParentLocationId\n" +
                "INNER JOIN tblLocations R ON R.LocationId = D.ParentLocationId\n" +
                "WHERE F.FamilyId  = ?";

        String[] args = {String.valueOf(FamilyId)};
        JSONArray Family = sqlHandler.getResult(sSQL, args);
        try {
            JSONObject sms = getFamilySMS(FamilyId);
            if (sms != null) {
                Family.getJSONObject(0).put("ApprovalOfSMS", sms.get("ApprovalOfSMS"));
                Family.getJSONObject(0).put("LanguageOfSMS", sms.get("LanguageOfSMS"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Family.toString();
    }

    private String getYear(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy", Locale.US);
        return format.format(date);
    }

    private Date addYear(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, number);
        return calendar.getTime();
    }

    private Date addMonth(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, number);
        return calendar.getTime();
    }

    private Date addDay(Date date, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, number);
        return calendar.getTime();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getPolicyPeriod(int ProdId, String EnrollDate) throws ParseException, JSONException {

        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        Date dEnrollDate = format.parse(EnrollDate);

        @Language("SQL")
        String sSQL = "SELECT IFNULL(AdministrationPeriod, 0) AdministrationPeriod, StartCycle1, StartCycle2, StartCycle3, StartCycle4, InsurancePeriod, IFNULL(GracePeriod, 0)GracePeriod\n" +
                "FROM tblProduct\n" +
                "WHERE ProdId = ?";

        String[] args = {String.valueOf(ProdId)};

        JSONArray productDetails = sqlHandler.getResult(sSQL, args);

        JSONObject object = productDetails.getJSONObject(0);

        String EnrollYear = getYear(dEnrollDate);

        String startCycleFormat = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(startCycleFormat, Locale.US);
        Date StartCycle1 = null;
        Date StartCycle2 = null;
        Date StartCycle3 = null;
        Date StartCycle4 = null;
        int GracePeriod = Integer.parseInt(object.getString("GracePeriod"));
        boolean hasCycle;
        Date StartDate;
        Date ExpiryDate;
        int InsurancePeriod = Integer.parseInt(object.getString("InsurancePeriod"));

        Date dateWithGracePeriod1 = null;
        Date dateWithGracePeriod2 = null;
        Date dateWithGracePeriod3 = null;
        Date dateWithGracePeriod4 = null;

        if ((!TextUtils.isEmpty(object.getString("StartCycle1"))) && (!object.getString("StartCycle1").equals("null"))) {
            StartCycle1 = sdf.parse(object.getString("StartCycle1") + "-" + EnrollYear);
            dateWithGracePeriod1 = addMonth(StartCycle1, GracePeriod);
        }
        if ((!TextUtils.isEmpty(object.getString("StartCycle2"))) && (!object.getString("StartCycle2").equals("null"))) {
            StartCycle2 = sdf.parse(object.getString("StartCycle2") + "-" + EnrollYear);
            dateWithGracePeriod2 = addMonth(StartCycle2, GracePeriod);
        }
        if ((!TextUtils.isEmpty(object.getString("StartCycle3"))) && (!object.getString("StartCycle3").equals("null"))) {
            StartCycle3 = sdf.parse(object.getString("StartCycle3") + "-" + EnrollYear);
            dateWithGracePeriod3 = addMonth(StartCycle3, GracePeriod);
        }
        if ((!TextUtils.isEmpty(object.getString("StartCycle4"))) && (!object.getString("StartCycle4").equals("null"))) {
            StartCycle4 = sdf.parse(object.getString("StartCycle4") + "-" + EnrollYear);
            dateWithGracePeriod4 = addMonth(StartCycle4, GracePeriod);
        }

        if (StartCycle1 != null) {
            //They are using cycles
            hasCycle = true;
            if (dEnrollDate.compareTo(dateWithGracePeriod1) == 0 || dEnrollDate.before(dateWithGracePeriod1))
                StartDate = StartCycle1;
            else if (dateWithGracePeriod2 != null && (dEnrollDate.compareTo(dateWithGracePeriod2) == 0 || dEnrollDate.before(dateWithGracePeriod2)))
                StartDate = StartCycle2;
            else if (dateWithGracePeriod3 != null && (dEnrollDate.compareTo(dateWithGracePeriod3) == 0 || dEnrollDate.before(dateWithGracePeriod3)))
                StartDate = StartCycle3;
            else if (dateWithGracePeriod4 != null && (dEnrollDate.compareTo(dateWithGracePeriod4) == 0 || dEnrollDate.before(dateWithGracePeriod4)))
                StartDate = StartCycle4;
            else
                StartDate = addYear(StartCycle1, 1);
        } else {
            //They are not using cycles
            hasCycle = false;
            StartDate = dEnrollDate;
        }

        ExpiryDate = addDay(addMonth(StartDate, InsurancePeriod), -1);

        SimpleDateFormat ymd = AppInformation.DateTimeInfo.getDefaultDateFormatter();

        JSONArray period = new JSONArray();
        JSONObject o = new JSONObject();
        o.put("StartDate", ymd.format(StartDate));
        o.put("ExpiryDate", ymd.format(ExpiryDate));
        o.put("HasCycle", hasCycle);

        period.put(o);

        return period.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void selectPicture() {
        File tempPhotoFile = FileUtils.createTempFile(activity, "images/selectPictureTemp.jpeg");
        tempPhotoUri = UriUtils.createUriForFile(activity, tempPhotoFile);
        if (tempPhotoUri == null) return;

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        final List<Intent> cameraIntents = new ArrayList<>();
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(EXTRA_OUTPUT, tempPhotoUri);
        cameraIntents.add(cameraIntent);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        activity.startActivityForResult(chooserIntent, RESULT_LOAD_IMG);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public double getPolicyValue(String enrollDate, int ProductId, int FamilyId, String startDate, boolean HasCycle, int PolicyId, String PolicyStage, int IsOffline) throws JSONException {
        Date ExpiryDate = null;
        String expiryDate = null;
        int PreviousPolicyId = 0;
        Date PreviousExpiryDate;

        if (PolicyId > 0) {
            @Language("SQL")
            String PreviousPolicy = "SELECT  FamilyId,  ProdId,  PolicyStage,  EnrollDate, ExpiryDate, isOffline FROM tblPolicy WHERE PolicyId = " + PolicyId;
            JSONArray PrvPolicyArray = sqlHandler.getResult(PreviousPolicy, null);
            JSONObject object = PrvPolicyArray.getJSONObject(0);
            FamilyId = Integer.parseInt(object.getString("FamilyId"));
            ProductId = Integer.parseInt(object.getString("ProdId"));
            enrollDate = object.getString("EnrollDate");
            PolicyStage = object.getString("PolicyStage");
            expiryDate = object.getString("ExpiryDate");
            IsOffline = Integer.parseInt(object.getString("isOffline"));
        }


//        General general = new General();
//        if (general.isNetworkAvailable(mContext) && IsOffline == 0) {
//            CallSoap cs = new CallSoap();
//            cs.setFunctionName("getPolicyValue");
//            return cs.getPolicyValue(FamilyId, ProductId, PolicyId, PolicyStage, enrollDate, PolicyId);
//
//        }

//        SELECT TOP 1  @EnrollDate = EnrollDate, @ExpiryDate = ExpiryDate FROM tblPolicy WHERE PolicyID = @PolicyId

        @Language("SQL")
        String productDetailsQuery = "SELECT \n" +
                "CASE WHEN Lumpsum='null' OR Lumpsum = '' THEN 0 ELSE Lumpsum END Lumpsum,\n" +
                "CASE WHEN premiumAdult = 'null' OR premiumAdult = '' THEN 0 ELSE premiumAdult END premiumAdult,\n" +
                "CASE WHEN premiumchild='null' OR premiumchild = '' THEN 0 ELSE premiumchild END premiumchild ,\n" +
                "CASE WHEN RegistrationLumpsum='null' OR RegistrationLumpsum = '' THEN 0 ELSE RegistrationLumpsum END RegistrationLumpsum,\n" +
                "CASE WHEN RegistrationFee='null' OR RegistrationFee = '' THEN 0 ELSE RegistrationFee END RegistrationFee,\n" +
                "CASE WHEN GeneralAssemblyLumpSum= 'null' OR GeneralAssemblyLumpSum = '' THEN 0 ELSE  GeneralAssemblyLumpSum END GeneralAssemblyLumpSum ,\n" +
                "CASE WHEN GeneralAssemblyFee='null' OR GeneralAssemblyFee = '' THEN 0 ELSE  GeneralAssemblyFee END GeneralAssemblyFee,\n" +
                "CASE WHEN Threshold= 'null' OR Threshold = '' THEN 0 ELSE Threshold END Threshold, \n" +
                "CASE WHEN MemberCount =  'null' OR MemberCount = '' THEN 0 ELSE MemberCount END MemberCount,\n" +

                "CASE WHEN EnrolmentDiscountPeriod='null' OR EnrolmentDiscountPeriod = '' THEN 0 ELSE  EnrolmentDiscountPeriod END EnrolmentDiscountPeriod, \n" +
                "CASE WHEN EnrolmentDiscountPerc='null' OR EnrolmentDiscountPerc = '' THEN 0 ELSE  EnrolmentDiscountPerc END EnrolmentDiscountPerc, \n" +
                "CASE WHEN RenewalDiscountPeriod='null' OR RenewalDiscountPeriod = '' THEN 0 ELSE  RenewalDiscountPeriod END RenewalDiscountPeriod, \n" +
                "CASE WHEN RenewalDiscountPerc='null' OR RenewalDiscountPerc = '' THEN 0 ELSE  RenewalDiscountPerc END RenewalDiscountPerc \n" +

                "FROM tblProduct WHERE ProdId = ? ";
        String[] args = {String.valueOf(ProductId)};
        JSONArray productDetails = sqlHandler.getResult(productDetailsQuery, args);
        JSONObject object = productDetails.getJSONObject(0);
        double LumpSum = Double.parseDouble(object.getString("Lumpsum"));
        double PremiumAdult = Double.parseDouble(object.getString("premiumAdult"));
        double PremiumChild = Double.parseDouble(object.getString("premiumchild"));
        double RegistrationLumpSum = Double.parseDouble(object.getString("RegistrationLumpsum"));
        double RegistrationFee = Double.parseDouble(object.getString("RegistrationFee"));
        double GeneralAssemblyLumpSum = Double.parseDouble(object.getString("GeneralAssemblyLumpSum"));
        double Threshold = Double.parseDouble(object.getString("Threshold"));
        double GeneralAssemblyFee = Double.parseDouble(object.getString("GeneralAssemblyFee"));
        int MemberCount = Integer.parseInt((object.getString("MemberCount")));
        // int EnrolmentDiscountPeriod = Integer.parseInt(object.getString("EnrolmentDiscountPeriod"));

        //Added Values
        Date MinDiscountDateN;
        Date MinDiscountDateR;

        int DiscountPeriodR = Integer.parseInt(object.getString("RenewalDiscountPeriod"));
        double DiscountPercentR = Double.parseDouble(object.getString("RenewalDiscountPerc"));
        int DiscountPeriodN = Integer.parseInt(object.getString("EnrolmentDiscountPeriod"));
        double DiscountPercentN = Double.parseDouble(object.getString("EnrolmentDiscountPerc"));

        @Language("SQL")
        String AdultMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE (strftime('%Y', 'now') - strftime('%Y', DOB))  >= 18 AND IFNULL(Relationship,0) <> 7  AND FamilyID ='" + FamilyId + "' ORDER BY InsureeId ASC LIMIT " + MemberCount;

        JSONArray AdultMembersArray = sqlHandler.getResult(AdultMembersQuery, null);
        JSONObject AdultObject = AdultMembersArray.getJSONObject(0);
        int AdultMembers = Integer.parseInt(AdultObject.getString("count"));
        if (AdultMembers > MemberCount) AdultMembers = MemberCount;

        @Language("SQL")
        String ChildMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE (strftime('%Y', 'now') - strftime('%Y', DOB))  < 18 AND IFNULL(Relationship,0) <> 7  AND FamilyID = '" + FamilyId + "' ORDER BY InsureeId ASC LIMIT " + MemberCount;
        JSONArray ChildMembersArray = sqlHandler.getResult(ChildMembersQuery, null);
        JSONObject ChildObject = ChildMembersArray.getJSONObject(0);
        int ChildMembers = Integer.parseInt(ChildObject.getString("count"));

        if ((AdultMembers + ChildMembers) >= MemberCount) ChildMembers = MemberCount - AdultMembers;

        @Language("SQL")
        String OAdultMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE(strftime('%Y', 'now') - strftime('%Y', DOB))  >= 18 AND IFNULL(Relationship,0) = 7  AND FamilyID = '" + FamilyId + "' ORDER BY InsureeId ASC LIMIT " + MemberCount;
        JSONArray OAdultMembersArray = sqlHandler.getResult(OAdultMembersQuery, null);
        JSONObject OAdultObject = OAdultMembersArray.getJSONObject(0);
        int OAdultMembers = Integer.parseInt(OAdultObject.getString("count"));

        if ((AdultMembers + ChildMembers + OAdultMembers) >= MemberCount)
            OAdultMembers = MemberCount - (AdultMembers + ChildMembers);

        @Language("SQL")
        String OChildMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE(strftime('%Y', 'now') - strftime('%Y', DOB))  < 18 AND IFNULL(Relationship,0) = 7  AND FamilyID = '" + FamilyId + "'  ORDER BY InsureeId ASC LIMIT " + MemberCount;
        JSONArray OChildMembersArray = sqlHandler.getResult(OChildMembersQuery, null);
        JSONObject OChildObject = OChildMembersArray.getJSONObject(0);
        int OChildMembers = Integer.parseInt(OChildObject.getString("count"));
        if ((AdultMembers + ChildMembers + OAdultMembers + OChildMembers) >= MemberCount)
            OAdultMembers = MemberCount - (AdultMembers + ChildMembers + OAdultMembers);

        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        //Get   Previous Expiry Date
        if (PreviousPolicyId > 0) {
            @Language("SQL")
            String PED_Query = "SELECT ExpiryDate FROM tblPolicy WHERE  PolicyId =" + PreviousPolicyId;
            JSONArray PED_Array = sqlHandler.getResult(PED_Query, null);
            JSONObject PEDObject = PED_Array.getJSONObject(0);
            Date prvDate = null;
            try {
                prvDate = format.parse(PEDObject.getString("ExpiryDate"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            PreviousExpiryDate = addDay(prvDate, 1);
        }
//        Get extra members in family

        Date EnrollDate = null;
        Date StartDate = null;
        try {
            EnrollDate = format.parse(enrollDate);
            if (PolicyId > 0) ExpiryDate = format.parse(expiryDate);
            StartDate = format.parse(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        double PolicyValue = 0;
        int ExtraAdult = 0;
        int ExtraChild = 0;
        double Contribution;
        double GeneralAssembly;
        double Registration = 0;
        double AddonAdult;
        double AddonChild;
        Date MinDiscountDate;
        if (Threshold > 0 && AdultMembers > Threshold)
            ExtraAdult = (int) (AdultMembers - Threshold);

        if (Threshold > 0 && ChildMembers > (Threshold - AdultMembers + ExtraAdult))
            ExtraChild = (int) (ChildMembers - ((Threshold - AdultMembers + ExtraAdult)));


//        Get the Contribution
        if (LumpSum > 0)
            Contribution = LumpSum;
        else
            Contribution = (AdultMembers * PremiumAdult) + (ChildMembers * PremiumChild);

//        Get the Assembly
        if (GeneralAssemblyLumpSum > 0)
            GeneralAssembly = GeneralAssemblyLumpSum;
        else
            GeneralAssembly = (AdultMembers + ChildMembers + OAdultMembers + OChildMembers) * GeneralAssemblyFee;

        //Calculate If New
        if (PolicyStage.equalsIgnoreCase("N")) {
            if (RegistrationLumpSum > 0)
                Registration = RegistrationLumpSum;
            else
                Registration = (AdultMembers + ChildMembers + OAdultMembers + OChildMembers) * RegistrationFee;
        }


        /* Any member above the maximum member count  or with excluded relationship calculate the extra addon amount */

        AddonAdult = (ExtraAdult + OAdultMembers) * PremiumAdult;
        AddonChild = (ExtraChild + OChildMembers) * PremiumChild;
        Contribution += AddonAdult + AddonChild;
        PolicyValue = Contribution + GeneralAssembly + Registration;

        String PolicyPeriod = null;
        try {
            PolicyPeriod = getPolicyPeriod(ProductId, enrollDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = new JSONArray(PolicyPeriod);
        JSONObject jsnobject = jsonArray.getJSONObject(0);
        try {
            StartDate = format.parse(jsnobject.getString("StartDate"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (PolicyStage.equalsIgnoreCase("N")) {
            MinDiscountDateN = addMonth(StartDate, DiscountPeriodN);
            if (EnrollDate.before(MinDiscountDateN) && HasCycle) {
                PolicyValue -= (PolicyValue * 0.01 * DiscountPercentN);
            }
        } else if (PolicyStage.equalsIgnoreCase("R")) {
            PreviousExpiryDate = StartDate;
            MinDiscountDateR = addMonth(PreviousExpiryDate, DiscountPeriodR);///@),@);
            if (EnrollDate.before(MinDiscountDateR))
                PolicyValue -= (PolicyValue * 0.01 * DiscountPercentR);
        }


        return PolicyValue;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getOfficers(int LocationId, String EnrolmentDate) {
        @Language("SQL")
        String OfficerQuery = " SELECT OfficerId, Code  ||\" - \"|| Othernames  ||\" \"||  LastName  Code, LocationId FROM tblOfficer \n" +
                " WHERE LocationId=" + LocationId + " AND ('" + EnrolmentDate + "' <= WorksTo OR  IFNULL(WorksTo,0)=0 OR  IFNULL('" + EnrolmentDate + "',0) = 0) \n" +
                " ORDER BY OfficerId";
        JSONArray Oficers = sqlHandler.getResult(OfficerQuery, null);

        return Oficers.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getProducts(int RegionId, int DistrictId, String EnrolmentDate) {
        @Language("SQL")
        String ProductQuery = "SELECT  ProdId, ProductCode, ProductName, ProductCode ||\" - \"|| ProductName ProductNameCombined  \n" +
                "FROM tblProduct P\n" +
                "INNER JOIN  uvwLocations L ON (P.LocationId = L.LocationId) \n" +
                "WHERE  ((L.RegionId = " + RegionId + " OR L.RegionId ='null') AND (L.DistrictId =  " + DistrictId + " OR L.DistrictId ='null') OR L.LocationId='null') AND " +
                "( '" + EnrolmentDate + "'  BETWEEN P.DateFrom AND P.DateTo OR IFNULL(" + EnrolmentDate + ",0) = 0 )  \n" +
                "ORDER BY  L.LocationId DESC";

        JSONArray Products = sqlHandler.getResult(ProductQuery, null);
        return Products.toString();
    }

    public String getProductsRD() {
        JSONArray Products = null;
        int RegionId = 0, DistrictId = 0;
        try {
            String loc = getOfficerLocation();
            JSONArray locArray = new JSONArray(loc);
            if (locArray.length() != 0) {
                for (int i = 0; i < locArray.length(); i++) {
                    JSONObject obj = locArray.getJSONObject(i);
                    RegionId = JsonUtils.getIntegerOrDefault(obj, "RegionId", 0);
                    DistrictId = JsonUtils.getIntegerOrDefault(obj, "DistrictId", 0);
                }
            }
            @Language("SQL")
            String ProductQuery = "SELECT ProdId, ProductCode, ProductName \n" +
                    "FROM tblProduct P\n" +
                    "INNER JOIN  uvwLocations L ON (P.LocationId = L.LocationId) OR (P.LocationId = 'null' OR P.LocationId = '') \n" +
                    "WHERE  ((L.RegionId = " + RegionId + " OR L.RegionId ='null') AND (L.DistrictId =  " + DistrictId + " OR L.DistrictId ='null') OR L.LocationId='null') " +
                    "ORDER BY  L.LocationId DESC";

            Products = sqlHandler.getResult(ProductQuery, null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Products != null ? Products.toString() : null;
    }

    public String getProductsByDistrict(int districtId, String date) {
        int regionId = sqlHandler.getRegionId(districtId);
        return getProducts(regionId, districtId, date);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean IsBulkCNUsed() {
        return BuildConfig.SHOW_BULK_CN_MENU;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String GetNextBulkCn(String productId) {
        String productCode = sqlHandler.getProductCode(productId);
        if (productCode != null) {
            return sqlHandler.getNextFreeCn(global.getOfficerCode(), productCode);
        }
        return null;
    }

    public void deleteBulkCn(String controlNumber) {
        sqlHandler.deleteData("tblBulkControlNumbers", "ControlNumber = ?", new String[]{controlNumber});
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean isFetchedControlNumber(String controlNumber) {
        return sqlHandler.isFetchedControlNumber(controlNumber);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int SavePolicy(String PolicyData, int FamilyId, int PolicyId) throws Exception {
        inProgress = true;
        int MaxPolicyId;
        int rtPolicyId = PolicyId;
        int isOffline = 1;
        try {
            MaxPolicyId = getNextAvailablePolicyId();

            HashMap<String, String> data = jsonToTable(PolicyData);
            ContentValues values = new ContentValues();
            // isOffline = getFamilyStatus(FamilyId);

            values.put("FamilyId", FamilyId);
            values.put("EnrollDate", data.get("txtEnrolmentDate"));
            values.put("StartDate", data.get("txtStartDate"));
            values.put("EffectiveDate", data.get("txtEffectiveDate"));
            values.put("ExpiryDate", data.get("txtExpiryDate"));
            values.put("PolicyStatus", data.get("hfPolicyStatus"));
            values.put("PolicyValue", data.get("hfPolicyValue"));
            values.put("ProdId", data.get("ddlProduct"));
            values.put("OfficerId", data.get("ddlOfficer"));

            String controlNumber = data.get("AssignedControlNumber");
            values.put("isOffline", isOffline);

            values.put("PolicyStage", "N");
            if (rtPolicyId == 0) {
                values.put("PolicyId", MaxPolicyId);
                sqlHandler.insertData("tblPolicy", values);
                rtPolicyId = MaxPolicyId;
                InsertPolicyInsuree(rtPolicyId, 1);
                if (IsBulkCNUsed()) {
                    sqlHandler.assignCnToPolicy(rtPolicyId, controlNumber);
                }
                InsertRecordedPolicies("new", String.valueOf(FamilyId), data.get("ddlProduct"), data.get("hfPolicyValue"), MaxPolicyId);
            } else {
                int Online = 2;
                sqlHandler.updateData("tblPolicy", values, "PolicyId = ? AND (isOffline = ? OR isOffline = ?) ", new String[]{String.valueOf(PolicyId), String.valueOf(isOffline), String.valueOf(Online)});
                if (IsBulkCNUsed()) {
                    sqlHandler.clearCnAssignedToPolicy(PolicyId);
                    sqlHandler.assignCnToPolicy(PolicyId, controlNumber);
                }
            }
            inProgress = false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (UserException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        while (inProgress) {
        }
        return rtPolicyId;

    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getFamilyPolicies(int FamilyId) throws ParseException {
        //mimi ni add
        //getPolicyValue(String enrollDate, int ProductId, int FamilyId, String startDate, boolean HasCycle, int PolicyId, String PolicyStage, int IsOffline) throws JSONException {
        boolean isValueChanged = false;
        @Language("SQL")
        String QueryPolicyValue = "SELECT P.PolicyId,Pro.ProdId , EffectiveDate, PolicyValue, StartDate, ExpiryDate, EnrollDate,FamilyId,PolicyStage,IsOffline FROM tblPolicy P\n" +
                "INNER JOIN tblProduct Pro ON Pro.ProdId = P.ProdId\n" +
                "WHERE FamilyId = " + FamilyId;
        JSONArray PolicyValueArray = sqlHandler.getResult(QueryPolicyValue, null);
        JSONObject ValueObject = null;
        String enrollDate = null;
        int ProductId;
        String startDate;
        boolean HasCycle = false;
        int PolicyId;
        String PolicyStage;
        int IsOffline;
        String getCycle;
        String PolicyValue = null;
        Double NewPolicyValue = null;

        for (int i = 0; i < PolicyValueArray.length(); i++) {
            try {
                ValueObject = PolicyValueArray.getJSONObject(i);
                enrollDate = ValueObject.getString("EnrollDate");
                ProductId = ValueObject.getInt("ProdId");

                PolicyId = ValueObject.getInt("PolicyId");
                PolicyStage = ValueObject.getString("StartDate");
                startDate = ValueObject.getString("PolicyStage");
                IsOffline = ValueObject.getInt("isOffline");
                PolicyValue = ValueObject.getString("PolicyValue");

                getCycle = getPolicyPeriod(ProductId, enrollDate);
                JSONArray CycleArray = new JSONArray();
                //CycleArray.put(getCycle).getJSONArray(0);
                JSONArray newJArray = new JSONArray(getCycle);
                JSONObject o = null;
                o = newJArray.getJSONObject(0);
                startDate = o.getString("StartDate");
                HasCycle = o.getBoolean("HasCycle");
                //Cycle affect start date
                NewPolicyValue = getPolicyValue(enrollDate, ProductId, FamilyId, startDate, HasCycle, PolicyId, PolicyStage, IsOffline);
                Double doublePolicyValue = Double.valueOf(PolicyValue);
                Double doubleNewPolicyValue = Double.valueOf(NewPolicyValue);
                if (!doublePolicyValue.equals(doubleNewPolicyValue)) {
                    if (!isValueChanged) isValueChanged = true;

                    ContentValues values = new ContentValues();
                    values.put("PolicyValue", NewPolicyValue);
                    try {//Update to new policy value
                        sqlHandler.updateData("tblPolicy", values, "PolicyId = ?", new String[]{String.valueOf(PolicyId)});
                    } catch (UserException e) {
                        e.printStackTrace();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Language("SQL")
        String Query = "SELECT  P.PolicyId, Prod.ProductCode, ProductName, EffectiveDate, PolicyValue, StartDate, EnrollDate, bcn.ControlNumber, \n" +
                "   CASE    WHEN PolicyStatus = 1 THEN '" + activity.getResources().getString(R.string.Idle) + "'   " +
                "   WHEN PolicyStatus = 2 THEN '" + activity.getResources().getString(R.string.Active) + "'  " +
                "   WHEN PolicyStatus = 4 THEN '" + activity.getResources().getString(R.string.Suspended) + "'  " +
                "   WHEN PolicyStatus = 8 THEN '" + activity.getResources().getString(R.string.Expired) + "'  END  PolicyStatus, " +
                "   PolicyStatus PolicyStatusValue, P.ExpiryDate, isOffline FROM tblPolicy P \n" +
                "   INNER JOIN tblProduct Prod ON P.ProdId=Prod.ProdId  \n " +
                "   LEFT JOIN tblBulkControlNumbers bcn on P.PolicyId=bcn.PolicyId " +
                "   WHERE FamilyId = ?";

        String[] arg = {String.valueOf(FamilyId)};
        JSONArray Policies = sqlHandler.getResult(Query, arg, "");
        final boolean finalIsValueChanged = isValueChanged;
        final String finalEnrollDate = enrollDate;
        final Double finalNewPolicyValue = NewPolicyValue;
        final String finalPolicyValue = PolicyValue;
        if (finalIsValueChanged) {
            activity.runOnUiThread(() -> ShowDialog(activity.getResources().getString(R.string.PolicyValueChange) + finalEnrollDate + "," + "has been changed from " + finalPolicyValue + " to " + finalNewPolicyValue));
        }

        return Policies.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getPolicy(int PolicyId) {
        @Language("SQL")
        String Query = "SELECT  P.PolicyId, P.ProdId, OfficerId , Prod.ProductCode, ProductName, PolicyStage, EffectiveDate, IFNULL(PolicyValue,0) PolicyValue, StartDate, EnrollDate, bcn.ControlNumber, \n" +
                "   CASE    WHEN PolicyStatus = 1 THEN '" + activity.getResources().getString(R.string.Idle) + "'   " +
                "   WHEN PolicyStatus = 2 THEN '" + activity.getResources().getString(R.string.Active) + "'  " +
                "   WHEN PolicyStatus = 4 THEN '" + activity.getResources().getString(R.string.Suspended) + "'  " +
                "   WHEN PolicyStatus = 8 THEN '" + activity.getResources().getString(R.string.Expired) + "'  END  PolicyStatus, " +
                "   PolicyStatus  PolicyStatusValue, P.ExpiryDate, (IFNULL(PolicyValue,0) - IFNULL(Contribution,0)) Balance ,  IFNULL(Contribution,0) Contribution, P.isOffline  FROM tblPolicy P \n" +
                "   INNER JOIN tblProduct Prod ON P.ProdId=Prod.ProdId  \n " +
                "   LEFT JOIN (SELECT MAX(PolicyId) PolicyId, IFNULL(Sum(Amount),0) Contribution ,PremiumId " +
                "   FROM  tblPremium WHERE PolicyId = " + PolicyId + " AND isPhotoFee = 'false' ) " +
                "   Pre ON Pre.PolicyId=P.PolicyId \n " +
                "   LEFT JOIN tblBulkControlNumbers bcn on P.PolicyId=bcn.PolicyId " +
                "   WHERE P.PolicyId = ?";

        String[] arg = {String.valueOf(PolicyId)};
        JSONArray Policies = sqlHandler.getResult(Query, arg, "");
        return Policies.toString();
    }


    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getPolicyVal(String PolicyId) {
        int PolicId = Integer.parseInt(PolicyId);
        int policyval = 0;
        @Language("SQL")
        String Query = "SELECT PolicyValue FROM tblPolicy WHERE PolicyId = " + PolicId + "";
        JSONArray Policies = sqlHandler.getResult(Query, null);
        try {
            JSONObject JmaxPolicyOb = Policies.getJSONObject(0);
            policyval = JmaxPolicyOb.getInt("PolicyValue");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return policyval;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getSumPrem(String PolicyId) {
        int PolicId = Integer.parseInt(PolicyId);
        int totalPremium = 0;
        @Language("SQL")
        String Query = "SELECT SUM(Amount) FROM tblPremium WHERE PolicyId = " + PolicId + "";
        JSONArray Policies = sqlHandler.getResult(Query, null);
        try {
            JSONObject JmaxPolicyOb = Policies.getJSONObject(0);
            totalPremium = JmaxPolicyOb.getInt("SUM(Amount)");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return totalPremium;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int SavePremiums(String PremiumData, int PolicyId, int PremiumId, int FamilyId) throws Exception {
        inProgress = true;
        int MaxPremiumId = 0;
        int isOffline = 1;
        int rtPremiumId = PremiumId;
        //  String CHFID = "";
        String ReceiptNo = "";
        try {
            MaxPremiumId = getNextAvailablePremiumId();

            HashMap<String, String> data = jsonToTable(PremiumData);
            ReceiptNo = data.get("txtReceipt");

            ContentValues values = new ContentValues();
            values.put("PolicyId", PolicyId);
            values.put("Amount", data.get("txtAmount"));
            values.put("payerId", data.get("ddlPayer"));
            values.put("Receipt", ReceiptNo);
            values.put("PayDate", data.get("txtPayDate"));
            values.put("PayType", data.get("ddlPayType"));
            values.put("isOffline", isOffline);
            values.put("IsPhotoFee", data.get("ddlPhotoFee"));

            if (rtPremiumId == 0) {
                values.put("PremiumId", MaxPremiumId);
                sqlHandler.insertData("tblPremium", values);
                rtPremiumId = MaxPremiumId;
            } else {
                int Online = 2;
                sqlHandler.updateData("tblPremium", values, "PremiumId = ? AND (isOffline = ? OR isOffline = ?) ", new String[]{String.valueOf(PremiumId), String.valueOf(isOffline), String.valueOf(Online)});

            }
            inProgress = false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (UserException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        while (inProgress) {
        }
        return rtPremiumId;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getPayers(int RegionId, int DistrictId) {
        @Language("SQL")
        String Query = "SELECT PayerId, PayerName,P.LocationId FROM tblPayer P " +
                "LEFT OUTER JOIN tblLocations L ON P.LocationId = L.LocationId " +
                "WHERE L.LocationId = ? OR L.LocationId = ? OR L.LocationId = 'null' OR L.LocationId = '' OR L.LocationId = 0 " +
                "ORDER BY L.LocationId";

        return sqlHandler.getResult(Query, new String[]{String.valueOf(RegionId), String.valueOf(DistrictId)}).toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getPremiums(int PolicyId) {
        @Language("SQL")
        String Query = "SELECT PremiumId, PayerId, Amount, Receipt , PayDate, " +
                "CASE PayType WHEN 'C' THEN '" + activity.getResources().getString(R.string.Cash) + "' WHEN 'B' THEN '" + activity.getResources().getString(R.string.BankTransfer) + "' WHEN 'M' THEN '" + activity.getResources().getString(R.string.MobilePhone) + "' END PayType, " +
                "isOffline,IsPhotoFee \n" +
                "FROM tblPremium WHERE PolicyId=?";
        String[] arg = {String.valueOf(PolicyId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        return Premiums.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getPremium(int PremiumId) {
        @Language("SQL")
        String Query = "SELECT PremiumId, PayerId, Amount, Receipt , PayDate, PayType,isOffline,IsPhotoFee \n" +
                "FROM tblPremium WHERE PremiumId=?";
        String[] arg = {String.valueOf(PremiumId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        return Premiums.toString();
    }

    //This Query
    public JSONArray getRecordedPolicies(String insuranceNumber, String otherNames, String lastName, String insuranceProduct, String uploadedFrom, String uploadedTo, String radioRenewal, String radioRequested) {
        @Language("SQL")
        String renewal = "";
        @Language("SQL")
        String requested = "";
        @Language("SQL")
        String upload;
        if (!radioRenewal.equals("")) {
            renewal = " AND isDone == '" + radioRenewal + "'";
        }
        if (radioRequested.equals("N")) {
            requested = " AND ControlNumberId is null";
        }
        if (radioRequested.equals("Y")) {
            requested = " AND typeof(ControlNumberId) = 'integer'";
        }

        String today = AppInformation.DateTimeInfo.getDefaultDateFormatter().format(Calendar.getInstance().getTime());

        String dateFrom = getOrDefault(uploadedFrom, "0001-01-01");
        String dateTo = getOrDefault(uploadedTo, today);

        upload = " AND UploadedDate BETWEEN '" + dateFrom + "' AND '" + dateTo + "'";

        @Language("SQL")
        String Query = "SELECT * FROM tblRecordedPolicies WHERE InsuranceNumber LIKE '%" + insuranceNumber + "%' AND LastName LIKE '%" + lastName + "%' AND OtherNames LIKE '%" + otherNames + "%' AND ProductCode LIKE '%" + insuranceProduct + "%'" + renewal + "" + requested + "" + upload + "";
        return sqlHandler.getResult(Query, null);
    }

    public JSONArray getRecordedPolicies(String insuranceNumber, String otherNames, String lastName, String insuranceProduct, String uploadedFrom, String uploadedTo, String radioRenewal, String requestedFrom, String requestedTo, String PaymentType) {
        @Language("SQL") String renewal = "";
        String request;
        @Language("SQL")
        String upload;
        @Language("SQL")
        String payment_type = "";

        if (!radioRenewal.equals("")) {
            renewal = " AND RP.isDone == '" + radioRenewal + "'";
        }
        if (!PaymentType.equals("")) {
            payment_type = " AND CN.PaymentType == '" + PaymentType + "'";
        }

        String today = AppInformation.DateTimeInfo.getDefaultDateFormatter().format(Calendar.getInstance().getTime());
        String earlyDate = "0001-01-01";

        String dateUploadedFrom = getOrDefault(uploadedFrom, earlyDate);
        String dateUploadedTo = getOrDefault(uploadedTo, today);

        String dateRequestedFrom = getOrDefault(requestedFrom, earlyDate);
        String dateRequestedTo = getOrDefault(requestedTo, today);

        if ("".equals(uploadedFrom) && "".equals(uploadedTo)) {
            upload = "";
        } else {
            upload = " AND RP.UploadedDate BETWEEN '" + dateUploadedFrom + "' AND '" + dateUploadedTo + "'";
        }

        request = " AND RP.ControlRequestDate BETWEEN '" + dateRequestedFrom + "' AND '" + dateRequestedTo + "'";

        @Language("SQL")
        String Query = "SELECT * FROM tblRecordedPolicies RP INNER JOIN tblControlNumber CN ON RP.ControlNumberId = CN.Id WHERE RP.InsuranceNumber LIKE '%" + insuranceNumber + "%' AND RP.LastName LIKE '%" + lastName + "%' AND RP.OtherNames LIKE '%" + otherNames + "%' AND RP.ProductCode LIKE '%" + insuranceProduct + "%'" + renewal + " " + request + " " + upload + " " + payment_type + "";
        return sqlHandler.getResult(Query, null);
    }

    public String getOrDefault(String value, String defaultValue) {
        if (value == null || "".equals(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public JSONArray getRecordedPoliciesWithIdentifier(String InternalIdentifier) {
        String code = getCode(InternalIdentifier);
        @Language("SQL")
        String Query = "SELECT * FROM tblRecordedPolicies WHERE ControlNumberId = '" + code + "'";
        return sqlHandler.getResult(Query, null);
    }

    public String getCode(String InternalIdentifier) {
        String code = "";
        @Language("SQL")
        String Query = "SELECT * FROM tblControlNumber WHERE InternalIdentifier = '" + InternalIdentifier + "'";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        try {
            JSONObject JmaxIdOb = RecordedPolicies.getJSONObject(0);
            code = JmaxIdOb.getString("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return code;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean IsReceiptNumberUnique(String ReceiptNo, int FamilyId) {
        String CHFID = "";
        int isOffline = 0;
        int isHead = 1;
        boolean res = true;
        @Language("SQL")
        String CHFIDQUERY = "SELECT CHFID,isOffline FROM tblInsuree WHERE isHead = " + isHead + " AND FamilyId = " + FamilyId;

        JSONArray JsonCHFID = sqlHandler.getResult(CHFIDQUERY, null);
        try {
            JSONObject JsonCHFIDJSONObject = JsonCHFID.getJSONObject(0);
            CHFID = JsonCHFIDJSONObject.getString("CHFID");
            isOffline = JsonCHFIDJSONObject.getInt("isOffline");
        } catch (JSONException e) {
            e.printStackTrace();
        }

/*        String Query = "SELECT PremiumId, PayerId, Amount, Receipt , PayDate, PayType,IsOffline,isPhotoFee \n" +
                "FROM tblPremium WHERE Receipt=?";*/

        @Language("SQL")
        String Query = "SELECT * FROM tblPremium WHERE LOWER(Receipt)=?";
        String arg[] = {(ReceiptNo.toLowerCase()).trim()};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        Premiums.toString();
        int Count = Premiums.length();

        if (Count > 0) {
            res = false;
        }
        return res;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String checkNet() {
        if (global.isNetworkAvailable()) {
            return "true";
        } else {
            return "false";
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void getLocalData() {
        activity.runOnUiThread(() -> {
            ((MainActivity) activity).PickMasterDataFileDialogFromPage();
            ((MainActivity) activity).calledFrom = "htmlpage";
        });
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int DeletePremium(int PremiumId, int PolicyId) {
        @Language("SQL")
        String Query = "DELETE FROM tblPremium WHERE PremiumId=?";
        String arg[] = {String.valueOf(PremiumId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        //Premiums.toString();
        //calculated by herman
        int sumpremiums = getSumPremium(PolicyId);
        int policyvalue = getPolicyValue(PolicyId);
        if (sumpremiums < policyvalue) {
            updatePolicystatus(PolicyId, 1);
        }
        return 1;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int DeletePremium(int PremiumId) {
        @Language("SQL")
        String Query = "DELETE FROM tblPremium WHERE PremiumId=?";
        String arg[] = {String.valueOf(PremiumId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        //Premiums.toString();
        return 1;
    }

    //get sum of premiums of policy id
    public int getSumPremium(int PolicyId) {
        int sumpremiums = 0;
        @Language("SQL")
        String Query = "SELECT SUM(Amount) FROM tblPremium WHERE PolicyId=?";
        String arg[] = {String.valueOf(PolicyId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        try {
            JSONObject JmaxPremiumOb = Premiums.getJSONObject(0);
            sumpremiums = JmaxPremiumOb.getInt("SUM(Amount)");
            //  CHFID = JsonCHFIDJSONObject.getString("CHFID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sumpremiums;
    }

    public int getPolicyValue(int PolicyId) {
        int polv = 0;
        @Language("SQL")
        String Query = "SELECT PolicyValue FROM tblPolicy WHERE PolicyId=?";
        String arg[] = {String.valueOf(PolicyId)};
        JSONArray policyvalue = sqlHandler.getResult(Query, arg);
        try {
            JSONObject JmaxPremiumOb = policyvalue.getJSONObject(0);
            polv = JmaxPremiumOb.getInt("PolicyValue");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polv;
    }

    public void updatePolicystatus(int PolicyId, int status) {
        ContentValues values = new ContentValues();
        values.put("PolicyStatus", String.valueOf(status));
        try {//Update to new policy value
            sqlHandler.updateData("tblPolicy", values, "PolicyId = ?", new String[]{String.valueOf(PolicyId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }


    @JavascriptInterface
    @SuppressWarnings("unused")
    public int DeletePolicy(int PolicyId) {
        String[] arg = {String.valueOf(PolicyId)};
        String selector = "PolicyId=?";
        sqlHandler.deleteData(SQLHandler.tblPremium, selector, arg);
        sqlHandler.deleteData(SQLHandler.tblRecordedPolicies, selector, arg);
        sqlHandler.deleteData(SQLHandler.tblInsureePolicy, selector, arg);
        sqlHandler.deleteData(SQLHandler.tblPolicy, selector, arg);
        sqlHandler.clearCnAssignedToPolicy(PolicyId);
        return 1;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int DeleteInsuree(int InsureeId) {
        int res = 0;
        int FamilyId = 0;
        try {
            @Language("SQL")
            String Query = "SELECT FamilyId FROM tblInsuree WHERE InsureeId = " + InsureeId;
            JSONArray FID = sqlHandler.getResult(Query, null);
            JSONObject Ob = FID.getJSONObject(0);
            FamilyId = Ob.getInt("FamilyId");

            @Language("SQL")
            String IsHeadQuery = "SELECT InsureeId FROM tblInsuree WHERE InsureeId=? AND ishead =?";
            String IsHeadarg[] = {String.valueOf(InsureeId), "1"};
            JSONArray IsHead = sqlHandler.getResult(IsHeadQuery, IsHeadarg);
            int Count = IsHead.length();
            if (Count > 0)
                res = 2;
            else if (Count == 0) {
                @Language("SQL")
                String InsureeQuery = "DELETE FROM tblInsuree WHERE InsureeId=?";
                String arg[] = {String.valueOf(InsureeId)};
                JSONArray result = sqlHandler.getResult(InsureeQuery, arg);
                //Added by Salumu on 12/12/2017 to delete InsureePolicy
                DeleteInsureePolicy(0, InsureeId);
                getFamilyPolicies(FamilyId);
                res = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int DeleteFamily(int FamilyId) {
        String[] familyIdArgument = new String[]{String.valueOf(FamilyId)};
        @Language("SQL")
        String policyIdSubquery = "(SELECT PolicyId FROM tblPolicy WHERE FamilyId = ?)";

        @Language("SQL")
        String updateFetchedCNQuery = "UPDATE " + SQLHandler.tblBulkControlNumbers + " " +
                "SET PolicyId = NULL WHERE Id IS NOT NULL AND PolicyId IN " +
                policyIdSubquery;
        sqlHandler.getResult(updateFetchedCNQuery, familyIdArgument);

        @Language("SQL")
        String deleteInsertedCNQuery = "DELETE FROM " + SQLHandler.tblBulkControlNumbers + " " +
                "WHERE Id IS NULL AND PolicyId IN " +
                policyIdSubquery;
        sqlHandler.getResult(deleteInsertedCNQuery, familyIdArgument);

        @Language("SQL")
        String PremiumQuery = "DELETE FROM tblPremium \n" +
                "WHERE PolicyId IN \n" +
                policyIdSubquery;
        sqlHandler.getResult(PremiumQuery, familyIdArgument);
        //Premium.toString();

        @Language("SQL")
        String InsureePolicyQuery = "DELETE FROM tblInsureePolicy \n" +
                "WHERE PolicyId IN \n" +
                policyIdSubquery;
        sqlHandler.getResult(InsureePolicyQuery, familyIdArgument);

        @Language("SQL")
        String PolicyQuery = "DELETE FROM tblPolicy WHERE FamilyId = ?";
        sqlHandler.getResult(PolicyQuery, familyIdArgument);

        @Language("SQL")
        String InsureeQuery = "DELETE FROM  tblInsuree WHERE FamilyId = ?";
        sqlHandler.getResult(InsureeQuery, familyIdArgument);

        @Language("SQL")
        String FamilyQuery = "DELETE FROM  tblFamilies WHERE FamilyId = ?";
        sqlHandler.getResult(FamilyQuery, familyIdArgument);
        return 1;
    }

    public String OfflineEnquire(String CHFID) {
        sqlHandler.isPrivate = false;
        @Language("SQL")
        String Query = "SELECT CHFID ,Photo ,InsureeName,DOB,Gender,ProductCode,ProductName,ExpiryDate,Status,DedType,Ded1,Ded2,Ceiling1,Ceiling2 FROM tblPolicyInquiry WHERE  Trim(CHFID) = ?";
        String[] arg = {CHFID};
        JSONArray Insuree = sqlHandler.getResult(Query, arg);
        return Insuree.toString();
    }

    public String OfflineRenewals(String OfficerCode) {
        @Language("SQL")
        String Query = "SELECT RenewalId, PolicyId, OfficerId, OfficerCode, CHFID, LastName, OtherNames, ProductCode, ProductName, VillageName, RenewalPromptDate, IMEI, Phone,LocationId,PolicyValue, EnrollDate, RenewalUUID " +
                " FROM tblRenewals WHERE LOWER(OfficerCode)=? AND isDone = ? ";
        String[] arg = {OfficerCode.toLowerCase(), "N"};
        JSONArray Renews = sqlHandler.getResult(Query, arg);
        return Renews.toString();
    }


    public void InsertRenewalsFromApi(@NonNull JSONArray result) {
        String TableName = "tblRenewals";
        String[] Columns = {"renewalId", "policyId", "officerId", "officerCode", "chfid", "lastName", "otherNames", "productCode", "productName", "villageName", "renewalPromptDate", "phone", "renewalUUID"};
        String Where = "isDone = ?";
        String[] WhereArg = {"N"};
        sqlHandler.deleteData(TableName, Where, WhereArg);
        try {
            sqlHandler.insertData(TableName, Columns, result, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void InsertRenewalsFromExtract(String Result) {
        String TableName = "tblRenewals";
        String[] Columns = {"RenewalId", "PolicyId", "OfficerId", "OfficerCode", "CHFID", "LastName", "OtherNames", "ProductCode", "ProductName", "VillageName", "RenewalPromptDate", "Phone", "RenewalUUID"};
        String Where = "isDone = ?";
        String[] WhereArg = {"N"};
        sqlHandler.deleteData(TableName, Where, WhereArg);
        try {
            sqlHandler.insertData(TableName, Columns, Result, "");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    public void deleteRecodedPolicy(String policyId) {
        String Where = "PolicyId = ?";
        String[] WhereArg = {policyId};
        sqlHandler.deleteData(SQLHandler.tblRecordedPolicies, Where, WhereArg);
    }

    public void InsertRecordedPolicies(String WhitchPolicy, String FamilyId, String ProdId, String PolicyValue, int PolicyId) throws JSONException {
        JSONObject O;
        JSONArray InsuranceNumberArray = getInsuranceNumber(FamilyId);
        O = InsuranceNumberArray.getJSONObject(0);
        String InsuranceNumber = O.getString("CHFID");

        JSONArray LastNameArray = getLastName(FamilyId);
        O = LastNameArray.getJSONObject(0);
        String LastName = O.getString("LastName");

        JSONArray OtherNamesArray = getOtherNames(FamilyId);
        O = OtherNamesArray.getJSONObject(0);
        String OtherNames = O.getString("OtherNames");

        JSONArray ProductCodeArray = getProductCode(ProdId);
        O = ProductCodeArray.getJSONObject(0);
        String ProductCode = O.getString("ProductCode");

        JSONArray ProductNameArray = getProductName(ProdId);
        O = ProductNameArray.getJSONObject(0);
        String ProductName = O.getString("ProductName");

        ContentValues values = new ContentValues();
        // isOffline = getFamilyStatus(FamilyId);

        values.put("PolicyId", PolicyId);
        values.put("InsuranceNumber", InsuranceNumber);
        values.put("LastName", LastName);
        values.put("OtherNames", OtherNames);
        values.put("ProductCode", ProductCode);
        values.put("ProductName", ProductName);
        if (WhitchPolicy.equals("new")) {
            values.put("isDone", "N");
        } else {
            values.put("isDone", "Y");
        }
        values.put("PolicyValue", PolicyValue);
        values.put("UploadedDate", "");
        values.put("ControlRequestDate", "");

        sqlHandler.insertData("tblRecordedPolicies", values);
    }

    private JSONArray getProductName(String prodId) {
        @Language("SQL")
        String Query = "SELECT ProductName " +
                "FROM  tblProduct WHERE prodId = ? ";
        String[] arg = {prodId};
        return sqlHandler.getResult(Query, arg);
    }

    private JSONArray getProductCode(String prodId) {
        @Language("SQL")
        String Query = "SELECT ProductCode " +
                "FROM  tblProduct WHERE prodId = ? ";
        String[] arg = {prodId};
        return sqlHandler.getResult(Query, arg);
    }

    private JSONArray getOtherNames(String familyId) {
        @Language("SQL")
        String Query = "SELECT OtherNames " +
                "FROM  tblInsuree WHERE FamilyId = ? AND isHead = 1 ";
        String[] arg = {familyId};
        return sqlHandler.getResult(Query, arg);
    }

    private JSONArray getLastName(String familyId) {
        @Language("SQL")
        String Query = "SELECT LastName " +
                "FROM  tblInsuree WHERE FamilyId = ? AND isHead = 1 ";
        String[] arg = {familyId};
        return sqlHandler.getResult(Query, arg);
    }

    private JSONArray getInsuranceNumber(String familyId) {
        @Language("SQL")
        String Query = "SELECT CHFID " +
                "FROM  tblInsuree WHERE FamilyId = ? AND isHead  = 1";
        String[] arg = {familyId};
        try {
            return sqlHandler.getResult(Query, arg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateUploadedDate(int PolicyId) {
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        ContentValues values = new ContentValues();
        values.put("UploadedDate", d);
        try {
            sqlHandler.updateData("tblRecordedPolicies", values, "PolicyId = ?", new String[]{String.valueOf(PolicyId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }

    public void DeleteRenewalOfflineRow(Integer RenewalId) {
        @Language("SQL")
        String Query = "DELETE FROM tblRenewals WHERE RenewalId=?";
        String[] arg = {String.valueOf(RenewalId)};
        JSONArray Renewal = sqlHandler.getResult(Query, arg);
    }

    public void UpdateRenewTable(int RenewalId) {
        ContentValues values = new ContentValues();
        values.put("isDone", "Y");
        try {
            sqlHandler.updateData("tblRenewals", values, "RenewalId = ?", new String[]{String.valueOf(RenewalId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }

    public String getOfflineFeedBack(String OfficerCode) {
        @Language("SQL")
        String Query = "SELECT ClaimId,ClaimUUID,OfficerId,OfficerCode,CHFID,LastName,OtherNames,HFCode,HFName,ClaimCode,DateFrom,DateTo,IMEI,Phone,FeedbackPromptDate " +
                "FROM  tblFeedbacks WHERE LOWER(OfficerCode) = ?  AND  isDone = ?";
        String[] arg = {OfficerCode.toLowerCase(), "N"};
        JSONArray FeedBacks = sqlHandler.getResult(Query, arg);
        return FeedBacks.toString();
    }

    public boolean InsertFeedbacks(@NonNull List<FeedbackRequest> feedbacks) {
        try {
            return InsertFeedbacks(toJSONArray(feedbacks));
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean InsertFeedbacks(@NonNull JSONArray Result) {
        String TableName = "tblFeedbacks";
        String[] Columns = {"claimUUID", "officerId", "officerCode", "chfid", "lastName", "otherNames", "hfCode", "hfName", "claimCode", "dateFrom", "dateTo", "phone", "feedbackPromptDate"};
        String Where = "isDone = ?";
        String[] WhereArg = {"N"};
        sqlHandler.deleteData(TableName, Where, WhereArg);
        try {
            sqlHandler.insertData(TableName, Columns, Result, "");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @NonNull
    private JSONArray toJSONArray(@NonNull List<FeedbackRequest> feedbacks) throws JSONException {
        JSONArray array = new JSONArray();
        for (FeedbackRequest feedback : feedbacks) {
            JSONObject json = new JSONObject();
            json.put("claimUUID", feedback.getClaimUUID());
            json.put("officerId", feedback.getOfficeId());
            json.put("officerCode", feedback.getOfficerCode());
            json.put("chfid", feedback.getChfId());
            json.put("lastName", feedback.getLastName());
            json.put("otherNames", feedback.getOtherNames());
            json.put("hfCode", feedback.getHfCode());
            json.put("hfName", feedback.getHfName());
            json.put("claimCode", feedback.getClaimCode());
            json.put("dateFrom", DateUtils.toDateString(feedback.getFromDate()));
            json.put("dateTo", feedback.getToDate() == null ? null : DateUtils.toDateString(feedback.getToDate()));
            json.put("phone", feedback.getPhone());
            json.put("feedbackPromptDate", DateUtils.toDateString(feedback.getPromptDate()));
            array.put(json);
        }
        return array;
    }

    public String CleanFeedBackTable(String ClaimUUID) {
        @Language("SQL")
        String Query = "DELETE FROM tblFeedbacks WHERE ClaimUUID = ?";
        String[] arg = {ClaimUUID};
        JSONArray Feedback = sqlHandler.getResult(Query, arg);
        return Feedback.toString();
    }

    public void UpdateFeedBack(String ClaimUUID) {
        ContentValues values = new ContentValues();
        values.put("isDone", "Y");
        try {
            sqlHandler.updateData("tblFeedbacks", values, "ClaimUUID = ?", new String[]{ClaimUUID});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }

    private void DeleteFeedBacks() {
        @Language("SQL")
        String Query = "DELETE FROM tblFeedbacks WHERE isDone = 'Y'";
        sqlHandler.getResult(Query, null);
    }

    private void DeleteRenewals() {
        @Language("SQL")
        String Query = "DELETE FROM tblRenewals WHERE isDone = 'Y'";
        sqlHandler.getResult(Query, null);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int UpdatePolicy(int PolicyId, String PayDate, int policystatus) throws ParseException {
        ContentValues values = new ContentValues();
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        @Language("SQL")
        String PolicyQuery = "SELECT StartDate FROM tblPolicy WHERE PolicyId = " + PolicyId;
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, null);
        String StartDate = null;
        JSONObject O = null;
        try {
            O = Policy.getJSONObject(0);
            StartDate = O.getString("StartDate");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Date Paydate = format.parse(PayDate);
        Date Startdate = format.parse(StartDate);
        String Effectivedate;

        if (Paydate.after(Startdate))
            Effectivedate = PayDate;
        else
            Effectivedate = StartDate;


        values.put("PolicyStatus", String.valueOf(policystatus));
        values.put("EffectiveDate", Effectivedate);

        try {
            sqlHandler.updateData("tblPolicy", values, "PolicyId = ?", new String[]{String.valueOf(PolicyId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
        return 1;//Update Success
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void uploadEnrolment() throws Exception {
        final ProgressDialog finalPd = ProgressDialog.show(activity, activity.getResources().getString(R.string.Sync), activity.getResources().getString(R.string.SyncProcessing));
        try {
            new Thread(() -> {
                try {
                    enrol_result = Enrol(1);
                } catch (UserException | JSONException | IOException | NumberFormatException e) {
                    finalPd.dismiss();
                    e.printStackTrace();
                }
                finalPd.dismiss();
                if (myList.size() == 0) {
                    activity.runOnUiThread(() -> {
                        if (enrol_result != 999) {
                            //if error is encountered
                            if (enrolMessages.size() > 0) {
                                CharSequence[] charSequence = enrolMessages.toArray(new CharSequence[0]);
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setTitle(activity.getResources().getString(R.string.UploadFailureReport));
                                builder.setCancelable(false);
                                builder.setItems(charSequence, null);
                                builder.setPositiveButton(activity.getResources().getString(R.string.Ok), (dialogInterface, i) -> dialogInterface.dismiss());
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                enrolMessages.clear();

                            } else {
                                ShowDialog(activity.getResources().getString(R.string.FamilyUploaded));
                            }
                        } else {
                            ShowDialog(activity.getResources().getString(R.string.NoDataAvailable));
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            if (finalPd.isShowing()) {
                finalPd.dismiss();
            }
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void CreateEnrolmentXML() {
        new Thread(() -> {
            try {
                enrol_result = Enrol(2);
                if (enrol_result == 0) {
                    storageManager.requestCreateFile(MainActivity.REQUEST_CREATE_ENROL_EXPORT,
                            "application/octet-stream", getEnrolmentExportFilename());
                }
            } catch (Exception e) {
                Log.e("ENROL XML", "Error while creating enrolment xml", e);
            }
            if (myList.size() == 0) {
                activity.runOnUiThread(() -> {
                    if (enrol_result != 999) {
                        //if error is encountered
                        if (enrolMessages.size() > 0) {
                            CharSequence[] charSequences = enrolMessages.toArray(new CharSequence[(enrolMessages.size())]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle(activity.getResources().getString(R.string.UploadFailureReport));
                            builder.setCancelable(false);
                            builder.setItems(charSequences, null);
                            builder.setPositiveButton(activity.getResources().getString(R.string.Ok), (dialogInterface, i) -> dialogInterface.dismiss());
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            enrolMessages.clear();
                        } else {
                            //deleteImage();
                            ShowDialog(activity.getResources().getString(R.string.XmlCreated));
                        }
                    } else {
                        AndroidUtils.showToast(activity, R.string.NoDataAvailable);
                    }
                    //ShowDialog(mContext.getResources().getString(R.string.FamilyUploaded));
                });
            } else {
                activity.runOnUiThread(
                        () -> ShowDialog(myList.toString()));
            }
        }).start();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void CreateRenewalExport() {
        if (FileUtils.getFileCount(new File(global.getSubdirectory("Renewal"))) > 0) {
            new Thread(() -> storageManager.requestCreateFile(MainActivity.REQUEST_CREATE_RENEWAL_EXPORT,
                    "application/octet-stream", getRenewalExportFilename())).start();
        } else {
            AndroidUtils.showToast(activity, R.string.NoDataAvailable);
        }

    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void CreateFeedbackExport() {
        if (FileUtils.getFileCount(new File(global.getSubdirectory("Feedback"))) > 0) {
            new Thread(() -> storageManager.requestCreateFile(MainActivity.REQUEST_CREATE_FEEDBACK_EXPORT,
                    "application/octet-stream", getFeedbackExportFilename())).start();
        } else {
            AndroidUtils.showToast(activity, R.string.NoDataAvailable);
        }
    }

    public boolean isPolicyRequired() {
        return !getRule("AllowFamilyWithoutPolicy", false);
    }

    public boolean isContributionRequired() {
        return !getRule("AllowPolicyWithoutPremium")
                && getRule("ShowPaymentOption", true);
    }

    public String getFamilyValidationError(String chfid, int errorMessageId) {
        return String.format("Family %s %s",
                chfid,
                activity.getResources().getString(errorMessageId)
        );
    }

    public String getInsureeValidationError(String chfid, String lastname, String othername, int errorMessageId) {
        return String.format("Insuree %s %s %s %s",
                chfid, lastname, othername,
                activity.getResources().getString(errorMessageId)
        );
    }

    public ArrayList<String> VerifyFamily() throws JSONException {
        ArrayList<String> FamilyIDs = new ArrayList<>();
        boolean result;
        int IsOffline;
        String FamilyId;

        @Language("SQL")
        String Query = "SELECT FamilyId,isOffline  FROM tblFamilies WHERE InsureeId != ''" +
                " ORDER BY FamilyId";
        JSONArray familiesToUpload = sqlHandler.getResult(Query, null);

        if (familiesToUpload.length() > 0) {
            myList.clear();
            for (int i = 0; i < familiesToUpload.length(); i++) {
                result = true;
                JSONObject object;
                try {
                    object = familiesToUpload.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }

                FamilyId = object.getString("FamilyId");
                String offline = object.getString("isOffline");
                IsOffline = "true".equalsIgnoreCase(offline) || "1".equals(offline) ? 1 : 0;

                Query = "SELECT F.FamilyId, F.InsureeId, F.LocationId, I.CHFID AS HOFCHFID, NULLIF(F.Poverty,'null') Poverty, NULLIF(F.FamilyType,'null') FamilyType, NULLIF(F.FamilyAddress,'null') FamilyAddress, NULLIF(F.Ethnicity,'null') Ethnicity, NULLIF(F.ConfirmationNo,'null') ConfirmationNo, F.ConfirmationType ConfirmationType,F.isOffline FROM tblFamilies F\n" +
                        "INNER JOIN tblInsuree I ON I.InsureeId = F.InsureeId WHERE F.InsureeId != ''";
                Query += " AND F.FamilyId = " + FamilyId;
                JSONArray familyArray = sqlHandler.getResult(Query, null);
                if (familyArray.length() == 0) {
                    result = false;
                }

                //get Insurees
                Query = "SELECT I.InsureeId, I.FamilyId, I.CHFID, I.LastName, I.OtherNames, I.DOB, I.Gender, NULLIF(I.Marital,'null') Marital, I.isHead, NULLIF(I.IdentificationNumber,'null') IdentificationNumber, NULLIF(I.Phone,'null') Phone, REPLACE(I.PhotoPath, RTRIM(PhotoPath, REPLACE(PhotoPath, '/', '')), '') PhotoPath, NULLIF(I.CardIssued,'null') CardIssued, NULLIF(I.Relationship,'null') Relationship, NULLIF(I.Profession,'null') Profession, NULLIF(I.Education,'null') Education, NULLIF(I.Email,'null') Email, CASE WHEN I.TypeOfId='null' THEN null ELSE I.TypeOfId END TypeOfId, NULLIF(I.HFID,'null') HFID, NULLIF(I.CurrentAddress,'null') CurrentAddress, NULLIF(I.GeoLocation,'null') GeoLocation, NULLIF(I.CurVillage,'null') CurVillage,I.isOffline \n" +
                        "FROM tblInsuree I \n" +
                        "WHERE I.InsureeId != ''";
                Query += " AND I.FamilyId  = " + FamilyId;
                JSONArray insureesArray = sqlHandler.getResult(Query, null);
                if (IsOffline == 1) {
                    if (insureesArray.length() == 0) {
                        result = false;
                    }
                }
                //get Policies
                Query = "SELECT PolicyId, FamilyId, EnrollDate, StartDate, NULLIF(EffectiveDate,'null') EffectiveDate, ExpiryDate, Policystatus, PolicyValue, ProdId, OfficerId, PolicyStage, isOffline\n" +
                        "FROM tblPolicy ";
                Query += " WHERE FamilyId = " + FamilyId;
                JSONArray policiesArray = sqlHandler.getResult(Query, null);

                if (IsOffline == 1 && isPolicyRequired() && policiesArray.length() == 0) { //Family offline without a policy
                    myList.add(getFamilyValidationError(familyArray.getJSONObject(0).getString("HOFCHFID"), R.string.WithoutPolicy));
                    result = false;
                }

                //get Premiums
                Query = "SELECT PR.PremiumId, PR.PolicyId, NULLIF(PR.PayerId,'null') PayerId, PR.Amount, PR.Receipt, PR.PayDate, PR.PayType, PR.isPhotoFee,PR.isOffline\n" +
                        "FROM tblPremium PR\n" +
                        "INNER JOIN tblPolicy PL ON PL.PolicyId = PR.PolicyId";
                Query += " WHERE FamilyId = " + FamilyId;
                JSONArray premiumsArray = sqlHandler.getResult(Query, null);
                if (IsOffline == 1 || policiesArray.length() != 0) { //Family offline or policy added to online family
                    if (isContributionRequired() && premiumsArray.length() == 0) {
                        myList.add(getFamilyValidationError(familyArray.getJSONObject(0).getString("HOFCHFID"), R.string.WithoutPremium));
                        result = false;
                    }
                }

                if (!VerifyPhoto(insureesArray)) {
                    result = false;
                }
                if (result) {
                    FamilyIDs.add(FamilyId);
                }
            }
        }

        if (myList.size() != 0) {
            ShowErrorMessages();
            myList.clear();
        }
        return FamilyIDs;
    }

    public boolean VerifyPhoto(JSONArray insurees) throws JSONException {
        boolean result = true;
        for (int j = 0; j < insurees.length(); j++) {
            JSONObject Insureeobject = insurees.getJSONObject(j);
            String s1 = Insureeobject.getString("isOffline");
            int IsOffline;
            if (s1.equals("true") || s1.equals("1")) IsOffline = 1;
            else IsOffline = 0;

            if (IsOffline == 1) {
                if (!getRule("AllowInsureeWithoutPhoto")) {
                    String PhotoPath = Insureeobject.getString("PhotoPath");
                    if (PhotoPath.length() == 0 || PhotoPath.equals("null")) {
                        myList.add(getInsureeValidationError(
                                Insureeobject.getString("CHFID"),
                                Insureeobject.getString("LastName"),
                                Insureeobject.getString("OtherNames"),
                                R.string.WithoutPhoto
                        ));
                        result = false;
                    }
                }
            }

        }
        return result;
    }

    private int Enrol(int CallerId) throws UserException, JSONException, IOException {
        ArrayList<String> verifiedId = new ArrayList<>();
        myList.clear();
        int rtEnrolledId = 0;
        int EnrolResult = 0;
        String Offline = null;

        @Language("SQL")
        String queryF, queryI, queryPL, queryPR, queryIP;

        //Verify Enrollments
        if (CallerId == 2) {
            verifiedId = VerifyFamily();
            if (verifiedId.size() == 0) {
                return 999;
            }
        }
        //Get all the families which are in offline state
        JSONArray familiesToUpload = sqlHandler.getResult("SELECT FamilyId, isOffline FROM tblFamilies WHERE InsureeId != '' ORDER BY FamilyId", null);
        int length;
        if (CallerId == 2) {
            length = 1;
        } else {
            length = familiesToUpload.length();
        }
        if (length == 0) {
            return 999;
        }
        //Loop through each familyId and get Header, Insuree, Policy and Premium details
        for (int i = 0; i < length; i++) {

            String CHFNumber = null;
            JSONObject object = null;
            try {
                object = familiesToUpload.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (object == null) {
                continue;
            }
            // try {
            String FamilyId = object.getString("FamilyId");
            String offlineString = object.getString("isOffline");
            int IsOffline = offlineString.equals("1") || offlineString.equalsIgnoreCase("true") ? 1 : 0;

            StringBuilder query = new StringBuilder(
                    "SELECT F.FamilyUUID as FamilyUUID, F.FamilyId AS FamilyId, F.InsureeId AS InsureeId, F.LocationId, I.CHFID AS HOFCHFID, NULLIF(F.Poverty,'null') Poverty, NULLIF(F.FamilyType,'null') FamilyType, NULLIF(F.FamilyAddress,'null') FamilyAddress, NULLIF(F.Ethnicity,'null') Ethnicity, NULLIF(F.ConfirmationNo,'null') ConfirmationNo, F.ConfirmationType ConfirmationType,F.isOffline isOffline FROM tblFamilies F INNER JOIN tblInsuree I ON I.InsureeId = F.InsureeId WHERE"
            );

            if (CallerId != 2) {
                query.append(" F.FamilyId = ").append(FamilyId).append("");
            } else {
                for (int j = 0; j < verifiedId.size(); j++) {
                    if ((verifiedId.size() - j) == 1) {
                        query.append(" F.FamilyId == ").append(verifiedId.get(j)).append("");
                    } else {
                        query.append(" F.FamilyId == ").append(verifiedId.get(j)).append(" OR");
                    }
                }
                if (verifiedId.size() == 0) {
                    query.append(" F.InsureeId != ''");
                } else {
                    query.append(" AND F.InsureeId != ''");
                }
            }


            queryF = query.toString();
            JSONArray familyArray = sqlHandler.getResult(queryF, null);

            JSONArray newFamilyArray = new JSONArray();
            JSONObject ob1 = null;
            for (int j = 0; j < familyArray.length(); j++) {
                ob1 = familyArray.getJSONObject(j);
                String typeofId = ob1.getString("FamilyType");
                String ConfirmationType = ob1.getString("ConfirmationType");
                String FId = ob1.getString("FamilyId");

                String s1 = ob1.getString("isOffline");
                if (s1.equals("true") || s1.equals("1")) Offline = "1";
                else if (s1.equals("2")) Offline = "2";
                else Offline = "0";

                if (Offline.equals("2") || Offline.equals("0")) {
                    // FId = "-" + FId;
                    ob1.put("FamilyId", FId);
                    ob1.put("isOffline", 0);
                }
                if (typeofId.equals("0")) {
                    ob1.put("FamilyType", "");
                }
                if (ConfirmationType.equals("0") || ConfirmationType.equals("null")) {
                    ob1.put("ConfirmationType", "");
                }
                JSONObject familySMS = getFamilySMS(FId);
                if (familySMS != null) {
                    // Ensure ApprovalOfSMS Is sent as Boolean
                    familySMS.put("ApprovalOfSMS",
                            familySMS.getString("ApprovalOfSMS").equals("1"));
                }
                ob1.put("FamilySMS", familySMS);
            }
            newFamilyArray.put(ob1);
            familyArray = newFamilyArray;

            //get Insureesf
            query = new StringBuilder(
                    "SELECT I.InsureeUUID AS InsureeUUID, I.InsureeId AS InsureeId, I.FamilyId AS FamilyId, I.CHFID, I.LastName, I.OtherNames, I.DOB, I.Gender, NULLIF(I.Marital,'') Marital, I.isHead, NULLIF(I.IdentificationNumber,'null') IdentificationNumber, NULLIF(I.Phone,'null') Phone, REPLACE(I.PhotoPath, RTRIM(PhotoPath, REPLACE(PhotoPath, '/', '')), '') PhotoPath, NULLIF(I.CardIssued,'null') CardIssued, NULLIF(I.Relationship,'null') Relationship, NULLIF(I.Profession,'null') Profession, NULLIF(I.Education,'null') Education, NULLIF(I.Email,'null') Email, CASE WHEN I.TypeOfId='null' THEN null ELSE I.TypeOfId END TypeOfId, NULLIF(I.HFID,'null') HFID, NULLIF(I.CurrentAddress,'null') CurrentAddress, NULLIF(I.GeoLocation,'null') GeoLocation, NULLIF(I.CurVillage,'null') CurVillage,I.isOffline, I.Vulnerability FROM tblInsuree I WHERE "
            );
            if (CallerId != 2) {
                query.append(" I.FamilyId = ").append(FamilyId).append(" \n");
            } else {
                query.append("(");
                for (int j = 0; j < verifiedId.size(); j++) {

                    if (getFamilyStatus(Integer.parseInt(verifiedId.get(j))) == 0) {

                        if ((verifiedId.size() - j) == 1) {
                            query.append("I.FamilyId = ").append(verifiedId.get(j));
                        } else {
                            query.append("I.FamilyId = ").append(verifiedId.get(j)).append(" OR ");
                        }
                    } else {
                        if ((verifiedId.size() - j) == 1) {
                            query.append(" I.FamilyId = ").append(verifiedId.get(j));
                        } else {
                            query.append(" I.FamilyId = ").append(verifiedId.get(j)).append(" OR ");
                        }
                    }
                }
                if (verifiedId.size() == 0) {
                    query.append(" I.InsureeId != ''");
                }
                query.append(")");
            }

            queryI = query.toString();
            JSONArray insureesArray = sqlHandler.getResult(queryI, null);

            if (insureesArray.length() > 0) {
                JSONObject o = insureesArray.getJSONObject(0);
                CHFNumber = o.getString("CHFID");

                JSONArray newInsureesArray = new JSONArray();

                for (int j = 0; j < insureesArray.length(); j++) {
                    JSONObject ob = insureesArray.getJSONObject(j);
                    String typeofId = ob.getString("TypeOfId");

                    if (typeofId.equals("0")) {
                        ob.put("TypeOfId", "");
                    }
                    newInsureesArray.put(ob);
                }

                insureesArray = newInsureesArray;
            }

            //get Policies
            query = new StringBuilder(
                    "SELECT p.PolicyId AS PolicyId, FamilyId AS FamilyId, EnrollDate, StartDate, NULLIF(EffectiveDate,'null') EffectiveDate, ExpiryDate, Policystatus, PolicyValue, ProdId, OfficerId, PolicyStage, isOffline, bcn.ControlNumber FROM tblPolicy p LEFT JOIN tblBulkControlNumbers bcn on p.PolicyId=bcn.PolicyId WHERE "
            );
            if (CallerId != 2) {
                query.append(" FamilyId = ").append(FamilyId);
            } else {
                for (int j = 0; j < verifiedId.size(); j++) {
                    if ((verifiedId.size() - j) == 1) {
                        query.append(" FamilyId == ").append(verifiedId.get(j));
                    } else {
                        query.append(" FamilyId == ").append(verifiedId.get(j)).append(" OR");
                    }
                }
                if (verifiedId.size() == 0) {
                    query.append(" FamilyId != ''");
                }
            }

            queryPL = query.toString();
            JSONArray policiesArray = sqlHandler.getResult(queryPL, null);


            if (IsOffline == 1 && isPolicyRequired() && policiesArray.length() == 0) {
                myList.add(getFamilyValidationError(
                        insureesArray.getJSONObject(0).getString("CHFID"),
                        R.string.WithoutPolicy
                ));
            }

            if (insureesArray.length() > 0 || policiesArray.length() > 0 || familyArray.length() > 0) {

                //get Premiums
                query = new StringBuilder(
                        "SELECT PR.PremiumId, PR.PolicyId, NULLIF(PR.PayerId,'null') PayerId, PR.Amount, PR.Receipt, PR.PayDate, PR.PayType, PR.isPhotoFee,PR.isOffline FROM tblPremium PR INNER JOIN tblPolicy PL ON PL.PolicyId = PR.PolicyId WHERE "
                );
                if (CallerId != 2) {
                    query.append(" FamilyId = ").append(FamilyId);
                } else {
                    for (int j = 0; j < verifiedId.size(); j++) {
                        if ((verifiedId.size() - j) == 1) {
                            query.append(" FamilyId == ").append(verifiedId.get(j));
                        } else {
                            query.append(" FamilyId == ").append(verifiedId.get(j)).append(" OR");
                        }

                    }
                    if (verifiedId.size() == 0) {
                        query.append(" PR.PolicyId != ''");
                    }
                }

                queryPR = query.toString();
                JSONArray premiumsArray = sqlHandler.getResult(queryPR, null);

                if (IsOffline == 1 || policiesArray.length() != 0) { //Family offline or policy added to online family
                    if (isContributionRequired() && premiumsArray.length() == 0) {
                        myList.add(getFamilyValidationError(
                                insureesArray.getJSONObject(0).getString("CHFID"),
                                R.string.WithoutPremium
                        ));
                    }
                }

                //get InsureePolicy
                query = new StringBuilder(
                        "SELECT DISTINCT(IP.InsureeId) AS InsureeId,IP.PolicyId,IP.EffectiveDate FROM tblInsureePolicy IP INNER JOIN tblPolicy PL ON PL.PolicyId = IP.PolicyId"
                );
                if (CallerId == 1) {
                    query.append(" AND  PL.FamilyId = ").append(FamilyId); // JOIN
                    query.append(" WHERE PL.FamilyId = ").append(FamilyId); // WHERE CLAUSE
                }

                queryIP = query.toString();

                if (CallerId != 2) {
                    Pair<String, byte[]>[] InsureeImages = FamilyPictures(insureesArray, 1);
                    if (myList.size() == 0) {
                        EnrolResult = uploadEnrols(familyArray, insureesArray, policiesArray, premiumsArray, InsureeImages);
                    } else {
                        ShowErrorMessages();
                        break;
                    }
                } else {
                    if (!"".equals(queryF)) {
                        sqlHandler.getExportAsXML(queryF, queryI, queryPL, queryPR, queryIP, global.getOfficerCode(), global.getOfficerId());
                        FamilyPictures(insureesArray, 2);
                    }
                    EnrolResult = 0;
                }
                if (EnrolResult >= 0) {
                    updatePolicyRecords(policiesArray);
                    if (IsOffline == 0 && EnrolResult > 0) {
                        ContentValues values = new ContentValues();
                        @Language("SQL")
                        String UpdateQuery;
                        values.put("isOffline", 2);
                        if (premiumsArray.length() > 0) {
                            values.put("PremiumId", EnrolResult);
                            sqlHandler.updateData("tblPremium", values, "PremiumId = ?", new String[]{"0"});
                            rtEnrolledId = EnrolResult;
                        } else if (policiesArray.length() > 0) {
                            UpdateQuery = "UPDATE tblPolicy SET PolicyId = " + EnrolResult + ", isOffline = 2 WHERE PolicyId = 0 AND isOffline = 0";
                            sqlHandler.getResult(UpdateQuery, null);
                            rtEnrolledId = EnrolResult;
                        } else if (insureesArray.length() > 0) {
                            values.put("InsureeId", EnrolResult);
                            sqlHandler.updateData("tblInsuree", values, "InsureeId = ?", new String[]{"0"});
                            rtEnrolledId = EnrolResult;
                        }
                    } else {
                        ContentValues valuesF = new ContentValues();
                        valuesF.put("isOffline", 0);
                        sqlHandler.updateData("tblFamilies", valuesF, "FamilyId = ?", new String[]{FamilyId});
                    }

                    if (myList.size() == 0) {
                        if (CallerId == 1) {
                            DeleteImages(insureesArray, verifiedId, CallerId);
                        }

                        DeleteUploadedData(Integer.parseInt(FamilyId), verifiedId, CallerId);
                        DeleteFamily(Integer.parseInt(FamilyId));
                    }


                } else {
                    String ErrMsg;
                    switch (EnrolResult) {
                        case -1:
                            ErrMsg = "[" + CHFNumber + "] " + activity.getString(R.string.MissingHOF);
                            break;
                        case -2:
                            ErrMsg = "[" + CHFNumber + "] " + activity.getString(R.string.DuplicateHOF);
                            break;
                        case -3:
                            ErrMsg = "[" + CHFNumber + "] " + activity.getString(R.string.DuplicateInsuranceNumber);
                            break;
                        case -4:
                            ErrMsg = "[" + CHFNumber + "] " + activity.getString(R.string.DuplicateReceiptNumber);
                            break;
                        case -5:
                            ErrMsg = "[" + CHFNumber + "] " + activity.getString(R.string.DoubleHeadInServer);
                            break;
                        case -6:
                            ErrMsg = "[" + CHFNumber + "] " + activity.getString(R.string.Interuption);
                            break;
                        case -400:
                            ErrMsg = "[" + CHFNumber + "] " + activity.getString(R.string.ServerError);
                            break;
                        default:
                            ErrMsg = "[" + CHFNumber + "] " + activity.getString(R.string.UncaughtException);
                    }
                    enrolMessages.add(ErrMsg);
                }

            } else {
                EnrolResult = 0;

                if (CallerId == 1) {
                    DeleteImages(insureesArray, verifiedId, CallerId);
                }
                DeleteUploadedData(Integer.parseInt(FamilyId), verifiedId, CallerId);
                if (IsOffline == 0) {
                    DeleteFamily(Integer.parseInt(FamilyId));
                }
            }
        }
        if (rtEnrolledId > 0) return rtEnrolledId;
        return EnrolResult;
    }

    private int uploadEnrols(
            @NonNull JSONArray familyArray,
            @NonNull JSONArray insureesArray,
            @NonNull JSONArray policiesArray,
            @NonNull JSONArray premiumsArray,
            @NonNull Pair<String, byte[]>[] insureeImages
    ) throws JSONException {
        JSONObject familyObj = familyArray.getJSONObject(0);
        try {
            Family family = familyFromJSONObject(familyObj, insureesArray, insureeImages);
            for (int j = 0; j < policiesArray.length(); j++) {
                JSONArray policyPremiums = new JSONArray();
                String policyId = policiesArray.getJSONObject(j).getString("PolicyId");
                for (int k = 0; k < premiumsArray.length(); k++) {
                    JSONObject premiumObject = premiumsArray.getJSONObject(k);
                    if (StringUtils.equals(policyId, premiumObject.getString("PolicyId"))) {
                        policyPremiums.put(premiumObject);
                    }
                }
                policiesArray.getJSONObject(j).put("premium", policyPremiums);
            }
            List<Family.Policy> policies = familyPolicyFromJSONObject(family.getUuid(), policiesArray);
            new UpdateFamily().execute(family, policies);
        } catch (Exception e) {
            e.printStackTrace();
            enrolMessages.add(Objects.requireNonNullElse(e.getMessage(), "Something went wrong updating the family"));
            return -400;
        }

        return 0;
    }

    @NonNull
    private Family familyFromJSONObject(
            @NonNull JSONObject json,
            @NonNull JSONArray insurees,
            @NonNull Pair<String, byte[]>[] insureeImages
    ) throws JSONException {
        List<Family.Member> members = new ArrayList<>();
        String familyUUID = JsonUtils.getStringOrDefault(json, "FamilyUUID", UUID.randomUUID().toString(), true);
        for (int i = 0; i < insurees.length(); i++) {
            members.add(familyMemberFromJSONObject(familyUUID, insurees.getJSONObject(i), insureeImages[i]));
        }
        return new Family(
                /* headChfId = */ json.getString("HOFCHFID"),
                /* id = */ Integer.parseInt(json.getString("FamilyId")),
                /* uuid = */ familyUUID,
                /* sms = */ null,
                /* locationId = */ Integer.parseInt(json.getString("LocationId")),
                /* isPoor = */ JsonUtils.getBooleanOrDefault(json, "Poverty", false),
                /* type = */ JsonUtils.getStringOrDefault(json, "FamilyType"),
                /* address = */ JsonUtils.getStringOrDefault(json, "FamilyAddress"),
                /* ethnicity = */ JsonUtils.getStringOrDefault(json, "Ethnicity"),
                /* confirmationNumber = */ JsonUtils.getStringOrDefault(json, "ConfirmationNo"),
                /* confirmationType = */ JsonUtils.getStringOrDefault(json, "ConfirmationType"),
                /* isOffline = */ JsonUtils.getBooleanOrDefault(json, "isOffline", false),
                /* members = */ members
        );
    }

    @NonNull
    private Family.Member familyMemberFromJSONObject(
            @NonNull String familyUUID,
            @NonNull JSONObject object,
            @Nullable Pair<String, byte[]> image
    ) throws JSONException {
        return new Family.Member(
                /* chfId = */ object.getString("CHFID"),
                /* isHead = */ JsonUtils.getBooleanOrDefault(object, "isHead", false),
                /* id = */ Integer.parseInt(object.getString("InsureeId")),
                /* uuid = */ JsonUtils.getStringOrDefault(object, "InsureeUUID"),
                /* familyId = */ Integer.parseInt(object.getString("FamilyId")),
                /* familyUuid = */ familyUUID,
                /* identificationNumber = */ JsonUtils.getStringOrDefault(object, "IdentificationNumber"),
                /* lastName = */ object.getString("LastName"),
                /* otherNames = */ object.getString("OtherNames"),
                /* dateOfBirth = */ Objects.requireNonNull(JsonUtils.getDateOrDefault(object, "DOB")),
                /* gender = */ object.getString("Gender"),
                /* marital = */ JsonUtils.getStringOrDefault(object, "Marital"),
                /* phone = */ JsonUtils.getStringOrDefault(object, "Phone"),
                /* cardIssued = */ JsonUtils.getBooleanOrDefault(object, "CardIssued", false),
                /* relationship = */ JsonUtils.getIntegerOrDefault(object, "Relationship"),
                /* profession = */ JsonUtils.getIntegerOrDefault(object, "Profession"),
                /* education = */ JsonUtils.getIntegerOrDefault(object, "Education"),
                /* email = */ JsonUtils.getStringOrDefault(object, "Email"),
                /* typeOfId = */ JsonUtils.getStringOrDefault(object, "TypeOfId"),
                /* healthFacilityId = */JsonUtils.getIntegerOrDefault(object, "HFID"),
                /* currentAddress = */ JsonUtils.getStringOrDefault(object, "CurrentAddress"),
                /* currentVillage = */ JsonUtils.getIntegerOrDefault(object, "CurVillage"),
                /* geolocation = */ JsonUtils.getStringOrDefault(object, "GeoLocation"),
                /* photoPath = */ image != null ? image.first : null,
                /* photoBytes = */ image != null ? image.second : null,
                /* isOffline = */ JsonUtils.getBooleanOrDefault(object, "isOffline", false)
        );
    }

    @NonNull
    private List<Family.Policy> familyPolicyFromJSONObject(
            @NonNull String familyUUID,
            @NonNull JSONArray array
    ) throws JSONException {
        List<Family.Policy> policies = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            policies.add(new Family.Policy(
                    /* id = */ Integer.parseInt(object.getString("PolicyId")),
                    /* uuid = */ null,
                    /* familyId = */ Integer.parseInt(object.getString("FamilyId")),
                    /* familyUuid = */ familyUUID,
                    /* enrollDate = */ Objects.requireNonNull(JsonUtils.getDateOrDefault(object, "EnrollDate")),
                    /* startDate = */ Objects.requireNonNull(JsonUtils.getDateOrDefault(object, "StartDate")),
                    /* effectiveDate = */ JsonUtils.getDateOrDefault(object, "EffectiveDate"),
                    /* expiryDate = */ Objects.requireNonNull(JsonUtils.getDateOrDefault(object, "ExpiryDate")),
                    /* status = */ JsonUtils.getStringOrDefault(object, "Policystatus"),
                    /* value = */ JsonUtils.getDoubleOrDefault(object, "PolicyValue"),
                    /* productId = */ JsonUtils.getIntegerOrDefault(object, "ProdId"),
                    /* officerId = */ Integer.parseInt(object.getString("OfficerId")),
                    /* stage = */ JsonUtils.getStringOrDefault(object, "PolicyStage"),
                    /* isOffline = */ JsonUtils.getBooleanOrDefault(object, "isOffline", false),
                    /* controlNumber = */ JsonUtils.getStringOrDefault(object, "ControlNumber"),
                    /* premiums = */ object.has("premium") ? familyPolicyPremiumsFromJSONObject(object.getJSONArray("premium")) : Collections.emptyList()
            ));
        }
        return policies;
    }

    @NonNull
    private List<Family.Policy.Premium> familyPolicyPremiumsFromJSONObject(
            @NonNull JSONArray array
    ) throws JSONException {
        List<Family.Policy.Premium> premiums = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            premiums.add(new Family.Policy.Premium(
                    /* id = */ Integer.parseInt(object.getString("PremiumId")),
                    /* policyId = */ Integer.parseInt(object.getString("PolicyId")),
                    /* policyUuid = */ null,
                    /* payerId = */ JsonUtils.getIntegerOrDefault(object, "PayerId"),
                    /* amount = */ JsonUtils.getDoubleOrDefault(object, "Amount"),
                    /* receipt = */ JsonUtils.getStringOrDefault(object, "Receipt"),
                    /* payDate = */ JsonUtils.getDateOrDefault(object, "PayDate"),
                    /* payType = */ JsonUtils.getStringOrDefault(object, "PayType"),
                    /* isPhotoFee = */ JsonUtils.getBooleanOrDefault(object, "isPhotoFee", false),
                    /* isOffline = */ JsonUtils.getBooleanOrDefault(object, "isOffline", false)
            ));
        }
        return premiums;
    }

    public void updatePolicyRecords(@NonNull JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject ob = jsonArray.getJSONObject(i);
            int policyId = ob.getInt("PolicyId");
            updateUploadedDate(policyId);
        }
    }

    public void clearDirectory(String directory) {
        File[] files = new File(global.getSubdirectory(directory)).listFiles();
        if (files != null) {
            FileUtils.deleteFiles(files);
        }
    }

    public Pair<String, byte[]>[] FamilyPictures(@NonNull JSONArray insurees, int CallerId) {
        Pair<String, byte[]>[] images = new Pair[insurees.length()];
        int IsOffline;
        for (int j = 0; j < insurees.length(); j++) {
            try {
                JSONObject InsureeObject = insurees.getJSONObject(j);
                String PhotoPath = (InsureeObject.getString("PhotoPath"));
                String chfId = (InsureeObject.getString("CHFID"));
                String lastName = (InsureeObject.getString("LastName"));
                String otherNames = (InsureeObject.getString("OtherNames"));

                String s1 = InsureeObject.getString("isOffline");
                if (s1.equals("true") || s1.equals("1")) IsOffline = 1;
                else if (s1.equals("false") || s1.equals("0")) IsOffline = 0;
                else IsOffline = Integer.parseInt(s1);

                if (PhotoPath.length() > 0 && !PhotoPath.equals("null")) {
                    File[] files = GetListOfImages(global.getImageFolder(), PhotoPath);

                    if (files.length > 0) {
                        try (InputStream in = new FileInputStream(files[0])) {
                            byte[] imgContent = IOUtils.toByteArray(in);
                            if (CallerId != 2) {
                                images[j] = new Pair<>(files[0].getName(), imgContent);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (CallerId != 2) {
                        images[j] = new Pair<>("", new byte[0]);
                    }
                } else {
                    if (IsOffline == 1) {
                        if (getRule("AllowInsureeWithoutPhoto")) {
                            byte[] empty = new byte[0];
                            Pair<String, byte[]> img = new Pair<>("", empty);
                            images[j] = img;
                        } else {
                            myList.add(getInsureeValidationError(
                                    chfId, lastName, otherNames,
                                    R.string.WithoutPhoto
                            ));
                            ShowErrorMessages();
                            break;
                        }
                    } else {
                        images[j] = new Pair<>("", new byte[0]);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return images;
    }

    public String getEnrolmentExportFilename() {
        return getExportFileName("Enrolment");
    }

    public String getRenewalExportFilename() {
        return getExportFileName("Renewal");
    }

    public String getFeedbackExportFilename() {
        return getExportFileName("Feedback");
    }

    public String getExportFileName(String type) {
        SimpleDateFormat formatZip = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.US);
        Calendar cal = Calendar.getInstance();
        String dzip = formatZip.format(cal.getTime());
        return type + "_" + global.getOfficerCode() + "_" + dzip + ".rar";
    }


    public File zipEnrolmentFiles() {
        File zipFile = FileUtils.createTempFile(activity, "exports/" + getEnrolmentExportFilename(), true);
        String password = getRarPwd();
        File imageDir = new File(global.getImageFolder());
        File familyDir = new File(global.getSubdirectory("Family"));

        return ZipUtils.zipDirectories(zipFile, password, familyDir, imageDir);
    }

    public File zipRenewalFiles() {
        File zipFile = FileUtils.createTempFile(activity, "exports/" + getRenewalExportFilename(), true);
        String password = getRarPwd();
        File familyDir = new File(global.getSubdirectory("Renewal"));

        return ZipUtils.zipDirectories(zipFile, password, familyDir);
    }

    public File zipFeedbackFiles() {
        File zipFile = FileUtils.createTempFile(activity, "exports/" + getFeedbackExportFilename(), true);
        String password = getRarPwd();
        File familyDir = new File(global.getSubdirectory("Feedback"));

        return ZipUtils.zipDirectories(zipFile, password, familyDir);
    }

    public boolean unZipWithPassword(String fileName, String password) {
        String targetPath = fileName;
        String unzippedFolderPath = global.getSubdirectory("Database");
        //String unzippedFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/IMIS/Enrolment/Enrolment_"+global.getOfficerCode()+"_"+d+".xml";
        //here we not don't have password set yet so we pass password from Edit Text rar input
        try {
            ZipUtils.unzipPath(targetPath, unzippedFolderPath, password);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean unZipFeedbacksRenewals(File file) {
        String password = getRarPwd();
        try {
            ZipUtils.unzipFile(file, file.getParentFile(), password);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String getRarPwd() {
        String password = "";
        try {
            SharedPreferences sharedPreferences = global.getSharedPreferences("MyPref", 0);

            if (!sharedPreferences.contains("rarPwd")) {
                password = AppInformation.DomainInfo.getDefaultRarPassword();
            } else {
                String encryptedRarPassword = sharedPreferences.getString("rarPwd", AppInformation.DomainInfo.getDefaultRarPassword());
                String trimEncryptedPassword = encryptedRarPassword.trim();
                String salt = sharedPreferences.getString("salt", null);
                String trimSalt = salt.trim();
                password = decryptRarPwd(trimEncryptedPassword, trimSalt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return password;
    }

    public void DeleteImages(JSONArray insurees, ArrayList<String> FamilyIDs, int CallerId) {

        for (int j = 0; j < insurees.length(); j++) {
            try {
                JSONObject Insureeobject = insurees.getJSONObject(j);
                if (CallerId == 2 && FamilyIDs.size() != 0) {
                    for (int x = 0; x < FamilyIDs.size(); x++) {
                        if ((Insureeobject.getString("FamilyId").equals(FamilyIDs.get(x)))) {
                            String PhotoPath = (Insureeobject.getString("PhotoPath"));
                            if (PhotoPath.length() > 0 && !PhotoPath.equals("null")) {


                                File[] files = GetListOfImages(global.getImageFolder(), PhotoPath);
                                if (files.length > 0) {
                                    for (int i = 0; i < files.length; i++) {
                                        files[i].delete();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    String PhotoPath = (Insureeobject.getString("PhotoPath"));
                    if (PhotoPath.length() > 0 && !PhotoPath.equals("null")) {
                        File[] files = GetListOfImages(global.getImageFolder(), PhotoPath);
                        if (files.length > 0) {
                            for (int i = 0; i < files.length; i++) {
                                files[i].delete();
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void ShowErrorMessages() {

        activity.runOnUiThread(() -> {
            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(activity);
            View promptsView = li.inflate(R.layout.error_message, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final TextView textView1 = promptsView.findViewById(R.id.textView1);
            final RecyclerView error_message = promptsView.findViewById(R.id.error_message);

            EnrollmentReport enrollmentReport = new EnrollmentReport(activity, myList);

            error_message.setLayoutManager(new LinearLayoutManager(activity));
            error_message.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
            error_message.setAdapter(enrollmentReport);

            String title = activity.getString(R.string.failedToUpload);
            textView1.setText(title.toUpperCase());
            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, id) -> dialog.cancel())
                    .show();
        });

    }


    public Boolean isOfficerCodeValid(String OfficerCode) throws JSONException {
        @Language("SQL")
        String Query = "SELECT OfficerId , OtherNames || ' ' || LastName AS OfficerName\n" +
                "FROM tblOfficer WHERE LOWER(Code)= LOWER(?)";
        String[] arg = {OfficerCode.toLowerCase()};
        JSONArray Officer = sqlHandler.getResult(Query, arg);
        int Count = Officer.length();
        JSONObject object;
        if (Count > 0) {
            object = Officer.getJSONObject(0);
            int OfficerId = Integer.parseInt(object.getString("OfficerId"));
            String OfficerName = object.getString("OfficerName");
            global.setOfficerId(OfficerId);
            global.setOfficerName(OfficerName);
            return true;
        } else {
            return false;
        }
    }

    // Login to Api from JavaScript (call method LoginToken)
    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean LoginJI(final String Username, final String Password) {
        return LoginToken(Username, Password);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void Logout() {
        global.getLoginRepository().logout();
        MainActivity.SetLoggedIn();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean isLoggedIn() {
        return global.isLoggedIn();
    }

    // Login to API and get Token JWT
    @SuppressLint("StaticFieldLeak")
    public boolean LoginToken(@NonNull String username, @NonNull String password) {
        try {
            return new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {
                    try {
                        new Login().execute(username, password);
                        return true;
                    } catch (Exception e) {
                        Log.d("ClientAndroidInterface", "Login failed", e);
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    MainActivity.SetLoggedIn();
                }
            }.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void DeleteUploadedData(final int FamilyId, ArrayList<String> FamilyIDs, int CallerId) {
        if (FamilyIDs.size() == 0) {
            FamilyIDs = new ArrayList<>() {{
                add(String.valueOf(FamilyId));
            }};
        }

        @Language("SQL")
        String wherePolicyIdIn = "PolicyId IN (SELECT PolicyId FROM tblPolicy WHERE "
                + combineFamilyIdsInWhereStatement(FamilyIDs) + ")";
        deleteUploadedTableData(SQLHandler.tblPremium, wherePolicyIdIn);
        deleteUploadedTableData(SQLHandler.tblInsureePolicy, wherePolicyIdIn);
        deleteUploadedTableData(SQLHandler.tblBulkControlNumbers, wherePolicyIdIn);
        deleteUploadedTableData(SQLHandler.tblPolicy, FamilyIDs);
        deleteUploadedTableData(SQLHandler.tblInsuree, FamilyIDs);
        deleteUploadedTableData(SQLHandler.tblFamilySMS, FamilyIDs);
        deleteUploadedTableData(SQLHandler.tblFamilies, FamilyIDs);
    }

    private void deleteUploadedTableData(String tableName, ArrayList<String> familyIDs) {
        @Language("SQL")
        String where;
        if (familyIDs.size() != 0) {
            where = combineFamilyIdsInWhereStatement(familyIDs);
        } else {
            where = " FamilyId = " + familyIDs;
        }
        deleteUploadedTableData(tableName, where);
    }

    private String combineFamilyIdsInWhereStatement(ArrayList<String> familyIDs) {
        StringBuilder combined = new StringBuilder();
        for (int j = 0; j < familyIDs.size(); j++) {
            if ((familyIDs.size() - j) == 1) {
                combined.append(" FamilyId == ").append(familyIDs.get(j));
            } else {
                combined.append(" FamilyId == ").append(familyIDs.get(j)).append(" OR");
            }
        }
        return combined.toString();
    }

    private void deleteUploadedTableData(String tableName, String where) {
        @Language("SQL")
        String query = "DELETE FROM " + tableName + " WHERE " + where;
        sqlHandler.getResult(query, null);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void uploadFeedbacks() {
        final int totalFiles;
        final ProgressDialog pd;

        if (!global.isNetworkAvailable()) {
            ShowDialog(activity.getResources().getString(R.string.NoInternet));
            return;
        }

        File feedbackDir = new File(global.getSubdirectory("Feedback"));
        File[] xmlFiles = feedbackDir.listFiles((file) -> file.getName().endsWith(".xml"));
        File[] jsonFiles = feedbackDir.listFiles((file) -> file.getName().endsWith(".json"));

        if (xmlFiles == null || jsonFiles == null) {
            ShowDialog(activity.getResources().getString(R.string.NoFiles));
            DeleteFeedBacks();
            return;
        }

        totalFiles = jsonFiles.length;

        if (totalFiles == 0) {
            ShowDialog(activity.getResources().getString(R.string.NoDataAvailable));
            DeleteFeedBacks();
            return;
        }

        pd = AndroidUtils.showProgressDialog(activity, R.string.Sync, R.string.SyncProcessing);

        new Thread(() -> {
            int uploadsAccepted = 0, uploadsRejected = 0, uploadFailed = 0;
            PostFeedback postFeedback = new PostFeedback();
            for (int i = 0; i < jsonFiles.length; i++) {
                try {
                    String jsonText = Objects.requireNonNull(FileUtils.readFileAsUTF8String(jsonFiles[i]));
                    PendingFeedback pendingFeedback = pendingFeedbackFromJSON(
                            new JSONObject(jsonText).getJSONObject("feedback")
                    );
                    postFeedback.execute(pendingFeedback);
                    uploadsAccepted += 1;
                    MoveFile(xmlFiles[i], 1);
                    MoveFile(jsonFiles[i], 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (
                            e instanceof HttpException &&
                                    ((HttpException) e).getCode() >= 400 &&
                                    ((HttpException) e).getCode() < 500
                    ) {
                        uploadsRejected += 1;
                        MoveFile(xmlFiles[i], 2);
                        MoveFile(jsonFiles[i], 2);
                    } else {
                        uploadFailed += 1;
                    }
                }
            }
            if (uploadsAccepted == 0 && uploadsRejected == 0) {
                ShowDialog(activity.getResources().getString(R.string.SomethingWrongServer));
            } else {
                ShowDialog(activity.getResources().getString(R.string.BulkUpload));
            }
            pd.dismiss();
        }).start();
    }

    private PendingFeedback pendingFeedbackFromJSON(@NonNull JSONObject object) throws JSONException {
        String answers = object.getString("Answers");
        return new PendingFeedback(
                /* claimUUID = */object.getString("ClaimUUID"),
                /* chfId = */ object.getString("CHFID"),
                /* careRendered = */ answers.charAt(0) == '1',
                /* paymentAsked = */ answers.charAt(1) == '1',
                /* drugPrescribed = */ answers.charAt(2) == '1',
                /* drugReceived = */ answers.charAt(3) == '1',
                /* assessment = */ Character.getNumericValue(answers.charAt(4))
        );
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void uploadRenewals() {
        final int totalFiles;
        final ProgressDialog pd;

        if (!global.isNetworkAvailable()) {
            ShowDialog(activity.getResources().getString(R.string.NoInternet));
            return;
        }

        File renewalDir = new File(global.getSubdirectory("Renewal"));
        File[] xmlFiles = renewalDir.listFiles((file) -> file.getName().endsWith(".xml"));
        File[] jsonFiles = renewalDir.listFiles((file) -> file.getName().endsWith(".json"));

        if (xmlFiles == null || jsonFiles == null) {
            ShowDialog(activity.getResources().getString(R.string.NoFiles));
            DeleteRenewals();
            return;
        }

        totalFiles = jsonFiles.length;

        if (totalFiles == 0) {
            ShowDialog(activity.getResources().getString(R.string.NoDataAvailable));
            DeleteRenewals();
            return;
        }

        pd = AndroidUtils.showProgressDialog(activity, R.string.Sync, R.string.SyncProcessing);

        new Thread(() -> {
            StringBuilder messageBuilder = new StringBuilder();
            String messageFormat = "[%s] %s\n";
            Map<Integer, String> messages = new HashMap<>();
            messages.put(ToRestApi.RenewalStatus.ACCEPTED, activity.getResources().getString(R.string.RenewalAccepted));
            messages.put(ToRestApi.RenewalStatus.ALREADY_ACCEPTED, activity.getResources().getString(R.string.RenewalAlreadyAccepted));
            messages.put(ToRestApi.RenewalStatus.REJECTED, activity.getResources().getString(R.string.RenewalRejected));
            messages.put(ToRestApi.RenewalStatus.DUPLICATE_RECEIPT, activity.getResources().getString(R.string.DuplicateReceiptNumber));
            messages.put(ToRestApi.RenewalStatus.GRACE_PERIOD_EXPIRED, activity.getResources().getString(R.string.GracePeriodExpired));
            messages.put(ToRestApi.RenewalStatus.CONTROL_NUMBER_ERROR, activity.getResources().getString(R.string.ControlNumberError));
            messages.put(ToRestApi.RenewalStatus.UNEXPECTED_EXCEPTION, activity.getResources().getString(R.string.UnexpectedException));

            ToRestApi rest = new ToRestApi();

            int acceptedRenewals = 0;
            for (int i = 0; i < jsonFiles.length; i++) {
                String jsonText = FileUtils.readFileAsUTF8String(jsonFiles[i]);
                if (jsonText == null) {
                    continue;
                }
                String renewalInsureeNo = "";

                HttpResponse response;
                int responseCode;
                int uploadStatus;

                try {
                    JSONObject obj = new JSONObject(jsonText).getJSONObject("Policy");
                    renewalInsureeNo = obj.getString("CHFID");
                    int renewalId = Integer.parseInt(obj.getString("RenewalId"));
                    uploadStatus = new DeletePolicyRenewal().execute(renewalId);

                } catch (JSONException e) {
                    Log.e(LOG_TAG_RENEWAL, "Invalid renewal json format", e);
                    messageBuilder.append(String.format(messageFormat, renewalInsureeNo, activity.getResources().getString(R.string.InvalidRenewalFile)));
                    continue;
                } catch (IOException e) {
                    Log.e(LOG_TAG_RENEWAL, "Error while sending renewal", e);
                    messageBuilder.append(String.format(messageFormat, renewalInsureeNo, activity.getResources().getString(R.string.SomethingWrongServer)));
                    continue;
                } catch (HttpException e) {
                    Log.e(LOG_TAG_RENEWAL, "Error while sending renewal", e);
                    if (e.getCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                        messageBuilder.append(String.format(messageFormat, renewalInsureeNo, activity.getResources().getString(R.string.NotFound)));
                        break;
                    } else if (e.getCode() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                        messageBuilder.append(String.format(messageFormat, renewalInsureeNo, activity.getResources().getString(R.string.LoginFail)));
                        break;
                    } else {
                        throw e;
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG_RENEWAL, "Error while sending renewal", e);
                    messageBuilder.append(String.format(messageFormat, renewalInsureeNo, activity.getResources().getString(R.string.InvalidRenewalFile)));
                    continue;
                }

                if (messages.containsKey(uploadStatus)) {
                    if (uploadStatus == ToRestApi.RenewalStatus.ACCEPTED || uploadStatus == ToRestApi.RenewalStatus.ALREADY_ACCEPTED) {
                        MoveFile(xmlFiles[i], 1);
                        MoveFile(jsonFiles[i], 1);
                        acceptedRenewals++;
                    } else {
                        MoveFile(xmlFiles[i], 2);
                        MoveFile(jsonFiles[i], 2);
                        messageBuilder.append(String.format(messageFormat, renewalInsureeNo, messages.get(uploadStatus)));
                    }
                }
            }

            String successMessage = activity.getResources().getString(R.string.BulkUpload);
            String failMessage = activity.getResources().getString(R.string.RenewalRejected);
            String resultMessage;
            if (acceptedRenewals == 0) {
                resultMessage = failMessage;
            } else if (acceptedRenewals != jsonFiles.length) {
                resultMessage = successMessage + "\n" + messageBuilder.toString();
            } else {
                resultMessage = successMessage;
            }

            activity.runOnUiThread(() -> AndroidUtils.showDialog(activity, resultMessage));

            pd.dismiss();
        }).start();
    }

    private File[] GetListOfImages(String DirectoryPath, final String FileName) {
        File Directory = new File(DirectoryPath);
        return Directory.listFiles((dir, filename) -> filename.equalsIgnoreCase(FileName));
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String GetListOfImagesContain(final String FileName) {
        File[] Photos = null;
        String newFileName = "";
        File Directory = new File(global.getImageFolder());

        Photos = Directory.listFiles((dir, filename) -> filename.startsWith(FileName + "_"));

        if (Photos != null && Photos.length > 0) {
            Arrays.sort(Photos, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
            newFileName = Photos[Photos.length - 1].toString();
        }
        return newFileName;
    }

    private void MoveFile(File file, int res) {
        String Accepted = "", Rejected = "";
        if (file.getName().contains("Renewal_") || file.getName().contains("RenewalJSON_")) {
            Accepted = "AcceptedRenewal";
            Rejected = "RejectedRenewal";
        } else if (file.getName().contains("feedback_") || file.getName().contains("feedbackJSON_")) {
            Accepted = "AcceptedFeedback";
            Rejected = "RejectedFeedback";
        }

        boolean status = false;
        switch (res) {
            case 1:
                status = file.renameTo(new File(global.getSubdirectory(Accepted), file.getName()));
                break;
            case 2:
                status = file.renameTo(new File(global.getSubdirectory(Rejected), file.getName()));
                break;
        }

        if (!status) {
            Log.w(Global.FILE_IO_LOG_TAG, "Moving file failed: " + file.getAbsolutePath());
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void popUpOfficerDialog() {
        ((MainActivity) activity).ShowEnrolmentOfficerDialog();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void downloadMasterData() {
        ProgressDialog pd = ProgressDialog.show(activity, activity.getResources().getString(R.string.Sync), activity.getResources().getString(R.string.DownloadingMasterData));
        new Thread(() -> {
            try {
                startDownloadingMasterData();

                activity.runOnUiThread(() -> {
                    ShowDialog(activity.getResources().getString(R.string.DataDownloadedSuccess));
                    global.setOfficerCode(null);
                    ((MainActivity) activity).ShowEnrolmentOfficerDialog();
                });
            } catch (JSONException e) {
                Log.e("MASTERDATA", "Error while parsing master data", e);
            } catch (UserException e) {
                Log.e("MASTERDATA", "Error while downloading master data", e);
                activity.runOnUiThread(() ->
                        AndroidUtils.showDialog(activity,
                                activity.getResources().getString(R.string.DataDownloadedFailed),
                                e.getMessage()));
            } catch (UserNotAuthenticatedException e) {
                activity.runOnUiThread(() ->
                        forceLoginDialogBox(/*onSuccess = */ this::downloadMasterData)
                );
            } finally {
                pd.dismiss();
            }
        }).start();
    }

    @WorkerThread
    public void importMasterData(String data) throws JSONException, UserException {
        try {
            processOldFormat(new JSONArray(data));
        } catch (JSONException e) {
            try {
                processNewFormat(new JSONObject(data));
            } catch (JSONException e2) {
                throw new UserException(activity.getResources().getString(R.string.DownloadMasterDataFailed), e2);
            }
        }
    }


    @WorkerThread
    public void startDownloadingMasterData() throws JSONException, UserException, UserNotAuthenticatedException {
        try {
            importMasterData(new FetchMasterData().execute());
        } catch (Exception e) {
            if (e instanceof UserNotAuthenticatedException) {
                throw (UserNotAuthenticatedException) e;
            }
            throw new UserException("Error while downloading the master data", e);
        }
    }

    @WorkerThread
    private void processOldFormat(@NonNull JSONArray masterData) throws UserException {
        //Sequence of table
        /*
            1   :   ConfirmationTypes
            2   :   Controls
            3   :   Education
            4   :   FamilyTypes
            5   :   HF
            6   :   IdentificationTypes
            7   :   Languages
            8   :   Locations
            9   :   Officers
            10  :   Payers
            11  :   Products
            12  :   Professions
            13  :   Relations
            14  :   PhoneDefaults
            15  :   Genders
            16  :   OfficerVillages
         */

        JSONArray ConfirmationTypes = new JSONArray();
        JSONArray Controls = new JSONArray();
        JSONArray Education = new JSONArray();
        JSONArray FamilyTypes = new JSONArray();
        JSONArray HF = new JSONArray();
        JSONArray IdentificationTypes = new JSONArray();
        JSONArray Languages = new JSONArray();
        JSONArray Locations = new JSONArray();
        JSONArray Officers = new JSONArray();
        JSONArray Payers = new JSONArray();
        JSONArray Products = new JSONArray();
        JSONArray Professions = new JSONArray();
        JSONArray Relations = new JSONArray();
        JSONArray PhoneDefaults = new JSONArray();
        JSONArray Genders = new JSONArray();
        //JSONArray OfficerVillages = new JSONArray();

        try {
            for (int i = 0; i < masterData.length(); i++) {
                String keyName = masterData.getJSONObject(i).keys().next();
                switch (keyName.toLowerCase()) {
                    case "confirmationtypes":
                        ConfirmationTypes = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "controls":
                        Controls = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "education":
                        Education = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "familytypes":
                        FamilyTypes = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "hf":
                        HF = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "identificationtypes":
                        IdentificationTypes = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "languages":
                        Languages = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "locations":
                        Locations = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "officers":
                        Officers = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "payers":
                        Payers = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "products":
                        Products = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "professions":
                        Professions = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "relations":
                        Relations = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "phonedefaults":
                        PhoneDefaults = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
                    case "genders":
                        Genders = (JSONArray) masterData.getJSONObject(i).get(keyName);
                        break;
/*                case "officersvillages":
                    OfficerVillages = (JSONArray) masterData.getJSONObject(i).get(keyName);
                    break;*/
                }
            }

            insertConfirmationTypes(ConfirmationTypes);
            insertControls(Controls);
            insertEducation(Education);
            insertFamilyTypes(FamilyTypes);
            insertHF(HF);
            insertIdentificationTypes(IdentificationTypes);
            insertLanguages(Languages);
            insertLocations(Locations);
            insertOfficers(Officers);
            insertPayers(Payers);
            insertProducts(Products);
            insertProfessions(Professions);
            insertRelations(Relations);
            insertPhoneDefaults(PhoneDefaults);
            insertGenders(Genders);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new UserException(activity.getResources().getString(R.string.DownloadMasterDataFailed), e);
        }
    }

    @WorkerThread
    private void processNewFormat(JSONObject masterData) throws UserException {
        try {
            insertConfirmationTypes((JSONArray) masterData.get("confirmationTypes"));
            insertControls((JSONArray) masterData.get("controls"));
            insertEducation((JSONArray) masterData.get("education"));
            insertFamilyTypes((JSONArray) masterData.get("familyTypes"));
            insertHF((JSONArray) masterData.get("hf"));
            insertIdentificationTypes((JSONArray) masterData.get("identificationTypes"));
            insertLanguages((JSONArray) masterData.get("languages"));
            insertLocations((JSONArray) masterData.get("locations"));
            insertOfficers((JSONArray) masterData.get("officers"));
            insertPayers((JSONArray) masterData.get("payers"));
            insertProducts((JSONArray) masterData.get("products"));
            insertProfessions((JSONArray) masterData.get("professions"));
            insertRelations((JSONArray) masterData.get("relations"));
            insertPhoneDefaults((JSONArray) masterData.get("phoneDefaults"));
            insertGenders((JSONArray) masterData.get("genders"));
        } catch (JSONException e) {
            e.printStackTrace();
            throw new UserException(activity.getResources().getString(R.string.DownloadMasterDataFailed), e);
        }
    }

    private String[] getColumnNames(JSONArray jsonArray) {
        String[] Columns = {};
        if (jsonArray.length() > 0) {
            ArrayList<String> columnsList = new ArrayList<>();
            Iterator<String> keys;
            try {
                keys = jsonArray.getJSONObject(0).keys();
                while (keys.hasNext())
                    columnsList.add(keys.next());
                Columns = columnsList.toArray(new String[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Columns;
    }

    // region Insert MasterData
    @WorkerThread
    private void insertConfirmationTypes(JSONArray jsonArray) throws JSONException {
        try {
            String[] Columns = getColumnNames(jsonArray);
            sqlHandler.insertData("tblConfirmationTypes", Columns, jsonArray, "DELETE FROM tblConfirmationTypes");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @WorkerThread
    private void insertControls(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblControls", Columns, jsonArray, "DELETE FROM tblControls;");
    }

    @WorkerThread
    private void insertEducation(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblEducations", Columns, jsonArray, "DELETE FROM tblEducations;");
    }

    @WorkerThread
    private void insertFamilyTypes(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblFamilyTypes", Columns, jsonArray, "DELETE FROM tblFamilyTypes;");
    }

    @WorkerThread
    private void insertHF(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblHF", Columns, jsonArray, "DELETE FROM tblHF;");
    }

    @WorkerThread
    private void insertIdentificationTypes(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblIdentificationTypes", Columns, jsonArray, "DELETE FROM tblIdentificationTypes;");
    }

    @WorkerThread
    private void insertLanguages(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblLanguages", Columns, jsonArray, "DELETE FROM tblLanguages;");
    }

    @WorkerThread
    private void insertLocations(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblLocations", Columns, jsonArray, "DELETE FROM tblLocations;");

    }

    @WorkerThread
    private void insertOfficers(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblOfficer", Columns, jsonArray, "DELETE FROM tblOfficer;");
    }

    @WorkerThread
    private void insertPayers(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblPayer", Columns, jsonArray, "DELETE FROM tblPayer;");
    }

    @WorkerThread
    private void insertProducts(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblProduct", Columns, jsonArray, "DELETE FROM tblProduct;");
    }

    @WorkerThread
    private void insertProfessions(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblProfessions", Columns, jsonArray, "DELETE FROM tblProfessions;");
    }

    @WorkerThread
    private void insertRelations(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblRelations", Columns, jsonArray, "DELETE FROM tblRelations;");
    }

    @WorkerThread
    private void insertPhoneDefaults(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblIMISDefaultsPhone", Columns, jsonArray, "DELETE FROM tblIMISDefaultsPhone;");
    }

    @WorkerThread
    private void insertGenders(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblGender", Columns, jsonArray, "DELETE FROM tblGender;");
    }
    // endregion Insert Master Data

    public int isMasterDataAvailable() {
        @Language("SQL")
        String Query = "SELECT * FROM tblLanguages";
        JSONArray Languages = sqlHandler.getResult(Query, null);
        return Languages.length();

    }

    public JSONArray getLanguage() {
        @Language("SQL")
        String Query = "SELECT * FROM tblLanguages";
        return sqlHandler.getResult(Query, null);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getTotalFamily() {
        @Language("SQL")
        String FamilyQuery = "SELECT count(1) Families  FROM  tblfamilies WHERE isoffline = 1 OR isoffline = 0"; // WHERE isoffline = 1 OR isoffline = 0
        JSONArray Families = sqlHandler.getResult(FamilyQuery, null);
        JSONObject object = null;
        int TotalFamilies = 0;
        try {
            object = Families.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalFamilies = object != null ? Integer.parseInt(object.getString("Families")) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalFamilies;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getTotalInsuree() {
        @Language("SQL")
        String InsureeQuery = "SELECT count(1) Insuree FROM tblInsuree WHERE isoffline !=''"; //WHERE isoffline = 1 OR isoffline = 0
        JSONArray Insuree = sqlHandler.getResult(InsureeQuery, null);
        JSONObject object = null;
        int TotalInsuree = 0;
        try {
            object = Insuree.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalInsuree = object != null ? Integer.parseInt(object.getString("Insuree")) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalInsuree;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getTotalPolicy() {
        @Language("SQL")
        String PolicyQuery = "SELECT count(1) Policies  FROM  tblPolicy WHERE isoffline = 1 OR isoffline = 0"; //WHERE isoffline = 1 OR isoffline = 0
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, null);
        JSONObject object = null;
        int TotalPolicies = 0;
        try {
            object = Policy.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPolicies = object != null ? Integer.parseInt(object.getString("Policies")) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPolicies;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getTotalPremium() {
        @Language("SQL")
        String PremiumQuery = "SELECT count(1) Premiums  FROM  tblPremium WHERE isoffline = 1 OR isoffline = 0"; // WHERE isoffline = 1 OR isoffline = 0
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        JSONObject object = null;
        int TotalPremiums = 0;
        try {
            object = Premium.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPremiums = object != null ? Integer.parseInt(object.getString("Premiums")) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

    public String getPayer(int LocationId) {
        @Language("SQL")
        String Query = "SELECT D.LocationId DistrictId , R.LocationId RegionId  FROM tblLocations V " +
                "INNER JOIN tblLocations W ON W.LocationId = V.ParentLocationId " +
                "INNER JOIN tblLocations D ON D.LocationId = W.ParentLocationId " +
                "INNER JOIN tblLocations R ON R.LocationId = D.ParentLocationId " +
                "WHERE V.LocationId = " + LocationId + " OR W.LocationId = " + LocationId +
                " OR D.LocationId = " + LocationId + " OR R.LocationId = " + LocationId;
        int RegionId = 0;
        int DistrictId = 0;

        JSONArray RD = sqlHandler.getResult(Query, null);
        JSONObject object;
        JSONArray Payers;
        if (RD.length() > 0) {
            try {
                object = RD.getJSONObject(0);
                RegionId = Integer.parseInt(object.getString("RegionId"));
                DistrictId = Integer.parseInt(object.getString("DistrictId"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            @Language("SQL")
            String PayerQuery = "SELECT PayerId, PayerName,P.LocationId FROM tblPayer P \n" +
                    "INNER JOIN uvwLocations L ON P.LocationId = L.LocationId\n" +
                    "WHERE (L.RegionId = " + RegionId + " OR L.RegionId ='null' OR L.RegionId ='') AND (L.DistrictId = " + DistrictId + " OR L.DistrictId ='null' OR L.DistrictId ='')  " +
                    "ORDER BY L.LocationId";
            Payers = sqlHandler.getResult(PayerQuery, null);
        } else {
            Payers = new JSONArray();
        }
        return Payers.toString();
    }

    public String getPayersByDistrictId(int OfficeLocationId) {
        @Language("SQL")
        String PayerQuery = "SELECT P.PayerId, P.PayerName, P.LocationId FROM tblPayer P\n" +
                " LEFT OUTER JOIN tblLocations L ON P.LocationId = L.LocationId\n" +
                " WHERE (P.LocationId = " + OfficeLocationId + " OR L.ParentLocationId = P.LocationId OR P.LocationId = 'null' OR P.LocationId = '')  " +
                " ORDER BY L.LocationId";
        JSONArray Payers = sqlHandler.getResult(PayerQuery, null);
        return Payers.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void getScannedNumber() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        try {
            activity.startActivityForResult(intent, RESULT_SCAN);
        } catch (Exception e) {
            Log.e("ENROL", "Error while trying to initiate QR scan", e);
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getOfficerLocation() {
        String officerCode = global.getOfficerCode();
        if (officerCode == null) {
            officerCode = "";
        }
        @Language("SQL")
        String Query = " SELECT RegionId, DistrictId FROM uvwLocations UL\n" +
                " INNER JOIN tblOfficer O ON O.LocationId = UL.DistrictId \n" +
                " WHERE LOWER(O.Code) = '" + officerCode.toLowerCase() + "'";
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        return jsonArray.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getFamilyPolicy(int FamilyId) {
        @Language("SQL")
        String Query = "SELECT count(1) Ins, IFNULL(Threshold,0) Threshold,IFNULL(MemberCount,0) MemberCount, IFNULL(PolicyId,0) PolicyId  FROM tblInsuree I\n" +
                "LEFT JOIN(\n" +
                "SELECT Threshold,MemberCount, PolicyId FROM tblPolicy  PL\n" +
                "INNER JOIN tblProduct PR ON PL.ProdId = PR.ProdId\n" +
                "WHERE PL.FamilyId =" + FamilyId + "\n" +
                "LIMIT 1) Policy ON 1 =1\n" +
                "WHERE I.FamilyId=" + FamilyId + " \n" +
                "GROUP BY PolicyId ";
        JSONArray FamilyPolicy = sqlHandler.getResult(Query, null);
        return FamilyPolicy.toString();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getMaxInstallments(String id) {
        int PolicyId = Integer.parseInt(id);
        JSONArray MaxInstallArray = null;

        int ProdId = getProdId(PolicyId);
        try {
            @Language("SQL")
            String Query = "SELECT MaxInstallments from tblProduct where ProdId = " + ProdId + "";
            MaxInstallArray = sqlHandler.getResult(Query, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (MaxInstallArray == null) {
            return 0;
        }
        int MaxInstallments = 0;
        for (int i = 0; i < MaxInstallArray.length(); i++) {
            try {
                JSONObject MaxObject = MaxInstallArray.getJSONObject(i);
                MaxInstallments = MaxObject.getInt("MaxInstallments");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return MaxInstallments;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getGracePeriods(String id) {
        int PolicyId = Integer.parseInt(id);
        JSONArray GracePeriodArray = null;

        int ProdId = getProdId(PolicyId);
        try {
            @Language("SQL")
            String Query = "SELECT GracePeriod from tblProduct where ProdId = " + ProdId + "";
            GracePeriodArray = sqlHandler.getResult(Query, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (GracePeriodArray == null) {
            return 0;
        }

        int gracePeriod = 0;
        for (int i = 0; i < GracePeriodArray.length(); i++) {
            try {
                JSONObject MaxObject = GracePeriodArray.getJSONObject(i);
                gracePeriod = MaxObject.getInt("GracePeriod");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return gracePeriod;
    }

    public int getProdId(int PolicyId) {
        int ProdId = 0;
        @Language("SQL")
        String Query = "SELECT ProdId from tblPolicy where PolicyId = " + PolicyId + "";
        JSONArray MaxInstallArray = sqlHandler.getResult(Query, null);
        JSONObject MaxObject = null;
        for (int i = 0; i < MaxInstallArray.length(); i++) {
            try {
                MaxObject = MaxInstallArray.getJSONObject(i);
                ProdId = MaxObject.getInt("ProdId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ProdId;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getPolicyStatus(int PolicyId) {
        int PolicyStatus = 0;
        @Language("SQL")
        String Query = "SELECT PolicyStatus from tblPolicy where PolicyId = " + PolicyId + "";
        JSONArray MaxInstallArray = sqlHandler.getResult(Query, null);
        JSONObject MaxObject = null;
        for (int i = 0; i < MaxInstallArray.length(); i++) {
            try {
                MaxObject = MaxInstallArray.getJSONObject(i);
                PolicyStatus = MaxObject.getInt("PolicyStatus");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return PolicyStatus;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void SaveInsureePolicy(int InsureId, int FamilyId, Boolean Activate, int isOffline) {
        @Language("SQL")
        String InsQuery = "SELECT count(1) TotalIns FROM tblInsuree I WHERE FamilyId =" + FamilyId;
        JSONArray InsArray = sqlHandler.getResult(InsQuery, null);
        JSONObject InsObject = null;
        int TotalIns = 0;
        int MaxInsureePolicyId = 0;
        int PolicyId = 0;
        double NewPolicyValue = 0;
        String EffectiveDate = null;
        String StartDate = null;
        String PolicyStage = null;
        String EnrollmentDate = null;
        String EnrollDate = null;
        String ExpiryDate = null;
        int MaxMember = 0;
        int ProdID = 0;
        boolean HasCycle = false;
        try {
            InsObject = InsArray.getJSONObject(0);
            TotalIns = Integer.parseInt(InsObject.getString("TotalIns"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        @Language("SQL")
        String PolicyQuery = " SELECT PolicyId,PolicyValue,EffectiveDate,PolicyStage,ProdID,StartDate, EnrollDate,isOffline FROM tblPolicy WHERE FamilyID  =  " + FamilyId;
        JSONArray PolicyArray = sqlHandler.getResult(PolicyQuery, null);
        JSONObject PolicyObject = null;
        for (int i = 0; i < PolicyArray.length(); i++) {
            try {
                PolicyObject = PolicyArray.getJSONObject(i);
                PolicyId = Integer.parseInt(PolicyObject.getString("PolicyId"));
                EffectiveDate = (PolicyObject.getString("EffectiveDate"));
                PolicyStage = PolicyObject.getString("PolicyStage");
                StartDate = PolicyObject.getString("StartDate");
                ProdID = Integer.parseInt(PolicyObject.getString("ProdId"));
                EnrollmentDate = PolicyObject.getString("EnrollDate");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            @Language("SQL")
            String MemberCount = "SELECT MemberCount,StartCycle1 FROM tblProduct WHERE ProdId =" + ProdID;
            JSONArray MCArray = sqlHandler.getResult(MemberCount, null);
            JSONObject MCObject = null;
            try {
                MCObject = MCArray.getJSONObject(0);
                MaxMember = Integer.parseInt(MCObject.getString("MemberCount"));
                if ((!TextUtils.isEmpty(MCObject.getString("StartCycle1"))) && (!MCObject.getString("StartCycle1").equals("null"))) {
                    HasCycle = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (MaxMember >= TotalIns) {
                if (!Activate) EffectiveDate = null;

                @Language("SQL")
                String MaxIdQuery = "SELECT  Count(InsureePolicyId)+1  InsureePolicyId  FROM tblInsureePolicy";
                JSONArray JsonA = sqlHandler.getResult(MaxIdQuery, null);
                try {
                    JSONObject JmaxOb = JsonA.getJSONObject(0);
                    MaxInsureePolicyId = JmaxOb.getInt("InsureePolicyId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                ContentValues values = new ContentValues();
                @Language("SQL")
                String PolicyQuery2 = " SELECT StartDate,EnrollDate,ExpiryDate FROM tblPolicy WHERE PolicyID =" + PolicyId;
                JSONArray PolicyArray2 = sqlHandler.getResult(PolicyQuery2, null);
                JSONObject PolicyObject2 = null;
                try {
                    PolicyObject2 = PolicyArray2.getJSONObject(0);
                    StartDate = PolicyObject2.getString("StartDate");
                    ExpiryDate = PolicyObject2.getString("ExpiryDate");
                    EnrollDate = PolicyObject2.getString("EnrollDate");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                values.put("InsureePolicyId", MaxInsureePolicyId);
                values.put("InsureeId", InsureId);
                values.put("PolicyId", PolicyId);
                values.put("EnrollmentDate", EnrollDate);
                values.put("StartDate", StartDate);
                values.put("EffectiveDate", EffectiveDate);
                values.put("ExpiryDate", ExpiryDate);
                values.put("isOffline", isOffline);

                sqlHandler.insertData("tblInsureePolicy", values);

            }
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void InsertPolicyInsuree(int PolicyId, int IsOffline) {
        int MaxMember = 0;
        int MaxInsureePolicyId = 0;
        @Language("SQL")
        String MaxIdQuery = "SELECT  Count(InsureePolicyId)+1  InsureePolicyId  FROM tblInsureePolicy";
        JSONArray JsonA = sqlHandler.getResult(MaxIdQuery, null);
        try {
            JSONObject JmaxOb = JsonA.getJSONObject(0);
            MaxInsureePolicyId = JmaxOb.getInt("InsureePolicyId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        @Language("SQL")
        String MemberCount = "SELECT MemberCount FROM tblProduct Prod  \n" +
                " INNER JOIN tblPolicy P ON P.ProdId =Prod.ProdId \n" +
                " WHERE PolicyId =" + PolicyId + " LIMIT 1";
        JSONArray MCArray = sqlHandler.getResult(MemberCount, null);
        try {
            JSONObject MCObject = MCArray.getJSONObject(0);
            MaxMember = Integer.parseInt(MCObject.getString("MemberCount"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        @Language("SQL")
        String SavePolicyInsuree = "INSERT INTO tblInsureePolicy(InsureePolicyId,InsureeId,PolicyId,EnrollmentDate,StartDate,EffectiveDate,ExpiryDate,isOffline)\n" +
                "SELECT " + MaxInsureePolicyId + ",  InsureeId ,PolicyId ,EnrollDate,StartDate, EffectiveDate ,ExpiryDate,I.isOffline FROM tblPolicy P\n" +
                "INNER JOIN tblInsuree I ON  I.FamilyId = P.FamilyId\n" +
                "WHERE PolicyID = " + PolicyId + "\n" +
                "LIMIT " + MaxMember;
        sqlHandler.getResult(SavePolicyInsuree, null);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void UpdateInsureePolicy(int PolicyId) {//herman new
        ContentValues values = new ContentValues();
        @Language("SQL")
        String PolicyQuery = "SELECT EffectiveDate FROM tblPolicy WHERE PolicyId = " + PolicyId;
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, null);
        String EffectiveDate = null;
        JSONObject O = null;
        try {
            O = Policy.getJSONObject(0);
            EffectiveDate = O.getString("EffectiveDate");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        values.put("EffectiveDate", EffectiveDate);

        try {
            sqlHandler.updateData("tblInsureePolicy", values, "PolicyId = ?", new String[]{String.valueOf(PolicyId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }

    public File[] getPhotos() {
        String path = activity.getApplicationInfo().dataDir + "/Images/";
        File Directory = new File(path);
        FilenameFilter filter = (dir, filename) -> filename.contains("0");
        return Directory.listFiles(filter);
    }

    private SecretKeySpec generateKey(String encPassword) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = encPassword.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }

    public String encryptRarPwd(String dataToEncrypt, String encPassword) throws Exception {
        SecretKeySpec key = generateKey(encPassword);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(dataToEncrypt.getBytes());
        return Base64.encodeToString(encVal, Base64.DEFAULT);
    }

    public String decryptRarPwd(String dataToDecrypt, String decPassword) throws Exception {
        SecretKeySpec key = generateKey(decPassword);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(dataToDecrypt, Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue);
    }

    public String generateSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void SaveRarPassword(String password) {
        try {
            SharedPreferences sharedPreferences = activity.getApplicationContext().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String salt = generateSalt();
            String trimSalt = salt.trim();
            String encryptedPassword = encryptRarPwd(password, trimSalt);
            String trimEncryptedPassword = encryptedPassword.trim();
            editor.putString("rarPwd", trimEncryptedPassword);
            editor.putString("salt", trimSalt);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void BackToDefaultRarPassword() {
        try {
            String defaultRarPassword = AppInformation.DomainInfo.getDefaultRarPassword();
            SharedPreferences sharedPreferences = activity.getApplicationContext().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String salt = generateSalt();
            String trimSalt = salt.trim();
            String encryptedPassword = encryptRarPwd(defaultRarPassword, trimSalt);
            String trimEncryptedPassword = encryptedPassword.trim();
            editor.putString("rarPwd", trimEncryptedPassword);
            editor.putString("salt", trimSalt);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int ModifyFamily(final String insuranceNumber) {
        inProgress = true;

        int insureeCount = sqlHandler.getCount("tblInsuree", "Trim(CHFID) = ?", new String[]{insuranceNumber});
        if (insureeCount > 0) {
            ShowDialog(activity.getResources().getString(R.string.FamilyExists));
            return 0;
        } else {
            try {
                Family family = new FetchFamily().execute(insuranceNumber);
                InsertFamilyDataFromOnline(family);
                InsertInsureeDataFromOnline(family.getMembers());
                return 1;
            } catch (Exception e) {
                Log.e("MODIFYFAMILY", "Error while downloading a family", e);
                if (e instanceof HttpException && ((HttpException) e).getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    ShowDialog(activity.getResources().getString(R.string.InsuranceNumberNotFound));
                } else {
                    ShowDialog(activity.getResources().getString(R.string.SomethingWrongServer) + ": " + e.getMessage());
                }
            }
        }

        return 0;
    }

    private void InsertFamilyDataFromOnline(@NonNull Family family) throws JSONException {
        @Language("SQL")
        String QueryCheck = "SELECT FamilyUUID FROM tblFamilies WHERE FamilyUUID = '" + family.getUuid() + "' AND (isOffline IS false OR isOffline = 0 OR isOffline = 2)";
        if (sqlHandler.getResult(QueryCheck, null).length() == 0) {
            String[] Columns = {"familyId", "familyUUID", "insureeId", "insureeUUID", "locationId", "poverty", "isOffline", "familyType",
                    "familyAddress", "ethnicity", "confirmationNo", "confirmationType"};
            sqlHandler.insertData("tblFamilies", Columns, toJSONArray(family), "");

            if (family.getSms() != null) {
                try {
                    addOrUpdateFamilySms(family.getId(),
                            family.getSms().isApproval(),
                            family.getSms().getLanguage()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w("ModifyFamily", "No familySMS data in family payload");
                }
            }
        }
    }

    @NonNull
    private JSONArray toJSONArray(@NonNull Family family) throws JSONException {
        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("familyId", family.getId());
        jsonObject.put("familyUUID", family.getUuid());
        jsonObject.put("insureeId", family.getHead().getId());
        jsonObject.put("insureeUUID", family.getHead().getUuid());
        jsonObject.put("locationId", family.getLocationId());
        jsonObject.put("poverty", family.isPoor());
        jsonObject.put("isOffline", family.isOffline());
        jsonObject.put("familyType", family.getType());
        jsonObject.put("familyAddress", family.getAddress());
        jsonObject.put("ethnicity", family.getEthnicity());
        jsonObject.put("confirmationNo", family.getConfirmationNumber());
        jsonObject.put("confirmationType", family.getConfirmationType());
        array.put(jsonObject);
        return array;
    }


    private void InsertInsureeDataFromOnline(@NonNull List<Family.Member> members) throws JSONException {
        JSONArray array = new JSONArray();
        for (Family.Member member : members) {
            @Language("SQL")
            String QueryCheck = "SELECT InsureeUUID FROM tblInsuree WHERE Trim(CHFID) = '" + member.getChfId() + "' AND (isOffline IS false OR isOffline = 0 OR isOffline = 2)";
            if (sqlHandler.getResult(QueryCheck, null).length() == 0) {
                array.put(toJSONObject(member));
            }
        }
        String[] Columns = {"identificationNumber", "familyId", "insureeId", "insureeUUID", "familyUUID", "chfid", "lastName", "otherNames", "dob", "gender", "marital", "isHead", "phone", "photoPath", "cardIssued",
                "isOffline", "relationship", "profession", "education", "email", "typeOfId", "hfid", "currentAddress", "geoLocation", "curVillage"};
        sqlHandler.insertData("tblInsuree", Columns, array, "");
    }

    @NonNull
    private JSONObject toJSONObject(@NonNull Family.Member member) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identificationNumber", member.getIdentificationNumber());
        jsonObject.put("familyId", member.getFamilyId());
        jsonObject.put("insureeId", member.getId());
        jsonObject.put("insureeUUID", member.getUuid());
        jsonObject.put("familyUUID", member.getFamilyUuid());
        jsonObject.put("chfid", member.getChfId());
        jsonObject.put("lastName", member.getLastName());
        jsonObject.put("otherNames", member.getOtherNames());
        jsonObject.put("dob", DateUtils.toDateString(member.getDateOfBirth()));
        jsonObject.put("gender", member.getGender());
        jsonObject.put("marital", member.getMarital());
        jsonObject.put("isHead", member.isHead());
        jsonObject.put("phone", member.getPhone());
        jsonObject.put("photoPath", member.getPhotoPath());
        jsonObject.put("cardIssued", member.isCardIssued());
        jsonObject.put("isOffline", member.isOffline());
        jsonObject.put("relationship", member.getRelationship());
        jsonObject.put("profession", member.getProfession());
        jsonObject.put("education", member.getEducation());
        jsonObject.put("email", member.getEmail());
        jsonObject.put("typeOfId", member.getTypeOfId());
        jsonObject.put("hfid", member.getHealthFacilityId());
        jsonObject.put("currentAddress", member.getCurrentAddress());
        jsonObject.put("geoLocation", member.getGeolocation());
        jsonObject.put("curVillage", member.getCurrentVillage());
        return jsonObject;
    }

    //****************************Online Statistics ******************************//
    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getTotalFamilyOnline() {
        @Language("SQL")
        String FamilyQuery = "SELECT count(1) Families FROM tblfamilies F INNER JOIN tblInsuree I ON F.FamilyId = I.FamilyId WHERE F.isOffline = 0 AND (F.FamilyId < 0 OR I.InsureeId < 0) Group By F.FamilyId";
        JSONArray Families = sqlHandler.getResult(FamilyQuery, null);
        int TotalFamilies = 0;
        try {
            if (Families.length() != 0) {
                JSONObject object = Families.getJSONObject(0);
                TotalFamilies = object != null ? Integer.parseInt(object.getString("Families")) : 0;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalFamilies;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getTotalInsureeOnline() {
        @Language("SQL")
        String InsureeQuery = "SELECT count(1) Insuree  FROM tblInsuree WHERE isOffline = 0 AND InsureeId < 0";
        JSONArray Insuree = sqlHandler.getResult(InsureeQuery, null);
        int TotalInsuree = 0;
        try {
            JSONObject object = Insuree.getJSONObject(0);
            TotalInsuree = object != null ? Integer.parseInt(object.getString("Insuree")) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalInsuree;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getTotalPolicyOnline() {
        @Language("SQL")
        String PolicyQuery = "SELECT count(1) Policies  FROM  tblPolicy WHERE isOffline = 0 ";
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, null);
        int TotalPolicies = 0;
        try {
            JSONObject object = Policy.getJSONObject(0);
            TotalPolicies = object != null ? Integer.parseInt(object.getString("Policies")) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPolicies;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getTotalPremiumOnline() {
        @Language("SQL")
        String PremiumQuery = "SELECT count(1) Premiums  FROM  tblPremium  WHERE isOffline = 0 ";
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        int TotalPremiums = 0;
        try {
            JSONObject object = Premium.getJSONObject(0);
            TotalPremiums = object != null ? Integer.parseInt(object.getString("Premiums")) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getCountPremiums(String id) {
        int PolicyId = Integer.parseInt(id);
        @Language("SQL")
        String PremiumQuery = "SELECT count(1) Premiums  FROM  tblPremium  WHERE isPhotoFee = 'false' AND PolicyId = " + PolicyId + "";
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        int TotalPremiums = 0;
        try {
            JSONObject object = Premium.getJSONObject(0);
            TotalPremiums = object != null ? Integer.parseInt(object.getString("Premiums")) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getSumPremium() {
        @Language("SQL")
        String PremiumQuery = "SELECT SUM(Amount) FROM tblPremium WHERE isOffline = 1";
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        String TotalPremiums = "0";
        try {
            if (Premium.length() == 0) {
                TotalPremiums = "0.00";
            } else {
                JSONObject object = Premium.getJSONObject(0);
                if (object != null) {
                    String amt = object.getString("SUM(Amount)");
                    if (!"".equals(amt)) {
                        int Amount = Integer.parseInt(amt);
                        String number = String.valueOf(Amount);
                        double amount = Double.parseDouble(number);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            DecimalFormat formatter = new DecimalFormat("#,###.00");
                            TotalPremiums = formatter.format(amount);
                        }
                    } else {
                        return TotalPremiums;
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

    public int getLocationId() throws JSONException {
        @Language("SQL")
        String Query = "SELECT LocationId FROM tblOfficer WHERE Code = '" + global.getOfficerCode() + "'";
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        JSONObject object = jsonArray.getJSONObject(0);
        return object.getInt("LocationId");
    }

    public int getLocationId(String OfficerCode) {
        @Language("SQL")
        String Query = "SELECT LocationId FROM tblOfficer WHERE LOWER(Code)=? ";
        String[] arg = {OfficerCode.toLowerCase()};
        JSONArray Renews = sqlHandler.getResult(Query, arg);
        int locationId = 0;
        JSONObject O = null;
        if (Renews.length() > 0) {
            try {
                O = Renews.getJSONObject(0);
                locationId = Integer.parseInt(O.getString("LocationId"));
            } catch (JSONException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return locationId;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getFamilyStat(int FamilyId) {
        int status = 0;
        try {
            status = getFamilyStatus(FamilyId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;
    }

    private int getFamilyStatus(int FamilyId) throws JSONException {
        if (FamilyId < 0) return 0;
        @Language("SQL")
        String Query = "SELECT isOffline FROM tblFamilies WHERE FamilyId = " + FamilyId;
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        if (jsonArray.length() == 0) return 1;
        JSONObject object = jsonArray.getJSONObject(0);

        String s1 = object.getString("isOffline");
        if (s1.equals("true") || s1.equals("1")) return 1;
        else return 0;
    }

    private int getInsureeStatus(int InsureeId) throws JSONException {//herman
        if (InsureeId == 0) return 1;
        @Language("SQL")
        String Query = "SELECT isOffline FROM tblInsuree WHERE InsureeId = " + InsureeId;
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        if (jsonArray.length() == 0) {
            return 1;
        } else {
            JSONObject object = jsonArray.getJSONObject(0);

            String s1 = object.getString("isOffline");
            if (s1.equals("true") || s1.equals("1")) return 1;
            else return 0;
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int DeleteOnlineData(final int Id, final String DeleteInfo) {
        int DataDeleted = 0;
        try {
            Toast.makeText(activity, "Please wait...", Toast.LENGTH_LONG).show();
            DataDeleted = 1;
            if (DeleteInfo.equalsIgnoreCase("F")) DeleteFamily(Id);//Enrollment page
            if (DeleteInfo.equalsIgnoreCase("I")) DeleteInsuree(Id);//family and insuree page
            Toast.makeText(activity, activity.getResources().getString(R.string.dataDeleted), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return DataDeleted;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public int getOfficerId() {
        return global.getOfficerId();
    }

    //Added by Salumu 12/12/2017 to delete insuree policy
    public void DeleteInsureePolicy(int PolicyId, int InsureeId) {
        try {
            String TableName = "tblInsureePolicy";
            String WhereClause = "PolicyId=" + PolicyId + " OR InsureeId=" + InsureeId + "";
            sqlHandler.deleteData(TableName, WhereClause, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean getRule(String rulename) {
        return getRule(rulename, false);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean getRule(String rulename, boolean defaultValue) {
        boolean rule = false;
        @Language("SQL")
        String Query = "SELECT RuleValue FROM tblIMISDefaultsPhone WHERE RuleName=?";
        String[] arg = {rulename};
        JSONArray rulevalue = sqlHandler.getResult(Query, arg);

        try {
            if (rulevalue.length() > 0) {
                JSONObject RuleObject = rulevalue.getJSONObject(0);

                rule = RuleObject.getBoolean("RuleValue");
            } else {
                rule = defaultValue;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rule;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean CheckInternetAvailable() {
        if (!global.isNetworkAvailable()) {
            ShowDialog(activity.getResources().getString(R.string.NoInternet));
            return false;
        }
        return true;
    }

    @MainThread
    public void forceLoginDialogBox(@NonNull final Runnable onSuccess) {
        showLoginDialogBox(onSuccess, () -> forceLoginDialogBox(onSuccess));
    }

    @MainThread
    public void showLoginDialogBox(@Nullable final Runnable onSuccess, @Nullable final Runnable onError) {
        if (!CheckInternetAvailable()) {
            AndroidUtils.showDialog(activity, R.string.NoInternet);
            if (onError != null) {
                onError.run();
            }
            return;
        }
        LayoutInflater li = LayoutInflater.from(activity);
        View promptsView = li.inflate(R.layout.login_dialog, null);
        final TextView username = promptsView.findViewById(R.id.UserName);
        final TextView password = promptsView.findViewById(R.id.Password);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(
                        R.string.Ok,
                        (dialog, id) -> {
                            if (!username.getText().toString().equals("") || !password.getText().toString().equals("")) {
                                boolean isUserLogged = LoginToken(username.getText().toString(), password.getText().toString());
                                if (isUserLogged) {
                                    if (onSuccess != null) {
                                        onSuccess.run();
                                    }
                                } else {
                                    AndroidUtils.showConfirmDialog(
                                            activity, R.string.LoginFail,
                                            (d, w) -> {
                                                if (onError != null) {
                                                    onError.run();
                                                }
                                            }
                                    );
                                }
                            } else {
                                Toast.makeText(activity, "Please enter user name and password", Toast.LENGTH_LONG).show();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getOfficerCode() {
        return global.getOfficerCode();
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void launchPayment() {
        Intent intent = new Intent(activity, PaymentOverview.class);
        activity.startActivity(intent);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void launchActivity(String activity) {
        if (activity.equals("Enquire")) {
            Intent intent = new Intent(this.activity, Enquire.class);
            this.activity.startActivity(intent);
        }
        if (activity.equals("Renewals")) {
            Intent intent = new Intent(this.activity, RenewList.class);
            this.activity.startActivity(intent);
        }
        if (activity.equals("Feedbacks")) {
            Intent intent = new Intent(this.activity, FeedbackList.class);
            this.activity.startActivity(intent);
        }
        if (activity.equals("Reports")) {
            Intent intent = new Intent(this.activity, Reports.class);
            this.activity.startActivity(intent);
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getSelectedLanguage() {
        return ((MainActivity) activity).getSelectedLanguage();
    }

    public boolean isProductsListUnique(JSONObject policies) {
        ArrayList<String> productCodes = new ArrayList<>();
        try {
            JSONArray policiesArray = policies.getJSONArray("policies");
            for (int i = 0; i < policiesArray.length(); i++) {
                JSONObject policyObject = policiesArray.getJSONObject(i);
                String productCode = policyObject.getString("insurance_product_code");
                productCodes.add(productCode);
            }
            Set<String> nonRepeatedCodes = new HashSet<>(productCodes);
            if (nonRepeatedCodes.size() != 1) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public JSONArray getNotEnrolledPolicies(String insuranceNumber, String insuranceProduct, String renewal) {
        String aRenewal = "";
        if (!renewal.equals("")) {
            aRenewal = " AND isDone == '" + renewal + "'";
        }

        @Language("SQL")
        String query = "SELECT * FROM tblRecordedPolicies WHERE InsuranceNumber LIKE '%" + insuranceNumber +
                "%' AND ProductCode LIKE '%" + insuranceProduct + "%' " + aRenewal + " AND UploadedDate == ''";
        return sqlHandler.getResult(query, null);
    }

    private int getNextAvailablePolicyId() {
        return getMaxIdFromTable("PolicyId", "tblPolicy");
    }

    private int getNextAvailableInsureeId() {
        return getMaxIdFromTable("InsureeId", "tblInsuree");
    }

    private int getNextAvailableFamilyId() {
        return getMaxIdFromTable("FamilyId", "tblFamilies");
    }

    private int getNextAvailablePremiumId() {
        return getMaxIdFromTable("PremiumId", "tblPremium");
    }

    private int getMaxIdFromTable(String idFieldName, String tableName) {
        String query = String.format("SELECT  IFNULL(MAX(ABS(%s)),0)+1  %s  FROM %s",
                idFieldName, idFieldName, tableName);
        JSONArray JsonMaxPolicy = sqlHandler.getResult(query, null);
        try {
            JSONObject JmaxPolicyOb = JsonMaxPolicy.getJSONObject(0);
            return JmaxPolicyOb.getInt(idFieldName);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new SQLException(
                    "Couldn't get max id " + idFieldName +
                            " for table " + tableName);
        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public boolean isLoggingEnabled() {
        return Log.isLoggingEnabled;
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void clearLogs() {
        AndroidUtils.showConfirmDialog(
                activity,
                R.string.ConfirmClearLogs,
                (d, i) -> new Thread(Log::deleteLogFiles).start()
        );
    }

    @JavascriptInterface
    public void exportLogs() {
        AndroidUtils.showConfirmDialog(
                activity,
                R.string.ConfirmExportLogs,
                (d, i) -> new Thread(() -> Log.zipLogFiles(activity)).start()
        );
    }
}
