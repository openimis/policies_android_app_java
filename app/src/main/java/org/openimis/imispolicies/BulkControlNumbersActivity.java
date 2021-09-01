package org.openimis.imispolicies;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class BulkControlNumbersActivity extends AppCompatActivity {
    private static final String LOG_TAG = "BULKCN";
    private static final String CN_COUNT_INITIAL_VALUE = "0";
    private TextView assignedCNCount;
    private ListView assignedCNDetails;
    private TextView freeCNCount;
    private ListView freeCNDetails;
    private TextView fetchBulkCn;
    private SQLHandler sqlHandler;
    private Global global;

    private String officerCode;
    private JSONArray availableProducts;

    private String[] productNames;
    private String[] productCodes;

    private View productDropDown;
    private TextView dropdownDescription;
    private Spinner dropdownSpinner;

    private AlertDialog productDialog;

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
        fetchBulkCn = findViewById(R.id.FetchBulkCn);
        assignedCNDetails = findViewById(R.id.AssignedCNDetails);
        freeCNDetails = findViewById(R.id.FreeCNDetails);

        assignedCNCount.setText(CN_COUNT_INITIAL_VALUE);
        freeCNCount.setText(CN_COUNT_INITIAL_VALUE);

        fetchBulkCn.setOnClickListener((view) -> {
            if (global.isNetworkAvailable() && global.isLoggedIn())
                productDialog.show();
            else {
                Toast.makeText(this, R.string.LogInToFetchCn, Toast.LENGTH_LONG).show();
            }
        });

        officerCode = global.getOfficerCode();
        availableProducts = sqlHandler.getAvailableProducts(officerCode);
        productDialog = createProductDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCNCount();
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
        int assignedCount = sqlHandler.getAssignedCNCount(officerCode, product.getString("ProductCode"));
        return createCnDetailsEntry(product, assignedCount);
    }

    protected Map<String, String> createAssignedCnDetailsEntry(JSONObject product) throws JSONException {
        int freeCount = sqlHandler.getFreeCNCount(officerCode, product.getString("ProductCode"));
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

    protected AlertDialog createProductDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        productDropDown = li.inflate(R.layout.dropdown_dialog, (ViewGroup) null);

        dropdownDescription = productDropDown.findViewById(R.id.description);
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
        dropdownSpinner.setAdapter(arrayAdapter);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(productDropDown);

        return alertDialogBuilder
                .setPositiveButton(getResources().getString(R.string.Fetch),
                        (dialog, id) -> ControlNumberService.fetchBulkControlNumbers(this,
                                productCodes[dropdownSpinner.getSelectedItemPosition()]))
                .setNegativeButton(getResources().getString(R.string.Cancel),
                        (dialog, id) -> dialog.cancel())
                .create();
    }
}
