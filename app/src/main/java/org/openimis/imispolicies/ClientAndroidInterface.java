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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;

import org.apache.commons.io.IOUtils;
import org.openimis.imispolicies.util.UriUtils;
import org.openimis.imispolicies.util.ZipUtils;
import org.openimis.imispolicies.tools.ImageManager;
import org.openimis.imispolicies.tools.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.exact.CallSoap.CallSoap;
import com.exact.InsureeImages;
import com.exact.uploadfile.UploadFile;
import com.google.zxing.client.android.CaptureActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Set;
import java.util.regex.Pattern;

import org.openimis.imispolicies.tools.StorageManager;
import org.openimis.imispolicies.util.AndroidUtils;
import org.openimis.imispolicies.util.FileUtils;
import org.openimis.imispolicies.util.JsonUtils;
import org.openimis.imispolicies.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;
import static android.provider.MediaStore.EXTRA_OUTPUT;

public class ClientAndroidInterface {
    private static final String LOG_TAG_RENEWAL = "RENEWAL";

    private Context mContext;

    private SQLHandler sqlHandler;
    private Global global;
    private int Uploaded;
    private HashMap<String, String> controls = new HashMap<>();
    private String Path;
    private JSONArray Attachments = new JSONArray();
    private JSONArray TempAttachments = new JSONArray();
    private File[] files;
    private int result;
    private int UserId;
    public static boolean Activate = false;
    private int IsFamilyAvailable;
    private int DataDeleted;
    private int rtPolicyId = 0;
    private int rtPremiumId = 0;
    private int rtInsureeId = 0;
    private int rtEnrolledId = 0;
    private int enrol_result;
    private String resu;
    private String fname = null;
    private Bitmap theImage;

    private SQLiteDatabase db;

    static public String filePath = null;

    EnrollmentReport enrollmentReport;

    private ArrayList<String> mylist = new ArrayList<>();
    private String salt;

    Picasso picassoInstance;
    Target imageTarget;
    StorageManager storageManager;


    ClientAndroidInterface(Context c) {
        mContext = c;
        global = (Global) mContext.getApplicationContext();
        sqlHandler = new SQLHandler(c);
        getControls();
        SQLiteDatabase database = sqlHandler.getReadableDatabase();
        Path = global.getAppDirectory();
        filePath = database.getPath();
        storageManager = StorageManager.of(mContext);
        database.close();
        // activity = (Activity) c.getApplicationContext();
        picassoInstance = new Picasso.Builder(mContext)
                .listener((picasso, path, exception) -> Log.e("Images", String.format("Image load failed: %s", path.toString()), exception))
                .loggingEnabled(BuildConfig.LOGGING_ENABLED)
                .build();
    }


    @JavascriptInterface
    public void SetUrl(String Url) {
        global.setCurrentUrl(Url);
    }

    @JavascriptInterface
    public void showAttachmentDialog() {

        ((MainActivity) mContext).PickAttachmentDialogFromPage();
    }

