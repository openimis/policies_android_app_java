package tz.co.exact.imis;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class OverViewPolicies extends AppCompatActivity {
    JSONArray policy;
    ClientAndroidInterface clientAndroidInterface;
    RecyclerView PolicyRecyclerView;
    OverViewPoliciesAdapter overViewPoliciesAdapter;


    public static List<String> num = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview_policies);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.OverViewPolicies));

        final String[] n = {""};
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                    n[0] = "";
                for(int i=0; i<num.size(); i++){
                    n[0] += num.get(i)+"\n";
                }
                trackBox(n[0]);
            }
        });

        clientAndroidInterface = new ClientAndroidInterface(this);
        fillRecordedPolicies();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void fillRecordedPolicies(){
        policy = clientAndroidInterface.getRecordedPolicies();//OrderArray;
        LayoutInflater li = LayoutInflater.from(OverViewPolicies.this);
        View promptsView = li.inflate(R.layout.activity_overview_policies, null);
        PolicyRecyclerView = (RecyclerView) findViewById(R.id.listofpolicies);
        overViewPoliciesAdapter = new OverViewPoliciesAdapter(this,policy);
        PolicyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        PolicyRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        PolicyRecyclerView.setAdapter(overViewPoliciesAdapter);
    }

    public void trackBox(String Number){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.controls, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView display = (TextView) promptsView.findViewById(R.id.display);

        display.setText(Number);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Get Control Number",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
