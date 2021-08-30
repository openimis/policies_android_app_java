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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BulkControlNumbersActivity extends AppCompatActivity {
    private static final String LOG_TAG = "BULKCN";
    TextView assignedCNCount;
    ListView assignedCNDetails;
    TextView freeCNCount;
    ListView freeCNDetails;
    TextView fetchBulkCn;
    SQLHandler sqlHandler;
    Global global;

    String officerCode;
    JSONArray availableProducts;

    String[] productNames;
    String[] productCodes;

    View productDropDown;
    TextView dropdownDescription;
    Spinner dropdownSpinner;

    AlertDialog productDialog;

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

        assignedCNCount.setText("0");
        freeCNCount.setText("0");
        fetchBulkCn.setOnClickListener((view) -> {
            if(global.isNetworkAvailable() && global.isLoggedIn())
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    protected void refreshCNCount() {
        assignedCNCount.setText(String.format(Locale.US, "%d", sqlHandler.getAssignedCNCount(officerCode)));
        freeCNCount.setText(String.format(Locale.US, "%d", sqlHandler.getFreeCNCount(officerCode)));

        List<Map<String, String>> assignedCNDetailsData = new ArrayList<>();
        List<Map<String, String>> freeCNDetailsData = new ArrayList<>();

        try {
            for (int i = 0; i < availableProducts.length(); i++) {
                int assignedCount = sqlHandler.getAssignedCNCount(officerCode, availableProducts.getJSONObject(i).getString("ProductCode"));
                if (assignedCount >= 0) {
                    Map<String, String> row = new HashMap<>();
                    row.put("ProductName", String.format("%s - %s",
                            availableProducts.getJSONObject(i).getString("ProductCode"),
                            availableProducts.getJSONObject(i).getString("ProductName")));
                    row.put("CNAmount", String.valueOf(assignedCount));

                    assignedCNDetailsData.add(row);
                }
                int freeCount = sqlHandler.getFreeCNCount(officerCode, availableProducts.getJSONObject(i).getString("ProductCode"));
                if (freeCount >= 0) {
                    Map<String, String> row = new HashMap<>();
                    row.put("ProductName", String.format("%s - %s",
                            availableProducts.getJSONObject(i).getString("ProductCode"),
                            availableProducts.getJSONObject(i).getString("ProductName")));
                    row.put("CNAmount", String.valueOf(freeCount));

                    freeCNDetailsData.add(row);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Parsing CN details failed", e);
        }

        assignedCNDetails.setAdapter(new SimpleAdapter(
                this,
                assignedCNDetailsData,
                R.layout.bulk_cn_product_details,
                new String[]{"ProductName", "CNAmount"},
                new int[]{R.id.AssignedCNProductName, R.id.AssignedCNProductCount}
        ));

        freeCNDetails.setAdapter(new SimpleAdapter(
                this,
                freeCNDetailsData,
                R.layout.bulk_cn_product_details,
                new String[]{"ProductName", "CNAmount"},
                new int[]{R.id.AssignedCNProductName, R.id.AssignedCNProductCount}
        ));
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
