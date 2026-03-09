package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
    JSONArray families = new JSONArray();
    ClientAndroidInterface ca;
    TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrolment);
        setTitle(getApplicationContext().getString(R.string.Families));
        ca = new ClientAndroidInterface(this);

        recyclerView = findViewById(R.id.recyclerFamilies);
        btnAdd = findViewById(R.id.btnAddNew);
        tvEmpty = findViewById(R.id.tvEmptyFamily);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            families = loadFamilies();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        tvEmpty.setVisibility(families.length() == 0 ? TextView.VISIBLE : TextView.GONE);

        FamilyAdapter adapter = new FamilyAdapter(this,families);
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {

            Intent intent = new Intent(this, FamilyActivity.class);
            intent.putExtra("familyId",0);
            startActivity(intent);

        });

    }

    private JSONArray loadFamilies() throws JSONException {
        String families = ca.getAllFamilies();
        return new JSONArray(families);
    }
}