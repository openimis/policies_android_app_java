package org.openimis.imispolicies;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import java.util.Locale;

public class SearchOverViewPolicies extends AppCompatActivity {

    Button btnSearch;
    Button btnClear;
    EditText Insurance_Number;
    EditText Other_Names;
    EditText Last_Name;
    Spinner Insurance_Product;
    TextView Uploaded_From;
    TextView Uploaded_To;

    RadioButton Renewal_Yes;
    RadioButton Renewal_No;
    RadioGroup Radio_Renewal;

    RadioButton Requested_Yes;
    RadioButton Requested_No;
    RadioGroup Radio_Requested;

    Calendar uploadedFromCalendar;
    Calendar uploadedToCalendar;
    private ArrayList<HashMap<String, String>> ProductList = new ArrayList<>();

    ClientAndroidInterface clientAndroidInterface;

    ListView lv1;
    private RadioButton radioButtonRenewal;
    private RadioButton radioButtonRequested;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_over_view_policies);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.SearchPolicies));
        }

        clientAndroidInterface = new ClientAndroidInterface(this);
        lv1 = findViewById(R.id.lv1);

        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);

        Uploaded_From = findViewById(R.id.uploaded_from);
        Uploaded_To = findViewById(R.id.uploaded_to);
        Insurance_Number = findViewById(R.id.insurance_number);
        Insurance_Product = findViewById(R.id.insurance_product);
        Other_Names = findViewById(R.id.other_names);
        Last_Name = findViewById(R.id.last_name);
        Renewal_Yes = findViewById(R.id.renewal_yes);
        Renewal_No = findViewById(R.id.renewal_no);
        Radio_Renewal = findViewById(R.id.radio_renewal);
        Requested_Yes = findViewById(R.id.requested_yes);
        Requested_No = findViewById(R.id.requested_no);
        Radio_Requested = findViewById(R.id.radio_requested);

        btnSearch.setOnClickListener(view -> {
            String InsuranceNumber = Insurance_Number.getText().toString();
            String OtherNames = Other_Names.getText().toString();
            String LastName = Last_Name.getText().toString();
            String InsuranceProduct = GetSelectedProduct();
            String UploadedFrom = Uploaded_From.getText().toString();
            String UploadedTo = Uploaded_To.getText().toString();
            String RadioRenewal = "";
            String RadioRequested = "";

            if (Renewal_Yes.isChecked() || Renewal_No.isChecked()) {
                int selectedId = Radio_Renewal.getCheckedRadioButtonId();
                radioButtonRenewal = findViewById(selectedId);
                RadioRenewal = radioButtonRenewal.getText().toString();
            }

            if (Requested_Yes.isChecked() || Requested_No.isChecked()) {
                int selectedId = Radio_Requested.getCheckedRadioButtonId();
                radioButtonRequested = findViewById(selectedId);
                RadioRequested = radioButtonRequested.getText().toString();
            }

            Intent intent = new Intent(SearchOverViewPolicies.this, OverViewPolicies.class);
            intent.putExtra("RENEWAL", RadioRenewal);
            intent.putExtra("INSURANCE_NUMBER", InsuranceNumber);
            intent.putExtra("OTHER_NAMES", OtherNames);
            intent.putExtra("LAST_NAME", LastName);
            intent.putExtra("INSURANCE_PRODUCT", InsuranceProduct);
            intent.putExtra("UPLOADED_FROM", UploadedFrom);
            intent.putExtra("UPLOADED_TO", UploadedTo);
            intent.putExtra("REQUESTED_YES_NO", RadioRequested);
            startActivity(intent);

        });

        btnClear.setOnClickListener(view -> {
            Uploaded_From.setText("");
            Uploaded_To.setText("");
            Insurance_Number.setText("");
            Insurance_Product.setSelection(0);
            Other_Names.setText("");
            Last_Name.setText("");
            Radio_Renewal.clearCheck();
            Radio_Requested.clearCheck();
            lv1.setVisibility(View.GONE);

            Calendar today = Calendar.getInstance();

            uploadedFromCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            uploadedToCalendar.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        });

        uploadedFromCalendar = Calendar.getInstance();
        uploadedToCalendar = Calendar.getInstance();

        Uploaded_From.setOnClickListener(
                v -> new DatePickerDialog(
                        SearchOverViewPolicies.this,
                        (view, year, monthOfYear, dayOfMonth) -> {
                            uploadedFromCalendar.set(Calendar.YEAR, year);
                            uploadedFromCalendar.set(Calendar.MONTH, monthOfYear);
                            uploadedFromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateLabel(uploadedFromCalendar, Uploaded_From);
                        },
                        uploadedFromCalendar.get(Calendar.YEAR),
                        uploadedFromCalendar.get(Calendar.MONTH),
                        uploadedFromCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
        );

        Uploaded_To.setOnClickListener(
                v -> new DatePickerDialog(
                        SearchOverViewPolicies.this,
                        (view, year, monthOfYear, dayOfMonth) -> {
                            uploadedToCalendar.set(Calendar.YEAR, year);
                            uploadedToCalendar.set(Calendar.MONTH, monthOfYear);
                            uploadedToCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateLabel(uploadedToCalendar, Uploaded_To);
                        },
                        uploadedToCalendar.get(Calendar.YEAR),
                        uploadedToCalendar.get(Calendar.MONTH),
                        uploadedToCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
        );

        BindSpinnerProduct();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void updateLabel(Calendar calendar, TextView view) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        view.setText(formatter.format(calendar.getTime()));
    }

    private void BindSpinnerProduct() {
        clientAndroidInterface = new ClientAndroidInterface(this);

        String result = clientAndroidInterface.getProductsRD();

        JSONArray jsonArray;
        JSONObject object;

        try {
            jsonArray = new JSONArray(result);

            ProductList.clear();

            if (jsonArray.length() == 0) {
                HashMap<String, String> Product = new HashMap<>();
                Product.put("ProductCode", "");
                Product.put("ProductName", getResources().getString(R.string.SelectProduct));
                ProductList.add(Product);

                SimpleAdapter adapter = new SimpleAdapter(SearchOverViewPolicies.this, ProductList, R.layout.spinnerproducts,
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

                    SimpleAdapter adapter = new SimpleAdapter(SearchOverViewPolicies.this, ProductList, R.layout.spinnerproducts,
                            new String[]{"ProductCode", "ProductName"},
                            new int[]{R.id.tvProductCode, R.id.tvProductName});

                    Insurance_Product.setAdapter(adapter);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
