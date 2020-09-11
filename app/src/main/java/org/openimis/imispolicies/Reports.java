package org.openimis.imispolicies;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.openimis.imispolicies.R;

public class Reports extends AppCompatActivity {
    Button SnapshotIndicators;
    Button CummulativeIndicators;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SnapshotIndicators = (Button) findViewById(R.id.SnapshotIndicators);
        CummulativeIndicators = (Button) findViewById(R.id.CummulativeIndicators);


        SnapshotIndicators.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSnapshotIndicators();
            }
        });

        CummulativeIndicators.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCummulativeIndicators();
            }
        });
    }

    public void openSnapshotIndicators(){
        Intent i = new Intent(this, SnapshotIndicators.class);
        startActivity(i);
    }
    public void openCummulativeIndicators(){
        Intent i = new Intent(this, CummulativeIndicators.class);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                super.onOptionsItemSelected(item);
        }
        return false;

    }
}
