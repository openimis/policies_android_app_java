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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import org.openimis.imispolicies.domain.entity.Report;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.usecase.FetchReportEnrolment;
import org.openimis.imispolicies.usecase.FetchReportFeedback;
import org.openimis.imispolicies.usecase.FetchReportRenewal;
import org.openimis.imispolicies.util.AndroidUtils;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Statistics extends AppCompatActivity {

    private static final String TITLE_EXTRA = "Title";
    private static final String TYPE_EXTRA = "Type";

    @NonNull
    public static Intent newIntent(@NonNull Context context, @Nullable String title, @NonNull Type type) {
        Intent intent = new Intent(context, Statistics.class);
        intent.putExtra(TITLE_EXTRA, title);
        intent.putExtra(TYPE_EXTRA, type);
        return intent;
    }

    private EditText etFromDate;
    private EditText etToDate;
    private ListView lvStats;
    private ProgressDialog pd;
    private Type caller;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        caller = (Type) intent.getSerializableExtra(TYPE_EXTRA);
        if (intent.getStringExtra(TITLE_EXTRA) != null) {
            setTitle(intent.getStringExtra(TITLE_EXTRA));
        }

        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        lvStats = findViewById(R.id.lvStats);

        etFromDate.setOnClickListener((view) ->
                getDateDialog(
                        FromDatePickerListener,
                        null,
                        getDateOrNull(etToDate)
                ).show()
        );
        etToDate.setOnClickListener((view) ->
                getDateDialog(
                        ToDatePickerListener,
                        getDateOrNull(etFromDate),
                        null
                ).show()
        );
    }


    private final DatePickerDialog.OnDateSetListener FromDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            etFromDate.setText(new StringBuilder().append(day).append("/").append(month + 1).append("/").append(year));

            if (etToDate.getText().length() == 0) {
                etToDate.setText(etFromDate.getText().toString());
            }
        }
    };

    private final DatePickerDialog.OnDateSetListener ToDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            etToDate.setText(new StringBuilder().append(day).append("/").append(month + 1).append("/").append(year));
        }
    };

    @NonNull
    private DatePickerDialog getDateDialog(
            @NonNull DatePickerDialog.OnDateSetListener listener,
            @Nullable Date minDate,
            @Nullable Date maxDate
    ) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                listener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        DatePicker picker = dialog.getDatePicker();
        if (minDate != null) {
            picker.setMinDate(minDate.getTime());
        }
        if (maxDate != null) {
            picker.setMaxDate(maxDate.getTime());
        }
        return dialog;
    }


    private boolean isValidData() {
        if (etFromDate.getText().length() == 0) {
            AndroidUtils.showDialog(this, R.string.MissingStartDate);
            return false;
        }
        if (etToDate.getText().length() == 0) {
            AndroidUtils.showDialog(this, R.string.MissingEndDate);
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
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.mnuGetStats) {
            if (!isValidData()) {
                return false;
            }
            pd = AndroidUtils.showProgressDialog(this, 0, R.string.InProgress);
            new Thread() {
                public void run() {
                    if (caller != Type.ENROLMENT) {
                        GetStatistics(caller);
                    } else {
                        GetEnrolmentStats();
                    }
                    pd.dismiss();
                }
            }.start();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @WorkerThread
    private void GetStatistics(@Nullable Type caller) {
        Date fromDate = getDate(etFromDate);
        Date toDate = getDate(etToDate);
        Report report;
        try {
            if (Type.FEEDBACK.equals(caller)) {
                report = new FetchReportFeedback().execute(fromDate, toDate);
            } else if (Type.RENEWAL.equals(caller)) {
                report = new FetchReportRenewal().execute(fromDate, toDate);
            } else {
                throw new IllegalArgumentException("Caller '" + caller + "' is not valid");
            }
        } catch (Exception e) {
            if (e instanceof HttpException && ((HttpException) e).getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                runOnUiThread(() -> {
                    new ClientAndroidInterface(this).showLoginDialogBox(() -> GetStatistics(caller), null);
                });
                return;
            }
            e.printStackTrace();
            AndroidUtils.showDialog(this, R.string.NoStatFound);
            return;
        }
        runOnUiThread(() -> {
            List<Map<String, String>> feedbackStats = new ArrayList<>();
            Map<String, String> data = new HashMap<>();
            data.put("Label", "Total Sent");
            data.put("Value", String.valueOf(report.getSent()));
            feedbackStats.add(data);

            data = new HashMap<>();
            data.put("Label", "Accepted");
            data.put("Value", String.valueOf(report.getAccepted()));
            feedbackStats.add(data);

            ListAdapter adapter = new SimpleAdapter(Statistics.this,
                    feedbackStats,
                    R.layout.lvstats,
                    new String[]{"Label", "Value"},
                    new int[]{R.id.tvStatsLabel, R.id.tvStats}
            );

            lvStats.setAdapter(adapter);
        });
    }

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    @Nullable
    private Date getDateOrNull(@NonNull TextView tv) {
        Date date;
        try {
            date = dateFormat.parse(String.valueOf(tv.getText()));
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }
        return date;
    }

    @NonNull
    private Date getDate(@NonNull TextView tv) {
        Date date = getDateOrNull(tv);
        return date == null ? new Date() : date;
    }

    private void GetEnrolmentStats() {
        Report.Enrolment report;
        try {
            report = new FetchReportEnrolment().execute(getDate(etFromDate), getDate(etToDate));
        } catch (Exception e) {
            if (e instanceof HttpException && ((HttpException) e).getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                runOnUiThread(() -> {
                    new ClientAndroidInterface(this).showLoginDialogBox(() -> GetStatistics(caller), null);
                });
                return;
            }
            AndroidUtils.showDialog(this, R.string.NoStatFound);
            return;
        }

        runOnUiThread(() -> {
            List<Map<String, String>> EnrolmentStats = new ArrayList<>();
            Map<String, String> data = new HashMap<>();
            data.put("Label", "Total Submitted");
            data.put("Value", String.valueOf(report.getSubmitted()));
            EnrolmentStats.add(data);

            data = new HashMap<>();
            data.put("Label", "Assigned");
            data.put("Value", String.valueOf(report.getAssigned()));
            EnrolmentStats.add(data);

            ListAdapter adapter = new SimpleAdapter(Statistics.this,
                    EnrolmentStats,
                    R.layout.lvstats,
                    new String[]{"Label", "Value"},
                    new int[]{R.id.tvStatsLabel, R.id.tvStats}
            );

            lvStats.setAdapter(adapter);
        });
    }

    public enum Type {
        FEEDBACK, RENEWAL, ENROLMENT
    }
}
