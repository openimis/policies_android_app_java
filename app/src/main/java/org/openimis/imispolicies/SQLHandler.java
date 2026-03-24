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
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import org.intellij.lang.annotations.Language;

import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.tools.Log;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class SQLHandler extends SQLiteOpenHelper {

    public static final String DBNAME = "IMIS.db3";
    private static final String OFFLINEDBNAME = "ImisData.db3";
    public Boolean isPrivate = true;
    private final Context context;
    private final Global global;
    private SQLiteDatabase mDatabase;
    private static final int DATABASE_VERSION = 3;

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
    public static final String tblIncomeLevel = "tblIncomeLevel";
    public static final String tblResidenceEnvironment = "tblResidenceEnvironment";
    public static final String tblNoDisability = "tblNoDisability";
    public static final String tblNonDisablingDisease = "tblNonDisablingDisease";
    public static final String tblMutualInsuranceCoverage = "tblMutualInsuranceCoverage";
    public static final String tblHousingType = "tblHousingType";
    public static final String tblContributionPlan = "tblContributionPlan";
    public static final String tblInsureeAttachments = "tblInsureeAttachments";

    public SQLHandler(Context context) {
        super(context, DBNAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
        global = (Global) this.context.getApplicationContext();
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
                            "InsureeChfId TEXT," +
                            "LocationId NUMERIC," +
                            "Poverty BOOLEAN," +
                            "isOffline NUMERIC," +
                            "FamilyType TEXT," +
                            "FamilyAddress TEXT," +
                            "Ethnicity TEXT," +
                            "ConfirmationNo TEXT," +
                            "ConfirmationType TEXT," +
                            "ParentId INTEGER" +
                            ")"
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
                            "RuleValue BIT," +
                            "Usage TEXT" + ")"
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
                            "Vulnerability BOOLEAN," +
                            "ProfessionalSituation TEXT," +
                            "IncomeLevel NUMERIC," +
                            "ResidenceEnvironment NUMERIC," +
                            "PaymentMethod TEXT," +
                            "OtherHousehold TEXT," +
                            "AccountDetails TEXT," +
                            "NoDisability NUMERIC," +
                            "NonDisablingDisease NUMERIC," +
                            "MutualInsuranceCoverage NUMERIC," +
                            "HousingType NUMERIC," +
                            "fixIncome REAL" + ")"
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
                    "CREATE TABLE 'tblPayer' (" +
                            "PayerId NUMERIC," +
                            "PayerName TEXT," +
                            "LocationId NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblPolicy' (" +
                            "PolicyId INTEGER," +
                            "PolicyUuid TEXT," +
                            "FamilyId NUMERIC," +
                            "EnrollDate DATE," +
                            "StartDate DATE," +
                            "EffectiveDate DATE," +
                            "ExpiryDate DATE," +
                            "SigningDate DATE," +
                            "PolicyStatus NUMERIC," +
                            "PolicyValue NUMERIC," +
                            "ProdId NUMERIC," +
                            "ContributionPlanId TEXT," +
                            "OfficerId NUMERIC," +
                            "isOffline NUMERIC," +
                            "Periodicity TEXT," +
                            "PaymentDay TEXT," +
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
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblIncomeLevel + "(" +
                            "Id INTEGER," +
                            "FirstLanguage TEXT," +
                            "SecondLanguage TEXT" +")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblResidenceEnvironment + "("
                            + "id INTEGER PRIMARY KEY,"
                            + "ResidenceEnvironment VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" + ")"
            );
            
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblNoDisability + "("
                            + "id INTEGER PRIMARY KEY,"
                            + "NoDisabilityLabel VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" +")"
            );
            
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblNonDisablingDisease + "("
                            + "id INTEGER PRIMARY KEY,"
                            + "NonDisablingDisease VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" +")"
            );
            
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblMutualInsuranceCoverage + "("
                            + "id INTEGER PRIMARY KEY,"
                            + "MutualInsuranceCoverage VARCHAR(150),"
                            + "AltLanguage VARCHAR(150),"
                            + "SortOrder INTEGER" +")"
            );
            
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblHousingType + "("
                            + "id INTEGER PRIMARY KEY,"
                            + "HousingType VARCHAR(150),"
                            + "AltLanguage VARCHAR(150),"
                            + "SortOrder INTEGER" +")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblContributionPlan + "(" +
                            "Id INTEGER," +
                            "Code TEXT," +
                            "Name TEXT," +
                            "ProductId INTEGER," +
                            "CalculationRules TEXT,"+
                            "Periodicity TEXT," +
                            "ValidFrom DATE," +
                            "ValidTo Date," +
                            "CpId" +")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblInsureeAttachments + "(" +
                            "Id INTEGER," +
                            "Title TEXT," +
                            "Filename TEXT," +
                            "Content TEXT," +
                            "InsureeId INTEGER," +
                            "FamilyId INTEGER" + ")"
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
        // Drop all existing tables
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
        db.execSQL("DROP TABLE IF EXISTS " + tblPayer);
        db.execSQL("DROP TABLE IF EXISTS " + tblPolicy);
        db.execSQL("DROP TABLE IF EXISTS " + tblPremium);
        db.execSQL("DROP TABLE IF EXISTS " + tblProduct);
        db.execSQL("DROP TABLE IF EXISTS " + tblProfessions);
        db.execSQL("DROP TABLE IF EXISTS " + tblRecordedPolicies);
        db.execSQL("DROP TABLE IF EXISTS " + tblRelations);
        db.execSQL("DROP TABLE IF EXISTS " + tblRenewals);
        db.execSQL("DROP TABLE IF EXISTS " + tblIncomeLevel);
        db.execSQL("DROP TABLE IF EXISTS " + tblResidenceEnvironment);
        db.execSQL("DROP TABLE IF EXISTS " + tblNoDisability);
        db.execSQL("DROP TABLE IF EXISTS " + tblNonDisablingDisease);
        db.execSQL("DROP TABLE IF EXISTS " + tblMutualInsuranceCoverage);
        db.execSQL("DROP TABLE IF EXISTS " + tblHousingType);
        db.execSQL("DROP TABLE IF EXISTS " + tblInsureeAttachments);
        db.execSQL("DROP TABLE IF EXISTS " + tblBulkControlNumbers);
        db.execSQL("DROP TABLE IF EXISTS " + tblFamilySMS);
        db.execSQL("DROP TABLE IF EXISTS " + tblContributionPlan);
        
        // Recreate all tables with the current schema
        onCreate(db);
        
        android.util.Log.d("Upgrade", "DB Version upgraded from " + oldVersion + " to " + newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
        mDatabase = db;
        ensureInsureeTableHasRequiredColumns();
    }

    private void openDatabase() {
        String dbPath = context.getDatabasePath(DBNAME).getPath();
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
    
    private boolean tableExists(SQLiteDatabase db, String tableName) {
        try (Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?", 
                new String[]{tableName})) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error checking if table exists: " + e.getMessage());
            return false;
        }
    }
    
    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null)) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                if (columnName.equals(name)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error checking if column exists: " + e.getMessage());
            return false;
        }
    }
    
    private void ensureInsureeTableHasRequiredColumns() {
        try {
            openDatabase();
            
            // Check and add ResidenceEnvironment column if missing
            if (!columnExists(mDatabase, "tblInsuree", "ResidenceEnvironment")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN ResidenceEnvironment NUMERIC");
            }
            
            // Check and add IncomeLevel column if missing
            if (!columnExists(mDatabase, "tblInsuree", "IncomeLevel")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN IncomeLevel NUMERIC");
            }
            
            // Check and add other potentially missing columns
            if (!columnExists(mDatabase, "tblInsuree", "PaymentMethod")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN PaymentMethod TEXT");
            }
            
            if (!columnExists(mDatabase, "tblInsuree", "OtherHousehold")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN OtherHousehold TEXT");
            }
            
            if (!columnExists(mDatabase, "tblInsuree", "AccountDetails")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN AccountDetails TEXT");
            }
            
            // Check and add new columns for the 4 new fields
            if (!columnExists(mDatabase, "tblInsuree", "NoDisability")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN NoDisability NUMERIC");
            }
            
            // Add missing location columns
            if (!columnExists(mDatabase, "tblInsuree", "CurDistrict")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN CurDistrict NUMERIC");
                Log.d("Database", "Added missing column: CurDistrict to tblInsuree");
            }
            
            if (!columnExists(mDatabase, "tblInsuree", "CurWard")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN CurWard NUMERIC");
                Log.d("Database", "Added missing column: CurWard to tblInsuree");
            }
            
            if (!columnExists(mDatabase, "tblInsuree", "CurRegion")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN CurRegion NUMERIC");
                Log.d("Database", "Added missing column: CurRegion to tblInsuree");
            }
            
            if (!columnExists(mDatabase, "tblInsuree", "NonDisablingDisease")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN NonDisablingDisease NUMERIC");
            }
            
            if (!columnExists(mDatabase, "tblInsuree", "MutualInsuranceCoverage")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN MutualInsuranceCoverage NUMERIC");
            }
            
            if (!columnExists(mDatabase, "tblInsuree", "HousingType")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN HousingType NUMERIC");
            }

            if (!columnExists(mDatabase, "tblInsuree", "fixIncome")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN fixIncome REAL");
            }
            
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error ensuring tblInsuree has required columns: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }

    @NonNull
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
        
                }
            }

            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return resultSet;
    }

    @NonNull
    public JSONArray getResult(String tableName, String[] columns, String where, String orderBy) {
        return getResult(tableName, columns, where, orderBy, "0");
    }

    @NonNull
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
    @NonNull
    public JSONArray getResult(@Language("SQL") String Query, String[] args) {
        return getResult(Query, args, "0");
    }

    public void getExportAsXML(
            @Language("SQL") String QueryF,
            @Language("SQL") String QueryI,
            @Language("SQL") String QueryPL,
            @Language("SQL") String QueryPR,
            @Language("SQL") String QueryIP,
            String OfficerCode,
            int OfficerId
    ) throws IOException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        File Dir = new File(global.getSubdirectory("Family"));

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
                String subLabel;
                String label;
                String Query;
                if (i == 1) {
                    Query = QueryF;
                    label = "Families";
                    subLabel = "Family";
                } else if (i == 2) {
                    Query = QueryI;
                    label = "Insurees";
                    subLabel = "Insuree";
                } else if (i == 3) {
                    Query = QueryPL;
                    label = "Policies";
                    subLabel = "Policy";
                } else if (i == 4) {
                    Query = QueryIP;
                    label = "InsureePolicies";
                    subLabel = "InsureePolicy";
                } else {
                    Query = QueryPR;
                    label = "Premiums";
                    subLabel = "Premium";
                }

                serializer.startTag(null, label);
                openDatabase();
                Cursor cursor = mDatabase.rawQuery(Query, null);
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    int totalColumns = cursor.getColumnCount();
                    serializer.startTag(null, subLabel);
                    for (int j = 0; j < totalColumns; j++) {

                        if (cursor.getString(j) != null) {

                            if (label.equals("Families")) {
                                if (Objects.equals(cursor.getColumnName(j), "FamilyType")) {
                                    if (cursor.getString(j).equals("0")) {
                                        serializer.startTag(null, cursor.getColumnName(j));
                                        serializer.text("");
                                        serializer.endTag(null, cursor.getColumnName(j));
                                    }

                                } else if (Objects.equals(cursor.getColumnName(j), "ConfirmationType")) {
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
                    if (subLabel.equals("Family")) {
                        addFamilySmsTag(serializer, cursor.getString(0));
                    }
                    serializer.endTag(null, subLabel);
                    cursor.moveToNext();
                }
                serializer.endTag(null, label);
                cursor.close();
                closeDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        serializer.endTag(null, "Enrolment");
        serializer.endDocument();
        serializer.flush();
        fos.close();
    }

    private void addFamilySmsTag(@NonNull XmlSerializer serializer, String familyId) throws IOException {
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
            e.printStackTrace();
        }
        serializer.endTag(null, "FamilySMS");
    }

    public void insertData(String TableName, String[] Columns, String data, String PreExecute) throws JSONException {
        insertData(TableName, Columns, new JSONArray(data), PreExecute);
    }

    public void insertData(String TableName, String[] Columns, JSONArray array, String PreExecute) throws JSONException {
        String dbPath = ClientAndroidInterface.filePath;
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        try {
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
                    JSONObject object = array.getJSONObject(i);
                    ContentValues cv = new ContentValues();
                    for (String c : Columns) {
                        try {
                            cv.put(c, object.getString(c));
                        } catch (JSONException ignored) {

                        }
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

    public void insertData(String tableName, ContentValues contentValues) {
        try {
            // Ensure tblInsuree has required columns before inserting
            if ("tblInsuree".equals(tableName)) {
                ensureInsureeTableHasRequiredColumns();
            }
            
            openDatabase();
            mDatabase.insertOrThrow(tableName, null, contentValues);
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
            // Ensure tblInsuree has required columns before updating
            if ("tblInsuree".equals(tableName)) {
                ensureInsureeTableHasRequiredColumns();
            }
            openDatabase();
            rowsUpdated = mDatabase.update(tableName, contentValues, whereClause, whereArgs);
            if (throwOnNoRowsUpdated && rowsUpdated <= 0) {
                throw new UserException(context.getResources().getString(R.string.ErrorUpdate));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return rowsUpdated;
    }

    public void updateData(String tableName, ContentValues contentValues, String whereClause, String[] whereArgs) throws UserException {
        updateData(tableName, contentValues, whereClause, whereArgs, true);
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
        try (Cursor c = mDatabase.query(table,
                new String[]{"COUNT(*)"},
                selection,
                selectionArgs,
                null,
                null,
                null)) {
            c.moveToFirst();
            return c.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeDatabase();
        }
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

    public void assignCnToPolicy(int policyId, String controlNumber) {
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
                    return;
                }

                ContentValues values = new ContentValues();
                values.put("OfficerCode", global.getOfficerCode());
                values.put("PolicyId", policyId);
                values.put("Amount", policyData.getJSONObject(0).getString("PolicyValue"));
                values.put("ProductCode", policyData.getJSONObject(0).getString("ProductCode"));
                values.put("ControlNumber", controlNumber);

                mDatabase.insert(tblBulkControlNumbers, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }

    public void clearCnAssignedToPolicy(int policyId) {
        openDatabase();
        ContentValues values = new ContentValues();
        values.put("PolicyId", (String) null);

        try {
            mDatabase.update(tblBulkControlNumbers,
                    values,
                    "PolicyId = ? and Id IS NOT NULL",
                    new String[]{String.valueOf(policyId)});

            mDatabase.delete(tblBulkControlNumbers,
                    "PolicyId = ? and Id IS NULL",
                    new String[]{String.valueOf(policyId)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
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

    /**
     * @return Default Language as specified by SortOrder in tblLanguages, return DEFAULT_LANGUAGE_CODE before initialization
     */
    @NonNull
    public String getDefaultLanguage() {
        openDatabase();
        String result = BuildConfig.DEFAULT_LANGUAGE_CODE;
        try (Cursor c = mDatabase.query(tblLanguages,
                new String[]{"LanguageCode"},
                null,
                null,
                null,
                null,
                "SortOrder ASC",
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

    @NonNull
    public JSONArray getSupportedLanguages() {
        return getResult(tblLanguages, new String[]{"LanguageCode"}, null, null);
    }

    public int getProductId(String productCode) {
        openDatabase();
        String productId = null;
        try (Cursor cursor = mDatabase.query(tblProduct,
                new String[]{"ProdId"},
                "ProductCode = ?",
                new String[]{productCode},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                productId = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return Integer.parseInt(Objects.requireNonNull(productId));
    }

    public int getContributionProductId(String contributionPlanId) {
        openDatabase();
        String productId = null;
        try (Cursor cursor = mDatabase.query(tblContributionPlan,
                new String[]{"ProductId"},
                "CpId = ?",
                new String[]{contributionPlanId},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                productId = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return Integer.parseInt(Objects.requireNonNull(productId));
    }

    public int getOfficerId(String officerCode) {
        openDatabase();
        String officerId = null;
        try (Cursor cursor = mDatabase.query(tblOfficer,
                new String[]{"OfficerId"},
                "Code = ?",
                new String[]{officerCode},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                officerId = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return Integer.parseInt(Objects.requireNonNull(officerId));
    }

    public String getContributionPlanId(String contributionPlanCode) {
        openDatabase();
        String cpId = null;
        try (Cursor cursor = mDatabase.query(tblContributionPlan,
                new String[]{"CpId"},
                "Code = ?",
                new String[]{contributionPlanCode},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                cpId = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return cpId;
    }

    public String getContributionPlanCode(String contributionPlanId) {
        openDatabase();
        String cpCode = null;
        try (Cursor cursor = mDatabase.query(tblContributionPlan,
                new String[]{"Code"},
                "CpId = ?",
                new String[]{contributionPlanId},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                cpCode = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return cpCode;
    }

    // Methods to handle ResidenceEnvironment data
    public void insertResidenceEnvironment(int code, String residenceEnvironment, String altLanguage, int sortOrder) {

        openDatabase();
        try {
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblResidenceEnvironment)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblResidenceEnvironment + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "ResidenceEnvironment VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" + ")"
                );

            }
            
            ContentValues values = new ContentValues();
            values.put("Code", code);
            values.put("ResidenceEnvironment", residenceEnvironment);
            values.put("AltLanguage", altLanguage);
            values.put("SortOrder", sortOrder);
            mDatabase.insertWithOnConflict(tblResidenceEnvironment, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error inserting ResidenceEnvironment: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }

    public JSONArray getResidenceEnvironments() {

        try {
            openDatabase();
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblResidenceEnvironment)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblResidenceEnvironment + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "ResidenceEnvironment VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" + ")"
                );

            }
            closeDatabase();
            
            JSONArray result = getResult(tblResidenceEnvironment, null, null, "SortOrder ASC");

            return result;
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error getting ResidenceEnvironments: " + e.getMessage());
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public String getResidenceEnvironmentByCode(int code) {
        openDatabase();
        String residenceEnvironment = null;
        try {
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblResidenceEnvironment)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblResidenceEnvironment + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "ResidenceEnvironment VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" + ")"
                );

            }
            
            try (Cursor cursor = mDatabase.query(tblResidenceEnvironment,
                    new String[]{"ResidenceEnvironment"},
                    "Code = ?",
                    new String[]{String.valueOf(code)},
                    null,
                    null,
                    null,
                    "1")) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    residenceEnvironment = cursor.getString(0);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error getting ResidenceEnvironment by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return residenceEnvironment;
    }

    public void insertResidenceEnvironments(JSONArray residenceEnvironments) {

        try {
            openDatabase();
            
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblResidenceEnvironment)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblResidenceEnvironment + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "ResidenceEnvironment VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" + ")"
                );

            }
            
            // Clear existing data
            mDatabase.delete(tblResidenceEnvironment, null, null);

            closeDatabase();
            
            // Insert new data
            for (int i = 0; i < residenceEnvironments.length(); i++) {
                JSONObject item = residenceEnvironments.getJSONObject(i);
                insertResidenceEnvironment(
                    item.getInt("Code"),
                    item.getString("ResidenceEnvironment"),
                    item.optString("AltLanguage", ""),
                    item.optInt("SortOrder", 0)
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error inserting ResidenceEnvironments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== IncomeLevel Methods ====================
    public void insertIncomeLevel(int code, String incomeLevel, String altLanguage, int sortOrder) {

        openDatabase();
        try {
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblIncomeLevel)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblIncomeLevel + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "IncomeLevel VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" + ")"
                );

            }
            
            ContentValues values = new ContentValues();
            values.put("Code", code);
            values.put("IncomeLevel", incomeLevel);
            values.put("AltLanguage", altLanguage);
            values.put("SortOrder", sortOrder);
            mDatabase.insertWithOnConflict(tblIncomeLevel, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error inserting IncomeLevel: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }

    public JSONArray getIncomeLevels() {

        try {
            openDatabase();
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblIncomeLevel)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblIncomeLevel + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "IncomeLevel VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" + ")"
                );

            }
            closeDatabase();
            
            JSONArray result = getResult(tblIncomeLevel, null, null, "SortOrder ASC");

            return result;
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error getting IncomeLevels: " + e.getMessage());
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public String getIncomeLevelByCode(int code) {
        openDatabase();
        String incomeLevel = null;
        try {
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblIncomeLevel)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblIncomeLevel + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "IncomeLevel VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" + ")"
                );

            }
            
            try (Cursor cursor = mDatabase.query(tblIncomeLevel,
                    new String[]{"IncomeLevel"},
                    "Code = ?",
                    new String[]{String.valueOf(code)},
                    null,
                    null,
                    null,
                    "1")) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    incomeLevel = cursor.getString(0);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error getting IncomeLevel by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return incomeLevel;
    }

    public void insertIncomeLevels(JSONArray incomeLevels) {


        try {
            openDatabase();
            
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblIncomeLevel)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblIncomeLevel + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "IncomeLevel VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" + ")"
                );

            }
            
            // Clear existing data
            mDatabase.delete(tblIncomeLevel, null, null);

            closeDatabase();
            
            // Insert new data
            for (int i = 0; i < incomeLevels.length(); i++) {
                JSONObject item = incomeLevels.getJSONObject(i);
                int code = item.optInt("Code", item.optInt("code", 0));
                String incomeLevel = item.optString("IncomeLevel", item.optString("incomeLevel", item.optString("name", "")));
                String altLanguage = item.optString("AltLanguage", item.optString("altLanguage", ""));
                int sortOrder = item.optInt("SortOrder", item.optInt("sortOrder", 0));
                
                insertIncomeLevel(code, incomeLevel, altLanguage, sortOrder);
            }
        } catch (JSONException e) {
            // Gestion silencieuse de l'erreur JSON
        } catch (Exception e) {
            // Silent handling of database errors
        }
    }

    // ==================== NoDisability Methods ====================
    public void insertNoDisability(int code, String name, String altLanguage, int sortOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Code", code);
        values.put("NoDisabilityLabel", name);
        values.put("AltLanguage", altLanguage);
        values.put("SortOrder", sortOrder);
        db.insert(tblNoDisability, null, values);
        db.close();
    }

    public JSONArray getNoDisabilities() {
        try {
            openDatabase();
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblNoDisability)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblNoDisability + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "NoDisabilityLabel VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" +")"
                );

            }
            closeDatabase();
            
            JSONArray result = getResult(tblNoDisability, null, null, "SortOrder ASC");

            return result;
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error getting NoDisabilities: " + e.getMessage());
            e.printStackTrace();
            return new JSONArray();
        }
    }



    // ==================== NonDisablingDisease Methods ====================
    public void insertNonDisablingDisease(int code, String name, String altLanguage, int sortOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Code", code);
        values.put("NonDisablingDisease", name);
        values.put("AltLanguage", altLanguage);
        values.put("SortOrder", sortOrder);
        db.insert(tblNonDisablingDisease, null, values);
        db.close();
    }

    public JSONArray getNonDisablingDiseases() {
        try {
            openDatabase();
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblNonDisablingDisease)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblNonDisablingDisease + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "NonDisablingDisease VARCHAR(100),"
                            + "AltLanguage VARCHAR(100),"
                            + "SortOrder INTEGER" +")"
                );

            }
            closeDatabase();
            
            JSONArray result = getResult(tblNonDisablingDisease, null, null, "SortOrder ASC");

            return result;
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error getting NonDisablingDiseases: " + e.getMessage());
            e.printStackTrace();
            return new JSONArray();
        }
    }



    // ==================== MutualInsuranceCoverage Methods ====================
    public void insertMutualInsuranceCoverage(int code, String name, String altLanguage, int sortOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Code", code);
        values.put("MutualInsuranceCoverage", name);
        values.put("AltLanguage", altLanguage);
        values.put("SortOrder", sortOrder);
        db.insert(tblMutualInsuranceCoverage, null, values);
        db.close();
    }

    public JSONArray getMutualInsuranceCoverages() {
        try {
            openDatabase();
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblMutualInsuranceCoverage)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblMutualInsuranceCoverage + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "MutualInsuranceCoverage VARCHAR(150),"
                            + "AltLanguage VARCHAR(150),"
                            + "SortOrder INTEGER" +")"
                );

            }
            closeDatabase();
            
            JSONArray result = getResult(tblMutualInsuranceCoverage, null, null, "SortOrder ASC");

            return result;
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error getting MutualInsuranceCoverages: " + e.getMessage());
            e.printStackTrace();
            return new JSONArray();
        }
    }



    // ==================== HousingType Methods ====================
    public void insertHousingType(int code, String name, String altLanguage, int sortOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Code", code);
        values.put("HousingType", name);
        values.put("AltLanguage", altLanguage);
        values.put("SortOrder", sortOrder);
        db.insert(tblHousingType, null, values);
        db.close();
    }

    public JSONArray getHousingTypes() {
        try {
            openDatabase();
            // Check if table exists, create it if it doesn't
            if (!tableExists(mDatabase, tblHousingType)) {

                mDatabase.execSQL(
                    "CREATE TABLE " + tblHousingType + "("
                            + "Code INTEGER PRIMARY KEY,"
                            + "HousingType VARCHAR(150),"
                            + "AltLanguage VARCHAR(150),"
                            + "SortOrder INTEGER" +")"
                );

            }
            closeDatabase();
            
            JSONArray result = getResult(tblHousingType, null, null, "SortOrder ASC");

            return result;
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error getting HousingTypes: " + e.getMessage());
            e.printStackTrace();
            return new JSONArray();
        }
    }



    // ==================== Column Management Methods ====================
    
    /**
     * Ensure tblInsuree table has all required columns for the new fields
     */
    private void ensureInsureeTableHasNewColumns() {
        try {
            openDatabase();
            
            // Check and add NoDisability column
            if (!columnExists(mDatabase, "tblInsuree", "NoDisability")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN NoDisability NUMERIC");

            }
            
            // Check and add NonDisablingDisease column
            if (!columnExists(mDatabase, "tblInsuree", "NonDisablingDisease")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN NonDisablingDisease NUMERIC");

            }
            
            // Check and add MutualInsuranceCoverage column
            if (!columnExists(mDatabase, "tblInsuree", "MutualInsuranceCoverage")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN MutualInsuranceCoverage NUMERIC");

            }
            
            // Check and add HousingType column
            if (!columnExists(mDatabase, "tblInsuree", "HousingType")) {
                mDatabase.execSQL("ALTER TABLE tblInsuree ADD COLUMN HousingType NUMERIC");

            }
            
        } catch (Exception e) {
            android.util.Log.e("SQLHandler", "Error ensuring new columns in tblInsuree: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifie le contenu des 4 nouvelles tables pour s'assurer qu'elles sont correctement peuplées
     * Cette méthode est silencieuse et ne génère pas de logs
     */
    public void verifyNewTablesContent() {
        // Silent table verification
        try {
            getIncomeLevels();
        } catch (Exception e) {
            // Gestion silencieuse des erreurs
        }
        
        try {
            getNoDisabilities();
        } catch (Exception e) {
            // Gestion silencieuse des erreurs
        }
        
        try {
            getNonDisablingDiseases();
        } catch (Exception e) {
            // Gestion silencieuse des erreurs
        }
        
        try {
            getMutualInsuranceCoverages();
        } catch (Exception e) {
            // Gestion silencieuse des erreurs
        }
        
        try {
            getHousingTypes();
        } catch (Exception e) {
            // Gestion silencieuse des erreurs
        }
    }
    
    /**
     * Insère les données de la table NoDisabilities dans la base de données
     * @param jsonArray Données JSON à insérer
     */
    public void insertNoDisabilities(JSONArray jsonArray) {
        try {
            openDatabase();
            
            if (!tableExists(mDatabase, tblNoDisability)) {
                String createTableQuery = "CREATE TABLE " + tblNoDisability + " (" +
                        "Code INTEGER PRIMARY KEY, " +
                        "NoDisabilityLabel VARCHAR(100), " +
                        "AltLanguage VARCHAR(100), " +
                        "SortOrder INTEGER)";
                mDatabase.execSQL(createTableQuery);
            }
            
            mDatabase.execSQL("DELETE FROM " + tblNoDisability);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                int code = item.optInt("Code", item.optInt("code", item.optInt("id", i + 1)));
                String noDisabilityLabel = item.optString("NoDisability", item.optString("name", item.optString("noDisabilityLabel", "")));
                String altLanguage = item.optString("AltLanguage", item.optString("altLanguage", ""));
                int sortOrder = item.optInt("SortOrder", item.optInt("sortOrder", item.optInt("id", i + 1)));
                
                ContentValues values = new ContentValues();
                values.put("Code", code);
                values.put("NoDisabilityLabel", noDisabilityLabel);
                values.put("AltLanguage", altLanguage);
                values.put("SortOrder", sortOrder);
                mDatabase.insertWithOnConflict(tblNoDisability, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }
    
    /**
     * Insert NonDisablingDiseases data from server (batch insertion)
     * @param jsonArray JSONArray containing NonDisablingDiseases data from server
     */
    public void insertNonDisablingDiseases(JSONArray jsonArray) {
        try {
            openDatabase();
            
            // Create table if it doesn't exist
            if (!tableExists(mDatabase, tblNonDisablingDisease)) {
                String createTableQuery = "CREATE TABLE " + tblNonDisablingDisease + " (" +
                        "Code INTEGER PRIMARY KEY, " +
                        "NonDisablingDisease VARCHAR(100), " +
                        "AltLanguage VARCHAR(100), " +
                        "SortOrder INTEGER)";
                mDatabase.execSQL(createTableQuery);
            }
            
            // Clear existing data
            mDatabase.execSQL("DELETE FROM " + tblNonDisablingDisease);
            
            // Insert new data
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                int code = item.optInt("Code", item.optInt("code", item.optInt("id", i + 1)));
                String nonDisablingDisease = item.optString("NonDisablingDisease", item.optString("name", item.optString("nonDisablingDisease", "")));
                String altLanguage = item.optString("AltLanguage", item.optString("altLanguage", ""));
                int sortOrder = item.optInt("SortOrder", item.optInt("sortOrder", item.optInt("id", i + 1)));
                
                ContentValues values = new ContentValues();
                values.put("Code", code);
                values.put("NonDisablingDisease", nonDisablingDisease);
                values.put("AltLanguage", altLanguage);
                values.put("SortOrder", sortOrder);
                mDatabase.insertWithOnConflict(tblNonDisablingDisease, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }
    
    /**
     * Insert MutualInsuranceCoverages data from server (batch insertion)
     * @param jsonArray JSONArray containing MutualInsuranceCoverages data from server
     */
    public void insertMutualInsuranceCoverages(JSONArray jsonArray) {
        try {
            openDatabase();
            
            // Create table if it doesn't exist
            if (!tableExists(mDatabase, tblMutualInsuranceCoverage)) {
                String createTableQuery = "CREATE TABLE " + tblMutualInsuranceCoverage + " (" +
                        "Code INTEGER PRIMARY KEY, " +
                        "MutualInsuranceCoverage VARCHAR(150), " +
                        "AltLanguage VARCHAR(150), " +
                        "SortOrder INTEGER)";
                mDatabase.execSQL(createTableQuery);
            }
            
            // Clear existing data
            mDatabase.execSQL("DELETE FROM " + tblMutualInsuranceCoverage);
            
            // Insert new data
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                int code = item.optInt("Code", item.optInt("code", item.optInt("id", i + 1)));
                String mutualInsuranceCoverage = item.optString("MutualInsuranceCoverage", item.optString("name", item.optString("mutualInsuranceCoverage", "")));
                String altLanguage = item.optString("AltLanguage", item.optString("altLanguage", ""));
                int sortOrder = item.optInt("SortOrder", item.optInt("sortOrder", item.optInt("id", i + 1)));
                
                ContentValues values = new ContentValues();
                values.put("Code", code);
                values.put("MutualInsuranceCoverage", mutualInsuranceCoverage);
                values.put("AltLanguage", altLanguage);
                values.put("SortOrder", sortOrder);
                mDatabase.insertWithOnConflict(tblMutualInsuranceCoverage, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }
    
    /**
     * Insert HousingTypes data from server (batch insertion)
     * @param jsonArray JSONArray containing HousingTypes data from server
     */
    public void insertHousingTypes(JSONArray jsonArray) {
        try {
            openDatabase();
            
            // Create table if it doesn't exist
            if (!tableExists(mDatabase, tblHousingType)) {
                String createTableQuery = "CREATE TABLE " + tblHousingType + " (" +
                        "Code INTEGER PRIMARY KEY, " +
                        "HousingType VARCHAR(150), " +
                        "AltLanguage VARCHAR(150), " +
                        "SortOrder INTEGER)";
                mDatabase.execSQL(createTableQuery);
            }
            
            // Clear existing data
            mDatabase.execSQL("DELETE FROM " + tblHousingType);
            
            // Insert new data
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                int code = item.optInt("Code", item.optInt("code", item.optInt("id", i + 1)));
                String housingType = item.optString("HousingType", item.optString("name", item.optString("housingType", "")));
                String altLanguage = item.optString("AltLanguage", item.optString("altLanguage", ""));
                int sortOrder = item.optInt("SortOrder", item.optInt("sortOrder", item.optInt("id", i + 1)));
                
                ContentValues values = new ContentValues();
                values.put("Code", code);
                values.put("HousingType", housingType);
                values.put("AltLanguage", altLanguage);
                values.put("SortOrder", sortOrder);
                mDatabase.insertWithOnConflict(tblHousingType, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }
    
    /**
     * Initialize database with new columns if needed
     * Call this method before performing any Insuree operations
     */
    public void initializeNewColumns() {
        ensureInsureeTableHasNewColumns();
    }
}
