package org.openimis.imispolicies;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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

import android.app.ProgressDialog;
import android.widget.Toast;

public class NotEnrolledPoliciesOverview extends AppCompatActivity {
    private Global global;
    JSONArray policy;
    ClientAndroidInterface clientAndroidInterface;
    RecyclerView PolicyRecyclerView;
    NotEnrolledPoliciesOverviewAdapter notEnrolledPoliciesOverviewAdapter;

    ToRestApi toRestApi;
    Token tokenl;
    ProgressDialog pd;

    TextView ValueNumberOfPolices;
    TextView ValueAmountOfContribution;
    TextView NothingFound;

    int SmsRequired = 0;

    public static int search_count = 0;
    public static List<String> num = new ArrayList<>();
    public static JSONArray paymentDetails = new JSONArray();
    public static int PolicyValueToSend = 0;
    public static JSONObject getControlNumber = new JSONObject();

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd");
    Calendar cal = Calendar.getInstance();
    String dt2 = format2.format(cal.getTime());
    private String AmountCalculated;
    private String amountConfirmed;
    private String PaymentType = "";

    String InsuranceNumber = "";
    String InsuranceProduct = "";
    String RadioRenewal = "";
    static String PayType = "";

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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
        setContentView(R.layout.not_enrolled_policies_overview);
        global = (Global) getApplicationContext();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.not_enrolled_policies));
        }

        tokenl = new Token();
        toRestApi = new ToRestApi();

        ValueNumberOfPolices = findViewById(R.id.numOfFoundPoliciesValue);
        ValueAmountOfContribution = findViewById(R.id.amountOfContributionsValue);

        NothingFound = findViewById(R.id.noPoliciesFound);

        final String[] n = {""};
        Button requestButton = findViewById(R.id.requestControlNumberButton);
        requestButton.setOnClickListener(view -> {

            if (global.isNetworkAvailable()) {
                if (tokenl.getTokenText() == null || tokenl.getTokenText().length() <= 0) {
                    LoginDialogBox();
                } else {
                    Global global = (Global) getApplicationContext();

                    try {
                        getControlNumber.put("phone_number", "");
                        getControlNumber.put("request_date", dt2);
                        getControlNumber.put("enrolment_officer_code", global.getOfficerCode());
                        getControlNumber.put("policies", paymentDetails);
                        getControlNumber.put("amount_to_be_paid", PolicyValueToSend);

                        n[0] = "";
                        for (int i = 0; i < num.size(); i++) {
                            n[0] += num.get(i) + "\n";
                        }
                        AmountCalculated = String.valueOf(PolicyValueToSend);
                        if (num.size() != 0) {
                            trackBox(getControlNumber, String.valueOf(PolicyValueToSend));
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
                Snackbar.make(view1, getResources().getString(R.string.NoInternet), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button deleteButton = findViewById(R.id.deleteNotEnrolledPoliciesButton);
        deleteButton.setOnClickListener(view -> {
            if (paymentDetails.length() > 0) {
                int unDeletedPoliciesCount = 0;
                int totalPolicies = paymentDetails.length();

                for (int i = 0; i < paymentDetails.length(); i++) {
                    try {
                        JSONObject payment = paymentDetails.getJSONObject(0);
                        String policyid = payment.getString("PolicyId");
                        String uploaded_date = payment.getString("uploaded_date");
                        if (uploaded_date.equals("")) {
                            unDeletedPoliciesCount++;
                        } else {
                            clientAndroidInterface.deleteRecodedPolicy(policyid);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (unDeletedPoliciesCount > 0) {
                    String sms;
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
                    View view12 = findViewById(R.id.actv);
                    Snackbar.make(view12, getResources().getString(R.string.no_data_delete), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                });
            }

        });

        Button selectAllButton = findViewById(R.id.selectAllButton);
        // OTC-316 temporary hide button 'select all'
        selectAllButton.setVisibility(View.INVISIBLE);
        selectAllButton.setOnClickListener((view) -> {
            RecyclerView policiesList = findViewById(R.id.listOfNotEnrolledPolicies);
            NotEnrolledPoliciesOverviewAdapter adapter = (NotEnrolledPoliciesOverviewAdapter) policiesList.getAdapter();
            if (adapter != null) {
                adapter.selectAll();
            }
        });

        clientAndroidInterface = new ClientAndroidInterface(this);

        InsuranceNumber = getIntent().getStringExtra("INSURANCE_NUMBER");
        InsuranceProduct = getIntent().getStringExtra("INSURANCE_PRODUCT");
        RadioRenewal = getIntent().getStringExtra("RENEWAL");
        fillRecordedPolicies();

        int PolicyValue = 0;
        JSONObject ob;
        if (policy != null) {
            for (int j = 0; j < policy.length(); j++) {
                try {
                    ob = policy.getJSONObject(j);
                    PolicyValue += Integer.parseInt(ob.getString("PolicyValue"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            search_count = notEnrolledPoliciesOverviewAdapter.getCount();
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

    public void updateSelectAllButton(boolean isAllChecked) {
        Button selectAllButton = findViewById(R.id.selectAllButton);
        if (!isAllChecked) {
            selectAllButton.setText(getResources().getString(R.string.SelectAllButton));
        } else {
            selectAllButton.setText(getResources().getString(R.string.DeselectAllButton));
        }
    }

    public void fillRecordedPolicies() {
        policy = clientAndroidInterface.getNotEnrolledPolicies(InsuranceNumber, InsuranceProduct, RadioRenewal);//OrderArray;
        LayoutInflater li = LayoutInflater.from(NotEnrolledPoliciesOverview.this);
        li.inflate(R.layout.activity_over_view_policies, null);
        PolicyRecyclerView = findViewById(R.id.listOfNotEnrolledPolicies);
        notEnrolledPoliciesOverviewAdapter = new NotEnrolledPoliciesOverviewAdapter(this, policy);
        PolicyRecyclerView.setAdapter(notEnrolledPoliciesOverviewAdapter);
        PolicyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //PolicyRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    public void trackBox(final JSONObject policies, String Number) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.controls, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText finalAmount = promptsView.findViewById(R.id.display);
        final EditText phoneNumber = promptsView.findViewById(R.id.phonenumber);
        final Spinner paymentType = promptsView.findViewById(R.id.payment_type2);
        final CheckBox sendSms = promptsView.findViewById(R.id.send_sms);

        sendSms.setOnClickListener(view -> {
            if (((CompoundButton) view).isChecked()) {
                SmsRequired = 1;
            } else {
                SmsRequired = 0;
            }
        });

        addItemsOnSpinner2(paymentType);
        addListenerOnSpinnerItemSelection(paymentType);

        finalAmount.setText(Number);
        if (clientAndroidInterface.getSpecificControl("TotalAmount").equals("R")) {
            finalAmount.setEnabled(false);
        }

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Get_Control_Number),
                        (dialog, id) -> {
                            try {
                                policies.put("phone_number", phoneNumber.getText().toString());
                                policies.put("amount_to_be_paid", finalAmount.getText().toString());
                                policies.put("SmsRequired", SmsRequired);

                                if (PayType.equals("Mobile Phone")) {
                                    policies.put("type_of_payment", "MobilePhone");
                                } else if (PayType.equals("Bank Transfer")) {
                                    policies.put("type_of_payment", "BankTransfer");
                                } else {
                                    policies.put("type_of_payment", "");
                                }
                                amountConfirmed = finalAmount.getText().toString();
                                PaymentType = PayType;

                                if (SmsRequired == 1 && phoneNumber.getText().toString().equals("")) {
                                    clientAndroidInterface.ShowDialog(getResources().getString(R.string.phone_number_not_provided));
                                } else {
                                    if (!clientAndroidInterface.isProductsListUnique(policies)) {
                                        policyDeleteDialogReport(getResources().getString(R.string.not_unique_products));
                                    } else {
                                        getControlNumber(policies);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.button_cancel),
                        (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void addItemsOnSpinner2(Spinner PaymentSpinner) {
        List<String> list = new ArrayList<>();
        list.add("Mobile Phone");
        list.add("Bank Transfer");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        PaymentSpinner.setAdapter(dataAdapter);
    }

    public void addListenerOnSpinnerItemSelection(Spinner PaymentSpinner) {
        PaymentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PayType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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

    public void LoginDialogBox() {
        Global global = (Global) getApplicationContext();
        if (!clientAndroidInterface.CheckInternetAvailable()) {
            return;
        }

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
                                pd = ProgressDialog.show(NotEnrolledPoliciesOverview.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                new Thread() {
                                    public void run() {
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
                                                    Toast.makeText(NotEnrolledPoliciesOverview.this, NotEnrolledPoliciesOverview.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                } else {
                                                    pd.dismiss();
                                                    Toast.makeText(NotEnrolledPoliciesOverview.this, NotEnrolledPoliciesOverview.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                    LoginDialogBox();
                                                }
                                            });
                                        } else {
                                            runOnUiThread(() -> {
                                                pd.dismiss();
                                                Toast.makeText(NotEnrolledPoliciesOverview.this, NotEnrolledPoliciesOverview.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                LoginDialogBox();
                                            });
                                        }
                                    }
                                }.start();
                            } else {
                                LoginDialogBox();
                                Toast.makeText(NotEnrolledPoliciesOverview.this, NotEnrolledPoliciesOverview.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void getControlNumber(final JSONObject order) {
        pd = ProgressDialog.show(this, "", getResources().getString(R.string.Get_Control_Number));
        ControlNumberService.requestControlNumber(this, order, paymentDetails);
    }

    private void handleRequestResult(Intent intent) {
        if (intent.getAction().equals(ControlNumberService.ACTION_REQUEST_SUCCESS)) {
            policyDeleteDialogReport(getResources().getString(R.string.requestSent));
        }
        else if (intent.getAction().equals(ControlNumberService.ACTION_REQUEST_ERROR)) {
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

    public void refresh()
    {
        finish();
        startActivity(getIntent());
    }
}
