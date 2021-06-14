package org.openimis.imispolicies;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.exact.general.General;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CummulativeIndicators extends AppCompatActivity {

    private General _General = new General(AppInformation.DomainInfo.getDomain());
    private ClientAndroidInterface ca;

    Boolean ClickedFrom = false;
    Boolean ClickedTo = false;

    Calendar myCalendar;

    Button DateFrom;
    Button DateTo;
    Button btnGet;

    TextView NPC;
    TextView RPC;
    TextView EPC;
    TextView SPC;
    TextView CCC;

    private ProgressDialog pd;
    RelativeLayout CommulativeReport;

    String cumulative = null;

    DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, monthOfYear);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateLabel();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cummulative_indicators);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.CummulativeIndicators));
        }

        ca = new ClientAndroidInterface(this);

        DateFrom = findViewById(R.id.DateFrom);
        DateTo = findViewById(R.id.DateTo);
        btnGet = findViewById(R.id.btnGet);

        NPC = findViewById(R.id.NPC);
        RPC = findViewById(R.id.RPC);
        EPC = findViewById(R.id.EPC);
        SPC = findViewById(R.id.SPC);
        CCC = findViewById(R.id.CCC);

        CommulativeReport = findViewById(R.id.CommulativeReport);

        myCalendar = Calendar.getInstance();

        DateFrom.setOnClickListener((view) -> {
            ClickedTo = false;
            ClickedFrom = true;
            new DatePickerDialog(CummulativeIndicators.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        DateTo.setOnClickListener((view) -> {
            ClickedFrom = false;
            ClickedTo = true;
            new DatePickerDialog(CummulativeIndicators.this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnGet.setOnClickListener((view) -> {
            if (!DateFrom.getText().equals("Date From") && !DateTo.getText().equals("Date To")) {
                GetCommulativeIndicators(String.valueOf(DateFrom.getText()), String.valueOf(DateTo.getText()));
            } else {
                showToast(R.string.pick_date,Toast.LENGTH_LONG);
            }
        });
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        if (ClickedFrom) {
            DateFrom.setText(sdf.format(myCalendar.getTime()));
        } else {
            DateTo.setText(sdf.format(myCalendar.getTime()));
        }
    }

    public void GetCommulativeIndicators(final String DateFrom, final String DateTo) {
        if (_General.isNetworkAvailable(this)) {
            pd = ProgressDialog.show(CummulativeIndicators.this, "", getResources().getString(R.string.GetingCummulativeReport));
            try {
                new Thread(() -> {
                    getCommulativeIndicators(DateFrom, DateTo);
                    runOnUiThread(this::showCumulativeIndicators);
                    pd.dismiss();
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (!ca.CheckInternetAvailable())
                return;
        }
    }

    public void getCommulativeIndicators(String DateFrom, String DateTo) {
        JSONObject cumulativeObj = new JSONObject();
        try {
            cumulativeObj.put("FromDate", DateFrom);
            cumulativeObj.put("ToDate", DateTo);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        ToRestApi rest = new ToRestApi();
        HttpResponse response = rest.postToRestApiToken(cumulativeObj, "report/indicators/cumulative");

        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                HttpEntity entity = response.getEntity();
                cumulative = EntityUtils.toString(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            switch (response.getStatusLine().getStatusCode()) {
                case 401:
                    showToast(R.string.LoginFail, Toast.LENGTH_LONG);
                    break;
                case 500:
                    showToast(R.string.SomethingWrongServer, Toast.LENGTH_LONG);
                    break;
                default:
                    showToast(R.string.ErrorOccurred, Toast.LENGTH_LONG);
                    break;
            }
        }
    }

    public void showCumulativeIndicators() {
        if (cumulative != null && cumulative.length() > 0) {
            try {
                JSONObject ob = new JSONObject(cumulative);

                NPC.setText(ob.getString("newPolicies"));
                RPC.setText(ob.getString("renewedPolicies"));
                EPC.setText(ob.getString("expiredPolicies"));
                SPC.setText(ob.getString("suspendedPolicies"));
                CCC.setText(ob.getString("collectedContribution"));

                CommulativeReport.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.ErrorOccurred, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.NoData, Toast.LENGTH_LONG).show();
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
