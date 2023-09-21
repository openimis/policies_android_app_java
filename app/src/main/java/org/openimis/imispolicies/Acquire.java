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
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.openimis.imispolicies.tools.ImageManager;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.tools.StorageManager;
import org.openimis.imispolicies.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class Acquire extends AppCompatActivity {
    private static final String LOG_TAG = "ACQUIRE";
    private static final int SCAN_QR_REQUEST_CODE = 0;
    private static final int TAKE_PHOTO_REQUEST_CODE = 1;
    private static final String TEMP_PHOTO_PATH = "images/acquireTemp.jpg";

    private Global global;

    private ImageButton btnScan, btnTakePhoto;
    private Button btnSubmit;
    private EditText etCHFID;
    private ImageView iv;
    private ProgressDialog pd;
    private Bitmap theImage;
    private String Path = null;
    private int result = 0;

    private double Longitude, Latitude;
    private LocationManager lm;
    private String towers;
    private ClientAndroidInterface ca;
    private SQLHandler sqlHandler;
    private Uri tempPhotoUri;

    private Picasso picasso;
    private StorageManager storageManager;

    private final Target imageTarget = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            theImage = bitmap;
            iv.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Exception exception, Drawable errorDrawable) {
            Log.e(LOG_TAG, "Loading acquired photo failed", exception);
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
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.Acquire));
        }

        global = (Global) getApplicationContext();
        ca = new ClientAndroidInterface(this);
        picasso = new Picasso.Builder(this).build();
        storageManager = StorageManager.of(this);
        sqlHandler = new SQLHandler(this);

        etCHFID = findViewById(R.id.etCHFID);
        iv = findViewById(R.id.imageView);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnScan = findViewById(R.id.btnScan);
        btnSubmit = findViewById(R.id.btnSubmit);

        File tempPhotoFile = FileUtils.createTempFile(this, TEMP_PHOTO_PATH);
        if (tempPhotoFile != null) {
            tempPhotoUri = FileProvider.getUriForFile(this,
                    String.format("%s.fileprovider", BuildConfig.APPLICATION_ID),
                    tempPhotoFile);
            if (tempPhotoUri == null) {
                Log.w(LOG_TAG, "Failed to create temp photo URI");
            }
        }

        etCHFID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                File photoFile = null;
                String insureeNumber = text.toString();
                if (!insureeNumber.isEmpty()) {
                    photoFile = ImageManager.of(Acquire.this).getNewestInsureeImage(insureeNumber);
                }
                if (photoFile != null) {
                    picasso.load(photoFile)
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
            Location loc;
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
            Log.w(LOG_TAG, "No location providers found");
            Toast.makeText(Acquire.this, "No location providers found", Toast.LENGTH_LONG).show();
        }

        iv.setOnClickListener(v -> {
        });

        btnTakePhoto.setOnClickListener(v -> {
            if (!etCHFID.getText().toString().isEmpty()) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);
                    global.grantUriPermissions(this, tempPhotoUri, intent, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Log.e(LOG_TAG, "Image capture activity not found", e);
                }
            } else {
                Toast.makeText(this, R.string.MissingCHFID, Toast.LENGTH_LONG).show();
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
        FileUtils.removeTempFile(this, TEMP_PHOTO_PATH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    picasso.invalidate(tempPhotoUri);
                    picasso.load(tempPhotoUri)
                            .resize(global.getIntKey("image_width_limit", 400),
                                    global.getIntKey("image_height_limit", 400))
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
        String date = AppInformation.DateTimeInfo.getDefaultFileDatetimeFormatter().format(new Date());
        String fName = etCHFID.getText() + "_" + global.getOfficerCode() + "_" + date + "_" + Latitude + "_" + Longitude + ".jpg";

        File[] oldInsureeImages = ImageManager.of(this).getInsureeImages(etCHFID.getText().toString());

        File file = new File(global.getSubdirectory("Images"), fName);
        if (file.exists()) {
            Log.w(LOG_TAG, String.format("File already exists: %s", file.getAbsolutePath()));
        }

        FileOutputStream out = new FileOutputStream(file);
        theImage.compress(Bitmap.CompressFormat.JPEG, global.getIntKey("image_jpeg_quality", 40), out);
        out.close();

        if (file.length() == 0L) {
            Log.w(LOG_TAG, "Compressing photo failed, the resulting file has no content");
            if (!file.delete()) {
                Log.w(LOG_TAG, "Deleting empty output file failed");
            }
            return 0;
        } else {
            FileUtils.deleteFiles(oldInsureeImages);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("PhotoPath", file.getAbsolutePath());
        String[] whereArgs = {etCHFID.getText().toString()};

        if (sqlHandler.updateData("tblInsuree", contentValues, "CHFID = ?", whereArgs, false) == 0) {
            Log.w(LOG_TAG, String.format("Cannot update photo path. No insuree for CHFID: %s", etCHFID.getText().toString()));
        }

        return 1;
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
                if (!global.isNetworkAvailable()) {
                    ShowDialog(getResources().getString(R.string.InternetRequired));
                    return false;
                }
                if (global.getOfficerCode().length() == 0) {
                    ShowDialog(getResources().getString(R.string.MissingOfficer));
                    return false;
                }

                startActivity(Statistics.newIntent(this, null, Statistics.Type.ENROLMENT));
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
