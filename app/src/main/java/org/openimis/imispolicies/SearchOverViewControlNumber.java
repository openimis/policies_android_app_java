package org.openimis.imispolicies;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.openimis.imispolicies.R;

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

    Calendar myCalendar;
    Calendar myCalendar1;
    Calendar myCalendarReqFrom;
    Calendar myCalendarReqTo;

    Spinner PaymentSpinner;

    ClientAndroidInterface clientAndroidInterface;

    AlertDialog alertDialog;

    ListView lv1;
    ListAdapter adapter;
    ArrayList<HashMap<String, String>> FeedbackList = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> ProductList = new ArrayList<>();
    private RadioButton radioButtonRenewal;
    private RadioButton radioButtonSms;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_over_view_control_number);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.SearchControlNumbers));

        clientAndroidInterface = new ClientAndroidInterface(this);
        lv1 = (ListView) findViewById(R.id.lv1);
        //fillProducts();

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnClear = (Button) findViewById(R.id.btnClear);

        Uploaded_From = (TextView) findViewById(R.id.uploaded_from);
        Uploaded_To = (TextView) findViewById(R.id.uploaded_to);
        Requested_From = (TextView) findViewById(R.id.requested_from);
        Requested_To = (TextView) findViewById(R.id.requested_to);
        Insurance_Number = (EditText) findViewById(R.id.insurance_number);
        Insurance_Product = (Spinner) findViewById(R.id.insurance_product);
        Other_Names = (EditText) findViewById(R.id.other_names);
        Last_Name = (EditText) findViewById(R.id.last_name);
        Renewal_Yes = (RadioButton) findViewById(R.id.renewal_yes);
        Renewal_No = (RadioButton) findViewById(R.id.renewal_yes);
        Sms_Yes = (RadioButton) findViewById(R.id.sms_required_yes);
        Sms_No = (RadioButton) findViewById(R.id.sms_required_no);
        Radio_Renewal = (RadioGroup) findViewById(R.id.radio_renewal);
        Radio_Sms = (RadioGroup) findViewById(R.id.radio_sms);
        Requested_Yes = (RadioButton) findViewById(R.id.requested_yes);
        Requested_No = (RadioButton) findViewById(R.id.requested_no);
        Radio_Requested = (RadioGroup) findViewById(R.id.radio_requested);

        PaymentSpinner = (Spinner) findViewById(R.id.payment_type);

        addItemsOnSpinner2();
        addListenerOnSpinnerItemSelection();
        BindSpinnerProduct();



