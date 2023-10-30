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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.domain.entity.FeedbackRequest;
import org.openimis.imispolicies.usecase.FetchFeedback;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackList extends AppCompatActivity {

    private ClientAndroidInterface ca;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedbacks);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ca = new ClientAndroidInterface(this);
        EditText etFeedbackSearch = findViewById(R.id.etFeedbackSearch);
        lv = findViewById(R.id.lvFeedbacks);
        fillFeedbacks();

        SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        swipe.setColorSchemeResources(
                R.color.DarkBlue,
                R.color.Maroon,
                R.color.LightBlue,
                R.color.Red);

        swipe.setEnabled(false);

        swipe.setOnRefreshListener(() -> {
            swipe.setRefreshing(true);

            if (!ca.CheckInternetAvailable()) {
                swipe.setRefreshing(false);
                return;
            }

            (new Handler()).postDelayed(() -> {
                try {
                    if (Global.getGlobal().isLoggedIn()) {
                        RefreshFeedbacks();
                    } else {
                        ca.forceLoginDialogBox(() -> {
                            Toast.makeText(FeedbackList.this, FeedbackList.this.getResources().getString(R.string.Login_Successful), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(FeedbackList.this, FeedbackList.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                    swipe.setRefreshing(false);
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }

            }, 3000);
        });

        etFeedbackSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((SimpleAdapter) lv.getAdapter()).getFilter().filter(s);
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
            Intent intent = new Intent(getApplicationContext(), Feedback.class);
            HashMap<String, String> oItem;
            //noinspection unchecked
            oItem = (HashMap<String, String>) parent.getItemAtPosition(position);

            intent.putExtra("CHFID", oItem.get("CHFID"));
            intent.putExtra("ClaimUUID", oItem.get("ClaimUUID"));
            intent.putExtra("ClaimCode", oItem.get("ClaimCode"));
            intent.putExtra("OfficerCode", Global.getGlobal().getOfficerCode());
            startActivityForResult(intent, 0);
        });
    }

    @MainThread
    private void fillFeedbacks() {
        if (!ca.CheckInternetAvailable())
            return;

        String result = ca.getOfflineFeedBack(Global.getGlobal().getOfficerCode());
        List<Map<String, String>> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(result);
            if (jsonArray.length() == 0) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.NoFeedbackFound), Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    HashMap<String, String> feedback = new HashMap<>();
                    feedback.put("CHFID", object.getString("CHFID"));
                    feedback.put("FullName", object.getString("LastName") + " " + object.getString("OtherNames"));
                    feedback.put("HFName", object.getString("HFCode") + ":" + object.getString("HFName"));
                    feedback.put("ClaimCode", object.getString("ClaimCode"));
                    feedback.put("DateFromTo", object.getString("DateFrom") + " - " + object.getString("DateTo"));
                    feedback.put("FeedbackPromptDate", object.getString("FeedbackPromptDate"));
                    feedback.put("ClaimUUID", object.getString("ClaimUUID"));
                    list.add(feedback);
                }
            }

            lv.setAdapter(new SimpleAdapter(this, list, R.layout.feedbacklist,
                    new String[]{"CHFID", "FullName", "HFName", "ClaimCode", "DateFromTo", "FeedbackPromptDate"},
                    new int[]{R.id.tvCHFID, R.id.tvFullName, R.id.tvHFName, R.id.tvClaimCode, R.id.tvDates, R.id.tvTime}));

            setTitle("Feedbacks (" + list.size() + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getMasterDataText(String filename) {
        //ca.unZipFeedbacksRenewals(filename);
        String fname = filename.substring(0, filename.indexOf("."));
        String aBuffer = "";
        try {
            String dir = Global.getGlobal().getSubdirectory("Database");
            File myFile = new File(dir, fname);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            aBuffer = myReader.readLine();

            myReader.close();
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
            String path = uri.getPath();
            File f = new File(path);
            if (f.getName().toLowerCase().equals("feedback_" + Global.getGlobal().getOfficerCode().toLowerCase() + ".rar")) {
                String aBuffer = getMasterDataText((f.getName()));
                ConfirmDialogFeedbackRenewal(f.getName(), aBuffer);
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
                if (!Global.getGlobal().isNetworkAvailable()) {
                    ca.ShowDialog(getResources().getString(R.string.InternetRequired));
                    return false;
                }
                startActivity(Statistics.newIntent(this, "Feedback Statistics", Statistics.Type.FEEDBACK));
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return false;

    }

    private void ConfirmDialogFeedbackRenewal(String filename, String aBuffer) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                FeedbackList.this);
        alertDialog2.setTitle("Load file:");
        alertDialog2.setMessage(filename);
        alertDialog2.setPositiveButton("OK",
                (dialog, which) -> {
                    try {
                        if (ca.InsertFeedbacks(new JSONArray(aBuffer))) {
                            fillFeedbacks();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).setNegativeButton("Quit",
                (dialog, id) -> {
                    dialog.cancel();
                    finish();
                });
        alertDialog2.show();
    }

    public void openDialogForFeedbackRenewal() {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                FeedbackList.this);

        alertDialog2.setTitle("NO INTERNET CONNECTION");
        alertDialog2.setMessage("Do you want to import .txt file from your IMIS folder?");
        alertDialog2.setPositiveButton("Yes",
                (dialog, which) -> {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    try {
                        startActivityForResult(intent, 5);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "There are no file explorer clients installed.", Toast.LENGTH_SHORT).show();
                    }
                    // Write your code here to execute after dialog
                }).setNegativeButton("No",
                (dialog, id) -> {
                    dialog.cancel();
                    finish();
                });

// Showing Alert Dialog
        alertDialog2.show();
    }

    private void RefreshFeedbacks() throws IOException, XmlPullParserException {
        if (ca.CheckInternetAvailable()) {
            //   pd = ProgressDialog.show(this, "", getResources().getString(R.string.Loading));
            new Thread(() -> {
                try {
                    List<FeedbackRequest> feedbacks = new FetchFeedback().execute();
                    boolean IsInserted = ca.InsertFeedbacks(feedbacks);
                    if (!IsInserted) {
                        ca.ShowDialog(getResources().getString(R.string.ErrorInsert));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ca.ShowDialog(getResources().getString(R.string.ErrorOccurred) + ": " + e.getMessage());
                }

                runOnUiThread(this::fillFeedbacks);
            }).start();
        } else {
            openDialogForFeedbackRenewal();
        }
    }
}
