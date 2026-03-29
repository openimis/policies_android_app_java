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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.tools.Log;

import java.util.HashMap;

public class FamilyInsurees extends AppCompatActivity {

    public static int familyId;
    TextView regionName, districtName, wardName, villageName;
    ClientAndroidInterface ca;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    public static int regionId;
    public static int districtId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_insurees);
        setTitle(getResources().getString(R.string.FamilyAndInsurees));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        ca = new ClientAndroidInterface(this);
        familyId = getIntent().getIntExtra("FamilyId", 0);
        initViews();
        if(familyId != 0){
            LoadFamilyHeader(familyId);
        }

        PagerAdapter adapter = new PagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getTitle(position))
        ).attach();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void LoadFamilyHeader(int familyId){
        String FamilyHeader = ca.getFamilyHeader(familyId);
        bindDataFromDatafield(FamilyHeader);
    }

    private void bindDataFromDatafield(String data){
        try {
            JSONArray array = new JSONArray(data);
            JSONObject object = array.getJSONObject(0);
            regionId = object.getInt("RegionId");
            districtId = object.getInt("DistrictId");
            regionName.setText(object.getString("RegionName"));
            districtName.setText(object.getString("DistrictName"));
            wardName.setText(object.getString("WardName"));
            villageName.setText(object.getString("VillageName"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void initViews() {
        regionName = findViewById(R.id.RegionName);
        districtName = findViewById(R.id.DistrictName);
        wardName = findViewById(R.id.WardName);
        villageName = findViewById(R.id.VillageName);
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