/*        Insurance_Product.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lv1.setVisibility(View.VISIBLE);
                ((SimpleAdapter) adapter).getFilter().filter(s);
                //  HashMap<String, String> Payer = new HashMap<>();
                // String searchString = etOfficer.getText().toString();
                // int LocID = ca.getLocationId(searchString);
                //BindSpinnerPayersXXXX(LocID);
                // BindSpinnerProduct(LocID);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/


/*        Insurance_Product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_search_engine();
            }
        });*/


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                if(Renewal_Yes.isChecked() || Renewal_No.isChecked()){
                    int selectedId = Radio_Renewal.getCheckedRadioButtonId();
                    radioButtonRenewal = (RadioButton) findViewById(selectedId);
                    RadioRenewal = radioButtonRenewal.getText().toString();
                }

                if(Sms_Yes.isChecked() || Sms_No.isChecked()){
                    int selectedId = Radio_Sms.getCheckedRadioButtonId();
                    radioButtonSms = (RadioButton) findViewById(selectedId);
                    if(radioButtonSms.getText().toString().equals("Y")){
                        RadioSms = "1";
                    }else if(radioButtonSms.getText().toString().equals("N")){
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

            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        myCalendar = Calendar.getInstance();
        myCalendar1 = Calendar.getInstance();
        myCalendarReqFrom = Calendar.getInstance();
        myCalendarReqTo = Calendar.getInstance();

        Uploaded_From.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(SearchOverViewControlNumber.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        Uploaded_To.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(SearchOverViewControlNumber.this, date1, myCalendar1
                        .get(Calendar.YEAR), myCalendar1.get(Calendar.MONTH),
                        myCalendar1.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        Requested_From.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(SearchOverViewControlNumber.this, dateFrom, myCalendarReqFrom
                        .get(Calendar.YEAR), myCalendarReqFrom.get(Calendar.MONTH),
                        myCalendarReqFrom.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        Requested_To.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(SearchOverViewControlNumber.this, dateTo, myCalendarReqTo
                        .get(Calendar.YEAR), myCalendarReqTo.get(Calendar.MONTH),
                        myCalendarReqTo.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


    }

    // add items into spinner dynamically
    public void addItemsOnSpinner2() {

        List<String> list = new ArrayList<String>();
        list.add("");
        list.add("Bank Transfer");
        list.add("Mobile Phone");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        PaymentSpinner.setAdapter(dataAdapter);
    }




    public void addListenerOnSpinnerItemSelection() {
        PaymentSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    public boolean onOptionsItemSelected(MenuItem item){
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

            if(jsonArray.length() == 0){
                HashMap<String, String> Product = new HashMap<>();
                Product.put("ProductCode", "");
                Product.put("ProductName", getResources().getString(R.string.SelectProduct));
                ProductList.add(Product);

                SimpleAdapter adapter = new SimpleAdapter(SearchOverViewControlNumber.this, ProductList, R.layout.spinnerproducts,
                        new String[]{"ProductCode", "ProductName"},
                        new int[]{R.id.tvProductCode, R.id.tvProductName});

                Insurance_Product.setAdapter(adapter);
            }else{
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

    public void fillProducts(ListView lv1){
        JSONArray jsonArray = null;
        JSONObject object;
        String result = clientAndroidInterface.getProducts();

        try {
            jsonArray = new JSONArray(result);
            if(jsonArray.length()==0){
                FeedbackList.clear();
                lv1.setAdapter(null);
                Toast.makeText(this,getResources().getString(R.string.NoFeedbackFound), Toast.LENGTH_LONG).show();
                return;
            }else{
                FeedbackList.clear();
                lv1.setAdapter(null);

                for(int i= 0;i < jsonArray.length();i++){
                    object = jsonArray.getJSONObject(i);

                    HashMap<String, String> feedback = new HashMap<String, String>();
                    feedback.put("ProductName", object.getString("ProductName"));
                    FeedbackList.add(feedback);
                }

                adapter = new SimpleAdapter(SearchOverViewControlNumber.this, FeedbackList, R.layout.txtviewproduct,
                        new String[]{"ProductName"},
                        new int[]{R.id.tv});



                lv1.setAdapter(adapter);

                //setTitle("Products (" + String.valueOf(lv1.getCount()) + ")");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    DatePickerDialog.OnDateSetListener date1 = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar1.set(Calendar.YEAR, year);
            myCalendar1.set(Calendar.MONTH, monthOfYear);
            myCalendar1.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel1();
        }

    };


    DatePickerDialog.OnDateSetListener dateFrom = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendarReqFrom.set(Calendar.YEAR, year);
            myCalendarReqFrom.set(Calendar.MONTH, monthOfYear);
            myCalendarReqFrom.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabelReqFrom();
        }

    };

    DatePickerDialog.OnDateSetListener dateTo = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendarReqTo.set(Calendar.YEAR, year);
            myCalendarReqTo.set(Calendar.MONTH, monthOfYear);
            myCalendarReqTo.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabelReqTo();
        }

    };

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Uploaded_From.setText(String.valueOf(sdf.format(myCalendar.getTime())));
    }
    private void updateLabel1() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Uploaded_To.setText(String.valueOf(sdf.format(myCalendar1.getTime())));
    }

    private void updateLabelReqFrom() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Requested_From.setText(String.valueOf(sdf.format(myCalendarReqFrom.getTime())));
    }

    private void updateLabelReqTo() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Requested_To.setText(String.valueOf(sdf.format(myCalendarReqTo.getTime())));
    }

    public void new_search_engine(){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.new_search_engine, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                //this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                this,R.style.yourDialog);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText type1 = (EditText) promptsView.findViewById(R.id.type1);
        final ListView lv1 = (ListView) promptsView.findViewById(R.id.lv1);

        fillProducts(lv1);

        type1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lv1.setVisibility(View.VISIBLE);
                if(type1.getText().toString().equals("")){
                    lv1.setVisibility(View.GONE);
                }
                ((SimpleAdapter) adapter).getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> oItem;
                //noinspection unchecked
                oItem = (HashMap<String, String>) parent.getItemAtPosition(position);
                Spinner Insurance_Product = (Spinner) findViewById(R.id.insurance_product);
                type1.setText(oItem.get("ProductName").toString());
                //Insurance_Product.setText(oItem.get("ProductName").toString());
                lv1.setVisibility(View.GONE);
                alertDialog.dismiss();

                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            }
        });



        // set dialog message
        alertDialogBuilder
                .setCancelable(true);

        // create alert dialog
        alertDialog = alertDialogBuilder.create();



        // show it
        alertDialog.show();
    }

    private String GetSelectedProduct() {
        String Product = "";
        try{
            HashMap<String, String> P = new HashMap<>();
            //noinspection unchecked
            P = (HashMap<String, String>) Insurance_Product.getSelectedItem();
            if(P.get("ProductCode").toString().equals("0") || P.get("ProductCode") == null || P.get("ProductCode").toString().equals("")) {
                Product = "";
            }else{
                Product = P.get("ProductCode");
            }

        }catch (Exception e){
            // e.printStackTrace();
        }

        return Product;
    }
}

