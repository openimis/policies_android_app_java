package org.openimis.imispolicies;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.exact.general.General;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.openimis.imispolicies.R;

public class OverViewControlNumbers extends AppCompatActivity {
    public SQLHandler sqlHandler;

    ToRestApi toRestApi;
    Token tokenl;
    ProgressDialog pd;

    JSONArray policy;
    ClientAndroidInterface clientAndroidInterface;
    RecyclerView PolicyRecyclerView;
    OverViewControlNumberAdapter overViewControlNumberAdapter;

    TextView ValueNumberOfPolices;
    TextView ValueAmountOfContribution;

    public static int search_count = 0;

    public static List<String> num = new ArrayList<>();

    public static JSONArray paymentDetails = new JSONArray();

    public static int PolicyValueToSend = 0;
    public static JSONObject getControlNumber = new JSONObject();

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Calendar cal = Calendar.getInstance();
    String dt = format.format(cal.getTime());
    private String AmountCalculated;
    private String amountConfirmed;
    TextView NothingFound;

    String InsuranceNumber = "";
    String OtherNames = "";
    String LastName = "";
    String InsuranceProduct = "";
    String UploadedFrom = "";
    String UploadedTo = "";
    String RequestedFrom = "";
    String RequestedTo = "";
    String RadioRenewal = "";
    String RadioSms = "";
    String PaymentType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over_view_control_numbers);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.control_numbers));

        toRestApi = new ToRestApi();
        tokenl = new Token();

        ValueNumberOfPolices = (TextView) findViewById(R.id.ValueNumberOfPolices);
        ValueAmountOfContribution = (TextView) findViewById(R.id.ValueAmountOfContribution);
        pd = new ProgressDialog(this);
        pd.dismiss();

        NothingFound = (TextView) findViewById(R.id.NothingFound);

        final String[] n = {""};
        Button fab = (Button) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                General _general = new General(AppInformation.DomainInfo.getDomain());

                if (_general.isNetworkAvailable(OverViewControlNumbers.this)) {
                    if (tokenl.getTokenText().length() <= 0) {
                        LoginDialogBox();
                    } else {
                        Global global = new Global();
                        global = (Global) getApplicationContext();

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

            }
        });

        Button fab2 = (Button) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            View view = findViewById(R.id.actv);
                            Snackbar.make(view, getResources().getString(R.string.no_data_delete), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });

                }

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
                fab.setVisibility(View.GONE);
                fab2.setVisibility(View.GONE);
            }
            ValueNumberOfPolices.setText(String.valueOf(search_count));
            ValueAmountOfContribution.setText(String.valueOf(PolicyValue));
        } else {
            ValueNumberOfPolices.setText("0");
            ValueAmountOfContribution.setText("0");
        }

    }

