package org.openimis.imispolicies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.openimis.imispolicies.util.AndroidUtils;


public class LoginActivity extends AppCompatActivity {

    TextInputLayout layoutLoginName, layoutPassword;
    MaterialButton btnLogin;
    TextInputEditText txtLoginName, txtPassword;
    String officerCode;
    ClientAndroidInterface ca;
    int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getResources().getString(R.string.Login));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ca = new ClientAndroidInterface(this);
        officerCode = ca.getOfficerCode();
        page = getIntent().getIntExtra("Page",0);
        initViews();
        canSave();
        setupListenners();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews(){
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutLoginName = findViewById(R.id.layoutLoginName);
        btnLogin = findViewById(R.id.btnLogin);
        txtLoginName = findViewById(R.id.txtLoginName);
        txtPassword = findViewById(R.id.txtPassword);

        txtLoginName.setText(officerCode);
    }

    private void canSave(){
        if(txtLoginName.getText().toString().isEmpty() ||
                txtPassword.getText().toString().isEmpty()
        ){
            btnLogin.setEnabled(false);
        } else {
            btnLogin.setEnabled(true);
        }
    }

    private void setupListenners(){
        txtLoginName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                canSave();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                canSave();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnLogin.setOnClickListener(v ->  {
            boolean hasInternet = ca.CheckInternetAvailable();
            if(!hasInternet){
                AndroidUtils.showDialog(LoginActivity.this, getResources().getString(R.string.NoInternet));
            } else {
                try {
                    String username = txtLoginName.getText().toString();
                    String password = txtPassword.getText().toString();
                    ProgressBar loginProgressBar = findViewById(R.id.loginProgressBar);
                    loginProgressBar.setVisibility(View.VISIBLE);
                    new Thread(() ->{
                        boolean loggedIn = ca.LoginJI(username, password);
                        runOnUiThread(() -> {
                            if (loggedIn) {
                                if (page == 0) {
                                    finish();
                                } else if (page == 1) {
                                    loginProgressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(LoginActivity.this, SearchActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else if (page == 2) {
                                    loginProgressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(LoginActivity.this, Enrolment.class);
                                    startActivity(intent);
                                    finish();
                                } else if (page == 4) {
                                    loginProgressBar.setVisibility(View.GONE);
                                    ca.launchActivity("Reports");
                                    finish();
                                } else if (page == 5) {
                                    loginProgressBar.setVisibility(View.GONE);
                                    ca.launchActivity("Enquire");
                                    finish();
                                } else {
                                    loginProgressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                loginProgressBar.setVisibility(View.GONE);
                                AndroidUtils.showDialog(LoginActivity.this, getResources().getString(R.string.LoginFail));
                            }
                        });
                    }).start();


                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}