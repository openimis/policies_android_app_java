package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

public class Enrolment extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton btnAdd;
    ClientAndroidInterface ca;

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
        btnAdd = findViewById(R.id.btnAddNew);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadFamilies();
        btnAdd.setOnClickListener(v -> {

            Intent intent = new Intent(this, FamilyActivity.class);
            intent.putExtra("familyId",0);
            startActivity(intent);
        });
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Option 2: Fermer l'activité directement
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}