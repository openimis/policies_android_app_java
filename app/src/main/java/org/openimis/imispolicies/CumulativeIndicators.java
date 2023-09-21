package org.openimis.imispolicies;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.openimis.imispolicies.domain.entity.CumulativePolicies;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.usecase.FetchCumulativePolicies;
import org.openimis.imispolicies.util.AndroidUtils;
import org.openimis.imispolicies.util.TextViewUtils;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Calendar;

import kotlin.jvm.functions.Function1;

public class CumulativeIndicators extends AppCompatActivity {

    private static final String FROM = "from";
    private static final String TO = "to";
    private View cumulativeReport;
    private TextView NPC;
    private TextView RPC;
    private TextView EPC;
    private TextView SPC;
    private TextView CCC;

    @Nullable
    private Calendar from = null;
    @Nullable
    private Calendar to = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cumulative_indicators);


        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.CumulativeIndicators));
        }

        TextView dateFrom = findViewById(R.id.DateFrom);
        TextView dateTo = findViewById(R.id.DateTo);

        if (savedInstanceState != null) {
            long fromLong = savedInstanceState.getLong(FROM);
            if (fromLong != -1) {
                from = Calendar.getInstance();
                from.setTimeInMillis(fromLong);
                TextViewUtils.setDate(dateFrom, from.getTime());
            }
            long toLong = savedInstanceState.getLong(TO);
            if (toLong != -1) {
                to = Calendar.getInstance();
                to.setTimeInMillis(toLong);
                TextViewUtils.setDate(dateTo, to.getTime());
            }
        }

        NPC = findViewById(R.id.NPC);
        RPC = findViewById(R.id.RPC);
        EPC = findViewById(R.id.EPC);
        SPC = findViewById(R.id.SPC);
        CCC = findViewById(R.id.CCC);

        cumulativeReport = findViewById(R.id.CumulativeReport);

        dateFrom.setOnClickListener((view) -> showDatePickerDialog(dateFrom, from, (calendar) -> {
            from = calendar;
            return null;
        }));

        dateTo.setOnClickListener((view) -> showDatePickerDialog(dateTo, to, (calendar) -> {
            to = calendar;
            return null;
        }));

        findViewById(R.id.btnGet).setOnClickListener((view) -> {
            if (!dateFrom.getText().equals("Date From") && !dateTo.getText().equals("Date To")) {
                new GetCumulativePoliciesAsync(this).execute(from, to);
            } else {
                showToast(R.string.pick_date, Toast.LENGTH_LONG);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(FROM, from != null ? from.getTimeInMillis() : -1);
        outState.putLong(TO, to != null ? to.getTimeInMillis() : -1);
    }

    private void showDatePickerDialog(
            @NonNull TextView tv,
            @Nullable Calendar date,
            @NonNull Function1<Calendar, Void> onDateSet
    ) {
        Calendar c = date;
        if (c == null) {
            c = Calendar.getInstance();
        }
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    TextViewUtils.setDate(tv, calendar.getTime());
                    onDateSet.invoke(calendar);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        DatePicker picker = dialog.getDatePicker();
        if (from != null) {
            picker.setMinDate(from.getTimeInMillis());
        } if (to != null) {
            picker.setMaxDate(to.getTimeInMillis());
        }
        dialog.show();
    }

    public void showCumulativeIndicators(@NonNull CumulativePolicies cumulative) {
        NPC.setText(String.valueOf(cumulative.getNewPolicies()));
        RPC.setText(String.valueOf(cumulative.getRenewedPolicies()));
        EPC.setText(String.valueOf(cumulative.getExpiredPolicies()));
        SPC.setText(String.valueOf(cumulative.getSuspendedPolicies()));
        CCC.setText(String.valueOf(cumulative.getCollectedContributions()));

        cumulativeReport.setVisibility(View.VISIBLE);
    }

    public void showToast(@StringRes int id, int length) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), id, length).show());
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
    private static class GetCumulativePoliciesAsync extends AsyncTask<Calendar, Void, CumulativePolicies> {
        @NonNull
        private final WeakReference<CumulativeIndicators> reference;
        private WeakReference<ProgressDialog> pd;

        GetCumulativePoliciesAsync(CumulativeIndicators activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            CumulativeIndicators activity = reference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.cumulativeReport.setVisibility(View.GONE);
            pd = new WeakReference<>(
                    AndroidUtils.showProgressDialog(activity, 0, R.string.GetingSnapShotReport)
            );
        }

        @Override
        protected CumulativePolicies doInBackground(Calendar... calendars) {
            try {
                return new FetchCumulativePolicies().execute(calendars[0].getTime(), calendars[1].getTime());
            } catch (Exception e) {
                e.printStackTrace();
                CumulativeIndicators activity = reference.get();
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
        protected void onPostExecute(@Nullable CumulativePolicies cumulativePolicies) {
            ProgressDialog progress = pd.get();
            if (progress != null) {
                progress.dismiss();
            }
            if (cumulativePolicies != null) {
                CumulativeIndicators activity = reference.get();
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                activity.showCumulativeIndicators(cumulativePolicies);
            }
        }
    }
}
