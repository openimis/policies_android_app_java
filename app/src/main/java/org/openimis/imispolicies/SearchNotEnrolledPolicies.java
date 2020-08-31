package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchNotEnrolledPolicies extends AppCompatActivity {

    EditText insuranceNumber;
    Spinner insuranceProduct;
    ListView productsListView;
    RadioButton renewalYesButton;
    RadioButton renewalNoButton;
    RadioButton checkedRadioButton;
    RadioGroup renewalRadio;
    Button notEnrolledPoliciesSearchBtn;
    private ClientAndroidInterface clientAndroidInterface;
    private ArrayList<HashMap<String, String>> productsList = new ArrayList<>();
    private String radioRenewal;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_for_not_enrolled_policies);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.request_enrolled_policies));

        insuranceNumber = (EditText) findViewById(R.id.ins_num);
        insuranceProduct = (Spinner) findViewById(R.id.ins_product);
        renewalYesButton = (RadioButton) findViewById(R.id.renewal_yes_button);
        renewalNoButton = (RadioButton) findViewById(R.id.renewal_no_button);
        renewalRadio = (RadioGroup) findViewById(R.id.renewal_radio);
        productsListView = (ListView) findViewById(R.id.ins_prod_list);
        notEnrolledPoliciesSearchBtn = (Button) findViewById(R.id.notEnrolledPoliciesSearchBtn);

        bindSpinnerProduct();

        notEnrolledPoliciesSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchNotEnrolledPolicies.this, NotEnrolledPoliciesOverview.class);
                intent.putExtra("INSURANCE_NUMBER", insuranceNumber.getText().toString());
                intent.putExtra("INSURANCE_PRODUCT", getSelectedProduct());
                intent.putExtra("RENEWAL", checkRenewalChoice());
                startActivity(intent);
            }
        });
    }

    private void bindSpinnerProduct() {
        clientAndroidInterface = new ClientAndroidInterface(this);

        String result = clientAndroidInterface.getProductsRD();

        JSONArray jsonArray = null;
        JSONObject object;

        try {
            jsonArray = new JSONArray(result);

            productsList.clear();

            if (jsonArray.length() == 0) {
                HashMap<String, String> product = new HashMap<>();
                product.put("ProductCode", "");
                product.put("ProductName", getResources().getString(R.string.SelectProduct));
                productsList.add(product);

                SimpleAdapter adapter = new SimpleAdapter(SearchNotEnrolledPolicies.this, productsList, R.layout.spinnerproducts,
                        new String[]{"ProductCode", "ProductName"},
                        new int[]{R.id.tvProductCode, R.id.tvProductName});

                insuranceProduct.setAdapter(adapter);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getJSONObject(i);

                    // Enter an empty record
                    if (i == 0) {
                        HashMap<String, String> Product = new HashMap<>();
                        Product.put("ProductCode", "");
                        Product.put("ProductName", getResources().getString(R.string.SelectProduct));
                        productsList.add(Product);
                    }

                    HashMap<String, String> Product = new HashMap<>();
                    Product.put("ProductCode", object.getString("ProductCode"));
                    Product.put("ProductName", object.getString("ProductName"));

                    productsList.add(Product);

                    SimpleAdapter adapter = new SimpleAdapter(SearchNotEnrolledPolicies.this, productsList, R.layout.spinnerproducts,
                            new String[]{"ProductCode", "ProductName"},
                            new int[]{R.id.tvProductCode, R.id.tvProductName});

                    insuranceProduct.setAdapter(adapter);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private String getSelectedProduct() {
        String Product = "";
        try {
            HashMap<String, String> P = new HashMap<>();
            //noinspection unchecked
            P = (HashMap<String, String>) insuranceProduct.getSelectedItem();
            if (P.get("ProductCode").toString().equals("0") || P.get("ProductCode") == null || P.get("ProductCode").toString().equals("")) {
                Product = "";
            } else {
                Product = P.get("ProductCode");
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }

        return Product;
    }

    private String checkRenewalChoice() {
        if (renewalYesButton.isChecked() || renewalNoButton.isChecked()) {
            int selectedId = renewalRadio.getCheckedRadioButtonId();
            checkedRadioButton = (RadioButton) findViewById(selectedId);
            radioRenewal = checkedRadioButton.getText().toString();
        } else {
            radioRenewal = "";
        }
        return radioRenewal;
    }
}
