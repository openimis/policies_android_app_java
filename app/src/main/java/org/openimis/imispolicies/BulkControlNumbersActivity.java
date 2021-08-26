package org.openimis.imispolicies;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
        fetchBulkCn.setOnClickListener((view) -> fetchBulkControlNumbers());
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
        String officerCode = global.getOfficerCode();
        assignedCNCount.setText(String.format(Locale.US, "%d", sqlHandler.getAssignedCNCount("0", officerCode)));
        freeCNCount.setText(String.format(Locale.US, "%d", sqlHandler.getFreeCNCount("0", officerCode)));

        List<Map<String, String>> assignedCNDetailsData = new ArrayList<>();
        List<Map<String, String>> freeCNDetailsData = new ArrayList<>();

        JSONArray products = sqlHandler.getAvailableProducts(officerCode);
        try {
            for (int i = 0; i < products.length(); i++) {
                int assignedCount = sqlHandler.getAssignedCNCount(products.getJSONObject(i).getString("ProdId"), officerCode);
                if (assignedCount > 0) {
                    Map<String, String> row = new HashMap<>();
                    row.put("ProductName", String.format("%s - %s",
                            products.getJSONObject(i).getString("ProductCode"),
                            products.getJSONObject(i).getString("ProductName")));
                    row.put("CNAmount", String.valueOf(assignedCount));

                    assignedCNDetailsData.add(row);
                }
                int freeCount = sqlHandler.getFreeCNCount(products.getJSONObject(i).getString("ProdId"), officerCode);
                if (freeCount > 0) {
                    Map<String, String> row = new HashMap<>();
                    row.put("ProductName", String.format("%s - %s",
                            products.getJSONObject(i).getString("ProductCode"),
                            products.getJSONObject(i).getString("ProductName")));
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

    protected void fetchBulkControlNumbers() {
        ControlNumberService.fetchBulkControlNumbers(this);
    }
}
