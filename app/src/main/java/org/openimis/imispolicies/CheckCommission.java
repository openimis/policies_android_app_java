package org.openimis.imispolicies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.openimis.imispolicies.tools.Log;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckCommission extends AppCompatActivity {

    private static final String LOG_TAG = "CHECKCOMMISION";
    private Global global;
    private Spinner spPayer;
    private Spinner spProduct;
    private Spinner spMonth;
    private Spinner spMode;
    private ClientAndroidInterface ca;

    private EditText edYear;
    private Button btnClear;
    private Button btnCheck;

    private Token tokenl;
    private ToRestApi toRestApi;
    private ProgressDialog pd;


    private List<HashMap<String, String>> payerList;
    private List<HashMap<String, String>> productList;
    private List<String> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global = (Global) getApplicationContext();

        setContentView(R.layout.activity_check_commission);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.CheckCommission));
        }

        spPayer = findViewById(R.id.spPayer);
        spProduct = findViewById(R.id.spProduct);
        spMonth = findViewById(R.id.spMonth);
        spMode = findViewById(R.id.spMode);
        edYear = findViewById(R.id.edYear);
        btnCheck = findViewById(R.id.btnCheck);
        btnClear = findViewById(R.id.btnClear);

        tokenl = new Token();
        toRestApi = new ToRestApi();
        pd = new ProgressDialog(this);

        btnCheck.setOnClickListener(view -> {
            JSONObject obj = new JSONObject();
            try {
                obj.put("enrolment_officer_code", ca.getOfficerCode());
                obj.put("month", getSelectedMonth());
                obj.put("year", getSelectedYear());
                obj.put("insrance_product_code", getSelectedProduct());
                obj.put("payer", GetSelectedPayer());
                obj.put("mode", getSelectedMode());

                if (obj.getInt("year") < 1000 || obj.getInt("year") > 9999) {
                    Toast.makeText(this, getResources().getString(R.string.empty_year), Toast.LENGTH_LONG).show();
                    return;
                }

                if (obj.getInt("month") == -1) {
                    Toast.makeText(this, getResources().getString(R.string.EmptyMonth), Toast.LENGTH_LONG).show();
                    return;
                }

                new Thread(() -> getCommission(obj)).start();
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error while processing input fields", e);
            }
        });

        BindSpinnerMonth();
        BindSpinnerProduct();
        BindSpinnerPayers();
        BindSpinnerPayMode();
    }


    private void BindSpinnerMonth() {
        categoryList = new ArrayList<>();
        categoryList.add(getResources().getString(R.string.SelectMonth));
        categoryList.add("January");
        categoryList.add("February");
        categoryList.add("March");
        categoryList.add("April");
        categoryList.add("May");
        categoryList.add("June");
        categoryList.add("July");
        categoryList.add("August");
        categoryList.add("September");
        categoryList.add("October");
        categoryList.add("November");
        categoryList.add("December");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(dataAdapter);
    }

    private void BindSpinnerPayMode() {
        List<String> mode = new ArrayList<>();
        mode.add("Paid");
        mode.add("Prescribed");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mode);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMode.setAdapter(dataAdapter);
    }

    private void BindSpinnerPayers() {
        payerList = new ArrayList<>();
        ca = new ClientAndroidInterface(this);
        String OfficerCode = global.getOfficerCode();
        int LocationId = ca.getLocationId(OfficerCode);
        String result = ca.getPayer(LocationId);

        JSONArray jsonArray;
        JSONObject object;
        HashMap<String, String> payer;

        try {
            jsonArray = new JSONArray(result);
            payer = new HashMap<>();
            payer.put("PayerId", String.valueOf(0));
            payer.put("PayerName", getResources().getString(R.string.SelectPayer));
            payerList.add(payer);

            for (int i = 0; i < jsonArray.length(); i++) {
                object = jsonArray.getJSONObject(i);

                payer = new HashMap<>();
                payer.put("PayerId", object.getString("PayerId"));
                payer.put("PayerName", object.getString("PayerName"));

                payerList.add(payer);
            }

            SimpleAdapter adapter = new SimpleAdapter(CheckCommission.this, payerList, R.layout.spinnerpayer,
                    new String[]{"PayerId", "PayerName"},
                    new int[]{R.id.tvPayerId, R.id.tvPayerName});

            spPayer.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void BindSpinnerProduct() {
        productList = new ArrayList<>();
        ca = new ClientAndroidInterface(this);

        String result = "";
        try {
            result = ca.getProductsByDistrict(ca.getLocationId(), null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray;
        JSONObject object;
        HashMap<String, String> Product;
        try {
            jsonArray = new JSONArray(result);
            Product = new HashMap<>();
            Product.put("ProductCode", "");
            Product.put("ProductName", getResources().getString(R.string.SelectProduct));
            productList.add(Product);

            for (int i = 0; i < jsonArray.length(); i++) {
                object = jsonArray.getJSONObject(i);

                Product = new HashMap<>();
                Product.put("ProductCode", object.getString("ProductCode"));
                Product.put("ProductName", object.getString("ProductName"));

                productList.add(Product);
            }

            SimpleAdapter adapter = new SimpleAdapter(CheckCommission.this, productList, R.layout.spinnerproducts,
                    new String[]{"ProductCode", "ProductName"},
                    new int[]{R.id.tvProductCode, R.id.tvProductName});

            spProduct.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String GetSelectedPayer() {
        try {
            int pos = spPayer.getSelectedItemPosition();
            if (pos > 0) {
                Map<String, String> item = payerList.get(pos);
                return item.get("PayerId");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while reading selected payer", e);
        }
        return "";
    }

    private String getSelectedMode() {
        Object obj;
        try {
            obj = spMode.getSelectedItem();
            return ("Prescribed".equals(obj.toString())) ? "0" : "1";
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while reading selected mode", e);
        }
        return "";
    }

    private String getSelectedProduct() {
        try {
            int pos = spProduct.getSelectedItemPosition();
            if (pos > 0) {
                Map<String, String> item = productList.get(pos);
                return item.get("ProductCode");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while reading selected product", e);
        }
        return "";
    }

    private int getSelectedMonth() {
        int month = -1;
        String selectedObj;
        selectedObj = (String) spMonth.getSelectedItem();
        if (selectedObj != null) {
            month = categoryList.indexOf(selectedObj);
        }
        return month;
    }

    private int getSelectedYear() {
        int year;
        try {
            year = Integer.parseInt(edYear.getText().toString());
        } catch (NumberFormatException e) {
            year = -1;
        }
        return year;
    }


    private void getCommission(JSONObject obj) {
        runOnUiThread(() -> pd = ProgressDialog.show(this, "", getResources().getString(R.string.Get_Commission)));
        HttpResponse response;
        try {
            response = toRestApi.postToRestApiToken(obj, "policy/commissions");
            HttpEntity respEntity = response.getEntity();

            int cod = response.getStatusLine().getStatusCode();
            if (cod >= 400) {
                final int c = cod;
                runOnUiThread(() -> {
                    pd.dismiss();
                    LoginDialogBox();
                    if (tokenl.getTokenText().length() > 1) {
                        View view = findViewById(R.id.actv);
                        if (c == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                            Snackbar.make(view, c + "-" + getResources().getString(R.string.ServerError), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else {
                            Snackbar.make(view, c + "-" + getResources().getString(R.string.has_no_rights), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }
                });
            } else {
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
                        if (error_occured[0].equals("true")) {
                            error_message[0] = main_res.getString("messageValue");

                            runOnUiThread(() -> pd.dismiss());
                            Snackbar.make(view, error_message[0], Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else {
                            String comm = "";
                            String amt = "";
                            String data = main_res.getString("data");
                            JSONArray arr = new JSONArray(data);
                            for (int x = 0; x < arr.length(); x++) {
                                try {
                                    ox = arr.getJSONObject(x);
                                    Amount[0] = ox.getString("amount");
                                    Commissions[0] = ox.getString("commission");
                                    if (Commissions[0].equals("null") || Commissions[0].equals("")) {
                                        comm = getResources().getString(R.string.no_data);
                                    } else {
                                        comm = Commissions[0];
                                    }
                                    if (Amount[0].equals("null") || Amount[0].equals("")) {
                                        amt = "0";
                                    } else {
                                        amt = Amount[0];
                                    }

                                    final String finalAmt = amt;
                                    final String finalComm = comm;
                                    runOnUiThread(() -> CommissionsDialogReport(finalAmt, finalComm));


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    runOnUiThread(() -> pd.dismiss());
                                    view = findViewById(R.id.actv);
                                    Snackbar.make(view, String.valueOf(e), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }

                        }
                        //JSONArray arr = new JSONArray(commissions);
                        runOnUiThread(() -> pd.dismiss());


                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            pd.dismiss();
                            LoginDialogBox();
                            if (tokenl.getTokenText().length() > 1) {
                                View view = findViewById(R.id.actv);
                                Snackbar.make(view, getResources().getString(R.string.has_no_rights), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(() -> pd.dismiss());
                    View view = findViewById(R.id.actv);
                    Snackbar.make(view, getResources().getString(R.string.NoInternet), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }


        } catch (IOException e) {
            runOnUiThread(() -> pd.dismiss());
            View view = findViewById(R.id.actv);
            Snackbar.make(view, getResources().getString(R.string.NoInternet), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }


    public void LoginDialogBox() {
        if (!ca.CheckInternetAvailable()) {
            return;
        }

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setView(promptsView);

        final TextView username = promptsView.findViewById(R.id.UserName);
        final TextView password = promptsView.findViewById(R.id.Password);
        String officer_code = global.getOfficerCode();
        username.setText(String.valueOf(officer_code));

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Ok),
                        (dialog, id) -> {
                            if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {
                                pd = ProgressDialog.show(this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

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
                                        try {
                                            content = EntityUtils.toString(respEntity);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK && content != null) {
                                        JSONObject ob;
                                        String token = "";
                                        String validTo = "";
                                        try {
                                            ob = new JSONObject(content);
                                            token = ob.getString("access_token");
                                            validTo = ob.getString("expires_on");

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        tokenl.saveTokenText(token, validTo, global.getOfficerCode());

                                        final String finalToken = token;
                                        runOnUiThread(() -> {
                                            if (finalToken.length() > 0) {
                                                pd.dismiss();
                                                Toast.makeText(this, getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                            } else {
                                                pd.dismiss();
                                                Toast.makeText(this, getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                LoginDialogBox();
                                            }
                                        });
                                    } else {
                                        runOnUiThread(() -> {
                                            pd.dismiss();
                                            Toast.makeText(this, getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                            LoginDialogBox();
                                        });
                                    }

                                }
                                ).start();
                            } else {
                                LoginDialogBox();
                                Toast.makeText(this, getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        (dialog, id) -> dialog.cancel())
                .show();
    }

    public void CommissionsDialogReport(String Amount, String Commissions) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.commissions_report, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);

        final TextView AmountValue = promptsView.findViewById(R.id.AmountValue);
        final TextView CommissionsValue = promptsView.findViewById(R.id.CommissionsValue);
        AmountValue.setText(Amount);
        CommissionsValue.setText(Commissions);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.button_ok),
                        (dialog, id) -> dialog.dismiss())
                .show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
