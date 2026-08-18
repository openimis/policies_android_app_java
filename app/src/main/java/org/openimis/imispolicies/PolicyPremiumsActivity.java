package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.json.JSONArray;
import org.json.JSONException;

public class PolicyPremiumsActivity extends AppCompatActivity {

    ClientAndroidInterface ca;
    RecyclerView recyclerView;
    private int familyId, policyId, regionId, districtId;
    String page = "premiums";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_premiums);
        setTitle(R.string.Premiums);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ca = new ClientAndroidInterface(this);
        familyId = getIntent().getIntExtra("FamilyId", 0);
        policyId = getIntent().getIntExtra("PolicyId", 0);
        regionId = getIntent().getIntExtra("RegionId", 0);
        districtId = getIntent().getIntExtra("DistrictId", 0);
        initViews();
        loadPremiums();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_add && page.equals("premiums")) {
            Intent intent = new Intent(this, PremiumActivity.class);
            intent.putExtra("FamilyId", familyId);
            intent.putExtra("PolicyId", policyId);
            intent.putExtra("RegionId", regionId);
            intent.putExtra("DistrictId", districtId);
            intent.putExtra("PremiumId", 0);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews(){
        recyclerView = findViewById(R.id.recyclerPremiums);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadPremiums() {
        String premiums = ca.getPremiums(policyId);
        try{
            JSONArray premiumArray = new JSONArray(premiums);
            PremiumAdapter adapter = new PremiumAdapter(this,premiumArray, policyId, familyId, regionId, districtId);
            recyclerView.setAdapter(adapter);
        } catch(JSONException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPremiums();
    }
}