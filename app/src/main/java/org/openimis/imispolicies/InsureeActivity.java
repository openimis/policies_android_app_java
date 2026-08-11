package org.openimis.imispolicies;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.client.android.Intents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.util.JsonDropdownHelper;
import org.openimis.imispolicies.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class InsureeActivity extends AppCompatActivity {

    JSONObject familyObject;
    private JSONObject insureeObject;

    private ImageView imgInsuree;
    private MaterialCardView cardImage;
    private TextInputEditText txtLastName, txtOtherNames, txtInsuranceNumber, txtBirthDate, txtCurrentAddress, txtPhoneNumber,
            txtEmail, txtIdentificationNumber;
    private TextInputLayout layoutChfId, layoutOtherNames, layoutLastName, layoutBirthDate, layoutGender, layoutRelationships;
    private MaterialButton btnSave, btnScan;
    private MaterialAutoCompleteTextView ddlMaritalStatus, ddlBeneficiaryCard, ddlGenders, ddlRelationships, ddlCurrentRegion,
            ddlCurrenDistricts, ddlCurrentMunicipality, ddlCurrentVillage, ddlFSPRegion, ddlFSPDistrict, ddlFSPCategory, ddlFSP,
            ddlProfession, ddlEducation, ddlIdentificationType, ddlVulnerability;

    private int insureeId = 0;
    private String isOffline = ".";
    private int isHead = -1;
    private String photoPath = "";
    private String hfImagePath;
    private String hfNewPhotoPath;
    private int familyId;
    public Uri tempPhotoUri = null;

    public static String filePath = null;

    public static int RESULT_LOAD_IMG = 1;
    public static int RESULT_SCAN = 100;

    // Date picker
    private MaterialDatePicker<Long> datePicker;

    ClientAndroidInterface ca;

    private String FSPDistrictId, HFLevel;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ClientAndroidInterface.RESULT_LOAD_IMG && resultCode == RESULT_OK) {
            Uri selectedImage;
            if (data == null || data.getData() == null ||
                    (data.getData() != null
                            && data.getAction() != null
                            && data.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE))) {
                Log.d("Main", "RESULT_LOAD_IMG got a camera result, in the predefined location");
                selectedImage = ClientAndroidInterface.tempPhotoUri;
                captureImageCallback(selectedImage);
            } else {
                // File selection
                selectedImage = data.getData();
                ca.setTempPhotoUri(selectedImage);
                selectImageCallback(selectedImage);
            }
        } else if (requestCode == ClientAndroidInterface.RESULT_SCAN && resultCode == RESULT_OK && data != null) {
            String insureeNumber = data.getStringExtra(Intents.Scan.RESULT);
            if (!StringUtils.isEmpty(insureeNumber)) {
                scanQrCallback(insureeNumber);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insuree);
        setTitle(getResources().getString(R.string.AddEditInsuree));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        insureeObject = new JSONObject();
        ca = new ClientAndroidInterface(this);
        insureeId = getIntent().getIntExtra("InsureeId", 0);
        familyId = getIntent().getIntExtra("FamilyId", 0);
        initViews();
        layoutRelationships.setVisibility(View.GONE);

        String jsonString = getIntent().getStringExtra("FamilyData");
        if (jsonString != null) {
            // creation d'une famille
            try {
                familyObject = new JSONObject(jsonString);
                isHead = 1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if(insureeId != 0){
                // modification d'un assuré
                loadInitialData();
            } else {
                // création d'un membre
                isHead = 0;
                layoutRelationships.setVisibility(View.VISIBLE);
                layoutRelationships.setError(" ");
            }
        }
        setupPickers();
        setupDatePicker();
        setupListeners();
        defineRequiredField();
        canSave();
    }

    private void initViews() {
        ddlRelationships = findViewById(R.id.ddlRelationships);
        txtInsuranceNumber = findViewById(R.id.txtInsuranceNumber);
        txtOtherNames = findViewById(R.id.txtOtherNames);
        txtLastName = findViewById(R.id.txtLastName);
        imgInsuree = findViewById(R.id.imgInsuree);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        ddlGenders = findViewById(R.id.ddlGenders);
        cardImage = findViewById(R.id.cardImage);
        btnSave = findViewById(R.id.btnSave);
        ddlMaritalStatus = findViewById(R.id.ddlMaritalStatus);
        ddlBeneficiaryCard = findViewById(R.id.ddlBeneficiaryCard);
        ddlCurrentRegion = findViewById(R.id.ddlCurrentRegion);
        ddlCurrenDistricts = findViewById(R.id.ddlCurrentDistrict);
        ddlCurrentMunicipality = findViewById(R.id.ddlCurrentMunicipality);
        ddlCurrentVillage = findViewById(R.id.ddlCurrentVillage);
        ddlFSPRegion = findViewById(R.id.ddlFSPRegion);
        ddlFSPDistrict = findViewById(R.id.ddlFSPDistrict);
        ddlFSPCategory = findViewById(R.id.ddlFSPCategory);
        ddlFSP = findViewById(R.id.ddlFSP);
        ddlProfession = findViewById(R.id.ddlProfession);
        ddlEducation = findViewById(R.id.ddlEducation);
        ddlIdentificationType = findViewById(R.id.ddlIdentificationType);
        ddlVulnerability = findViewById(R.id.ddlVulnerability);
        btnScan = findViewById(R.id.btnScan);
        layoutChfId = findViewById(R.id.layoutChfID);
        layoutOtherNames = findViewById(R.id.layoutOtherNames);
        layoutLastName = findViewById(R.id.layoutLastName);
        layoutBirthDate = findViewById(R.id.layoutBirthDate);
        layoutGender = findViewById(R.id.layoutGenders);
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber);
        txtEmail = findViewById(R.id.txtEmail);
        txtCurrentAddress = findViewById(R.id.txtCurrentAddress);
        txtIdentificationNumber = findViewById(R.id.txtIdentificationNumber);
        layoutRelationships = findViewById(R.id.layoutRelationships);
    }

    private void defineRequiredField(){
        layoutChfId.setError(" ");
        layoutLastName.setError(" ");
        layoutBirthDate.setError(" ");
        layoutOtherNames.setError(" ");
        layoutGender.setError(" ");
    }

    private void setupPickers() {
        fillRelationships();
        fillGenders();
        fillMaritalStatus();
        fillBeneficiaryCard();
        fillCurrentRegions();
        fillFSPRegions();
        fillEducations();
        fillProfession();
        fillIdentificationTypes();
        fillVulnerability();
    }

    private void canSave(){
        if(txtInsuranceNumber.getText().toString().isEmpty() ||
                txtBirthDate.getText().toString().isEmpty() ||
                txtLastName.getText().toString().isEmpty() ||
                txtOtherNames.getText().toString().isEmpty() ||
                ddlGenders.getText().toString().isEmpty()
        ){
            btnSave.setEnabled(false);
        } else if (isHead != 1 && ddlRelationships.getText().toString().isEmpty()) {
            btnSave.setEnabled(false);
        } else {
            btnSave.setEnabled(true);
        }
    }

    private void fillRelationships (){
        try {
            String textLanguage = "Relation";
//            if (ca.getSelectedLanguage() != "en") {
//                textLanguage = "AltLanguage";
//            }
            // Setup Relationship Spinner
            String relationships = ca.getRelationships();
            JSONArray relationshipsArray = new JSONArray(relationships);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlRelationships,
                    relationshipsArray,
                    textLanguage,
                    "",
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    String relationId = selectedItem.getString("RelationId");
                                    insureeObject.put("ddlRelationship", relationId);
                                    canSave();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(insureeObject != null && insureeObject.has("Relationship") && !insureeObject.getString("Relationship").isEmpty()){
                String savedValue = insureeObject.getString("Relationship");
                insureeObject.put("ddlRelationship", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlRelationships,
                        relationshipsArray,
                        textLanguage,
                        "RelationId",
                        savedValue
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillCurrentRegions (){
        try{
            String data = ca.getRegionsWO();
            JSONArray regionsArray = new JSONArray(data);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlCurrentRegion,
                    regionsArray,
                    "LocationName",
                    "",
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if(selectedItem != null){
                                try {
                                    String regionId = selectedItem.getString("LocationId");
                                    insureeObject.put("ddlCurrentRegion", regionId);
                                    fillCurrentDistricts(regionId);
                                    ddlCurrentMunicipality.setAdapter(null);
                                    ddlCurrentVillage.setAdapter(null);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
            );

            if(insureeObject != null && insureeObject.has("CurRegion") && !insureeObject.getString("CurRegion").isEmpty()){
                String savedValue = insureeObject.getString("CurRegion");
                insureeObject.put("ddlCurrentRegion", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlCurrentRegion,
                        regionsArray,
                        "LocationName",
                        "LocationId",
                        savedValue
                );
            }
        } catch (JSONException e){
            throw new RuntimeException(e);
        }
    }

    private void fillFSPRegions (){
        try{
            String data = ca.getRegionsWO();
            JSONArray regionsArray = new JSONArray(data);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlFSPRegion,
                    regionsArray,
                    "LocationName",
                    "",
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if(selectedItem != null){
                                try {
                                    String regionId = selectedItem.getString("LocationId");
                                    insureeObject.put("ddlFSPRegion", regionId);
                                    ddlFSPDistrict.setText("");
                                    ddlFSP.setText("");
                                    ddlFSPCategory.setText("");
                                    fillFSPDistricts(regionId);
                                    fillFSP(null, null);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
            );

            if(insureeObject != null && insureeObject.has("FSPRegion") && !insureeObject.getString("FSPRegion").isEmpty()){
                String savedValue = insureeObject.getString("FSPRegion");
                insureeObject.put("ddlFSPRegion", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlFSPRegion,
                        regionsArray,
                        "LocationName",
                        "LocationId",
                        savedValue
                );
            }
        } catch (JSONException e){
            throw new RuntimeException(e);
        }
    }

    private void fillCurrentDistricts(String regionId){
        try{
            if(regionId != null && !regionId.isEmpty()){
                String data = ca.getDistrictsWO(Integer.parseInt(regionId));
                JSONArray districtsArray = new JSONArray(data);
                JsonDropdownHelper.bindDropdown(
                        this,
                        ddlCurrenDistricts,
                        districtsArray,
                        "LocationName",
                        "",
                        new JsonDropdownHelper.OnJsonItemSelectedListener() {
                            @Override
                            public void onItemSelected(JSONObject selectedItem, int position) {
                                if(selectedItem != null){
                                    try {
                                        String districtId = selectedItem.getString("LocationId");
                                        insureeObject.put("ddlCurrentDistrict", districtId);
                                        ddlCurrentMunicipality.setText("");
                                        ddlCurrentVillage.setText("");
                                        fillCurrentWard(districtId);
                                        fillCurrentVillage(null);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                );

                if(insureeObject != null && insureeObject.has("CurDistrict") && !insureeObject.getString("CurDistrict").isEmpty()){
                    String savedValue = insureeObject.getString("CurDistrict");
                    insureeObject.put("ddlCurrentDistrict", savedValue);
                    JsonDropdownHelper.selectValue(
                            this,
                            ddlCurrenDistricts,
                            districtsArray,
                            "LocationName",
                            "LocationId",
                            savedValue
                    );
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillFSPDistricts(String regionId){
        try{
            if(regionId != null && !regionId.isEmpty()){
                String data = ca.getDistrictsWO(Integer.parseInt(regionId));
                JSONArray districtsArray = new JSONArray(data);
                JsonDropdownHelper.bindDropdown(
                        this,
                        ddlFSPDistrict,
                        districtsArray,
                        "LocationName",
                        "",
                        new JsonDropdownHelper.OnJsonItemSelectedListener() {
                            @Override
                            public void onItemSelected(JSONObject selectedItem, int position) {
                                if(selectedItem != null){
                                    try {
                                        if(!selectedItem.getString("LocationId").isEmpty()){
                                            FSPDistrictId = selectedItem.getString("LocationId");
                                            insureeObject.put("ddlFSPDistrict", FSPDistrictId);
                                            ddlFSPCategory.setText("");
                                            ddlFSP.setText("");
                                            fillFSPCategory();
                                            fillFSP(FSPDistrictId, HFLevel);
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                );

                if(insureeObject != null && insureeObject.has("FSPDistrict") &&
                        !insureeObject.getString("FSPDistrict").isEmpty()){
                    String savedValue = insureeObject.getString("FSPDistrict");
                    insureeObject.put("ddlFSPDistrict", savedValue);
                    JsonDropdownHelper.selectValue(
                            this,
                            ddlFSPDistrict,
                            districtsArray,
                            "LocationName",
                            "LocationId",
                            savedValue
                    );
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillCurrentWard(String districtId){
        try {
            if (districtId != null && !districtId.isEmpty()) {
                String data = ca.getWards(Integer.parseInt(districtId));
                JSONArray wardsArray = new JSONArray(data);
                JsonDropdownHelper.bindDropdown(
                        this,
                        ddlCurrentMunicipality,
                        wardsArray,
                        "LocationName",
                        null,
                        new JsonDropdownHelper.OnJsonItemSelectedListener() {
                            @Override
                            public void onItemSelected(JSONObject selectedItem, int position) {
                                if(selectedItem != null){
                                    try {
                                        String wardId = selectedItem.getString("LocationId");
                                        insureeObject.put("ddlCurrentMunicipality", wardId);
                                        ddlCurrentVillage.setText("");
                                        fillCurrentVillage(wardId);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                );

                if(insureeObject != null && insureeObject.has("CurWard") &&
                        !insureeObject.getString("CurWard").isEmpty()){
                    String savedValue = insureeObject.getString("CurWard");
                    insureeObject.put("ddlCurrentMunicipality", savedValue);
                    JsonDropdownHelper.selectValue(
                            this,
                            ddlCurrentMunicipality,
                            wardsArray,
                            "LocationName",
                            "LocationId",
                            savedValue
                    );
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillFSPCategory(){
        try {
            String data = ca.getHFLevels();
            JSONArray wardsArray = new JSONArray(data);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlFSPCategory,
                    wardsArray,
                    "HFLevel",
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if(selectedItem != null){
                                try {
                                    HFLevel = selectedItem.getString("Code");
                                    insureeObject.put("ddlFSPCategory", HFLevel);
                                    ddlFSP.setText("");
                                    fillFSP(FSPDistrictId, HFLevel);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
            );

            if(insureeObject != null && insureeObject.has("FSPCategory") &&
                    !insureeObject.getString("FSPCategory").isEmpty()){
                String savedValue = insureeObject.getString("FSPCategory");
                insureeObject.put("ddlFSPCategory", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlFSPCategory,
                        wardsArray,
                        "HFLevel",
                        "Code",
                        savedValue
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillCurrentVillage(String wardId){
        if(wardId != null && !wardId.isEmpty()) {
            String data = ca.getVillages(Integer.parseInt(wardId));
            try {
                JSONArray villagesArray = new JSONArray(data);
                JsonDropdownHelper.bindDropdown(
                        this,
                        ddlCurrentVillage,
                        villagesArray,
                        "LocationName",
                        null,
                        new JsonDropdownHelper.OnJsonItemSelectedListener() {
                            @Override
                            public void onItemSelected(JSONObject selectedItem, int position) {
                                if(selectedItem != null){
                                    try {
                                        String villageId = selectedItem.getString("LocationId");
                                        insureeObject.put("ddlCurrentVillage", villageId);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });

                if(insureeObject != null && insureeObject.has("CurVillage") &&
                        !insureeObject.getString("CurVillage").isEmpty()){
                    String savedValue = insureeObject.getString("CurVillage");
                    insureeObject.put("ddlCurrentVillage", savedValue);
                    JsonDropdownHelper.selectValue(
                            this,
                            ddlCurrentVillage,
                            villagesArray,
                            "LocationName",
                            "LocationId",
                            savedValue
                    );
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void fillFSP(String districtId, String HFLevel){
        if(districtId != null && !districtId.isEmpty() && HFLevel != null && !HFLevel.isEmpty()) {
            String data = ca.getHF(Integer.parseInt(districtId), HFLevel);
            try {
                JSONArray villagesArray = new JSONArray(data);
                JsonDropdownHelper.bindDropdown(
                        this,
                        ddlFSP,
                        villagesArray,
                        "HF",
                        null,
                        new JsonDropdownHelper.OnJsonItemSelectedListener() {
                            @Override
                            public void onItemSelected(JSONObject selectedItem, int position) {
                                if(selectedItem != null){
                                    try {
                                        String FSPId = selectedItem.getString("HFID");
                                        insureeObject.put("ddlFSP", FSPId);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });

                if(insureeObject != null && insureeObject.has("HFID") &&
                        !insureeObject.getString("HFID").isEmpty()){
                    String savedValue = insureeObject.getString("HFID");
                    insureeObject.put("ddlFSP", savedValue);
                    JsonDropdownHelper.selectValue(
                            this,
                            ddlFSP,
                            villagesArray,
                            "HF",
                            "HFID",
                            savedValue
                    );
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void fillGenders() {

        try {
            String textLanguage = "Gender";
//            if (ca.getSelectedLanguage() != "en") {
//                textLanguage = "AltLanguage";
//            }
            String genders = ca.getGender();
            JSONArray gendersArray = new JSONArray(genders);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlGenders,
                    gendersArray,
                    textLanguage,
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    String code = selectedItem.getString("Code");
                                    insureeObject.put("ddlGender", code);
                                    canSave();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(insureeObject != null && insureeObject.has("Gender") &&
                    !insureeObject.getString("Gender").isEmpty()){
                String savedValue = insureeObject.getString("Gender");
                insureeObject.put("ddlGender", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlGenders,
                        gendersArray,
                        textLanguage,
                        "Code",
                        savedValue
                        );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillEducations(){
        String textLanguage = "Education";
//        if (ca.getSelectedLanguage() != "en") {
//            textLanguage = "AltLanguage";
        String data = ca.getEducations();
        try {
            JSONArray educationsArray = new JSONArray(data);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlEducation,
                    educationsArray,
                    textLanguage,
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            try {
                                String educationId = selectedItem.getString("EducationId");
                                insureeObject.put("ddlEducation", educationId);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            if(insureeObject != null && insureeObject.has("Education") &&
                    !insureeObject.getString("Education").isEmpty()){
                String savedValue = insureeObject.getString("Education");
                insureeObject.put("ddlEducation", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlEducation,
                        educationsArray,
                        textLanguage,
                        "EducationId",
                        savedValue
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillProfession() {
        String textLanguage = "Profession";
        String data = ca.getProfessions();
        try {
            JSONArray professionsArray = new JSONArray(data);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlProfession,
                    professionsArray,
                    textLanguage,
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            try {
                                String professionId = selectedItem.getString("ProfessionId");
                                insureeObject.put("ddlProfession", professionId);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            if(insureeObject != null && insureeObject.has("Profession") &&
                    !insureeObject.getString("Profession").isEmpty()){
                String savedValue = insureeObject.getString("Profession");
                insureeObject.put("ddlProfession", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlProfession,
                        professionsArray,
                        textLanguage,
                        "ProfessionId",
                        savedValue
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillIdentificationTypes(){
        String textLanguage = "IdentificationTypes";
        String data = ca.getIdentificationTypes();
        try {
            JSONArray identificationTypesArray = new JSONArray(data);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlIdentificationType,
                    identificationTypesArray,
                    textLanguage,
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            try {
                                String identificationTypeId = selectedItem.getString("IdentificationCode");
                                insureeObject.put("ddlIdentificationType", identificationTypeId);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

            if(insureeObject != null && insureeObject.has("typeOfId") &&
                    !insureeObject.getString("TypeOfId").isEmpty()){
                String savedValue = insureeObject.getString("TypeOfId");
                insureeObject.put("ddlIdentificationType", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlIdentificationType,
                        identificationTypesArray,
                        textLanguage,
                        "Code",
                        savedValue
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillVulnerability (){
        String data = ca.getVulnerability();
        try {
            JSONArray vulnerabilityArray = new JSONArray(data);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlVulnerability,
                    vulnerabilityArray,
                    "key",
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    String vulnerabilityId = selectedItem.getString("value");
                                    insureeObject.put("ddlVulnerability", vulnerabilityId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(insureeObject != null && insureeObject.has("Vulnerability") &&
                    !insureeObject.getString("Vulnerability").isEmpty()){
                String savedValue = insureeObject.getString("Vulnerability");
                insureeObject.put("ddlVulnerability", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlVulnerability,
                        vulnerabilityArray,
                        "key",
                        "value",
                        savedValue
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    private void fillMaritalStatus() {
        String maritalStatus = ca.getMaritalStatus();
        try {
            JSONArray maritalStatusData = new JSONArray(maritalStatus);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlMaritalStatus,
                    maritalStatusData,
                    "Status", // displayField
                    null,     // pas de texte par défaut
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    String code = selectedItem.getString("Code");
                                    insureeObject.put("ddlMaritalStatus", code);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(insureeObject != null && insureeObject.has("Marital") &&
                    !insureeObject.getString("Marital").isEmpty()){
                String savedValue = insureeObject.getString("Marital");
                insureeObject.put("ddlMaritalStatus", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlMaritalStatus,
                        maritalStatusData,
                        "Status",
                        "Code",
                        savedValue
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // fillBeneficiaryCard avec JSON
    private void fillBeneficiaryCard() {
        String yesNoData = ca.getYesNo();
        try {
            JSONArray yesNoArray = new JSONArray(yesNoData);
            JsonDropdownHelper.bindDropdown(
                    this,
                    ddlBeneficiaryCard,
                    yesNoArray,
                    "key", // displayField
                    null,
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    String value = selectedItem.getString("value");
                                    insureeObject.put("ddlBeneficiaryCard", value);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

            if(insureeObject != null && insureeObject.has("CardIssued") &&
                    !insureeObject.getString("CardIssued").isEmpty()){
                String savedValue = insureeObject.getString("CardIssued");
                insureeObject.put("ddlBeneficiaryCard", savedValue);
                JsonDropdownHelper.selectValue(
                        this,
                        ddlBeneficiaryCard,
                        yesNoArray,
                        "key",
                        "value",
                        savedValue
                );
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    private void setupDatePicker() {
        // Create Material Date Picker
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(getResources().getString(R.string.SelectBirthDate));
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            txtBirthDate.setText(format.format(calendar.getTime()));
        });

        txtBirthDate.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupListeners() {

        // Insurance number on text changed
        txtInsuranceNumber.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String chfId = txtInsuranceNumber.getText().toString().trim();
//                boolean ans = ca.isValidInsuranceNumber(chfId);
//                if(ans != true){
//                    txtInsuranceNumber.setText("");
//                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {

            }
        });

        // Image card click listener
        cardImage.setOnClickListener(v -> ca.selectPicture());

        // Save button
        btnSave.setOnClickListener(v -> saveFormData());

        btnScan.setOnClickListener(v -> {
            ca.getScannedNumber();
        });

    }

    public void scanQrCallback(String insureeNumber){
        if(ca.isValidInsuranceNumber(insureeNumber)){
            txtInsuranceNumber.setText(insureeNumber);
            getImage();
        } else {
            txtInsuranceNumber.setText("");
            txtInsuranceNumber.setFocusable(true);
        }
    }

    public void selectImageCallback(Uri imageUri) {
        if (imageUri != null) {
            try {
                InputStream testStream = getContentResolver().openInputStream(imageUri);
                if (testStream != null) {
                    testStream.close();
                }
                hfNewPhotoPath = getPathFromUri(imageUri);
                Log.d("DEBUG_URI", "L'URI est accessible: " + hfNewPhotoPath);
                loadImage(imageUri);
            } catch (Exception e) {
                Log.e("DEBUG_URI", "L'URI n'est pas accessible: " + e.getMessage());
                Toast.makeText(this, "Image non accessible", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Log.d("selectImageCallback", "No image selected");
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void captureImageCallback(Uri imageUri) {
        if (imageUri != null) {
            try {
                InputStream testStream = getContentResolver().openInputStream(imageUri);
                if (testStream != null) {
                    testStream.close();
                }
                hfNewPhotoPath = imageUri.toString();
                Log.d("DEBUG_URI", "L'URI est accessible: " + hfNewPhotoPath);
                loadImage(imageUri);
            } catch (Exception e) {
                Log.e("DEBUG_URI", "L'URI n'est pas accessible: " + e.getMessage());
                Toast.makeText(this, "Image non accessible", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Log.d("selectImageCallback", "No image selected");
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getPathFromUri(Uri uri) {
        String path = null;

        // Pour les URI de type content://
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                path = cursor.getString(columnIndex);
                cursor.close();
            }
        }
        // Pour les URI de type file://
        else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
        }

        return path != null ? path : "";
    }

    private void getImage() {
        String insuranceNumber = txtInsuranceNumber.getText().toString().trim();
        String imagePath = ca.GetListOfImagesContain(insuranceNumber);

        if (imagePath != null && !imagePath.isEmpty()) {
            hfImagePath = "file://" + imagePath;
            loadImage(hfImagePath);
        } else {
            imgInsuree.setImageResource(android.R.color.transparent);
            imgInsuree.setImageDrawable(null);
            hfImagePath = "";
        }
    }

    private void loadImage(String imagePath) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imgInsuree.setImageBitmap(bitmap);
        }else{
            imgInsuree.setImageResource(R.drawable.image_not_supported);
        }
    }

    private void loadImage(Uri imageUri){
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imgInsuree.setImageBitmap(bitmap);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadInitialData() {
        Bundle extras = getIntent().getExtras();
        try {
            if (extras != null) {
                String insuree = ca.getInsuree(insureeId);
                JSONArray array = new JSONArray(insuree);
                insureeObject = array.getJSONObject(0);
                isOffline = insureeObject.getString("isOffline");
                if(insureeObject.getString("isHead").equals("true") || insureeObject.getString("isHead").equals("1")){
                    isHead = 1;
                } else {
                    isHead = 0;
                }
                photoPath = insureeObject.getString("PhotoPath");
                if(isHead == 1){
                    layoutRelationships.setVisibility(View.GONE);
                } else {
                    layoutRelationships.setVisibility(View.VISIBLE);
                    layoutRelationships.setError(" ");
                }
                bindInsureeData(insureeObject);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void bindInsureeData(JSONObject object) throws JSONException {

         txtInsuranceNumber.setText(object.getString("CHFID"));
         txtOtherNames.setText(object.getString("OtherNames"));
         txtLastName.setText(object.getString("LastName"));
         txtBirthDate.setText(object.getString("DOB"));
         txtIdentificationNumber.setText(object.getString("IdentificationNumber"));
         txtPhoneNumber.setText(object.getString("Phone"));
         txtEmail.setText(object.getString("Email"));
         txtCurrentAddress.setText(object.getString("CurrentAddress"));

         if(photoPath.isEmpty()){
             getImage();
         }

         //Load image if exists
         if (photoPath != null && !photoPath.isEmpty()) {
             var photoFolder = ca.GetSystemImageFolder();
             if (photoPath.indexOf(photoFolder) == -1) {
                 photoPath = photoFolder + photoPath;
                 loadImage(photoPath);
             }
         }
    }

    public void saveFormData() {
        try {
            getImage();
            if(isOffline.equals("true")||isOffline.equals("1")){
                isOffline = "1";
            } else {
                isOffline = "0";
            }
            insureeObject.put("isOffline", Integer.parseInt(isOffline));
            insureeObject.put("hfisHead", isHead);
            insureeObject.put("hfImagePath", hfImagePath);
            insureeObject.put("hfNewPhotoPath", hfNewPhotoPath);
            insureeObject.put("txtInsuranceNumber", txtInsuranceNumber.getText().toString());
            insureeObject.put("txtLastName", txtLastName.getText().toString());
            insureeObject.put("txtOtherNames", txtOtherNames.getText().toString());
            insureeObject.put("txtBirthDate", txtBirthDate.getText().toString());
            insureeObject.put("txtIdentificationNumber", txtIdentificationNumber.getText().toString());
            insureeObject.put("txtPhoneNumber", txtPhoneNumber.getText().toString());
            insureeObject.put("txtEmail", txtEmail.getText().toString());
            insureeObject.put("txtCurrentAddress", txtCurrentAddress.getText().toString());
            insureeObject.put("hfInsureeId", String.valueOf(insureeId));

            if(familyObject != null){
                familyId = ca.SaveFamily(familyObject.toString(),insureeObject.toString());
                if(familyId > 0){
                    Intent intent = new Intent(this, FamilyInsurees.class);
                    intent.putExtra("FamilyId", familyId);
                    setResult(RESULT_OK);
                    startActivity(intent);
                    finish();
                }
            } else if(insureeId == 0){
                // ajout d'un membre
                int ans = ca.SaveInsuree(insureeObject.toString(),familyId, isHead, 0, 0);
                if(ans!=0 && ans != 7){
                    FragmentActivity activity = (FragmentActivity) this;
                    FragmentManager fm = activity.getSupportFragmentManager();
                    Bundle result = new Bundle();
                    result.putBoolean("refresh_insurees", true);
                    fm.setFragmentResult("requestKey", result);
                    finish();
                }
            } else {
                // modification d'un assuré
                String FamilyPolicy = ca.getFamilyPolicy(familyId);
                JSONArray policies = new JSONArray(FamilyPolicy);
                JSONObject policy = policies.getJSONObject(0);
                int MemberCount = Integer.parseInt(policy.getString("MemberCount"));
                int Threshold = 0;
                if(!policy.getString("Threshold").equals("null") && !policy.getString("Threshold").equals("")) {
                    Threshold = Integer.parseInt(policy.getString("Threshold"));
                }
                int TotalIns = Integer.parseInt(policy.getString("Ins"));
                int PolicyId = Integer.parseInt(policy.getString("PolicyId"));
                int exceedThreshold = -1;

                if (PolicyId > 0 && insureeId == 0) {

                    if (TotalIns >= MemberCount) {
                        exceedThreshold = 0;
                        ca.ShowDialog(getResources().getString(R.string.ExceedMemberCount));
                    } else if (TotalIns >= Threshold) {
                        exceedThreshold = 1;
                    } else {
                        exceedThreshold = 0;
                    }
                }
                try {
                    int ans = ca.SaveInsuree(insureeObject.toString(),familyId, isHead, 0, 0);
                    if(ans!=0 && ans != 7){
                        finish();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
}