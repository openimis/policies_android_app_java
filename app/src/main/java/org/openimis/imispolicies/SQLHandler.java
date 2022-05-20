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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import org.openimis.imispolicies.tools.Log;
import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SQLHandler extends SQLiteOpenHelper {

    public static final String DBNAME = "IMIS.db3";
    private static final String OFFLINEDBNAME = "ImisData.db3";
    public Boolean isPrivate = true;
    private Context mContext;
    private Global global;
    private SQLiteDatabase mDatabase;
    private static final int DATABASE_VERSION = 2;
    int count = 1;


    //table names
    private static final String android_metadata = "android_metadata";
    private static final String sqlite_sequence = "sqlite_sequence";
    public static final String tblConfirmationTypes = "tblConfirmationTypes";
    public static final String tblControlNumber = "tblControlNumber";
    public static final String tblControls = "tblControls";
    public static final String tblEducations = "tblEducations";
    public static final String tblFamilies = "tblFamilies";
    public static final String tblFamilyTypes = "tblFamilyTypes";
    public static final String tblFeedbacks = "tblFeedbacks";
    public static final String tblGender = "tblGender";
    public static final String tblHF = "tblHF";
    public static final String tblIMISDefaultsPhone = "tblIMISDefaultsPhone";
    public static final String tblIdentificationTypes = "tblIdentificationTypes";
    public static final String tblInsuree = "tblInsuree";
    public static final String tblInsureePolicy = "tblInsureePolicy";
    public static final String tblLanguages = "tblLanguages";
    public static final String tblLocations = "tblLocations";
    public static final String tblOfficer = "tblOfficer";
    public static final String tblOfficerVillages = "tblOfficerVillages";
    public static final String tblPayer = "tblPayer";
    public static final String tblPolicy = "tblPolicy";
    public static final String tblPremium = "tblPremium";
    public static final String tblProduct = "tblProduct";
    public static final String tblProfessions = "tblProfessions";
    public static final String tblRecordedPolicies = "tblRecordedPolicies";
    public static final String tblRelations = "tblRelations";
    public static final String tblRenewals = "tblRenewals";
    public static final String tblBulkControlNumbers = "tblBulkControlNumbers";
    public static final String tblFamilySMS = "tblFamilySMS";

    public SQLHandler(Context context) {
        super(context, DBNAME, null, DATABASE_VERSION);
        this.mContext = context;
        global = (Global) mContext.getApplicationContext();
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblConfirmationTypes + "("
                            + "ConfirmationTypeCode TEXT,"
                            + "ConfirmationType TEXT NOT NULL,"
                            + "SortOrder NUMERIC NOT NULL,"
                            + "AltLanguage TEXT " + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblControlNumber + "("
                            + "Id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "AmountCalculated INTEGER,"
                            + "AmountConfirmed INTEGER,"
                            + "ControlNumber TEXT,"
                            + "InternalIdentifier TEXT,"
                            + "PaymentType TEXT,"
                            + "SmsRequired TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblControls + "("
                            + "FieldName TEXT,"
                            + "Adjustibility TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblEducations + "("
                            + "EducationId NUMERIC,"
                            + "Education TEXT,"
                            + "SortOrder NUMERIC,"
                            + "AltLanguage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblFamilies + "(" +
                            "FamilyId INTEGER," +
                            "InsureeId NUMERIC," +
                            "LocationId NUMERIC," +
                            "Poverty BOOLEAN," +
                            "isOffline NUMERIC," +
                            "FamilyType TEXT," +
                            "FamilyAddress TEXT," +
                            "Ethnicity TEXT," +
                            "ConfirmationNo TEXT," +
                            "ConfirmationType TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblFamilyTypes + "(" +
                            "FamilyTypeCode TEXT," +
                            "FamilyType TEXT," +
                            "SortOrder NUMERIC," +
                            "AltLanguage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblFeedbacks + "(" +
                            "ClaimId INTEGER," +
                            "ClaimUUID TEXT," +
                            "OfficerId INTEGER," +
                            "OfficerCode TEXT," +
                            "CHFID TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "HFCode TEXT," +
                            "HFName TEXT," +
                            "ClaimCode TEXT," +
                            "DateFrom TEXT," +
                            "DateTo TEXT," +
                            "IMEI TEXT," +
                            "FeedbackPromptDate TEXT," +
                            "Phone TEXT," +
                            "isDone TEXT DEFAULT 'N'" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblGender' (" +
                            "Code TEXT," +
                            "Gender TEXT," +
                            "AltLanguage TEXT," +
                            "SortOrder NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblHF' (" +
                            "HFID NUMERIC," +
                            "HFCode TEXT," +
                            "HFName TEXT," +
                            "LocationId NUMERIC," +
                            "HFLevel TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblIMISDefaultsPhone' (" +
                            "RuleName TEXT," +
                            "RuleValue BIT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblIdentificationTypes' (" +
                            "IdentificationCode TEXT," +
                            "IdentificationTypes TEXT," +
                            "AltLanguage TEXT," +
                            "SortOrder NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblInsuree' (" +
                            "InsureeId INTEGER," +
                            "FamilyId NUMERIC," +
                            "CHFID TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "DOB TEXT," +
                            "Gender INTEGER," +
                            "Marital TEXT," +
                            "isHead NUMERIC," +
                            "IdentificationNumber TEXT," +
                            "Phone TEXT," +
                            "PhotoPath TEXT," +
                            "CardIssued BOOLEAN," +
                            "isOffline BOOLEAN," +
                            "Relationship NUMERIC," +
                            "Profession NUMERIC," +
                            "Education NUMERIC," +
                            "Email TEXT," +
                            "TypeOfId TEXT," +
                            "HFID NUMERIC," +
                            "CurrentAddress TEXT," +
                            "GeoLocation TEXT," +
                            "CurVillage NUMERIC," +
                            "Vulnerability BOOLEAN" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblInsureePolicy' (" +
                            "InsureePolicyId INTEGER," +
                            "InsureeId INTEGER," +
                            "PolicyId NUMERIC," +
                            "EnrollmentDate DATE," +
                            "StartDate DATE," +
                            "EffectiveDate DATE," +
                            "ExpiryDate DATE," +
                            "isOffline NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblLanguages' (" +
                            "LanguageCode TEXT," +
                            "LanguageName TEXT," +
                            "SortOrder NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblLocations + " (" +
                            "LocationId NUMERIC," +
                            "LocationCode TEXT," +
                            "LocationName TEXT," +
                            "ParentLocationId NUMERIC," +
                            "LocationType TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblOfficer' (" +
                            "OfficerId NUMERIC," +
                            "Code TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "Phone TEXT," +
                            "LocationId NUMERIC," +
                            "OfficerIdSubst NUMERIC," +
                            "WorksTo DATE" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblOfficerVillages' (" +
                            "code TEXT," +
                            "Ward TEXT," +
                            "Village TEXT," +
                            "LocationId INTEGER," +
                            "WardID INTEGER" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblPayer' (" +
                            "PayerId NUMERIC," +
                            "PayerName TEXT," +
                            "LocationId NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblPolicy' (" +
                            "PolicyId INTEGER," +
                            "FamilyId NUMERIC," +
                            "EnrollDate DATE," +
                            "StartDate DATE," +
                            "EffectiveDate DATE," +
                            "ExpiryDate DATE," +
                            "PolicyStatus NUMERIC," +
                            "PolicyValue NUMERIC," +
                            "ProdId NUMERIC," +
                            "OfficerId NUMERIC," +
                            "isOffline NUMERIC," +
                            "PolicyStage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblPremium' (" +
                            "PremiumId INTEGER," +
                            "PolicyId NUMERIC," +
                            "PayerId NUMERIC," +
                            "Amount NUMERIC," +
                            "Receipt TEXT," +
                            "PayDate DATE," +
                            "PayType TEXT," +
                            "isOffline NUMERIC," +
                            "isPhotoFee BOOLEAN" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblProduct' (" +
                            "ProdId NUMERIC," +
                            "ProductCode TEXT," +
                            "ProductName TEXT," +
                            "LocationId NUMERIC," +
                            "InsurancePeriod NUMERIC," +
                            "DateFrom DATE," +
                            "DateTo DATE," +
                            "ConversionProdId NUMERIC," +
                            "Lumpsum NUMERIC," +
                            "MemberCount NUMERIC," +
                            "PremiumAdult NUMERIC," +
                            "PremiumChild NUMERIC," +
                            "RegistrationLumpsum NUMERIC," +
                            "RegistrationFee NUMERIC," +
                            "GeneralAssemblyLumpsum NUMERIC," +
                            "GeneralAssemblyFee NUMERIC," +
                            "StartCycle1 TEXT," +
                            "StartCycle2 TEXT," +
                            "StartCycle3 TEXT," +
                            "StartCycle4 TEXT," +
                            "GracePeriodRenewal NUMERIC," +
                            "MaxInstallments NUMERIC," +
                            "WaitingPeriod NUMERIC," +
                            "Threshold NUMERIC," +
                            "RenewalDiscountPerc NUMERIC," +
                            "RenewalDiscountPeriod NUMERIC," +
                            "AdministrationPeriod NUMERIC," +
                            "EnrolmentDiscountPerc NUMERIC," +
                            "EnrolmentDiscountPeriod NUMERIC," +
                            "GracePeriod INT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblProfessions' (" +
                            "ProfessionId NUMERIC," +
                            "Profession TEXT," +
                            "SortOrder NUMERIC," +
                            "AltLanguage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblRecordedPolicies' (" +
                            "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "PolicyId INTEGER," +
                            "InsuranceNumber TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "ProductCode BLOB," +
                            "ProductName TEXT," +
                            "isDone TEXT DEFAULT 'N'," +
                            "PolicyValue NUMERIC," +
                            "UploadedDate TEXT," +
                            "ControlRequestDate TEXT," +
                            "Code INTEGER DEFAULT 'N'" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblRelations' (" +
                            "RelationId NUMERIC," +
                            "Relation TEXT," +
                            "SortOrder NUMERIC," +
                            "AltLanguage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblRenewals' (" +
                            "RenewalId NUMERIC," +
                            "PolicyId INTEGER," +
                            "OfficerId INTEGER," +
                            "OfficerCode TEXT," +
                            "CHFID TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "ProductCode TEXT," +
                            "ProductName TEXT," +
                            "VillageName TEXT," +
                            "RenewalPromptDate TEXT," +
                            "IMEI TEXT," +
                            "Phone TEXT," +
                            "PaymentMethod TEXT," +
                            "isDone TEXT DEFAULT 'N'," +
                            "LocationId INTEGER," +
                            "PolicyValue NUMERIC," +
                            "EnrollDate TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE VIEW uvwLocations As SELECT 'null' LocationId," +
                            " 'null' RegionId ," +
                            " 'null' RegionCode," +
                            " 'National' RegionName," +
                            " 'null' DistrictId," +
                            " 'null' DistrictName," +
                            " 'null' DistrictCode," +
                            " 'null' LocationTyPe UNION ALL SELECT LocationId," +
                            " LocationId RegionId ," +
                            " LocationCode RegionCode," +
                            " LocationName RegionName," +
                            " 'null' DistrictId," +
                            " 'null' DistrictName," +
                            " 'null' DistrictCode," +
                            "LocationTyPe FROM tbllocations" +
                            " where LocationTyPe ='R' " +
                            " UNION ALL SELECT LocationId," +
                            " ParentLocationId RegionId ," +
                            " LocationCode RegionCode," +
                            "LocationName RegionName," +
                            " LocationId DistrictId," +
                            " LocationName DistrictName," +
                            "LocationCode DistrictCode," +
                            "LocationTyPe" +
                            " FROM tbllocations " +
                            "where LocationTyPe ='D'");
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblBulkControlNumbers + "(" +
                            "Id INTEGER," +
                            "BillId INTEGER," +
                            "ProductCode TEXT," +
                            "OfficerCode TEXT," +
                            "ControlNumber TEXT," +
                            "Amount REAL," +
                            "PolicyId INTEGER" + ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onDowngrade(db, oldVersion, newVersion);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + android_metadata);
        db.execSQL("DROP TABLE IF EXISTS " + sqlite_sequence);
        db.execSQL("DROP TABLE IF EXISTS " + tblConfirmationTypes);
        db.execSQL("DROP TABLE IF EXISTS " + tblControlNumber);
        db.execSQL("DROP TABLE IF EXISTS " + tblControls);
        db.execSQL("DROP TABLE IF EXISTS " + tblEducations);
        db.execSQL("DROP TABLE IF EXISTS " + tblFamilies);
        db.execSQL("DROP TABLE IF EXISTS " + tblFamilyTypes);
        db.execSQL("DROP TABLE IF EXISTS " + tblFeedbacks);
        db.execSQL("DROP TABLE IF EXISTS " + tblGender);
        db.execSQL("DROP TABLE IF EXISTS " + tblHF);
        db.execSQL("DROP TABLE IF EXISTS " + tblIMISDefaultsPhone);
        db.execSQL("DROP TABLE IF EXISTS " + tblIdentificationTypes);
        db.execSQL("DROP TABLE IF EXISTS " + tblInsuree);
        db.execSQL("DROP TABLE IF EXISTS " + tblInsureePolicy);
        db.execSQL("DROP TABLE IF EXISTS " + tblLanguages);
        db.execSQL("DROP TABLE IF EXISTS " + tblLocations);
        db.execSQL("DROP TABLE IF EXISTS " + tblOfficer);
        db.execSQL("DROP TABLE IF EXISTS " + tblOfficerVillages);
        db.execSQL("DROP TABLE IF EXISTS " + tblPayer);
        db.execSQL("DROP TABLE IF EXISTS " + tblPolicy);
        db.execSQL("DROP TABLE IF EXISTS " + tblPremium);
        db.execSQL("DROP TABLE IF EXISTS " + tblProduct);
        db.execSQL("DROP TABLE IF EXISTS " + tblProfessions);
        db.execSQL("DROP TABLE IF EXISTS " + tblRecordedPolicies);
        db.execSQL("DROP TABLE IF EXISTS " + tblRelations);
        db.execSQL("DROP TABLE IF EXISTS " + tblRenewals);
        if (oldVersion < 2) {
            String sql = "ALTER TABLE tblRenewals ADD COLUMN LocationId INTEGER;";
            db.execSQL(sql);
            Log.d("Upgrade", "DB Version upgraded from 1 to 2");
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    private void openDatabase() {
        String dbPath = mContext.getDatabasePath(DBNAME).getPath();
        String dbOfflinePath = global.getAppDirectory() + File.separator + OFFLINEDBNAME;
        if (mDatabase != null && mDatabase.isOpen()) {
            return;
        }
        if (isPrivate)
            mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        else
            mDatabase = SQLiteDatabase.openDatabase(dbOfflinePath, null, SQLiteDatabase.OPEN_READWRITE);

    }

    public void closeDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public JSONArray getResult(String tableName, String[] columns, String Where, String OrderBy, String nullOverride) {
        openDatabase();
        JSONArray resultSet = new JSONArray();
        Cursor cursor = mDatabase.query(tableName, columns, Where, null, null, null, OrderBy);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumns = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumns; i++) {
                try {
                    if (cursor.getString(i) != null)
                        rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                    else
                        rowObject.put(cursor.getColumnName(i), nullOverride);
                } catch (Exception e) {
                    Log.d("Tag Name ", e.getMessage());
                }
            }

            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return resultSet;
    }

    //get result in JSON format
    public JSONArray getResult(String tableName, String[] columns, String where, String orderBy) {
        return getResult(tableName, columns, where, orderBy, "0");
    }

    public JSONArray getResult(String Query, String[] args, String nullOverride) {
        openDatabase();
        JSONArray resultSet = new JSONArray();
        try {
            Cursor cursor = mDatabase.rawQuery(Query, args);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int totalColumns = cursor.getColumnCount();
                JSONObject rowObject = new JSONObject();
                for (int i = 0; i < totalColumns; i++) {
                    try {
                        if (cursor.getString(i) != null)
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        else
                            rowObject.put(cursor.getColumnName(i), nullOverride);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("Tag Name", e.getMessage());
                    }
                }
                resultSet.put(rowObject);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        closeDatabase();
        return resultSet;
    }

    public JSONArray getResult(String Query, String[] args) {
        return getResult(Query, args, "0");
    }

    public String getExportAsXML(String QueryF, String QueryI, String QueryPL, String QueryPR, String QueryIP, String OfficerCode, int OfficerId) throws IOException {
        String Query = null;
        String label = null;
        String sublabel = null;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        File Dir = new File(global.getSubdirectory("Family"));
        Dir.mkdir();


        //Here we are giving name to the XML file
        String FileName = "Enrolment_" + OfficerCode + "_" + d + ".xml";

        //Here we are creating file in that directory
        File EnrollmentXML = new File(Dir, FileName);
        //Here we are creating outputstream
        FileOutputStream fos = new FileOutputStream(EnrollmentXML, true);
        XmlSerializer serializer = Xml.newSerializer();

        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, Boolean.TRUE);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, "Enrolment");

        serializer.startTag(null, "FileInfo");

        serializer.startTag(null, "UserId");
        serializer.text("-2");
        serializer.endTag(null, "UserId");

        serializer.startTag(null, "OfficerId");
        serializer.text(String.valueOf(OfficerId));
        serializer.endTag(null, "OfficerId");

        serializer.endTag(null, "FileInfo");


        try {
            for (int i = 1; i <= 5; i++) {
                if (i == 1) {
                    Query = QueryF;
                    label = "Families";
                    sublabel = "Family";
                } else if (i == 2) {
                    Query = QueryI;
                    label = "Insurees";
                    sublabel = "Insuree";
                } else if (i == 3) {
                    Query = QueryPL;
                    label = "Policies";
                    sublabel = "Policy";
                } else if (i == 4) {
                    Query = QueryIP;
                    label = "InsureePolicies";
                    sublabel = "InsureePolicy";
                } else if (i == 5) {
                    Query = QueryPR;
                    label = "Premiums";
                    sublabel = "Premium";
                }

                serializer.startTag(null, label);
                openDatabase();
                Cursor cursor = mDatabase.rawQuery(Query, null);
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    int totalColumns = cursor.getColumnCount();
                    serializer.startTag(null, sublabel);
                    for (int j = 0; j < totalColumns; j++) {

                        if (cursor.getString(j) != null) {

                            if (label.equals("Families")) {
                                if (cursor.getColumnName(j) == "FamilyType") {
                                    if (cursor.getString(j).equals("0")) {
                                        serializer.startTag(null, cursor.getColumnName(j));
                                        serializer.text("");
                                        serializer.endTag(null, cursor.getColumnName(j));
                                    }

                                } else if (cursor.getColumnName(j) == "ConfirmationType") {
                                    if (cursor.getString(j).equals("0")) {
                                        serializer.startTag(null, cursor.getColumnName(j));
                                        serializer.text("");
                                        serializer.endTag(null, cursor.getColumnName(j));
                                    }
                                } else if (cursor.getColumnName(j).equals("isOffline")) {
                                    String isOffline = cursor.getString(j);
                                    if (isOffline.equals("2"))
                                        isOffline = "0";
                                    serializer.startTag(null, cursor.getColumnName(j));
                                    serializer.text(isOffline);
                                    serializer.endTag(null, cursor.getColumnName(j));
                                } else {
                                    serializer.startTag(null, cursor.getColumnName(j));
                                    serializer.text(cursor.getString(j));
                                    serializer.endTag(null, cursor.getColumnName(j));
                                }
                            } else {
                                serializer.startTag(null, cursor.getColumnName(j));
                                serializer.text(cursor.getString(j));
                                serializer.endTag(null, cursor.getColumnName(j));
                            }


                        } else {
                            serializer.startTag(null, cursor.getColumnName(j));
                            serializer.text("");
                            serializer.endTag(null, cursor.getColumnName(j));
                        }
                    }
                    if (sublabel.equals("Family")) {

                        serializer = addFamilySmsTag(serializer, cursor.getString(0));
                    }
                    serializer.endTag(null, sublabel);
                    cursor.moveToNext();
                }
                serializer.endTag(null, label);
                cursor.close();
                closeDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        serializer.endDocument();
        serializer.flush();
        fos.close();

        return FileName;

    }

    private XmlSerializer addFamilySmsTag(XmlSerializer serializer, String familyId) throws IOException {
        String[] args = {familyId};
        serializer.startTag(null, "FamilySMS");
        try {
            JSONObject familySMS =
                    getResult("SELECT * FROM tblFamilySMS where FamilyId = ? LIMIT 1;",
                            args).getJSONObject(0);
            serializer.startTag(null, "FamilyId");
            serializer.text(args[0]);
            serializer.endTag(null, "FamilyId");

            serializer.startTag(null, "ApprovalOfSMS");
            serializer.text(
                    String.valueOf(familySMS.getString("ApprovalOfSMS").equals("1"))
            );
            serializer.endTag(null, "ApprovalOfSMS");

            serializer.startTag(null, "LanguageOfSMS");
            serializer.text(familySMS.getString("LanguageOfSMS"));

            serializer.endTag(null, "LanguageOfSMS");
        } catch (Exception e) {
            Log.d("CreateEnrolmentXML", "Failed to create FamilySMS tag in enrolment");
            e.printStackTrace();
        }
        serializer.endTag(null, "FamilySMS");
        return serializer;
    }


    public void insertData(String TableName, String[] Columns, String data, String PreExecute) throws JSONException {
        String dbPath = ClientAndroidInterface.filePath;
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        try {
            JSONArray array;
            JSONObject object;

            array = new JSONArray(data);

            if (array.length() == 0)
                return;

            if (!mDatabase.isOpen()) {
                openDatabase();
            }

            if (!TextUtils.isEmpty(PreExecute)) {
                mDatabase.execSQL(PreExecute);
            }


            mDatabase.beginTransaction();
            for (int i = 0; i < array.length(); i++) {
                try {
                    object = array.getJSONObject(i);
                    ContentValues cv = new ContentValues();
                    for (String c : Columns) {
                        cv.put(c, object.getString(c));
                    }
                    mDatabase.insert(TableName, null, cv);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mDatabase.isOpen()) {
            closeDatabase();
        }

    }

    public int insertData(String tableName, ContentValues contentValues) {
        try {
            openDatabase();
            Long lastInsertedId = mDatabase.insertOrThrow(tableName, null, contentValues);
            return (int) (long) lastInsertedId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeDatabase();
        }
    }

    public int updateData(String tableName, ContentValues contentValues, String whereClause, String[] whereArgs, boolean throwOnNoRowsUpdated) throws UserException {
        openDatabase();
        int rowsUpdated = 0;
        try {
            openDatabase();
            rowsUpdated = mDatabase.update(tableName, contentValues, whereClause, whereArgs);
            if (throwOnNoRowsUpdated && rowsUpdated <= 0) {
                throw new UserException(mContext.getResources().getString(R.string.ErrorUpdate));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return rowsUpdated;
    }

    public int updateData(String tableName, ContentValues contentValues, String whereClause, String[] whereArgs) throws UserException {
        return updateData(tableName, contentValues, whereClause, whereArgs, true);
    }

    public void deleteData(String tableName, String whereClause, String[] whereArgs) {
        try {
            openDatabase();
            mDatabase.delete(tableName, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }

    public int getCount(String table, String selection, String[] selectionArgs) {
        openDatabase();
        int result = 0;
        try (Cursor c = mDatabase.query(table,
                new String[]{"COUNT(*)"},
                selection,
                selectionArgs,
                null,
                null,
                null)) {
            c.moveToFirst();
            result = c.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeDatabase();
        }
        return result;
    }

    public int getAssignedCNCount(String officerCode, String productCode) {
        return getCount(tblBulkControlNumbers,
                "PolicyId IS NOT NULL AND UPPER(ProductCode) = UPPER(?) AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{productCode, officerCode});
    }

    public int getFreeCNCount(String officerCode, String productCode) {
        return getCount(tblBulkControlNumbers,
                "PolicyId IS NULL AND UPPER(ProductCode) = UPPER(?) AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{productCode, officerCode});
    }

    public int getAssignedCNCount(String officerCode) {
        return getCount(tblBulkControlNumbers,
                "PolicyId IS NOT NULL AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{officerCode});
    }

    public int getFreeCNCount(String officerCode) {
        return getCount(tblBulkControlNumbers,
                "PolicyId IS NULL AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{officerCode});
    }

    public String getProductCode(String productId) {
        openDatabase();
        String productCode = null;
        try (Cursor cursor = mDatabase.query(tblProduct,
                new String[]{"UPPER(ProductCode)"},
                "ProdId = ?",
                new String[]{productId},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                productCode = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return productCode;
    }

    public JSONArray getAvailableProducts(String officerCode) {
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        String date = format.format(Calendar.getInstance().getTime());

        openDatabase();
        JSONArray result;
        try {
            String query = "SELECT DISTINCT p.ProdId, p.ProductCode, p.ProductName " +
                    "FROM tblOfficer o INNER JOIN tblLocations ld ON o.LocationId=ld.LocationId " +
                    "INNER JOIN tblLocations lr ON ld.ParentLocationId=lr.LocationId " +
                    "INNER JOIN tblProduct p ON (ld.LocationId=p.LocationId OR lr.LocationId=p.LocationId OR p.LocationId='null') " +
                    "WHERE UPPER(o.Code)=UPPER(?) and ? <= p.DateTo";

            Cursor c = mDatabase.rawQuery(query, new String[]{officerCode, date});
            result = cursorToJsonArray(c);
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        } finally {
            closeDatabase();
        }
        return result;
    }

    public String getNextFreeCn(String officerCode, String productCode) {
        openDatabase();
        String result = null;
        try (Cursor c = mDatabase.query(tblBulkControlNumbers,
                new String[]{"ControlNumber"},
                "PolicyId IS NULL AND UPPER(ProductCode) = UPPER(?) AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{productCode, officerCode},
                null,
                null,
                null,
                "1")) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                result = c.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return result;
    }

    public boolean assignCnToPolicy(int policyId, String controlNumber) {
        try {
            if (isFetchedControlNumber(controlNumber)) {
                openDatabase();
                ContentValues values = new ContentValues();
                values.put("PolicyId", policyId);
                mDatabase.update(tblBulkControlNumbers,
                        values,
                        "ControlNumber = ?",
                        new String[]{controlNumber});
            } else {
                openDatabase();
                JSONArray policyData = cursorToJsonArray(mDatabase.rawQuery(
                        "SELECT po.PolicyValue, UPPER(pr.ProductCode) as ProductCode FROM tblPolicy po INNER JOIN tblProduct pr on pr.ProdId=po.ProdId WHERE PolicyId = ?",
                        new String[]{String.valueOf(policyId)}));

                if (policyData.length() == 0) {
                    return false;
                }

                ContentValues values = new ContentValues();
                values.put("OfficerCode", global.getOfficerCode());
                values.put("PolicyId", policyId);
                values.put("Amount", policyData.getJSONObject(0).getString("PolicyValue"));
                values.put("ProductCode", policyData.getJSONObject(0).getString("ProductCode"));
                values.put("ControlNumber", controlNumber);

                return mDatabase.insert(tblBulkControlNumbers, null, values) > 0;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return false;
    }

    public boolean clearCnAssignedToPolicy(int policyId) {
        openDatabase();
        ContentValues values = new ContentValues();
        values.put("PolicyId", (String) null);

        try {
            int updatedRecords = mDatabase.update(tblBulkControlNumbers,
                    values,
                    "PolicyId = ? and Id IS NOT NULL",
                    new String[]{String.valueOf(policyId)});

            int deletedRecords = mDatabase.delete(tblBulkControlNumbers,
                    "PolicyId = ? and Id IS NULL",
                    new String[]{String.valueOf(policyId)});

            return updatedRecords + deletedRecords > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return false;
    }

    public boolean isFetchedControlNumber(String controlNumber) {
        return getCount(tblBulkControlNumbers,
                "ControlNumber = ? AND Id IS NOT NULL",
                new String[]{controlNumber}) > 0;
    }

    private JSONArray cursorToJsonArray(Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                int totalColumn = cursor.getColumnCount();
                JSONObject rowObject = new JSONObject();
                for (int i = 0; i < totalColumn; i++) {
                    if (cursor.getColumnName(i) != null) {
                        rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                    }
                }
                resultSet.put(rowObject);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public int getRegionId(int districtId) {
        openDatabase();
        int result = 0;
        try (Cursor c = mDatabase.query(tblLocations,
                new String[]{"ParentLocationId"},
                "LocationId = ?",
                new String[]{Integer.toString(districtId)},
                null,
                null,
                null,
                "1")) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                result = c.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return result;
    }
}
