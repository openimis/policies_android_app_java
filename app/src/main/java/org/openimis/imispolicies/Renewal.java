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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.Util.StringUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.widget.AdapterView.INVALID_POSITION;

public class Renewal extends AppCompatActivity {
    private Global global;
    private SQLHandler sqlHandler;

    private ClientAndroidInterface ca;
    private EditText etOfficer;
    private EditText etCHFID;
    private EditText etReceiptNo;
    private EditText etProductCode;
    private EditText etAmount;
    private Button btnSubmit;
    private ImageButton btnScan;
    private ProgressDialog pd;
    private CheckBox chkDiscontinue;
    private Spinner spPayer;
    private Spinner spProduct;
    private String FileName;
    private EditText etControlNumber;

    private int LocationId;
    private int RenewalId;
    private String RenewalUUID;
    private int result;
    private EditText PolicyValue;

    private ListAdapter adapter;

    private final ArrayList<HashMap<String, String>> PayersList = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> ProductList = new ArrayList<>();

    private static final int INTENT_ACTIVITY_SCAN_CODE = 554;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ca = new ClientAndroidInterface(this);
        global = (Global) getApplicationContext();
        sqlHandler = new SQLHandler(this);

        setContentView(R.layout.renewal);

        etOfficer = findViewById(R.id.etofficer);
        etCHFID = findViewById(R.id.etCHFID);
        etReceiptNo = findViewById(R.id.etReceiptNo);
        etProductCode = findViewById(R.id.etProductCode);
        etAmount = findViewById(R.id.etAmount);
        btnSubmit = findViewById(R.id.btnSubmit);
        chkDiscontinue = findViewById(R.id.chkDiscontinue);
        PolicyValue = findViewById(R.id.txtPolicyValue);
        spPayer = findViewById(R.id.spPayer);
        spProduct = findViewById(R.id.spProduct);
        etControlNumber = findViewById(R.id.etControlNumber);
        btnScan = findViewById(R.id.btnScan);

        if (!ca.getRule("ShowPaymentOption", true)) {
            etReceiptNo.setVisibility(View.GONE);
            etAmount.setVisibility(View.GONE);
            spPayer.setVisibility(View.GONE);
        }

        if (!ca.IsBulkCNUsed()) {
            etControlNumber.setVisibility(View.GONE);
        }

        if (getIntent().getStringExtra("CHFID").equals(getResources().getString(R.string.UnlistedRenewalPolicies))) {
            setEditable(etOfficer, true);
            etOfficer.setInputType(View.TEXT_ALIGNMENT_TEXT_START);

            etCHFID.setText("");
            setEditable(etCHFID, true);
            etCHFID.setInputType(View.TEXT_ALIGNMENT_TEXT_START);

            setEditable(PolicyValue, true);
            PolicyValue.setInputType(View.TEXT_ALIGNMENT_TEXT_START);

            etProductCode.setVisibility(View.GONE);
            chkDiscontinue.setVisibility(View.GONE);
            PolicyValue.setVisibility(View.GONE);
        } else {
            etCHFID.setText(getIntent().getStringExtra("CHFID"));
        }

        etAmount.setText("0");
        etOfficer.setText(getIntent().getStringExtra("OfficerCode"));
        RenewalId = Integer.parseInt(getIntent().getStringExtra("RenewalId"));
        RenewalUUID = getIntent().getStringExtra("RenewalUUID");
        etProductCode.setText(getIntent().getStringExtra("ProductCode"));
        LocationId = Integer.parseInt(getIntent().getStringExtra("LocationId"));
        PolicyValue.setText(getIntent().getStringExtra("PolicyValue"));


        if (getIntent().getStringExtra("CHFID").equals(getResources().getString(R.string.UnlistedRenewalPolicies))) {
            BindSpinnerPayers();
            BindSpinnerProduct();
        } else {
            spProduct.setVisibility(View.GONE);
            assignNextFreeCn(etProductCode.getText().toString());
            BindSpinnerPayers();
        }

        final PayerAdapter adapter = new PayerAdapter(Renewal.this);
        spPayer.setAdapter(adapter);

