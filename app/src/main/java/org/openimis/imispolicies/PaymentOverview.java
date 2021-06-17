package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class PaymentOverview extends AppCompatActivity {
    TextView OverViewPolicies;
    TextView NotEnrolledPolicies;
    TextView OverViewControlNumber;
    TextView CheckCommission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_overview);
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.PaymentOverview));
        }

        OverViewPolicies = findViewById(R.id.OverViewPolicies);
        NotEnrolledPolicies = findViewById(R.id.NotEnrolledPolicies);
        OverViewControlNumber = findViewById(R.id.OverViewControlNumber);
        CheckCommission = findViewById(R.id.CheckCommission);



        OverViewPolicies.setOnClickListener(view -> openPage(SearchOverViewPolicies.class));
        NotEnrolledPolicies.setOnClickListener(view -> openPage(SearchNotEnrolledPolicies.class));
        OverViewControlNumber.setOnClickListener(view -> openPage(SearchOverViewControlNumber.class));
        CheckCommission.setOnClickListener(view -> openPage(CheckCommission.class));

    }

    public void openPage(Class<? extends AppCompatActivity> page)
    {
        Intent intent = new Intent(this, page);
        startActivity(intent);
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
