package org.openimis.imispolicies;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SearchOverViewControlNumber extends AppCompatActivity {

    Button btnSearch;
    Button btnClear;
    EditText Insurance_Number;
    EditText Other_Names;
    EditText Last_Name;
    Spinner Insurance_Product;
    TextView Uploaded_From;
    TextView Uploaded_To;
    TextView Requested_From;
    TextView Requested_To;
    static String PayType = "";

    RadioButton Renewal_Yes;
    RadioButton Renewal_No;
    RadioButton Sms_Yes;
    RadioButton Sms_No;
    RadioGroup Radio_Renewal;
    RadioGroup Radio_Sms;

    RadioButton Requested_Yes;
    RadioButton Requested_No;
    RadioGroup Radio_Requested;

    Calendar uploadedFromCalendar;
    Calendar uploadedToCalendar;
    Calendar requestedFromCalendar;
    Calendar requestedToCalendar;

    Spinner PaymentSpinner;

    ClientAndroidInterface clientAndroidInterface;

    ListView lv1;
    private ArrayList<HashMap<String, String>> ProductList = new ArrayList<>();
    private RadioButton radioButtonRenewal;
    private RadioButton radioButtonSms;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_over_view_control_number);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.SearchControlNumbers));
        }

        clientAndroidInterface = new ClientAndroidInterface(this);
        lv1 = findViewById(R.id.lv1);

        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);

        Uploaded_From = findViewById(R.id.uploaded_from);
        Uploaded_To = findViewById(R.id.uploaded_to);
        Requested_From = findViewById(R.id.requested_from);
        Requested_To = findViewById(R.id.requested_to);
        Insurance_Number = findViewById(R.id.insurance_number);
        Insurance_Product = findViewById(R.id.insurance_product);
        Other_Names = findViewById(R.id.other_names);
        Last_Name = findViewById(R.id.last_name);
        Renewal_Yes = findViewById(R.id.renewal_yes);
        Renewal_No = findViewById(R.id.renewal_no);
        Sms_Yes = findViewById(R.id.sms_required_yes);
        Sms_No = findViewById(R.id.sms_required_no);
        Radio_Renewal = findViewById(R.id.radio_renewal);
        Radio_Sms = findViewById(R.id.radio_sms);
        Requested_Yes = findViewById(R.id.requested_yes);
        Requested_No = findViewById(R.id.requested_no);
        Radio_Requested = findViewById(R.id.radio_requested);

        PaymentSpinner = findViewById(R.id.payment_type);

        addItemsOnSpinner2();
        addListenerOnSpinnerItemSelection();
        BindSpinnerProduct();

        btnSearch.setOnClickListener(view -> {
            String InsuranceNumber = Insurance_Number.getText().toString();
            String OtherNames = Other_Names.getText().toString();
            String LastName = Last_Name.getText().toString();
            String InsuranceProduct = GetSelectedProduct();
            String UploadedFrom = Uploaded_From.getText().toString();
            String UploadedTo = Uploaded_To.getText().toString();
            String RequestedFrom = Requested_From.getText().toString();
            String RequestedTo = Requested_To.getText().toString();
            String PaymentType = PayType;
            String RadioRenewal = "";
            String RadioSms = "";

            if (Renewal_Yes.isChecked() || Renewal_No.isChecked()) {
                int selectedId = Radio_Renewal.getCheckedRadioButtonId();
                radioButtonRenewal = findViewById(selectedId);
                RadioRenewal = radioButtonRenewal.getText().toString();
            }

            if (Sms_Yes.isChecked() || Sms_No.isChecked()) {
                int selectedId = Radio_Sms.getCheckedRadioButtonId();
                radioButtonSms = findViewById(selectedId);
                if (radioButtonSms.getText().toString().equals("Y")) {
                    RadioSms = "1";
                } else if (radioButtonSms.getText().toString().equals("N")) {
                    RadioSms = "0";
                }
            }

            Intent intent = new Intent(SearchOverViewControlNumber.this, OverViewControlNumbers.class);
            intent.putExtra("RENEWAL", RadioRenewal);
            intent.putExtra("SMS", RadioSms);
            intent.putExtra("INSURANCE_NUMBER", InsuranceNumber);
            intent.putExtra("OTHER_NAMES", OtherNames);
            intent.putExtra("LAST_NAME", LastName);
            intent.putExtra("INSURANCE_PRODUCT", InsuranceProduct);
            intent.putExtra("UPLOADED_FROM", UploadedFrom);
            intent.putExtra("UPLOADED_TO", UploadedTo);
            intent.putExtra("REQUESTED_FROM", RequestedFrom);
            intent.putExtra("REQUESTED_TO", RequestedTo);
            intent.putExtra("PAYMENT_TYPE", PaymentType);
            startActivity(intent);
        });

        btnClear.setOnClickListener(view -> {
            Uploaded_From.setText("");
            Uploaded_To.setText("");
            Requested_From.setText("");
            Requested_To.setText("");
            Insurance_Number.setText("");
            Insurance_Product.setSelection(0);
            Other_Names.setText("");
            Last_Name.setText("");
            Radio_Renewal.clearCheck();
            Radio_Sms.clearCheck();
            lv1.setVisibility(View.GONE);

            Calendar today = Calendar.getInstance();

            uploadedFromCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            uploadedToCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            requestedFromCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            requestedToCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        });

        uploadedFromCalendar = Calendar.getInstance();
        uploadedToCalendar = Calendar.getInstance();
        requestedFromCalendar = Calendar.getInstance();
        requestedToCalendar = Calendar.getInstance();

        Uploaded_From.setOnClickListener(v ->
                new DatePickerDialog(SearchOverViewControlNumber.this,
                        (view, year, monthOfYear, dayOfMonth) -> {
                            uploadedFromCalendar.set(Calendar.YEAR, year);
                            uploadedFromCalendar.set(Calendar.MONTH, monthOfYear);
                            uploadedFromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateLabel(uploadedFromCalendar, Uploaded_From);
                        },
                        uploadedFromCalendar.get(Calendar.YEAR),
                        uploadedFromCalendar.get(Calendar.MONTH),
                        uploadedFromCalendar.get(Calendar.DAY_OF_MONTH)
                ).show());

        Uploaded_To.setOnClickListener(v ->
                new DatePickerDialog(SearchOverViewControlNumber.this,
                        (view, year, monthOfYear, dayOfMonth) -> {
                            uploadedToCalendar.set(Calendar.YEAR, year);
                            uploadedToCalendar.set(Calendar.MONTH, monthOfYear);
                            uploadedToCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateLabel(uploadedToCalendar, Uploaded_To);
                        },
                        uploadedToCalendar.get(Calendar.YEAR),
                        uploadedToCalendar.get(Calendar.MONTH),
                        uploadedToCalendar.get(Calendar.DAY_OF_MONTH)
                ).show());

        Requested_From.setOnClickListener(v ->
                new DatePickerDialog(SearchOverViewControlNumber.this,
                        (view, year, monthOfYear, dayOfMonth) -> {
                            requestedFromCalendar.set(Calendar.YEAR, year);
                            requestedFromCalendar.set(Calendar.MONTH, monthOfYear);
                            requestedFromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateLabel(requestedFromCalendar, Requested_From);
                        },
                        requestedFromCalendar.get(Calendar.YEAR),
                        requestedFromCalendar.get(Calendar.MONTH),
                        requestedFromCalendar.get(Calendar.DAY_OF_MONTH)
                ).show());

        Requested_To.setOnClickListener(v ->
                new DatePickerDialog(SearchOverViewControlNumber.this,
                        (view, year, monthOfYear, dayOfMonth) -> {
                            requestedToCalendar.set(Calendar.YEAR, year);
                            requestedToCalendar.set(Calendar.MONTH, monthOfYear);
                            requestedToCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateLabel(requestedToCalendar, Requested_To);
                        },
                        requestedToCalendar.get(Calendar.YEAR),
                        requestedToCalendar.get(Calendar.MONTH),
                        requestedToCalendar.get(Calendar.DAY_OF_MONTH)
                ).show());
    }

    // add items into spinner dynamically
    public void addItemsOnSpinner2() {

        List<String> list = new ArrayList<>();
        list.add("");
        list.add("Bank Transfer");
        list.add("Mobile Phone");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        PaymentSpinner.setAdapter(dataAdapter);
    }

    public void addListenerOnSpinnerItemSelection() {
        PaymentSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void BindSpinnerProduct() {
        clientAndroidInterface = new ClientAndroidInterface(this);

        String result = clientAndroidInterface.getProductsRD();
        //result = clientAndroidInterface.getProductsByDistrict(clientAndroidInterface.getLocationId());

        JSONArray jsonArray = null;
        JSONObject object;

        try {
            jsonArray = new JSONArray(result);

            ProductList.clear();

            if (jsonArray.length() == 0) {
                HashMap<String, String> Product = new HashMap<>();
                Product.put("ProductCode", "");
                Product.put("ProductName", getResources().getString(R.string.SelectProduct));
                ProductList.add(Product);

                SimpleAdapter adapter = new SimpleAdapter(SearchOverViewControlNumber.this, ProductList, R.layout.spinnerproducts,
                        new String[]{"ProductCode", "ProductName"},
                        new int[]{R.id.tvProductCode, R.id.tvProductName});

                Insurance_Product.setAdapter(adapter);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getJSONObject(i);

                    // Enter an empty record
                    if (i == 0) {
                        HashMap<String, String> Product = new HashMap<>();
                        Product.put("ProductCode", "");
                        Product.put("ProductName", getResources().getString(R.string.SelectProduct));
                        ProductList.add(Product);
                    }

                    HashMap<String, String> Product = new HashMap<>();
                    Product.put("ProductCode", object.getString("ProductCode"));
                    Product.put("ProductName", object.getString("ProductName"));


                    ProductList.add(Product);

                    SimpleAdapter adapter = new SimpleAdapter(SearchOverViewControlNumber.this, ProductList, R.layout.spinnerproducts,
                            new String[]{"ProductCode", "ProductName"},
                            new int[]{R.id.tvProductCode, R.id.tvProductName});
                    Insurance_Product.setAdapter(adapter);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateLabel(Calendar calendar, TextView view) {
        SimpleDateFormat formatter = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        view.setText(formatter.format(calendar.getTime()));
    }

    private String GetSelectedProduct() {
        String Product = "";
        try {
            HashMap<String, String> P;
            //noinspection unchecked
            P = (HashMap<String, String>) Insurance_Product.getSelectedItem();
            Product = P.get("ProductCode");
            if (Product == null || ("0").equals(Product)) {
                Product = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Product;
    }
}
