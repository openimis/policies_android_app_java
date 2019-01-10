package tz.co.exact.imis;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;

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

public class OverViewPolicies extends AppCompatActivity {
    JSONArray policy;
    ClientAndroidInterface clientAndroidInterface;
    RecyclerView PolicyRecyclerView;
    OverViewPoliciesAdapter overViewPoliciesAdapter;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview_policies);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("View Policies " + "( "+search_count+" )");//getResources().getString(R.string.OverViewPolicies)

        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        final String[] n = {""};
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Global global = new Global();
                global = (Global) getApplicationContext();

                try {
                    getControlNumber.put("phoneNumber", "");
                    getControlNumber.put("requestDate", dt);
                    getControlNumber.put("officerCode", global.getOfficerCode());
                    getControlNumber.put("paymentDetails",paymentDetails);
                    getControlNumber.put("amount",PolicyValueToSend);

                    n[0] = "";
                    for(int i=0; i<num.size(); i++){
                        n[0] += num.get(i)+"\n";
                    }
                    AmountCalculated = String.valueOf(PolicyValueToSend);
                    trackBox(getControlNumber,String.valueOf(PolicyValueToSend));

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
        String search_string = getIntent().getStringExtra("SEARCH_STRING");
        fillRecordedPolicies(search_string);

        search_count = overViewPoliciesAdapter.getCount();

    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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
    }



    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void fillRecordedPolicies(String search_string){
        policy = clientAndroidInterface.getRecordedPolicies(search_string);//OrderArray;
        LayoutInflater li = LayoutInflater.from(OverViewPolicies.this);
        View promptsView = li.inflate(R.layout.activity_overview_policies, null);
        PolicyRecyclerView = (RecyclerView) findViewById(R.id.listofpolicies);
        overViewPoliciesAdapter = new OverViewPoliciesAdapter(this,policy);
        PolicyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        PolicyRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        PolicyRecyclerView.setAdapter(overViewPoliciesAdapter);
    }

    public void trackBox(final JSONObject policies, String Number){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.controls, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText amount = (EditText) promptsView.findViewById(R.id.display);
        final EditText phoneNumber = (EditText) promptsView.findViewById(R.id.phonenumber);

        amount.setText(Number);

        final EditText finalAmount = (EditText) promptsView.findViewById(R.id.display);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Get Control Number",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                try {
                                    policies.put("phoneNumber",phoneNumber.getText().toString());
                                    policies.put("amount",finalAmount.getText().toString());
                                    amountConfirmed = finalAmount.getText().toString();
                                    getControlNumber(policies);
                                } catch (JSONException e) {
                                    e.printStackTrace();
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

    private int getControlNumber(final JSONObject order) throws IOException {

        Thread thread = new Thread(){
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://imis-mv.swisstph-mis.ch/restapi/api/GetControlNumber");
// Request parameters and other properties.
                try {
                    StringEntity postingString = new StringEntity(order.toString());
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
                //HttpResponse response = httpClient.execute(httpPost);
                //HttpEntity respEntity = response.getEntity();

                if ("s" != null) {
                    // EntityUtils to get the response content
                    final String content = "l0";//EntityUtils.toString(respEntity);
                    if(!content.equals("{\"Message\":\"An error occurred while processing your request.\"}")){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                spinner.setVisibility(View.GONE);
                                int id = insertAfterRequest(AmountCalculated,amountConfirmed);
                                updateAfterRequest(id);

                                finish();
                                Intent i = new Intent(OverViewPolicies.this, OverViewPolicies.class);
                                startActivity(i);

                                View view = findViewById(R.id.actv);
                                Snackbar.make(view, content, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            public void run() {
                                spinner.setVisibility(View.GONE);
                                View view = findViewById(R.id.actv);
                                Snackbar.make(view, content, Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        });

                    }

                }


            }
        };


        thread.start();

        return 0;
    }

    private void updateAfterRequest(int Code) {
        JSONObject ob = null;
        for(int j = 0;j < paymentDetails.length();j++){
            try {
                ob = paymentDetails.getJSONObject(j);
                int Id = Integer.parseInt(ob.getString("Id"));
                clientAndroidInterface.updateRecordedPolicy(Id,Code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private int insertAfterRequest(String amountCalculated, String amountConfirmed) {
        return clientAndroidInterface.insertRecordedPolicy(amountCalculated,amountConfirmed);
    }

}
