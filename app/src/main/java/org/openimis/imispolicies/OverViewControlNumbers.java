package org.openimis.imispolicies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OverViewControlNumbers extends AppCompatActivity {
    public SQLHandler sqlHandler;
    private Global global;

    private ToRestApi toRestApi;
    private Token tokenl;
    private ProgressDialog pd;

    private JSONArray policy;
    private ClientAndroidInterface clientAndroidInterface;
    private RecyclerView PolicyRecyclerView;
    private OverViewControlNumberAdapter overViewControlNumberAdapter;

    private TextView ValueNumberOfPolices;
    private TextView ValueAmountOfContribution;

    public static int search_count = 0;

    public static List<String> num = new ArrayList<>();

    public static JSONArray paymentDetails = new JSONArray();

    public static int PolicyValueToSend = 0;
    public static JSONObject getControlNumber = new JSONObject();
    private SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();

    private Calendar cal = Calendar.getInstance();
    private String dt = format.format(cal.getTime());
    private String AmountCalculated;
    private String amountConfirmed;
    private TextView NothingFound;

    private String InsuranceNumber = "";
    private String OtherNames = "";
    private String LastName = "";
    private String InsuranceProduct = "";
    private String UploadedFrom = "";
    private String UploadedTo = "";
    private String RequestedFrom = "";
    private String RequestedTo = "";
    private String RadioRenewal = "";
    private String RadioSms = "";
    private String PaymentType = "";

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleRequestResult(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ControlNumberService.ACTION_REQUEST_SUCCESS);
        intentFilter.addAction(ControlNumberService.ACTION_REQUEST_ERROR);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over_view_control_numbers);
        global = (Global) getApplicationContext();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.control_numbers));
        }

        toRestApi = new ToRestApi();
        tokenl = new Token();

        ValueNumberOfPolices = findViewById(R.id.ValueNumberOfPolices);
        ValueAmountOfContribution = findViewById(R.id.ValueAmountOfContribution);
        pd = new ProgressDialog(this);
        pd.dismiss();

        NothingFound = findViewById(R.id.NothingFound);

        final String[] n = {""};
        Button requestButton = findViewById(R.id.requestButton);
        requestButton.setOnClickListener(view -> {

            if (global.isNetworkAvailable()) {
                if (tokenl.getTokenText().length() <= 0) {
                    LoginDialogBox();
                } else {
                    try {
                        getControlNumber.put("internalIdentifier", PolicyValueToSend);

                        n[0] = "";
                        for (int i = 0; i < num.size(); i++) {
                            n[0] += num.get(i) + "\n";
                        }
                        AmountCalculated = String.valueOf(PolicyValueToSend);
                        if (num.size() != 0) {
                            trackBox(paymentDetails);
                        } else {
                            View view1 = findViewById(R.id.actv);
                            Snackbar.make(view1, getResources().getString(R.string.select_policy), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                View view1 = findViewById(R.id.actv);
                Snackbar.make(view1, "", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        });

        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(view -> {
            if (paymentDetails.length() > 0) {
                String unDeletedPolicies = "";
                int unDeletedPoliciesCount = 0;
                int totalPolicies = paymentDetails.length();
                for (int i = 0; i < paymentDetails.length(); i++) {
                    try {
                        JSONObject payment = paymentDetails.getJSONObject(0);
                        String policyid = payment.getString("PolicyId");
                        String uploaded_date = payment.getString("uploaded_date");

                        if (uploaded_date.equals("")) {
                            unDeletedPoliciesCount++;
                            unDeletedPolicies += policyid;
                        } else {
                            clientAndroidInterface.deleteRecodedPolicy(policyid);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (unDeletedPoliciesCount > 0) {
                    String sms = "";
                    if (totalPolicies == 1) {
                        sms = getResources().getString(R.string.cant_be_deleted);
                    } else {
                        sms = unDeletedPoliciesCount + " " + getResources().getString(R.string.of) + " " + totalPolicies + " " + getResources().getString(R.string.notUploaded);
                    }
                    num.clear();
                    policyDeleteDialogReport(sms);
                } else {
                    num.clear();
                    policyDeleteDialogReport(getResources().getString(R.string.dataDeleted));
                }
            } else {
                runOnUiThread(() -> {
                    pd.dismiss();
                    View view1 = findViewById(R.id.actv);
                    Snackbar.make(view1, getResources().getString(R.string.no_data_delete), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                });

            }

        });

        clientAndroidInterface = new ClientAndroidInterface(this);

        InsuranceNumber = getIntent().getStringExtra("INSURANCE_NUMBER");
        OtherNames = getIntent().getStringExtra("OTHER_NAMES");
        LastName = getIntent().getStringExtra("LAST_NAME");
        InsuranceProduct = getIntent().getStringExtra("INSURANCE_PRODUCT");
        UploadedFrom = getIntent().getStringExtra("UPLOADED_FROM");
        UploadedTo = getIntent().getStringExtra("UPLOADED_TO");
        UploadedTo = getIntent().getStringExtra("REQUESTED_FROM");
        UploadedTo = getIntent().getStringExtra("REQUESTED_TO");
        RadioRenewal = getIntent().getStringExtra("RENEWAL");
        RadioSms = getIntent().getStringExtra("SMS");
        PaymentType = getIntent().getStringExtra("PAYMENT_TYPE");

        fillRecordedPolicies();

        int PolicyValue = 0;
        JSONObject ob = null;
        if (policy != null) {
            for (int j = 0; j < policy.length(); j++) {
                try {
                    ob = policy.getJSONObject(j);
                    PolicyValue += Integer.parseInt(ob.getString("PolicyValue"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            search_count = overViewControlNumberAdapter.getCount();
            if (search_count == 0) {
                NothingFound.setVisibility(View.VISIBLE);
                requestButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }
            ValueNumberOfPolices.setText(String.valueOf(search_count));
            ValueAmountOfContribution.setText(String.valueOf(PolicyValue));
        } else {
            ValueNumberOfPolices.setText("0");
            ValueAmountOfContribution.setText("0");
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void policyDeleteDialogReport(String message) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.policy_delete_report_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView report_message = promptsView.findViewById(R.id.report_message);
        report_message.setText(message);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.button_ok),
                        (dialog, id) -> refresh());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void fillRecordedPolicies() {
        policy = clientAndroidInterface.getRecordedPolicies(InsuranceNumber, OtherNames, LastName, InsuranceProduct, UploadedFrom, UploadedTo, RadioRenewal, RequestedFrom, RequestedTo, PaymentType);//OrderArray;
        LayoutInflater li = LayoutInflater.from(OverViewControlNumbers.this);
        li.inflate(R.layout.activity_over_view_control_numbers, null);
        PolicyRecyclerView = findViewById(R.id.listofpolicies);
        overViewControlNumberAdapter = new OverViewControlNumberAdapter(this, policy);
        PolicyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //PolicyRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        PolicyRecyclerView.setAdapter(overViewControlNumberAdapter);
    }


    public void trackBox(final JSONArray policies) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.assigncontrolnumber, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Get Control Number", (dialog, id) -> {
                    if (clientAndroidInterface.isLoggedIn())
                        getControlNumber(policies);
                    else
                        LoginDialogBox();
                })
                .setNegativeButton("Cancel",
                        (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void LoginDialogBox() {
        if (!global.isNetworkAvailable()) {
            clientAndroidInterface.ShowDialog(getResources().getString(R.string.NoInternet));
            return;
        }

        Global global = (Global) OverViewControlNumbers.this.getApplicationContext();

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView username = promptsView.findViewById(R.id.UserName);
        final TextView password = promptsView.findViewById(R.id.Password);
        String officer_code = global.getOfficerCode();
        username.setText(String.valueOf(officer_code));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Ok),
                        (dialog, id) -> {
                            if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {
                                pd = ProgressDialog.show(OverViewControlNumbers.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                new Thread(() -> {
                                    JSONObject object = new JSONObject();
                                    try {
                                        object.put("userName", username.getText().toString());
                                        object.put("password", password.getText().toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    String functionName = "login";
                                    HttpResponse response = toRestApi.postToRestApi(object, functionName);

                                    String content = null;
                                    HttpEntity respEntity = response.getEntity();
                                    if (respEntity != null) {
                                        final String[] code = {null};
                                        // EntityUtils to get the response content

                                        try {
                                            content = EntityUtils.toString(respEntity);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                                        JSONObject ob = null;
                                        String token = "";
                                        String validTo = "";
                                        try {
                                            ob = new JSONObject(content);
                                            token = ob.getString("access_token");
                                            validTo = ob.getString("expires_on");

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        tokenl.saveTokenText(token, validTo);

                                        final String finalToken = token;
                                        runOnUiThread(() -> {
                                            if (finalToken.length() > 0) {
                                                pd.dismiss();
                                                Toast.makeText(OverViewControlNumbers.this, OverViewControlNumbers.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                            } else {
                                                pd.dismiss();
                                                Toast.makeText(OverViewControlNumbers.this, OverViewControlNumbers.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                LoginDialogBox();
                                            }
                                        });
                                    } else {
                                        runOnUiThread(() -> {
                                            pd.dismiss();
                                            Toast.makeText(OverViewControlNumbers.this, OverViewControlNumbers.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                            LoginDialogBox();
                                        });
                                    }
                                }
                                ).start();
                            } else {
                                LoginDialogBox();
                                Toast.makeText(OverViewControlNumbers.this, OverViewControlNumbers.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void getControlNumber(final JSONArray order) {
        pd = ProgressDialog.show(this, "", getResources().getString(R.string.Get_Control_Number));
        ControlNumberService.getAssignedControlNumber(this, order);
    }

    private void handleRequestResult(Intent intent) {
        if (intent.getAction().equals(ControlNumberService.ACTION_REQUEST_SUCCESS)) {
            policyDeleteDialogReport(getResources().getString(R.string.requestSent));
        } else if (intent.getAction().equals(ControlNumberService.ACTION_REQUEST_ERROR)) {
            String errorMessage = intent.getStringExtra(ControlNumberService.FIELD_ERROR_MESSAGE);
            showSnackbar(errorMessage);
        }
        pd.dismiss();
    }

    public void showSnackbar(String message) {
        View activity = findViewById(R.id.actv);
        Snackbar.make(activity, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void refresh() {
        finish();
        startActivity(getIntent());
    }
}
