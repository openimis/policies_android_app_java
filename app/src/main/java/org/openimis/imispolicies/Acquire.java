//Copyright (c) 2016-%CurrentYear% Swiss Agency for Development and Cooperation (SDC)
//
//The program users must agree to the following terms:
//
//Copyright notices
//This program is free software: you can redistribute it and/or modify it under the terms of the GNU AGPL v3 License as published by the 
//Free Software Foundation, version 3 of the License.
//This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU AGPL v3 License for more details www.gnu.org.
//
//Disclaimer of Warranty
//There is no warranty for the program, to the extent permitted by applicable law; except when otherwise stated in writing the copyright 
//holders and/or other parties provide the program "as is" without warranty of any kind, either expressed or implied, including, but not 
//limited to, the implied warranties of merchantability and fitness for a particular purpose. The entire risk as to the quality and 
//performance of the program is with you. Should the program prove defective, you assume the cost of all necessary servicing, repair or correction.
//
//Limitation of Liability 
//In no event unless required by applicable law or agreed to in writing will any copyright holder, or any other party who modifies and/or 
//conveys the program as permitted above, be liable to you for damages, including any general, special, incidental or consequential damages 
//arising out of the use or inability to use the program (including but not limited to loss of data or data being rendered inaccurate or losses 
//sustained by you or third parties or a failure of the program to operate with any other programs), even if such holder or other party has been 
//advised of the possibility of such damages.
//
//In case of dispute arising out or in relation to the use of the program, it is subject to the public law of Switzerland. The place of jurisdiction is Berne.

package org.openimis.imispolicies;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.provider.MediaStore.EXTRA_OUTPUT;

public class Acquire extends AppCompatActivity {
    private static final String LOG_TAG = "ACQUIRE";
    private static final int SCAN_QR_REQUEST_CODE = 0;
    private static final int TAKE_PHOTO_REQUEST_CODE = 1;

    private Global global;

    private ImageButton btnScan, btnTakePhoto;
    private Button btnSubmit;
    private EditText etCHFID;
    private ImageView iv;
    private ProgressDialog pd;
    private Bitmap theImage;
    private String Path = null;
    private int result = 0;

    private String msg = "";
    private double Longitude, Latitude;
    private LocationManager lm;
    private String towers;
    private ClientAndroidInterface ca;
    private SQLHandler sqlHandler;

    private File tempPhotoFile;
    private Uri tempPhotoUri;

    private Picasso picasso;

