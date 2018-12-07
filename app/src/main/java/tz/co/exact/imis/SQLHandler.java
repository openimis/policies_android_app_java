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

package tz.co.exact.imis;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
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

import static tz.co.exact.imis.MainActivity.global;

public class SQLHandler extends SQLiteOpenHelper {

    public static final String DBNAME = "IMIS.db3";
    private static final String OFFLINEDBNAME = "ImisData.db3";
    public Boolean isPrivate = true;
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private static final int DATABASE_VERSION = 2;
    int count = 1;



    public SQLHandler(Context context) {
        super(context, DBNAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2){
            String sql = "ALTER TABLE tblRenewals ADD COLUMN LocationId INTEGER;";
            db.execSQL(sql);
            Log.d("Upgrade", "DB Version upgraded from 1 to 2");
        }
    }

    private void openDatabase() {
        String dbPath = mContext.getDatabasePath(DBNAME).getPath();
        String dbOfflinePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/" + OFFLINEDBNAME;
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


    //get result in JSON format
    public JSONArray getResult(String tableName, String[] columns, String Where, String OrderBy) {
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
                        rowObject.put(cursor.getColumnName(i), "");
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

    public JSONArray getResult(String Query, String[] args) {
        openDatabase();
        JSONArray resultSet = new JSONArray();
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
                        rowObject.put(cursor.getColumnName(i), "0");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Tag Name", e.getMessage());
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        closeDatabase();
        return resultSet;

    }

    public String getResultXML2(String QueryF,String QueryI,String QueryPL,String QueryPR,String QueryIP,String OfficerCode, int OfficerId) throws IOException {
        String Query = null;
        String label = null;
        String sublabel = null;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        String Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMIS/";
        //Here we are creating a directory
        File MyDir = new File(Path);
        MyDir.mkdir();

        File Dir = new File(Path + "Family");
        Dir.mkdir();


        //Here we are giving name to the XML file
        String FileName = "Enrolment_"+OfficerCode+"_"+d+".xml";

        //Here we are creating file in that directory
        File EnrollmentXML = new File(Dir,FileName);
        //Here we are creating outputstream
        FileOutputStream fos = new FileOutputStream(EnrollmentXML,true);
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


        try{
            for(int i = 1; i <= 5; i++){
                if(i == 1){
                    Query = QueryF;
                    label = "Families";
                    sublabel = "Family";
                }else if(i == 2){
                    Query = QueryI;
                    label = "Insurees";
                    sublabel = "Insuree";
                }else if(i == 3){
                    Query = QueryPL;
                    label = "Policies";
                    sublabel = "Policy";
                }else if(i == 4){
                    Query = QueryIP;
                    label = "InsureePolicies";
                    sublabel = "InsureePolicy";
                }else if(i == 5){
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

                        if (cursor.getString(j) != null){

                            if(label.equals("Families")){
                                if(cursor.getColumnName(j) == "FamilyType"){
                                    if(cursor.getString(j).equals("0")){
                                        serializer.startTag(null, cursor.getColumnName(j));
                                        serializer.text("");
                                        serializer.endTag(null, cursor.getColumnName(j));
                                    }

                                }else if(cursor.getColumnName(j) == "ConfirmationType"){
                                    if(cursor.getString(j).equals("0")){
                                        serializer.startTag(null, cursor.getColumnName(j));
                                        serializer.text("");
                                        serializer.endTag(null, cursor.getColumnName(j));
                                    }
                                }else{
                                    serializer.startTag(null, cursor.getColumnName(j));
                                    serializer.text(cursor.getString(j));
                                    serializer.endTag(null, cursor.getColumnName(j));
                                }
                            }else {
                                serializer.startTag(null, cursor.getColumnName(j));
                                serializer.text(cursor.getString(j));
                                serializer.endTag(null, cursor.getColumnName(j));
                            }


                        } else{
                            serializer.startTag(null, cursor.getColumnName(j));
                            serializer.text("");
                            serializer.endTag(null, cursor.getColumnName(j));
                        }

                    }
                    serializer.endTag(null, sublabel);
                    cursor.moveToNext();
                }
                serializer.endTag(null, label);
                cursor.close();
                closeDatabase();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        serializer.endDocument();
        serializer.flush();
        fos.close();

        return FileName;

    }


    public void insertData(String TableName, String[] Columns, String data, String PreExecute) throws JSONException {
        openDatabase();

        JSONArray array = null;
        JSONObject object;

        array = new JSONArray(data);

        if(array.length()==0)
            return;

        if (!TextUtils.isEmpty(PreExecute))
            mDatabase.execSQL(PreExecute);

        mDatabase.beginTransaction();
        for(int i= 0;i < array.length();i++){
            try {
                object = array.getJSONObject(i);
                ContentValues cv = new ContentValues();
                for(String c: Columns){

                    cv.put(c,  object.getString(c));
                }
                mDatabase.insert(TableName,null,cv);

            } catch (JSONException e) {
                e.printStackTrace();
                throw e;
            }

        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        mDatabase.close();
    }

    public int insertData(String tableName, ContentValues contentValues) throws UserException {
        try {
            openDatabase();
            Long lastInsertedId = mDatabase.insertOrThrow(tableName, null, contentValues);
            if (lastInsertedId <= 0) {
                throw new UserException((String) mContext.getResources().getText(R.string.ErrorInsert));
            }
            return (int) (long) lastInsertedId;
        } catch (SQLException | UserException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeDatabase();
        }
    }

    public int updateData(String tableName, ContentValues contentValues, String whereClause, String[] whereArgs) throws UserException {
        int rowsUpdated;
        try {
            openDatabase();
            rowsUpdated = mDatabase.update(tableName, contentValues, whereClause, whereArgs);
            if (rowsUpdated <= 0) {
                throw new UserException(mContext.getResources().getString(R.string.ErrorUpdate));
            }
            return rowsUpdated;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeDatabase();
        }
    }


    public void deleteData(String tableName, String whereClause, String[] whereArgs) {
        try {
            openDatabase();
            mDatabase.delete(tableName, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeDatabase();
        }
    }

}
