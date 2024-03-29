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
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.client.android.Intents;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.network.exception.UserNotAuthenticatedException;
import org.openimis.imispolicies.tools.LanguageManager;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.util.AndroidUtils;
import org.openimis.imispolicies.util.StringUtils;
import org.openimis.imispolicies.util.UriUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String LOG_TAG = "MAIN_ACTIVITY";
    private static final int REQUEST_PERMISSIONS_CODE = 1;
    private static final int REQUEST_PICK_MD_FILE = 2;
    public static final int REQUEST_CREATE_ENROL_EXPORT = 3;
    public static final int REQUEST_CREATE_FEEDBACK_EXPORT = 4;
    public static final int REQUEST_CREATE_RENEWAL_EXPORT = 5;
    private NavigationView navigationView;

    // This is super ugly but I'm not fixing the app.
    // This is less ugly than what was there before.
    @SuppressLint("StaticFieldLeak")
    @Nullable
    private static MainActivity instance;

    private WebView wv;
    static Global global;
    private static final int MENU_LANGUAGE_1 = Menu.FIRST;
    private static final int MENU_LANGUAGE_2 = Menu.FIRST + 1;
    private static final int MENU_LANGUAGE_3 = Menu.FIRST + 2;
    private String Language1 = "";
    private String Language2 = "";
    private String LanguageCode1 = "";
    private String LanguageCode2 = "";
    private String selectedLanguage;
    public String ImagePath;
    public String InsureeNumber;
    TextView Login;
    TextView OfficerName;
    ClientAndroidInterface ca;
    String aBuffer = "";
    String calledFrom = "java";
    public File f;
    public String etRarPassword = "";
    private AlertDialog enrolmentOfficerDialog;
    private AlertDialog masterDataDialog;
    private AlertDialog permissionDialog;

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
            wv.evaluateJavascript(String.format("selectImageCallback(\"%s\");", selectedImage), null);
        } else if (requestCode == ClientAndroidInterface.RESULT_SCAN && resultCode == RESULT_OK && data != null) {
            String insureeNumber = data.getStringExtra(Intents.Scan.RESULT);
            if (!StringUtils.isEmpty(insureeNumber)) {
                wv.evaluateJavascript(String.format("scanQrCallback(\"%s\");", insureeNumber), null);
            }
        } else if (requestCode == REQUEST_PICK_MD_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        byte[] bytes = IOUtils.toByteArray(getContentResolver().openInputStream(uri));
                        String path = global.getSubdirectory("Database");
                        f = new File(path, "MasterData.rar");
                        if (f.exists() || f.createNewFile())
                            new FileOutputStream(f).write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ShowDialogTex2();
                }
            } else {
                finish();
            }
        } else if (requestCode == REQUEST_CREATE_ENROL_EXPORT && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();

            File exportFile = ca.zipEnrolmentFiles();
            if (exportFile != null) {
                ca.clearDirectory("Family");
                ca.clearDirectory("Images");

                UriUtils.copyFileToUri(this, exportFile, fileUri);
                if (!exportFile.delete()) {
                    Log.w("EXPORT", "Deleting enrol export cache failed");
                }
                AndroidUtils.showToast(this, R.string.XmlCreated);
            }
        } else if (requestCode == REQUEST_CREATE_RENEWAL_EXPORT && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();

            File exportFile = ca.zipRenewalFiles();
            if (exportFile != null) {
                ca.clearDirectory("Renewal");

                UriUtils.copyFileToUri(this, exportFile, fileUri);
                if (!exportFile.delete()) {
                    Log.w("EXPORT", "Deleting renewal export cache failed");
                }
                AndroidUtils.showToast(this, R.string.XmlCreated);
            }
        } else if (requestCode == REQUEST_CREATE_FEEDBACK_EXPORT && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();

            File exportFile = ca.zipFeedbackFiles();
            if (exportFile != null) {
                ca.clearDirectory("Feedback");

                UriUtils.copyFileToUri(this, exportFile, fileUri);
                if (!exportFile.delete()) {
                    Log.w("EXPORT", "Deleting feedback export cache failed");
                }
                AndroidUtils.showToast(this, R.string.XmlCreated);
            }

        } else {
            //if user cancels
            ClientAndroidInterface.inProgress = false;
            this.InsureeNumber = null;
            this.ImagePath = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkRequirements()) {
            onAllRequirementsMet();
        }
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        global = (Global) getApplicationContext();
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        SQLHandler sqlHandler = new SQLHandler(this);
        sqlHandler.isPrivate = true;
        //Set the Image folder path
        global.setImageFolder(global.getSubdirectory("Images"));
        //Check if database exists
        File database = global.getDatabasePath(SQLHandler.DBNAME);
        if (!database.exists()) {
            sqlHandler.getReadableDatabase();
            if (copyDatabase(this)) {
                Toast.makeText(this, "Copy database success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Copy database failed", Toast.LENGTH_SHORT).show();
                return;
            }
        } else
            sqlHandler.getReadableDatabase();

        //Create image folder
        createImageFolder();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        wv = findViewById(R.id.webview);
        WebSettings settings = wv.getSettings();
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        settings.setJavaScriptEnabled(true);
        //noinspection deprecation
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setDomStorageEnabled(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setUseWideViewPort(true);
        settings.setSaveFormData(true);
        settings.setAllowFileAccess(true);
        //noinspection deprecation
        settings.setEnableSmoothTransition(true);
        settings.setLoadWithOverviewMode(true);
        wv.addJavascriptInterface(new ClientAndroidInterface(this), "Android");

        //Register for context acquire_menu
        registerForContextMenu(wv);

        wv.loadUrl("file:///android_asset/pages/Home.html");
        wv.setWebViewClient(new MyWebViewClient(MainActivity.this));

        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                //noinspection ConstantConditions
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
                getSupportActionBar().setSubtitle(title);
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerview = navigationView.getHeaderView(0);
        Login = headerview.findViewById(R.id.tvLogin);
        OfficerName = headerview.findViewById(R.id.tvOfficerName);

        Login.setOnClickListener(v -> {
            wv.loadUrl("file:///android_asset/pages/Login.html?s=3");
            drawer.closeDrawer(GravityCompat.START);
            SetLoggedIn();
        });
        ca = new ClientAndroidInterface(this);
        if (ca.isMasterDataAvailable() > 0) {
            loadLanguages();
        }


        navigationView.setCheckedItem(R.id.nav_home);

        if (checkRequirements()) {
            onAllRequirementsMet();
        }

        setVisibilityOfPaymentMenu();
    }

    private void setVisibilityOfPaymentMenu() {
        navigationView = findViewById(R.id.nav_view);
        MenuItem navPayment = navigationView.getMenu().findItem(R.id.nav_payment);
        navPayment.setVisible(AppInformation.MenuInfo.getShowPaymentNumberMenu()
                || AppInformation.MenuInfo.getShowBulkCNMenu());
    }

    @Override
    protected void onResume() {
        super.onResume();
        OfficerName.setText(global.getOfficerName());
    }

    public static void SetLoggedIn() {
        MainActivity activity = instance;
        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.runOnUiThread(() -> {
            if (global.isLoggedIn()) {
                activity.Login.setText(R.string.Logout);
            } else {
                activity.Login.setText(R.string.Login);
            }
        });
    }

    private void loadLanguages() {
        ClientAndroidInterface ca = new ClientAndroidInterface(this);
        JSONArray Languages = ca.getLanguage();
        JSONObject LanguageObject = null;

        try {
            LanguageObject = Languages.getJSONObject(0);
            Language1 = (LanguageObject.getString("LanguageName"));
            LanguageCode1 = (LanguageObject.getString("LanguageCode"));
            if (Languages.length() > 1) {
                LanguageObject = Languages.getJSONObject(1);
                Language2 = (LanguageObject.getString("LanguageName"));
                LanguageCode2 = (LanguageObject.getString("LanguageCode"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void PickMasterDataFileDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getResources().getString(R.string.NoInternetTitle))
                .setMessage(getResources().getString(R.string.DoImport))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Yes),
                        (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            try {
                                startActivityForResult(intent, REQUEST_PICK_MD_FILE);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.NoFileExporerInstalled), Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(getResources().getString(R.string.No),
                        (dialog, id) -> {
                            dialog.cancel();
                            finish();
                        }).show();
    }

    public void PickMasterDataFileDialogFromPage() {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                MainActivity.this);

        alertDialog2.setTitle(getResources().getString(R.string.NoInternetTitle));
        alertDialog2.setMessage(getResources().getString(R.string.DoImport));

        alertDialog2.setPositiveButton(getResources().getString(R.string.Yes),
                (dialog, which) -> {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    try {
                        startActivityForResult(intent, REQUEST_PICK_MD_FILE);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.NoFileExporerInstalled), Toast.LENGTH_SHORT).show();
                    }
                    // Write your code here to execute after dialog
                }).setNegativeButton(getResources().getString(R.string.No),
                (dialog, id) -> dialog.cancel()).show();
    }

    public void ConfirmMasterDataDialog(String filename) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                MainActivity.this);

        alertDialog2.setTitle(getResources().getString(R.string.LoadFile));
        alertDialog2.setMessage(filename);
        alertDialog2.setCancelable(false);

        alertDialog2.setPositiveButton(getResources().getString(R.string.Ok),
                (dialog, which) -> {
                    new MasterDataLocalAsync(this).execute(aBuffer);
                }).setNegativeButton(getResources().getString(R.string.Quit),
                (dialog, id) -> {
                    dialog.cancel();
                    finish();
                }).show();
    }

    public void ConfirmDialogPage(String filename) {
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                MainActivity.this);

        alertDialog2.setTitle(getResources().getString(R.string.LoadFile));
        alertDialog2.setMessage(filename);
        alertDialog2.setCancelable(false);

        alertDialog2.setPositiveButton(getResources().getString(R.string.Ok),
                (dialog, which) -> {
                    new MasterDataLocalAsync(this).execute(aBuffer);
                }).setNegativeButton(getResources().getString(R.string.Quit),
                (dialog, id) -> dialog.cancel()).show();
    }

    public void ShowEnrolmentOfficerDialog() {
        final ClientAndroidInterface ca = new ClientAndroidInterface(this);
        final int MasterData = ca.isMasterDataAvailable();

        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View promptsView = li.inflate(R.layout.dialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView.findViewById(R.id.txtOfficerCode);
        final TextView tVDialogTile = promptsView.findViewById(R.id.tvDialogTitle);

        if (MasterData == 0) {
            tVDialogTile.setText(getResources().getString(R.string.MasterDataNotFound));
            userInput.setVisibility(View.GONE);
        }

        int positiveButton, negativeButton;
        if (MasterData > 0) {
            positiveButton = R.string.Ok;
            negativeButton = R.string.Cancel;

        } else {
            positiveButton = R.string.Yes;
            negativeButton = R.string.No;
        }

        enrolmentOfficerDialog = alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(positiveButton),
                        (dialog, id) -> {
                            try {
                                if (MasterData > 0) {
                                    if (ca.isOfficerCodeValid(userInput.getText().toString())) {
                                        global.setOfficerCode(userInput.getText().toString());
                                        OfficerName.setText(global.getOfficerName());
                                        SetLoggedIn();
                                        // Officer villages are currently turned off
//                                            if(_General.isNetworkAvailable(MainActivity.this)){
//                                                ca.getOfficerVillages(userInput.getText().toString());
//                                            }
                                    } else {
                                        ShowEnrolmentOfficerDialog();
                                        ca.ShowDialog(getResources().getString(R.string.IncorrectOfficerCode));
                                    }
                                } else {
                                    if (!global.isNetworkAvailable()) {
                                        PickMasterDataFileDialog();
                                    } else {
                                        new MasterDataAsync(this).execute();
                                    }
                                    //ca.downloadMasterData();
                                    //ShowDialogTex();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        })
                .setNegativeButton(getResources().getString(negativeButton),
                        (dialog, id) -> {
                            dialog.cancel();
                            finish();
                        }).show();
    }

    public void ShowMasterDataDialog() {
        masterDataDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.MasterData)
                .setMessage(R.string.MasterDataNotFound)
                .setCancelable(false)
                .setPositiveButton(R.string.Yes,
                        (dialog, id) -> {
                            if (!global.isNetworkAvailable()) {
                                PickMasterDataFileDialog();
                            } else {
                                new MasterDataAsync(this).execute();
                            }
                        })
                .setNegativeButton(R.string.ForceClose,
                        (dialog, id) -> {
                            dialog.cancel();
                            finish();
                        })
                .show();
    }


    public void ShowDialogTex2() {

        final ClientAndroidInterface ca = new ClientAndroidInterface(this);
        LayoutInflater li = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View promptsView = li.inflate(R.layout.rar_pass_dialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView.findViewById(R.id.etRarPass);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.Yes),
                        (dialog, id) -> {
                            try {
                                etRarPassword = userInput.getText().toString();
                                getMasterDataText2(f.getPath(), etRarPassword);

                                if (calledFrom == "java") {
                                    ConfirmMasterDataDialog((f.getName()));
                                } else {
                                    ConfirmDialogPage((f.getName()));
                                }
                            } catch (Exception e) {
                                e.getMessage();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.No),
                        (dialog, id) -> {
                            dialog.cancel();
                            finish();
                        });

         alertDialogBuilder.show();
    }

    public String getMasterDataText2(String fileName, String password) {
        ca.unZipWithPassword(fileName, password);
        String fname = "MasterData.txt";
        try {
            String dir = global.getSubdirectory("Database");
            File myFile = new File(dir, fname);//"/"+dir+"/MasterData.txt"
//            BufferedReader myReader = new BufferedReader(
//                    new InputStreamReader(
//                            new FileInputStream(myFile), "UTF32"));
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            aBuffer = myReader.readLine();

            myReader.close();
/*            Scanner in = new Scanner(new FileReader("/"+dir+"/MasterData.txt"));
            StringBuilder sb = new StringBuilder();
            while(in.hasNext()) {
                sb.append(in.next());
            }
            in.close();
            aBuffer = sb.toString();*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }

    private boolean copyDatabase(Context context) {
        try {
            InputStream inputStream = getApplicationContext().getAssets().open("database/" + SQLHandler.DBNAME);
            String outFileName = getApplicationContext().getApplicationInfo().dataDir + "/databases/" + SQLHandler.DBNAME;
            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();

            Log.w(LOG_TAG, "DB Copied");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createImageFolder() {
        File imageFolder = new File(global.getImageFolder());
        if (!imageFolder.exists())
            imageFolder.mkdir();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the acquire_menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.acquire_menu.language, acquire_menu);
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_LANGUAGE_1, 0, Language1);
        if (!Language2.equals("")) {
            menu.add(0, MENU_LANGUAGE_2, 0, Language2);
        }
        menu.add(0, MENU_LANGUAGE_3, 0, getResources().getString(R.string.LanguageSettings));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_LANGUAGE_1:
                selectedLanguage = LanguageCode1;
                new LanguageManager(this).setLanguage(selectedLanguage);
                return true;
            case MENU_LANGUAGE_2:
                selectedLanguage = LanguageCode2;
                new LanguageManager(this).setLanguage(selectedLanguage);
                return true;
            case MENU_LANGUAGE_3:
                new LanguageManager(this).openLanguageSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            wv.loadUrl("file:///android_asset/pages/Home.html");
        } else if (id == R.id.nav_acquire) {
            Intent intent = new Intent(this, Acquire.class);
            startActivity(intent);
        } else if (id == R.id.nav_enrolment) {
            wv.loadUrl("file:///android_asset/pages/Enrollment.html");
        } else if (id == R.id.nav_modify_family) {
            global = (Global) getApplicationContext();
            if (global.isLoggedIn()) {
                wv.loadUrl("file:///android_asset/pages/Search.html");
            } else {
                wv.loadUrl("file:///android_asset/pages/Login.html?s=1");
            }

        } else if (id == R.id.nav_renewal) {
            Intent i = new Intent(this, RenewList.class);
            startActivity(i);

        } else if (id == R.id.nav_reports) {
            Global global = (Global) getApplicationContext();
            if (global.isLoggedIn()) {
                Intent i = new Intent(this, Reports.class);
                startActivity(i);
            } else {
                wv.loadUrl("file:///android_asset/pages/Login.html?s=4");
            }
        } else if (id == R.id.nav_feedback) {
            Intent intent = new Intent(this, FeedbackList.class);
            startActivity(intent);
        } else if (id == R.id.nav_sync) {
            wv.loadUrl("file:///android_asset/pages/Sync.html");
        } else if (id == R.id.nav_about) {
            wv.loadUrl("file:///android_asset/pages/About.html");
        } else if (id == R.id.nav_settings) {
            wv.loadUrl("file:///android_asset/pages/Settings.html");
        } else if (id == R.id.nav_quit) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.QuitAppQuestion))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.Yes), (dialogInterface, i) -> {
                        finish();
                        System.exit(0);
                    })
                    .setNegativeButton(getResources().getString(R.string.No), (dialogInterface, i) -> {
                    }).create().show();
        } else if (id == R.id.nav_enquire) {
            global = (Global) getApplicationContext();
            if (global.isLoggedIn()) {
                Intent intent = new Intent(this, Enquire.class);
                startActivity(intent);
            } else {
                wv.loadUrl("file:///android_asset/pages/Login.html?s=5");
            }
        } else if (id == R.id.nav_payment) {
            ClientAndroidInterface ca = new ClientAndroidInterface(this);
            ca.launchPayment();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (wv.canGoBack()) {
                        if (global.getCurrentUrl() != null)
                            wv.loadUrl("file:///android_asset/pages/" + global.getCurrentUrl());
                        else
                            wv.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class MasterDataAsync extends AsyncTask<Void, Void, Throwable> {
        private final WeakReference<MainActivity> activity;
        private WeakReference<ProgressDialog> pd = null;

        public MasterDataAsync(@NonNull MainActivity context) {
            this.activity = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            Context context = activity.get();
            if (context == null) {
                return;
            }
            pd = new WeakReference<>(AndroidUtils.showProgressDialog(context, R.string.Sync, R.string.DownloadingMasterData));
        }

        @Override
        protected Throwable doInBackground(Void... voids) {
            try {
                Activity context = activity.get();
                if (context == null) {
                    return null;
                }
                new ClientAndroidInterface(context).startDownloadingMasterData();
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }

            return null;
        }


        @Override
        protected void onPostExecute(Throwable exception) {
            ProgressDialog progress = pd.get();
            if (progress != null) {
                progress.dismiss();
            }
            final Activity context = activity.get();
            if (context == null) {
                return;
            }
            if (exception instanceof UserNotAuthenticatedException) {
                new ClientAndroidInterface(context).forceLoginDialogBox(() -> restart(context));
                return;
            }
            restart(context);
        }

        private void restart(@NonNull Activity activity) {
            Intent refresh = new Intent(activity, MainActivity.class);
            activity.startActivity(refresh);
            activity.finish();
        }
    }

    public static class MasterDataLocalAsync extends AsyncTask<String, Void, Void> {

        private final WeakReference<MainActivity> activity;
        private WeakReference<ProgressDialog> pd;

        public MasterDataLocalAsync(@NonNull MainActivity context) {
            this.activity = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            Context context = activity.get();
            if (context == null) {
                return;
            }
            pd = new WeakReference<>(AndroidUtils.showProgressDialog(context, R.string.Sync, R.string.DownloadingMasterData));
        }

        @Override
        protected Void doInBackground(String... buffers) {
            Activity context = activity.get();
            if (context == null) {
                return null;
            }
            ClientAndroidInterface ca = new ClientAndroidInterface(context);
            try {
                ca.importMasterData(buffers[0]);
            } catch (JSONException | UserException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ProgressDialog progress = pd.get();
            if (progress != null) {
                progress.dismiss();
            }
            Activity context = activity.get();
            if (context == null) {
                return;
            }
            Intent refresh = new Intent(context, MainActivity.class);
            context.startActivity(refresh);
            context.finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                        && !permission.equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                    // MANAGE_EXTERNAL_STORAGE always report as denied by design
                    return false;
                }
            }
        }
        return true;
    }

    public void PermissionsDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.Permissions)
                .setMessage(getResources().getString(R.string.PermissionsInfo, getResources().getString(R.string.app_name_policies)))
                .setCancelable(false)
                .setPositiveButton(R.string.Ok,
                        (dialog, id) -> ActivityCompat.requestPermissions(this, global.getPermissions(), REQUEST_PERMISSIONS_CODE))
                .setNegativeButton(R.string.ForceClose,
                        (dialog, id) -> {
                            dialog.cancel();
                            finish();
                        });

        permissionDialog = alertDialogBuilder.show();
    }

    public boolean checkRequirements() {
        if (!hasPermissions(this, global.getPermissions())) {
            PermissionsDialog();
            return false;
        }

        if (ca.isMasterDataAvailable() < 1) {
            ShowMasterDataDialog();
            return false;
        }

        return true;
    }

    public void onAllRequirementsMet() {
        if (TextUtils.isEmpty(global.getOfficerCode())) {
            ShowEnrolmentOfficerDialog();
        }
        global.isSDCardAvailable();
        new LanguageManager(this).checkSystemLanguage();
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    @Override
    public void recreate() {
        for (AlertDialog dialog : new AlertDialog[]{masterDataDialog, enrolmentOfficerDialog, permissionDialog}) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        super.recreate();
    }

    @Override
    protected void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
