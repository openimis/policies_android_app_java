package tz.co.exact.imis;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class SearchOverViewControlNumber extends AppCompatActivity {

    Button btnSearch;
    Button from_date;
    Button to_date;
    Button requested_from;
    Button requested_to;

    EditText search1;
    EditText search2;
    RadioButton radio_yes;
    RadioButton radio_no;
    RadioGroup radio_group1;

    Calendar myCalendar;
    Calendar myCalendar1;
    Calendar myCalendarReqFrom;
    Calendar myCalendarReqTo;

    ClientAndroidInterface clientAndroidInterface;

    ListView lv1;
    ListAdapter adapter;
    ArrayList<HashMap<String, String>> FeedbackList = new ArrayList<HashMap<String, String>>();
    private RadioButton radioButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_over_view_control_number);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clientAndroidInterface = new ClientAndroidInterface(this);
        lv1 = (ListView) findViewById(R.id.lv1);
        fillProducts();

        btnSearch = (Button) findViewById(R.id.btnSearch);
        from_date = (Button) findViewById(R.id.from_date);
        to_date = (Button) findViewById(R.id.To_date);
        requested_from = (Button) findViewById(R.id.requested_from);
        requested_to = (Button) findViewById(R.id.requested_to);

        search1 = (EditText) findViewById(R.id.search1);
        search2 = (EditText) findViewById(R.id.search2);

        radio_yes = (RadioButton) findViewById(R.id.radio_yes);
        radio_no = (RadioButton) findViewById(R.id.radio_no);
        radio_group1 = (RadioGroup) findViewById(R.id.radio_group1);


        search1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                search2.setText("");
                from_date.setText("Uploaded From");
                to_date.setText("Uploaded To");
                radio_group1.clearCheck();
            }
        });
        search2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                search1.setText("");
                from_date.setText("Uploaded From");
                to_date.setText("Uploaded To");
                radio_group1.clearCheck();
            }
        });
        search2.addTextChangedListener(new TextWatcher() {
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
        });

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                search1.setText("");
                from_date.setText("Uploaded From");
                to_date.setText("Uploaded To");

                HashMap<String, String> oItem;
                //noinspection unchecked
                oItem = (HashMap<String, String>) parent.getItemAtPosition(position);
                search2.setText(oItem.get("ProductName").toString());


            }
        });

        radio_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search1.setText("");
                search2.setText("");
                from_date.setText("Uploaded From");
                to_date.setText("Uploaded To");
            }
        });

        radio_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search1.setText("");
                search2.setText("");
                from_date.setText("Uploaded From");
                to_date.setText("Uploaded To");
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search_1 = search1.getText().toString();
                String search_2 = search2.getText().toString();
                String search_3 = "";
                if(radio_no.isChecked() || radio_yes.isChecked()){
                    int selectedId = radio_group1.getCheckedRadioButtonId();
                    radioButton = (RadioButton) findViewById(selectedId);
                    search_3 = radioButton.getText().toString();
                }
                String search = search_1+search_2+search_3;

                String fromdate = from_date.getText().toString();
                String todate = to_date.getText().toString();

                String reqfrom = requested_from.getText().toString();
                String reqto = requested_to.getText().toString();

                if(!fromdate.equals("Uploaded From") || !todate.equals("Uploaded To")){
                    Intent intent = new Intent(SearchOverViewControlNumber.this, OverViewControlNumbers.class);
                    intent.putExtra("FROMDATE", fromdate);
                    intent.putExtra("TODATE", todate);
                    startActivity(intent);
                }else if(!search.equals("")){
                    Intent intent = new Intent(SearchOverViewControlNumber.this, OverViewControlNumbers.class);
                    intent.putExtra("SEARCH_STRING", search);
                    startActivity(intent);
                }else if(!reqfrom.equals("Requested From") || !reqto.equals("Requested To")){
                    Intent intent = new Intent(SearchOverViewControlNumber.this, OverViewControlNumbers.class);
                    intent.putExtra("REQFROM", reqfrom);
                    intent.putExtra("REQTO", reqto);
                    startActivity(intent);
                }else{

                }

            }
        });

        myCalendar = Calendar.getInstance();
        myCalendar1 = Calendar.getInstance();
        myCalendarReqFrom = Calendar.getInstance();
        myCalendarReqTo = Calendar.getInstance();

        from_date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                search1.setText("");
                search2.setText("");
                // TODO Auto-generated method stub
                new DatePickerDialog(SearchOverViewControlNumber.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        to_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search1.setText("");
                search2.setText("");
                // TODO Auto-generated method stub
                new DatePickerDialog(SearchOverViewControlNumber.this, date1, myCalendar1
                        .get(Calendar.YEAR), myCalendar1.get(Calendar.MONTH),
                        myCalendar1.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        requested_from.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                search1.setText("");
                search2.setText("");
                // TODO Auto-generated method stub
                new DatePickerDialog(SearchOverViewControlNumber.this, dateFrom, myCalendarReqFrom
                        .get(Calendar.YEAR), myCalendarReqFrom.get(Calendar.MONTH),
                        myCalendarReqFrom.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        requested_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search1.setText("");
                search2.setText("");
                // TODO Auto-generated method stub
                new DatePickerDialog(SearchOverViewControlNumber.this, dateTo, myCalendarReqTo
                        .get(Calendar.YEAR), myCalendarReqTo.get(Calendar.MONTH),
                        myCalendarReqTo.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


    }

    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void fillProducts(){
        JSONArray jsonArray = null;
        JSONObject object;
        String result = clientAndroidInterface.getProducts();

        try {
            jsonArray = new JSONArray(result);
            if(jsonArray.length()==0){
                FeedbackList.clear();
                lv1.setAdapter(null);
                Toast.makeText(this,getResources().getString(R.string.NoFeedbackFound),Toast.LENGTH_LONG).show();
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

            requested_from.setText("");
            requested_to.setText("");

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

            requested_from.setText("");
            requested_to.setText("");

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

            from_date.setText("");
            to_date.setText("");

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

            from_date.setText("");
            to_date.setText("");

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

        from_date.setText(String.valueOf(sdf.format(myCalendar.getTime())));
    }
    private void updateLabel1() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        to_date.setText(String.valueOf(sdf.format(myCalendar1.getTime())));
    }

    private void updateLabelReqFrom() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        requested_from.setText(String.valueOf(sdf.format(myCalendarReqFrom.getTime())));
    }

    private void updateLabelReqTo() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        requested_to.setText(String.valueOf(sdf.format(myCalendarReqTo.getTime())));
    }
}

