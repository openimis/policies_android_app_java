package org.openimis.imispolicies;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BulkControlNumbersActivity extends AppCompatActivity {
    private static final String LOG_TAG = "BULKCN";
    private static final String CN_COUNT_INITIAL_VALUE = "0";
    private TextView assignedCNCount;
    private ListView assignedCNDetails;
    private TextView freeCNCount;
    private ListView freeCNDetails;
    private SQLHandler sqlHandler;
    private Global global;

    private String officerCode;
    private JSONArray availableProducts;

    private String[] productNames;
    private String[] productCodes;

    private Spinner dropdownSpinner;

    private AlertDialog productDialog;
    private ScheduledExecutorService productDialogTimer;
    private ProgressDialog progressDialog;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleRequestResult(intent);
        }
    };

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulk_control_numbers);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.BulkCN);
        }

        global = (Global) getApplicationContext();
        sqlHandler = new SQLHandler(this);

        assignedCNCount = findViewById(R.id.AssignedCNCount);
        freeCNCount = findViewById(R.id.FreeCNCount);
        TextView fetchBulkCn = findViewById(R.id.FetchBulkCn);
        assignedCNDetails = findViewById(R.id.AssignedCNDetails);
        freeCNDetails = findViewById(R.id.FreeCNDetails);

        assignedCNCount.setText(CN_COUNT_INITIAL_VALUE);
        freeCNCount.setText(CN_COUNT_INITIAL_VALUE);

        officerCode = global.getOfficerCode();
        availableProducts = sqlHandler.getAvailableProducts(officerCode);
        createProductDialog();

        fetchBulkCn.setOnClickListener((view) -> {
            if (global.isNetworkAvailable() && global.isLoggedIn()) {
                productDialog.show();
                shutdownTimer();
                startTimer(productCodes[dropdownSpinner.getSelectedItemPosition()], productDialog);
            } else {
                Toast.makeText(this, R.string.LogInToFetchCn, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shutdownTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ControlNumberService.ACTION_REQUEST_SUCCESS);
        intentFilter.addAction(ControlNumberService.ACTION_REQUEST_ERROR);
        registerReceiver(broadcastReceiver, intentFilter);
        refreshCNCount();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void handleRequestResult(Intent intent) {
        if (intent.getAction().equals(ControlNumberService.ACTION_REQUEST_ERROR)) {
            String errorMessage = intent.getStringExtra(ControlNumberService.FIELD_ERROR_MESSAGE);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
        progressDialog.dismiss();

        new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.CnRequestComplete, String.valueOf(global.getIntKey("min_cn_request_interval", 60))))
                .setPositiveButton(R.string.Ok, (dialog, id) -> refreshCNCount())
                .show();
    }

    protected void refreshCNCount() {
        assignedCNCount.setText(String.format(Locale.US, "%d", sqlHandler.getAssignedCNCount(officerCode)));
        freeCNCount.setText(String.format(Locale.US, "%d", sqlHandler.getFreeCNCount(officerCode)));

        List<Map<String, String>> assignedCNDetailsData = new ArrayList<>();
        List<Map<String, String>> freeCNDetailsData = new ArrayList<>();

        try {
            for (int i = 0; i < availableProducts.length(); i++) {
                JSONObject product = availableProducts.getJSONObject(i);
                assignedCNDetailsData.add(createAssignedCnDetailsEntry(product));
                freeCNDetailsData.add(createFreeCnDetailsEntry(product));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Parsing CN details failed", e);
        }

        assignedCNDetails.setAdapter(createCnDetailsAdapter(assignedCNDetailsData));
        freeCNDetails.setAdapter(createCnDetailsAdapter(freeCNDetailsData));
    }

    protected Map<String, String> createFreeCnDetailsEntry(JSONObject product) throws JSONException {
        int assignedCount = sqlHandler.getFreeCNCount(officerCode, product.getString("ProductCode"));
        return createCnDetailsEntry(product, assignedCount);
    }

    protected Map<String, String> createAssignedCnDetailsEntry(JSONObject product) throws JSONException {
        int freeCount = sqlHandler.getAssignedCNCount(officerCode, product.getString("ProductCode"));
        return createCnDetailsEntry(product, freeCount);
    }

    protected Map<String, String> createCnDetailsEntry(JSONObject product, int cnAmount) throws JSONException {
        Map<String, String> row = new HashMap<>();
        row.put("ProductName", String.format("%s - %s",
                product.getString("ProductCode"),
                product.getString("ProductName")));
        row.put("CNAmount", String.valueOf(cnAmount));
        return row;
    }

    protected SimpleAdapter createCnDetailsAdapter(List<Map<String, String>> data) {
        return new SimpleAdapter(
                this,
                data,
                R.layout.bulk_cn_product_details,
                new String[]{"ProductName", "CNAmount"},
                new int[]{R.id.CNProductName, R.id.CNProductCount}
        );
    }

    protected void createProductDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View productDropDown = li.inflate(R.layout.dropdown_dialog, (ViewGroup) null);

        TextView dropdownDescription = productDropDown.findViewById(R.id.description);
        dropdownDescription.setText(R.string.SelectProduct);

        dropdownSpinner = productDropDown.findViewById(R.id.dropdown);

        productNames = new String[availableProducts.length()];
        productCodes = new String[availableProducts.length()];

        try {
            for (int i = 0; i < availableProducts.length(); i++) {
                productNames[i] = String.format("%s - %s",
                        availableProducts.getJSONObject(i).getString("ProductCode"),
                        availableProducts.getJSONObject(i).getString("ProductName"));
                productCodes[i] = availableProducts.getJSONObject(i).getString("ProductCode");
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Parsing product codes for dropdown failed", e);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productNames);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(productDropDown);
        productDialog = alertDialogBuilder
                .setPositiveButton(getResources().getString(R.string.Fetch),
                        (dialog, id) -> {
                            shutdownTimer();
                            progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.FetchBulkCN));
                            String productCode = productCodes[dropdownSpinner.getSelectedItemPosition()];
                            global.setLongKey(String.format("last_%s_request", productCode), System.currentTimeMillis());
                            ControlNumberService.fetchBulkControlNumbers(this, productCode);
                        })
                .setNegativeButton(getResources().getString(R.string.Cancel),
                        (dialog, id) -> {
                            shutdownTimer();
                            dialog.cancel();
                        })
                .setOnCancelListener((dialog) -> shutdownTimer())
                .create();

        //disable button by default, enable after interval check
        productDialog.setOnShowListener((dialog) -> ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));

        dropdownSpinner.setAdapter(arrayAdapter);
        dropdownSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                shutdownTimer();
                startTimer(productCodes[position], productDialog);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                shutdownTimer();
            }
        });
    }

    protected void checkLastProductRequest(String productCode, final AlertDialog productDialog) {
        int minInterval = (global.getIntKey("min_cn_request_interval", 60));
        long lastProductRequestMillis = global.getLongKey(String.format("last_%s_request", productCode), 0);
        long currentMillis = System.currentTimeMillis();
        long interval = (currentMillis - lastProductRequestMillis) / 1000L;
        if (interval >= minInterval) {
            this.runOnUiThread(() -> enableFetchButton(productDialog));
            shutdownTimer();
        } else {
            this.runOnUiThread(() -> disableFetchButton(minInterval - interval, productDialog));
        }
    }

    protected void startTimer(String productCode, final AlertDialog productDialog) {
        Log.i("BULKCN", "Async timer starting");
        productDialogTimer = Executors.newSingleThreadScheduledExecutor();
        productDialogTimer.scheduleAtFixedRate(() -> checkLastProductRequest(productCode, productDialog), 0, 1000, TimeUnit.MILLISECONDS);
    }

    protected void shutdownTimer() {
        if (productDialogTimer != null && !productDialogTimer.isShutdown()) {
            Log.i("BULKCN", "Async timer stopping");
            productDialogTimer.shutdown();
        }
    }

    protected void enableFetchButton(final AlertDialog productDialog) {
        Button fetchButton = productDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (fetchButton != null) {
            fetchButton.setEnabled(true);
            fetchButton.setText(getResources().getString(R.string.Fetch));
            fetchButton.invalidate();
        }
    }

    protected void disableFetchButton(long seconds, final AlertDialog productDialog) {
        Button fetchButton = productDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (fetchButton != null) {
            fetchButton.setEnabled(false);
            fetchButton.setText(getResources().getString(R.string.WaitXSeconds, String.valueOf(seconds)));
            fetchButton.invalidate();
        }
    }
}
