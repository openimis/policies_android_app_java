package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.util.JsonDropdownHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PolicyActivity extends AppCompatActivity {

    private TextInputLayout layoutEnrolmentDate, layoutProduct, layoutEffectiveDate, layoutStartDate, layoutExpiryDate, layoutControlNumber;
    private TextView txtPolicyStatus, spPolicyValue, spContribution, spBalance;
    private MaterialButton btnSave;
    private MaterialAutoCompleteTextView ddlProduct;
    private TextInputEditText txtEnrolmentDate, txtEffectiveDate, txtStartDate, txtExpiryDate, AssignedControlNumber;
    private JSONObject policyObject;
    private int policyId = 0;
    private int familyId;
    ClientAndroidInterface ca;
    boolean hasCycle;
    private int officerId, regionId, districtId, productId;
    private MaterialDatePicker<Long> datePicker;
    private String isOffline = "1";
    private String policyStatus = "1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        setTitle(getResources().getString(R.string.AddEditPolicy));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initViews();
        setupListeners();
        setupDatePicker();
        defineRequiredField();

        policyObject = new JSONObject();
        ca = new ClientAndroidInterface(this);
        policyId = getIntent().getIntExtra("PolicyId", 0);
        familyId = getIntent().getIntExtra("FamilyId", 0);
        regionId = getIntent().getIntExtra("RegionId", 0);
        districtId = getIntent().getIntExtra("DistrictId", 0);

        if(!ca.IsBulkCNUsed()){
            layoutControlNumber.setVisibility(View.GONE);
        }
        layoutEffectiveDate.setEnabled(false);
        txtExpiryDate.setEnabled(false);
        officerId = ca.getOfficerId();

        if(policyId != 0){
            loadInitialData();
        }
        loadProducts(regionId, districtId, null);
        canSave();
    }

    private void initViews(){
        layoutEnrolmentDate = findViewById(R.id.layoutEnrolmentDate);
        layoutProduct = findViewById(R.id.layoutProduct);
        layoutEffectiveDate = findViewById(R.id.layoutEffectiveDate);
        layoutStartDate = findViewById(R.id.layoutStartDate);
        layoutExpiryDate = findViewById(R.id.layoutExpiryDate);
        layoutControlNumber = findViewById(R.id.layoutControlNumber);
        txtPolicyStatus = findViewById(R.id.txtPolicyStatus);
        spPolicyValue = findViewById(R.id.spPolicyValue);
        spContribution = findViewById(R.id.spContribution);
        spBalance = findViewById(R.id.spBalance);
        btnSave = findViewById(R.id.btnSavePolicy);
        ddlProduct = findViewById(R.id.ddlProduct);
        txtEnrolmentDate = findViewById(R.id.txtEnrolmentDate);
        txtEffectiveDate = findViewById(R.id.txtEffectiveDate);
        txtStartDate = findViewById(R.id.txtStartDate);
        txtExpiryDate = findViewById(R.id.txtExpiryDate);
        AssignedControlNumber = findViewById(R.id.AssignedControlNumber);
    }

    private void setupListeners(){
        txtEnrolmentDate.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePolicy();
            }
        });
    }

    private void defineRequiredField(){
        layoutEnrolmentDate.setError(" ");
        layoutProduct.setError(" ");
        layoutStartDate.setError(" ");
        layoutExpiryDate.setError(" ");
    }

    private void canSave(){
        if(txtEnrolmentDate.getText().toString().isEmpty() ||
                txtStartDate.getText().toString().isEmpty() ||
                txtExpiryDate.getText().toString().isEmpty() ||
                ddlProduct.getText().toString().isEmpty()
        ){
            btnSave.setEnabled(false);
        } else {
            btnSave.setEnabled(true);
        }
    }

    private void loadProducts(int regionId, int districtId, String enrolmentDate){
        try {
            String products = ca.getProducts(regionId, districtId, enrolmentDate);
            JSONArray productsArray = new JSONArray(products);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlProduct,
                    productsArray,
                    "ProductNameCombined",
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    productId = selectedItem.getInt("ProdId");
                                    String enrolDate = txtEnrolmentDate.getText().toString().trim();
                                    policyObject.put("ddlProduct", productId);
                                    getPolicyPeriod(enrolDate, productId, familyId, policyId);
                                    canSave();

                                    if(ca.IsBulkCNUsed()) {
                                        if(productId == 0) {
                                            AssignedControlNumber.setText("");
                                        }
                                        String controlNumber = ca.GetNextBulkCn(String.valueOf(productId));
                                        if(controlNumber.equals("undefined")) {
                                            ca.ShowDialog(getResources().getString(R.string.noBulkCNAvailable));
                                            AssignedControlNumber.setText("");
                                        } else {
                                            AssignedControlNumber.setText("");
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(policyObject != null && policyObject.has("ProdId") && policyObject.getInt("ProdId") != 0){
                productId = policyObject.getInt("ProdId");
                policyObject.put("ddlProduct", productId);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlProduct,
                        productsArray,
                        "ProductNameCombined",
                        "ProdId",
                        String.valueOf(productId)
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupDatePicker() {
        // Create Material Date Picker
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(getResources().getString(R.string.EnrolmentDate));
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            txtEnrolmentDate.setText(format.format(calendar.getTime()));
            String enrolDate = txtEnrolmentDate.getText().toString().trim();
            loadProducts(regionId, districtId, enrolDate);
            if(productId != 0){
                getPolicyPeriod(format.format(calendar.getTime()), productId, familyId, policyId);
            }
            canSave();
        });

        txtEnrolmentDate.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void getPolicyPeriod(String EnrolmentDate, int ProdId, int FamilyId, int policyId){
        if(EnrolmentDate.length() == 0 || ProdId == 0){
            return;
        }
        try {
            String period = ca.getPolicyPeriod(ProdId, EnrolmentDate);
            JSONArray periodArray = new JSONArray(period);
            String startDate = periodArray.getJSONObject(0).getString("StartDate");
            String expiryDate = periodArray.getJSONObject(0).getString("ExpiryDate");
            hasCycle = periodArray.getJSONObject(0).getBoolean("HasCycle");

            txtStartDate.setText(startDate);
            txtExpiryDate.setText(expiryDate);

            txtStartDate.setEnabled(hasCycle);

            double PolicyValue = ca.getPolicyValue(EnrolmentDate, ProdId, FamilyId, startDate, hasCycle, 0, "N", isOffline);
            spPolicyValue.setText(String.valueOf(PolicyValue));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void savePolicy(){
        try {
            policyObject.put("ddlOfficer",officerId);
            policyObject.put("AssignedControlNumber", AssignedControlNumber.getText());
            policyObject.put("hfPolicyValue", spPolicyValue.getText());
            policyObject.put("hfPolicyStatus", txtPolicyStatus.getText());
            policyObject.put("txtExpiryDate", txtExpiryDate.getText());
            policyObject.put("txtEnrolmentDate", txtEnrolmentDate.getText());
            policyObject.put("txtStartDate", txtStartDate.getText());
            policyObject.put("txtEffectiveDate", txtEffectiveDate.getText());
            policyObject.put("hfPolicyStatus", policyStatus);

            policyId = ca.SavePolicy(policyObject.toString(), familyId, policyId);
            if(policyId > 0){
                FragmentActivity activity = (FragmentActivity) this;
                FragmentManager fm = activity.getSupportFragmentManager();
                Bundle result = new Bundle();
                result.putBoolean("refresh_policies", true);
                fm.setFragmentResult("requestKey", result);
                finish();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadInitialData(){
        try {
            String policy = ca.getPolicy(policyId);
            JSONArray array = new JSONArray(policy);
            policyObject = array.getJSONObject(0);
            String EnrollDate = policyObject.getString("EnrollDate");
            isOffline = policyObject.getString("isOffline");
            String formattedDate = "";
            try {
                if(isOffline.equals("0") || isOffline.equals("2") || isOffline.equals("false")){
                    SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                    Date date = inputFormat.parse(EnrollDate);

                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    formattedDate = outputFormat.format(date);
                } else {
                    formattedDate = EnrollDate;
                }
            } catch (ParseException e){
                e.printStackTrace();
            }
            productId = policyObject.getInt("ProdId");
            officerId = policyObject.getInt("OfficerId");
            txtStartDate.setText(policyObject.getString("StartDate"));
            txtEnrolmentDate.setText(formattedDate);
            txtExpiryDate.setText(policyObject.getString("ExpiryDate"));
            txtPolicyStatus.setText(policyObject.getString("PolicyStatus"));
            spBalance.setText(policyObject.getString("Balance"));
            spContribution.setText(policyObject.getString("Contribution"));
            policyStatus = policyObject.getString("PolicyStatusValue");
            double currentPolicyValue = policyObject.getDouble("PolicyValue");
            spPolicyValue.setText(String.valueOf(currentPolicyValue));
            String policyStage = policyObject.getString("PolicyStage");
            if(ca.IsBulkCNUsed()){
                if(!policyObject.getString("ControlNumber").isEmpty()){
                    AssignedControlNumber.setText(policyObject.getString("ControlNumber"));
                } else {
                    AssignedControlNumber.setText("");
                }
            }

            double NewPolicyValue = ca.getPolicyValue(txtEnrolmentDate.getText().toString(), productId, familyId, txtStartDate.getText().toString(), hasCycle, policyId, policyStage, isOffline);
            if (NewPolicyValue != currentPolicyValue) {
                Date Vdate = new Date(txtEnrolmentDate.getText().toString());  //or your date here
                var NewDate = ((Vdate.getMonth() + 1) + '/' + Vdate.getDate() + '/' + Vdate.getYear());
                ca.ShowDialog(ca.getString("PolicyValueChange") + NewDate + ' ' + ca.getString("Changed"));
            }

            if (policyStatus.equals("1")) {
                layoutExpiryDate.setEnabled(true);
            } else {
                layoutExpiryDate.setEnabled(false);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}