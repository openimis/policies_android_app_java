package cm.ynote.educash;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.exact.general.General;

import cm.ynote.educash.R;

public class Reports extends AppCompatActivity {
    Button SnapshotIndicators;
    Button CummulativeIndicators;

    General _General;
    private ClientAndroidInterface ca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _General = new General(AppInformation.DomainInfo.getDomain());
        ca = new ClientAndroidInterface(this);

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
        openReport(SnapshotIndicators.class);
    }

    public void openCummulativeIndicators(){
        openReport(CummulativeIndicators.class);
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
