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

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;

import org.openimis.imispolicies.tools.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import cz.msebera.android.httpclient.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.util.FileUtils;
import org.openimis.imispolicies.util.UriUtils;

import java.io.File;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class RenewList extends AppCompatActivity {
    private static final String LOG_TAG = "RENEWAL";
    private static final int REQUEST_IMPORT_RENEWAL_FILE = 1;
    private Global global;
    private ListView lv;
    private SwipeRefreshLayout swipe;
    private ArrayList<HashMap<String, String>> renewalList = new ArrayList<>();
    private String OfficerCode;
    private ClientAndroidInterface ca;
    private ListAdapter adapter;
    public String UnlistedRenPolicy;
    boolean isUserLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global = (Global) getApplicationContext();
        ca = new ClientAndroidInterface(this);
        setContentView(R.layout.renewals);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        OfficerCode = global.getOfficerCode();
        UnlistedRenPolicy = getResources().getString(R.string.UnlistedRenewalPolicies);

        EditText etRenewalSearch = findViewById(R.id.etRenewalSearch);
        lv = findViewById(R.id.lvRenewals);

        swipe = findViewById(R.id.swipe);
        swipe.setColorSchemeResources(
                R.color.DarkBlue,
                R.color.Maroon,
                R.color.LightBlue,
                R.color.Red);
        swipe.setEnabled(false);
        swipe.setOnRefreshListener(() -> {
            swipe.setRefreshing(true);
            new Handler().postDelayed(() -> {
                if (global.isNetworkAvailable()) {
                    if (global.isLoggedIn()) {
                        refreshRenewals();
                    } else {
                        loginDialogBox(this::refreshRenewals);
                    }
                } else {
                    requestImportRenewal();
                }

                swipe.setRefreshing(false);
            }, 1000);
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
                swipe.setEnabled(firstVisibleItem == 0);
            }
        });

        lv.setOnItemClickListener((parent, view, position, id) -> {
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
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillRenewals();
    }

    public void confirmImportRenewal(String filename, Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.LoadFile)
                .setMessage(filename)
                .setPositiveButton(R.string.Yes,
                        (dialog, which) -> new Thread(() -> {
                            File renewalFile = copyRenewalFile(uri);
                            if (renewalFile != null) {
                                ca.unZipFeedbacksRenewals(renewalFile);
                                File[] unzippedFiles = null;
                                File renewalDir = renewalFile.getParentFile();
                                if (renewalDir != null) {
                                    unzippedFiles = renewalDir.listFiles((file) -> file.isFile() && !file.equals(renewalFile));
                                }
                                if (unzippedFiles != null) {
                                    if (unzippedFiles.length == 0) {
                                        Log.w(LOG_TAG, "No renewal files after unpacking");
                                    }
                                    for (File f : unzippedFiles) {
                                        String renewals = loadRenewalFile(f);
                                        if (renewals != null) {
                                            ca.InsertRenewalsFromExtract(renewals);
                                        }
                                    }
                                    runOnUiThread(this::fillRenewals);
                                    FileUtils.deleteFiles(unzippedFiles);
                                }
                                FileUtils.deleteFile(renewalFile);
                            }
                        }).start())
                .setNegativeButton(R.string.No,
                        (dialog, id) -> dialog.cancel())
                .show();
    }

    private String loadRenewalFile(File file) {
        if (file.exists()) {
            return FileUtils.readFileAsUTF8String(file);
        } else {
            Log.e(LOG_TAG, "Unpacked renewal file does not exists");
            return null;
        }
    }

    public void NoRenewalsFoundDialog() {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                RenewList.this);

        alertDialog2.setTitle("Alert")
                .setMessage(getResources().getString(R.string.NoRenewalFound))
                .setPositiveButton("OK", null)
                .create()
                .show();
    }

    private File copyRenewalFile(Uri uri) {
        File copiedFile = UriUtils.copyUriContentToCache(this, uri, "imports/renewal/renewal.rar");
        if (copiedFile != null) {
            return copiedFile;
        } else {
            Log.e(LOG_TAG, "Copying renewal file failed, uri: " + uri);
            return null;
        }
    }

    public void requestImportRenewal() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.NoInternetTitle)
                .setMessage(R.string.ImportRenewalFile)
                .setPositiveButton(R.string.Yes,
                        (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            try {
                                startActivityForResult(intent, REQUEST_IMPORT_RENEWAL_FILE);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.NoFileExporerInstalled), Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton(R.string.No,
                        (dialog, id) -> dialog.cancel())
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT_RENEWAL_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String filename = UriUtils.getDisplayName(this, uri);
            String expectedFilename = String.format("renewal_%s.rar", global.getOfficerCode().toLowerCase());

            if (filename != null && filename.toLowerCase().equals(expectedFilename)) {
                confirmImportRenewal(filename, uri);
            } else {
                Toast.makeText(this, getResources().getString(R.string.FileDoesntBelongHere), Toast.LENGTH_LONG).show();
            }

        }
    }

    private void fillRenewals() {
        renewalList.clear();
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        ClientAndroidInterface ca = new ClientAndroidInterface(this);
        String result = ca.OfflineRenewals(OfficerCode);
        JSONObject object;

        try {
            HashMap<String, String> renewal;
            JSONArray jsonArray = new JSONArray(result);

            renewalList.clear();
            renewal = new HashMap<>();
            renewal.put("RenewalId", "0");
            renewal.put("CHFID", UnlistedRenPolicy);
            renewal.put("FullName", getResources().getString(R.string.RenewYourPolicy));
            renewal.put("Product", getResources().getString(R.string.Product));
            renewal.put("VillageName", "");
            renewal.put("PolicyValue", "");
            renewal.put("PolicyId", "");
            renewal.put("ProductCode", "");
            renewal.put("LocationId", String.valueOf(ca.getLocationId(OfficerCode)));
            renewal.put("RenewalPromptDate", d);
            renewal.put("RenewalUUID", "");
            renewalList.add(renewal);

            if (jsonArray.length()==0) {
                this.NoRenewalsFoundDialog();
            }
            for (int i = 0; i < jsonArray.length(); i++) {

                object = jsonArray.getJSONObject(i);
                renewal = new HashMap<>();
                renewal.put("RenewalId", object.getString("RenewalId"));
                renewal.put("CHFID", object.getString("CHFID"));
                renewal.put("FullName", object.getString("LastName") + " " + object.getString("OtherNames"));
                renewal.put("Product", object.getString("ProductCode") + " : " + object.getString("ProductName"));
                renewal.put("VillageName", object.getString("VillageName"));
                renewal.put("RenewalPromptDate", object.getString("RenewalPromptDate"));
                renewal.put("PolicyId", object.getString("PolicyId"));
                renewal.put("ProductCode", object.getString("ProductCode"));
                renewal.put("LocationId", object.getString("LocationId"));
                renewal.put("PolicyValue", object.getString("PolicyValue"));
                renewal.put("RenewalUUID", object.getString("RenewalUUID"));
                renewalList.add(renewal);
            }

            adapter = new SimpleAdapter(this, renewalList, R.layout.renewallist,
                    new String[]{"CHFID", "FullName", "Product", "VillageName", "RenewalPromptDate"},
                    new int[]{R.id.tvCHFID, R.id.tvFullName, R.id.tvProduct, R.id.tvVillage, R.id.tvTime});

            lv.setAdapter(adapter);

            setTitle("Renewals (" + lv.getCount() + ")");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void refreshRenewals() {
        if (global.isNetworkAvailable()) {
            if (global.isLoggedIn()) {
                new Thread(() -> {
                    String result = null;
                    int responseCode = 0;
                    try {
                        ToRestApi rest = new ToRestApi();
                        HttpResponse response = rest.getFromRestApiToken("policy");
                        responseCode = response.getStatusLine().getStatusCode();
                        result = rest.getContent(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (responseCode == HttpURLConnection.HTTP_OK && result != null) {
                        ca.InsertRenewalsFromApi(result);
                        runOnUiThread(this::fillRenewals);
                    } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        Toast.makeText(this, getResources().getString(R.string.LogInToDownloadRenewals), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.SomethingWrongServer), Toast.LENGTH_LONG).show();
                    }
                }
                ).start();
            } else {
                Toast.makeText(this, getResources().getString(R.string.LogInToDownloadRenewals), Toast.LENGTH_LONG).show();
            }
        } else {
            requestImportRenewal();
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
        onBackPressed();
        return true;
    }

    public void loginDialogBox(Runnable onLoggedIn) {
        if (!global.isNetworkAvailable())
            return;

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);

        final TextView username = promptsView.findViewById(R.id.UserName);
        final TextView password = promptsView.findViewById(R.id.Password);

        username.setText(global.getOfficerCode());

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.Ok,
                        (dialog, id) -> {
                            if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {
                                new Thread(() -> {
                                    isUserLogged = ca.LoginToken(username.getText().toString(), password.getText().toString());
                                    runOnUiThread(() -> {
                                        if (isUserLogged) {
                                            onLoggedIn.run();
                                        } else {
                                            Toast.makeText(RenewList.this, RenewList.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                            loginDialogBox(onLoggedIn);
                                        }
                                    });
                                }).start();
                            } else {
                                Toast.makeText(RenewList.this, RenewList.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                                loginDialogBox(onLoggedIn);
                            }
                        })
                .setNegativeButton(R.string.Cancel, (dialog, id) -> dialog.cancel())
                .show();
    }
}
