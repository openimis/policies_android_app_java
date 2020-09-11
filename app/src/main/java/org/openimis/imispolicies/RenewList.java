//Copyright (c) 2016-%CurrentYear% Swiss Agency for Development and Cooperation (SDC)
//
//The program users must agree to the following terms:
//
//Copyright notices
//This program is free software: you can redistribute it and/or modify it under the terms of the GNU AGPL v3 License as published by the 
//Free Software Foundation, version 3 of the License.
//This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU AGPL v3 License for more details www.gnu.org.
//
//Disclaimer of Warranty
//There is no warranty for the program, to the extent permitted by applicable law; except when otherwise stated in writing the copyright 
//holders and/or other parties provide the program "as is" without warranty of any kind, either expressed or implied, including, but not 
//limited to, the implied warranties of merchantability and fitness for a particular purpose. The entire risk as to the quality and 
//performance of the program is with you. Should the program prove defective, you assume the cost of all necessary servicing, repair or correction.
//
//Limitation of Liability 
//In no event unless required by applicable law or agreed to in writing will any copyright holder, or any other party who modifies and/or 
//conveys the program as permitted above, be liable to you for damages, including any general, special, incidental or consequential damages 
//arising out of the use or inability to use the program (including but not limited to loss of data or data being rendered inaccurate or losses 
//sustained by you or third parties or a failure of the program to operate with any other programs), even if such holder or other party has been 
//advised of the possibility of such damages.
//
//In case of dispute arising out or in relation to the use of the program, it is subject to the public law of Switzerland. The place of jurisdiction is Berne.

package org.openimis.imispolicies;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.exact.CallSoap.CallSoap;
import com.exact.general.General;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.openimis.imispolicies.R;

public class RenewList extends AppCompatActivity {
    private General _general = new General(AppInformation.DomainInfo.getDomain());
    String aBuffer = "";
    private ListView lv;
    private SwipeRefreshLayout swipe;
    private ArrayList<HashMap<String, String>> RenewalList = new ArrayList<>();
    private String OfficerCode;
    private ClientAndroidInterface ca;
    private Global global;
    private EditText etRenewalSearch;
    private ListAdapter adapter;
    public String UnlistedRenPolicy;

    boolean isUserLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ca = new ClientAndroidInterface(this);
        setContentView(R.layout.renewals);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        global = (Global) getApplicationContext();
        OfficerCode = global.getOfficerCode();
        UnlistedRenPolicy = getResources().getString(R.string.UnlistedRenewalPolicies);

        etRenewalSearch = (EditText) findViewById(R.id.etRenewalSearch);
        lv = (ListView) findViewById(R.id.lvRenewals);

        fillRenewals("");
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipe.setColorSchemeResources(
                R.color.DarkBlue,
                R.color.Maroon,
                R.color.LightBlue,
                R.color.Red);
        swipe.setEnabled(false);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Global global = new Global();
                            global = (Global) RenewList.this.getApplicationContext();

                            Token token = null;

                            try{
                                token = global.getJWTToken();
                            }catch (Exception e){}

                            if (_general.isNetworkAvailable(RenewList.this.getApplicationContext())) {
                                if(token != null){
                                    RefreshRenewals();
                                }else{
                                    LoginDialogBox("Renewals");
                                }
                            }else {
                                openDialogForFeedbackRenewal();
                            }

                            // FetchPayers();
                        } catch (IOException | XmlPullParserException e) {
                            e.printStackTrace();
                        }
                        swipe.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        etRenewalSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                ((SimpleAdapter) adapter).getFilter().filter(s);


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    swipe.setEnabled(true);
                else
                    swipe.setEnabled(false);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), Renewal.class);

                HashMap<String, String> oItem;
                //noinspection unchecked
                oItem = (HashMap<String, String>) parent.getItemAtPosition(position);
                intent.putExtra("CHFID", oItem.get("CHFID"));
                intent.putExtra("ProductCode", oItem.get("ProductCode"));
                intent.putExtra("RenewalId", oItem.get("RenewalId"));
                intent.putExtra("OfficerCode", OfficerCode);
                intent.putExtra("LocationId", oItem.get("LocationId"));
                intent.putExtra("PolicyValue", oItem.get("PolicyValue"));
                intent.putExtra("RenewalUUID", oItem.get("RenewalUUID"));
                startActivityForResult(intent, 0);

            }
        });

    }
    public void ConfirmDialogFeedbackRenewal(String filename) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                RenewList.this);

