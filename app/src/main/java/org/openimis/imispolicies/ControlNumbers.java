package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.openimis.imispolicies.R;

public class ControlNumbers extends AppCompatActivity {
    TextView OverViewPolicies;
    TextView OverViewControlNumber;
    TextView CheckCommission;
    TextView NotEnrolledPolicies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_numbers);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.Overviews));


        OverViewPolicies = (TextView) findViewById(R.id.OverViewPolicies);
        OverViewControlNumber = (TextView) findViewById(R.id.OverViewControlNumber);
        CheckCommission = (TextView) findViewById(R.id.CheckCommission);
        NotEnrolledPolicies = (TextView) findViewById(R.id.NotEnrolledPolicies);


        OverViewPolicies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ControlNumbers.this, SearchOverViewPolicies.class);
                startActivity(intent);
            }
        });
        OverViewControlNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ControlNumbers.this, SearchOverViewControlNumber.class);
                startActivity(intent);
            }
        });

        CheckCommission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ControlNumbers.this, CheckCommission.class);
                startActivity(intent);
            }
        });

        NotEnrolledPolicies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ControlNumbers.this, SearchNotEnrolledPolicies.class);
                startActivity(intent);
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
