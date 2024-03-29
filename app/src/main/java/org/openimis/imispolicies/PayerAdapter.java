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

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.intellij.lang.annotations.Language;

public class PayerAdapter extends CursorAdapter {

    private final SQLHandler sqlHandler;

    public PayerAdapter(Context context) {
        //noinspection deprecation
        super(context, null);
        sqlHandler = new SQLHandler(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.payer_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final int descColumnIndex = cursor.getColumnIndexOrThrow("PayerName");
        String Suggestion = cursor.getString(descColumnIndex);
        TextView text1 = view.findViewById(R.id.text1);
        text1.setText(Suggestion);

    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }

        String locationId = "1";
        return SearchPayer(
                (constraint != null ? constraint.toString() : ""), locationId);
    }

    private Cursor SearchPayer(String InputText, String LocationId) {
        @Language("SQL")
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

    @Override
    public CharSequence convertToString(Cursor cursor) {
        final int columnIndex = cursor.getColumnIndexOrThrow("PayerName");
        return cursor.getString(columnIndex);
    }
}