// Setting Dialog Title
        alertDialog2.setTitle("Load file:");
        alertDialog2.setMessage(filename);

// Setting Icon to Dialog
        // alertDialog2.setIcon(R.drawable.delete);

// Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(ca.InsertRenewals(aBuffer).equals("1")){
                            fillRenewals("");
                        }
                    }
                }).setNegativeButton("Quit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

// Showing Alert Dialog
        alertDialog2.show();
    }
    public String getMasterDataText(String filename){
        ca.unZipFeedbacksRenewals(filename);
        String fname = filename.substring(0, filename.indexOf("."));
        try {
            String dir = Environment.getExternalStorageDirectory() + File.separator + "IMIS/";
            File myFile = new File("/" + dir + "/" + fname + ".txt");//"/"+dir+"/MasterData.txt"
//            BufferedReader myReader = new BufferedReader(
//                    new InputStreamReader(
//                            new FileInputStream(myFile), "UTF32"));
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            aBuffer = myReader.readLine();

            myReader.close();
/*            Scanner in = new Scanner(new FileReader("/"+dir+"/MasterData.txt"));
            StringBuilder sb = new StringBuilder();
            while(in.hasNext()) {
                sb.append(in.next());
            }
            in.close();
            aBuffer = sb.toString();*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }
    public void openDialogForFeedbackRenewal() {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                RenewList.this);

// Setting Dialog Title
        alertDialog2.setTitle("NO INTERNET CONNECTION");
        alertDialog2.setMessage("Do you want to import .txt file from your IMIS folder?");

// Setting Icon to Dialog
        // alertDialog2.setIcon(R.drawable.delete);

// Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        try{
                            startActivityForResult(intent, 5);
                        } catch (ActivityNotFoundException e){
                            Toast.makeText(getApplicationContext(), "There are no file explorer clients installed.", Toast.LENGTH_SHORT).show();
                        }
                        // Write your code here to execute after dialog
                    }
                }).setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

// Showing Alert Dialog
        alertDialog2.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            fillRenewals("");
        }
        if(requestCode == 5 && resultCode == RESULT_OK){
            Uri uri = data.getData();
            String path = "";
            path = uri.getPath();
            File f = new File(path);

            if(f.getName().toString().toLowerCase().equals("renewal_"+global.getOfficerCode().toLowerCase()+".rar")){
                getMasterDataText((f.getName()).toString());
                ConfirmDialogFeedbackRenewal((f.getName()).toString());
            }else{
                Toast.makeText(this, getResources().getString(R.string.FileDoesntBelongHere), Toast.LENGTH_LONG).show();
            }

        }
    }
    private void fillRenewals() {
        RenewalList.clear();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        ClientAndroidInterface ca = new ClientAndroidInterface(this);
        String result = ca.OfflineRenewals(OfficerCode);
        JSONArray jsonArray = null;
        JSONObject object;

        try {
            RenewalList.clear();
            jsonArray = new JSONArray(result);
            HashMap<String, String> Renewal;
            Renewal = new HashMap<>();
            Renewal.put("RenewalId", "0");
            Renewal.put("CHFID", UnlistedRenPolicy);
            Renewal.put("FullName",getResources().getString(R.string.RenewYourPolicy));
            Renewal.put("Product", getResources().getString(R.string.Product));
            Renewal.put("VillageName", "");
            Renewal.put("PolicyValue", "");
            Renewal.put("PolicyId", "");
            Renewal.put("ProductCode", "");
            Renewal.put("LocationId", String.valueOf(ca.getLocationId(OfficerCode)));
            Renewal.put("EnrollDate", d);
            Renewal.put("RenewalUUID", "");
            RenewalList.add(Renewal);
            if (jsonArray.length() == 0) {
                //RenewalList.clear();
                Toast.makeText(this, getResources().getString(R.string.NoRenewalFound), Toast.LENGTH_LONG).show();
            } else {
                RenewalList.clear();
                Renewal = new HashMap<>();
                Renewal.put("RenewalId", "0");
                Renewal.put("CHFID", UnlistedRenPolicy);
                Renewal.put("FullName",getResources().getString(R.string.RenewYourPolicy));
                Renewal.put("Product", getResources().getString(R.string.Product));
                Renewal.put("VillageName", "");
                Renewal.put("PolicyValue", "");
                Renewal.put("PolicyId", "");
                Renewal.put("ProductCode", "");
                Renewal.put("LocationId", String.valueOf(ca.getLocationId(OfficerCode)));
                Renewal.put("EnrollDate", d);
                Renewal.put("RenewalUUID", "");
                RenewalList.add(Renewal);
                for (int i = 0; i < jsonArray.length(); i++) {

                    object = jsonArray.getJSONObject(i);

                    Renewal = new HashMap<>();
                    Renewal.put("RenewalId", object.getString("RenewalId"));
                    Renewal.put("CHFID", object.getString("CHFID"));
                    Renewal.put("FullName", object.getString("LastName") + " " + object.getString("OtherNames"));
                    Renewal.put("Product", object.getString("ProductCode") + " : " + object.getString("ProductName"));
                    Renewal.put("VillageName", object.getString("VillageName"));
                    Renewal.put("EnrollDate", object.getString("EnrollDate"));
                    Renewal.put("PolicyId", object.getString("PolicyId"));
                    Renewal.put("ProductCode", object.getString("ProductCode"));
                    Renewal.put("LocationId", object.getString("LocationId"));
                    Renewal.put("PolicyValue", object.getString("PolicyValue"));
                    Renewal.put("RenewalUUID", object.getString("RenewalUUID"));
                    RenewalList.add(Renewal);
                }
            }


            adapter = new SimpleAdapter(this, RenewalList, R.layout.renewallist,
                    new String[]{"CHFID", "FullName", "Product", "VillageName", "EnrollDate"},
                    new int[]{R.id.tvCHFID, R.id.tvFullName, R.id.tvProduct, R.id.tvVillage, R.id.tvTime});

            lv.setAdapter(adapter);

            setTitle("Renewals (" + String.valueOf(lv.getCount()) + ")");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void fillRenewals(String label) {
        RenewalList.clear();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        ClientAndroidInterface ca = new ClientAndroidInterface(this);
        String result = ca.OfflineRenewals(OfficerCode);
        JSONArray jsonArray = null;
        JSONObject object;

        try {
            RenewalList.clear();
            jsonArray = new JSONArray(result);
            HashMap<String, String> Renewal;
            Renewal = new HashMap<>();
            Renewal.put("RenewalId", "0");
            Renewal.put("CHFID", UnlistedRenPolicy);
            Renewal.put("FullName",getResources().getString(R.string.RenewYourPolicy));
            Renewal.put("Product", getResources().getString(R.string.Product));
            Renewal.put("VillageName", "");
            Renewal.put("PolicyValue", "");
            Renewal.put("PolicyId", "");
            Renewal.put("ProductCode", "");
            Renewal.put("LocationId", String.valueOf(ca.getLocationId(OfficerCode)));
            Renewal.put("EnrollDate", d);
            Renewal.put("RenewalUUID", "");
            RenewalList.add(Renewal);
            if (jsonArray.length() == 0) {
                //RenewalList.clear();
                //Toast.makeText(this, getResources().getString(R.string.NoRenewalFound), Toast.LENGTH_LONG).show();
            } else {
                RenewalList.clear();
                Renewal = new HashMap<>();
                Renewal.put("RenewalId", "0");
                Renewal.put("CHFID", UnlistedRenPolicy);
                Renewal.put("FullName",getResources().getString(R.string.RenewYourPolicy));
                Renewal.put("Product", getResources().getString(R.string.Product));
                Renewal.put("VillageName", "");
                Renewal.put("PolicyValue", "");
                Renewal.put("PolicyId", "");
                Renewal.put("ProductCode", "");
                Renewal.put("LocationId", String.valueOf(ca.getLocationId(OfficerCode)));
                Renewal.put("EnrollDate", d);
                Renewal.put("RenewalUUID", "");
                RenewalList.add(Renewal);

                for (int i = 0; i < jsonArray.length(); i++) {

                    object = jsonArray.getJSONObject(i);

                    Renewal = new HashMap<>();
                    Renewal.put("RenewalId", object.getString("RenewalId"));
                    Renewal.put("CHFID", object.getString("CHFID"));
                    Renewal.put("FullName", object.getString("LastName") + " " + object.getString("OtherNames"));
                    Renewal.put("Product", object.getString("ProductCode") + " : " + object.getString("ProductName"));
                    Renewal.put("VillageName", object.getString("VillageName"));
                    Renewal.put("EnrollDate", object.getString("EnrollDate"));
                    Renewal.put("PolicyId", object.getString("PolicyId"));
                    Renewal.put("ProductCode", object.getString("ProductCode"));
                    Renewal.put("LocationId", object.getString("LocationId"));
                    Renewal.put("PolicyValue", object.getString("PolicyValue"));
                    Renewal.put("RenewalUUID", object.getString("RenewalUUID"));
                    RenewalList.add(Renewal);
                }
            }

            adapter = new SimpleAdapter(this, RenewalList, R.layout.renewallist,
                    new String[]{"CHFID", "FullName", "Product", "VillageName", "EnrollDate"},
                    new int[]{R.id.tvCHFID, R.id.tvFullName, R.id.tvProduct, R.id.tvVillage, R.id.tvTime});

            lv.setAdapter(adapter);

            setTitle("Renewals (" + String.valueOf(lv.getCount()) + ")");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void RefreshRenewals() throws IOException, XmlPullParserException {
        if (_general.isNetworkAvailable(this)) {
            new Thread() {
                public void run() {
                    String result = null;

                    try {
                        ToRestApi rest = new ToRestApi();
                        result = rest.getObjectFromRestApiToken("policy");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (result != null) {
                        ca.InsertRenewals(result);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fillRenewals();
                        }
                    });
                }
            }.start();

        } else {
            openDialogForFeedbackRenewal();
            //Toast.makeText(this, getResources().getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.mnuStatistics:
                Intent stats = new Intent(this, Statistics.class);
                stats.putExtra("Title", "Renewal Statistics");
                stats.putExtra("Caller", "R");
                startActivity(stats);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void LoginDialogBox(final String page){

        final int[] userid = {0};

        Global global = (Global) RenewList.this.getApplicationContext();
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView username = (TextView) promptsView.findViewById(R.id.UserName);
        final TextView password = (TextView) promptsView.findViewById(R.id.Password);

        username.setText(global.getOfficerCode().toString());

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if(!username.getText().toString().equals("") && !password.getText().toString().equals("")){

                                    new Thread() {
                                        public void run() {

                                            try {
                                                isUserLogged = ca.LoginToken(username.getText().toString(),password.getText().toString());
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(isUserLogged){
                                                        if(page.equals("Renewals")){
                                                            finish();
                                                            Intent intent = new Intent(RenewList.this, RenewList.class);
                                                            startActivity(intent);
                                                            Toast.makeText(RenewList.this,RenewList.this.getResources().getString(R.string.Login_Successful),Toast.LENGTH_LONG).show();
                                                        }

                                                    }else{
                                                        Toast.makeText(RenewList.this,RenewList.this.getResources().getString(R.string.LoginFail),Toast.LENGTH_LONG).show();
                                                        LoginDialogBox(page);
                                                        //ca.ShowDialog(RenewList.this.getResources().getString(R.string.LoginFail));
                                                    }
                                                }
                                            });

                                        }
                                    }.start();


                                }else{
                                    Toast.makeText(RenewList.this,RenewList.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                                    LoginDialogBox(page);
                                }
                            }
                        })
                .setNegativeButton(R.string.Cancel,
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
