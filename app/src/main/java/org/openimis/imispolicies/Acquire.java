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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.exact.CallSoap.CallSoap;
import com.exact.general.General;
import com.exact.uploadfile.UploadFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.openimis.imispolicies.R;

public class Acquire extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */


    General _General = new General(AppInformation.DomainInfo.getDomain());

    ImageButton btnScan, btnTakePhoto;
    Button btnSubmit;
    EditText etCHFID;
    ImageView iv;
    ProgressDialog pd;
    Global global;
    Bitmap theImage;
    String Path = null;
    int result = 0;
    String aBuffer = "";


    String msg = "";
    File[] Images;
    int TotalImages;
    int UploadCounter;
    double Longitude, Latitude;
    LocationManager lm;
    String towers;
    ClientAndroidInterface ca;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.acquire_main);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.Acquire));


        global = (Global) getApplicationContext();
        ca = new ClientAndroidInterface(this);

        Path = getApplicationContext().getApplicationInfo().dataDir + "/Images/";
        etCHFID = (EditText) findViewById(R.id.etCHFID);
        iv = (ImageView) findViewById(R.id.imageView);
        btnTakePhoto = (ImageButton) findViewById(R.id.btnTakePhoto);
        btnScan = (ImageButton) findViewById(R.id.btnScan);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        etCHFID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable InsNo) {
                if (!InsNo.toString().isEmpty()) {
                    String files = ca.GetListOfImagesContain(InsNo.toString());
                    if (files.trim() != "" && InsNo.length() > 0) {
                        File imgFile = new File(files);
                        Bitmap myBitmap;
                        if (imgFile.exists()) {
                            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            iv.setImageBitmap(myBitmap);
                        }
                        else {

                            iv.setImageResource(R.drawable.person);
                        }

                    }
                    else {

                        iv.setImageResource(R.drawable.person);
                    }
                }
                else {

                    iv.setImageResource(R.drawable.person);
                }
            }
        });


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        towers = lm.getBestProvider(c, false);
        if(towers != null){
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

        }else{
            Toast.makeText(Acquire.this, "No providers found", Toast.LENGTH_LONG).show();
        }

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!etCHFID.getText().toString().isEmpty()) {
//                    String files = ca.GetListOfImagesContain(etCHFID.getText().toString());
//                    if (files.trim() != "" && etCHFID.getText().length() > 0) {
//                        File imgFile = new File(files);
//
//                        Bitmap myBitmap;
//                        if (imgFile.exists()) {
//                            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                            iv.setImageBitmap(myBitmap);
//                        }
//
//                    } else {
//
//                        iv.setImageResource(R.drawable.person);
//                    }
//
//                }
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try{
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 1);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Escape escape = new Escape();
                int validInsuranceNumber = escape.CheckInsuranceNumber(etCHFID.getText().toString());
                if (validInsuranceNumber > 0){
                    ca.ShowDialog(getResources().getString(validInsuranceNumber));
                    return;
                }

                if (!isValidate()) return;

                pd = ProgressDialog.show(Acquire.this, "", getResources().getString(R.string.Uploading));
                new Thread() {
                    public void run() {
                        try {
                            result = SubmitData();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                switch (result) {
                                    case 1:
                                        msg = getResources().getString(R.string.PhotoSaved);
                                        break;
                                    default:
                                        msg = getResources().getString(R.string.CouldNotUpload);
                                        break;
                                }

                                Toast.makeText(Acquire.this, msg, Toast.LENGTH_LONG).show();

                                etCHFID.setText("");
                                iv.setImageResource(R.drawable.person);
                                theImage = null;
                                etCHFID.requestFocus();

                            }
                        });

                        pd.dismiss();
                    }
                }.start();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case 0:
                    theImage = (Bitmap) data.getExtras().get("data");
                    iv.setImageBitmap(theImage);

                    break;
                case 1:
                    if (resultCode == RESULT_OK) {
                        String CHFID = data.getStringExtra("SCAN_RESULT");
                        etCHFID.setText(CHFID);
                    }

                    break;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    protected boolean isValidate() {


        if (etCHFID.getText().length() == 0) {
            ShowDialog(etCHFID, getResources().getString(R.string.MissingCHFID));
            return false;
        }
        //Toast.makeText(this, theImage.getWidth(), Toast.LENGTH_LONG).show();
        if (theImage == null) {
            ShowDialog(iv, getResources().getString(R.string.MissingImage));
            return false;

        }

        if (!isValidCHFID()) {
            ShowDialog(etCHFID, getResources().getString(R.string.InvalidInsuranceNumber));
            return false;
        }

        return true;
    }

    protected AlertDialog ShowDialog(final TextView tv, String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tv.requestFocus();
                    }
                }).show();
    }

    protected AlertDialog ShowDialog(final ImageView tv, String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tv.requestFocus();
                    }
                }).show();
    }

    private int SubmitData() throws IOException {
        int Uploaded = 0;
        //Create folder if folder is not exists
        File myDir = new File(Path);
        myDir.mkdir();

        //Get current date and format it in yyyyMMdd format
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        String fName = etCHFID.getText() + "_" + global.getOfficerCode() + "_" + d + "_" + Double.toString(Latitude) + "_" + Double.toString(Longitude) + ".jpg";
        //Create file and delete if exists
        File file = new File(myDir, fName);
        if (file.exists()) file.delete();
        Uploaded = 1;
        FileOutputStream out = new FileOutputStream(file);
        theImage.compress(Bitmap.CompressFormat.JPEG, 90, out);

        out.flush();
        out.close();

        return Uploaded;
//        }

    }

    //create acquire_menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.mnuStatistics:
                Statistics acquire = new Statistics();
                acquire.IsEnrolment = true;
                if (!_General.isNetworkAvailable(Acquire.this)) {
                    ShowDialog(getResources().getString(R.string.InternetRequired));
                    return false;
                }
                if (global.getOfficerCode().toString().length() == 0) {
                    ShowDialog(getResources().getString(R.string.MissingOfficer));
                    return false;
                }

                Intent Stats = new Intent(Acquire.this, Statistics.class);
