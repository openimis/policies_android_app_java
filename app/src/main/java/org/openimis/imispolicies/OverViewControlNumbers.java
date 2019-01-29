package org.openimis.imispolicies;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OverViewControlNumbers extends AppCompatActivity {
    public SQLHandler sqlHandler;


    JSONArray policy;
    ClientAndroidInterface clientAndroidInterface;
    RecyclerView PolicyRecyclerView;
    OverViewControlNumberAdapter overViewControlNumberAdapter;

    TextView ValueNumberOfPolices;
    TextView ValueAmountOfContribution;

    public static int search_count = 0;

    private ProgressBar spinner;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over_view_control_numbers);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("OverViewControlNumber");

        ValueNumberOfPolices = (TextView) findViewById(R.id.ValueNumberOfPolices);
        ValueAmountOfContribution = (TextView) findViewById(R.id.ValueAmountOfContribution);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        NothingFound = (TextView) findViewById(R.id.NothingFound);

        final String[] n = {""};
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Global global = new Global();
                global = (Global) getApplicationContext();

                try {
                    getControlNumber.put("internalIdentifier",PolicyValueToSend);

                    n[0] = "";
                    for(int i=0; i<num.size(); i++){
                        n[0] += num.get(i)+"\n";
                    }
                    AmountCalculated = String.valueOf(PolicyValueToSend);
                    if(num.size() != 0){
                        trackBox(paymentDetails);
                    }else{
                        View view1 = findViewById(R.id.actv);
                        Snackbar.make(view1, "Please select a policy/policies to request", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (paymentDetails.length()>0){
                    for(int i=0; i<paymentDetails.length(); i++){

                        try {

                            JSONObject payment = paymentDetails.getJSONObject(0);
                            String policyid = payment.getString("PolicyId");
                            clientAndroidInterface.deleteRecodedPolicy(policyid);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            spinner.setVisibility(View.GONE);
                            View view = findViewById(R.id.actv);
                            Snackbar.make(view, "Data deleted successfully", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        public void run() {
                            spinner.setVisibility(View.GONE);
                            View view = findViewById(R.id.actv);
                            Snackbar.make(view, "No Data to delete", Snackbar.LENGTH_LONG)
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

        fillRecordedPolicies();

        int PolicyValue = 0;
        JSONObject ob = null;
        if(policy != null){
            for(int j = 0; j < policy.length(); j++){
                try {
                    ob = policy.getJSONObject(j);
                    PolicyValue += Integer.parseInt(ob.getString("PolicyValue"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            search_count = overViewControlNumberAdapter.getCount();
            if(search_count == 0){
                NothingFound.setVisibility(View.VISIBLE);
            }
            ValueNumberOfPolices.setText(String.valueOf(search_count));
            ValueAmountOfContribution.setText(String.valueOf(PolicyValue)+"/=");
        }else{
            ValueNumberOfPolices.setText("0");
            ValueAmountOfContribution.setText("0/=");
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



    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void fillRecordedPolicies(){
        policy = clientAndroidInterface.getRecordedPolicies(InsuranceNumber,OtherNames,LastName,InsuranceProduct,UploadedFrom,UploadedTo,RadioRenewal,RequestedFrom,RequestedTo);//OrderArray;
        LayoutInflater li = LayoutInflater.from(OverViewControlNumbers.this);
        View promptsView = li.inflate(R.layout.activity_over_view_control_numbers, null);
        PolicyRecyclerView = (RecyclerView) findViewById(R.id.listofpolicies);
        overViewControlNumberAdapter = new OverViewControlNumberAdapter(this,policy);
        PolicyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        PolicyRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        PolicyRecyclerView.setAdapter(overViewControlNumberAdapter);
    }


    public void trackBox(final JSONArray policies){
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
                            public void onClick(DialogInterface dialog,int id) {
                                try {
                                    getControlNumber(policies);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    private int getControlNumber(final JSONArray order) throws IOException {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requests",order);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(){
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://imis-mv.swisstph-mis.ch/restapi/api/GetAssignedControlNumbers");
// Request parameters and other properties.
                try {
                    StringEntity postingString = new StringEntity(jsonObject.toString());
                    httpPost.setEntity(postingString);
                    httpPost.setHeader("Content-type", "application/json");
                } catch (UnsupportedEncodingException e) {
                    // writing error to Log
                    e.printStackTrace();
                }
/*
 * Execute the HTTP Request
 */
                runOnUiThread(new Runnable() {
                    public void run() {
                        spinner.setVisibility(View.VISIBLE);
                    }
                });

                //Send Request Here
                HttpResponse response = null;
                try {
                    response = httpClient.execute(httpPost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                HttpEntity respEntity = response.getEntity();

                if (respEntity != null) {
                    final String[] error_occured = {null};
                    final String[] error_message = {null};
                    final String[] internal_Identifier = {null};
                    final String[] control_number = {null};
                    // EntityUtils to get the response content
                    String content = null;
                    try {
                        content = EntityUtils.toString(respEntity);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONObject res = new JSONObject(content);
                        JSONObject ob = null;

                        String erroroccured = res.getString("error_occured");
                        String assignedcontrolnumbers = res.getString("assigned_control_numbers");

                        if(erroroccured.equals("true")){
                            error_message[0] = res.getString("error_message");
                        }else {
                            JSONArray arr = new JSONArray(assignedcontrolnumbers);
                            for(int j = 0;j < arr.length();j++){
                                try {
                                    ob = arr.getJSONObject(j);

                                    internal_Identifier[0] = ob.getString("internal_identifier");
                                    control_number[0] = ob.getString("control_number");

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            spinner.setVisibility(View.GONE);
                                            updateAfterRequest(internal_Identifier[0], control_number[0]);

                                            finish();
                                            Intent i = new Intent(OverViewControlNumbers.this, SearchOverViewControlNumber.class);
                                            startActivity(i);

                                        }
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }


                        View view = findViewById(R.id.actv);
                        Snackbar.make(view, getResources().getString(R.string.process_complete), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            }
        };


        thread.start();

        return 0;
    }

    private void updateAfterRequest(String InternalIdentifier, String ControlNumber) {
        clientAndroidInterface.assignControlNumber(InternalIdentifier, ControlNumber);
    }


}