        chkDiscontinue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (chkDiscontinue.isChecked())
                DiscontinuePolicy();
        });

        btnSubmit.setOnClickListener(v -> {
            if (!chkDiscontinue.isChecked()) {
                final String[] renewal = {null};

                if (getIntent().getStringExtra("CHFID").equals(getResources().getString(R.string.UnlistedRenewalPolicies))) {
                    etProductCode.setText(GetSelectedProduct());
                }

                if (!isValidate()) {
                    pd.dismiss();
                    return;
                }

                Runnable saveRenewal = () -> new Thread(() -> {
                    renewal[0] = WriteJSON();
                    WriteXML();

                    runOnUiThread(() -> {
                        UpdateRow(RenewalId);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.SavedOnSDCard), Toast.LENGTH_LONG).show();

                        //Go back to the previous activity.
                        finish();
                    });
                }).start();

                if (ca.IsBulkCNUsed() && !ca.isFetchedControlNumber(etControlNumber.getText().toString())) {
                    ConfirmControlNumber(saveRenewal);
                } else {
                    saveRenewal.run();
                }
            }
        });

        btnScan.setOnClickListener(v -> {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    try {
                        startActivityForResult(intent, INTENT_ACTIVITY_SCAN_CODE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        etOfficer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchString = etOfficer.getText().toString();
                int LocID = ca.getLocationId(searchString);
                BindSpinnerPayers(LocID);
                BindSpinnerProduct();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (ca.IsBulkCNUsed()) {
                    assignNextFreeCn(ProductList.get(position).get("ProductCode"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private String GetSelectedPayer() {
        int selectedPosition = spPayer.getSelectedItemPosition();
        return selectedPosition != INVALID_POSITION ? PayersList.get(selectedPosition).get("PayerId") : "0";
    }

    private String GetSelectedProduct() {
        int selectedPosition = spProduct.getSelectedItemPosition();
        return selectedPosition != INVALID_POSITION ? ProductList.get(selectedPosition).get("ProductCode") : "0";
    }

    private void WriteXML() {
        try {
            //Create All directories
            File MyDir = new File(global.getMainDirectory());

            //Create File name
            Date date = Calendar.getInstance().getTime();
            String d = AppInformation.DateTimeInfo.getDefaultFileDatetimeFormatter().format(date);
            FileName = "RenPol_" + d + "_" + etCHFID.getText().toString() + "_" + etReceiptNo.getText().toString() + ".xml";
            d = AppInformation.DateTimeInfo.getDefaultDateFormatter().format(date);
            String PayerId = GetSelectedPayer();
            //Create XML file
            File policyXML = new File(MyDir, FileName);

            FileOutputStream fos = new FileOutputStream(policyXML);

            XmlSerializer serializer = Xml.newSerializer();

            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.TRUE);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "Policy");

            serializer.startTag(null, "RenewalId");
            serializer.text(String.valueOf(RenewalId));
            serializer.endTag(null, "RenewalId");

            serializer.startTag(null, "Officer");
            serializer.text(etOfficer.getText().toString());
            serializer.endTag(null, "Officer");

            serializer.startTag(null, "CHFID");
            serializer.text(etCHFID.getText().toString());
            serializer.endTag(null, "CHFID");

            serializer.startTag(null, "ReceiptNo");
            serializer.text(etReceiptNo.getText().toString());
            serializer.endTag(null, "ReceiptNo");

            serializer.startTag(null, "ProductCode");
            serializer.text(etProductCode.getText().toString());
            serializer.endTag(null, "ProductCode");

            serializer.startTag(null, "Amount");
            serializer.text(etAmount.getText().toString());
            serializer.endTag(null, "Amount");

            serializer.startTag(null, "Date");
            serializer.text(d);
            serializer.endTag(null, "Date");

            serializer.startTag(null, "Discontinue");
            serializer.text(String.valueOf(chkDiscontinue.isChecked()));
            serializer.endTag(null, "Discontinue");

            serializer.startTag(null, "PayerId");
            serializer.text(PayerId);
            serializer.endTag(null, "PayerId");

            if (ca.IsBulkCNUsed()) {
                serializer.startTag(null, "ControlNumber");
                serializer.text(etControlNumber.getText().toString());
                serializer.endTag(null, "ControlNumber");
            }

            serializer.endTag(null, "Policy");
            serializer.endDocument();
            serializer.flush();
            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String WriteJSON() {
        File MyDir = new File(global.getMainDirectory());

        Date date = Calendar.getInstance().getTime();
        String d = AppInformation.DateTimeInfo.getDefaultFileDatetimeFormatter().format(date);
        FileName = "RenPolJSON_" + d + "_" + etCHFID.getText().toString() + "_" + etReceiptNo.getText().toString() + ".txt";
        String PayerId = GetSelectedPayer();
        String ProductCode = GetSelectedProduct();
        d = AppInformation.DateTimeInfo.getDefaultDateFormatter().format(date);

        //Create XML file
        File policyJSON = new File(MyDir, FileName);

        JSONObject FullObject = new JSONObject();

        try {
            JSONObject RenewalObject = new JSONObject();

            RenewalObject.put("RenewalId", String.valueOf(RenewalId));
            RenewalObject.put("Officer", etOfficer.getText().toString());
            RenewalObject.put("CHFID", etCHFID.getText().toString());
            RenewalObject.put("ReceiptNo", etReceiptNo.getText().toString());
            RenewalObject.put("ProductCode", etProductCode.getText().toString());
            RenewalObject.put("Amount", etAmount.getText().toString());
            RenewalObject.put("Date", d);
            RenewalObject.put("Discontinue", String.valueOf(chkDiscontinue.isChecked()));
            RenewalObject.put("PayerId", PayerId);

            if (ca.IsBulkCNUsed()) {
                RenewalObject.put("ControlNumber", etControlNumber.getText().toString());
            }

            FullObject.put("Policy", RenewalObject);

            try {
                FileOutputStream fOut = new FileOutputStream(policyJSON);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(FullObject.toString());
                myOutWriter.close();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return FullObject.toString();
    }

    private void DiscontinuePolicy() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.DiscontinuePolicyQ)
                .setPositiveButton(R.string.Yes, (dialog, which) -> {
                    dialog.dismiss();
                    pd = ProgressDialog.show(Renewal.this, "", getResources().getString(R.string.Uploading));
                    new Thread(() -> {
                        ToRestApi rest = new ToRestApi();
                        try {
                            String result = null;
                            int responseCode = 0;
                            if (global.isNetworkAvailable()) {
                                HttpResponse response = rest.deleteFromRestApiToken("policy/renew/" + RenewalUUID);
                                responseCode = response.getStatusLine().getStatusCode();
                                result = rest.getContent(response);
                            } else {
                                WriteXML();
                            }
                            if (responseCode == HttpURLConnection.HTTP_OK && TextUtils.equals(result, "1")) {
                                DeleteRow(RenewalId);
                                pd.dismiss();
                                finish();
                            }
                        } catch (final Exception e) {
                            e.printStackTrace();
                            pd.dismiss();
                            runOnUiThread(() -> {
                                chkDiscontinue.setChecked(false);
                                ca.ShowDialog(e.toString());
                            });
                        }
                    }).start();

                })
                .setNegativeButton(R.string.No, (dialog, which) -> chkDiscontinue.setChecked(false)).show();
    }

    private void ConfirmControlNumber(Runnable onConfirmed) {
        String message = getResources().getString(R.string.ConfirmControlNumber, etControlNumber.getText().toString());
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.Yes, (dialog, which) -> onConfirmed.run())
                .setNegativeButton(R.string.No, (dialog, which) -> {
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_ACTIVITY_SCAN_CODE && data != null) {
            etCHFID.setText(data.getStringExtra(Intents.Scan.RESULT));
        }
    }

    private void UpdateRow(int RenewalId) {
        if (RenewalId != 0) {
            ca.UpdateRenewTable(RenewalId);
        }
        ca.deleteBulkCn(etControlNumber.getText().toString());
    }

    private void DeleteRow(int RenewalId) {
        ca.DeleteRenewalOfflineRow(RenewalId);
    }

    private void BindSpinnerPayers(int LocationId) {
        String result = ca.getPayersByDistrictId(LocationId);

        JSONArray jsonArray;
        JSONObject object;

        try {
            jsonArray = new JSONArray(result);

            PayersList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                object = jsonArray.getJSONObject(i);

                // Enter an empty record
                if (i == 0) {
                    HashMap<String, String> Payer = new HashMap<>();
                    Payer.put("PayerId", String.valueOf(0));
                    Payer.put("PayerName", getResources().getString(R.string.SelectPayer));
                    PayersList.add(Payer);
                }

                HashMap<String, String> Payer = new HashMap<>();
                Payer.put("PayerId", object.getString("PayerId"));
                Payer.put("PayerName", object.getString("PayerName"));

                PayersList.add(Payer);

                adapter = new SimpleAdapter(Renewal.this, PayersList, R.layout.spinnerpayer,
                        new String[]{"PayerId", "PayerName"},
                        new int[]{R.id.tvPayerId, R.id.tvPayerName});

                spPayer.setAdapter((SpinnerAdapter) adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void BindSpinnerPayers() {
        BindSpinnerPayers(LocationId);
    }

    private void BindSpinnerProduct() {
        String result = ca.getProductsRD();

        JSONArray jsonArray;
        JSONObject object;

        try {
            jsonArray = new JSONArray(result);

            ProductList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                object = jsonArray.getJSONObject(i);

                // Enter an empty record
                if (i == 0) {
                    HashMap<String, String> Product = new HashMap<>();
                    Product.put("ProdId", String.valueOf(0));
                    Product.put("ProductCode", String.valueOf(0));
                    Product.put("ProductName", getResources().getString(R.string.SelectProduct));
                    ProductList.add(Product);
                }

                HashMap<String, String> Product = new HashMap<>();
                Product.put("ProdId", object.getString("ProdId"));
                Product.put("ProductCode", object.getString("ProductCode"));
                Product.put("ProductName", object.getString("ProductName"));


                ProductList.add(Product);

                SimpleAdapter adapter = new SimpleAdapter(Renewal.this, ProductList, R.layout.spinnerproducts,
                        new String[]{"ProductCode", "ProductName"},
                        new int[]{R.id.tvProductCode, R.id.tvProductName});

                spProduct.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidate() {

        if (etOfficer.getText().length() == 0) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.MissingOfficer));
                etOfficer.requestFocus();
            });

            return false;
        }

        if (etCHFID.getText().length() == 0) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.MissingCHFID));
                etCHFID.requestFocus();
            });

            return false;
        }


        if (etReceiptNo.getVisibility() == View.VISIBLE && etReceiptNo.getText().length() == 0) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.MissingReceiptNo));
                etReceiptNo.requestFocus();
            });

            return false;
        }

        if (etProductCode.getText().length() == 0 || TextUtils.equals(etProductCode.getText(), "0")) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.MissingProductCode));
                etProductCode.requestFocus();
            });

            return false;
        }

        if (etAmount.getVisibility() == View.VISIBLE && etAmount.getText().length() == 0) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.MissingAmount));
                etAmount.requestFocus();
            });

            return false;
        }

        if (spProduct.getVisibility() == View.VISIBLE && spProduct.getSelectedItemPosition() == 0) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.MissingProductCode));
                etControlNumber.requestFocus();
            });

            return false;
        }

        if (ca.IsBulkCNUsed() && etControlNumber.getText().toString().length() == 0) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.noBulkCNAssigned));
                etControlNumber.requestFocus();
            });

            return false;
        }

        if (global.isNetworkAvailable() && etReceiptNo.getVisibility() == View.VISIBLE) {
            if (etReceiptNo.getText().toString().trim().length() > 0) {
                HttpResponse response = null;

                try {
                    JSONObject receiptObj = new JSONObject();
                    receiptObj.put("ReceiptNo", etReceiptNo.getText().toString());
                    receiptObj.put("CHFID", etCHFID.getText().toString());

                    ToRestApi rest = new ToRestApi();
                    response = rest.postToRestApiToken(receiptObj, "premium/receipt");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (response != null) {
                    int responseCode = response.getStatusLine().getStatusCode();
                    boolean isUniqueReceiptNo = false;
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try {
                            isUniqueReceiptNo = Boolean.parseBoolean(EntityUtils.toString(response.getEntity()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!isUniqueReceiptNo) {
                            runOnUiThread(() -> {
                                ca.ShowDialog(getResources().getString(R.string.InvalidReceiptNo));
                                etReceiptNo.requestFocus();
                            });
                            return false;
                        }
                    } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        runOnUiThread(() -> {
                            ca.ShowDialog(getResources().getString(R.string.LogInToCheckRecieptNo));
                            etReceiptNo.requestFocus();
                        });
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private void showInfoDialog(int resId) {
        new AlertDialog.Builder(this)
                .setMessage(resId)
                .setPositiveButton(getResources().getString(R.string.Ok), ((dialog, which) -> dialog.dismiss()))
                .show();
    }

    private void assignNextFreeCn(String productCode) {
        if (!StringUtil.equals(productCode, "0")) {
            String controlNumber = sqlHandler.getNextFreeCn(etOfficer.getText().toString(), productCode);
            if (controlNumber != null) {
                etControlNumber.setText(controlNumber);
            } else {
                showInfoDialog(R.string.noBulkCNAvailable);
                etControlNumber.setText("");
            }
        } else {
            etControlNumber.setText("");
        }
    }

    private void setEditable(EditText view, boolean state) {
        view.setClickable(state);
        view.setCursorVisible(state);
        view.setFocusable(state);
        view.setFocusableInTouchMode(state);
        view.setLongClickable(state);
    }
}
