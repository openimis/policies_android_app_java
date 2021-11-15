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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Xml;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Renewal extends AppCompatActivity {
    private Global global;

    private ClientAndroidInterface ca;
    private EditText etOfficer;
    private EditText etCHFID;
    private EditText etReceiptNo;
    private EditText etProductCode;
    private EditText etAmount;
    private Button btnSubmit;
    private ProgressDialog pd;
    private CheckBox chkDiscontinue;
    private Spinner spPayer;
    private Spinner spProduct;
    private AutoCompleteTextView etPayer;
    private String FileName;
    private File PolicyXML;
    private File PolicyJSON;
    private String OfficerCode;

    private int LocationId;

    private int RenewalId;
    private String RenewalUUID;
    private int result;
    private EditText PolicyValue;

    private ListAdapter adapter;

    private ArrayList<HashMap<String, String>> PayersList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> ProductList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global = (Global) getApplicationContext();

        setContentView(R.layout.renewal);

        etOfficer = findViewById(R.id.etofficer);
        etCHFID = findViewById(R.id.etCHFID);
        etReceiptNo = findViewById(R.id.etReceiptNo);
        etProductCode = findViewById(R.id.etProductCode);
        etAmount = findViewById(R.id.etAmount);
        btnSubmit = findViewById(R.id.btnSubmit);
        chkDiscontinue = findViewById(R.id.chkDiscontinue);
        PolicyValue = findViewById(R.id.txtPolicyValue);

        if (getIntent().getStringExtra("CHFID").equals(getResources().getString(R.string.UnlistedRenewalPolicies))) {

            etOfficer.setClickable(true);
            etOfficer.setCursorVisible(true);
            etOfficer.setInputType(View.TEXT_ALIGNMENT_TEXT_START);
            etOfficer.setFocusable(true);
            etOfficer.setFocusableInTouchMode(true);
            etOfficer.setLongClickable(true);

            etCHFID.setText("");
            etCHFID.setClickable(true);
            etCHFID.setCursorVisible(true);
            etCHFID.setInputType(View.TEXT_ALIGNMENT_TEXT_START);
            etCHFID.setFocusable(true);
            etCHFID.setFocusableInTouchMode(true);
            etCHFID.setLongClickable(true);

            PolicyValue.setClickable(true);
            PolicyValue.setCursorVisible(true);
            PolicyValue.setInputType(View.TEXT_ALIGNMENT_TEXT_START);
            PolicyValue.setFocusable(true);
            PolicyValue.setFocusableInTouchMode(true);
            PolicyValue.setLongClickable(true);

            etProductCode.setVisibility(View.GONE);
            chkDiscontinue.setVisibility(View.GONE);
            PolicyValue.setVisibility(View.GONE);

        } else {
            etCHFID.setText(getIntent().getStringExtra("CHFID"));
        }

        etOfficer.setText(getIntent().getStringExtra("OfficerCode"));
        OfficerCode = getIntent().getStringExtra("OfficerCode");
        RenewalId = Integer.parseInt(getIntent().getStringExtra("RenewalId"));
        RenewalId = Integer.parseInt(getIntent().getStringExtra("RenewalId"));
        RenewalUUID = getIntent().getStringExtra("RenewalUUID");
        etProductCode.setText(getIntent().getStringExtra("ProductCode"));
        LocationId = Integer.parseInt(getIntent().getStringExtra("LocationId"));
        PolicyValue.setText(getIntent().getStringExtra("PolicyValue"));

        spPayer = findViewById(R.id.spPayer);
        spProduct = findViewById(R.id.spProduct);
        etPayer = findViewById(R.id.etOfficer);

        if (getIntent().getStringExtra("CHFID").equals(getResources().getString(R.string.UnlistedRenewalPolicies))) {
            BindSpinnerPayersXXXX(LocationId);
            BindSpinnerProduct();
        } else {
            spProduct.setVisibility(View.GONE);
            BindSpinnerPayers();
        }


        final PayerAdapter adapter = new PayerAdapter(Renewal.this);
        etPayer.setAdapter(adapter);
        etPayer.setThreshold(1);

        etPayer.setOnItemClickListener(adapter);

        chkDiscontinue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (chkDiscontinue.isChecked())
                DiscontinuePolicy();
        });

        btnSubmit.setOnClickListener(v -> {
            if (!chkDiscontinue.isChecked()) {

                pd = ProgressDialog.show(Renewal.this, "", getResources().getString(R.string.Uploading));
                final String[] renewal = {null};

                new Thread(() -> {
                    if (getIntent().getStringExtra("CHFID").equals(getResources().getString(R.string.UnlistedRenewalPolicies))) {
                        GetSelectedProduct();
                    }

                    if (!isValidate()) {
                        pd.dismiss();
                        return;
                    }
                    renewal[0] = WriteJSON();
                    WriteXML();
                    result = 3;

                    runOnUiThread(() -> {
                        ca = new ClientAndroidInterface(Renewal.this);
                        switch (result) {
                            case 1:
                                DeleteRow(RenewalId);
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.UploadedSuccessfully), Toast.LENGTH_LONG).show();
                                break;
                            case 2:
                                DeleteRow(RenewalId);
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.ServerRejected), Toast.LENGTH_LONG).show();
                                break;
                            case 3:
                                UpdateRow(RenewalId);
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.SavedOnSDCard), Toast.LENGTH_LONG).show();
                                break;
                            case -1:
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.RenewalNotUploaded), Toast.LENGTH_LONG).show();
                                break;
                        }
                        //Go back to the previous activity.
                        finish();
                    });

                    pd.dismiss();
                }).start();
            }
        });

        etOfficer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HashMap<String, String> Payer = new HashMap<>();
                String searchString = etOfficer.getText().toString();
                int LocID = ca.getLocationId(searchString);
                BindSpinnerPayersXXXX(LocID);
                BindSpinnerProduct();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private String GetSelectedPayer() {
        String Payer = "0";
        try {
            HashMap<String, String> P;
            //noinspection unchecked
            P = (HashMap<String, String>) spPayer.getSelectedItem();
            if (P.get("PayerId") == null) {
                Payer = "0";
            } else {
                Payer = P.get("PayerId");
            }

        } catch (Exception e) {
            // e.printStackTrace();
        }

        return Payer;
    }

    private String GetSelectedProduct() {
        String Product = "0";
        try {
            HashMap<String, String> P;
            //noinspection unchecked
            P = (HashMap<String, String>) spProduct.getSelectedItem();
            if (P.get("ProductCode") == null) {
                Product = "0";
            } else {
                Product = P.get("ProductCode");
                etProductCode.setText(Product);
            }

        } catch (Exception e) {
            // e.printStackTrace();
        }

        return Product;
    }

    private void WriteXML() {
        try {
            //Create All directories
            File MyDir = new File(global.getMainDirectory());

            //Create File name
            SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
            Calendar cal = Calendar.getInstance();
            String d = format.format(cal.getTime());
            FileName = "RenPol_" + d + "_" + etCHFID.getText().toString() + "_" + etReceiptNo.getText().toString() + ".xml";

            String PayerId = GetSelectedPayer();
            //Create XML file
            PolicyXML = new File(MyDir, FileName);

            FileOutputStream fos = new FileOutputStream(PolicyXML);

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

        //Create all the directories required
        File MyDir = new File(global.getMainDirectory());

        //Create a file name
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        FileName = "RenPolJSON_" + d + "_" + etCHFID.getText().toString() + "_" + etReceiptNo.getText().toString() + ".txt";
        String PayerId = GetSelectedPayer();
        String ProductCode = GetSelectedProduct();

        //Create XML file
        PolicyJSON = new File(MyDir, FileName);

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

            FullObject.put("Policy", RenewalObject);

            try {
                FileOutputStream fOut = new FileOutputStream(PolicyJSON);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(FullObject.toString());
                myOutWriter.close();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
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
                            String result;
                            if (global.isNetworkAvailable()) {
                                HttpResponse response = rest.deleteFromRestApiToken("policy/renew/" + RenewalUUID);
                                int responseCode = response.getStatusLine().getStatusCode();
                                result = rest.getContent(response);
                            } else {
                                WriteXML();
                            }
                            DeleteRow(RenewalId);
                            pd.dismiss();
                            finish();

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

    private void UpdateRow(int RenewalId) {
        ca.updateRenewTable(RenewalId);
    }

    private void DeleteRow(int RenewalId) {
        ca.deleteRenewalOfflineRow(RenewalId);
    }

    private void BindSpinnerPayersXXXX(int LocationId) {

        ca = new ClientAndroidInterface(this);
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
        ca = new ClientAndroidInterface(this);
        String result = ca.getPayer(LocationId);

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

                SimpleAdapter adapter = new SimpleAdapter(Renewal.this, PayersList, R.layout.spinnerpayer,
                        new String[]{"PayerId", "PayerName"},
                        new int[]{R.id.tvPayerId, R.id.tvPayerName});

                spPayer.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void BindSpinnerProduct() {
        ca = new ClientAndroidInterface(this);
        String result = ca.getProductsRD();

        JSONArray jsonArray = null;
        JSONObject object;

        try {
            jsonArray = new JSONArray(result);

            ProductList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                object = jsonArray.getJSONObject(i);

                // Enter an empty record
                if (i == 0) {
                    HashMap<String, String> Product = new HashMap<>();
                    Product.put("ProductCode", String.valueOf(0));
                    Product.put("ProductName", getResources().getString(R.string.SelectProduct));
                    ProductList.add(Product);
                }

                HashMap<String, String> Product = new HashMap<>();
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


        if (etReceiptNo.getText().length() == 0) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.MissingReceiptNo));
                etReceiptNo.requestFocus();
            });

            return false;
        }

        if (etProductCode.getText().length() == 0) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.MissingProductCode));
                etProductCode.requestFocus();
            });

            return false;
        }

        if (etAmount.getText().length() == 0) {
            runOnUiThread(() -> {
                ca.ShowDialog(getResources().getString(R.string.MissingAmount));
                etAmount.requestFocus();
            });

            return false;
        }

        if (global.isNetworkAvailable()) {
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
}
