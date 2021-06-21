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
import android.content.Context;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.openimis.imispolicies.R;

public class FeedbackList extends AppCompatActivity {
    private ListView lv;
    private SwipeRefreshLayout swipe;
    private ArrayList<HashMap<String, String>> FeedbackList = new ArrayList<>();
    private String OfficerCode;
    String aBuffer = "";

    private General _general = new General(AppInformation.DomainInfo.getDomain());
    private ClientAndroidInterface ca;
    private Global global;
    private EditText etFeedbackSearch;
    private ListAdapter adapter;

    boolean isUserLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedbacks);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        global = (Global) getApplicationContext();
        OfficerCode = global.getOfficerCode();
        ca = new ClientAndroidInterface(this);
        etFeedbackSearch = (EditText) findViewById(R.id.etFeedbackSearch);
        lv = (ListView) findViewById(R.id.lvFeedbacks);
        fillFeedbacks();

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

                if (!ca.CheckInternetAvailable()) {
                    swipe.setRefreshing(false);
                    return;
                }

                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            swipe.setRefreshing(false);
                            Global global = new Global();
                            global = (Global) FeedbackList.this.getApplicationContext();

                            Token token = null;

                            try {
                                token = global.getJWTToken();
                            } catch (Exception e) {
                            }

                            if (token != null) {
                                RefreshFeedbacks();
                            } else {
                                LoginDialogBox("Feedbacks");
                            }

                        } catch (IOException | XmlPullParserException e) {
                            e.printStackTrace();
                        }

                    }
                }, 3000);
            }
        });

        etFeedbackSearch.addTextChangedListener(new TextWatcher() {
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

                String claimUUID = "";

                Intent intent = new Intent(getApplicationContext(), Feedback.class);
                HashMap<String, String> oItem;
                //noinspection unchecked
                oItem = (HashMap<String, String>) parent.getItemAtPosition(position);

                try {
                    claimUUID = ca.getClaimUUIDByClaimCode(oItem.get("ClaimCode"))
                            .getJSONObject(0)
                            .getString("ClaimUUID");
                } catch (Exception e) {
                }

                intent.putExtra("CHFID", oItem.get("CHFID"));
                intent.putExtra("ClaimId", oItem.get("ClaimId"));
                intent.putExtra("ClaimUUID", claimUUID);
                intent.putExtra("ClaimCode", oItem.get("ClaimCode"));
                intent.putExtra("OfficerCode", OfficerCode);
                startActivityForResult(intent, 0);
            }
        });
    }

    private void fillFeedbacks() {
        if (!ca.CheckInternetAvailable())
            return;

        String result = ca.getOfflineFeedBack(OfficerCode);

        JSONArray jsonArray = null;
        JSONObject object;

        try {
            jsonArray = new JSONArray(result);
            if (jsonArray.length() == 0) {
                FeedbackList.clear();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.NoFeedbackFound), Toast.LENGTH_LONG).show();
            } else {
                FeedbackList.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getJSONObject(i);

                    HashMap<String, String> feedback = new HashMap<>();
                    feedback.put("CHFID", object.getString("CHFID"));
                    feedback.put("FullName", object.getString("LastName") + " " + object.getString("OtherNames"));
                    feedback.put("HFName", object.getString("HFCode") + ":" + object.getString("HFName"));
                    feedback.put("ClaimCode", object.getString("ClaimCode"));
                    feedback.put("DateFromTo", object.getString("DateFrom") + " - " + object.getString("DateTo"));
                    feedback.put("FeedbackPromptDate", object.getString("FeedbackPromptDate"));
                    feedback.put("ClaimId", object.getString("ClaimId"));
                    FeedbackList.add(feedback);
                }


            }
            adapter = new SimpleAdapter(this, FeedbackList, R.layout.feedbacklist,
                    new String[]{"CHFID", "FullName", "HFName", "ClaimCode", "DateFromTo", "FeedbackPromptDate"},
                    new int[]{R.id.tvCHFID, R.id.tvFullName, R.id.tvHFName, R.id.tvClaimCode, R.id.tvDates, R.id.tvTime});

            lv.setAdapter(adapter);


            setTitle("Feedbacks (" + String.valueOf(lv.getCount()) + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getMasterDataText(String filename) {
        ca.unZipFeedbacksRenewals(filename);
        String fname = filename.substring(0, filename.indexOf("."));
        try {
            String dir = global.getSubdirectory("Database");
            File myFile = new File(dir, fname);//"/"+dir+"/MasterData.txt"
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            fillFeedbacks();
        }
        if (requestCode == 5 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = "";
            path = uri.getPath();
            File f = new File(path);
            if (f.getName().toString().toLowerCase().equals("feedback_" + global.getOfficerCode().toLowerCase() + ".rar")) {
                getMasterDataText((f.getName()).toString());
                ConfirmDialogFeedbackRenewal((f.getName()).toString());
            } else {
                Toast.makeText(this, getResources().getString(R.string.FileDoesntBelongHere), Toast.LENGTH_LONG).show();
            }
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
                if (!_general.isNetworkAvailable(this)) {
                    ca.ShowDialog(getResources().getString(R.string.InternetRequired));
                    return false;
                }
                Intent Stats = new Intent(this, Statistics.class);
                Stats.putExtra("Title", "Feedback Statistics");
                Stats.putExtra("Caller", "F");
                startActivity(Stats);
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return false;

    }

    public void ConfirmDialogFeedbackRenewal(String filename) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                FeedbackList.this);

// Setting Dialog Title
        alertDialog2.setTitle("Load file:");
        alertDialog2.setMessage(filename);

// Setting Icon to Dialog
        // alertDialog2.setIcon(R.drawable.delete);

// Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (ca.InsertFeedbacks(aBuffer)) {
                            fillFeedbacks();
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

    public void openDialogForFeedbackRenewal() {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                FeedbackList.this);

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
                        try {
                            startActivityForResult(intent, 5);
                        } catch (ActivityNotFoundException e) {
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

    private void RefreshFeedbacks() throws IOException, XmlPullParserException {
        if (ca.CheckInternetAvailable()) {
            //   pd = ProgressDialog.show(this, "", getResources().getString(R.string.Loading));
            new Thread() {
                public void run() {
                    String result = null;

                    try {
                        ToRestApi rest = new ToRestApi();
                        result = rest.getObjectFromRestApiToken("feedback");

                        if (result.equalsIgnoreCase("[]") || result == null) {
                            FeedbackList.clear();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Boolean IsInserted = ca.InsertFeedbacks(result);

                    if (!IsInserted) {
                        ca.ShowDialog(getResources().getString(R.string.ErrorOccurred));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fillFeedbacks();
                        }
                    });

                    //  pd.dismiss();
                    //   swipe.setRefreshing(false);

                }
            }.start();

            //fillFeedbacks();
        } else {
            openDialogForFeedbackRenewal();
            //Toast.makeText(this, getResources().getString(R.string.NoInternet), Toast.LENGTH_LONG).show();
        }
    }

    public void LoginDialogBox(final String page) {
        if (!ca.CheckInternetAvailable())
            return;

        final int[] userid = {0};

        Global global = (Global) FeedbackList.this.getApplicationContext();
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView username = (TextView) promptsView.findViewById(R.id.UserName);
        final TextView password = (TextView) promptsView.findViewById(R.id.Password);

        username.setText(global.getOfficerCode().toString());

        ca = new ClientAndroidInterface(this);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.Ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!username.getText().toString().equals("") && !password.getText().toString().equals("")) {

                                    new Thread() {
                                        public void run() {
                                            isUserLogged = ca.LoginToken(username.getText().toString(), password.getText().toString());

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isUserLogged) {
                                                        if (page.equals("Feedbacks")) {
                                                            finish();
                                                            Intent intent = new Intent(FeedbackList.this, FeedbackList.class);
                                                            startActivity(intent);
                                                            Toast.makeText(FeedbackList.this, FeedbackList.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                                                        }

                                                    } else {
                                                        Toast.makeText(FeedbackList.this, FeedbackList.this.getResources().getString(R.string.LoginFail), Toast.LENGTH_LONG).show();
                                                        LoginDialogBox(page);
                                                        //ca.ShowDialog(FeedbackList.this.getResources().getString(R.string.LoginFail));
                                                    }
                                                }
                                            });

                                        }
                                    }.start();


                                } else {
                                    Toast.makeText(FeedbackList.this, FeedbackList.this.getResources().getString(R.string.Enter_Credentials), Toast.LENGTH_LONG).show();
                                    LoginDialogBox(page);
                                }
                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
