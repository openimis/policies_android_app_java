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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.text.DateFormat;

import com.exact.CallSoap.CallSoap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import cm.ynote.educash.R;

public class Statistics extends AppCompatActivity {
    private EditText etFromDate;
    private EditText etToDate;
    private ListView lvStats;
    private ProgressDialog pd;
    private int year;
    private int month;
    private int day;
    private static final int FromDate_Dialog_ID = 0;
    private static final int ToDate_Dialog_ID = 1;
    private final Calendar cal = Calendar.getInstance();
    private ArrayList<HashMap<String, String>> FeedbackStats = new ArrayList<>();
    ArrayList<HashMap<String,String>> EnrolmentStats = new ArrayList<HashMap<String, String>>();
    private String OfficerCode;
    private String Caller;
    private Global global;
    public static boolean IsEnrolment = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);
        global = (Global) getApplicationContext();

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            @SuppressWarnings("ConstantConditions") String Title = intent.getExtras().get("Title").toString();
            //noinspection ConstantConditions
            Caller = intent.getExtras().get("Caller").toString();
            setTitle(Title);
        }

        OfficerCode = global.getOfficerCode();

        etFromDate = (EditText) findViewById(R.id.etFromDate);
        etToDate = (EditText) findViewById(R.id.etToDate);
        lvStats = (ListView) findViewById(R.id.lvStats);

        etFromDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //noinspection deprecation
                showDialog(FromDate_Dialog_ID);
                return false;
            }
        });

        etToDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //noinspection deprecation
                showDialog(ToDate_Dialog_ID);
                return false;
            }
        });

    }


    private DatePickerDialog.OnDateSetListener FromDatePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int Selectedyear, int SelectedMonth, int SelectedDay) {
            year = Selectedyear;
            month = SelectedMonth;
            day = SelectedDay;

            etFromDate.setText(new StringBuilder().append(day).append("/").append(month + 1).append("/").append(year));

            if (etToDate.getText().length() == 0) {
                etToDate.setText(etFromDate.getText().toString());
            }
        }
    };


    private DatePickerDialog.OnDateSetListener ToDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int SelectedYear, int SelectedMonth, int SelectedDay) {
            year = SelectedYear;
            month = SelectedMonth;
            day = SelectedDay;

            etToDate.setText(new StringBuilder().append(day).append("/").append(month + 1).append("/").append(year));


        }
    };


    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case FromDate_Dialog_ID:
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DATE);

                return new DatePickerDialog(this, FromDatePickerListener, year, month, day);

            case ToDate_Dialog_ID:
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DATE);

                return new DatePickerDialog(this, ToDatePickerListener, year, month, day);

        }
        return null;
    }


    private boolean isValidData() {
        if (etFromDate.getText().length() == 0) {
            ShowDialog(getResources().getString(R.string.MissingStartDate));
            return false;
        }
        if (etToDate.getText().length() == 0) {
            ShowDialog(getResources().getString(R.string.MissingEndDate));
            return false;

        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.mnuGetStats:

                if (!isValidData()) return false;


                pd = new ProgressDialog(this);
                pd.setCancelable(true);
                pd = ProgressDialog.show(this, "", getResources().getString(R.string.InProgress));

                new Thread() {
                    public void run() {
                        if(!IsEnrolment)
                        GetStatistics();
                        else
                            GetEnrolmentStats();
                        pd.dismiss();
                    }

                }.start();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void GetStatistics() {
        FeedbackStats = new ArrayList<>();

        CallSoap cs = new CallSoap();

        Date FromDate, ToDate;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        FromDate = new Date();
        ToDate = new Date();
        try {
            FromDate = dateFormat.parse(String.valueOf(etFromDate.getText()));
            ToDate = dateFormat.parse(String.valueOf(etToDate.getText()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String stats = "";

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        JSONObject objStats = new JSONObject();
        try {
            objStats.put("fromDate", formatter.format(FromDate));
            objStats.put("toDate", formatter.format(ToDate));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ToRestApi rest = new ToRestApi();

        if (Caller.equals("F")) {
            HttpResponse response = rest.postToRestApiToken(objStats,"report/feedback");
            try {
                stats = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {}

        } else if (Caller.equals("R")) {
            HttpResponse response = rest.postToRestApiToken(objStats,"report/renewal");
            try {
                stats = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {}
        }

        final String finalStats = stats;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                try {
                    JSONObject statsObj = new JSONObject(finalStats);
                    if (statsObj.length() == 0) {
                        ShowDialog(getResources().getString(R.string.NoStatFound));
                    } else {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("Label", "Total Sent");
                        if (Caller.equals("F"))
                            data.put("Value", String.valueOf(statsObj.get("feedbackSent")));
                        else if (Caller.equals("R"))
                            data.put("Value", String.valueOf(statsObj.get("renewalSent")));
                        FeedbackStats.add(data);

                        data = new HashMap<>();
                        data.put("Label", "Accepted");
                        if (Caller.equals("F"))
                            data.put("Value", String.valueOf(statsObj.get("feedbackAccepted")));
                        else if (Caller.equals("R"))
                            data.put("Value", String.valueOf(statsObj.get("renewalAccepted")));
                        FeedbackStats.add(data);

                        ListAdapter adapter = new SimpleAdapter(Statistics.this,
                                FeedbackStats,
                                R.layout.lvstats,
                                new String[]{"Label", "Value"},
                                new int[]{R.id.tvStatsLabel, R.id.tvStats}
                        );

                        lvStats.setAdapter(adapter);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


    }
    private void GetEnrolmentStats(){
        EnrolmentStats = new ArrayList<HashMap<String, String>>();

        Date FromDate, ToDate;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        FromDate = new Date();
        ToDate = new Date();
        try {
            FromDate = dateFormat.parse(String.valueOf(etFromDate.getText()));
            ToDate = dateFormat.parse(String.valueOf(etToDate.getText()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        JSONObject objEnrolment = new JSONObject();
        try {
            objEnrolment.put("fromDate", formatter.format(FromDate));
            objEnrolment.put("toDate", formatter.format(ToDate));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ToRestApi rest = new ToRestApi();
        HttpResponse response = rest.postToRestApiToken(objEnrolment,"report/enrolment");

        HttpEntity entity = response.getEntity();
        String entityString = "";
        JSONObject enrolmentStatsObj = new JSONObject();

        try {
            entityString = EntityUtils.toString(entity);
            enrolmentStatsObj = new JSONObject(entityString);
        } catch (Exception e) {}

        final JSONObject finalEnrolmentStatsObj = enrolmentStatsObj;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(finalEnrolmentStatsObj.length() == 0){
                        ShowDialog(getResources().getString(R.string.NoStatFound));
                    }else{
                        HashMap<String,String> data = new HashMap<String, String>();
                        data.put("Label", "Total Submitted");
                        data.put("Value", String.valueOf(finalEnrolmentStatsObj.get("totalSubmitted")));
                        EnrolmentStats.add(data);

                        data = new HashMap<String, String>();
                        data.put("Label", "Assigned");
                        data.put("Value", String.valueOf(finalEnrolmentStatsObj.get("totalAssigned")));
                        EnrolmentStats.add(data);

                        ListAdapter adapter = new SimpleAdapter(Statistics.this,
                                EnrolmentStats,
                                R.layout.lvstats,
                                new String[]{"Label","Value"},
                                new int[]{R.id.tvStatsLabel,R.id.tvStats}
                        );

                        lvStats.setAdapter(adapter);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private AlertDialog ShowDialog(String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }


}
