package org.openimis.imispolicies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class ControlNumbers extends AppCompatActivity {
    TextView OverViewPolicies;
    TextView OverViewControlNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_numbers);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.ControlNumbers));


        OverViewPolicies = (TextView) findViewById(R.id.OverViewPolicies);
        OverViewControlNumber = (TextView) findViewById(R.id.OverViewControlNumber);

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
