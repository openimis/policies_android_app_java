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

public class Enrolment extends AppCompatActivity {

    RecyclerView recyclerView;
    ClientAndroidInterface ca;
    String page = "families";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrolment);
        setTitle(getApplicationContext().getString(R.string.Families));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        ca = new ClientAndroidInterface(this);

        recyclerView = findViewById(R.id.recyclerFamilies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadFamilies();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFamilies();
    }

    private void loadFamilies() {
        String families = ca.getAllFamilies();
        try {
            JSONArray familyArray = new JSONArray(families);
            FamilyAdapter adapter = new FamilyAdapter(this,familyArray);
            recyclerView.setAdapter(adapter);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void onRefresh() {
        loadFamilies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Option 2: Fermer l'activité directement
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_add && page.equals("families")) {
            Intent intent = new Intent(this, FamilyActivity.class);
            intent.putExtra("familyId",0);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}