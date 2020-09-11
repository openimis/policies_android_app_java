package org.openimis.imispolicies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openimis.imispolicies.R;

public class CheckCommission extends AppCompatActivity {

    Spinner spPayer;
    Spinner spProduct;
    Spinner spMonth;
    Spinner spMode;
    ClientAndroidInterface ca;

    EditText edYear;

    Button btnClear;
    Button btnCheck;

    Token tokenl;
    ToRestApi toRestApi;
    ProgressDialog pd;


    private ArrayList<HashMap<String, String>> PayersList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> ProductList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_commission);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.CheckCommission));


        spPayer = (Spinner) findViewById(R.id.spPayer);
        spProduct = (Spinner) findViewById(R.id.spProduct);
        spMonth = (Spinner) findViewById(R.id.spMonth);
        spMode = (Spinner) findViewById(R.id.spMode);

        edYear = (EditText) findViewById(R.id.edYear);

        btnCheck = (Button) findViewById(R.id.btnCheck);

        tokenl = new Token();
        toRestApi = new ToRestApi();
        pd = new ProgressDialog(this);

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String m = GetSelectedMonth();
                String pr = GetSelectedProduct();
                String py = GetSelectedPayer();
                String mode = GetSelectedMode();
                String yr = edYear.getText().toString();
                if(yr.equals("")){
                    pd.dismiss();
                    view = findViewById(R.id.actv);
                        Snackbar.make(view,getResources().getString(R.string.empty_year), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                }else{
                    getCommission(m,pr,py,yr,mode);
                }


            }
        });

        BindSpinnerMonth();
        BindSpinnerProduct();
        BindSpinnerPayers();
        BindSpinnerPayMode();
    }


    private void BindSpinnerMonth() {

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("January");
        categories.add("February");
        categories.add("March");
        categories.add("April");
        categories.add("May");
        categories.add("June");
        categories.add("July");
        categories.add("August");
        categories.add("September");
        categories.add("October");
        categories.add("November");
        categories.add("December");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spMonth.setAdapter(dataAdapter);
    }

    private void BindSpinnerPayMode() {

        // Spinner Drop down elements
        List<String> mode = new ArrayList<String>();
        mode.add("Paid");
        mode.add("Prescribed");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mode);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spMode.setAdapter(dataAdapter);
    }

    private void BindSpinnerPayers() {
        ca = new ClientAndroidInterface(this);

        Global global = new Global();
        global = (Global) CheckCommission.this.getApplicationContext();
        String OfficerCode = global.getOfficerCode();
        int LocationId = ca.getLocationId(OfficerCode);


        String result = ca.getPayer(LocationId);

        JSONArray jsonArray = null;
        JSONObject object;

        try {
            jsonArray = new JSONArray(result);

            if(jsonArray.length() == 0){
                HashMap<String, String> Payer1 = new HashMap<>();
                Payer1.put("PayerId", String.valueOf(0));
                Payer1.put("PayerName", getResources().getString(R.string.SelectPayer));
                PayersList.add(Payer1);

                SimpleAdapter adapter = new SimpleAdapter(CheckCommission.this, PayersList, R.layout.spinnerpayer,
                        new String[]{"PayerId", "PayerName"},
                        new int[]{R.id.tvPayerId, R.id.tvPayerName});

                spPayer.setAdapter(adapter);
            }else{
                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getJSONObject(i);

                    // Enter an empty record
                    if (i == 0) {
                        HashMap<String, String> Payer = new HashMap<>();
                        Payer.put("PayerId", String.valueOf(0));
                        Payer.put("PayerName", getResources().getString(R.string.SelectPayer));
                        PayersList.add(Payer);
                    }

                    HashMap<String, String> Payer = new HashMap<>();
                    Payer.put("PayerId", object.getString("PayerId"));
                    Payer.put("PayerName", object.getString("PayerName"));

                    PayersList.add(Payer);

                    SimpleAdapter adapter = new SimpleAdapter(CheckCommission.this, PayersList, R.layout.spinnerpayer,
                            new String[]{"PayerId", "PayerName"},
                            new int[]{R.id.tvPayerId, R.id.tvPayerName});

                    spPayer.setAdapter(adapter);

                }
            }




        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void BindSpinnerProduct() {
        ca = new ClientAndroidInterface(this);

        Global global = new Global();
        global = (Global) CheckCommission.this.getApplicationContext();
        String OfficerCode = global.getOfficerCode();
        int LocationId = ca.getLocationId(OfficerCode);

        //String result = ca.getProductsByDistrict(LocationId);
        String result = "";
        try {
            result = ca.getProductsByDistrict(ca.getLocationId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = null;
        JSONObject object;

        try {
            jsonArray = new JSONArray(result);

            ProductList.clear();

            if(jsonArray.length() == 0){
                HashMap<String, String> Product = new HashMap<>();
                Product.put("ProductCode", String.valueOf(0));
                Product.put("ProductName", getResources().getString(R.string.SelectProduct));
                ProductList.add(Product);

                SimpleAdapter adapter = new SimpleAdapter(CheckCommission.this, ProductList, R.layout.spinnerproducts,
                        new String[]{"ProductCode", "ProductName"},
                        new int[]{R.id.tvProductCode, R.id.tvProductName});

                spProduct.setAdapter(adapter);
            }else{
                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getJSONObject(i);

                    // Enter an empty record
                    if (i == 0) {
                        HashMap<String, String> Product = new HashMap<>();
                        Product.put("ProductCode", "");
                        Product.put("ProductName", getResources().getString(R.string.SelectProduct));
                        ProductList.add(Product);

                    }


                    HashMap<String, String> Product = new HashMap<>();
                    Product.put("ProductCode", object.getString("ProductCode"));
                    Product.put("ProductName", object.getString("ProductName"));


                    ProductList.add(Product);

                    SimpleAdapter adapter = new SimpleAdapter(CheckCommission.this, ProductList, R.layout.spinnerproducts,
                            new String[]{"ProductCode", "ProductName"},
                            new int[]{R.id.tvProductCode, R.id.tvProductName});

                    spProduct.setAdapter(adapter);

                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private String GetSelectedPayer() {
        String Payer = "";
        try{
            HashMap<String, String> P = new HashMap<>();
            //noinspection unchecked
            P = (HashMap<String, String>) spPayer.getSelectedItem();
            if(P.get("PayerId") == null || P.get("PayerId").toString().equals("0")) {
                Payer = "";
            }else{
                Payer = P.get("PayerId");
            }

        }catch (Exception e){
            // e.printStackTrace();
        }

        return Payer;
    }
    private String GetSelectedMode() {
        String Mode = "0";
        Object obj;
        try{
            HashMap<String, String> P = new HashMap<>();
            //noinspection unchecked
            obj = spMode.getSelectedItem();
            Mode = (obj.toString().equals("Paid"))?"0":"1";

        }catch (Exception e){
            e.printStackTrace();
        }

        return Mode;
    }

    private String GetSelectedProduct() {
        String Product = "";
        try{
            HashMap<String, String> P = new HashMap<>();
            //noinspection unchecked
            P = (HashMap<String, String>) spProduct.getSelectedItem();
            if(P.get("ProductCode") == null || P.get("ProductCode").toString().equals("0") || P.get("ProductCode").toString().equals("")) {
                Product = "";
            }else{
                Product = P.get("ProductCode");
            }

        }catch (Exception e){
            // e.printStackTrace();
        }

        return Product;
    }

    private String GetSelectedMonth() {
        String Month = "0";
        Object obj;
        try{
            HashMap<String, String> P = new HashMap<>();
            //noinspection unchecked
            obj = spMonth.getSelectedItem();
            if(obj.toString() == null) {
                Month = "";
            }else{
                Month = obj.toString();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return Month;
    }


    private void getCommission(String m, String pr, String py, String yr, String mode) {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            obj.put("enrolment_officer_code", ca.getOfficerCode().toString());
            if(m.equals("January")){obj.put("month", 1);}
            if(m.equals("February")){obj.put("month", 2);}
            if(m.equals("March")){obj.put("month", 3);}
            if(m.equals("April")){obj.put("month", 4);}
            if(m.equals("May")){obj.put("month", 5);}
            if(m.equals("June")){obj.put("month", 6);}
            if(m.equals("July")){obj.put("month", 7);}
            if(m.equals("August")){obj.put("month", 8);}
            if(m.equals("September")){obj.put("month", 9);}
            if(m.equals("October")){obj.put("month", 10);}
            if(m.equals("November")){obj.put("month", 11);}
            if(m.equals("December")){obj.put("month", 12);}
            obj.put("insrance_product_code", pr);
            obj.put("payer", py);
            obj.put("year", yr);
            obj.put("mode", mode);

            final String obj_to_send = obj.toString();

            Thread thread = new Thread(){
                public void run() {
                    String uri = AppInformation.DomainInfo.getDomain()+"api/";
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(uri + "policy/commissions");
                    try {
                        StringEntity postingString = new StringEntity(obj_to_send);
                        httpPost.setEntity(postingString);
                        httpPost.setHeader("Content-type", "application/json");
                        httpPost.setHeader("Authorization", "bearer "+tokenl.getTokenText());
                    } catch (UnsupportedEncodingException e) {
                        // writing error to Log
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd = ProgressDialog.show(CheckCommission.this, "", getResources().getString(R.string.Get_Commission));
                        }
                    });

                    //Send Request Here
                    HttpResponse response = null;
                    try {
                        response = httpClient.execute(httpPost);

                        HttpEntity respEntity = response.getEntity();

                        int cod = response.getStatusLine().getStatusCode();
                        if(cod >= 400){
                            final int c = cod;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pd.dismiss();
                                    LoginDialogBox();
                                    if(tokenl.getTokenText().length() > 1){
                                        View view = findViewById(R.id.actv);
                                        if(c == 500){
                                            Snackbar.make(view, c + "-"+getResources().getString(R.string.ServerError), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }else{
                                            Snackbar.make(view, c + "-"+getResources().getString(R.string.has_no_rights), Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }

                                    }
                                }
                            });
                        }else{
                            if (respEntity != null) {
                                final String[] error_occured = {null};
                                final String[] error_message = {null};
                                final String[] Amount = {null};
                                final String[] Commissions = {null};
                                // EntityUtils to get the response content
                                String content = null;
                                content = EntityUtils.toString(respEntity);

                                try {
                                    JSONObject main_res = new JSONObject(content);
                                    JSONObject ox = null;
                                    View view = findViewById(R.id.actv);

                                    error_occured[0] = main_res.getString("errorOccured");
                                    if(error_occured[0].equals("true")){
                                        error_message[0] = main_res.getString("messageValue");

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {pd.dismiss();}
                                        });
                                        Snackbar.make(view, error_message[0], Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }else {
                                        String comm = "";
                                        String amt = "";
                                        String data = main_res.getString("data");
                                        JSONArray arr = new JSONArray(data);
                                        for(int x = 0;x < arr.length();x++){
                                            try {
                                                ox = arr.getJSONObject(x);
                                                Amount[0] = ox.getString("amount");
                                                Commissions[0] = ox.getString("commission");
                                                if(Commissions[0].equals("null") || Commissions[0].equals("")){
                                                    comm = getResources().getString(R.string.no_data);
                                                }else {
                                                    comm = Commissions[0];
                                                }
                                                if(Amount[0].equals("null") || Amount[0].equals("")){
                                                    amt = "0";
                                                }else{
                                                    amt = Amount[0];
                                                }

                                                final String finalAmt = amt;
                                                final String finalComm = comm;
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        CommissionsDialogReport(finalAmt, finalComm);
                                                    }
                                                });


                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {pd.dismiss();}
                                                });
                                                view = findViewById(R.id.actv);
                                                Snackbar.make(view, String.valueOf(e), Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        }

                                    }
                                    //JSONArray arr = new JSONArray(commissions);
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                pd.dismiss();
                                            }
                                        });


                                } catch (JSONException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            pd.dismiss();
                                            LoginDialogBox();
                                            if(tokenl.getTokenText().length() > 1){
                                                View view = findViewById(R.id.actv);
                                                Snackbar.make(view, getResources().getString(R.string.has_no_rights), Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                        }
                                    });
                                }
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {pd.dismiss();}
                                });
                                View view = findViewById(R.id.actv);
                                Snackbar.make(view, getResources().getString(R.string.NoInternet), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }



                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {pd.dismiss();}
                        });
                        View view = findViewById(R.id.actv);
                        Snackbar.make(view, getResources().getString(R.string.NoInternet), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            };
            thread.start();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void LoginDialogBox() {

        //final int[] userid = {0};

        Global global = (Global) CheckCommission.this.getApplicationContext();

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
                                if(!username.getText().toString().equals("") && !password.getText().toString().equals("")){
                                    pd = ProgressDialog.show(CheckCommission.this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                    new Thread() {
                                        public void run() {
/*                                            CallSoap callSoap = new CallSoap();
                                            callSoap.setFunctionName("isValidLogin");
                                            userid[0] = callSoap.isUserLoggedIn(username.getText().toString(),password.getText().toString());*/
                                            JSONObject object = new JSONObject();
                                            try {
                                                object.put("userName",username.getText().toString());
                                                object.put("password",password.getText().toString());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            String functionName = "login";
                                            HttpResponse response = toRestApi.postToRestApi(object,functionName);

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

                                            if(response.getStatusLine().getStatusCode() == 200){
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
                                                        if(finalToken.length() > 0){
                                                            pd.dismiss();
                                                            Toast.makeText(CheckCommission.this,CheckCommission.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                        }else{
                                                            pd.dismiss();
                                                            Toast.makeText(CheckCommission.this,CheckCommission.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                            LoginDialogBox();
                                                        }
                                                    }
                                                });
                                            }else{
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        pd.dismiss();
                                                        Toast.makeText(CheckCommission.this,CheckCommission.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                        LoginDialogBox();
                                                    }
                                                });
                                            }

                                        }
                                    }.start();
                                }else{
                                    LoginDialogBox();
                                    Toast.makeText(CheckCommission.this,CheckCommission.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
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

    public void CommissionsDialogReport(String Amount, String Commissions){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.commissions_report, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView AmountValue = (TextView) promptsView.findViewById(R.id.AmountValue);
        final TextView CommissionsValue = (TextView) promptsView.findViewById(R.id.CommissionsValue);

        AmountValue.setText(Amount);
        CommissionsValue.setText(Commissions);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.button_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
