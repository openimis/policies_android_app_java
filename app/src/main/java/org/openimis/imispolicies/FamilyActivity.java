package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.util.JsonDropdownHelper;

public class FamilyActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CLOSE = 14;

    private MaterialAutoCompleteTextView spRegion, spDistrict, spWard, spVillage, spPovertyStatus, spFamilyType,
            spConfirmationType, spApprovalSMS, spLanguageSMS;
    private TextInputLayout layoutRegion, layoutDistrict, layoutWard, layoutVillage;
    Button btnNext;

    int familyId = 0;
    JSONObject familyObj;
    ClientAndroidInterface ca;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CLOSE && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);
        setTitle(getApplicationContext().getString(R.string.AddNewFamily));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        ca = new ClientAndroidInterface(this);
        familyObj = new JSONObject();

        spRegion = findViewById(R.id.spRegion);
        spDistrict = findViewById(R.id.spDistrict);
        spWard = findViewById(R.id.spWard);
        spVillage = findViewById(R.id.spVillage);
        spPovertyStatus = findViewById(R.id.spPovertyStatus);
        spFamilyType = findViewById(R.id.spFamilyType);
        spConfirmationType = findViewById(R.id.spConfirmationType);
        spApprovalSMS = findViewById(R.id.spApprovalSMS);
        spLanguageSMS = findViewById(R.id.spLanguageSMS);
        btnNext = findViewById(R.id.btnNext);
        layoutRegion = findViewById(R.id.layoutRegion);
        layoutDistrict = findViewById(R.id.layoutDistrict);
        layoutWard = findViewById(R.id.layoutWard);
        layoutVillage = findViewById(R.id.layoutVillage);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("familyId")){
            familyId = intent.getIntExtra("familyId", 0);
        }

        try {
            defineRequiredField();
            fillRegions();
            getPovertyStatus();
            getConfirmationTypes();
            getFamilyTypes();
            getApprovalOfSMS();
            getLanguageOfSMS();
            setupListeners();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        canSave();
    }

    private void defineRequiredField(){
        layoutRegion.setError(" ");
        layoutDistrict.setError(" ");
        layoutWard.setError(" ");
        layoutVillage.setError(" ");
    }

    private void fillRegions() throws JSONException {
        String regions = ca.getRegions();
        JSONArray regionsArray = new JSONArray(regions);
        JsonDropdownHelper.bindDropdown(
                this,
                spRegion,
                regionsArray,
                "LocationName",
                null,
                new JsonDropdownHelper.OnJsonItemSelectedListener() {
                    @Override
                    public void onItemSelected(JSONObject selectedItem, int position) {
                        try {
                            String regionId = selectedItem.getString("LocationId");
                            fillDistricts(regionId);
                            spDistrict.setText("");
                            spWard.setText("");
                            spVillage.setText("");
                            spWard.setAdapter(null);
                            spVillage.setAdapter(null);
                            canSave();
                            familyObj.put("ddlRegion", regionId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private void fillDistricts(String regionId) throws JSONException {
        String districts = ca.getDistricts(Integer.parseInt(regionId));
        JSONArray districtArray = new JSONArray(districts);
        JsonDropdownHelper.bindDropdown(
                this,
                spDistrict,
                districtArray,
                "LocationName",
                null,
                new JsonDropdownHelper.OnJsonItemSelectedListener() {
                    @Override
                    public void onItemSelected(JSONObject selectedItem, int position) {
                        try {
                            String districtId = selectedItem.getString("LocationId");
                            fillWards(Integer.parseInt(districtId));
                            spWard.setText("");
                            spVillage.setText("");
                            spVillage.setAdapter(null);
                            canSave();
                            familyObj.put("ddlDistrict", districtId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private void fillWards(int districtId) throws JSONException {
        String wards = ca.getWards(districtId);
        JSONArray wardsArray = new JSONArray(wards);
        JsonDropdownHelper.bindDropdown(
                this,
                spWard,
                wardsArray,
                "LocationName",
                null,
                new JsonDropdownHelper.OnJsonItemSelectedListener() {
                    @Override
                    public void onItemSelected(JSONObject selectedItem, int position) {
                        try {
                            String wardId = selectedItem.getString("LocationId");
                            spVillage.setText("");
                            fillVillages(Integer.parseInt(wardId));
                            canSave();
                            familyObj.put("ddlWard", wardId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private void fillVillages(int wardId) throws JSONException {
        String villages = ca.getVillages(wardId);
        JSONArray villagesArray = new JSONArray(villages);
        JsonDropdownHelper.bindDropdown(
                this,
                spVillage,
                villagesArray,
                "LocationName",
                null,
                new JsonDropdownHelper.OnJsonItemSelectedListener() {
                    @Override
                    public void onItemSelected(JSONObject selectedItem, int position) {
                        try {
                            familyObj.put("ddlVillage", selectedItem.getString("LocationId"));
                            canSave();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    private void saveFamily() {
        try {
            familyObj.put("hfFamilyId", String.valueOf(familyId));
            Intent intent = new Intent(this, InsureeActivity.class);
            intent.putExtra("FamilyData", familyObj.toString());
            intent.putExtra("FamilyId", familyId);
            startActivityForResult(intent, REQUEST_CODE_CLOSE);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void canSave() {
        if(spVillage.getText().toString().isEmpty()){
            btnNext.setEnabled(false);
        } else {
            btnNext.setEnabled(true);
        }
    }

    private void getPovertyStatus() throws JSONException {
        String yesNoString = ca.getYesNo();
        JSONArray yesNoArray = new JSONArray(yesNoString);
        JsonDropdownHelper.bindDropdown(
                this,
                spPovertyStatus,
                yesNoArray,
                "key",
                null,
                new JsonDropdownHelper.OnJsonItemSelectedListener() {
                    @Override
                    public void onItemSelected(JSONObject selectedItem, int position) {
                        try {
                            familyObj.put("ddlPovertyStatus", selectedItem.getString("key"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    private void getConfirmationTypes() throws JSONException {
        String textLanguage = "ConfirmationType";
        String confirmationString = ca.getConfirmationTypes();
        JSONArray confirmationArray = new JSONArray(confirmationString);
        JsonDropdownHelper.bindDropdown(
                this,
                spConfirmationType,
                confirmationArray,
                textLanguage,
                null,
                new JsonDropdownHelper.OnJsonItemSelectedListener() {
                    @Override
                    public void onItemSelected(JSONObject selectedItem, int position) {
                        try{
                            familyObj.put("ddlConfirmationType", selectedItem.getString("ConfirmationTypeCode"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    private void getFamilyTypes() throws JSONException {
        String textLanguage = "FamilyType";
        String familyTypesString = ca.getGroupTypes();
        JSONArray familyTypesArray = new JSONArray(familyTypesString);
        JsonDropdownHelper.bindDropdown(
                this,
                spFamilyType,
                familyTypesArray,
                textLanguage,
                null,
                new JsonDropdownHelper.OnJsonItemSelectedListener() {
                    @Override
                    public void onItemSelected(JSONObject selectedItem, int position) {
                        try {
                            familyObj.put("ddlGroupType", selectedItem.getString("FamilyTypeCode"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    private void getApprovalOfSMS() throws JSONException {
        String approvalString = ca.getApprovalOfSMS();
        JSONArray approvalArray = new JSONArray(approvalString);
        JsonDropdownHelper.bindDropdown(
                this,
                spApprovalSMS,
                approvalArray,
                "key",
                null,
                new JsonDropdownHelper.OnJsonItemSelectedListener() {
                    @Override
                    public void onItemSelected(JSONObject selectedItem, int position) {
                        try {
                            familyObj.put("ddlApprovalOfSMS", selectedItem.getString("value"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    private void getLanguageOfSMS() throws JSONException {
        String textLanguage = "LanguageName";
        String languagesString = ca.getLanguagesOfSMS();
        JSONArray languagesArray = new JSONArray(languagesString);
        JsonDropdownHelper.bindDropdown(
                this,
                spLanguageSMS,
                languagesArray,
                textLanguage,
                null,
                new JsonDropdownHelper.OnJsonItemSelectedListener() {
                    @Override
                    public void onItemSelected(JSONObject selectedItem, int position) {
                        try {
                            familyObj.put("ddlLanguageOfSMS", selectedItem.getString("LanguageCode"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupListeners(){

        btnNext.setOnClickListener(v -> saveFamily());

    }

}