//                Stats.putExtra("Title", getResources().getString(R.string.Statistics));
//                Stats.putExtra("OfficerCode", global.getOfficerCode().toString());
                 startActivity(Stats);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    protected AlertDialog ShowDialog(String msg) {
        return new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //et.requestFocus();
                        return;
                    }
                }).show();

    }

    private boolean ConnectsFTP() {
        UploadFile uf = new UploadFile();
        return uf.isValidFTPCredentials();

    }

    private void UploadAllImages() {
        for (int i = 0; i < Images.length; i++) {
            UploadCounter = i + 1;
            runOnUiThread(ChangeMessage);
            UploadFile uf = new UploadFile();
            if (uf.uploadFileToServer(Acquire.this, Images[i], "Acquire")) {
                RegisterUploadDetails(Images[i].getName());
                Images[i].delete();
            }
        }
    }

    Runnable ChangeMessage = new Runnable() {

        @Override
        public void run() {
            //Change progress dialog message here
            pd.setMessage(UploadCounter + " " + getResources().getString(R.string.Of) + " " + TotalImages + " " + getResources().getString(R.string.Uploading));

        }
    };

    private void RegisterUploadDetails(String ImageName) {
        String[] FileName = ImageName.split("_");
        String CHFID, OfficerCode;


        if (FileName.length > 0) {
            CHFID = FileName[0];
            OfficerCode = FileName[1];

            CallSoap cs = new CallSoap();
            cs.setFunctionName("InsertPhotoEntry");
            cs.InsertPhotoEntry(CHFID, OfficerCode, ImageName);
        }
    }

    private boolean isValidCHFID() {
//	if (etCHFID.getText().toString().length() != 9) return false;
//	String chfid;
//	int Part1, Part2;
//	Part1 = Integer.parseInt(etCHFID.getText().toString())/10;
//	Part2 = Part1 % 7;
//
//	chfid = etCHFID.getText().toString().substring(0, 8) + Integer.toString(Part2);
//	return etCHFID.getText().toString().equals(chfid);\
        Escape escape = new Escape();
        int result = escape.CheckInsuranceNumber(etCHFID.getText().toString());

        return  (result == 0);

    }


}
