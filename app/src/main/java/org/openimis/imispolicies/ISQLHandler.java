package org.openimis.imispolicies;

import androidx.annotation.NonNull;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.intellij.lang.annotations.Language;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public interface ISQLHandler {

    // Basic CRUD
    void insertData(String tableName, ContentValues contentValues);
    void insertData(String TableName, String[] Columns, String data, String PreExecute) throws JSONException;
    void insertData(String TableName, String[] Columns, JSONArray array, String PreExecute) throws JSONException;
    void updateData(String tableName, ContentValues contentValues, String whereClause, String[] whereArgs) throws UserException;
    int updateData(String tableName, ContentValues contentValues, String whereClause, String[] whereArgs, boolean throwOnNoRowsUpdated) throws UserException;
    void deleteData(String tableName, String whereClause, String[] whereArgs);

    // Get counts
    int getCount(String table, String selection, String[] selectionArgs);
    int getAssignedCNCount(String officerCode, String productCode);
    int getFreeCNCount(String officerCode, String productCode);
    int getAssignedCNCount(String officerCode);
    int getFreeCNCount(String officerCode);

    // Product methods
    String getProductCode(String productId);
    @NonNull
    JSONArray getAvailableProducts(String officerCode);

    // Control Number methods
    String getNextFreeCn(String officerCode, String productCode);
    void assignCnToPolicy(int policyId, String controlNumber);
    void clearCnAssignedToPolicy(int policyId);
    boolean isFetchedControlNumber(String controlNumber);

    // Database queries returning JSON
    @NonNull
    JSONArray getResult(String tableName, String[] columns, String where, String orderBy, String nullOverride);
    @NonNull
    JSONArray getResult(String tableName, String[] columns, String where, String orderBy);
    @NonNull
    JSONArray getResult(@Language("SQL") String Query, String[] args, String nullOverride);
    @NonNull
    JSONArray getResult(@Language("SQL") String Query, String[] args);

    // Language methods
    @NonNull
    String getDefaultLanguage();
    @NonNull
    JSONArray getSupportedLanguages();

    // XML export
    void getExportAsXML(
            @Language("SQL") String QueryF,
            @Language("SQL") String QueryI,
            @Language("SQL") String QueryPL,
            @Language("SQL") String QueryPR,
            @Language("SQL") String QueryIP,
            String OfficerCode,
            int OfficerId
    ) throws IOException;

    // Region methods
    int getRegionId(int districtId);

    SQLiteDatabase getReadableDatabase();
}
