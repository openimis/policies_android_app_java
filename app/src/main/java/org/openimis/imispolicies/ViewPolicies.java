package org.openimis.imispolicies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import org.openimis.imispolicies.R;

public class ViewPolicies extends AppCompatActivity {

    ListView promptPolicy;
    ListAdapter adapter;
    ArrayList<HashMap<String, String>> PolicyList = new ArrayList<HashMap<String, String>>();

    ClientAndroidInterface clientAndroidInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_policies);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clientAndroidInterface = new ClientAndroidInterface(this);
        promptPolicy = (ListView) findViewById(R.id.promptPolicy);

        JSONArray jsonArray = null;
        JSONObject object;

        String InternalIdentifier = getIntent().getStringExtra("IDENTIFIER");
        jsonArray = clientAndroidInterface.getRecordedPoliciesWithIdentifier(InternalIdentifier);

        try {
            if (jsonArray.length() == 0) {
                PolicyList.clear();
                promptPolicy.setAdapter(null);
                //Toast.makeText(this, getResources().getString(R.string.NoFeedbackFound), Toast.LENGTH_LONG).show();
                return;
            } else {
                PolicyList.clear();
                promptPolicy.setAdapter(null);

                for (int i = 0; i < jsonArray.length(); i++) {
                    object = jsonArray.getJSONObject(i);

                    HashMap<String, String> feedback = new HashMap<String, String>();
                    feedback.put("renewal", object.getString("isDone"));
                    feedback.put("insurancenumber", object.getString("InsuranceNumber"));
                    feedback.put("fullname", object.getString("LastName") + " " + object.getString("OtherNames"));
                    feedback.put("product", object.getString("ProductCode") + "-" + object.getString("ProductName"));
                    feedback.put("uploaded", object.getString("UploadedDate"));
                    PolicyList.add(feedback);
                }

                adapter = new SimpleAdapter(ViewPolicies.this, PolicyList, R.layout.listofpolicies,
                        new String[]{"renewal", "insurancenumber", "fullname", "product", "uploaded"},
                        new int[]{R.id.renewal,R.id.insurancenumber,R.id.fullname,R.id.product,R.id.uploaded});


                promptPolicy.setAdapter(adapter);


            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
