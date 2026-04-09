package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

public class PolicyPremiumsActivity extends AppCompatActivity {

    ClientAndroidInterface ca;
    RecyclerView recyclerView;
    FloatingActionButton btnAdd;
    private int familyId, policyId, regionId, districtId;

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
        policyId = getIntent().getIntExtra("RegionId", 0);
        policyId = getIntent().getIntExtra("DistrictId", 0);
        initViews();
        setupListenners();
        loadPremiums();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews(){
        btnAdd = findViewById(R.id.btnAddNewPremiums);
        recyclerView = findViewById(R.id.recyclerPremiums);
    }

    private void setupListenners(){
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, PremiumActivity.class);
            intent.putExtra("FamilyId", familyId);
            intent.putExtra("PolicyId", policyId);
            intent.putExtra("RegionId", regionId);
            intent.putExtra("DistrictId", regionId);
            intent.putExtra("PremiumId", 0);
            startActivity(intent);
        });
    }

    private void loadPremiums() {
        String premiums = ca.getPremiums(policyId);
        try{
            JSONArray premiumArray = new JSONArray(premiums);
            PremiumAdapter adapter = new PremiumAdapter(this,premiumArray);
            recyclerView.setAdapter(adapter);
        } catch(JSONException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPremiums();
    }
}