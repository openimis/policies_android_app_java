package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.tools.Log;

import java.util.HashMap;

public class FamilyInsurees extends AppCompatActivity {

    private int familyId;
    RecyclerView recyclerInsurees;
    FloatingActionButton btnAddInsuree;
    TextView regionName, districtName, wardName, villageName;
    ClientAndroidInterface ca;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_insurees);
        setTitle(getResources().getString(R.string.FamilyAndInsurees));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ca = new ClientAndroidInterface(this);
        familyId = getIntent().getIntExtra("FamilyId", 0);
        initViews();
        recyclerInsurees.setLayoutManager(new LinearLayoutManager(this));

        if(familyId != 0){
            LoadFamilyHeader(familyId);
            LoadInsurees(familyId);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadInsurees(familyId);
    }

    private void LoadFamilyHeader(int familyId){
        String FamilyHeader = ca.getFamilyHeader(familyId);
        bindDataFromDatafield(FamilyHeader);
    }

    private void LoadInsurees(int familyId) {
        String Insurees = ca.getInsureesForFamily(familyId);
        try {
            JSONArray insureeArray = new JSONArray(Insurees);
            InsureeAdapter adapter = new InsureeAdapter(this,insureeArray, familyId);
            recyclerInsurees.setAdapter(adapter);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void bindDataFromDatafield(String data){
        try {
            JSONArray array = new JSONArray(data);
            JSONObject object = array.getJSONObject(0);
            regionName.setText(object.getString("RegionName"));
            districtName.setText(object.getString("DistrictName"));
            wardName.setText(object.getString("WardName"));
            villageName.setText(object.getString("VillageName"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void initViews() {
        recyclerInsurees = findViewById(R.id.recyclerInsurees);
        regionName = findViewById(R.id.RegionName);
        districtName = findViewById(R.id.DistrictName);
        wardName = findViewById(R.id.WardName);
        villageName = findViewById(R.id.VillageName);

        btnAddInsuree = findViewById(R.id.btnNewInsuree);
        btnAddInsuree.setOnClickListener(v -> {
            Intent intent = new Intent(this, InsureeActivity.class);
            intent.putExtra("FamilyId", familyId);
            //intent.putExtra("InsureeId", insureeId);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}