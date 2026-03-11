package org.openimis.imispolicies;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InsureeActivity extends AppCompatActivity {

    JSONObject familyObject;
    JSONObject insureeObject;

    private ImageView imgInsuree;
    private MaterialCardView cardImage;
    private TextInputEditText txtLastName, txtOtherNames, txtInsuranceNumber, txtBirthDate, txtCurrentAddress, txtPhoneNumber,
            txtEmail, txtIdentificationNumber;
    private MaterialButton btnSave, btnScan;
    private MaterialAutoCompleteTextView ddlMaritalStatus, ddlBeneficiaryCard, ddlGenders, ddlRelationships, ddlCurrentRegion,
            ddlCurrenDistricts, ddlCurrentMunicipality, ddlCurrentVillage, ddlFSPRegion, ddlFSPDistrict, ddlFSPCategory, ddlFSP,
            ddlProfession, ddlEducation, ddlIdentificationType, ddlVulnerability;

    private int insureeId = 0;
    private String isOffline = ".";
    private int isHead = -1;
    private String photoPath = "";
    private String newPhotoPath = "";
    private Uri photoUri;
    private Calendar calendar = Calendar.getInstance();
    private String hfImagePath;
    private String hfNewPhotoPath;

    public static String filePath = null;
    public static Uri tempPhotoUri = null;
    public static int RESULT_LOAD_IMG = 1;
    public static int RESULT_SCAN = 100;

    // Adapters
    private JSONSpinnerAdapter relationAdapter;
    private JSONSpinnerAdapter genderAdapter;

    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

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
            } else {
                // File selection
                selectedImage = data.getData();
            }
            selectImageCallback(selectedImage);
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
        ca = new ClientAndroidInterface(this);
        insureeObject = new JSONObject();
        initViews();
        setupPickers();
        setupDatePicker();
        setupListeners();

        String jsonString = getIntent().getStringExtra("FamilyData");
        if (jsonString != null) {
            try {
                familyObject = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            loadInitialData();
        }

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
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );
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
                                    ddlCurrenDistricts.setText("");
                                    ddlCurrentMunicipality.setText("");
                                    ddlCurrentVillage.setText("");
                                    fillCurrentDistricts(regionId);
                                    fillCurrentWard(null);
                                    fillCurrentVillage(null);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
            );
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
                                        ddlCurrentVillage.setText("");
                                        fillCurrentVillage(wardId);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                );
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
                                    ddlFSP.setText("");
                                    fillFSP(FSPDistrictId, HFLevel);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
            );
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
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });
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
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        });
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
                    textLanguage, // displayField
                    null,     // pas de texte par défaut
                    new JsonDropdownHelper.OnJsonItemSelectedListener() {
                        @Override
                        public void onItemSelected(JSONObject selectedItem, int position) {
                            if (selectedItem != null) {
                                try {
                                    String code = selectedItem.getString("Code");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );
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
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
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
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
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
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
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
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );
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
                                    String status = selectedItem.getString("Status");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );
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
                                    String key = selectedItem.getString("key");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Texte par défaut sélectionné
                                Log.d("BeneficiaryCard", "Default selected");
                            }
                        }
                    }
            );
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

    public JSONArray addFirst(JSONArray array, JSONObject object) throws JSONException {
        JSONArray newArray = new JSONArray();
        newArray.put(object);
        for (int i = 0; i < array.length(); i++) {
            newArray.put(array.get(i));
        }
        return newArray;
    }

    private void setupListeners() {

        // Insurance number on text changed
        txtInsuranceNumber.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

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

            // Afficher l'URI dans les logs pour déboguer
            Log.d("DEBUG_URI", "URI reçue: " + imageUri.toString());
            Log.d("DEBUG_URI", "Scheme: " + imageUri.getScheme());
            Log.d("DEBUG_URI", "Path: " + imageUri.getPath());


            // Vérifier si l'URI est accessible
            try {
                InputStream testStream = getContentResolver().openInputStream(imageUri);
                if (testStream != null) {
                    testStream.close();
                    Log.d("DEBUG_URI", "L'URI est accessible");
                }
            } catch (Exception e) {
                Log.e("DEBUG_URI", "L'URI n'est pas accessible: " + e.getMessage());
                Toast.makeText(this, "Image non accessible", Toast.LENGTH_SHORT).show();
                return;
            }

            String imagePath = getPathFromUri(imageUri);
            hfNewPhotoPath = imagePath;
            saveImagePath(imagePath);

            loadImage(imageUri);

            // Log pour débogage
            Log.d("selectImageCallback", "Image loaded from URI: " + imageUri.toString());
            Log.d("selectImageCallback", "Image path: " + imagePath);

            // Afficher un message de confirmation
            Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();

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

    private String getSavedImagePath() {
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        return prefs.getString("hfNewPhotoPath", "");
    }

    private void getImage() {
        String insuranceNumber = txtInsuranceNumber.getText().toString().trim();
        String imagePath = ca.GetListOfImagesContain(insuranceNumber);

        if (imagePath != null && !imagePath.isEmpty()) {
            loadImage(imagePath);
            hfImagePath = "file://" + imagePath;
        } else {
            imgInsuree.setImageResource(android.R.color.transparent);
            imgInsuree.setImageDrawable(null);
            hfImagePath = "";
        }
        saveImagePath(hfImagePath);

        // Log pour débogage
        Log.d("getImage", "Insurance Number: " + insuranceNumber + ", Image Path: " + hfImagePath);
    }

    private void saveImagePath(String path) {
        SharedPreferences prefs = getSharedPreferences("MyApp", MODE_PRIVATE);
        prefs.edit().putString("hfImagePath", path).apply();
    }

    private void loadImage(String imagePath) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imgInsuree.setImageBitmap(bitmap);
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
        // Load any initial data from intent or database
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            insureeId = extras.getInt("insureeId", 0);
            isOffline = extras.getString("isOffline", ".");
            isHead = extras.getInt("isHead", -1);
            photoPath = extras.getString("photoPath", "");
            newPhotoPath = extras.getString("newPhotoPath", "");

            // Populate fields with data if editing
            if (insureeId > 0) {
                // Load existing insuree data
                loadInsureeData(insureeId);
            }
        }
    }

    private void loadInsureeData(int id) {
        // Implement loading data from database or repository
        Toast.makeText(this, "Loading insuree data for ID: " + id, Toast.LENGTH_SHORT).show();

        // Example of setting data:
        // txtInsuranceNumber.setText("123456");
        // txtOtherNames.setText("John");
        // txtLastName.setText("Doe");
        // txtBirthDate.setText("1990-01-01");
        // ddlGender.setText("Male", false);
        // ddlRelationship.setText("Spouse", false);

        // Load image if exists
        // if (photoPath != null && !photoPath.isEmpty()) {
        //     imgInsuree.setImageBitmap(BitmapFactory.decodeFile(photoPath));
        // }
    }

    // Method to handle no image
    public void onNoImage(ImageView imageView) {
        // Set default image when loading fails
        imageView.setImageResource(R.drawable.person);
    }

    private boolean validateForm() {
        boolean isValid = true;

        return isValid;
    }

    // Method to save the form data
    public void saveFormData() {
        // Validate required fields
        if (!validateForm()) {
            return;
        }

        // Collect data
//        String relation = ddRelationship.getSelectedItem().toString();
//        String insuranceNumber = txtInsuranceNumber.getText().toString().trim();
//        String otherNames = txtOtherNames.getText().toString().trim();
//        String lastName = txtLastName.getText().toString().trim();
//        String birthDate = txtBirthDate.getText().toString().trim();
//        String gender = ddGender.getSelectedItem().toString();

        // Create object or save to database
        JSONObject insureeObj = new JSONObject();

//        insuree.setInsureeId(insureeId);
//        insuree.setChfid(insuranceNumber);
//        insuree.setOtherNames(otherNames);
//        insuree.setLastName(lastName);
//        insuree.setDob(birthDate);
//        insuree.setGender(gender);
//        insuree.setPhotoPath(photoPath);
//        insuree.setNewPhotoPath(newPhotoPath);
//        insuree.setOffline(isOffline);
//        insuree.setHead(isHead == 1);

        // Save to database
        // databaseHelper.insertOrUpdateInsuree(insuree);

        ca.SaveFamily(familyObject.toString(),insureeObj.toString());
        Toast.makeText(this, "Form saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}