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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import org.openimis.imispolicies.tools.Log;

import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.util.Base64;

import com.squareup.picasso.Picasso;

import cz.msebera.android.httpclient.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.util.JsonUtils;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class Enquire extends AppCompatActivity {
    private static final String LOG_TAG = "ENQUIRE";
    private static final int REQUEST_SCAN_QR_CODE = 1;
    private Global global;
    private Escape escape;
    private Picasso picasso;
    private ClientAndroidInterface ca;
    private EditText etCHFID;
    private TextView tvCHFID;
    private TextView tvName;
    private TextView tvGender;
    private TextView tvDOB;
    private TextView tvPolicyStatus;
    private ListView lv;
    private ImageView iv;
    private LinearLayout ll;
    private ProgressDialog pd;
    private ArrayList<HashMap<String, String>> PolicyList = new ArrayList<>();
    private Bitmap theImage;
    private String result;
    private boolean ZoomOut = false;
    private int orgHeight, orgWidth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enquire);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.Enquire));
        }
        global = (Global) getApplicationContext();
        ca = new ClientAndroidInterface(this);
        escape = new Escape();
        picasso = new Picasso.Builder(this).build();

        isSDCardAvailable();

        etCHFID = findViewById(R.id.etCHFID);
        tvCHFID = findViewById(R.id.tvCHFID);
        tvName = findViewById(R.id.tvName);
        tvDOB = findViewById(R.id.tvDOB);
        tvPolicyStatus = findViewById(R.id.tvPolicyStatus);
        tvGender = findViewById(R.id.tvGender);
        iv = findViewById(R.id.imageView);
        ImageButton btnGo = findViewById(R.id.btnGo);
        ImageButton btnScan = findViewById(R.id.btnScan);
        lv = findViewById(R.id.listView1);
        ll = findViewById(R.id.llListView);

        iv.setOnClickListener(v -> {
            if (ZoomOut) {
                iv.setLayoutParams(new LinearLayout.LayoutParams(orgWidth, orgHeight));
                iv.setAdjustViewBounds(true);
                ZoomOut = false;
            } else {
                orgWidth = iv.getWidth();
                orgHeight = iv.getHeight();
                iv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ZoomOut = true;
            }
        });

        btnGo.setOnClickListener(v -> {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            ClearForm();
            int validInsuranceNumber = escape.CheckInsuranceNumber(etCHFID.getText().toString());
            if (validInsuranceNumber > 0) {
                ca.ShowDialog(getResources().getString(validInsuranceNumber));
                return;
            }

            pd = ProgressDialog.show(Enquire.this, "", getResources().getString(R.string.GetingInsuuree));
            new Thread(() -> {
                getInsureeInfo();
                pd.dismiss();
            }).start();
        });
        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 1);
            ClearForm();
        });

        etCHFID.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                ClearForm();
                pd = ProgressDialog.show(Enquire.this, "", getResources().getString(R.string.GetingInsuuree));
                new Thread(() -> {
                    getInsureeInfo();
                    pd.dismiss();
                }).start();
            }
            return false;
        });

    }

    private void isSDCardAvailable() {

        if (global.isSDCardAvailable() == 0) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.ReadOnly))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ForceClose), (dialog, which) -> finish()).show();

        }
        if (global.isSDCardAvailable() == -1) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.NoSDCard))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ForceClose), (dialog, which) -> finish()).create().show();
        }
    }

    private void ClearForm() {
        tvCHFID.setText(getResources().getString(R.string.InsuranceNumber));
        tvName.setText(getResources().getString(R.string.InsureeName));
        tvDOB.setText(getResources().getString(R.string.BirthDate));
        tvPolicyStatus.setText(getResources().getString(R.string.EnquirePolicyLabel));
        tvGender.setText(getResources().getString(R.string.Gender));
        iv.setImageResource(R.drawable.person);
        ll.setVisibility(View.INVISIBLE);
        PolicyList.clear();
        lv.setAdapter(null);
    }

    private void getInsureeInfo() {
        String chfid = etCHFID.getText().toString();
        result = "";

        if (global.isNetworkAvailable()) {
            try {
                ToRestApi rest = new ToRestApi();
                HttpResponse response = rest.getFromRestApiToken("insuree/" + chfid + "/enquire");
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject obj = new JSONObject(rest.getContent(response));
                    JSONArray arr = new JSONArray();
                    arr.put(obj);
                    result = arr.toString();
                }
                else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    runOnUiThread(() -> showDialog(getResources().getString(R.string.RecordNotFound)));
                } else {
                    runOnUiThread(() -> showDialog(rest.getHttpError(this, responseCode, response.getStatusLine().getReasonPhrase())));
                }
            }
            catch(Exception e){
                Log.e(LOG_TAG, "Fetching online enquire failed", e);
                result="UNKNOWN_ERROR";
            }
        } else {
            //TODO: yet to be done
            result = getDataFromDb(etCHFID.getText().toString());
        }

        runOnUiThread(() -> {
            try {
                JSONArray jsonArray = new JSONArray(result);
                ll.setVisibility(View.VISIBLE);

                int i = 0;
                for (i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (!etCHFID.getText().toString().trim().equals(jsonObject.getString("chfid").trim()))
                        continue;
                    tvCHFID.setText(jsonObject.getString("chfid"));
                    tvName.setText(jsonObject.getString("insureeName"));
                    tvDOB.setText(jsonObject.getString("dob"));
                    tvPolicyStatus.setText(getResources().getString(R.string.EnquirePolicyLabel));
                    tvGender.setText(jsonObject.getString("gender"));

                    if (global.isNetworkAvailable()) {
                        String photo_url_str = "";
                        try {
                            if (!JsonUtils.isStringEmpty(jsonObject, "photoBase64", true)) {
                                try {
                                    byte[] imageBytes = Base64.decode(jsonObject.getString("photoBase64").getBytes(), Base64.DEFAULT);
                                    Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                    iv.setImageBitmap(image);
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "Error while processing Base64 image", e);
                                    iv.setImageDrawable(getResources().getDrawable(R.drawable.person));
                                }
                            } else if (!JsonUtils.isStringEmpty(jsonObject, "photoPath", true)) {
                                photo_url_str = AppInformation.DomainInfo.getDomain() + jsonObject.getString("photoPath");
                                iv.setImageResource(R.drawable.person);
                                picasso.load(photo_url_str)
                                        .placeholder(R.drawable.person)
                                        .error(R.drawable.person)
                                        .into(iv);
                            } else {
                                iv.setImageDrawable(getResources().getDrawable(R.drawable.person));
                            }
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Fetching image failed", e);
                        }
                    } else {
                        byte[] photo = jsonObject.getString("photoPath").getBytes();
                        ByteArrayInputStream is = new ByteArrayInputStream(photo);
                        theImage = BitmapFactory.decodeStream(is);
                        if (theImage != null) {
                            iv.setImageBitmap(theImage);
                        } else {
                            iv.setImageResource(R.drawable.person);
                        }
                    }

                    jsonArray = jsonObject.getJSONArray("details");

                    if (jsonArray.length() == 1 && jsonArray.getJSONObject(0).getString("expiryDate").equals("null")) {
                        tvPolicyStatus.setText(getResources().getString(R.string.EnquirePolicyNotCovered));
                    } else {
                        tvPolicyStatus.setText(getResources().getString(R.string.EnquirePolicyCovered));
                    }

                    for (i = 0; i < jsonArray.length(); i++) {
                        HashMap<String, String> Policy = new HashMap<>();
                        jsonObject = jsonArray.getJSONObject(i);
                        double iDedType = Double.parseDouble(JsonUtils.getStringOrDefault(jsonObject, "dedType", "0", true));

                        String Ded = "", Ded1 = "", Ded2 = "";
                        String Ceiling = "", Ceiling1 = "", Ceiling2 = "";

                        String jDed1, jDed2, jCeiling1, jCeiling2;

                        jDed1 = JsonUtils.getStringOrDefault(jsonObject, "ded1", "", true);
                        jDed2 = JsonUtils.getStringOrDefault(jsonObject, "ded2", "", true);
                        jCeiling1 = JsonUtils.getStringOrDefault(jsonObject, "ceiling1", "", true);
                        jCeiling2 = JsonUtils.getStringOrDefault(jsonObject, "ceiling2", "", true);

                        //Get the type

                        if (iDedType == 1 | iDedType == 2 | iDedType == 3) {
                            if (!jDed1.equals("")) Ded1 = jsonObject.getString("ded1");
                            if (!jCeiling1.equals("")) Ceiling1 = jsonObject.getString("ceiling1");

                            if (!Ded1.equals("")) Ded = "Deduction: " + Ded1;
                            if (!Ceiling1.equals("")) Ceiling = "Ceiling: " + Ceiling1;

                        } else if (iDedType == 1.1 | iDedType == 2.1 | iDedType == 3.1) {

                            if (jDed1.length() > 0) Ded1 = " IP:" + jsonObject.getString("ded1");
                            if (jDed2.length() > 0) Ded2 = " OP:" + jsonObject.getString("ded2");
                            if (jCeiling1.length() > 0)
                                Ceiling1 = " IP:" + jsonObject.getString("ceiling1");
                            if (jCeiling2.length() > 0)
                                Ceiling2 = " OP:" + jsonObject.getString("ceiling2");

                            if (!(Ded1 + Ded2).equals("")) Ded = "Deduction: " + Ded1 + Ded2;
                            if (!(Ceiling1 + Ceiling2).equals(""))
                                Ceiling = "Ceiling: " + Ceiling1 + Ceiling2;

                        }

                        if (JsonUtils.isStringEmpty(jsonObject, "expiryDate", true)) {
                            Policy.put("Heading", getResources().getString(R.string.EnquireNoPolicies));
                        } else {
                            Policy.put("Heading", jsonObject.getString("productCode"));
                            Policy.put("Heading1", JsonUtils.getStringOrDefault(jsonObject, "expiryDate", "", true) + "  " + jsonObject.getString("status"));
                            Policy.put("SubItem1", jsonObject.getString("productName"));
                            Policy.put("SubItem2", Ded);
                            Policy.put("SubItem3", Ceiling);
                        }

                        String TotalAdmissionsLeft = buildEnquireValue(jsonObject, "totalAdmissionsLeft", R.string.totalAdmissionsLeft);
                        String TotalVisitsLeft = buildEnquireValue(jsonObject, "totalVisitsLeft", R.string.totalVisitsLeft);
                        String TotalConsultationsLeft = buildEnquireValue(jsonObject, "totalConsultationsLeft", R.string.totalConsultationsLeft);
                        String TotalSurgeriesLeft = buildEnquireValue(jsonObject, "totalSurgeriesLeft", R.string.totalSurgeriesLeft);
                        String TotalDeliveriesLeft = buildEnquireValue(jsonObject, "totalDelivieriesLeft", R.string.totalDeliveriesLeft);
                        String TotalAntenatalLeft = buildEnquireValue(jsonObject, "totalAntenatalLeft", R.string.totalAntenatalLeft);
                        String ConsultationAmountLeft = buildEnquireValue(jsonObject, "consultationAmountLeft", R.string.consultationAmountLeft);
                        String SurgeryAmountLeft = buildEnquireValue(jsonObject, "surgeryAmountLeft", R.string.surgeryAmountLeft);
                        String HospitalizationAmountLeft = buildEnquireValue(jsonObject, "hospitalizationAmountLeft", R.string.hospitalizationAmountLeft);
                        String AntenatalAmountLeft = buildEnquireValue(jsonObject, "antenatalAmountLeft", R.string.antenatalAmountLeft);
                        String DeliveryAmountLeft = buildEnquireValue(jsonObject, "deliveryAmountLeft", R.string.deliveryAmountLeft);

                        if (!ca.getSpecificControl("TotalAdmissionsLeft").equals("N")) {
                            Policy.put("SubItem4", TotalAdmissionsLeft);
                        }
                        if (!ca.getSpecificControl("TotalVisitsLeft").equals("N")) {
                            Policy.put("SubItem5", TotalVisitsLeft);
                        }
                        if (!ca.getSpecificControl("TotalConsultationsLeft").equals("N")) {
                            Policy.put("SubItem6", TotalConsultationsLeft);
                        }
                        if (!ca.getSpecificControl("TotalSurgeriesLeft").equals("N")) {
                            Policy.put("SubItem7", TotalSurgeriesLeft);
                        }
                        if (!ca.getSpecificControl("TotalDelivieriesLeft").equals("N")) {
                            Policy.put("SubItem8", TotalDeliveriesLeft);
                        }
                        if (!ca.getSpecificControl("TotalAntenatalLeft").equals("N")) {
                            Policy.put("SubItem9", TotalAntenatalLeft);
                        }
                        if (!ca.getSpecificControl("ConsultationAmountLeft").equals("N")) {
                            Policy.put("SubItem10", ConsultationAmountLeft);
                        }
                        if (!ca.getSpecificControl("SurgeryAmountLeft").equals("N")) {
                            Policy.put("SubItem11", SurgeryAmountLeft);
                        }
                        if (!ca.getSpecificControl("HospitalizationAmountLeft").equals("N")) {
                            Policy.put("SubItem12", HospitalizationAmountLeft);
                        }
                        if (!ca.getSpecificControl("AntenatalAmountLeft").equals("N")) {
                            Policy.put("SubItem13", AntenatalAmountLeft);
                        }
                        if (!ca.getSpecificControl("DeliveryAmountLeft").equals("N")) {
                            Policy.put("SubItem14", DeliveryAmountLeft);
                        }

                        PolicyList.add(Policy);
                        etCHFID.setText("");
                        //break;
                    }
                }
                ListAdapter adapter = new SimpleAdapter(Enquire.this,
                        PolicyList, R.layout.policylist,
                        new String[]{"Heading", "Heading1", "SubItem1", "SubItem2", "SubItem3", "SubItem4", "SubItem5", "SubItem6", "SubItem7", "SubItem8", "SubItem9", "SubItem10", "SubItem11", "SubItem12", "SubItem13", "SubItem14"},
                        new int[]{R.id.tvHeading, R.id.tvHeading1, R.id.tvSubItem1, R.id.tvSubItem2, R.id.tvSubItem3, R.id.tvSubItem4, R.id.tvSubItem5, R.id.tvSubItem6, R.id.tvSubItem7, R.id.tvSubItem8, R.id.tvSubItem9, R.id.tvSubItem10, R.id.tvSubItem11, R.id.tvSubItem12, R.id.tvSubItem13, R.id.tvSubItem14}
                );

                lv.setAdapter(adapter);


            } catch (JSONException e) {
                Log.e("Error", "JSON related error when parsing enquiry response", e);
                result = "";
            } catch (Exception e) {
                Log.e("Error", "Unknown error when parsing enquiry response", e);
                result = "";
            }
        });
    }

    protected String buildEnquireValue(JSONObject jsonObject, String jsonKey, int labelId) throws JSONException {
        boolean ignore = jsonObject.getString(jsonKey).equalsIgnoreCase("null");
        if (ignore) {
            return "";
        } else {
            String label = getResources().getString(labelId);
            return label + ": " + jsonObject.getString(jsonKey);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SCAN_QR_CODE:
                if (resultCode == RESULT_OK) {
                    String CHFID = data.getStringExtra("SCAN_RESULT");
                    etCHFID.setText(CHFID);

                    pd = ProgressDialog.show(Enquire.this, "", getResources().getString(R.string.GetingInsuuree));
                    new Thread(() -> {
                        getInsureeInfo();
                        pd.dismiss();
                    }).start();
                }
                break;
        }
    }

    private String getDataFromDb(String chfid) {
        try {
            //ca.OfflineEnquire(chfid);
            result = ca.getDataFromDb2(chfid);
            return result;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error when fetching offline enquire data", e);
            result = e.toString();
        }
        return result;
    }

    protected AlertDialog showDialog(String msg, DialogInterface.OnClickListener okCallback, DialogInterface.OnClickListener cancelCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false);

        if (okCallback != null) {
            builder.setPositiveButton(R.string.Ok, okCallback);
        } else {
            builder.setPositiveButton(R.string.Ok, ((dialog, which) -> dialog.cancel()));
        }

        if (cancelCallback != null) {
            builder.setNegativeButton(R.string.Cancel, cancelCallback);
        }

        return builder.show();
    }

    protected AlertDialog showDialog(String msg) {
        return showDialog(msg, null, null);
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
