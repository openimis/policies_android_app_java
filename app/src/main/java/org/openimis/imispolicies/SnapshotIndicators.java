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

import android.app.DatePickerDialog;
import android.app.ProgressDialog;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.DatePicker;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.exact.CallSoap.CallSoap;
import com.exact.general.General;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.openimis.imispolicies.R;

public class SnapshotIndicators extends AppCompatActivity {

    private General _General = new General(AppInformation.DomainInfo.getDomain());
    private ClientAndroidInterface ca;
    private ProgressDialog pd;

    Calendar myCalendar;

    Button btnPick;
    Button btnGet;

    TextView tvDate;
    TextView FAPC;
    TextView FEPC;
    TextView FIPC;
    TextView FSPC;

    RelativeLayout snapshotReport;

    String snapshot = null;
    public static boolean inProgress = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapshot_indicators);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.SnapshotIndicators));

        ca = new ClientAndroidInterface(this);

        btnPick= (Button) findViewById(R.id.btnPick);
        btnGet= (Button) findViewById(R.id.btnGet);

        tvDate= (TextView) findViewById(R.id.tvDate);

        FAPC= (TextView) findViewById(R.id.FAPC);
        FEPC= (TextView) findViewById(R.id.FEPC);
        FIPC= (TextView) findViewById(R.id.FIPC);
        FSPC= (TextView) findViewById(R.id.FSPC);

        snapshotReport = (RelativeLayout) findViewById(R.id.snapshotReport);


        myCalendar = Calendar.getInstance();

        btnPick.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(SnapshotIndicators.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(tvDate.getText()).equals("")){
                    GetSnapshotIndicators(String.valueOf(tvDate.getText()));
                }else{
                    Toast.makeText(getApplicationContext(), "Pick date then Get.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        tvDate.setText(String.valueOf(sdf.format(myCalendar.getTime())));
    }


    private void GetSnapshotIndicators(final String today){
        if(_General.isNetworkAvailable(this)){
            pd = ProgressDialog.show(SnapshotIndicators.this, "", getResources().getString(R.string.GetingSnapShotReport));
            try {
                new Thread() {
                    public void run() {
                        GetSnapshot(today);
                        pd.dismiss();
                    }
                }.start();

            }catch (Exception e){
                e.printStackTrace();
            }

        }


    }

    public void GetSnapshot(String today) {
        Global global = new Global();
        global = (Global) this.getApplicationContext();

        try {

            JSONObject snapshotObj = new JSONObject();
            snapshotObj.put("SnapshotDate", today);


            ToRestApi rest = new ToRestApi();
            HttpResponse response = rest.postToRestApiToken(snapshotObj,"report/indicators/snapshot");

            HttpEntity entity = response.getEntity();
            snapshot = EntityUtils.toString(entity);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(snapshot.length() != 0 || snapshot != null){
                        try {
                            // TODO: Check if iteration works better
                            JSONObject ob = new JSONObject(snapshot);
                            FAPC.setText(ob.getString("active"));
                            FEPC.setText(ob.getString("expired"));
                            FIPC.setText(ob.getString("idle"));
                            FSPC.setText(ob.getString("suspended"));

                            snapshotReport.setVisibility(View.VISIBLE);
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "No data found.", Toast.LENGTH_SHORT);
                        }

                    }else{
                        Toast.makeText(getApplicationContext(), "No data found.", Toast.LENGTH_SHORT);
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                super.onOptionsItemSelected(item);
        }
        return false;

    }


}

