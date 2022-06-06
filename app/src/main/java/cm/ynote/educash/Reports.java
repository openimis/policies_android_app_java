package cm.ynote.educash;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

<<<<<<< HEAD:app/src/main/java/cm/ynote/educash/Reports.java
import com.exact.general.General;

import cm.ynote.educash.R;

=======
>>>>>>> repo/main:app/src/main/java/org/openimis/imispolicies/Reports.java
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
