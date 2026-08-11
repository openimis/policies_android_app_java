package org.openimis.imispolicies;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.util.AndroidUtils;
import org.openimis.imispolicies.util.JsonDropdownHelper;

import java.text.ParseException;
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
    ProgressDialog pd;
    String isPhotoFee = "";
    int currentBalance;
    String policyStatus;
    String payDate;
    String isOffline;


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
        premiumObject = new JSONObject();
        initViews();
        setRequiredFields();
        setupDatePicker();
        setupListenners();
        getPolicyValue();
        loadPayers();
        if(premiumId != 0){
            loadInitialData();
        } else {
            int policyValue = ca.getPolicyVal(String.valueOf(policyId));
            int prevAmount = ca.getSumPrem(String.valueOf(policyId));
            currentBalance = policyValue - prevAmount;
            if (currentBalance <= 0) {
                showConfirmDialog();
            }
            txtPolicyValue.setText(String.valueOf(policyValue));
            txtPremiumPolicyStatus.setText(policyStatus);
        }

        spBalance.setText(String.valueOf(currentBalance));
        txtPremiumPolicyStatus.setText(policyStatus);
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
            payDate = format.format(calendar.getTime());
            txtPremiumPayDate.setText(payDate);
            canSave();
        });

        txtPremiumPayDate.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupListenners(){
        btnSavePremium.setOnClickListener(v ->{
            savePremium();
        });
        txtPremiumAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isPhotoFee.equals("true")){
                    int ogContribution = Integer.parseInt(spContribution.getText().toString());
                    var Amount = Integer.parseInt(txtPremiumAmount.getText().toString());
                    int newBalance;
                    if (premiumId != 0) {
                        int policyValue = ca.getPolicyVal(String.valueOf(policyId));
                        int prevAmount = ca.getSumPrem(String.valueOf(policyId));
                        int balance = policyValue - prevAmount;
                        int newAmt = balance + previousAmount;
                        newBalance = newAmt - Amount;
                    } else {
                        int policyValue = ca.getPolicyVal(String.valueOf(policyId));
                        int prevAmount = ca.getSumPrem(String.valueOf(policyId));
                        var currentBalance = policyValue - prevAmount;
                        newBalance = currentBalance - Amount;
                    }

                    int newContribution = Amount + ogContribution;

                    spBalance.setText(String.valueOf(newBalance));
                    spContribution.setText(String.valueOf(newContribution));
                    canSave();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        txtPremiumReceipt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                canSave();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
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
                                    isPhotoFee = selectedItem.getString("value");
                                    premiumObject.put("ddlPhotoFee", isPhotoFee);
                                    int balance = Integer.parseInt(spBalance.getText().toString());
                                    int contribution = Integer.parseInt(spContribution.getText().toString());
                                    if (isPhotoFee.equals("true")) {
                                        photoValue = 0;
                                        spContribution.setText("0");
                                    } else {
                                        if (balance < 0 || currentBalance < 0) {
                                            txtPremiumAmount.setText("0");
                                            contribution = 0;
                                        } else {
                                            txtPremiumAmount.setText(String.valueOf(currentBalance));
                                            contribution = currentBalance;
                                        }
                                        spContribution.setText(String.valueOf(contribution));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(premiumObject != null && premiumObject.has("isPhotoFee") && premiumObject.getString("isPhotoFee") != null){
                isPhotoFee = premiumObject.getString("isPhotoFee");
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
                                    canSave();
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
            payDate = premiumObject.getString("PayDate");
            txtPremiumPayDate.setText(payDate);
            txtPremiumReceipt.setText(premiumObject.getString("Receipt"));
            txtPremiumAmount.setText(String.valueOf(previousAmount));
            txtPolicyValue.setText(String.valueOf(policyValue));
            int prevAmount = ca.getSumPrem(String.valueOf(policyId));
            currentBalance = policyValue - prevAmount;
            int currentContribution = policyValue - currentBalance;
            isOffline = premiumObject.getString("isOffline");
            policyStatus = policyObject.getString("PolicyStatus");
            spBalance.setText(String.valueOf(currentBalance));
            spContribution.setText(String.valueOf(currentContribution));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void getPolicyValue(){
        String strPolicy = ca.getPolicy(policyId);
        try {
            JSONArray array = new JSONArray(strPolicy);
            policyObject = array.getJSONObject(0);
            policyStatus = policyObject.getString("PolicyStatus");
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

    private void showLoadingDialog(){
        pd = new ProgressDialog(this);
        pd.setTitle(getResources().getString(R.string.saving));
        pd.setMessage(getResources().getString(R.string.Loading));
        pd.setCancelable(false);
        pd.show();
    }

    private void savePremium(){
        showLoadingDialog();

        new Thread(()->{
            try{
                boolean results = true;
                String ReceiptNo = txtPremiumReceipt.getText().toString();
                boolean IsReceiptUnique = ca.IsReceiptNumberUnique(ReceiptNo, familyId);
                String PolicyBalance = spBalance.getText().toString();

                premiumObject.put("txtPayDate", payDate);
                premiumObject.put("txtAmount", txtPremiumAmount.getText().toString());
                premiumObject.put("txtReceipt", ReceiptNo);

                if (IsReceiptUnique || premiumId != 0){
                    if(isPhotoFee.equals("true")){
                        int PremiumId = ca.SavePremiums(premiumObject.toString(), policyId, premiumId, familyId);
                        if(PremiumId != 0){
                            finish();
                        } else {
                            AndroidUtils.showToast(this, "Error occurred during the process");
                        }
                        pd.dismiss();
                        finish();
                    } else {
                        if (premiumId <= 0 && Integer.parseInt(PolicyBalance) > 0){
                            var maxInstallments = ca.getMaxInstallments(String.valueOf(policyId));
                            var totalPremiums = ca.getCountPremiums(String.valueOf(policyId));
                            if (Integer.parseInt(PolicyBalance) > 0 && totalPremiums < maxInstallments){
                                if (totalPremiums == (maxInstallments - 1) && isPhotoFee.equals("false")){
                                    runOnUiThread(() -> showMaxInstallementDialog());
                                } else {
                                    runOnUiThread(()->showPriceBelowDialog());
                                }
                            } else {
                                if (Integer.parseInt(PolicyBalance) <= 0) {
                                    ca.UpdatePolicy(policyId, payDate, ActivePolicy);
                                    ca.UpdateInsureePolicy(policyId);
                                    pd.dismiss();
                                    finish();
                                } else if (Integer.parseInt(PolicyBalance) > 0) {
                                    runOnUiThread(()->showPriceBelowDialog());
                                } else if (results != false) {
                                    finish();
                                }
                            }
                        } else if (Integer.parseInt(PolicyBalance) < 0) {
                            runOnUiThread(()->showExceedsPolicyDialog());
                        } else if (results == true) {
                            Log.e("je suis ici", "je suis ici");
                            ca.SavePremiums(premiumObject.toString(), policyId, premiumId, familyId);
                            int PolicyStatus = Integer.parseInt(PolicyBalance) > 0 ? IdlePolicy : ActivePolicy;
                            ca.UpdatePolicy(policyId, payDate, PolicyStatus);
                            ca.UpdateInsureePolicy(policyId);
                            finish();
                        }
                    }
                } else {
                    AndroidUtils.showDialog(this, getResources().getString(R.string.ReceiptNotUnique));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showMaxInstallementDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(ca.getString("MaxInstallment"));
        builder.setCancelable(false);

        builder.setPositiveButton(ca.getString("Wait"), (dialog, which) -> {
            try {
                ca.SavePremiums(
                        premiumObject.toString(),
                        policyId,
                        premiumId,
                        familyId
                );
                ca.UpdatePolicy(policyId, payDate, IdlePolicy);
                ca.UpdateInsureePolicy(policyId);
                finish();
                dialog.dismiss();
                pd.dismiss();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        builder.setNeutralButton(ca.getString("Suspend"), (dialog, which) -> {
            try {
                ca.SavePremiums(
                        premiumObject.toString(),
                        policyId,
                        premiumId,
                        familyId
                );
                ca.UpdatePolicy(policyId, payDate, SuspendedPolicy);
                ca.UpdateInsureePolicy(policyId);
                finish();
                dialog.dismiss();
                pd.dismiss();
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        });

        builder.setNegativeButton(ca.getString("Enforce"), (dialog, which) -> {
            try {
                ca.SavePremiums(
                        premiumObject.toString(),
                        policyId,
                        premiumId,
                        familyId
                );
                ca.UpdatePolicy(policyId, payDate, ActivePolicy);
                ca.UpdateInsureePolicy(policyId);
                finish();
                dialog.dismiss();
                pd.dismiss();
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        });

        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button noButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        noButton.setText(ca.getString("No"));
        noButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void showPriceBelowDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(ca.getString("PriceBelow"));
        builder.setCancelable(false);

        builder.setPositiveButton(ca.getString("Ok"), (dialog, which) -> {
            try {
                ca.SavePremiums(
                        premiumObject.toString(),
                        policyId,
                        premiumId,
                        familyId
                );
                ca.UpdatePolicy(policyId, payDate, IdlePolicy);
                ca.UpdateInsureePolicy(policyId);
                finish();
                dialog.dismiss();
                pd.dismiss();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        builder.setNeutralButton(ca.getString("Enforce"), (dialog, which) -> {
            try {
                ca.SavePremiums(
                        premiumObject.toString(),
                        policyId,
                        premiumId,
                        familyId
                );
                btnSavePremium.setEnabled(false);
                ca.UpdatePolicy(policyId, payDate, ActivePolicy);
                ca.UpdateInsureePolicy(policyId);
                finish();
                dialog.dismiss();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        builder.setNegativeButton(ca.getString("No"), (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showExceedsPolicyDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(ca.getString("ExceedsPolicy"));
        builder.setCancelable(false);

        builder.setPositiveButton(ca.getString("Ok"), (dialog, which) -> {
            try {
                ca.SavePremiums(
                        premiumObject.toString(),
                        policyId,
                        premiumId,
                        familyId
                );
                ca.UpdatePolicy(policyId, payDate, ActivePolicy);
                ca.UpdateInsureePolicy(policyId);
                pd.dismiss();
                finish();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        builder.setNegativeButton(ca.getString("Cancel"), (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}