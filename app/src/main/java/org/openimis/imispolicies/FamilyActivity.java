package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FamilyActivity extends AppCompatActivity {

    Spinner spRegion, spDistrict, spWard, spVillage, spPovertyStatus, spFamilyType,
            spConfirmationType, spApprovalSMS, spLanguageSMS;
    Button btnNext;

    int familyId = 0;
    JSONObject familyObj;
    ClientAndroidInterface ca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);
        setTitle(getApplicationContext().getString(R.string.AddNewFamily));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("familyId")){
            familyId = intent.getIntExtra("familyId", 0);
        }

        try {
            loadRegions();
            getPovertyStatus();
            getConfirmationTypes();
            getFamilyTypes();
            getApprovalOfSMS();
            getLanguageOfSMS();
            setupListeners();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        canSaveFamily();
    }

    private void loadRegions() throws JSONException {
        String regions = ca.getRegions();
        JSONArray regionsArray = new JSONArray(regions);
        JSONObject emptyItem = new JSONObject();
        emptyItem.put("LocationId", "");
        emptyItem.put("LocationName", getApplicationContext().getResources().getString(R.string.SelectRegion));
        JSONArray finalRegions = addFirst(regionsArray, emptyItem);
        JSONSpinnerAdapter adapter =
                new JSONSpinnerAdapter(this, finalRegions, "LocationName");
        spRegion.setAdapter(adapter);
    }

    private void loadDistricts(int regionId) throws JSONException {
        String districts = ca.getDistricts(regionId);
        JSONArray districtArray = new JSONArray(districts);
        JSONObject emptyItem = new JSONObject();
        emptyItem.put("LocationId", "");
        emptyItem.put("LocationName", getApplicationContext().getResources().getString(R.string.SelectDistrict));
        JSONArray finalDistricts = addFirst(districtArray, emptyItem);
        JSONSpinnerAdapter adapter = new JSONSpinnerAdapter(this, finalDistricts, "LocationName");
        spDistrict.setAdapter(adapter);
    }

    private void loadWards(int districtId) throws JSONException {
        String wards = ca.getWards(districtId);
        JSONArray wardsArray = new JSONArray(wards);
        JSONObject emptyObject = new JSONObject();
        emptyObject.put("LocationId", "");
        emptyObject.put("LocationName", getApplicationContext().getResources().getString(R.string.SelectWard));
        JSONArray finalWards = addFirst(wardsArray, emptyObject);
        JSONSpinnerAdapter adapter = new JSONSpinnerAdapter(this, finalWards, "LocationName");
        spWard.setAdapter(adapter);
    }

    private void loadVillages(int wardId) throws JSONException {
        String villages = ca.getVillages(wardId);
        JSONArray villagesArray = new JSONArray(villages);
        JSONObject emptyObject = new JSONObject();
        emptyObject.put("LocationId", "");
        emptyObject.put("LocationName", getApplicationContext().getResources().getString(R.string.SelectVillage));
        JSONArray finalVillages = addFirst(villagesArray, emptyObject);
        JSONSpinnerAdapter adapter = new JSONSpinnerAdapter(this, finalVillages, "LocationName");
        spVillage.setAdapter(adapter);
    }

    private void saveFamily() {
        try {
            familyObj.put("FamilyId", familyId);
            Intent intent = new Intent(this, InsureeActivity.class);
            intent.putExtra("FamilyData", familyObj.toString());
            startActivity(intent);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void canSaveFamily() {

        try {
            JSONObject selectedVillage =
                    (JSONObject) spVillage.getSelectedItem();
            if(spVillage.getSelectedItem() == null || selectedVillage.getString("LocationId").isEmpty()){
                btnNext.setEnabled(false);
            } else {
                btnNext.setEnabled(true);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void getPovertyStatus() throws JSONException {
        String yesNoString = ca.getYesNo();
        JSONArray yesNoArray = new JSONArray(yesNoString);

        JSONObject hint = new JSONObject();
        hint.put("value", getString(R.string.SelectPovertyStatus));
        hint.put("key", "");

        JSONArray finalYesNoArray = addFirst(yesNoArray, hint);
        JSONSpinnerAdapter adapter =
                new JSONSpinnerAdapter(this, finalYesNoArray, "value");

        spPovertyStatus.setAdapter(adapter);
    }

    private void getConfirmationTypes() throws JSONException {
        String textLanguage = "ConfirmationType";
        String confirmationString = ca.getConfirmationTypes();
        JSONArray confirmationArray = new JSONArray(confirmationString);

        JSONObject hint = new JSONObject();
        hint.put("ConfirmationTypeCode", "");
        hint.put(textLanguage, getString(R.string.SelectConfirmationType));

        JSONArray finalConfirmationArray = addFirst(confirmationArray, hint);

        JSONSpinnerAdapter adapter =
                new JSONSpinnerAdapter(this, finalConfirmationArray, textLanguage);
        spConfirmationType.setAdapter(adapter);
    }

    private void getFamilyTypes() throws JSONException {
        String textLanguage = "FamilyType";
        String familyTypesString = ca.getGroupTypes();
        JSONArray familyTypesArray = new JSONArray(familyTypesString);

        JSONObject hint = new JSONObject();
        hint.put("FamilyTypeCode", "");
        hint.put(textLanguage, getString(R.string.SelectFamilyType));

        JSONArray finaFamilyTypeArray = addFirst(familyTypesArray, hint);
        JSONSpinnerAdapter adapter =
                new JSONSpinnerAdapter(this, finaFamilyTypeArray, "FamilyType");
        spFamilyType.setAdapter(adapter);
    }

    private void getApprovalOfSMS() throws JSONException {
        String approvalString = ca.getApprovalOfSMS();
        JSONArray approvalArray = new JSONArray(approvalString);

        JSONObject hint = new JSONObject();
        hint.put("value", "");
        hint.put("key", getString(R.string.approvalOfSMS));

        JSONArray finalApprovalArray = addFirst(approvalArray, hint);
        JSONSpinnerAdapter adapter =
                new JSONSpinnerAdapter(this, finalApprovalArray, "key");
        spApprovalSMS.setAdapter(adapter);
    }

    private void getLanguageOfSMS() throws JSONException {
        String textLanguage = "LanguageName";
        String languagesString = ca.getLanguagesOfSMS();
        JSONArray languagesArray = new JSONArray(languagesString);

        JSONObject hint = new JSONObject();
        hint.put("LanguageCode", "");
        hint.put(textLanguage, getString(R.string.languageOfSMS));

        JSONArray finalLanguagesArray = addFirst(languagesArray, hint);

        JSONSpinnerAdapter adapter =
                new JSONSpinnerAdapter(this, finalLanguagesArray, "LanguageName");
        spLanguageSMS.setAdapter(adapter);
    }

    public JSONArray addFirst(JSONArray array, JSONObject object) throws JSONException {
        JSONArray newArray = new JSONArray();
        newArray.put(object);
        for (int i = 0; i < array.length(); i++) {
            newArray.put(array.get(i));
        }
        return newArray;
    }

    public void setupListeners(){

        spRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                JSONObject selectedRegion = ((JSONObject) spRegion.getSelectedItem());
                try {
                    String regionId = selectedRegion.getString("LocationId");
                    if(regionId.isEmpty()){
                        spDistrict.setAdapter(null);
                        spWard.setAdapter(null);
                        spVillage.setAdapter(null);
                    } else{
                        loadDistricts(Integer.parseInt(regionId));
                    }
                    canSaveFamily();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                JSONObject district = (JSONObject) spDistrict.getSelectedItem();
                canSaveFamily();
                try {
                    String districtId = district.getString("LocationId");
                    if (districtId.isEmpty()){
                        spWard.setAdapter(null);
                        spVillage.setAdapter(null);
                    } else {
                        loadWards(Integer.parseInt(districtId));
                    }
                    canSaveFamily();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                JSONObject ward = (JSONObject) spWard.getSelectedItem();
                canSaveFamily();
                try {
                    String wardId = ward.getString("LocationId");
                    if(wardId.isEmpty()){
                        spVillage.setAdapter(null);
                    } else {
                        loadVillages(Integer.parseInt(wardId));
                    }
                    canSaveFamily();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    JSONObject village = (JSONObject) spVillage.getSelectedItem();
                    canSaveFamily();
                    familyObj.put("LocationId", village.getString("LocationId"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spPovertyStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    JSONObject selectedItem = (JSONObject) spPovertyStatus.getSelectedItem();
                    familyObj.put("Poverty", selectedItem.getString("key"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spConfirmationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try{
                    JSONObject selectedItem = (JSONObject) spConfirmationType.getSelectedItem();
                    familyObj.put("ConfirmationType", selectedItem.getString("ConfirmationTypeCode"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spFamilyType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    JSONObject selectedItem = (JSONObject) spFamilyType.getSelectedItem();
                    familyObj.put("FamilyType", selectedItem.getString("FamilyTypeCode"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spLanguageSMS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    JSONObject selectedItem = (JSONObject) spLanguageSMS.getSelectedItem();
                    familyObj.put("LanguageOfSMS", selectedItem.getString("LanguageCode"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spApprovalSMS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    JSONObject selectedItem = (JSONObject) spApprovalSMS.getSelectedItem();
                    familyObj.put("ApprovalOfSMS", selectedItem.getString("key"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnNext.setOnClickListener(v -> saveFamily());

    }

}