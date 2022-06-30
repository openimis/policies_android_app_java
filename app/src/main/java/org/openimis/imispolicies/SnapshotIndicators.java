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
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;

import android.widget.Button;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SnapshotIndicators extends AppCompatActivity {
    private Global global;
    private ProgressDialog pd;

    private Calendar myCalendar;

    private Button btnPick;
    private Button btnGet;

    private TextView tvDate;
    private TextView FAPC;
    private TextView FEPC;
    private TextView FIPC;
    private TextView FSPC;

    private RelativeLayout snapshotReport;

    private String snapshot = null;

    private DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateLabel();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapshot_indicators);

        global = (Global) getApplicationContext();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.SnapshotIndicators));
        }

        btnPick = findViewById(R.id.btnPick);
        btnGet = findViewById(R.id.btnGet);

        tvDate = findViewById(R.id.tvDate);

        FAPC = findViewById(R.id.FAPC);
        FEPC = findViewById(R.id.FEPC);
        FIPC = findViewById(R.id.FIPC);
        FSPC = findViewById(R.id.FSPC);

        snapshotReport = findViewById(R.id.snapshotReport);


        myCalendar = Calendar.getInstance();

        btnPick.setOnClickListener((view) -> new DatePickerDialog(SnapshotIndicators.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        btnGet.setOnClickListener((view) -> {
            if (!(tvDate.getText()).equals("")) {
                GetSnapshotIndicators(String.valueOf(tvDate.getText()));
            } else {
                showToast(R.string.pick_date, Toast.LENGTH_LONG);
            }
        });
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        tvDate.setText(sdf.format(myCalendar.getTime()));
    }


    private void GetSnapshotIndicators(final String today) {
        if (global.isNetworkAvailable()) {
            pd = ProgressDialog.show(SnapshotIndicators.this, "", getResources().getString(R.string.GetingSnapShotReport));
            try {
                new Thread(() -> {
                    GetSnapshot(today);
                    runOnUiThread(this::showSnapshot);
                    pd.dismiss();
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void GetSnapshot(String today) {
        JSONObject snapshotObj = new JSONObject();
        try {
            snapshotObj.put("SnapshotDate", today);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        ToRestApi rest = new ToRestApi();
        HttpResponse response = rest.postToRestApiToken(snapshotObj, "report/indicators/snapshot");

        if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
            try {
                HttpEntity entity = response.getEntity();
                snapshot = EntityUtils.toString(entity);
            } catch (IOException e) {
                showToast(R.string.ErrorOccurred, Toast.LENGTH_LONG);
                e.printStackTrace();
            }
        } else {
            switch (response.getStatusLine().getStatusCode()) {
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    showToast(R.string.LoginFail, Toast.LENGTH_LONG);
                    break;
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    showToast(R.string.SomethingWrongServer, Toast.LENGTH_LONG);
                    break;
                default:
                    showToast(R.string.ErrorOccurred, Toast.LENGTH_LONG);
                    break;
            }
        }
    }

    public void showSnapshot() {
        if (snapshot != null && snapshot.length() > 0) {
            try {
                JSONObject ob = new JSONObject(snapshot);
                FAPC.setText(ob.getString("active"));
                FEPC.setText(ob.getString("expired"));
                FIPC.setText(ob.getString("idle"));
                FSPC.setText(ob.getString("suspended"));

                snapshotReport.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
                showToast(R.string.ErrorOccurred, Toast.LENGTH_LONG);
            }
        } else {
            showToast(R.string.NoDataAvailable, Toast.LENGTH_LONG);
        }
    }

    public void showToast(@StringRes int id, int length) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), id, length).show());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

