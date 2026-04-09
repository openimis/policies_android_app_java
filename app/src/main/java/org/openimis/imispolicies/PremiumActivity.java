package org.openimis.imispolicies;

import android.content.DialogInterface;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.util.AndroidUtils;
import org.openimis.imispolicies.util.JsonDropdownHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PremiumActivity extends AppCompatActivity {

    ClientAndroidInterface ca;
    private TextInputLayout layoutPremiumAmount, layoutReceiptNo, layoutPayDate, layoutPaymentType;
    private TextView spContribution, spBalance, txtPolicyValue, txtPremiumPolicyStatus;
    private MaterialButton btnSavePremium;
    private MaterialAutoCompleteTextView ddlPayer, ddlPhotoFee, ddlPayType;
    private TextInputEditText txtPremiumAmount, txtPremiumReceipt, txtPremiumPayDate;
    private MaterialDatePicker<Long> datePicker;
    private int policyId, familyId, regionId, districtId, premiumId;
    private JSONObject premiumObject, policyObject;
    int photoValue = 1;
    int previousAmount = 0;
    final int IdlePolicy = 1;
    final int ActivePolicy = 2;
    final int SuspendedPolicy = 4;
    final int ExpiredPolicy = 8;
    final int ReadyPolicy = 16;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);
        setTitle(R.string.AddEditPremium);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ca = new ClientAndroidInterface(this);
        policyId = getIntent().getIntExtra("PolicyId", 0);
        familyId = getIntent().getIntExtra("FamilyId", 0);
        regionId = getIntent().getIntExtra("RegionId", 0);
        districtId = getIntent().getIntExtra("DistrictId", 0);
        premiumId = getIntent().getIntExtra("PremiumId", 0);
        initViews();
        setRequiredFields();
        setupDatePicker();
        setupListenners();
        getPolicyValue();
        if(premiumId != 0){
            loadInitialData();
        } else {
            int policyValue = ca.getPolicyVal(String.valueOf(policyId));
            int prevAmount = ca.getSumPrem(String.valueOf(policyId));
            int currentBalance = policyValue - prevAmount;

            if (currentBalance <= 0) {
                showConfirmDialog();
            }
        }
        loadPayers();
        loadPhotoFees();
        loadPaymentTypes();
        canSave();

        String adj = ca.getSpecificControlHtml("TotalAmount");
        if (adj.equals("M") || adj.equals("R")) {
            layoutPremiumAmount.setEnabled(false);
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

    private void initViews(){
        layoutPremiumAmount = findViewById(R.id.layoutPremiumAmount);
        layoutReceiptNo = findViewById(R.id.layoutReceiptNo);
        layoutPayDate = findViewById(R.id.layoutPayDate);
        layoutPaymentType = findViewById(R.id.layoutPaymentType);
        spContribution = findViewById(R.id.spContribution);
        spBalance = findViewById(R.id.spBalance);
        txtPolicyValue = findViewById(R.id.txtPolicyValue);
        txtPremiumPolicyStatus = findViewById(R.id.txtPremiumPolicyStatus);
        btnSavePremium = findViewById(R.id.btnSavePremium);
        ddlPayer = findViewById(R.id.ddlPayer);
        ddlPhotoFee = findViewById(R.id.ddlPhotoFee);
        ddlPayType = findViewById(R.id.ddlPayType);
        txtPremiumAmount = findViewById(R.id.txtPremiumAmount);
        txtPremiumReceipt = findViewById(R.id.txtPremiumReceipt);
        txtPremiumPayDate = findViewById(R.id.txtPremiumPayDate);
    }

    private void setRequiredFields(){
        layoutPremiumAmount.setError(" ");
        layoutReceiptNo.setError(" ");
        layoutPayDate.setError(" ");
        layoutPaymentType.setError(" ");
    }

    private void setupDatePicker(){
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(getResources().getString(R.string.PayDate));
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            txtPremiumPayDate.setText(format.format(calendar.getTime()));
            canSave();
        });

        txtPremiumPayDate.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupListenners(){
        btnSavePremium.setOnClickListener(v ->{

        });
    }

    private void canSave(){
        if(txtPremiumPayDate.getText().toString().isEmpty() ||
                txtPremiumReceipt.getText().toString().isEmpty() ||
                txtPremiumAmount.getText().toString().isEmpty() ||
                ddlPayType.getText().toString().isEmpty()
        ){
            btnSavePremium.setEnabled(false);
        } else {
            btnSavePremium.setEnabled(true);
        }
    }

    private void loadPayers(){
        try {
            String payers = ca.getPayers(regionId, districtId);
            JSONArray payersArray = new JSONArray(payers);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlPayer,
                    payersArray,
                    "PayerName",
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    int payerId = selectedItem.getInt("PayerId");
                                    premiumObject.put("ddlPayer", payerId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(premiumObject != null && premiumObject.has("PayerId") && premiumObject.getInt("PayerId") != 0){
                int payerId = premiumObject.getInt("PayerId");
                premiumObject.put("ddlPayer", payerId);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlPayer,
                        payersArray,
                        "PayerName",
                        "PayerId",
                        String.valueOf(payerId)
                );
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void loadPhotoFees(){
        try {
            JSONArray photoFees = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("value", "true");
            object.put("key", getResources().getString(R.string.PremiumPhotoFee));
            photoFees.put(object);

            object = new JSONObject();
            object.put("value", "false");
            object.put("key", getResources().getString(R.string.PremiumContribution));
            photoFees.put(object);

            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlPhotoFee,
                    photoFees,
                    "key",
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    String isPhotoFee = selectedItem.getString("value");
                                    premiumObject.put("ddlPhotoFee", isPhotoFee);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(premiumObject != null && premiumObject.has("isPhotoFee") && premiumObject.getString("isPhotoFee") != null){
                String isPhotoFee = premiumObject.getString("isPhotoFee");
                premiumObject.put("ddlPhotoFee", isPhotoFee);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlPhotoFee,
                        photoFees,
                        "key",
                        "value",
                        isPhotoFee
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadPaymentTypes(){
        try{
            JSONArray paymentTypes = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("value", "C");
            object.put("key", getResources().getString(R.string.Cash));
            paymentTypes.put(object);

            object = new JSONObject();
            object.put("value", "M");
            object.put("key", getResources().getString(R.string.MobilePhone));
            paymentTypes.put(object);

            object = new JSONObject();
            object.put("value", "B");
            object.put("key", getResources().getString(R.string.BankTransfer));
            paymentTypes.put(object);

            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlPayType,
                    paymentTypes,
                    "key",
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    String payType = selectedItem.getString("value");
                                    premiumObject.put("ddlPayType", payType);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(premiumObject != null && premiumObject.has("PayType") && premiumObject.getString("PayType") != null){
                String payType = premiumObject.getString("PayType");
                premiumObject.put("ddlPayType", payType);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlPayType,
                        paymentTypes,
                        "key",
                        "value",
                        payType
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadInitialData(){
        try {
            String strPremium = ca.getPremium(premiumId);
            JSONArray array = new JSONArray(strPremium);
            premiumObject = array.getJSONObject(0);
            previousAmount = premiumObject.getInt("Amount");
            int policyValue = ca.getPolicyVal(String.valueOf(policyId));
            int balance = premiumObject.getInt("Balance");
            int contribution = premiumObject.getInt("Contribution");

            txtPremiumPayDate.setText(premiumObject.getString("PayDate"));
            txtPremiumReceipt.setText(premiumObject.getString("ReceiptNo"));
            txtPremiumAmount.setText(String.valueOf(previousAmount));
            int prevAmount = ca.getSumPrem(String.valueOf(policyId));
            int currentBalance = policyValue - prevAmount;
            int currentContribution = contribution - previousAmount;
            String isOffline = premiumObject.getString("isOffline");

            spBalance.setText(currentBalance);
            spContribution.setText(currentContribution);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void getPolicyValue(){
        String strPolicy = ca.getPolicy(policyId);
        try {
            JSONArray array = new JSONArray(strPolicy);
            policyObject = array.getJSONObject(0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void showConfirmDialog() {
        AndroidUtils.showDialog(
                this,
                null,
                getResources().getString(R.string.PolicyCovered),
                false,
                getResources().getString(R.string.Yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                },
                null,
                null,
                getResources().getString(R.string.No),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }
        );
    }
}