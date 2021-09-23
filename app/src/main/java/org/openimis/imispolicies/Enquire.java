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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import com.exact.general.General;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Enquire extends AppCompatActivity {
    private static final String LOG_TAG = "ENQUIRE";
    private static final int REQUEST_SCAN_QR_CODE = 1;
    private Global global;
    private General general;
    private Escape escape;
    private ClientAndroidInterface ca;
    private EditText etCHFID;
    private TextView tvCHFID;
    private TextView tvName;
    private TextView tvGender;
    private TextView tvDOB;
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
        general = new General(AppInformation.DomainInfo.getDomain());
        global = (Global) getApplicationContext();
        ca = new ClientAndroidInterface(this);
        escape = new Escape();

        isSDCardAvailable();

        etCHFID = findViewById(R.id.etCHFID);
        tvCHFID = findViewById(R.id.tvCHFID);
        tvName = findViewById(R.id.tvName);
        tvDOB = findViewById(R.id.tvDOB);
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

        if (general.isSDCardAvailable() == 0) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.ReadOnly))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ForceClose), (dialog, which) -> finish()).show();

        }
        if (general.isSDCardAvailable() == -1) {
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
            ToRestApi rest = new ToRestApi();
            String res = rest.getObjectFromRestApiToken("insuree/" + chfid + "/enquire");

            JSONObject jobj = new JSONObject();
            try {
                jobj = new JSONObject(res);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Response is not a proper JSON", e);
            }

            JSONArray arr = new JSONArray();
            arr.put(jobj);

            result = arr.toString();
        } else {
            //TODO: yet to be done
            result = getDataFromDb(etCHFID.getText().toString());
        }

        runOnUiThread(() -> {
            try {
                JSONArray jsonArray = new JSONArray(result);

                if (jsonArray.length() == 0) {
                    ca.ShowDialog(getResources().getString(R.string.RecordNotFound));
                    return;
                }

                ll.setVisibility(View.VISIBLE);

                int i = 0;
                for (i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (!etCHFID.getText().toString().trim().equals(jsonObject.getString("chfid").trim()))
                        continue;
                    tvCHFID.setText(jsonObject.getString("chfid"));
                    tvName.setText(jsonObject.getString("insureeName"));
                    tvDOB.setText(jsonObject.getString("dob"));
                    tvGender.setText(jsonObject.getString("gender"));

                    if (global.isNetworkAvailable()) {
                        String photo_url_str = "";
                        try {
                            if (jsonObject.has("photoBase64") && !jsonObject.isNull("photoBase64") && !"null".equals(jsonObject.getString("photoBase64"))) {
                                try {
                                    byte[] imageBytes = Base64.decode(jsonObject.getString("photoBase64").getBytes(), Base64.DEFAULT);
                                    Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                    iv.setImageBitmap(image);
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "Error while processing Base64 image", e);
                                    iv.setImageDrawable(getResources().getDrawable(R.drawable.person));
                                }
                            } else if (jsonObject.has("photoPath") && !jsonObject.isNull("photoPath") && !"null".equals(jsonObject.getString("photoPath"))) {
                                photo_url_str = AppInformation.DomainInfo.getDomain() + jsonObject.getString("photoPath");
                                iv.setImageResource(R.drawable.person);
                                Picasso.with(this)
                                        .load(photo_url_str)
                                        .placeholder(R.drawable.person)
                                        .error(R.drawable.person).into(iv);
                            } else {
                                iv.setImageDrawable(getResources().getDrawable(R.drawable.person));
                            }
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Fetching image failed", e);
                        }
                    } else {
                        if (ca.theImage != null) {
                            iv.setImageBitmap(ca.theImage);
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

                    }

                    jsonArray = jsonObject.getJSONArray("details");

                    for (i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);


                        HashMap<String, String> Policy = new HashMap<>();
                        jsonObject = jsonArray.getJSONObject(i);
                        double iDedType = 0;
                        if (!jsonObject.getString("dedType").equalsIgnoreCase("null"))
                            iDedType = Double.parseDouble(jsonObject.getString("dedType"));

                        String Ded = "", Ded1 = "", Ded2 = "";
                        String Ceiling = "", Ceiling1 = "", Ceiling2 = "";

                        String jDed1 = "", jDed2 = "", jCeiling1 = "", jCeiling2 = "";

                        if (jsonObject.getString("ded1").equalsIgnoreCase("null")) jDed1 = "";
                        else jDed1 = jsonObject.getString("ded1");
                        if (jsonObject.getString("ded2").equalsIgnoreCase("null")) jDed2 = "";
                        else jDed2 = jsonObject.getString("ded2");
                        if (jsonObject.getString("ceiling1").equalsIgnoreCase("null"))
                            jCeiling1 = "";
                        else jCeiling1 = jsonObject.getString("ceiling1");
                        if (jsonObject.getString("ceiling2").equalsIgnoreCase("null"))
                            jCeiling2 = "";
                        else jCeiling2 = jsonObject.getString("ceiling2");

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


                        Policy.put("Heading", jsonObject.getString("productCode"));
                        Policy.put("Heading1", jsonObject.getString("expiryDate") + "  " + jsonObject.getString("status"));
                        Policy.put("SubItem1", jsonObject.getString("productName"));
                        Policy.put("SubItem2", Ded);
                        Policy.put("SubItem3", Ceiling);

                        String TotalAdmissionsLeft = "";
                        String TotalVisitsLeft = "";
                        String TotalConsultationsLeft = "";
                        String TotalSurgeriesLeft = "";
                        String TotalDeliveriesLeft = "";
                        String TotalAntenatalLeft = "";
                        String ConsultationAmountLeft = "";
                        String SurgeryAmountLeft = "";
                        String HospitalizationAmountLeft = "";
                        String AntenatalAmountLeft = "";
                        String DeliveryAmountLeft = "";

                        TotalAdmissionsLeft = buildEnquireValue(jsonObject, "totalAdmissionsLeft", R.string.totalAdmissionsLeft);
                        TotalVisitsLeft = buildEnquireValue(jsonObject, "totalVisitsLeft", R.string.totalVisitsLeft);
                        TotalConsultationsLeft = buildEnquireValue(jsonObject, "totalConsultationsLeft", R.string.totalConsultationsLeft);
                        TotalSurgeriesLeft = buildEnquireValue(jsonObject, "totalSurgeriesLeft", R.string.totalSurgeriesLeft);
                        TotalDeliveriesLeft = buildEnquireValue(jsonObject, "totalDelivieriesLeft", R.string.totalDeliveriesLeft);
                        TotalAntenatalLeft = buildEnquireValue(jsonObject, "totalAntenatalLeft", R.string.totalAntenatalLeft);
                        ConsultationAmountLeft = buildEnquireValue(jsonObject, "consultationAmountLeft", R.string.consultationAmountLeft);
                        SurgeryAmountLeft = buildEnquireValue(jsonObject, "surgeryAmountLeft", R.string.surgeryAmountLeft);
                        HospitalizationAmountLeft = buildEnquireValue(jsonObject, "hospitalizationAmountLeft", R.string.hospitalizationAmountLeft);
                        AntenatalAmountLeft = buildEnquireValue(jsonObject, "antenatalAmountLeft", R.string.antenatalAmountLeft);
                        DeliveryAmountLeft = buildEnquireValue(jsonObject, "deliveryAmountLeft", R.string.deliveryAmountLeft);

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