    private Target imageTarget = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            theImage = bitmap;
            iv.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.e(LOG_TAG, "Loading acquired photo failed");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acquire_main);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.Acquire));


        global = (Global) getApplicationContext();
        ca = new ClientAndroidInterface(this);

        picasso = new Picasso.Builder(this)
                .listener((picasso, uri, exception) -> Log.e(LOG_TAG, String.format("Image load failed: %s", uri.toString()), exception))
                .loggingEnabled(BuildConfig.LOG)
                .build();

        Path = global.getSubdirectory("Images") + "/";
        tempPhotoFile = new File(Path, "temp.jpg");
        try {
            if (tempPhotoFile.delete()) {
                Log.i(LOG_TAG, "Leftover temp image deleted");
            }
            if (!tempPhotoFile.createNewFile()) {
                Log.w(LOG_TAG, "Temp photo file already exists");
            }
            tempPhotoUri = FileProvider.getUriForFile(this,
                    String.format("%s.fileprovider", BuildConfig.APPLICATION_ID),
                    tempPhotoFile);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Temp photo file creation failed", e);
        }

        etCHFID = findViewById(R.id.etCHFID);
        iv = findViewById(R.id.imageView);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnScan = findViewById(R.id.btnScan);
        btnSubmit = findViewById(R.id.btnSubmit);

        sqlHandler = new SQLHandler(this);

        etCHFID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable InsNo) {
                String path = null;
                if (!InsNo.toString().isEmpty()) {
                    path = ca.GetListOfImagesContain(InsNo.toString());
                }
                if (path != null && !"".equals(path)) {
                    File file = new File(path);
                    picasso.load(file)
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(iv);
                } else {
                    picasso.load(R.drawable.person).into(iv);
                }
            }
        });


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        towers = lm.getBestProvider(c, false);
        if (towers != null) {
            Location loc = null;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            loc = lm.getLastKnownLocation(towers);

            if (loc != null) {
                Longitude = loc.getLongitude();
                Latitude = loc.getLatitude();
            }

        } else {
            Toast.makeText(Acquire.this, "No providers found", Toast.LENGTH_LONG).show();
        }

        iv.setOnClickListener(v -> {
        });

        btnTakePhoto.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(EXTRA_OUTPUT, tempPhotoUri);
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                Log.e(LOG_TAG, "Image capture activity not found", e);
            }
        });

        btnScan.setOnClickListener(v -> {
            try {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, SCAN_QR_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                Log.e(LOG_TAG, "QR Scan activity not found", e);
            }
        });

        btnSubmit.setOnClickListener(v -> {
            Escape escape = new Escape();
            int validInsuranceNumber = escape.CheckInsuranceNumber(etCHFID.getText().toString());
            if (validInsuranceNumber > 0) {
                ca.ShowDialog(getResources().getString(validInsuranceNumber));
                return;
            }

            if (!isValidate()) return;

            pd = ProgressDialog.show(Acquire.this, "", getResources().getString(R.string.Uploading));
            new Thread(() -> {
                try {
                    result = SubmitData();
                } catch (IOException | UserException e) {
                    e.printStackTrace();
                }

                runOnUiThread(() -> {
                    switch (result) {
                        case 1:
                            msg = getResources().getString(R.string.PhotoSaved);
                            break;
                        default:
                            msg = getResources().getString(R.string.CouldNotUpload);
                            break;
                    }

                    Toast.makeText(Acquire.this, getResources().getString(R.string.PhotoSaved), Toast.LENGTH_LONG).show();

                    etCHFID.setText("");
                    iv.setImageResource(R.drawable.person);
                    theImage = null;
                    etCHFID.requestFocus();
                });
                pd.dismiss();
            }).start();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!tempPhotoFile.delete()) {
            Log.w(LOG_TAG, "Temp photo file deletion failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    picasso.invalidate(tempPhotoUri);
                    picasso.load(tempPhotoUri)
                            .resize(global.getIntSetting("image_width_limit", 400),
                                    global.getIntSetting("image_height_limit", 400))
                            .centerInside()
                            .into(imageTarget);
                }
                break;
            case SCAN_QR_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String insureeNumber = data.getStringExtra("SCAN_RESULT");
                    etCHFID.setText(insureeNumber);
                }
                break;
        }
    }

    protected boolean isValidate() {
        if (etCHFID.getText().length() == 0) {
            ShowDialog(getResources().getString(R.string.MissingCHFID), (dialog, which) -> etCHFID.requestFocus());
            return false;
        }

        if (theImage == null) {
            ShowDialog(getResources().getString(R.string.MissingImage), (dialog, which) -> iv.requestFocus());
            return false;
        }

        if (!isValidCHFID()) {
            ShowDialog(getResources().getString(R.string.InvalidInsuranceNumber), (dialog, which) -> etCHFID.requestFocus());
            return false;
        }

        return true;
    }

    private int SubmitData() throws IOException, UserException {
        int Uploaded = 0;
        File myDir = new File(Path);

        //Get current date and format it in yyyyMMdd format
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        String fName = etCHFID.getText() + "_" + global.getOfficerCode() + "_" + d + "_" + Latitude + "_" + Longitude + ".jpg";
        //Create file and delete if exists
        File file = new File(myDir, fName);
        if (file.exists()) file.delete();
        Uploaded = 1;
        FileOutputStream out = new FileOutputStream(file);
        theImage.compress(Bitmap.CompressFormat.JPEG, global.getIntSetting("image_jpeg_quality", 40), out);

        out.flush();
        out.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put("PhotoPath", file.getAbsolutePath());
        String[] whereArgs = {etCHFID.getText().toString()};

        sqlHandler.updateData("tblInsuree", contentValues, "CHFID = ?", whereArgs);

        return Uploaded;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                finish();
                return true;

            case R.id.mnuStatistics:
                Statistics acquire = new Statistics();
                acquire.IsEnrolment = true;
                if (!global.isNetworkAvailable()) {
                    ShowDialog(getResources().getString(R.string.InternetRequired));
                    return false;
                }
                if (global.getOfficerCode().length() == 0) {
                    ShowDialog(getResources().getString(R.string.MissingOfficer));
                    return false;
                }

                Intent Stats = new Intent(Acquire.this, Statistics.class);
                startActivity(Stats);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    protected AlertDialog ShowDialog(String msg) {
        return ShowDialog(msg, (dialog, which) -> {
        });
    }

    protected AlertDialog ShowDialog(String msg, DialogInterface.OnClickListener positiveButtonListener) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", positiveButtonListener).show();
    }

    private boolean isValidCHFID() {
        Escape escape = new Escape();
        int result = escape.CheckInsuranceNumber(etCHFID.getText().toString());

        return (result == 0);
    }
}