/*    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //overViewPoliciesAdapter.getFilter().filter(s);
                return true;
            }
        });

        return true;
    }*/


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

        final TextView report_message = (TextView) promptsView.findViewById(R.id.report_message);
        report_message.setText(message);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.button_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                Intent intent = new Intent(OverViewControlNumbers.this, OverViewControlNumbers.class);
                                intent.putExtra("RENEWAL", RadioRenewal);
                                intent.putExtra("INSURANCE_NUMBER", InsuranceNumber);
                                intent.putExtra("OTHER_NAMES", OtherNames);
                                intent.putExtra("LAST_NAME", LastName);
                                intent.putExtra("INSURANCE_PRODUCT", InsuranceProduct);
                                intent.putExtra("UPLOADED_FROM", UploadedFrom);
                                intent.putExtra("UPLOADED_TO", UploadedTo);
                                intent.putExtra("REQUESTED_FROM", RequestedFrom);
                                intent.putExtra("REQUESTED_TO", RequestedTo);
                                intent.putExtra("PAYMENT_TYPE", PaymentType);
                                intent.putExtra("SMS", RadioSms);
                                startActivity(intent);
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void fillRecordedPolicies() {
        policy = clientAndroidInterface.getRecordedPolicies(InsuranceNumber, OtherNames, LastName, InsuranceProduct, UploadedFrom, UploadedTo, RadioRenewal, RequestedFrom, RequestedTo, PaymentType);//OrderArray;
        LayoutInflater li = LayoutInflater.from(OverViewControlNumbers.this);
        View promptsView = li.inflate(R.layout.activity_over_view_control_numbers, null);
        PolicyRecyclerView = (RecyclerView) findViewById(R.id.listofpolicies);
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
                .setPositiveButton("Get Control Number",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    getControlNumber(policies);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    
    public void LoginDialogBox() {

        //final int[] userid = {0};

        Global global = (Global) OverViewControlNumbers.this.getApplicationContext();

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView username = (TextView) promptsView.findViewById(R.id.UserName);
        final TextView password = (TextView) promptsView.findViewById(R.id.Password);
        String officer_code = global.getOfficerCode();
        username.setText(String.valueOf(officer_code));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {
                                    pd = ProgressDialog.show(OverViewControlNumbers.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                    new Thread() {
                                        public void run() {
/*                                            CallSoap callSoap = new CallSoap();
                                            callSoap.setFunctionName("isValidLogin");
                                            userid[0] = callSoap.isUserLoggedIn(username.getText().toString(),password.getText().toString());*/
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

                                            if (response.getStatusLine().getStatusCode() == 200) {
                                                JSONObject ob = null;
                                                String token = null;
                                                try {
                                                    ob = new JSONObject(content);
                                                    token = ob.getString("access_token");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                tokenl.saveTokenText(token.toString());

                                                final String finalToken = token;
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (finalToken.length() > 0) {
                                                            pd.dismiss();
                                                            Toast.makeText(OverViewControlNumbers.this, OverViewControlNumbers.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                        } else {
                                                            pd.dismiss();
                                                            Toast.makeText(OverViewControlNumbers.this, OverViewControlNumbers.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                            LoginDialogBox();
                                                        }
                                                    }
                                                });
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        pd.dismiss();
                                                        Toast.makeText(OverViewControlNumbers.this, OverViewControlNumbers.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                        LoginDialogBox();
                                                    }
                                                });
                                            }

                                        }
                                    }.start();
                                } else {
                                    LoginDialogBox();
                                    Toast.makeText(OverViewControlNumbers.this, OverViewControlNumbers.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private int getControlNumber(final JSONArray order) throws IOException, JSONException {

        //JSONArray newPolicies = removeDublicatePolicies(order);

        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("requests",order);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(){
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(AppInformation.DomainInfo.getDomain()+"/restapi/api/GetAssignedControlNumbers");
// Request parameters and other properties.
                try {
                    StringEntity postingString = new StringEntity(jsonObject.toString());
                    httpPost.setEntity(postingString);
                    httpPost.setHeader("Content-type", "application/json");
                    httpPost.setHeader("Authorization", "bearer "+tokenl.getTokenText());
                } catch (UnsupportedEncodingException e) {
                    // writing error to Log
                    e.printStackTrace();
                }
/*
 * Execute the HTTP Request
 */
                runOnUiThread(new Runnable() {
                    public void run() {
                        pd = ProgressDialog.show(OverViewControlNumbers.this, "", getResources().getString(R.string.Get_Control_Number));
                    }
                });

                HttpResponse response = null;
                try {
                    response = toRestApi.postToRestApiToken(jsonObject, "payment/GetAssignedControlNumbers");

                    HttpEntity respEntity = response.getEntity();

                    int cod = response.getStatusLine().getStatusCode();

                    if (respEntity != null) {
                        final String[] error_occured = {null};
                        final String[] error_message = {null};
                        final String[] internal_Identifier = {null};
                        final String[] control_number = {null};
                        // EntityUtils to get the response content
                        String content = null;
                        content = EntityUtils.toString(respEntity);

                        try {
                            JSONObject res = new JSONObject(content);
                            JSONObject ob = null;


                            if (cod >= 400) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd.dismiss();
                                        LoginDialogBox();
                                        if (tokenl.getTokenText().length() > 1) {
                                            View view = findViewById(R.id.actv);
                                            Snackbar.make(view, getResources().getString(R.string.has_no_rights), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd.dismiss();
                                    }
                                });
                                if (error_occured.equals("true")) {
                                    String erroroccured = res.getString("error_occured");
                                    error_message[0] = res.getString("error_message");

                                    View view = findViewById(R.id.actv);
                                    Snackbar.make(view, error_message[0], Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                } else {
                                    String assignedcontrolnumbers = res.getString("assigned_control_numbers");
                                    JSONArray arr = new JSONArray(assignedcontrolnumbers);
                                    for (int j = 0; j < arr.length(); j++) {
                                        try {
                                            ob = arr.getJSONObject(j);
                                            internal_Identifier[0] = ob.getString("internal_identifier");
                                            control_number[0] = ob.getString("control_number");
                                            updateAfterRequest(internal_Identifier[0], control_number[0]);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    pd.dismiss();
                                                }
                                            });
                                            View view = findViewById(R.id.actv);
                                            Snackbar.make(view, String.valueOf(e), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    }
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            pd.dismiss();
                                            policyDeleteDialogReport(getResources().getString(R.string.requestSent));
                                        }
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pd.dismiss();
                                    LoginDialogBox();
                                    if (tokenl.getTokenText().length() > 1) {
                                        View view = findViewById(R.id.actv);
                                        Snackbar.make(view, getResources().getString(R.string.has_no_rights), Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.dismiss();
                            }
                        });
                        View view = findViewById(R.id.actv);
                        Snackbar.make(view, getResources().getString(R.string.NoInternet), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pd.dismiss();
                        }
                    });
                    View view = findViewById(R.id.actv);
                    Snackbar.make(view, getResources().getString(R.string.NoInternet), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        };
        thread.start();

        return 0;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private JSONArray removeDublicatePolicies(JSONArray order) throws JSONException {
        JSONObject newPolicies = new JSONObject();
        String identifier = "";
        int count = order.length();
        try {
            for (int i = 0; i < count; i++) {

                JSONObject obj = new JSONObject(order.getString(i).toString());
                String objIdentifier = obj.get("internal_identifier").toString();
                if (!identifier.equals(objIdentifier)) {
                    newPolicies.put("internal_identifier", objIdentifier);
                }

                identifier = objIdentifier;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return order;
    }

    private void updateAfterRequest(String InternalIdentifier, String ControlNumber) {
        clientAndroidInterface.assignControlNumber(InternalIdentifier, ControlNumber);
    }


}

