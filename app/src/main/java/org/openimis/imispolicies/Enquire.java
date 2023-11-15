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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.openimis.imispolicies.domain.entity.Insuree;
import org.openimis.imispolicies.domain.entity.Policy;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.usecase.FetchInsureeInquire;
import org.openimis.imispolicies.util.DateUtils;
import org.openimis.imispolicies.util.TextViewUtils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Enquire extends ImisActivity {
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
    private final ArrayList<HashMap<String, String>> PolicyList = new ArrayList<>();
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
            View currentFocus = getCurrentFocus();
            if (currentFocus != null) {
                inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

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
        final Insuree insuree;

        if (global.isNetworkAvailable()) {
            try {
                insuree = new FetchInsureeInquire().execute(chfid);
            } catch (Exception e) {
                if (e instanceof HttpException) {
                    if (((HttpException) e).getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        ca.ShowDialog(getResources().getString(R.string.RecordNotFound));
                    } else {
                        ca.ShowDialog(getResources().getString(R.string.NoInternet));
                    }
                } else {
                    ca.ShowDialog(getResources().getString(R.string.UnknownError));
                }
                return;
            }
        } else {
            //TODO: yet to be done
            try {
                insuree = getDataFromDb(etCHFID.getText().toString());
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error when fetching offline enquire data", e);
                ca.ShowDialog(getResources().getString(R.string.UnknownError));
                return;
            }
        }

        runOnUiThread(() -> {
            ll.setVisibility(View.VISIBLE);
            if (insuree != null) {
                tvCHFID.setText(insuree.getChfId());
                tvName.setText(insuree.getName());
                TextViewUtils.setDate(tvDOB, insuree.getDateOfBirth());
                tvPolicyStatus.setText(R.string.EnquirePolicyLabel);
                tvGender.setText(insuree.getGender());
                try {
                    if (insuree.getPhoto() != null) {
                        try {
                            iv.setImageBitmap(BitmapFactory.decodeByteArray(insuree.getPhoto(), 0, insuree.getPhoto().length));
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error while processing Base64 image", e);
                            iv.setImageResource(R.drawable.person);
                        }
                    } else if (insuree.getPhotoPath() != null) {
                        picasso.load(AppInformation.DomainInfo.getDomain() + insuree.getPhotoPath())
                                .placeholder(R.drawable.person)
                                .error(R.drawable.person)
                                .into(iv);
                    } else {
                        iv.setImageResource(R.drawable.person);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Fetching image failed", e);
                }

                if (insuree.getPolicies().size() == 1 && insuree.getPolicies().get(0).getExpiryDate() == null) {
                    tvPolicyStatus.setText(getResources().getString(R.string.EnquirePolicyNotCovered));
                } else {
                    tvPolicyStatus.setText(getResources().getString(R.string.EnquirePolicyCovered));
                }

                for (Policy policy : insuree.getPolicies()) {
                    HashMap<String, String> policyMap = new HashMap<>();

                    double iDedType = policy.getDeductibleType() == null ? 0 : policy.getDeductibleType();

                    String Ded = "", Ded1 = "", Ded2 = "";
                    String Ceiling = "", Ceiling1 = "", Ceiling2 = "";

                    //Get the type
                    if (iDedType == 1 | iDedType == 2 | iDedType == 3) {
                        if (policy.getDeductibleIp() != null)
                            Ded1 = String.valueOf(policy.getDeductibleIp());
                        if (policy.getCeilingIp() != null)
                            Ceiling1 = String.valueOf(policy.getCeilingIp());

                        if (!Ded1.equals("")) Ded = "Deduction: " + Ded1;
                        if (!Ceiling1.equals("")) Ceiling = "Ceiling: " + Ceiling1;

                    } else if (iDedType == 1.1 | iDedType == 2.1 | iDedType == 3.1) {

                        if (policy.getDeductibleIp() != null)
                            Ded1 = " IP:" + policy.getDeductibleIp();
                        if (policy.getDeductibleOp() != null)
                            Ded2 = " OP:" + policy.getDeductibleOp();
                        if (policy.getCeilingIp() != null)
                            Ceiling1 = " IP:" + policy.getCeilingIp();
                        if (policy.getCeilingOp() != null)
                            Ceiling2 = " OP:" + policy.getCeilingOp();

                        if (!(Ded1 + Ded2).equals("")) Ded = "Deduction: " + Ded1 + Ded2;
                        if (!(Ceiling1 + Ceiling2).equals(""))
                            Ceiling = "Ceiling: " + Ceiling1 + Ceiling2;

                    }

                    if (policy.getExpiryDate() == null) {
                        policyMap.put("Heading", getResources().getString(R.string.EnquireNoPolicies));
                    } else {
                        String expiryDate = policy.getExpiryDate() != null ?
                                DateUtils.toDateString(policy.getExpiryDate()) : null;
                        String status = policy.getStatus().name();
                        String heading1;
                        if (expiryDate != null) {
                            heading1 = expiryDate + " " + status;
                        } else {
                            heading1 = status;
                        }
                        policyMap.put("Heading", policy.getCode());
                        policyMap.put("Heading1", heading1);
                        policyMap.put("SubItem1", policy.getName());
                        policyMap.put("SubItem2", Ded);
                        policyMap.put("SubItem3", Ceiling);
                    }

                    if (!ca.getSpecificControl("TotalAdmissionsLeft").equals("N")) {
                        policyMap.put("SubItem4", buildEnquireValue(policy.getTotalAdmissionsLeft(), R.string.totalAdmissionsLeft));
                    }
                    if (!ca.getSpecificControl("TotalVisitsLeft").equals("N")) {
                        policyMap.put("SubItem5", buildEnquireValue(policy.getTotalVisitsLeft(), R.string.totalVisitsLeft));
                    }
                    if (!ca.getSpecificControl("TotalConsultationsLeft").equals("N")) {
                        policyMap.put("SubItem6", buildEnquireValue(policy.getTotalConsultationsLeft(), R.string.totalConsultationsLeft));
                    }
                    if (!ca.getSpecificControl("TotalSurgeriesLeft").equals("N")) {
                        policyMap.put("SubItem7", buildEnquireValue(policy.getTotalSurgeriesLeft(), R.string.totalSurgeriesLeft));
                    }
                    if (!ca.getSpecificControl("TotalDelivieriesLeft").equals("N")) {
                        policyMap.put("SubItem8", buildEnquireValue(policy.getTotalDeliveriesLeft(), R.string.totalDeliveriesLeft));
                    }
                    if (!ca.getSpecificControl("TotalAntenatalLeft").equals("N")) {
                        policyMap.put("SubItem9", buildEnquireValue(policy.getTotalAntenatalLeft(), R.string.totalAntenatalLeft));
                    }
                    if (!ca.getSpecificControl("ConsultationAmountLeft").equals("N")) {
                        policyMap.put("SubItem10", buildEnquireValue(policy.getConsultationAmountLeft(), R.string.consultationAmountLeft));
                    }
                    if (!ca.getSpecificControl("SurgeryAmountLeft").equals("N")) {
                        policyMap.put("SubItem11", buildEnquireValue(policy.getSurgeryAmountLeft(), R.string.surgeryAmountLeft));
                    }
                    if (!ca.getSpecificControl("HospitalizationAmountLeft").equals("N")) {
                        policyMap.put("SubItem12", buildEnquireValue(policy.getHospitalizationAmountLeft(), R.string.hospitalizationAmountLeft));
                    }
                    if (!ca.getSpecificControl("AntenatalAmountLeft").equals("N")) {
                        policyMap.put("SubItem13", buildEnquireValue(policy.getAntenatalAmountLeft(), R.string.antenatalAmountLeft));
                    }
                    if (!ca.getSpecificControl("DeliveryAmountLeft").equals("N")) {
                        policyMap.put("SubItem14", buildEnquireValue(policy.getDeliveryAmountLeft(), R.string.deliveryAmountLeft));
                    }

                    PolicyList.add(policyMap);
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
        });
    }

    protected String buildEnquireValue(@Nullable Number value, @StringRes int labelId) {
        if (value == null) {
            return "";
        } else {
            String label = getResources().getString(labelId);
            return label + ": " + value;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    @Nullable
    @SuppressLint({"WrongConstant", "Range"})
    private Insuree getDataFromDb(String chfid) throws Exception {
        try (SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(global.getAppDirectory() + "ImisData.db3", null)) {
            String[] columns = {"CHFID", "Photo", "InsureeName", "DOB", "Gender", "ProductCode", "ProductName", "ExpiryDate", "Status", "DedType", "Ded1", "Ded2", "Ceiling1", "Ceiling2"};
            String[] selectionArgs = {chfid};
            Cursor c = db.query("tblPolicyInquiry", columns, "Trim(CHFID)=?", selectionArgs, null, null, null);
            String name = null;
            Date dateOfBirth = null;
            String gender = null;
            byte[] photo = null;
            List<Policy> policies = new ArrayList<>();
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                if (c.isFirst()) {
                    name = c.getString(c.getColumnIndex("InsureeName"));
                    String dateOfBirthString = c.getString(c.getColumnIndex("DOB"));
                    if (dateOfBirthString != null) {
                        dateOfBirth = DateUtils.dateFromString(dateOfBirthString);
                    }
                    gender = c.getString(c.getColumnIndex("Gender"));
                    photo = c.getBlob(c.getColumnIndex("Photo"));
                }
                String expiryDate = c.getString(c.getColumnIndex("ExpiryDate"));
                String status = c.getString(c.getColumnIndex("Status"));
                String deductibleType = c.getString(c.getColumnIndex("DedType"));
                String deductibleIp = c.getString(c.getColumnIndex("Ded1"));
                String deductibleOp = c.getString(c.getColumnIndex("Ded2"));
                String ceilingIp = c.getString(c.getColumnIndex("Ceiling1"));
                String ceilingOp = c.getString(c.getColumnIndex("Ceiling2"));
                policies.add(new Policy(
                        /* code = */ c.getString(c.getColumnIndex("ProductCode")),
                        /* name = */ c.getString(c.getColumnIndex("ProductName")),
                        /* value = */ null,
                        /* expiryDate = */ expiryDate != null ? DateUtils.dateFromString(expiryDate) : null,
                        /* status = */ status != null ? Policy.Status.valueOf(status) : null,
                        /* deductibleType = */ deductibleType != null ? Double.parseDouble(deductibleType) : null,
                        /* deductibleIp = */ deductibleIp != null ? Double.parseDouble(deductibleIp) : null,
                        /* deductibleOp = */ deductibleOp != null ? Double.parseDouble(deductibleOp) : null,
                        /* ceilingIp = */ ceilingIp != null ? Double.parseDouble(ceilingIp) : null,
                        /* ceilingOp = */ ceilingOp != null ? Double.parseDouble(ceilingOp) : null,
                        /* antenatalAmountLeft = */ null,
                        /* consultationAmountLeft = */ null,
                        /* deliveryAmountLeft = */ null,
                        /* hospitalizationAmountLeft = */ null,
                        /* surgeryAmountLeft = */ null,
                        /* totalAdmissionsLeft = */ null,
                        /* totalAntenatalLeft = */ null,
                        /* totalConsultationsLeft = */ null,
                        /* totalDeliveriesLeft = */ null,
                        /* totalSurgeriesLeft = */ null,
                        /* totalVisitsLeft = */ null
                ));
            }
            c.close();
            db.close();
            return new Insuree(
                    /* chfId = */ chfid,
                    /* name = */ Objects.requireNonNull(name),
                    /* dateOfBirth = */ Objects.requireNonNull(dateOfBirth),
                    /* gender = */ gender,
                    /* photoPath = */ null,
                    /* photo = */ photo,
                    /* policies = */ policies
            );
        }
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
