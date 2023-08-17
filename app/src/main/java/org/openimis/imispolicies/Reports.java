package org.openimis.imispolicies;

import android.content.Intent;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

public class Reports extends AppCompatActivity {
    Button SnapshotIndicators;
    Button CumulativeIndicators;

    private ClientAndroidInterface ca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        ca = new ClientAndroidInterface(this);

        SnapshotIndicators = findViewById(R.id.SnapshotIndicators);
        CumulativeIndicators = findViewById(R.id.CumulativeIndicators);

        SnapshotIndicators.setOnClickListener((view) -> openSnapshotIndicators());

        CumulativeIndicators.setOnClickListener((view) -> openCumulativeIndicators());
    }

    public void openSnapshotIndicators() {
        openReport(SnapshotIndicators.class);
    }

    public void openCumulativeIndicators() {
        openReport(CumulativeIndicators.class);
    }

    protected void openReport(Class<?> reportClass) {
        boolean isInternetAvailable = ca.CheckInternetAvailable();
        if (isInternetAvailable) {
            Intent i = new Intent(this, reportClass);
            startActivity(i);
        }
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