    private void getControls() {
        String tableName = "tblControls";
        String[] columns = {"FieldName", "Adjustibility"};
        String where = null;
        String OrderBy = null;

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
        String OrderBy = null;
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
    public String getSpecificControlHtml(String FieldName) {

        String tableName = "tblControls";
        String[] columns = {"Adjustibility"};
        String where = "FieldName = '" + FieldName + "'";
        String OrderBy = null;
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
    public String GetSystemImageFolder() {
        return global.getImageFolder();
    }

    @JavascriptInterface
    public String getControl(String ctl) {
        if (controls.get(ctl) == null) {
            getControls();
        }
        return controls.get(ctl);
    }

    @JavascriptInterface
    public void ShowToast(String msg) {
        AndroidUtils.showToast(mContext, msg);
    }

    @JavascriptInterface
    public AlertDialog ShowDialog(String msg) {
        return AndroidUtils.showDialog(mContext, msg);
    }

    @JavascriptInterface
    public AlertDialog ShowDialogYesNo(final int InsureId, final int FamilyId, Boolean Activate, final int isOffline) {
        return new AlertDialog.Builder(mContext)
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

    @JavascriptInterface
    public String getString(String str) {
        try {
            Resources resources = mContext.getResources();
            return resources.getString(resources.getIdentifier(str, "string", mContext.getPackageName()));
        } catch (Resources.NotFoundException e) {
            Log.e("RESOURCES", String.format("Resource \"%s\" not found", str), e);
        }
        return "";
    }

    @JavascriptInterface
    public String getStringWithArgument(String str, String arg) {
        try {
            Resources resources = mContext.getResources();
            return resources.getString(resources.getIdentifier(str, "string", mContext.getPackageName()), arg);
        } catch (Resources.NotFoundException e) {
            Log.e("RESOURCES", String.format("Resource \"%s\" not found", str), e);
        }
        return "";
    }


    @JavascriptInterface
    public boolean isValidInsuranceNumber(String InsuranceNumber) {
        Escape escape = new Escape();
        int validInsuranceNumber = escape.CheckInsuranceNumber(InsuranceNumber);
        if (validInsuranceNumber != 0) {
            ShowDialog(mContext.getResources().getString(validInsuranceNumber));
            return false;
        }
        return true;
    }

    //get Region Without Officer
    @JavascriptInterface
    public String getRegionsWO() {
        global = (Global) mContext.getApplicationContext();
        String Query = "SELECT LocationId,LocationName FROM tblLocations WHERE LocationType = 'R'";
        JSONArray Regions = sqlHandler.getResult(Query, null);

        return Regions.toString();
    }

    //get District Without Officer
    @JavascriptInterface
    public String getDistrictsWO(int RegionId) {

        String Query = "SELECT  LocationId,LocationName FROM tblLocations \n" +
                "WHERE LocationType = 'D' AND ParentLocationId = " + RegionId;
        JSONArray Districts = sqlHandler.getResult(Query, null);
        return Districts.toString();
    }


    @JavascriptInterface
    public String getRegions() {
        global = (Global) mContext.getApplicationContext();
        String Query = "SELECT LocationId, LocationName FROM tblLocations WHERE LocationId = (SELECT L.ParentLocationId LocationId FROM tblLocations L\n" +
                "INNER JOIN tblOfficer O ON L.LocationId = O.LocationId\n" +
                "WHERE LOWER(O.Code) = '" + global.getOfficerCode().toLowerCase() + "')";
        JSONArray Regions = sqlHandler.getResult(Query, null);

        return Regions.toString();
    }

    @JavascriptInterface
    public String getDistricts(int RegionId) {
        global = (Global) mContext.getApplicationContext();
//        String tableName = "tblLocations";
//        String[] columns = {"LocationId", "LocationName"};
//        String where = "LocationType = 'D' AND ParentLocationId = " + RegionId;
        String Query = "SELECT * FROM tblLocations L\n" +
                "INNER JOIN tblOfficer O ON L.LocationId = O.LocationId\n" +
                "WHERE LOWER(O.Code) = '" + global.getOfficerCode().toLowerCase() + "' AND LocationType = 'D' AND ParentLocationId = " + RegionId;
        JSONArray Districts = sqlHandler.getResult(Query, null);
        return Districts.toString();
    }

    @JavascriptInterface
    public String getDistricts() {
        String tableName = "tblLocations";
        String[] columns = {"LocationId", "LocationName"};
        String where = "LocationType = 'D'";

        JSONArray Districts = sqlHandler.getResult(tableName, columns, where, null);

        return Districts.toString();
    }

    @JavascriptInterface
    public String getWards(int DistrictId) {
        String tableName = "tblLocations";
        String[] columns = {"LocationId", "LocationName"};
        String where = "LocationType = 'W' AND ParentLocationId = " + DistrictId;

        JSONArray Wards = sqlHandler.getResult(tableName, columns, where, null);

        return Wards.toString();
    }

    @JavascriptInterface
    public String getWardsOfficer(String OfficerCode) {
        JSONArray Wards = null;
        String tableName = "tblOfficerVillages";
        String[] columns = {"DISTINCT(WardID) WardID", "ward"};
        String where = " LOWER(code) = '" + OfficerCode.toLowerCase() + "'";
        try {
            Wards = sqlHandler.getResult(tableName, columns, where, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Wards.toString();
    }

    @JavascriptInterface
    public String getVillages(int WardId) {
        String tableName = "tblLocations";
        String[] columns = {"LocationId", "LocationName"};
        String where = "LocationType = 'V' AND ParentLocationId = " + WardId;

        JSONArray Villages = sqlHandler.getResult(tableName, columns, where, null);

        return Villages.toString();
    }

    @JavascriptInterface
    public String getVillagesOfficer(String WardID) {
        JSONArray Villages = null;
        String tableName = "tblOfficerVillages";
        String[] columns = {"DISTINCT(LocationId)", "village"};
        String where = " WardID = " + Integer.parseInt(WardID);

        try {
            Villages = sqlHandler.getResult(tableName, columns, where, null);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return Villages.toString();
    }

    @JavascriptInterface
    public String getYesNo() {
        JSONArray YesNo = new JSONArray();


        try {
            JSONObject object = new JSONObject();
            object.put("key", mContext.getResources().getString(R.string.Yes));
            object.put("value", 1);
            YesNo.put(object);

            object = new JSONObject();
            object.put("key", mContext.getResources().getString(R.string.No));
            object.put("value", 2);
            YesNo.put(object);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return YesNo.toString();
    }

    @JavascriptInterface
    public String getConfirmationTypes() {
        String tableName = "tblConfirmationTypes";
        String[] columns = {"ConfirmationTypeCode", "ConfirmationType", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray ConfirmationTypes = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return ConfirmationTypes.toString();
    }

    @JavascriptInterface
    public String getGroupTypes() {
        String tableName = "tblFamilyTypes";
        String[] columns = {"FamilyTypeCode", "FamilyType", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray GroupTypes = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return GroupTypes.toString();
    }

    @JavascriptInterface
    public String getLanguagesOfSMS() {
        String tableName = "tblLanguages";
        String[] columns = {"LanguageCode", "LanguageName"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray GroupTypes = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return GroupTypes.toString();
    }

    @JavascriptInterface
    public String getVulnerability() {
        JSONArray selectJsonArray = new JSONArray();
        try {
            JSONObject object = new JSONObject();
            object.put("key", mContext.getResources().getString(R.string.Yes));
            object.put("value", 1);
            selectJsonArray.put(object);

            object = new JSONObject();
            object.put("key", mContext.getResources().getString(R.string.No));
            object.put("value", 0);
            selectJsonArray.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return selectJsonArray.toString();
    }

    @JavascriptInterface
    public String getApprovalOfSMS() {
        JSONArray selectJsonArray = new JSONArray();
        try {
            JSONObject object = new JSONObject();
            object.put("key", mContext.getResources().getString(R.string.Yes));
            object.put("value", 1);
            selectJsonArray.put(object);

            object = new JSONObject();
            object.put("key", mContext.getResources().getString(R.string.No));
            object.put("value", 0);
            selectJsonArray.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return selectJsonArray.toString();
    }

    @JavascriptInterface
    public String getGender() {
        String tableName = "tblGender";
        String[] columns = {"Code", "Gender", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Gender = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Gender.toString();
    }

/*    @JavascriptInterface
    public String getGender() {
        JSONArray Gender = new JSONArray();

        JSONObject object = new JSONObject();
        try {
            object.put("Code", "");
            object.put("Gender", mContext.getResources().getString(R.string.SelectGender));
            Gender.put(object);

            object = new JSONObject();
            object.put("Code", "M");
            object.put("Gender", mContext.getResources().getString(R.string.Male));
            Gender.put(object);

            object = new JSONObject();
            object.put("Code", "F");
            object.put("Gender", mContext.getResources().getString(R.string.Female));
            Gender.put(object);

            object = new JSONObject();
            object.put("Code", "O");
            object.put("Gender", mContext.getResources().getString(R.string.Other));
            Gender.put(object);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Gender.toString();
    }*/

    @JavascriptInterface
    public String getMaritalStatus() {
        JSONArray maritalStatus = new JSONArray();
        JSONObject object = new JSONObject();

        try {
            object.put("Code", "");
            object.put("Status", mContext.getResources().getString(R.string.SelectMaritalStatus));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "M");
            object.put("Status", mContext.getResources().getString(R.string.Married));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "S");
            object.put("Status", mContext.getResources().getString(R.string.Single));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "D");
            object.put("Status", mContext.getResources().getString(R.string.Divorced));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "W");
            object.put("Status", mContext.getResources().getString(R.string.Widowed));
            maritalStatus.put(object);

            object = new JSONObject();
            object.put("Code", "N");
            object.put("Status", mContext.getResources().getString(R.string.NotSpecified));
            maritalStatus.put(object);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return maritalStatus.toString();
    }

    @JavascriptInterface
    public String getProfessions() {
        String tableName = "tblProfessions";
        String[] columns = {"ProfessionId", "Profession", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Professions = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Professions.toString();
    }

    @JavascriptInterface
    public String getEducations() {
        String tableName = "tblEducations";
        String[] columns = {"EducationId", "Education", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Educations = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Educations.toString();
    }

    @JavascriptInterface
    public String getIdentificationTypes() {
        String tableName = "tblIdentificationTypes";
        String[] columns = {"IdentificationCode", "IdentificationTypes", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Educations = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Educations.toString();
    }

    @JavascriptInterface
    public String getRelationships() {
        String tableName = "tblRelations";
        String[] columns = {"RelationId", "Relation", "AltLanguage"};
        String where = null;
        String OrderBy = "SortOrder";

        JSONArray Relations = sqlHandler.getResult(tableName, columns, null, OrderBy);

        return Relations.toString();
    }

    @JavascriptInterface
    public String getHFLevels() {
        JSONArray jsonHFLevels = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        LinkedHashMap<String, String> hfLevels = global.getHFLevels();

        try {
            jsonObject.put("Code", "");
            jsonObject.put("HFLevel", mContext.getResources().getString(R.string.SelectHFLevel));
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
    public String getHF(int DistrictId, String HFLevel) {
        JSONArray HFs;
        if (HFLevel != null) {
            String Query = "SELECT HFID,  HFCode ||\" : \"||  HFName HF FROM tblHF WHERE LocationId = ? AND HFLevel = ?";
            String[] args = {String.valueOf(DistrictId), HFLevel};

            HFs = sqlHandler.getResult(Query, args);
        } else {
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
                if (object.getString("value") != "null")
                    ControlValue = object.getString("value");
                else
                    ControlValue = null;
                data.put(ControlName, ControlValue);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;

    }

    @JavascriptInterface
    public int SaveFamily(String FamilyData, String InsureeData) {

        int FamilyId = 0;
        int InsureeId = 0;
        int MaxFamilyId = 0;

        try {
            global = (Global) mContext.getApplicationContext();
            MaxFamilyId = getNextAvailableFamilyId();

            /*if (InsureeData.length() > 0) {
                int validation = isValidInsureeData(jsonToTable(InsureeData));
                if (validation > 0) {
                    throw new UserException(mContext.getResources().getString(validation));
                }
            }*/

            //Insert Family
            //===============================================================================
            HashMap<String, String> data = jsonToTable(FamilyData);
            ContentValues values = new ContentValues();


            FamilyId = Integer.parseInt(data.get("hfFamilyId"));

            int LocationId = Integer.parseInt(data.get("ddlVillage"));

            Boolean Poverty = null;
            if (!TextUtils.isEmpty(data.get("ddlPovertyStatus"))) {
                Poverty = data.get("ddlPovertyStatus").equals("1");
            }

            String FamilyType = null;
            if (!TextUtils.isEmpty(data.get("ddlGroupType")) && !data.get("ddlGroupType").equals("0"))
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
                    Online = 2;
                    values.put("isOffline", 2);
                }
                sqlHandler.updateData("tblFamilies", values, "FamilyId = ? AND (isOffline = ? OR isOffline = ?) ", new String[]{String.valueOf(FamilyId), String.valueOf(isOffline), String.valueOf(Online)}, false);
                //Automatic sync
                /*
                if (isOffline == 0 && global.getUserId() > 0) {
                    pd = new ProgressDialog(mContext);
                    pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Uploading));
                    final int FinalFamilyId = FamilyId;
                    new Thread() {
                        public void run() {
                            try {
                                Enrol(FinalFamilyId, 0, 0, 0, 0);
                            } catch (UserException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            pd.dismiss();
                        }
                    }.start();

                }*/
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
            ShowDialog(mContext.getResources().getString(R.string.ErrorOccurred));

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
        Boolean approveSMS = familyFormData.get("ddlApprovalOfSMS").equals("1");
        String languageOfSMS =
                familyFormData.get("ddlLanguageOfSMS") == "" ? null : familyFormData.get("ddlLanguageOfSMS");
        addOrUpdateFamilySms(familyId, approveSMS, languageOfSMS);
    }

    public void addOrUpdateFamilySms(int familyId, Boolean approve, String language) throws UserException {
        ContentValues familySmsValues = new ContentValues();
        familySmsValues.put("ApprovalOfSMS", approve);
        familySmsValues.put("LanguageOfSMS", language);
        familySmsValues.put("FamilyID", familyId);

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
        String Query = "SELECT InsureeId FROM tblInsuree WHERE Trim(CHFID) = ? AND InsureeId <> ?";
        String args[] = {InsuranceNumber, InsureeId};
        JSONArray returnData = sqlHandler.getResult(Query, args);
        if (returnData.length() > 0) {
            Result = R.string.InsuranceNumberExists;
        } else {
            Result = 0;
        }
        return Result;
    }


    @JavascriptInterface
    public int SaveInsuree(String InsureeData, int FamilyId, int isHead, int ExceedThreshold, int PolicyId) throws Exception {

        inProgress = true;

        int InsureeId = 0;
        int IsHeadSet = 0;
        int isOffline = 1;
        int insureeIsOffline = 1;
        int MaxInsureeId = 0;
        try {
            global = (Global) mContext.getApplicationContext();
            HashMap<String, String> data = jsonToTable(InsureeData);

            /*int validation = isValidInsureeData(data);
            if (validation > 0) {
                ShowDialog(mContext.getResources().getString(validation));
                return 7;
            }*/

            MaxInsureeId = getNextAvailableInsureeId();
            ContentValues values = new ContentValues();
            ContentValues InsureePolicyvalues = new ContentValues();
            ContentValues FamilyValues = new ContentValues();

            InsureeId = Integer.parseInt(data.get("hfInsureeId"));
            rtInsureeId = InsureeId;

            String s1 = data.get("hfisHead");
            if (s1.equals("true") || s1.equals("1")) IsHeadSet = 1;
            else if (s1.equals("false") || s1.equals("0")) IsHeadSet = 0;
            else IsHeadSet = Integer.parseInt(data.get("hfisHead"));

            String Marital = null;
            if (data.get("ddlMaritalStatus") != "" && data.get("ddlMaritalStatus") != null)
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
            values.put("LastNameArab", data.get("txtLastNameArab"));
            values.put("GivenNameArab", data.get("txtGivenNameArab"));
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
            values.put("PhotoPath", PhotoPath);
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
                            if (isOffline == 0 || isOffline == 2) {
                                if (isOffline == 2) isOffline = 0;
                                int InsId = -rtInsureeId;
                                ShowDialogYesNo(InsId, FamilyId, Activate, isOffline);
                            } else {
                                ShowDialogYesNo(rtInsureeId, FamilyId, Activate, isOffline);
                            }
                        } else if (ExceedThreshold == 0) {
                            if (isOffline == 0 || isOffline == 2) {
                                if (isOffline == 2) isOffline = 0;
                                int InsId = -rtInsureeId;
                                SaveInsureePolicy(InsId, FamilyId, true, isOffline);
                            } else {
                                SaveInsureePolicy(rtInsureeId, FamilyId, true, isOffline);
                            }
                        }
                    } else {
                        sqlHandler.insertData("tblInsuree", values);
                        if (PolicyId > 0 && isHead == 0) {
                            getFamilyPolicies(FamilyId);
                        }
                        rtInsureeId = MaxInsureeId;
                        if (ExceedThreshold == 1)
                            ShowDialogYesNo(rtInsureeId, FamilyId, Activate, isOffline);
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
                        ShowDialogYesNo(rtInsureeId, FamilyId, Activate, isOffline);
                    else if (ExceedThreshold == 0)
                        SaveInsureePolicy(rtInsureeId, FamilyId, true, isOffline);
                }

            } else {//Existing Insuree
                values.put("isOffline", insureeIsOffline);
                sqlHandler.updateData("tblInsuree", values, "InsureeId = ? AND (isOffline = ?)", new String[]{String.valueOf(InsureeId), String.valueOf(insureeIsOffline)});
            }

        } catch (NumberFormatException | UserException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return rtInsureeId;
    }

    public int doInsureeExisttblInsPolicy(int InsureeId) {
        String InsureeIdQuery = "SELECT * FROM tblInsureePolicy WHERE InsureeId = " + InsureeId;
        JSONArray JsonInsuree = sqlHandler.getResult(InsureeIdQuery, null);
        return JsonInsuree.length();
    }

    private String copyImageFromGalleryToApplication(String selectedPath, String InsuranceNumber) {
        String result = "";

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            Calendar cal = Calendar.getInstance();
            String d = format.format(cal.getTime());

            File[] oldFiles = ImageManager.of(mContext).getInsureeImages(InsuranceNumber);
            Runnable deleteOldFiles = () -> FileUtils.deleteFiles(oldFiles);

            String outputFileName = InsuranceNumber + "_" + global.getOfficerCode() + "_" + d + "_0_0.jpg";
            File outputFile = new File(global.getImageFolder(), outputFileName);

            if (!outputFile.createNewFile()) {
                Log.e("IMAGES", "Creating image copy failed");
            }

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            imageTarget = new OutputStreamImageTarget(outputStream, global.getIntKey("image_jpeg_quality", 40), deleteOldFiles);
            try {
                ((Activity) mContext).runOnUiThread(() -> picassoInstance.load(selectedPath)
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
    public AlertDialog deleteDialog(String msg, final int FamilyId) {
        return new AlertDialog.Builder(mContext)
                .setTitle(R.string.TitleDelete)
                .setMessage(msg)
                .setIcon(R.drawable.ic_about)
                .setCancelable(false)
                .setPositiveButton(R.string.Yes, (dialogInterface, i) -> {
                    if (DeleteFamily(FamilyId) == 1) {
                        ShowDialog(mContext.getResources().getString(R.string.FamilyDeleted));
                    }
                })
                .setNegativeButton(R.string.No, (dialog, which) -> {
                }).show();
    }

    @JavascriptInterface
    public void shutDownProgress() {
        inProgress = false;
    }

    @JavascriptInterface
    public int DeleteOnlineDataF(final int FamilyId) {
        int res = 0;
        if (DeleteFamily(FamilyId) == 1) {
            res = 1;
        }
        return res;
    }

    @JavascriptInterface
    public void informUser() {
        ShowDialog(mContext.getResources().getString(R.string.DeleteFamilyOnlyOffline));
    }

    @JavascriptInterface
    public String getAllFamilies() {
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
    public String getInsureeAttachments(int FamilyId) throws JSONException {
        Attachments = new JSONArray();

        String Query = "SELECT Id,Title, Filename, Content, FamilyId \n" +
                "FROM tblInsureeAttachments \n" +
                "WHERE FamilyId = ?";
        String[] args = {String.valueOf(FamilyId)};

        JSONArray Attachs = sqlHandler.getResult(Query, args);
        Log.e("Attachments", Attachs.toString());

        for (int i = 0; i < Attachs.length(); i++) {
            Attachments.put(Attachs.getJSONObject(i));
        }

        for (int i = 0; i < TempAttachments.length(); i++) {
            Attachments.put(TempAttachments.getJSONObject(i));
        }

        return  Attachments.toString();

    }

    @JavascriptInterface
    public void addAttachment(int familyId, String title, String filename) throws JSONException {
        String contentFile = ((MainActivity) mContext).fileContent;
        if (familyId != 0) {
            int MaxAttachmentId = getNextAvailableAttachmentId();
            ContentValues AttachmentValues = new ContentValues();
            AttachmentValues.put("Filename", filename);
            AttachmentValues.put("Id", MaxAttachmentId);
            AttachmentValues.put("Title", title);
            AttachmentValues.put("Content", contentFile);
            AttachmentValues.put("FamilyId", familyId);
            sqlHandler.insertData("tblInsureeAttachments", AttachmentValues);
        } else {
            JSONObject obj = new JSONObject();
            obj.put("Title", title);
            obj.put("Filename", filename);
            obj.put("content", contentFile);
            TempAttachments.put(obj);
        }

    }

    @JavascriptInterface
    public int DeleteAttachment(int FamilyId,int attachmentId, String attachmentTitle, String attachmentName) throws JSONException {
        Log.e("attachmentTitle", attachmentTitle);
        Log.e("attachmentName", attachmentName);
        if (FamilyId != 0) {
            String[] attachmentIdArgument = new String[]{String.valueOf(attachmentId)};
            String Query = "DELETE FROM tblInsureeAttachments WHERE Id = ?";
            sqlHandler.getResult(Query, attachmentIdArgument);
            return 1;
        } else {
            for (int i = 0; i < TempAttachments.length(); i++) {
                JSONObject obj = TempAttachments.getJSONObject(i);
                if (obj.getString("Title").equals(attachmentTitle)) {
                    if(attachmentName.equals(obj.getString("Filename"))){
                        TempAttachments.remove(i);
                        return 1;
                    }
                }
            }
        }

        return -1;
    }

    @JavascriptInterface
    //Get last segment value from url
    public String queryString(String url, String name) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter(name);
    }

    @JavascriptInterface
    public String getInsuree(int InsureeId) {
        String Query = "SELECT InsureeId, FamilyId, CHFID, LastName, OtherNames, LastNameArab, GivenNameArab, DOB, Gender, Marital, isHead, IdentificationNumber, Phone, isOffline , PhotoPath, CardIssued, Relationship, Profession, Education, Email, TypeOfId, I.HFID, CurrentAddress,R.LocationId CurRegion, D.LocationId CurDistrict, W.LocationId CurWard,  I.CurVillage, HFR.LocationId FSPRegion, HFD.LocationId FSPDistrict, HF.HFLevel FSPCategory, I.Vulnerability\n" +
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
    public String getInsureesForFamily(int FamilyId) {
        String Query = "SELECT I.InsureeId, I.CHFID, I.Othernames ||\" \"|| I.LastName InsureeName, " +
                "CASE I.Gender WHEN 'M' THEN '" + mContext.getResources().getString(R.string.Male) + "' WHEN 'F' THEN '" + mContext.getResources().getString(R.string.Female) + "' ELSE '" + mContext.getResources().getString(R.string.Other) + "' END Gender, " +
                "I.DOB , I.isHead, isOffline, I.Vulnerability FROM tblInsuree I WHERE FamilyId = ? ORDER BY I.isHead DESC, I.InsureeId ASC";
        String[] arg = {String.valueOf(FamilyId)};
        JSONArray Insurees = sqlHandler.getResult(Query, arg);
        return Insurees.toString();
    }

    @JavascriptInterface
    public String getFamilyHeader(int FamilyId) {
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
    public String getFamily(int FamilyId) {
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
    public String getPolicyPeriod(int ProdId, String EnrollDate) throws ParseException, JSONException {

        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        Date dEnrollDate = format.parse(EnrollDate);

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
        boolean hasCycle = false;
        Date StartDate = null;
        Date ExpiryDate = null;
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
            if (dateWithGracePeriod1 != null && (dEnrollDate.compareTo(dateWithGracePeriod1) == 0 || dEnrollDate.before(dateWithGracePeriod1)))
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


    public static Uri tempPhotoUri = null;
    public static int RESULT_LOAD_IMG = 1;
    public static int RESULT_SCAN = 100;
    public static String ImagePath;
    public static String InsuranceNo;
    public static boolean inProgress = true;

    @JavascriptInterface
    public void selectPicture() {
        Path = global.getSubdirectory("Images") + "/";

        File tempPhotoFile = FileUtils.createTempFile(mContext, "images/selectPictureTemp.jpeg");
        tempPhotoUri = UriUtils.createUriForFile(mContext, tempPhotoFile);
        if (tempPhotoUri == null) return;

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        final List<Intent> cameraIntents = new ArrayList<>();
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(EXTRA_OUTPUT, tempPhotoUri);
        cameraIntents.add(cameraIntent);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        ((Activity) mContext).startActivityForResult(chooserIntent, RESULT_LOAD_IMG);
    }

    @JavascriptInterface
    public double getPolicyValue(String enrollDate, int ProductId, int FamilyId, String startDate, boolean HasCycle, int PolicyId, String PolicyStage, int IsOffline) throws JSONException {
        Date ExpiryDate = null;
        String expiryDate = null;
        int PreviousPolicyId = 0;
        Date PreviousExpiryDate;

        if (PolicyId > 0) {
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

        String AdultMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE (strftime('%Y', 'now') - strftime('%Y', DOB))  >= 18 AND IFNULL(Relationship,0) <> 7  AND FamilyID ='" + FamilyId + "' ORDER BY InsureeId ASC LIMIT " + MemberCount;

        JSONArray AdultMembersArray = sqlHandler.getResult(AdultMembersQuery, null);
        JSONObject AdultObject = AdultMembersArray.getJSONObject(0);
        int AdultMembers = Integer.parseInt(AdultObject.getString("count"));
        if (AdultMembers > MemberCount) AdultMembers = MemberCount;


        String ChildMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE (strftime('%Y', 'now') - strftime('%Y', DOB))  < 18 AND IFNULL(Relationship,0) <> 7  AND FamilyID = '" + FamilyId + "' ORDER BY InsureeId ASC LIMIT " + MemberCount;
        JSONArray ChildMembersArray = sqlHandler.getResult(ChildMembersQuery, null);
        JSONObject ChildObject = ChildMembersArray.getJSONObject(0);
        int ChildMembers = Integer.parseInt(ChildObject.getString("count"));

        if ((AdultMembers + ChildMembers) >= MemberCount) ChildMembers = MemberCount - AdultMembers;


        String OAdultMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE(strftime('%Y', 'now') - strftime('%Y', DOB))  >= 18 AND IFNULL(Relationship,0) = 7  AND FamilyID = '" + FamilyId + "' ORDER BY InsureeId ASC LIMIT " + MemberCount;
        JSONArray OAdultMembersArray = sqlHandler.getResult(OAdultMembersQuery, null);
        JSONObject OAdultObject = OAdultMembersArray.getJSONObject(0);
        int OAdultMembers = Integer.parseInt(OAdultObject.getString("count"));

        if ((AdultMembers + ChildMembers + OAdultMembers) >= MemberCount)
            OAdultMembers = MemberCount - (AdultMembers + ChildMembers);


        String OChildMembersQuery = "SELECT COUNT(InsureeId) count FROM tblInsuree WHERE(strftime('%Y', 'now') - strftime('%Y', DOB))  < 18 AND IFNULL(Relationship,0) = 7  AND FamilyID = '" + FamilyId + "'  ORDER BY InsureeId ASC LIMIT " + MemberCount;
        JSONArray OChildMembersArray = sqlHandler.getResult(OChildMembersQuery, null);
        JSONObject OChildObject = OChildMembersArray.getJSONObject(0);
        int OChildMembers = Integer.parseInt(OChildObject.getString("count"));
        if ((AdultMembers + ChildMembers + OAdultMembers + OChildMembers) >= MemberCount)
            OAdultMembers = MemberCount - (AdultMembers + ChildMembers + OAdultMembers);

        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        //Get   Previous Expiry Date
        if (PreviousPolicyId > 0) {
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
        double Contribution = 0;
        double GeneralAssembly = 0;
        double Registration = 0;
        double AddonAdult = 0;
        double AddonChild = 0;
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
            if (EnrollDate.before(MinDiscountDateN) && HasCycle == true) {
                PolicyValue -= (PolicyValue * 0.01 * DiscountPercentN);
            }
        } else if (PolicyStage.equalsIgnoreCase("R")) {


            if (PreviousPolicyId > 0) {

                PreviousExpiryDate = addDay(ExpiryDate, 1);

            } else {
                PreviousExpiryDate = StartDate;
            }
            MinDiscountDateR = addMonth(PreviousExpiryDate, DiscountPeriodR);///@),@);
            if (EnrollDate.before(MinDiscountDateR))
                PolicyValue -= (PolicyValue * 0.01 * DiscountPercentR);
        }


        return PolicyValue;
    }

    @JavascriptInterface
    public String getOfficers(int LocationId, String EnrolmentDate) {

        String OfficerQuery = " SELECT OfficerId, Code  ||\" - \"|| Othernames  ||\" \"||  LastName  Code, LocationId FROM tblOfficer \n" +
                " WHERE LocationId=" + LocationId + " AND ('" + EnrolmentDate + "' <= WorksTo OR  IFNULL(WorksTo,0)=0 OR  IFNULL('" + EnrolmentDate + "',0) = 0) \n" +
                " ORDER BY OfficerId";
        JSONArray Oficers = sqlHandler.getResult(OfficerQuery, null);

        return Oficers.toString();
    }

    @JavascriptInterface
    public String getProducts(int RegionId, int DistrictId, String EnrolmentDate) {

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
        String loc = null;
        int RegionId = 0, DistrictId = 0;
        try {
            JSONArray locArray = null;
            JSONObject obj = null;
            loc = getOfficerLocation();
            locArray = new JSONArray(loc);
            if (locArray.length() == 0) {

            } else {
                for (int i = 0; i < locArray.length(); i++) {
                    obj = locArray.getJSONObject(i);
                    RegionId = Integer.parseInt(obj.getString("RegionId"));
                    DistrictId = Integer.parseInt(obj.getString("DistrictId"));
                }
            }

            String ProductQuery = "SELECT ProdId, ProductCode, ProductName \n" +
                    "FROM tblProduct P\n" +
                    "INNER JOIN  uvwLocations L ON (P.LocationId = L.LocationId) OR (P.LocationId = 'null' OR P.LocationId = '') \n" +
                    "WHERE  ((L.RegionId = " + RegionId + " OR L.RegionId ='null') AND (L.DistrictId =  " + DistrictId + " OR L.DistrictId ='null') OR L.LocationId='null') " +
                    "ORDER BY  L.LocationId DESC";

            Products = sqlHandler.getResult(ProductQuery, null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Products.toString();
    }

    public String getProductsByDistrict(int districtId, String date) {
        int regionId = sqlHandler.getRegionId(districtId);
        return getProducts(regionId, districtId, date);
    }

    @JavascriptInterface
    public boolean IsBulkCNUsed() {
        return BuildConfig.SHOW_BULK_CN_MENU;
    }

    @JavascriptInterface
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
    public boolean isFetchedControlNumber(String controlNumber) {
        return sqlHandler.isFetchedControlNumber(controlNumber);
    }

    @JavascriptInterface
    public int SavePolicy(String PolicyData, int FamilyId, int PolicyId) throws Exception {
        inProgress = true;
        int MaxPolicyId;
        rtPolicyId = PolicyId;
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
            if (isOffline == 2) isOffline = 0;
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
                if (isOffline == 0 || isOffline == 2) {
                    isOffline = 0;
                    Online = 2;
                }
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
    public String getFamilyPolicies(int FamilyId) throws ParseException {
        //mimi ni add
        //getPolicyValue(String enrollDate, int ProductId, int FamilyId, String startDate, boolean HasCycle, int PolicyId, String PolicyStage, int IsOffline) throws JSONException {
        boolean isValueChanged = false;
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


        String Query = "SELECT  P.PolicyId, Prod.ProductCode, ProductName, EffectiveDate, PolicyValue, StartDate, EnrollDate, bcn.ControlNumber, \n" +
                "   CASE    WHEN PolicyStatus = 1 THEN '" + mContext.getResources().getString(R.string.Idle) + "'   " +
                "   WHEN PolicyStatus = 2 THEN '" + mContext.getResources().getString(R.string.Active) + "'  " +
                "   WHEN PolicyStatus = 4 THEN '" + mContext.getResources().getString(R.string.Suspended) + "'  " +
                "   WHEN PolicyStatus = 8 THEN '" + mContext.getResources().getString(R.string.Expired) + "'  END  PolicyStatus, " +
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
            ((Activity) mContext).runOnUiThread(() -> ShowDialog(mContext.getResources().getString(R.string.PolicyValueChange) + finalEnrollDate + "," + "has been changed from " + finalPolicyValue + " to " + finalNewPolicyValue));
        }

        return Policies.toString();
    }

    @JavascriptInterface
    public String getPolicy(int PolicyId) {
        String Query = "SELECT  P.PolicyId, P.ProdId, OfficerId , Prod.ProductCode, ProductName, PolicyStage, EffectiveDate, IFNULL(PolicyValue,0) PolicyValue, StartDate, EnrollDate, bcn.ControlNumber, \n" +
                "   CASE    WHEN PolicyStatus = 1 THEN '" + mContext.getResources().getString(R.string.Idle) + "'   " +
                "   WHEN PolicyStatus = 2 THEN '" + mContext.getResources().getString(R.string.Active) + "'  " +
                "   WHEN PolicyStatus = 4 THEN '" + mContext.getResources().getString(R.string.Suspended) + "'  " +
                "   WHEN PolicyStatus = 8 THEN '" + mContext.getResources().getString(R.string.Expired) + "'  END  PolicyStatus, " +
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
    public int getPolicyVal(String PolicyId) {
        int PolicId = Integer.parseInt(PolicyId);
        int policyval = 0;
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
    public int getSumPrem(String PolicyId) {
        int PolicId = Integer.parseInt(PolicyId);
        int totalPremium = 0;
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
    public int SavePremiums(String PremiumData, int PolicyId, int PremiumId, int FamilyId) throws Exception {
        inProgress = true;
        int MaxPremiumId = 0;
        int isOffline = 1;
        rtPremiumId = PremiumId;
        //  String CHFID = "";
        String ReceiptNo = "";
        try {
            global = (Global) mContext.getApplicationContext();
            MaxPremiumId = getNextAvailablePremiumId();

            //  isOffline = getFamilyStatus(FamilyId);
            if (isOffline == 2) isOffline = 0;

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
                if (isOffline == 0) MaxPremiumId = -MaxPremiumId;
                values.put("PremiumId", MaxPremiumId);
                sqlHandler.insertData("tblPremium", values);
                rtPremiumId = MaxPremiumId;
            } else {
                int Online = 2;
                if (isOffline == 0 || isOffline == 2) {
                    isOffline = 0;
                    Online = 2;
                }
                sqlHandler.updateData("tblPremium", values, "PremiumId = ? AND (isOffline = ? OR isOffline = ?) ", new String[]{String.valueOf(PremiumId), String.valueOf(isOffline), String.valueOf(Online)});

            }
            if (isOffline == 0 || isOffline == 2) {
//Automatic sync
/*                if(global.getUserId() >= 0){
                    pd = new ProgressDialog(mContext);
                    pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Uploading));
                    final int FinalFamilyId = FamilyId;
                    final int FinalPremium = rtPremiumId;
                    //new Thread() {
                        //public void run() {
                            try {

                                rtPremiumId = Enrol(FinalFamilyId, 0, 0, FinalPremium, 0);
                                inProgress = false;
                            } catch (UserException e) {
                                e.printStackTrace();
                            }

                            pd.dismiss();
                       // }
                   // }.start();
                }*/

            } else {
                inProgress = false;
            }

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
    public String getPayers(int RegionId, int DistrictId) {
        String Query = "SELECT PayerId, PayerName,P.LocationId FROM tblPayer P " +
                "LEFT OUTER JOIN tblLocations L ON P.LocationId = L.LocationId " +
                "WHERE L.LocationId = ? OR L.LocationId = ? OR L.LocationId = 'null' OR L.LocationId = '' OR L.LocationId = 0 " +
                "ORDER BY L.LocationId";
        /*        "INNER JOIN  uvwLocations L ON P.LocationId = L.LocationId\n" +
                "WHERE  (L.RegionId = " + RegionId + " OR L.RegionId ='null' OR L.RegionId ='') AND (L.DistrictId = " + DistrictId + " OR L.DistrictId ='null' OR L.DistrictId ='')  " +
                " ORDER BY L.LocationId";*/
        JSONArray Payers = sqlHandler.getResult(Query, new String[]{String.valueOf(RegionId), String.valueOf(DistrictId)});

        return Payers.toString();
    }

    @JavascriptInterface
    public String getPremiums(int PolicyId) {
        String Query = "SELECT PremiumId, PayerId, Amount, Receipt , PayDate, " +
                "CASE PayType WHEN 'C' THEN '" + mContext.getResources().getString(R.string.Cash) + "' WHEN 'B' THEN '" + mContext.getResources().getString(R.string.BankTransfer) + "' WHEN 'M' THEN '" + mContext.getResources().getString(R.string.MobilePhone) + "' END PayType, " +
                "isOffline,IsPhotoFee \n" +
                "FROM tblPremium WHERE PolicyId=?";
        String arg[] = {String.valueOf(PolicyId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        return Premiums.toString();
    }

    @JavascriptInterface
    public String getPremium(int PremiumId) {
        String Query = "SELECT PremiumId, PayerId, Amount, Receipt , PayDate, PayType,isOffline,IsPhotoFee \n" +
                "FROM tblPremium WHERE PremiumId=?";
        String arg[] = {String.valueOf(PremiumId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        return Premiums.toString();
    }

    public JSONArray getRecordedPolicies() {
        String Query = "SELECT * FROM tblRecordedPolicies WHERE ControlNumberId is null";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        return RecordedPolicies;
    }

    //This Query
    public JSONArray getRecordedPolicies(String insuranceNumber, String otherNames, String lastName, String insuranceProduct, String uploadedFrom, String uploadedTo, String radioRenewal, String radioRequested) {
        String renewal = "";
        String requested = "";
        String upload = "";
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

        String Query = "SELECT * FROM tblRecordedPolicies WHERE InsuranceNumber LIKE '%" + insuranceNumber + "%' AND LastName LIKE '%" + lastName + "%' AND OtherNames LIKE '%" + otherNames + "%' AND ProductCode LIKE '%" + insuranceProduct + "%'" + renewal + "" + requested + "" + upload + "";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        return RecordedPolicies;
    }

    public JSONArray getRecordedPolicies(String insuranceNumber, String otherNames, String lastName, String insuranceProduct, String uploadedFrom, String uploadedTo, String radioRenewal, String requestedFrom, String requestedTo, String PaymentType) {
        String renewal = "";
        String request;
        String upload = "";
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

        String Query = "SELECT * FROM tblRecordedPolicies RP INNER JOIN tblControlNumber CN ON RP.ControlNumberId = CN.Id WHERE RP.InsuranceNumber LIKE '%" + insuranceNumber + "%' AND RP.LastName LIKE '%" + lastName + "%' AND RP.OtherNames LIKE '%" + otherNames + "%' AND RP.ProductCode LIKE '%" + insuranceProduct + "%'" + renewal + " " + request + " " + upload + " " + payment_type + "";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        return RecordedPolicies;
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
        String Query = "SELECT * FROM tblRecordedPolicies WHERE ControlNumberId = '" + code + "'";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        return RecordedPolicies;
    }

    public String getCode(String InternalIdentifier) {
        String code = "";
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

    public JSONArray getRecordedPolicies(String search_string) {
        String Query = "SELECT * FROM tblRecordedPolicies (WHERE InsuranceNumber LIKE '%" + search_string + "%' OR LastName LIKE '%" + search_string + "%' OR OtherNames LIKE '%" + search_string + "%' OR ProductName LIKE '%" + search_string + "%' OR isDone LIKE '%" + search_string + "%') AND ControlNumberId = 'N'";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        return RecordedPolicies;
    }

    public JSONArray getRecordedPolicies(String From, String To) {
        String Query = "SELECT * FROM tblRecordedPolicies WHERE (UploadedDate BETWEEN '" + From + "' AND '" + To + "') AND ControlNumberId is null";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        return RecordedPolicies;
    }

    public JSONArray getPolicyRequestedControlNumber(String search_string) {
        String Query = "SELECT * FROM tblRecordedPolicies RP LEFT OUTER JOIN tblControlNumber CN on RP.ControlNumberId = CN.Id WHERE RP.InsuranceNumber LIKE '%" + search_string + "%' OR RP.LastName LIKE '%" + search_string + "%' OR RP.OtherNames LIKE '%" + search_string + "%' OR RP.ProductName LIKE '%" + search_string + "%' OR RP.isDone LIKE '%" + search_string + "%'";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        return RecordedPolicies;
    }

    public JSONArray getPolicyRequestedControlNumber(String From, String To) {
        String Query = "SELECT * FROM tblRecordedPolicies RP LEFT OUTER JOIN tblControlNumber CN on RP.ControlNumberId = CN.Id WHERE RP.UploadedDate BETWEEN '" + From + "' AND '" + To + "'";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        return RecordedPolicies;
    }

    public JSONArray getPolicyRequestedControlNumber(String From, String To, String i) {
        String Query = "SELECT * FROM tblRecordedPolicies RP LEFT OUTER JOIN tblControlNumber CN on RP.ControlNumberId = CN.Id WHERE RP.ControlRequestDate BETWEEN '" + From + "' AND '" + To + "'";
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, null);
        return RecordedPolicies;
    }

    public String getRecordedPoliciesById(int RenewalId) {
        String Query = "SELECT RenewalId, PolicyId, OfficerId, OfficerCode , InsuranceNumber, LastName,OtherNames,ProductCode,ProductName \n" +
                "RenewalPromptDate, isDone, LocationId, PolicyValue, UploadedDate, ControlRequestDate FROM tblRecordedPolicies WHERE RenewalId=?";
        String arg[] = {String.valueOf(RenewalId)};
        JSONArray RecordedPolicies = sqlHandler.getResult(Query, arg);
        return RecordedPolicies.toString();
    }

    public int insertControlNumber(String amountCalculated, String amountConfirmed, String control_number, String InternalIdentifier, String PaymentType, String SmsRequired) {
        ContentValues values = new ContentValues();
        values.put("AmountCalculated", String.valueOf(amountCalculated));
        values.put("AmountConfirmed", String.valueOf(amountConfirmed));
        values.put("ControlNumber", String.valueOf(control_number));
        values.put("InternalIdentifier", String.valueOf(InternalIdentifier));
        values.put("PaymentType", String.valueOf(PaymentType));
        values.put("SmsRequired", String.valueOf(SmsRequired));

        sqlHandler.insertData("tblControlNumber", values);

        return getMaxId();
    }

    public int getMaxId() {
        int id = 0;
        String Query = "SELECT Max(Id) As Id FROM tblControlNumber";
        JSONArray ID = sqlHandler.getResult(Query, null);
        try {
            JSONObject JmaxIdOb = ID.getJSONObject(0);
            id = JmaxIdOb.getInt("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    public boolean UpdateRecordedPolicies(int RenewalId, String col, String val) {
        ContentValues values = new ContentValues();
        values.put(col, val);
        try {
            sqlHandler.updateData("tblRecordedPolicies", values, "RenewalId = ?", new String[]{String.valueOf(RenewalId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
        return true;//Update Success
    }

    public void updateRecordedPolicy(int Id, int ControlNumberId) {
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        ;
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        ContentValues values = new ContentValues();
        values.put("ControlNumberId", ControlNumberId);
        values.put("ControlRequestDate", d);
        try {
            sqlHandler.updateData("tblRecordedPolicies", values, "Id = ?", new String[]{String.valueOf(Id)});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }

    public void assignControlNumber(String InternalIdentifier, String ControlNumber) {
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        ContentValues values = new ContentValues();
        values.put("ControlNumber", ControlNumber);
        try {
            sqlHandler.updateData("tblControlNumber", values, "InternalIdentifier = ?", new String[]{String.valueOf(InternalIdentifier)});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public boolean IsReceiptNumberUnique(String ReceiptNo, int FamilyId) {
        String CHFID = "";
        int isOffline = 0;
        int isHead = 1;
        Boolean res = true;
        String CHFIDQUERY = "SELECT CHFID,isOffline FROM tblInsuree WHERE isHead = " + isHead + " AND FamilyId = " + FamilyId;

        JSONArray JsonCHFID = sqlHandler.getResult(CHFIDQUERY, null);
        try {
            JSONObject JsonCHFIDJSONObject = JsonCHFID.getJSONObject(0);
            CHFID = JsonCHFIDJSONObject.getString("CHFID");
            isOffline = JsonCHFIDJSONObject.getInt("isOffline");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (global.isNetworkAvailable() && isOffline == 0) {
            CallSoap cs = new CallSoap();
            cs.setFunctionName("isUniqueReceiptNo");
            if (!cs.isUniqueReceiptNo(ReceiptNo.trim(), CHFID)) {
                res = false;
            }
        }

/*        String Query = "SELECT PremiumId, PayerId, Amount, Receipt , PayDate, PayType,IsOffline,isPhotoFee \n" +
                "FROM tblPremium WHERE Receipt=?";*/

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
    public String checkNet() {
        if (global.isNetworkAvailable()) {
            return "true";
        } else {
            return "false";
        }
    }

    @JavascriptInterface
    public void getLocalData() {

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) mContext).PickMasterDataFileDialogFromPage();
                ((MainActivity) mContext).calledFrom = "htmlpage";
            }
        });
    }

    @JavascriptInterface
    public int DeletePremium(int PremiumId, int PolicyId) {
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
    public int DeletePremium(int PremiumId) {
        String Query = "DELETE FROM tblPremium WHERE PremiumId=?";
        String arg[] = {String.valueOf(PremiumId)};
        JSONArray Premiums = sqlHandler.getResult(Query, arg);
        //Premiums.toString();
        return 1;
    }

    //get sum of premiums of policy id
    public int getSumPremium(int PolicyId) {
        int sumpremiums = 0;
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
    public int DeleteInsuree(int InsureeId) {
        int res = 0;
        int FamilyId = 0;
        try {
            String Query = "SELECT FamilyId FROM tblInsuree WHERE InsureeId = " + InsureeId;
            JSONArray FID = sqlHandler.getResult(Query, null);
            JSONObject Ob = FID.getJSONObject(0);
            FamilyId = Ob.getInt("FamilyId");

            String IsHeadQuery = "SELECT InsureeId FROM tblInsuree WHERE InsureeId=? AND ishead =?";
            String IsHeadarg[] = {String.valueOf(InsureeId), "1"};
            JSONArray IsHead = sqlHandler.getResult(IsHeadQuery, IsHeadarg);
            int Count = IsHead.length();
            if (Count > 0)
                res = 2;
            else if (Count == 0) {
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
    public int DeleteFamily(int FamilyId) {
        String[] familyIdArgument = new String[]{String.valueOf(FamilyId)};
        String policyIdSubquery = "(SELECT PolicyId FROM tblPolicy WHERE FamilyId = ?)";

        String updateFetchedCNQuery = "UPDATE " + SQLHandler.tblBulkControlNumbers + " " +
                "SET PolicyId = NULL WHERE Id IS NOT NULL AND PolicyId IN " +
                policyIdSubquery;
        sqlHandler.getResult(updateFetchedCNQuery, familyIdArgument);

        String deleteInsertedCNQuery = "DELETE FROM " + SQLHandler.tblBulkControlNumbers + " " +
                "WHERE Id IS NULL AND PolicyId IN " +
                policyIdSubquery;
        sqlHandler.getResult(deleteInsertedCNQuery, familyIdArgument);

        String PremiumQuery = "DELETE FROM tblPremium \n" +
                "WHERE PolicyId IN \n" +
                policyIdSubquery;
        sqlHandler.getResult(PremiumQuery, familyIdArgument);
        //Premium.toString();

        String InsureePolicyQuery = "DELETE FROM tblInsureePolicy \n" +
                "WHERE PolicyId IN \n" +
                policyIdSubquery;
        sqlHandler.getResult(InsureePolicyQuery, familyIdArgument);

        String PolicyQuery = "DELETE FROM tblPolicy WHERE FamilyId = ?";
        sqlHandler.getResult(PolicyQuery, familyIdArgument);

        String InsureeQuery = "DELETE FROM  tblInsuree WHERE FamilyId = ?";
        sqlHandler.getResult(InsureeQuery, familyIdArgument);

        String FamilyQuery = "DELETE FROM  tblFamilies WHERE FamilyId = ?";
        sqlHandler.getResult(FamilyQuery, familyIdArgument);
        //Families.toString();
        return 1;
    }

    public String getDataFromDb2(String chfid) {

        try {

            resu = "[{";

            db = openOrCreateDatabase(Path + "ImisData.db3", null);

            String[] columns = {"CHFID", "Photo", "InsureeName", "DOB", "Gender", "ProductCode", "ProductName", "ExpiryDate", "Status", "DedType", "Ded1", "Ded2", "Ceiling1", "Ceiling2"};

            Cursor c = db.query("tblPolicyInquiry", columns, "Trim(CHFID)=" + "\'" + chfid + "\'", null, null, null, null);

            int i = 0;
            boolean _isHeadingDone = false;

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                for (i = 0; i < 5; i++) {
                    if (!_isHeadingDone) {
                        if (c.getColumnName(i).equalsIgnoreCase("photo")) {
                            byte[] photo = c.getBlob(i);
                            if (photo != null) {
                                ByteArrayInputStream is = new ByteArrayInputStream(photo);
                                theImage = BitmapFactory.decodeStream(is);
                            }
                            continue;
                        }
                        resu = resu + "\"" + c.getColumnName(i) + "\":" + "\"" + c.getString(i) + "\",";
                    } else {

                    }
                }
                _isHeadingDone = true;

                if (c.isFirst())
                    resu = resu + "\"" + "Details" + "\":[{";
                else
                    resu = resu + "{";

                for (i = 5; i < c.getColumnCount(); i++) {

                    resu = resu + "\"" + c.getColumnName(i) + "\":" + "\"" + c.getString(i) + "\"";
                    if (i < c.getColumnCount() - 1)
                        resu = resu + ",";
                    else {
                        resu = resu + "}";
                        if (!c.isLast()) resu = resu + ",";
                    }

                }
                //result = result + "]}";
            }

            resu = resu + "]}]";

        } catch (Exception e) {
            resu = e.toString();
        }

        return resu;

    }

    public String OfflineEnquire(String CHFID) {
        sqlHandler.isPrivate = false;
        String Query = "SELECT CHFID ,Photo ,InsureeName,DOB,Gender,ProductCode,ProductName,ExpiryDate,Status,DedType,Ded1,Ded2,Ceiling1,Ceiling2 FROM tblPolicyInquiry WHERE  Trim(CHFID) = ?";
        String arg[] = {CHFID};
        JSONArray Insuree = sqlHandler.getResult(Query, arg);
        return Insuree.toString();
    }

    public String OfflineRenewals(String OfficerCode) {
        String Query = "SELECT RenewalId, PolicyId, OfficerId, OfficerCode, CHFID, LastName, OtherNames, ProductCode, ProductName, VillageName, RenewalPromptDate, IMEI, Phone,LocationId,PolicyValue, EnrollDate, RenewalUUID " +
                " FROM tblRenewals WHERE LOWER(OfficerCode)=? AND isDone = ? ";
        String arg[] = {OfficerCode.toLowerCase(), "N"};
        JSONArray Renews = sqlHandler.getResult(Query, arg);
        return Renews.toString();
    }


    public String InsertRenewalsFromApi(String Result) {
        String TableName = "tblRenewals";
        String[] Columns = {"renewalId", "policyId", "officerId", "officerCode", "chfid", "lastName", "otherNames", "productCode", "productName", "villageName", "renewalPromptDate", "phone", "renewalUUID"};
        String Where = "isDone = ?";
        String[] WhereArg = {"N"};
        sqlHandler.deleteData(TableName, Where, WhereArg);
        try {
            sqlHandler.insertData(TableName, Columns, Result, "");
        } catch (JSONException e) {
            e.printStackTrace();
            return String.valueOf(0);
        }
        return String.valueOf(1);
    }

    public String InsertRenewalsFromExtract(String Result) {
        String TableName = "tblRenewals";
        String[] Columns = {"RenewalId", "PolicyId", "OfficerId", "OfficerCode", "CHFID", "LastName", "OtherNames", "ProductCode", "ProductName", "VillageName", "RenewalPromptDate", "Phone", "RenewalUUID"};
        String Where = "isDone = ?";
        String[] WhereArg = {"N"};
        sqlHandler.deleteData(TableName, Where, WhereArg);
        try {
            sqlHandler.insertData(TableName, Columns, Result, "");
        } catch (JSONException e) {
            e.printStackTrace();
            return String.valueOf(0);
        }
        return String.valueOf(1);
    }

    public Boolean deleteRecodedPolicy(String policyId) {
        String Where = "PolicyId = ?";
        String[] WhereArg = {policyId};
        sqlHandler.deleteData(SQLHandler.tblRecordedPolicies, Where, WhereArg);

        return true;
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
        String Query = "SELECT ProductName " +
                "FROM  tblProduct WHERE prodId = ? ";
        String arg[] = {prodId};
        JSONArray ProductName = sqlHandler.getResult(Query, arg);
        return ProductName;
    }

    private JSONArray getProductCode(String prodId) {
        String Query = "SELECT ProductCode " +
                "FROM  tblProduct WHERE prodId = ? ";
        String arg[] = {prodId};
        JSONArray ProductCode = sqlHandler.getResult(Query, arg);
        return ProductCode;
    }

    private JSONArray getOtherNames(String familyId) {
        String Query = "SELECT OtherNames " +
                "FROM  tblInsuree WHERE FamilyId = ? AND isHead = 1 ";
        String arg[] = {familyId};
        JSONArray OtherNames = sqlHandler.getResult(Query, arg);
        return OtherNames;
    }

    private JSONArray getLastName(String familyId) {
        String Query = "SELECT LastName " +
                "FROM  tblInsuree WHERE FamilyId = ? AND isHead = 1 ";
        String arg[] = {familyId};
        JSONArray LastName = sqlHandler.getResult(Query, arg);
        return LastName;
    }

    private JSONArray getInsuranceNumber(String familyId) {
        JSONArray InsuranceNumber = null;
        String Query = "SELECT CHFID " +
                "FROM  tblInsuree WHERE FamilyId = ? AND isHead  = 1";
        String arg[] = {familyId};
        try {
            InsuranceNumber = sqlHandler.getResult(Query, arg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return InsuranceNumber;
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

    public int DeleteRenewalOfflineRow(Integer RenewalId) {
        String Query = "DELETE FROM tblRenewals WHERE RenewalId=?";
        String arg[] = {String.valueOf(RenewalId)};
        JSONArray Renewal = sqlHandler.getResult(Query, arg);
        Renewal.toString();
        return 1; //Delete Success
    }

    public int UpdateRenewTable(int RenewalId) {
        ContentValues values = new ContentValues();
        values.put("isDone", "Y");
        try {
            sqlHandler.updateData("tblRenewals", values, "RenewalId = ?", new String[]{String.valueOf(RenewalId)});
        } catch (UserException e) {
            e.printStackTrace();
        }
        return 1;//Update Success
    }

    public String getOfflineFeedBack(String OfficerCode) {
        String Query = "SELECT ClaimId,ClaimUUID,OfficerId,OfficerCode,CHFID,LastName,OtherNames,HFCode,HFName,ClaimCode,DateFrom,DateTo,IMEI,Phone,FeedbackPromptDate " +
                "FROM  tblFeedbacks WHERE LOWER(OfficerCode) = ?  AND  isDone = ?";
        String[] arg = {OfficerCode.toLowerCase(), "N"};
        JSONArray FeedBacks = sqlHandler.getResult(Query, arg);
        return FeedBacks.toString();
    }

    public Boolean InsertFeedbacks(String Result) {
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


    public Cursor SearchPayer(String InputText, String LocationId) {
        String Query = "WITH RECURSIVE AllLocations(LocationId, ParentLocationId) AS\n" +
                "(\n" +
                " SELECT 0 LocationId, NULL ParentLocationId FROM tbllocations\n" +
                " UNION \n" +
                " SELECT   LocationId, ParentLocationId FROM tblLocations WHERE LocationId =" + LocationId + "\n" +
                " UNION \n" +
                " SELECT L.LocationId, L.ParentLocationId\n" +
                " FROM tblLocations L, AllLocations\n" +
                " WHERE L.LocationId = AllLocations.ParentLocationId\n" +
                ")\n" +
                "SELECT PayerId, PayerName,P.LocationId FROM tblPayer P \n" +
                "INNER JOIN ALLLocations AL ON IFNULL(P.LocationId,0) =IFNULL(AL.LocationId,0) \n" +
                "WHERE PayerName LIKE '%" + InputText + "%' \n" +
                "ORDER BY AL.ParentLocationId ,AL.LocationId";
        Cursor c = (Cursor) sqlHandler.getResult(Query, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public String CleanFeedBackTable(String ClaimUUID) {
        String Query = "DELETE FROM tblFeedbacks WHERE ClaimUUID = ?";
        String arg[] = {ClaimUUID};
        JSONArray Feedback = sqlHandler.getResult(Query, arg);
        return Feedback.toString();
    }

    public boolean UpdateFeedBack(String ClaimUUID) {
        ContentValues values = new ContentValues();
        values.put("isDone", "Y");
        try {
            sqlHandler.updateData("tblFeedbacks", values, "ClaimUUID = ?", new String[]{ClaimUUID});
        } catch (UserException e) {
            e.printStackTrace();
        }
        return true;//Update Success
    }

    private void DeleteFeedBackRenewal() {
        String Query = "DELETE FROM tblFeedbacks WHERE isDone = 'Y'; DELETE FROM tblRenewals WHERE isDone = 'Y' ";
        sqlHandler.getResult(Query, null);
    }

    private void DeleteFeedBacks() {
        String Query = "DELETE FROM tblFeedbacks WHERE isDone = 'Y'";
        sqlHandler.getResult(Query, null);
    }

    private void DeleteRenewals() {
        String Query = "DELETE FROM tblRenewals WHERE isDone = 'Y'";
        sqlHandler.getResult(Query, null);
    }

    @JavascriptInterface
    public int UpdatePolicy(int PolicyId, String PayDate, int policystatus) throws ParseException {
        ContentValues values = new ContentValues();
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
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
        String Effectivedate = null;

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


    private ProgressDialog pd = null;
    private ArrayList<String> enrolMessages = new ArrayList<>();

    @JavascriptInterface
    public void uploadEnrolment() throws Exception {
        ProgressDialog pd = null;
        pd = ProgressDialog.show(mContext, mContext.getResources().getString(R.string.Sync), mContext.getResources().getString(R.string.SyncProcessing));
        final ProgressDialog finalPd = pd;
        try {
            new Thread(() -> {
                try {
                    enrol_result = Enrol(0, 0, 0, 0, 1);
                } catch (UserException | JSONException | IOException | NumberFormatException e) {
                    finalPd.dismiss();
                    e.printStackTrace();
                }
                finalPd.dismiss();
                if (mylist.size() == 0) {
                    ((Activity) mContext).runOnUiThread(() -> {
                        if (enrol_result != 999) {
                            //if error is encountered
                            if (enrolMessages != null && enrolMessages.size() > 0) {
                                CharSequence[] charSequence = enrolMessages.toArray(new CharSequence[0]);
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle(mContext.getResources().getString(R.string.UploadFailureReport));
                                builder.setCancelable(false);
                                builder.setItems(charSequence, null);
                                builder.setPositiveButton(mContext.getResources().getString(R.string.Ok), (dialogInterface, i) -> dialogInterface.dismiss());
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                enrolMessages.clear();

                            } else {
                                ShowDialog(mContext.getResources().getString(R.string.FamilyUploaded));
                            }
                        } else {
                            ShowDialog(mContext.getResources().getString(R.string.NoDataAvailable));
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
    public void CreateEnrolmentXML() {
        new Thread(() -> {
            try {
                enrol_result = Enrol(0, 0, 0, 0, 2);
                if (enrol_result == 0) {
                    storageManager.requestCreateFile(MainActivity.REQUEST_CREATE_ENROL_EXPORT,
                            "application/octet-stream", getEnrolmentExportFilename());
                }
            } catch (Exception e) {
                Log.e("ENROL XML", "Error while creating enrolment xml", e);
            }
            if (mylist.size() == 0) {
                ((Activity) mContext).runOnUiThread(() -> {
                    if (enrol_result != 999) {
                        //if error is encountered
                        if (enrolMessages.size() > 0 && enrolMessages != null) {
                            CharSequence[] charSequences = enrolMessages.toArray(new CharSequence[(enrolMessages.size())]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle(mContext.getResources().getString(R.string.UploadFailureReport));
                            builder.setCancelable(false);
                            builder.setItems(charSequences, null);
                            builder.setPositiveButton(mContext.getResources().getString(R.string.Ok), (dialogInterface, i) -> dialogInterface.dismiss());
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            enrolMessages.clear();
                        } else {
                            //deleteImage();
                            ShowDialog(mContext.getResources().getString(R.string.XmlCreated));
                        }
                    } else {
                        AndroidUtils.showToast(mContext, R.string.NoDataAvailable);
                    }
                    //ShowDialog(mContext.getResources().getString(R.string.FamilyUploaded));
                });
            } else {
                ((Activity) mContext).runOnUiThread(
                        () -> ShowDialog(mylist.toString()));
            }
        }).start();
    }

    @JavascriptInterface
    public void CreateRenewalExport() {
        if (FileUtils.getFileCount(new File(global.getSubdirectory("Renewal"))) > 0) {
            new Thread(() -> storageManager.requestCreateFile(MainActivity.REQUEST_CREATE_RENEWAL_EXPORT,
                    "application/octet-stream", getRenewalExportFilename())).start();
        } else {
            AndroidUtils.showToast(mContext, R.string.NoDataAvailable);
        }

    }

    @JavascriptInterface
    public void CreateFeedbackExport() {
        if (FileUtils.getFileCount(new File(global.getSubdirectory("Feedback"))) > 0) {
            new Thread(() -> storageManager.requestCreateFile(MainActivity.REQUEST_CREATE_FEEDBACK_EXPORT,
                    "application/octet-stream", getFeedbackExportFilename())).start();
        } else {
            AndroidUtils.showToast(mContext, R.string.NoDataAvailable);
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
                mContext.getResources().getString(errorMessageId)
        );
    }

    public String getInsureeValidationError(String chfid, String lastname, String othername, int errorMessageId) {
        return String.format("Insuree %s %s %s %s",
                chfid, lastname, othername,
                mContext.getResources().getString(errorMessageId)
        );
    }

    public ArrayList<String> VerifyFamily() throws JSONException {
        ArrayList<String> FamilyIDs = new ArrayList<String>();
        boolean result = true;
        int IsOffline = 1;
        String FamilyId = null;

        String Query = null;
        Query = "SELECT FamilyId,isOffline  FROM tblFamilies WHERE InsureeId != ''" +
                " ORDER BY FamilyId";
        JSONArray familiesToUpload = sqlHandler.getResult(Query, null);

        if (familiesToUpload.length() > 0) {
            mylist.clear();
            for (int i = 0; i < familiesToUpload.length(); i++) {
                result = true;
                JSONObject object = null;
                try {
                    object = familiesToUpload.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                FamilyId = object != null ? object.getString("FamilyId") : null;
                IsOffline = Integer.parseInt(object.getString("isOffline"));

                Query = "SELECT F.FamilyId, F.InsureeId, F.LocationId, I.CHFID AS HOFCHFID, NULLIF(F.Poverty,'null') Poverty, NULLIF(F.FamilyType,'null') FamilyType, NULLIF(F.FamilyAddress,'null') FamilyAddress, NULLIF(F.Ethnicity,'null') Ethnicity, NULLIF(F.ConfirmationNo,'null') ConfirmationNo, F.ConfirmationType ConfirmationType,F.isOffline FROM tblFamilies F\n" +
                        "INNER JOIN tblInsuree I ON I.InsureeId = F.InsureeId WHERE F.InsureeId != ''";
                Query += " AND F.FamilyId = " + FamilyId + "";
                JSONArray familyArray = sqlHandler.getResult(Query, null);
                if (familyArray.length() == 0) {
                    result = false;
                }

                //get Insurees
                Query = "SELECT I.InsureeId, I.FamilyId, I.CHFID, I.LastName, I.OtherNames, I.DOB, I.Gender, NULLIF(I.Marital,'null') Marital, I.isHead, NULLIF(I.IdentificationNumber,'null') IdentificationNumber, NULLIF(I.Phone,'null') Phone, REPLACE(I.PhotoPath, RTRIM(PhotoPath, REPLACE(PhotoPath, '/', '')), '') PhotoPath, NULLIF(I.CardIssued,'null') CardIssued, NULLIF(I.Relationship,'null') Relationship, NULLIF(I.Profession,'null') Profession, NULLIF(I.Education,'null') Education, NULLIF(I.Email,'null') Email, CASE WHEN I.TypeOfId='null' THEN null ELSE I.TypeOfId END TypeOfId, NULLIF(I.HFID,'null') HFID, NULLIF(I.CurrentAddress,'null') CurrentAddress, NULLIF(I.GeoLocation,'null') GeoLocation, NULLIF(I.CurVillage,'null') CurVillage,I.isOffline \n" +
                        "FROM tblInsuree I \n" +
                        "WHERE I.InsureeId != ''";
                Query += " AND I.FamilyId  = " + FamilyId + "";
                JSONArray insureesArray = sqlHandler.getResult(Query, null);
                if (IsOffline == 1) {
                    if (insureesArray.length() == 0) {
                        result = false;
                    }
                }
                //get Policies
                Query = "SELECT PolicyId, FamilyId, EnrollDate, StartDate, NULLIF(EffectiveDate,'null') EffectiveDate, ExpiryDate, Policystatus, PolicyValue, ProdId, OfficerId, PolicyStage, isOffline\n" +
                        "FROM tblPolicy ";
                Query += " WHERE FamilyId = " + FamilyId + "";
                JSONArray policiesArray = sqlHandler.getResult(Query, null);

                if (IsOffline == 1 && isPolicyRequired() && policiesArray.length() == 0) { //Family offline without a policy
                    mylist.add(getFamilyValidationError(familyArray.getJSONObject(0).getString("HOFCHFID"), R.string.WithoutPolicy));
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
                        mylist.add(getFamilyValidationError(familyArray.getJSONObject(0).getString("HOFCHFID"), R.string.WithoutPremium));
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
        } else {
            result = false;
        }

        if (mylist.size() != 0) {
            ShowErrorMessages();
            mylist.clear();
        }
        return FamilyIDs;
    }

    public boolean VerifyPhoto(JSONArray insurees) throws JSONException {
        boolean result = true;
        InsureeImages[] images = new InsureeImages[insurees.length()];
        String PhotoPath = null;
        String FileName = "";
        int IsOffline = 1;
        JSONObject Insureeobject = null;
        for (int j = 0; j < insurees.length(); j++) {
            Insureeobject = insurees.getJSONObject(j);
            PhotoPath = (Insureeobject.getString("PhotoPath"));

            String s1 = Insureeobject.getString("isOffline");
            if (s1.equals("true") || s1.equals("1")) IsOffline = 1;
            else IsOffline = 0;

            if (IsOffline == 1) {
                if (!getRule("AllowInsureeWithoutPhoto")) {
                    if (PhotoPath == null || PhotoPath.length() == 0 || PhotoPath.equals("null")) {
                        mylist.add(getInsureeValidationError(
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

    private int Enrol(int oFamilyId, int oInsureeId, int oPolicyId, int oPremiumId, int CallerId) throws UserException, JSONException, IOException {
        ArrayList<String> verifiedId = new ArrayList<String>();
        mylist.clear();
        String Query;
        String[] args;
        int EnrolResult = oInsureeId;
        int IsOffline = 1;
        String Offline = null;

        String QueryF = null;
        String QueryI = null;
        String QueryPL = null;
        String QueryPR = null;
        String QueryIP = null;
        String QueryIA = null;

        //Verify Enrollments
        if (CallerId == 2) {
            verifiedId = VerifyFamily();
            if (verifiedId.size() == 0) {
                return 999;
            }
        }
        //Get all the families which are in offline state

        Query = "SELECT FamilyId,isOffline  FROM tblFamilies WHERE InsureeId != ''" +
                " ORDER BY FamilyId";
//        }
        JSONArray familiesToUpload = sqlHandler.getResult(Query, null);
        int length = 0;
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

            String FamilyId = null;
            String CHFNumber = null;
            JSONObject object = null;
            try {
                object = familiesToUpload.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // try {
            FamilyId = object != null ? object.getString("FamilyId") : null;
            IsOffline = Integer.parseInt(object.getString("isOffline"));


            if (IsOffline == 2) IsOffline = 0;

            Query = "SELECT F.FamilyId AS FamilyId, F.InsureeId AS InsureeId, F.LocationId, I.CHFID AS HOFCHFID, NULLIF(F.Poverty,'null') Poverty, NULLIF(F.FamilyType,'null') FamilyType, NULLIF(F.FamilyAddress,'null') FamilyAddress, NULLIF(F.Ethnicity,'null') Ethnicity, NULLIF(F.ConfirmationNo,'null') ConfirmationNo, F.ConfirmationType ConfirmationType,F.isOffline isOffline FROM tblFamilies F\n" +
                    "INNER JOIN tblInsuree I ON I.InsureeId = F.InsureeId WHERE";

            if (CallerId != 2) {
                Query += " F.FamilyId = " + FamilyId + "";
            } else {
                for (int j = 0; j < verifiedId.size(); j++) {
                    if ((verifiedId.size() - j) == 1) {
                        Query += " F.FamilyId == " + verifiedId.get(j) + "";
                    } else {
                        Query += " F.FamilyId == " + verifiedId.get(j) + " OR";
                    }

                }
                if (verifiedId.size() == 0) {
                    Query += " F.InsureeId != ''";
                } else {
                    Query += " AND F.InsureeId != ''";
                }
            }


            QueryF = Query;

            JSONArray familyArray = sqlHandler.getResult(Query, null);

            JSONArray newFamilyArray = new JSONArray();
            JSONObject ob1 = null;
            for (int j = 0; j < familyArray.length(); j++) {
                ob1 = familyArray.getJSONObject(j);
                String typeofId = ob1.getString("FamilyType");
                String poverty = ob1.getString("Poverty");
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
            Query = "SELECT I.InsureeId AS InsureeId, I.FamilyId AS FamilyId, I.CHFID, I.LastName, I.OtherNames, I.LastNameArab, I.GivenNameArab, I.DOB, I.Gender, NULLIF(I.Marital,'') Marital, I.isHead, NULLIF(I.IdentificationNumber,'null') IdentificationNumber, NULLIF(I.Phone,'null') Phone, REPLACE(I.PhotoPath, RTRIM(PhotoPath, REPLACE(PhotoPath, '/', '')), '') PhotoPath, NULLIF(I.CardIssued,'null') CardIssued, NULLIF(I.Relationship,'null') Relationship, NULLIF(I.Profession,'null') Profession, NULLIF(I.Education,'null') Education, NULLIF(I.Email,'null') Email, CASE WHEN I.TypeOfId='null' THEN null ELSE I.TypeOfId END TypeOfId, NULLIF(I.HFID,'null') HFID, NULLIF(I.CurrentAddress,'null') CurrentAddress, NULLIF(I.GeoLocation,'null') GeoLocation, NULLIF(I.CurVillage,'null') CurVillage,I.isOffline, I.Vulnerability \n" +
                    "FROM tblInsuree I \n" + " WHERE ";
            if (CallerId != 2) {
                Query += " I.FamilyId = " + FamilyId + " \n";
                if (Integer.parseInt(Offline) == 0) {
                    if (CallerId == 1) {
                        Query += " AND  I.InsureeId < 0" + "";
                    }
                }
            } else {
                Query += "(";
                for (int j = 0; j < verifiedId.size(); j++) {

                    if (getFamilyStatus(Integer.parseInt(verifiedId.get(j))) == 0) {
                        if ((verifiedId.size() - j) == 1) {
                            Query += "I.InsureeId < 0 AND  I.FamilyId == " + verifiedId.get(j) + "";
                        } else {
                            Query += "I.InsureeId < 0 AND  I.FamilyId == " + verifiedId.get(j) + " OR ";
                        }
                    } else {
                        if ((verifiedId.size() - j) == 1) {
                            Query += " I.FamilyId == " + verifiedId.get(j) + "";
                        } else {
                            Query += " I.FamilyId == " + verifiedId.get(j) + " OR ";
                        }
                    }
                }
                if (verifiedId.size() == 0) {
                    Query += " I.InsureeId != ''";
                }
                Query += ")";
            }

            if (CallerId == 0) Query += " AND  I.InsureeId = " + oInsureeId;

            JSONArray insureesArray = sqlHandler.getResult(Query, null);

            QueryI = Query;

            JSONObject O = null;
            //try {
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
            Query = "SELECT p.PolicyId AS PolicyId, FamilyId AS FamilyId, EnrollDate, StartDate, NULLIF(EffectiveDate,'null') EffectiveDate, ExpiryDate, Policystatus, PolicyValue, ProdId, OfficerId, PolicyStage, isOffline, bcn.ControlNumber \n" +
                    "FROM tblPolicy p LEFT JOIN tblBulkControlNumbers bcn on p.PolicyId=bcn.PolicyId WHERE ";
            if (CallerId != 2) {
                Query += " FamilyId = " + FamilyId;
            } else {
                for (int j = 0; j < verifiedId.size(); j++) {
                    if ((verifiedId.size() - j) == 1) {
                        Query += " FamilyId == " + verifiedId.get(j) + "";
                    } else {
                        Query += " FamilyId == " + verifiedId.get(j) + " OR";
                    }
                }
                if (verifiedId.size() == 0) {
                    Query += " FamilyId != ''";
                }
            }

            if (CallerId == 0) Query += " AND PolicyId = " + oPolicyId;

            JSONArray policiesArray = sqlHandler.getResult(Query, null);
            QueryPL = Query;

            if (CallerId == 1 || CallerId == 2) {
                if (IsOffline == 1 && isPolicyRequired() && policiesArray.length() == 0) {
                    mylist.add(getFamilyValidationError(
                            insureesArray.getJSONObject(0).getString("CHFID"),
                            R.string.WithoutPolicy
                    ));
                }
            }

            if (insureesArray.length() > 0 || policiesArray.length() > 0 || familyArray.length() > 0) {

                //get Premiums
                Query = "SELECT PR.PremiumId, PR.PolicyId, NULLIF(PR.PayerId,'null') PayerId, PR.Amount, PR.Receipt, PR.PayDate, PR.PayType, PR.isPhotoFee,PR.isOffline\n" +
                        "FROM tblPremium PR\n" +
                        "INNER JOIN tblPolicy PL ON PL.PolicyId = PR.PolicyId WHERE ";
                if (CallerId != 2) {
                    Query += " FamilyId = " + FamilyId;
                } else {
                    for (int j = 0; j < verifiedId.size(); j++) {
                        if ((verifiedId.size() - j) == 1) {
                            Query += " FamilyId == " + verifiedId.get(j) + "";
                        } else {
                            Query += " FamilyId == " + verifiedId.get(j) + " OR";
                        }

                    }
                    if (verifiedId.size() == 0) {
                        Query += " PR.PolicyId != ''";
                    }
                }

                if (CallerId == 0) {
                    Query += " AND PR.PremiumId  = " + oPremiumId;
                }

                JSONArray premiumsArray = sqlHandler.getResult(Query, null);
                QueryPR = Query;

                if (CallerId == 1 || CallerId == 2) {
                    if (IsOffline == 1 || policiesArray.length() != 0) { //Family offline or policy added to online family
                        if (isContributionRequired() && premiumsArray.length() == 0) {
                            mylist.add(getFamilyValidationError(
                                    insureesArray.getJSONObject(0).getString("CHFID"),
                                    R.string.WithoutPremium
                            ));
                        }
                    }
                }

                //get InsureePolicy
                Query = "SELECT DISTINCT(IP.InsureeId) AS InsureeId,IP.PolicyId,IP.EffectiveDate\n" +
                        "FROM tblInsureePolicy IP\n" +
                        "INNER JOIN tblPolicy PL ON PL.PolicyId = IP.PolicyId";
                if (CallerId == 0) Query += " AND  IP.InsureeId = " + oInsureeId;
                if (CallerId == 1) Query += " AND  PL.FamilyId = " + FamilyId;
                if (CallerId != 2) {
                    Query += " WHERE PL.FamilyId = " + FamilyId;
                }

                JSONArray InsureePolicyArray = sqlHandler.getResult(Query, null);
                QueryIP = Query;

                JSONObject objEnrol = new JSONObject();

                objEnrol.put("Family", familyArray);
                String Family = objEnrol.toString();

                objEnrol = new JSONObject();

                try {
                    objEnrol.put("Insuree", insureesArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String Insuree = String.valueOf(objEnrol);

                objEnrol = new JSONObject();
                objEnrol.put("Policy", policiesArray);
                String Policy = objEnrol.toString();

                objEnrol = new JSONObject();
                objEnrol.put("Premium", premiumsArray);
                String Premium = objEnrol.toString();

                objEnrol = new JSONObject();
                objEnrol.put("InsureePolicy", InsureePolicyArray);
                String InsureePolicy = objEnrol.toString();

                if (CallerId != 2) {
                    InsureeImages[] InsureeImages = FamilyPictures(insureesArray, CallerId);

                    if (mylist.size() == 0) {
                        JSONObject resultObj = new JSONObject();
                        JSONArray familyArr = new JSONArray();

                        JSONObject familyObj = familyArray.getJSONObject(0);

                        // Insuree + picture + attachments
                        JSONArray tempInsureesArray = new JSONArray();

                        for (int j = 0; j < insureesArray.length(); j++) {
                            tempInsureesArray = insureesArray;
                            JSONObject imgObj = new JSONObject();

                            if (InsureeImages[j] != null) {
                                imgObj.put("ImageName", InsureeImages[j].ImageName);
                                imgObj.put("ImageContent", Base64.encodeToString(InsureeImages[j].ImageContent, Base64.DEFAULT));
                                tempInsureesArray.getJSONObject(j).put("picture", imgObj);
                            }
                        }

                        // get Insuree attachments
                        Query = "SELECT Title,Filename,Content From tblInsureeAttachments WHERE FamilyId = " + FamilyId;
                        JSONArray insureeAttachments = sqlHandler.getResult(Query, null);
                        tempInsureesArray.getJSONObject(0).put("attachments", insureeAttachments);


                        familyObj.put("insurees", tempInsureesArray);

                        // Policy + premium

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

                        familyObj.put("policies", policiesArray);
                        familyObj.put("insureePolicy", InsureePolicyArray);
                        familyArr.put(familyObj);
                        resultObj.put("family", familyArr);

                        ToRestApi rest = new ToRestApi();
                        HttpResponse response = rest.postToRestApiToken(resultObj, "family");
                        String responseString = rest.getContent(response);
                        if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK
                                || response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_CREATED) {
                            boolean parsingErrorOccured = false;
                            try {
                                JSONObject responseObject = new JSONObject(responseString);
                                if (responseObject.has("error_occured") && responseObject.getBoolean("error_occured")) {
                                    EnrolResult = -400;
                                    enrolMessages.add(responseObject.getString("error_message"));
                                } else if (responseObject.has("response")) {
                                    EnrolResult = responseObject.getInt("response");
                                } else {
                                    throw new JSONException("Response does not have required information");
                                }
                            } catch (JSONException e) {
                                EnrolResult = -400;
                                parsingErrorOccured = true;
                            }

                            if (parsingErrorOccured) {
                                try {
                                    EnrolResult = Integer.parseInt(responseString);
                                } catch (NumberFormatException e) {
                                    Log.e("ENROLL", "Sync response is not a valid json or int");
                                }
                            }
                        } else {
                            enrolMessages.add(mContext.getResources().getString(R.string.HttpResponse, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
                            EnrolResult = -400;
                        }

                        if (EnrolResult != 0) {
                            Log.d("ENROL", "API RESPONSE: " + mContext.getResources().getString(R.string.HttpResponse, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()) + "\n" + responseString);
                        }

                    } else {
                        ShowErrorMessages();
                        break;
                    }
                } else {
                    if (!"".equals(QueryF)) {
                        fname = sqlHandler.getExportAsXML(QueryF, QueryI, QueryPL, QueryPR, QueryIP, global.getOfficerCode(), global.getOfficerId());
                        FamilyPictures(insureesArray, 2);
                    }
                    EnrolResult = 0;
                }
                int g = 0;
//             EnrolResult=1001;
                if (EnrolResult >= 0) {
                    updatePolicyRecords(Policy);
                    if (IsOffline == 0 && EnrolResult > 0) {
                        ContentValues values = new ContentValues();
                        String UpdateQuery = "";
                        values.put("isOffline", 2);
                        if (oPremiumId != 0 && premiumsArray.length() > 0) {
                            values.put("PremiumId", EnrolResult);
                            sqlHandler.updateData("tblPremium", values, "PremiumId = ?", new String[]{String.valueOf(oPremiumId)});
                            rtEnrolledId = EnrolResult;
                        } else if (oPolicyId != 0 && policiesArray.length() > 0) {
//                        values.put("PolicyId", EnrolResult);
//                        sqlHandler.updateData("tblPolicy", values, "PolicyId = ?", new String[]{String.valueOf(oPolicyId)});
                            UpdateQuery = "UPDATE tblPolicy SET PolicyId = " + EnrolResult + ", isOffline = 2 WHERE PolicyId = " + oPolicyId + " AND isOffline = 0";
                            sqlHandler.getResult(UpdateQuery, null);
                            rtEnrolledId = EnrolResult;

                        } else if (oInsureeId != 0 && insureesArray.length() > 0) {
                            values.put("InsureeId", EnrolResult);
                            sqlHandler.updateData("tblInsuree", values, "InsureeId = ?", new String[]{String.valueOf(oInsureeId)});
                            rtEnrolledId = EnrolResult;
                        } else if (oFamilyId > 0 && oInsureeId == 0 && oPolicyId == 0 && oPremiumId == 0) {
                            sqlHandler.updateData("tblFamilies", values, "FamilyId = ?", new String[]{String.valueOf(oFamilyId)});
                        }
                    } else {
                        ContentValues valuesF = new ContentValues();
                        valuesF.put("isOffline", 0);
                        sqlHandler.updateData("tblFamilies", valuesF, "FamilyId = ?", new String[]{String.valueOf(FamilyId)});
                    }

                    if (mylist.size() == 0) {
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
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.MissingHOF);
                            break;
                        case -2:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.DuplicateHOF);
                            break;
                        case -3:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.DuplicateInsuranceNumber);
                            break;
                        case -4:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.DuplicateReceiptNumber);
                            break;
                        case -5:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.DoubleHeadInServer);
                            break;
                        case -6:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.Interuption);
                            break;
                        case -400:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.ServerError);
                            break;
                        default:
                            ErrMsg = "[" + CHFNumber + "] " + mContext.getString(R.string.UncaughtException);
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

    private JSONObject emptyFamilySMS() {
        JSONObject familySms = new JSONObject();
        try {
            familySms.put("ApprovalOfSMS", null);
            familySms.put("LanguageOfSMS", null);
            familySms.put("FamilyID", null);
            return familySms;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePolicyRecords(String policy) throws JSONException {
        JSONObject object = new JSONObject(policy);
        JSONObject ob = null;
        String policies = object.getString("Policy");
        JSONArray jsonArray = new JSONArray(policies);
        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                ob = jsonArray.getJSONObject(i);
                int policyId = ob.getInt("PolicyId");
                updateUploadedDate(policyId);
            }
        }
    }

    public void clearDirectory(String directory) {
        File[] files = new File(global.getSubdirectory(directory)).listFiles();
        if (files != null) {
            FileUtils.deleteFiles(files);
        }
    }

    public InsureeImages[] FamilyPictures(JSONArray insurees, int CallerId) throws IOException {
        InsureeImages[] images = new InsureeImages[insurees.length()];

        String PhotoPath = null;
        String FileName = "";
        String chfid = null;
        String lastname = null;
        String othername = null;
        int IsOffline = 1;
        JSONObject Insureeobject = null;
        for (int j = 0; j < insurees.length(); j++) {
            try {
                Insureeobject = insurees.getJSONObject(j);
                PhotoPath = (Insureeobject.getString("PhotoPath"));
                chfid = (Insureeobject.getString("CHFID"));
                lastname = (Insureeobject.getString("LastName"));
                othername = (Insureeobject.getString("OtherNames"));

                String s1 = Insureeobject.getString("isOffline");
                if (s1.equals("true") || s1.equals("1")) IsOffline = 1;
                else if (s1.equals("false") || s1.equals("0")) IsOffline = 0;
                else IsOffline = Integer.parseInt(s1);

                if (PhotoPath.length() > 0 && !PhotoPath.equals("null") && PhotoPath != null) {
                    FileName = PhotoPath;
                    final String newfile = FileName;

                    files = GetListOfImages(global.getImageFolder(), newfile);

                    if (files.length > 0) {
                        int size = (int) files[0].length();
                        byte[] imgcontent = new byte[size];

                        try {
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(files[0]));
                            buf.read(imgcontent, 0, imgcontent.length);
                            copy(newfile, imgcontent);
                            buf.close();

                            if (CallerId != 2) {
                                InsureeImages img = new InsureeImages(files[0].getName(), imgcontent);
                                images[j] = img;
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        byte[] imgcontent = new byte[0];
                        if (CallerId != 2) {
                            InsureeImages img = new InsureeImages("", imgcontent);
                            images[j] = img;
                        }

                    }

                } else {
                    if (IsOffline == 1) {
                        if (getRule("AllowInsureeWithoutPhoto")) {
                            byte[] empty = new byte[0];
                            InsureeImages img = new InsureeImages("", empty);
                            images[j] = img;
                        } else {
                            mylist.add(getInsureeValidationError(
                                    chfid, lastname, othername,
                                    R.string.WithoutPhoto
                            ));
                            ShowErrorMessages();
                            break;
                        }
                    } else {
                        byte[] empty = new byte[0];
                        InsureeImages img = new InsureeImages("", empty);
                        images[j] = img;
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return images;
    }

/*    public byte[] extractBytes (String ImageName) throws IOException {
        // open image
        File imgPath = new File(ImageName);
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage .getRaster();
        DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

        return ( data.getData() );
    }*/

    //@TargetApi(Build.VERSION_CODES.KITKAT)
    public static void copy(String name, byte[] data) throws IOException {
        OutputStream out;
        String photosDir = Global.getGlobal().getSubdirectory("Photos");

        File file = new File(photosDir + File.separator + name);
        file.createNewFile();
        out = new FileOutputStream(file);

        out.write(data);
        out.close();
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
        File zipFile = FileUtils.createTempFile(mContext, "exports/" + getEnrolmentExportFilename(), true);
        String password = getRarPwd();
        File imageDir = new File(global.getImageFolder());
        File familyDir = new File(global.getSubdirectory("Family"));

        return ZipUtils.zipDirectories(zipFile, password, familyDir, imageDir);
    }

    public File zipRenewalFiles() {
        File zipFile = FileUtils.createTempFile(mContext, "exports/" + getRenewalExportFilename(), true);
        String password = getRarPwd();
        File familyDir = new File(global.getSubdirectory("Renewal"));

        return ZipUtils.zipDirectories(zipFile, password, familyDir);
    }

    public File zipFeedbackFiles() {
        File zipFile = FileUtils.createTempFile(mContext, "exports/" + getFeedbackExportFilename(), true);
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

    public boolean unZip(String FileName) {
        String targetPath = global.getSubdirectory("Database") + File.separator + FileName;
        String unzippedFolderPath = global.getSubdirectory("Database");
        //String unzippedFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/IMIS/Enrolment/Enrolment_"+global.getOfficerCode()+"_"+d+".xml";
        String password = getRarPwd();

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
        global = (Global) mContext.getApplicationContext();
        String password = "";
        try {
            SharedPreferences sharedPreferences = global.getSharedPreferences("MyPref", 0);

            if (!sharedPreferences.contains("rarPwd")) {
                password = AppInformation.DomainInfo.getDefaultRarPassword();
            } else {
                String encryptedRarPassword = sharedPreferences.getString("rarPwd", AppInformation.DomainInfo.getDefaultRarPassword());
                String trimEncryptedPassword = encryptedRarPassword.trim();
                salt = sharedPreferences.getString("salt", null);
                String trimSalt = salt.trim();
                password = decryptRarPwd(trimEncryptedPassword, trimSalt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return password;
    }

    public void DeleteImages(JSONArray insurees, ArrayList<String> FamilyIDs, int CallerId) {

        String PhotoPath = null;
        String FileName = "";
        JSONObject Insureeobject = null;
        for (int j = 0; j < insurees.length(); j++) {
            try {
                Insureeobject = insurees.getJSONObject(j);
                if (CallerId == 2 && FamilyIDs.size() != 0) {
                    for (int x = 0; x < FamilyIDs.size(); x++) {
                        if ((Insureeobject.getString("FamilyId").equals(FamilyIDs.get(x)))) {
                            PhotoPath = (Insureeobject.getString("PhotoPath"));
                            if (PhotoPath.length() > 0 && !PhotoPath.equals("null") && PhotoPath != null) {
                                FileName = PhotoPath;
                                final String newfile = FileName;

                                files = GetListOfImages(global.getImageFolder(), newfile);
                                if (files.length > 0) {
                                    for (int i = 0; i < files.length; i++) {
                                        files[i].delete();
                                    }
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                } else {
                    PhotoPath = (Insureeobject.getString("PhotoPath"));
                    if (PhotoPath.length() > 0 && !PhotoPath.equals("null") && PhotoPath != null) {
                        FileName = PhotoPath;
                        final String newfile = FileName;

                        files = GetListOfImages(global.getImageFolder(), newfile);
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

        ((Activity) mContext).runOnUiThread(() -> {
            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(mContext);
            View promptsView = li.inflate(R.layout.error_message, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final TextView textView1 = (TextView) promptsView.findViewById(R.id.textView1);
            final RecyclerView error_message = (RecyclerView) promptsView.findViewById(R.id.error_message);

            enrollmentReport = new EnrollmentReport(mContext, mylist);

            error_message.setLayoutManager(new LinearLayoutManager(mContext));
            error_message.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
            error_message.setAdapter(enrollmentReport);

            String title = mContext.getString(R.string.failedToUpload);
            textView1.setText(title.toUpperCase());
            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, id) -> dialog.cancel())
                    .show();
        });

    }


    public Boolean isOfficerCodeValid(String OfficerCode) throws JSONException {
        String Query = "SELECT OfficerId , OtherNames || ' ' || LastName AS OfficerName\n" +
                "FROM tblOfficer WHERE LOWER(Code)= LOWER(?)";
        String arg[] = {OfficerCode.toLowerCase()};
        JSONArray Officer = sqlHandler.getResult(Query, arg);
        //  Officer.toString();
        int Count = Officer.length();
        String OfficerName = null;
        JSONObject object;
        if (Count > 0) {
            object = Officer.getJSONObject(0);
            global = (Global) mContext.getApplicationContext();
            int OfficerId = Integer.parseInt(object.getString("OfficerId"));
            OfficerName = object.getString("OfficerName");
            global.setOfficerId(OfficerId);
            global.setOfficerName(OfficerName);
            Officer.toString();
            return true;
        } else {
            return false;
        }
    }

    // Login to Api from JavaScript (call method LoginToken)
    @JavascriptInterface
    public boolean LoginJI(final String Username, final String Password) {
        return LoginToken(Username, Password);
    }

    @JavascriptInterface
    public void Logout() {
        global.getJWTToken().clearToken();

        ((Activity) mContext).runOnUiThread(
                () -> MainActivity.SetLoggedIn(mContext.getResources().getString(R.string.Login), mContext.getResources().getString(R.string.Logout))
        );
    }

    @JavascriptInterface
    public boolean isLoggedIn() {
        return global.isLoggedIn();
    }

    // Login to API and get Token JWT
    public boolean LoginToken(final String Username, final String Password) {
        global = (Global) mContext.getApplicationContext();

        ToRestApi rest = new ToRestApi();

        JSONObject object = new JSONObject();
        try {
            object.put("UserName", Username);
            object.put("Password", Password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String functionName = "login";

        HttpResponse response = rest.postToRestApi(object, functionName);

        String content = null;

        HttpEntity respEntity = response.getEntity();

        if (respEntity != null) {
            try {
                content = EntityUtils.toString(respEntity);
                if (content != null && content.length() > 0
                        && response.getStatusLine().getStatusCode() >= 400) {
                    Log.e("HTTP", "Error response: " + content);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
            JSONObject ob = null;
            String jwt = "";
            String validTo = "";
            try {
                ob = new JSONObject(content);
                jwt = ob.getString("access_token");
                validTo = ob.getString("expires_on");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            global.getJWTToken().saveTokenText(jwt, validTo, global.getOfficerCode());

            ((Activity) mContext).runOnUiThread(
                    () -> MainActivity.SetLoggedIn(mContext.getResources().getString(R.string.Login), mContext.getResources().getString(R.string.Logout))
            );

            return true;
        }
        return false;
    }

    @JavascriptInterface
    public int isValidLogin(final String Username, final String Password) throws InterruptedException {
        global = (Global) mContext.getApplicationContext();
        CallSoap cs = new CallSoap();
        cs.setFunctionName("isValidLogin");
        UserId = cs.isUserLoggedIn(Username, Password);
        global.setUserId(UserId);
        ((Activity) mContext).runOnUiThread(() -> MainActivity.SetLoggedIn(mContext.getResources().getString(R.string.Login), mContext.getResources().getString(R.string.Logout)));
        return UserId;
    }

    public int Login(final String Username, final String Password) throws InterruptedException {
        global = (Global) mContext.getApplicationContext();
        CallSoap cs = new CallSoap();
        cs.setFunctionName("isValidLogin");
        UserId = cs.isUserLoggedIn(Username, Password);
        global.setUserId(UserId);
        ((Activity) mContext).runOnUiThread(
                () -> MainActivity.SetLoggedIn(mContext.getResources().getString(R.string.Login), mContext.getResources().getString(R.string.Logout))
        );
        return UserId;
    }

    public String GetSnapshotIndicators() {
        String snapshot = null;
        global = (Global) mContext.getApplicationContext();
        CallSoap cs = new CallSoap();
        cs.setFunctionName("GetSnapshotIndicators");
        try {
            snapshot = cs.GetSnapshotIndicators("2017-07-01", "1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return snapshot;
    }

    private void DeleteUploadedData(final int FamilyId, ArrayList<String> FamilyIDs, int CallerId) {
        if (FamilyIDs.size() == 0) {
            FamilyIDs = new ArrayList<>() {{
                add(String.valueOf(FamilyId));
            }};
        }


        String wherePolicyIdIn = "PolicyId IN (SELECT PolicyId FROM tblPolicy WHERE "
                + combineFamilyIdsInWhereStatement(FamilyIDs) + ")";
        deleteUploadedTableData(SQLHandler.tblPremium, wherePolicyIdIn);
        deleteUploadedTableData(SQLHandler.tblInsureePolicy, wherePolicyIdIn);
        deleteUploadedTableData(SQLHandler.tblBulkControlNumbers, wherePolicyIdIn);
        deleteUploadedTableData(SQLHandler.tblPolicy, FamilyIDs);
        deleteUploadedTableData(SQLHandler.tblInsuree, FamilyIDs);
        deleteUploadedTableData(SQLHandler.tblInsureeAttachments, FamilyIDs);
        deleteUploadedTableData(SQLHandler.tblFamilySMS, FamilyIDs);
        deleteUploadedTableData(SQLHandler.tblFamilies, FamilyIDs);
    }

    private void deleteUploadedTableData(String tableName, int familyId) {
        String where = "FamilyId=" + familyId + "";
        deleteUploadedTableData(tableName, where);
    }

    private void deleteUploadedTableData(String tableName, ArrayList<String> familyIDs) {
        String where = "";

        if (familyIDs.size() != 0) {
            where += combineFamilyIdsInWhereStatement(familyIDs);
        } else {
            where += " FamilyId = " + familyIDs + "";
        }
        deleteUploadedTableData(tableName, where);
    }

    private String combineFamilyIdsInWhereStatement(ArrayList<String> familyIDs) {
        String combined = "";
        for (int j = 0; j < familyIDs.size(); j++) {
            if ((familyIDs.size() - j) == 1) {
                combined += " FamilyId == " + familyIDs.get(j) + "";
            } else {
                combined += " FamilyId == " + familyIDs.get(j) + " OR";
            }
        }
        return combined;
    }

    private void deleteUploadedTableData(String tableName, String where) {
        String query = "DELETE FROM " + tableName + " WHERE " + where;
        sqlHandler.getResult(query, null);
    }

    @JavascriptInterface
    public void uploadFeedbacks() {
        final int totalFiles;
        final ProgressDialog pd;

        if (!global.isNetworkAvailable()) {
            ShowDialog(mContext.getResources().getString(R.string.NoInternet));
            return;
        }

        File feedbackDir = new File(global.getSubdirectory("Feedback"));
        File[] xmlFiles = feedbackDir.listFiles((file) -> file.getName().endsWith(".xml"));
        File[] jsonFiles = feedbackDir.listFiles((file) -> file.getName().endsWith(".json"));

        if (xmlFiles == null || jsonFiles == null) {
            ShowDialog(mContext.getResources().getString(R.string.NoFiles));
            DeleteFeedBacks();
            return;
        }

        totalFiles = jsonFiles.length;

        if (totalFiles == 0) {
            ShowDialog(mContext.getResources().getString(R.string.NoDataAvailable));
            DeleteFeedBacks();
            return;
        }

        pd = AndroidUtils.showProgressDialog(mContext, R.string.Sync, R.string.SyncProcessing);

        new Thread(() -> {
            ToRestApi rest = new ToRestApi();
            JSONObject obj;
            int uploadsAccepted = 0, uploadsRejected = 0, uploadFailed = 0;
            for (int i = 0; i < jsonFiles.length; i++) {
                String jsonText = FileUtils.readFileAsUTF8String(jsonFiles[i]);

                HttpResponse response = null;
                int responseCode = -1;
                int uploadStatus = -1;

                try {
                    obj = new JSONObject(jsonText).getJSONObject("feedback");
                    response = rest.postToRestApiToken(obj, "feedback");
                    if (response != null) {
                        responseCode = response.getStatusLine().getStatusCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            uploadStatus = Integer.parseInt(rest.getContent(response));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (response == null || responseCode != HttpURLConnection.HTTP_OK || uploadStatus == ToRestApi.UploadStatus.ERROR) {
                    uploadFailed += 1;
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    if (uploadStatus == ToRestApi.UploadStatus.ACCEPTED) {
                        uploadsAccepted += 1;
                        MoveFile(xmlFiles[i], 1);
                        MoveFile(jsonFiles[i], 1);
                    } else {
                        uploadsRejected += 1;
                        MoveFile(xmlFiles[i], 2);
                        MoveFile(jsonFiles[i], 2);
                    }
                }
            }

            if (uploadsAccepted == 0 && uploadsRejected == 0) {
                result = ToRestApi.UploadStatus.ERROR;
            } else if (uploadsAccepted > 0 && uploadsRejected == 0) {
                result = ToRestApi.UploadStatus.ACCEPTED;
            } else if (uploadsAccepted == 0 && uploadsRejected > 0) {
                result = ToRestApi.UploadStatus.REJECTED;
            } else {
                //There are both accepted and rejected entries in a bulk
                result = ToRestApi.UploadStatus.ACCEPTED;
            }

            ((Activity) mContext).runOnUiThread(() -> {
                switch (result) {
                    case ToRestApi.UploadStatus.NO_RESPONSE:
                        ShowDialog(mContext.getResources().getString(R.string.FTPConnectionFailed));
                        break;
                    case ToRestApi.UploadStatus.ERROR:
                        ShowDialog(mContext.getResources().getString(R.string.SomethingWrongServer));
                        break;
                    default:
                        ShowDialog(mContext.getResources().getString(R.string.BulkUpload));
                }
            });
            pd.dismiss();
        }).start();
    }

    @JavascriptInterface
    public void uploadRenewals() {
        final int totalFiles;
        final ProgressDialog pd;

        if (!global.isNetworkAvailable()) {
            ShowDialog(mContext.getResources().getString(R.string.NoInternet));
            return;
        }

        File renewalDir = new File(global.getSubdirectory("Renewal"));
        File[] xmlFiles = renewalDir.listFiles((file) -> file.getName().endsWith(".xml"));
        File[] jsonFiles = renewalDir.listFiles((file) -> file.getName().endsWith(".json"));

        if (xmlFiles == null || jsonFiles == null) {
            ShowDialog(mContext.getResources().getString(R.string.NoFiles));
            DeleteRenewals();
            return;
        }

        totalFiles = jsonFiles.length;

        if (totalFiles == 0) {
            ShowDialog(mContext.getResources().getString(R.string.NoDataAvailable));
            DeleteRenewals();
            return;
        }

        pd = AndroidUtils.showProgressDialog(mContext, R.string.Sync, R.string.SyncProcessing);

        new Thread(() -> {
            StringBuilder messageBuilder = new StringBuilder();
            String messageFormat = "[%s] %s\n";
            Map<Integer, String> messages = new HashMap<>();
            messages.put(ToRestApi.RenewalStatus.ACCEPTED, mContext.getResources().getString(R.string.RenewalAccepted));
            messages.put(ToRestApi.RenewalStatus.ALREADY_ACCEPTED, mContext.getResources().getString(R.string.RenewalAlreadyAccepted));
            messages.put(ToRestApi.RenewalStatus.REJECTED, mContext.getResources().getString(R.string.RenewalRejected));
            messages.put(ToRestApi.RenewalStatus.DUPLICATE_RECEIPT, mContext.getResources().getString(R.string.DuplicateReceiptNumber));
            messages.put(ToRestApi.RenewalStatus.GRACE_PERIOD_EXPIRED, mContext.getResources().getString(R.string.GracePeriodExpired));
            messages.put(ToRestApi.RenewalStatus.CONTROL_NUMBER_ERROR, mContext.getResources().getString(R.string.ControlNumberError));
            messages.put(ToRestApi.RenewalStatus.UNEXPECTED_EXCEPTION, mContext.getResources().getString(R.string.UnexpectedException));

            ToRestApi rest = new ToRestApi();
            JSONObject obj;

            int acceptedRenewals = 0;
            for (int i = 0; i < jsonFiles.length; i++) {
                String jsonText = FileUtils.readFileAsUTF8String(jsonFiles[i]);
                String renewalInsureeNo = "";

                HttpResponse response;
                int responseCode;
                int uploadStatus = -1;

                try {
                    obj = new JSONObject(jsonText).getJSONObject("Policy");
                    renewalInsureeNo = obj.getString("CHFID");

                    response = rest.postToRestApiToken(obj, "policy/renew");
                    if (response != null) {
                        responseCode = response.getStatusLine().getStatusCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            uploadStatus = Integer.parseInt(EntityUtils.toString(response.getEntity()));
                        } else {
                            if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                                throw new JSONException("Renewal file caused 400 BAD REQUEST");
                            } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                                messageBuilder.append(String.format(messageFormat, renewalInsureeNo, mContext.getResources().getString(R.string.LoginFail)));
                                break;
                            } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                                throw new IOException("500 INTERNAL ERROR");
                            }
                        }
                    } else {
                        Log.e(LOG_TAG_RENEWAL, "Server not responding");
                        messageBuilder.append(String.format(messageFormat, renewalInsureeNo, mContext.getResources().getString(R.string.SomethingWrongServer)));
                        break;
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG_RENEWAL, "Invalid renewal json format", e);
                    messageBuilder.append(String.format(messageFormat, renewalInsureeNo, mContext.getResources().getString(R.string.InvalidRenewalFile)));
                    continue;
                } catch (IOException e) {
                    Log.e(LOG_TAG_RENEWAL, "Error while sending renewal", e);
                    messageBuilder.append(String.format(messageFormat, renewalInsureeNo, mContext.getResources().getString(R.string.SomethingWrongServer)));
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

            String successMessage = mContext.getResources().getString(R.string.BulkUpload);
            String failMessage = mContext.getResources().getString(R.string.RenewalRejected);
            String resultMessage;
            if (acceptedRenewals == 0) {
                resultMessage = failMessage;
            } else if (acceptedRenewals != jsonFiles.length) {
                resultMessage = successMessage + "\n" + messageBuilder.toString();
            } else {
                resultMessage = successMessage;
            }

            ((Activity) mContext).runOnUiThread(() -> AndroidUtils.showDialog(mContext, resultMessage));

            pd.dismiss();
        }).start();
    }

    private File[] GetListOfFiles(String DirectoryPath, final String FileType, final String FileName) {
        File Directory = new File(DirectoryPath);
        final Pattern p = Pattern.compile("(RenPol_)");
        FilenameFilter filter = (dir, filename) -> {
            if (FileType.equals("Image")) {
                //  return filename.equalsIgnoreCase(FileName);
                return filename.endsWith(".jpg");
            } else if (FileType.equals("Feedback")) {
                return filename.startsWith("feedback_");
            } else {
                return filename.startsWith("RenPol_");
            }
        };
        return Directory.listFiles(filter);
    }

    private File[] GetListOfJSONFiles(String DirectoryPath, final String FileType, final String FileName) {
        File Directory = new File(DirectoryPath);
        final Pattern p = Pattern.compile("(RenPol_)");
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                if (FileType.equals("Image")) {
                    //  return filename.equalsIgnoreCase(FileName);
                    return filename.endsWith(".jpg");
                } else if (FileType.equals("Feedback")) {
                    return filename.startsWith("feedbackJSON_");
                } else {
                    return filename.startsWith("RenPolJSON_");
                }
            }
        };
        return Directory.listFiles(filter);
    }

    private File[] GetListOfImages(String DirectoryPath, final String FileName) {
        File Directory = new File(DirectoryPath);
        return Directory.listFiles((dir, filename) -> filename.equalsIgnoreCase(FileName));
    }

    @JavascriptInterface
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


    private void RegisterUploadDetails(String ImageName) {
        String[] FileName = ImageName.split("_");
        String CHFID, OfficerCode;
        if (FileName.length > 0) {
            CHFID = FileName[0];
            OfficerCode = FileName[1];
            CallSoap cs = new CallSoap();
            cs.setFunctionName("InsertPhotoEntry");
            cs.InsertPhotoEntry(CHFID, OfficerCode, ImageName);
        }
    }

    public void getOfficerVillages(final String OfficerCode) {
        final ToRestApi toRestApi = new ToRestApi();
        final JSONObject object = new JSONObject();
        final String[] str = {""};


        Thread thread = new Thread() {
            public void run() {

                try {
                    object.put("enrollment_officer_code", OfficerCode);
                    str[0] = toRestApi.postObjectToRestApiObject(object, "/Locations/GetOfficerVillages");
                    JSONObject object1 = null;
                    object1 = new JSONObject(str[0]);
                    String OfficerVillages = object1.getString("data");

                    final JSONArray arr = new JSONArray(OfficerVillages);

                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                insertOfficerVillages(arr);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();
    }

    @JavascriptInterface
    public void popUpOfficerDialog() {
        ((MainActivity) mContext).ShowEnrolmentOfficerDialog();
    }

    @JavascriptInterface
    public void downloadMasterData() throws InterruptedException {
        ProgressDialog pd = ProgressDialog.show(mContext, mContext.getResources().getString(R.string.Sync), mContext.getResources().getString(R.string.DownloadingMasterData));
        new Thread(() -> {
            try {
                startDownloading();

                ((Activity) mContext).runOnUiThread(() -> {
                    ShowDialog(mContext.getResources().getString(R.string.DataDownloadedSuccess));
                    Global global = Global.getGlobal();
                    global.setOfficerCode("");
                    ((MainActivity) mContext).ShowEnrolmentOfficerDialog();
                });
            } catch (JSONException e) {
                Log.e("MASTERDATA", "Error while parsing master data", e);
            } catch (UserException e) {
                Log.e("MASTERDATA", "Error while downloading master data", e);
                ((Activity) mContext).runOnUiThread(() -> {
                    AndroidUtils.showDialog(mContext,
                            mContext.getResources().getString(R.string.DataDownloadedFailed),
                            e.getMessage());
                });
            } finally {
                pd.dismiss();
            }
        }).start();
    }

    public void importMasterData(String data) throws JSONException, UserException {
        try {
            JSONArray masterDataArray = new JSONArray(data);
            processOldFormat(masterDataArray);
        } catch (JSONException e) {
            try {
                JSONObject masterDataObject = new JSONObject(data);
                processNewFormat(masterDataObject);
            } catch (JSONException e2) {
                throw new UserException(mContext.getResources().getString(R.string.DownloadMasterDataFailed));
            }
        }
    }


    public void startDownloading() throws JSONException, UserException {
        ToRestApi rest = new ToRestApi();
        HttpResponse response = rest.getFromRestApi("master");
        String error = rest.getHttpError(mContext, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        if (error == null) {
            String MD = rest.getContent(response);
            JSONObject masterData = new JSONObject(MD);

            processNewFormat(masterData);
        } else {
            throw new UserException(error);
        }
    }

    private void processOldFormat(JSONArray masterData) throws UserException {
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
            throw new UserException(mContext.getResources().getString(R.string.DownloadMasterDataFailed));
        }
    }

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
            throw new UserException(mContext.getResources().getString(R.string.DownloadMasterDataFailed));
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

    private boolean insertConfirmationTypes(JSONArray jsonArray) throws JSONException {
        try {
            String[] Columns = getColumnNames(jsonArray);
            sqlHandler.insertData("tblConfirmationTypes", Columns, jsonArray.toString(), "DELETE FROM tblConfirmationTypes");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean insertControls(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblControls", Columns, jsonArray.toString(), "DELETE FROM tblControls;");
        return true;
    }

    private boolean insertEducation(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblEducations", Columns, jsonArray.toString(), "DELETE FROM tblEducations;");
        return true;
    }

    private boolean insertFamilyTypes(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblFamilyTypes", Columns, jsonArray.toString(), "DELETE FROM tblFamilyTypes;");
        return true;
    }

    private boolean insertHF(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblHF", Columns, jsonArray.toString(), "DELETE FROM tblHF;");
        return true;
    }

    private boolean insertIdentificationTypes(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblIdentificationTypes", Columns, jsonArray.toString(), "DELETE FROM tblIdentificationTypes;");
        return true;
    }

    private boolean insertLanguages(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblLanguages", Columns, jsonArray.toString(), "DELETE FROM tblLanguages;");
        return true;
    }

    private boolean insertLocations(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblLocations", Columns, jsonArray.toString(), "DELETE FROM tblLocations;");

        return true;
    }

    private boolean insertOfficers(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblOfficer", Columns, jsonArray.toString(), "DELETE FROM tblOfficer;");
        return true;
    }

    private boolean insertPayers(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblPayer", Columns, jsonArray.toString(), "DELETE FROM tblPayer;");
        return true;
    }

    private boolean insertProducts(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblProduct", Columns, jsonArray.toString(), "DELETE FROM tblProduct;");
        return true;
    }

    private boolean insertProfessions(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblProfessions", Columns, jsonArray.toString(), "DELETE FROM tblProfessions;");
        return true;
    }

    private boolean insertRelations(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblRelations", Columns, jsonArray.toString(), "DELETE FROM tblRelations;");
        return true;
    }

    private boolean insertPhoneDefaults(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblIMISDefaultsPhone", Columns, jsonArray.toString(), "DELETE FROM tblIMISDefaultsPhone;");
        return true;
    }

    private boolean insertGenders(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblGender", Columns, jsonArray.toString(), "DELETE FROM tblGender;");
        return true;
    }

    private boolean insertOfficerVillages(JSONArray jsonArray) throws JSONException {
        String[] Columns = getColumnNames(jsonArray);
        sqlHandler.insertData("tblOfficerVillages", Columns, jsonArray.toString(), "DELETE FROM tblOfficerVillages;");
        return true;
    }

    public int isMasterDataAvailable() {
        String Query = "SELECT * FROM tblLanguages";
        JSONArray Languages = sqlHandler.getResult(Query, null);
        return Languages.length();

    }

    public JSONArray getLanguage() {
        String Query = "SELECT * FROM tblLanguages";

        return sqlHandler.getResult(Query, null);
    }

    public JSONArray getClaimUUIDByClaimCode(String claimCode) {
        String Query = "SELECT ClaimUUID FROM tblFeedbacks WHERE ClaimCode ='" + claimCode + "'";

        return sqlHandler.getResult(Query, null);
    }


    @JavascriptInterface
    public int getTotalFamily() {

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
            TotalFamilies = Integer.parseInt(object != null ? object.getString("Families") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalFamilies;
    }

    @JavascriptInterface
    public int getTotalInsuree() {
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
            TotalInsuree = Integer.parseInt(object != null ? object.getString("Insuree") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalInsuree;
    }

    @JavascriptInterface
    public int getTotalPolicy() {
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
            TotalPolicies = Integer.parseInt(object != null ? object.getString("Policies") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPolicies;
    }

    @JavascriptInterface
    public int getTotalPremium() {
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
            TotalPremiums = Integer.parseInt(object != null ? object.getString("Premiums") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

    public String getPayer(int LocationId) {
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
        String PayerQuery = "SELECT P.PayerId, P.PayerName, P.LocationId FROM tblPayer P\n" +
                " LEFT OUTER JOIN tblLocations L ON P.LocationId = L.LocationId\n" +
                " WHERE (P.LocationId = " + OfficeLocationId + " OR L.ParentLocationId = P.LocationId OR P.LocationId = 'null' OR P.LocationId = '')  " +
                " ORDER BY L.LocationId";
        JSONArray Payers = sqlHandler.getResult(PayerQuery, null);
        return Payers.toString();
    }

    public static void setInsuranceNo(String insuranceNo) {
        InsuranceNo = insuranceNo;

    }

    @JavascriptInterface
    public void getScannedNumber() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        try {
            ((Activity) mContext).startActivityForResult(intent, RESULT_SCAN);
        } catch (Exception e) {
            Log.e("ENROL", "Error while trying to initiate QR scan", e);
        }
    }

    @JavascriptInterface
    public void clearInsuranceNo() {
        InsuranceNo = "";
    }

    @JavascriptInterface
    public String getOfficerLocation() {
        global = (Global) mContext.getApplicationContext();
        String Query = " SELECT RegionId, DistrictId FROM uvwLocations UL\n" +
                " INNER JOIN tblOfficer O ON O.LocationId = UL.DistrictId \n" +
                " WHERE LOWER(O.Code) = '" + global.getOfficerCode().toLowerCase() + "'";
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        return jsonArray.toString();
    }

    @JavascriptInterface
    public String getFamilyPolicy(int FamilyId) {
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
    public int getMaxInstallments(String id) {
        int PolicyId = Integer.parseInt(id);
        int MaxInstallments = 0;
        JSONArray MaxInstallArray = null;

        int ProdId = getProdId(PolicyId);
        try {
            String Query = "SELECT MaxInstallments from tblProduct where ProdId = " + ProdId + "";
            MaxInstallArray = sqlHandler.getResult(Query, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject MaxObject = null;
        for (int i = 0; i < MaxInstallArray.length(); i++) {
            try {
                MaxObject = MaxInstallArray.getJSONObject(i);
                MaxInstallments = MaxObject.getInt("MaxInstallments");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return MaxInstallments;
    }

    @JavascriptInterface
    public int getGracePeriods(String id) {
        int PolicyId = Integer.parseInt(id);
        int gracePeriod = 0;
        JSONArray GracePeriodArray = null;

        int ProdId = getProdId(PolicyId);
        try {
            String Query = "SELECT GracePeriod from tblProduct where ProdId = " + ProdId + "";
            GracePeriodArray = sqlHandler.getResult(Query, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject MaxObject = null;
        for (int i = 0; i < GracePeriodArray.length(); i++) {
            try {
                MaxObject = GracePeriodArray.getJSONObject(i);
                gracePeriod = MaxObject.getInt("GracePeriod");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return gracePeriod;
    }

    public int getProdId(int PolicyId) {
        int ProdId = 0;
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
    public int getPolicyStatus(int PolicyId) {
        int PolicyStatus = 0;
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
    public void SaveInsureeAttachments(int FamilyId) {
        int MaxAttachmentId = 0;
        try {

            MaxAttachmentId = getNextAvailableAttachmentId();

            for (int i = 0; i < Attachments.length(); i++) {
                ContentValues AttachmentValues = new ContentValues();
                JSONObject obj = Attachments.getJSONObject(i);
                if (!obj.has("Id")) {
                    AttachmentValues.put("Id", MaxAttachmentId);
                    AttachmentValues.put("Filename", obj.getString("Filename"));
                    AttachmentValues.put("Title", obj.getString("Title"));
                    AttachmentValues.put("Content", obj.getString("content"));
                    AttachmentValues.put("FamilyId", FamilyId);
                    sqlHandler.insertData("tblInsureeAttachments", AttachmentValues);
                }
            }

            TempAttachments = new JSONArray();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @JavascriptInterface
    public void SaveInsureePolicy(int InsureId, int FamilyId, Boolean Activate, int isOffline) {
        String InsQuery = "SELECT count(1) TotalIns FROM tblInsuree I WHERE FamilyId =" + FamilyId;
        JSONArray InsArray = sqlHandler.getResult(InsQuery, null);
        JSONObject InsObject = null;
        int TotalIns = 0;
        int MaxInsureePolicyId = 0;
        int PolicyId = 0;
        double PolicyValue = 0;
        double NewPolicyValue = 0;
        String EffectiveDate = null;
        String StartDate = null;
        String PolicyStage = null;
        String EnrollmentDate = null;
        String EnrollDate = null;
        String ExpiryDate = null;
        int IsOffline = 1;
        int MaxMember = 0;
        int ProdID = 0;
        boolean HasCycle = false;
        try {
            InsObject = InsArray.getJSONObject(0);
            TotalIns = Integer.parseInt(InsObject.getString("TotalIns"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String PolicyQuery = " SELECT PolicyId,PolicyValue,EffectiveDate,PolicyStage,ProdID,StartDate, EnrollDate,isOffline FROM tblPolicy WHERE FamilyID  =  " + FamilyId;
        JSONArray PolicyArray = sqlHandler.getResult(PolicyQuery, null);
        JSONObject PolicyObject = null;
        for (int i = 0; i < PolicyArray.length(); i++) {
            try {
                PolicyObject = PolicyArray.getJSONObject(i);
                PolicyId = Integer.parseInt(PolicyObject.getString("PolicyId"));
                PolicyValue = Double.parseDouble(PolicyObject.getString("PolicyValue"));
                EffectiveDate = (PolicyObject.getString("EffectiveDate"));
                PolicyStage = PolicyObject.getString("PolicyStage");
                StartDate = PolicyObject.getString("StartDate");
                ProdID = Integer.parseInt(PolicyObject.getString("ProdId"));
                EnrollmentDate = PolicyObject.getString("EnrollDate");
                IsOffline = Integer.parseInt(PolicyObject.getString("isOffline"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                e.printStackTrace();//sio error
            }
            if (MaxMember >= TotalIns) {
                try {
//                General general = new General();
//                if(general.isNetworkAvailable(mContext) && IsOffline == 0) {
//                    CallSoap cs = new CallSoap();
//                    cs.setFunctionName("getPolicyValue");
//                    NewPolicyValue =     cs.getPolicyValue(FamilyId, ProdID,0,PolicyStage,EnrollmentDate,PolicyId);
//
//                }
//                else
                    NewPolicyValue = getPolicyValue(EnrollmentDate, ProdID, FamilyId, StartDate, HasCycle, PolicyId, PolicyStage, isOffline);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

/*                   if (NewPolicyValue != PolicyValue) {

                   }*/
                if (!Activate) EffectiveDate = null;
 /*                String MaxIdQuery = "SELECT  IFNULL(COUNT(InsureePolicyId),0)+1  InsureePolicyId  FROM tblInsureePolicy";
               JSONArray JsonA = sqlHandler.getResult(MaxIdQuery, null);
                try {
                    //Integer.parseInt(PolicyObject.getString("ProdId"));
                    JSONObject JmaxOb = JsonA.getJSONObject(0);
                    MaxInsureePolicyId = JmaxOb.getString("InsureePolicyId ");
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                String MaxIdQuery = "SELECT  Count(InsureePolicyId)+1  InsureePolicyId  FROM tblInsureePolicy";
                JSONArray JsonA = sqlHandler.getResult(MaxIdQuery, null);
                try {
                    JSONObject JmaxOb = JsonA.getJSONObject(0);
                    MaxInsureePolicyId = JmaxOb.getInt("InsureePolicyId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                ContentValues values = new ContentValues();

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
    public void InsertPolicyInsuree(int PolicyId, int IsOffline) {
        int MaxMember = 0;
        int MaxInsureePolicyId = 0;
        String MaxIdQuery = "SELECT  Count(InsureePolicyId)+1  InsureePolicyId  FROM tblInsureePolicy";
        JSONArray JsonA = sqlHandler.getResult(MaxIdQuery, null);
        try {
            JSONObject JmaxOb = JsonA.getJSONObject(0);
            MaxInsureePolicyId = JmaxOb.getInt("InsureePolicyId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String MemberCount = "SELECT MemberCount FROM tblProduct Prod  \n" +
                " INNER JOIN tblPolicy P ON P.ProdId =Prod.ProdId \n" +
                " WHERE PolicyId =" + PolicyId + " LIMIT 1";
        JSONArray MCArray = sqlHandler.getResult(MemberCount, null);
        JSONObject MCObject = null;
        try {
            MCObject = MCArray.getJSONObject(0);
            MaxMember = Integer.parseInt(MCObject.getString("MemberCount"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String SavePolicyInsuree = "INSERT INTO tblInsureePolicy(InsureePolicyId,InsureeId,PolicyId,EnrollmentDate,StartDate,EffectiveDate,ExpiryDate,isOffline)\n" +
                "SELECT " + MaxInsureePolicyId + ",  InsureeId ,PolicyId ,EnrollDate,StartDate, EffectiveDate ,ExpiryDate,I.isOffline FROM tblPolicy P\n" +
                "INNER JOIN tblInsuree I ON  I.FamilyId = P.FamilyId\n" +
                "WHERE PolicyID = " + PolicyId + "\n" +
                "LIMIT " + MaxMember;
        sqlHandler.getResult(SavePolicyInsuree, null);
    }

    @JavascriptInterface
    public void UpdateInsureePolicy(int PolicyId) {//herman new
        ContentValues values = new ContentValues();
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
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
        String path = mContext.getApplicationInfo().dataDir + "/Images/";
        File Directory = new File(path);
        FilenameFilter filter = (dir, filename) -> filename.contains("0");
        File[] newFiles = Directory.listFiles(filter);
        return Directory.listFiles(filter);
    }


    @JavascriptInterface
    public void UploadPhotos() throws JSONException {
        pd = new ProgressDialog(mContext);
        pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Uploading));

        new Thread(() -> {
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            File[] Photo = getPhotos();

            if (Photo.length > 0) {
                UploadFile uf = new UploadFile();
                if (uf.isValidFTPCredentials()) {
                    for (int i = 0; i < Photo.length; i++) {
                        String FileName = Photo[i].toString().substring(Photo[i].toString().lastIndexOf("/") + 1);
                        String PhotoQuery = "SELECT PhotoPath FROM tblInsuree WHERE isOffline = 1 AND REPLACE(PhotoPath, RTRIM(PhotoPath, REPLACE(PhotoPath, '/', '')), '') = '" + FileName + "'";
                        JSONArray jsonArray = sqlHandler.getResult(PhotoQuery, null);
                        JSONObject jsonObject = null;
                        String PhotoPath = "";
                        if (jsonArray.length() > 0) {
                            try {
                                jsonObject = jsonArray.getJSONObject(0);
                                PhotoPath = jsonObject.getString("PhotoPath");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (PhotoPath.trim().length() == 0) {
                            if (uf.uploadFileToServer(mContext, Photo[i], "com.imispolicies.imis.enrollment")) {
                                RegisterUploadDetails(FileName);
                                Uploaded = 1;
                                Photo[i].delete();
                            }
                        }
                    }
                }

            } else {
                Uploaded = 0;
            }
            ((Activity) mContext).runOnUiThread(() -> {
                if (Uploaded == 1) {
                    ShowDialog(mContext.getResources().getString(R.string.PhotosUploaded));
                } else {
                    ShowDialog(mContext.getResources().getString(R.string.NoPhoto));
                }
            });
            pd.dismiss();
        }).start();
    }

    private SecretKeySpec generateKey(String encPassword) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = encPassword.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    public String encryptRarPwd(String dataToEncrypt, String encPassword) throws Exception {
        SecretKeySpec key = generateKey(encPassword);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(dataToEncrypt.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        return encryptedValue;
    }

    public String decryptRarPwd(String dataToDecrypt, String decPassword) throws Exception {
        SecretKeySpec key = generateKey(decPassword);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(dataToDecrypt, Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);

        return decryptedValue;
    }

    public String generateSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes(salt);
        String encodedSalt = Base64.encodeToString(salt, Base64.DEFAULT);

        return encodedSalt;
    }

    @JavascriptInterface
    public void SaveRarPassword(String password) {
        try {
            SharedPreferences sharedPreferences = mContext.getApplicationContext().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            salt = generateSalt();
            String trimSalt = salt.trim();
            String encryptedPassword = encryptRarPwd(password, trimSalt);
            String trimEncryptedPassword = encryptedPassword.trim();
            editor.putString("rarPwd", trimEncryptedPassword);
            editor.putString("salt", trimSalt);
            editor.apply();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @JavascriptInterface
    public void BackToDefaultRarPassword() {
        try {
            String defaultRarPassword = AppInformation.DomainInfo.getDefaultRarPassword();
            SharedPreferences sharedPreferences = mContext.getApplicationContext().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            salt = generateSalt();
            String trimSalt = salt.trim();
            String encryptedPassword = encryptRarPwd(defaultRarPassword, trimSalt);
            String trimEncryptedPassword = encryptedPassword.trim();
            editor.putString("rarPwd", trimEncryptedPassword);
            editor.putString("salt", trimSalt);
            editor.apply();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @JavascriptInterface
    public int ModifyFamily(final String InsuranceNumber) {
        IsFamilyAvailable = 0;
        inProgress = true;

        int insureeCount = sqlHandler.getCount("tblInsuree", "Trim(CHFID) = ?", new String[]{InsuranceNumber});
        if (insureeCount > 0) {
            ShowDialog(mContext.getResources().getString(R.string.FamilyExists));
        } else {
            try {
                ToRestApi rest = new ToRestApi();
                HttpResponse response = rest.getFromRestApiToken("family/" + InsuranceNumber.trim());
                String error = rest.getHttpError(mContext, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                String content = rest.getContent(response);

                if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    ShowDialog(mContext.getResources().getString(R.string.InsuranceNumberNotFound));
                } else if (!StringUtils.isEmpty(error)) {
                    ShowDialog(error);
                } else if (StringUtils.isEmpty(content)) {
                    ShowDialog(mContext.getResources().getString(R.string.SomethingWrongServer));
                } else {
                    JSONObject FamilyData = new JSONObject(content);
                    if (FamilyData.length() != 0) {
                        parseFamilyData(FamilyData);
                        IsFamilyAvailable = 1;
                    } else {
                        ShowDialog(mContext.getResources().getString(R.string.InsuranceNumberNotFound));
                    }
                }
            } catch (JSONException | UserException | IOException e) {
                Log.e("MODIFYFAMILY", "Error while downloading a family", e);
            }
        }

        return IsFamilyAvailable;
    }


    private void parseFamilyData(JSONObject familyData) throws JSONException, UserException, IOException {
        JSONArray newFamilyArr = new JSONArray();
        JSONArray newInsureeArr = new JSONArray();

        familyData.put("familyId", "-" + familyData.getString("familyId"));
        familyData.put("insureeId", "-" + familyData.getString("insureeId"));

        // Copy oryginal object
        JSONObject cloneFamilyData = new JSONObject(familyData.toString());
        JSONArray Insuree = (JSONArray) cloneFamilyData.get("insurees");

        String familyUUID = familyData.getString("familyUUID");
        String familyId = familyData.getString("familyId");

        // Add familyUUID to Insuree
        for (int i = 0; i < Insuree.length(); i++) {
            JSONObject obj = (JSONObject) Insuree.get(i);
            obj.put("familyId", familyId);
            obj.put("familyUUID", familyUUID);
            obj.put("insureeId", "-" + obj.getString("insureeId"));
        }

        // Remove insurees from FamilyData
        JSONArray RemovedInsurees = (JSONArray) cloneFamilyData.remove("insurees");

        // Family array without insurees
        JSONArray familyArray = new JSONArray();
        familyArray.put(cloneFamilyData);


        for (int j = 0; j < familyArray.length(); j++) {
            JSONObject ob = familyArray.getJSONObject(j);
            String poverty = ob.getString("poverty");
            String isOffline = ob.getString("isOffline");

            ob.put("poverty", "true".equals(poverty) || "1".equals(poverty) ? 1 : 0);
            ob.put("isOffline", "true".equals(isOffline) || "1".equals(isOffline) ? 1 : 0);

            newFamilyArr.put(ob);
        }

        for (int j = 0; j < Insuree.length(); j++) {
            JSONObject ob = Insuree.getJSONObject(j);
            String isOffline = ob.getString("isOffline");
            String isHead = ob.getString("isHead");

            ob.put("isOffline", "true".equals(isOffline) || "1".equals(isOffline) ? 1 : 0);
            ob.put("isHead", "true".equals(isHead) || "1".equals(isHead) ? 1 : 0);


            newInsureeArr.put(ob);
        }

        //Iterate insuree array and download image for each Insuree
        for (int i = 0; i < newInsureeArr.length(); i++) {
            JSONObject insureeObj = newInsureeArr.getJSONObject(i);

            if (!JsonUtils.isStringEmpty(insureeObj, "photoPath", true)) {
                String[] photoPathSegments = insureeObj.getString("photoPath").split("[\\\\/]");
                String photoName = photoPathSegments[photoPathSegments.length - 1];
                if (!StringUtils.isEmpty(photoName)) {
                    String imagePath = global.getImageFolder() + photoName;
                    insureeObj.put("photoPath", imagePath);
                    OutputStream imageOutputStream = new FileOutputStream(imagePath);
                    if (!JsonUtils.isStringEmpty(insureeObj, "photoBase64", true)) {
                        try {
                            byte[] imageBytes = Base64.decode(insureeObj.getString("photoBase64").getBytes(), Base64.DEFAULT);
                            Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            image.compress(Bitmap.CompressFormat.JPEG, 100, imageOutputStream);
                        } catch (Exception e) {
                            Log.e("MODIFYFAMILY", "Error while processing Base64 image", e);
                        }
                    } else {
                        if (photoName.length() > 0) {
                            String photoUrl = String.format("%sImages/Updated/%s", AppInformation.DomainInfo.getDomain(), photoName);
                            imageTarget = new OutputStreamImageTarget(imageOutputStream, 100);
                            ((Activity) mContext).runOnUiThread(() -> picassoInstance.load(photoUrl).into(imageTarget));
                        }
                    }
                }
            }
        }

        InsertFamilyDataFromOnline(newFamilyArr);
        InsertInsureeDataFromOnline(newInsureeArr);
    }

    private boolean InsertFamilyDataFromOnline(JSONArray jsonArray) throws JSONException {
        JSONObject object = jsonArray.getJSONObject(0);
        UUID FamilyUUID = UUID.fromString(object.getString("familyUUID"));

        String QueryCheck = "SELECT FamilyUUID FROM tblFamilies WHERE FamilyUUID = '" + FamilyUUID + "' AND (isOffline = 0 OR isOffline = 2)";
        JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
        if (CheckedArrey.length() == 0) {
            String Columns[] = {"familyId", "familyUUID", "insureeId", "insureeUUID", "locationId", "poverty", "isOffline", "familyType",
                    "familyAddress", "ethnicity", "confirmationNo", "confirmationType"};
            sqlHandler.insertData("tblFamilies", Columns, jsonArray.toString(), "");

            if (!JsonUtils.isStringEmpty(object, "familySMS", true)) {
                JSONObject smsData = object.getJSONObject("familySMS");
                try {
                    addOrUpdateFamilySms(object.getInt("familyId"),
                            smsData.getBoolean("approvalOfSMS"),
                            smsData.getString("languageOfSMS")
                    );
                } catch (UserException e) {
                    e.printStackTrace();
                    Log.w("ModifyFamily", "No familySMS data in family payload");
                }
            }
        }
        return true;
    }


    private boolean InsertInsureeDataFromOnline(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray TempJsonArray = new JSONArray();
            JSONObject object = jsonArray.getJSONObject(i);
            String CHFID = object.getString("chfid");
            String QueryCheck = "SELECT InsureeUUID FROM tblInsuree WHERE Trim(CHFID) = '" + CHFID + "' AND (isOffline = 0 OR isOffline = 2)";
            JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
            if (CheckedArrey.length() == 0) {
                TempJsonArray.put(jsonArray.getJSONObject(i));
                String Columns[] = {"identificationNumber", "familyId", "insureeId", "insureeUUID", "familyUUID", "chfid", "lastName", "otherNames", "dob", "gender", "marital", "isHead", "phone", "photoPath", "cardIssued",
                        "isOffline", "relationship", "profession", "education", "email", "typeOfId", "hfid", "currentAddress", "geoLocation", "curVillage"};
                sqlHandler.insertData("tblInsuree", Columns, TempJsonArray.toString(), "");
            }
        }
        return true;
    }

    private boolean InsertPolicyDataFromOnline(JSONArray jsonArray) throws JSONException {

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray TempJsonArray = new JSONArray();
            JSONObject object = jsonArray.getJSONObject(i);
            int PolicyId = object.getInt("PolicyId");
            String QueryCheck = "SELECT PolicyId FROM tblPolicy WHERE PolicyId = " + PolicyId + " AND (isOffline = 0 OR isOffline = 2)";
            JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
            if (CheckedArrey.length() == 0) {
                TempJsonArray.put(jsonArray.getJSONObject(i));
                String Columns[] = {"PolicyId", "FamilyId", "EnrollDate", "StartDate", "EffectiveDate", "ExpiryDate", "PolicyStatus", "PolicyValue",
                        "ProdId", "OfficerId", "isOffline", "PolicyStage"};
                sqlHandler.insertData("tblPolicy", Columns, TempJsonArray.toString(), "");

            }
        }

        return true;
    }

    private boolean InsertPolicyInsureeDataFromOnline(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray TempJsonArray = new JSONArray();
            JSONObject object = jsonArray.getJSONObject(i);
            int InsureePolicyId = object.getInt("InsureePolicyId");
            String QueryCheck = "SELECT PolicyId FROM tblInsureePolicy WHERE InsureePolicyId = " + InsureePolicyId + " AND (isOffline = 0 OR isOffline = 2)";
            JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
            if (CheckedArrey.length() == 0) {
                TempJsonArray.put(jsonArray.getJSONObject(i));
                String Columns[] = {"InsureePolicyId", "InsureeId", "PolicyId", "EnrollmentDate", "StartDate", "EffectiveDate", "ExpiryDate", "isOffline"};
                sqlHandler.insertData("tblInsureePolicy", Columns, TempJsonArray.toString(), "");

            }
        }
        return true;
    }

    private boolean InsertPremiumDataFromOnline(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray TempJsonArray = new JSONArray();
            JSONObject object = jsonArray.getJSONObject(i);
            int PremiumId = object.getInt("PremiumId");
            String QueryCheck = "SELECT PolicyId FROM tblPremium WHERE PremiumId = " + PremiumId + " AND (isOffline = 0 OR isOffline = 2)";
            JSONArray CheckedArrey = sqlHandler.getResult(QueryCheck, null);
            if (CheckedArrey.length() == 0) {
                TempJsonArray.put(jsonArray.getJSONObject(i));
                String Columns[] = {"PremiumId", "PolicyId", "PayerId", "Amount", "Receipt", "PayDate", "PayType", "isOffline", "isPhotoFee"};
                sqlHandler.insertData("tblPremium", Columns, TempJsonArray.toString(), "");
            }
        }

        return true;
    }

    //****************************Online Statistics ******************************//
    @JavascriptInterface
    public int getTotalFamilyOnline() {

        String FamilyQuery = "SELECT count(1) Families FROM tblfamilies F INNER JOIN tblInsuree I ON F.FamilyId = I.FamilyId WHERE F.isOffline = 0 AND (F.FamilyId < 0 OR I.InsureeId < 0) Group By F.FamilyId";
        JSONArray Families = sqlHandler.getResult(FamilyQuery, null);
        JSONObject object = null;
        int TotalFamilies = 0;
        try {
            if (Families.length() == 0) {
                TotalFamilies = 0;
            } else {
                object = Families.getJSONObject(0);
                TotalFamilies = Integer.parseInt(object != null ? object.getString("Families") : null);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalFamilies;
    }

    @JavascriptInterface
    public int getTotalInsureeOnline() {
        String InsureeQuery = "SELECT count(1) Insuree  FROM tblInsuree WHERE isOffline = 0 AND InsureeId < 0";
        JSONArray Insuree = sqlHandler.getResult(InsureeQuery, null);
        JSONObject object = null;
        int TotalInsuree = 0;
        try {
            object = Insuree.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalInsuree = Integer.parseInt(object != null ? object.getString("Insuree") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalInsuree;
    }

    @JavascriptInterface
    public int getTotalPolicyOnline() {
        String PolicyQuery = "SELECT count(1) Policies  FROM  tblPolicy WHERE isOffline = 0 ";
        JSONArray Policy = sqlHandler.getResult(PolicyQuery, null);
        JSONObject object = null;
        int TotalPolicies = 0;
        try {
            object = Policy.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPolicies = Integer.parseInt(object != null ? object.getString("Policies") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPolicies;
    }

    @JavascriptInterface
    public int getTotalPremiumOnline() {
        String PremiumQuery = "SELECT count(1) Premiums  FROM  tblPremium  WHERE isOffline = 0 ";
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        JSONObject object = null;
        int TotalPremiums = 0;
        try {
            object = Premium.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPremiums = Integer.parseInt(object != null ? object.getString("Premiums") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

    @JavascriptInterface
    public int getCountPremiums(String id) {
        int PolicyId = Integer.parseInt(id);
        String PremiumQuery = "SELECT count(1) Premiums  FROM  tblPremium  WHERE isPhotoFee = 'false' AND PolicyId = " + PolicyId + "";
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        JSONObject object = null;
        int TotalPremiums = 0;
        try {
            object = Premium.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            TotalPremiums = Integer.parseInt(object != null ? object.getString("Premiums") : null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return TotalPremiums;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @JavascriptInterface
    public String getSumPremium() {
        String PremiumQuery = "SELECT SUM(Amount) FROM tblPremium WHERE isOffline = 1";
        JSONArray Premium = sqlHandler.getResult(PremiumQuery, null);
        JSONObject object = null;
        String TotalPremiums = "0";
        try {
            if (Premium.length() == 0) {
                TotalPremiums = "0.00";
            } else {
                object = Premium.getJSONObject(0);
                if (object != null) {
                    String amt = object.getString("SUM(Amount)");
                    if (amt != "") {
                        int Amount = Integer.parseInt(amt);
                        String number = String.valueOf(Amount);
                        double amount = Double.parseDouble(number);
                        DecimalFormat formatter = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            formatter = new DecimalFormat("#,###.00");
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
        global = (Global) mContext.getApplicationContext();
        String Query = "SELECT LocationId FROM tblOfficer WHERE Code = '" + global.getOfficerCode() + "'";
        JSONArray jsonArray = sqlHandler.getResult(Query, null);
        JSONObject object = jsonArray.getJSONObject(0);
        int LocationId = object.getInt("LocationId");
        return LocationId;
    }

    public int getLocationId(String OfficerCode) {
        String Query = "SELECT LocationId FROM tblOfficer WHERE LOWER(Code)=? ";
        String arg[] = {OfficerCode.toLowerCase()};
        JSONArray Renews = sqlHandler.getResult(Query, arg);
        int locationId = 0;
        JSONObject O = null;
        if (Renews.length() > 0) {
            try {
                O = Renews.getJSONObject(0);
                locationId = Integer.parseInt(O.getString("LocationId"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return locationId;
    }

    @JavascriptInterface
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
    public int DeleteOnlineData(final int Id, final String DeleteInfo) {
        try {
            DataDeleted = 0;
            //global = (Global) mContext.getApplicationContext();
            //final int userId = global.getUserId();
            //if (userId > 0) {
            inProgress = true;
            //pd = new ProgressDialog(mContext);
            //pd = ProgressDialog.show(mContext, "", mContext.getResources().getString(R.string.Deleting));
            Toast.makeText(mContext, "Please wait...", Toast.LENGTH_LONG).show();

            //Uncoment this if you want to delete online data too.
            //CallSoap cs = new CallSoap();
            //cs.setFunctionName("DeleteFromPhone");
            DataDeleted = 1;//cs.DeleteFromPhone(Id, userId, DeleteInfo);

            inProgress = false;

            if (DataDeleted == 1) {
                if (DeleteInfo.equalsIgnoreCase("F")) DeleteFamily(Id);//Enrollment page
                if (DeleteInfo.equalsIgnoreCase("I")) DeleteInsuree(Id);//family and insuree page
//                    if (DeleteInfo.equalsIgnoreCase("PO")) DeletePolicy(UUID);//Family and policy page
//                    if (DeleteInfo.equalsIgnoreCase("PR")) DeletePremium(UUID);//PolicyPremium page

                Toast.makeText(mContext, mContext.getResources().getString(R.string.dataDeleted), Toast.LENGTH_LONG).show();

                inProgress = false;
            }

            // pd.dismiss();

/*                new Thread() {
                    public void run() {
                        CallSoap cs = new CallSoap();
                        cs.setFunctionName("DeleteFromPhone");
                        DataDeleted = cs.DeleteFromPhone(Id, userId, DeleteInfo);
                        inProgress = false;
                        if (DataDeleted == 1) {
                            if (DeleteInfo.equalsIgnoreCase("F")) DeleteFamily(Id);
                            if (DeleteInfo.equalsIgnoreCase("I")) DeleteInsuree(Id);
                            if (DeleteInfo.equalsIgnoreCase("PO")) DeletePolicy(Id);
                            if (DeleteInfo.equalsIgnoreCase("PR")) DeletePremium(Id);
                        }
                        //pd.dismiss();

                    }
                }.start();*/
/*            } else {
                DataDeleted = -1;
                inProgress = false;
            }*/

            while (inProgress) {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return DataDeleted;
    }

    @JavascriptInterface
    public int getOfficerId() {
        global = (Global) mContext.getApplicationContext();
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
    public boolean getRule(String rulename) {
        return getRule(rulename, false);
    }

    @JavascriptInterface
    public boolean getRule(String rulename, boolean defaultValue) {
        boolean rule = false;
        String Query = "SELECT RuleValue FROM tblIMISDefaultsPhone WHERE RuleName=?";
        String arg[] = {rulename};
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
    public boolean CheckInternetAvailable() {
        // check internet connection
        if (!global.isNetworkAvailable()) {
            ShowDialog(mContext.getResources().getString(R.string.NoInternet));
            return false;
        }
        return true;
    }

    public void LoginDialogBox(final String page) {

        ((Activity) mContext).runOnUiThread(() -> {
            // check internet connection
            if (!CheckInternetAvailable())
                return;
            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(mContext);
            View promptsView = li.inflate(R.layout.login_dialog, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final TextView username = promptsView.findViewById(R.id.UserName);
            final TextView password = promptsView.findViewById(R.id.Password);

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            (dialog, id) -> {
                                if (!username.getText().toString().equals("") || !password.getText().toString().equals("")) {
                                    boolean isUserLogged = false;
                                    isUserLogged = LoginToken(username.getText().toString(), password.getText().toString());

                                    if (isUserLogged) {
                                        if (page.equals("Enquire")) {
                                            ((Enquire) mContext).finish();
                                            Intent intent = new Intent(mContext, Enquire.class);
                                            mContext.startActivity(intent);
                                        }
                                        if (page.equals("Renewals")) {
                                            ((RenewList) mContext).finish();
                                            Intent intent = new Intent(mContext, RenewList.class);
                                            mContext.startActivity(intent);
                                        }
                                        if (page.equals("Feedbacks")) {
                                            ((FeedbackList) mContext).finish();
                                            Intent intent = new Intent(mContext, FeedbackList.class);
                                            mContext.startActivity(intent);
                                        }
                                        if (page.equals("Reports")) {
                                            ((FeedbackList) mContext).finish();
                                            Intent intent = new Intent(mContext, Reports.class);
                                            mContext.startActivity(intent);
                                        }

                                    } else {
                                        ShowDialog(mContext.getResources().getString(R.string.LoginFail));
                                    }
                                } else {
                                    Toast.makeText(mContext, "Please enter user name and password", Toast.LENGTH_LONG).show();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        });

    }

    @JavascriptInterface
    public String getOfficerCode() {
        global = (Global) mContext.getApplicationContext();
        return global.getOfficerCode();
    }

    @JavascriptInterface
    public int getUserId() {
        global = (Global) mContext.getApplicationContext();
        return global.getUserId();
    }

    @JavascriptInterface
    public void launchPayment() {
        Intent intent = new Intent(mContext, PaymentOverview.class);
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void launchActivity(String activity) {
        if (activity.equals("Enquire")) {
            Intent intent = new Intent(mContext, Enquire.class);
            mContext.startActivity(intent);
        }
        if (activity.equals("Renewals")) {
            Intent intent = new Intent(mContext, RenewList.class);
            mContext.startActivity(intent);
        }
        if (activity.equals("Feedbacks")) {
            Intent intent = new Intent(mContext, FeedbackList.class);
            mContext.startActivity(intent);
        }
        if (activity.equals("Reports")) {
            Intent intent = new Intent(mContext, Reports.class);
            mContext.startActivity(intent);
        }
    }

    @JavascriptInterface
    public String getSelectedLanguage() {
        return ((MainActivity) mContext).getSelectedLanguage();
    }

    public boolean isProductsListUnique(JSONObject policies) {
        ArrayList<String> productCodes = new ArrayList();
        try {
            JSONArray policiesArray = policies.getJSONArray("policies");
            for (int i = 0; i < policiesArray.length(); i++) {
                JSONObject policyObject = policiesArray.getJSONObject(i);
                String productCode = policyObject.getString("insurance_product_code");
                productCodes.add(productCode);
            }
            Set<String> nonRepeatedCodes = new HashSet(productCodes);
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

        String query = "SELECT * FROM tblRecordedPolicies WHERE InsuranceNumber LIKE '%" + insuranceNumber +
                "%' AND ProductCode LIKE '%" + insuranceProduct + "%' " + aRenewal + " AND UploadedDate == ''";
        JSONArray notEnrolledPolicies = sqlHandler.getResult(query, null);
        return notEnrolledPolicies;
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

    private int getNextAvailableAttachmentId() {
        return getMaxIdFromTable("Id", "tblInsureeAttachments");
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
    public boolean isLoggingEnabled() {
        return Log.isLoggingEnabled;
    }

    @JavascriptInterface
    public void clearLogs() {
        AndroidUtils.showConfirmDialog(
                mContext,
                R.string.ConfirmClearLogs,
                (d, i) -> new Thread(Log::deleteLogFiles).start()
        );
    }

    @JavascriptInterface
    public void exportLogs() {
        AndroidUtils.showConfirmDialog(
                mContext,
                R.string.ConfirmExportLogs,
                (d, i) -> new Thread(() -> Log.zipLogFiles(mContext)).start()
        );
    }
}
