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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.openimis.imispolicies.domain.entity.SnapshotPolicies;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.usecase.FetchSnapshotPolicies;
import org.openimis.imispolicies.util.AndroidUtils;
import org.openimis.imispolicies.util.TextViewUtils;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Calendar;

public class SnapshotIndicators extends AppCompatActivity {

    private static final String DATE = "date";
    private static final String WAS_PICKED = "was_picked";

    @NonNull
    private final Calendar myCalendar = Calendar.getInstance();

    private TextView tvDate;
    private TextView FAPC;
    private TextView FEPC;
    private TextView FIPC;
    private TextView FSPC;
    private View snapshotReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapshot_indicators);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.SnapshotIndicators));
        }

        Button btnPick = findViewById(R.id.btnPick);
        Button btnGet = findViewById(R.id.btnGet);

        snapshotReport = findViewById(R.id.snapshotReport);
        tvDate = findViewById(R.id.tvDate);
        FAPC = findViewById(R.id.FAPC);
        FEPC = findViewById(R.id.FEPC);
        FIPC = findViewById(R.id.FIPC);
        FSPC = findViewById(R.id.FSPC);

        if (savedInstanceState != null) {
            myCalendar.setTimeInMillis(savedInstanceState.getLong(DATE));
            if (savedInstanceState.getBoolean(WAS_PICKED)) {
                TextViewUtils.setDate(tvDate, myCalendar.getTime());
            }
        }

        btnPick.setOnClickListener((view) ->
                new DatePickerDialog(
                        SnapshotIndicators.this,
                        (v, year, monthOfYear, dayOfMonth) -> {
                            myCalendar.set(Calendar.YEAR, year);
                            myCalendar.set(Calendar.MONTH, monthOfYear);
                            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            TextViewUtils.setDate(tvDate, myCalendar.getTime());
                        },
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)
                ).show());

        btnGet.setOnClickListener((view) -> {
            if (!(tvDate.getText()).equals("")) {
                GetSnapshotIndicators(myCalendar);
            } else {
                showToast(R.string.pick_date, Toast.LENGTH_LONG);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DATE, myCalendar.getTime().getTime());
        outState.putBoolean(WAS_PICKED, tvDate.getText().length() > 0);
    }

    @SuppressWarnings("deprecation")
    private void GetSnapshotIndicators(@NonNull final Calendar today) {
        if (((Global) getApplicationContext()).isNetworkAvailable()) {
            new GetSnapshotPoliciesAsync(this).execute(today);
        }
    }

    @MainThread
    public void showSnapshot(@NonNull SnapshotPolicies snapshot) {
        FAPC.setText(String.valueOf(snapshot.getActive()));
        FEPC.setText(String.valueOf(snapshot.getExpired()));
        FIPC.setText(String.valueOf(snapshot.getIdle()));
        FSPC.setText(String.valueOf(snapshot.getSuspended()));
        snapshotReport.setVisibility(View.VISIBLE);
    }

    public void showToast(@StringRes int id, int length) {
        Toast.makeText(getApplicationContext(), id, length).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    private static class GetSnapshotPoliciesAsync extends AsyncTask<Calendar, Void, SnapshotPolicies> {
        @NonNull
        private final WeakReference<SnapshotIndicators> reference;
        private WeakReference<ProgressDialog> pd;

        GetSnapshotPoliciesAsync(SnapshotIndicators activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            SnapshotIndicators activity = reference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.snapshotReport.setVisibility(View.GONE);
            pd = new WeakReference<>(
                    AndroidUtils.showProgressDialog(activity, 0, R.string.GetingSnapShotReport)
            );
        }

        @Override
        protected SnapshotPolicies doInBackground(Calendar... calendars) {
            try {
                return new FetchSnapshotPolicies().execute(calendars[0].getTime());
            } catch (Exception e) {
                e.printStackTrace();
                SnapshotIndicators activity = reference.get();
                if (activity == null || activity.isFinishing()) {
                    return null;
                }
                if (e instanceof HttpException) {
                    switch (((HttpException) e).getCode()) {
                        case HttpURLConnection.HTTP_UNAUTHORIZED:
                            activity.showToast(R.string.LoginFail, Toast.LENGTH_LONG);
                            break;
                        case HttpURLConnection.HTTP_INTERNAL_ERROR:
                            activity.showToast(R.string.SomethingWrongServer, Toast.LENGTH_LONG);
                            break;
                        default:
                            activity.showToast(R.string.ErrorOccurred, Toast.LENGTH_LONG);
                            break;
                    }
                }
                activity.showToast(R.string.ErrorOccurred, Toast.LENGTH_LONG);
            }
            return null;
        }

        @Override
        protected void onPostExecute(@Nullable SnapshotPolicies snapshotPolicies) {
            ProgressDialog progress = pd.get();
            if (progress != null) {
                progress.dismiss();
            }
            if (snapshotPolicies != null) {
                SnapshotIndicators activity = reference.get();
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                activity.showSnapshot(snapshotPolicies);
            }
        }
    }
}
