package org.openimis.imispolicies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private ProgressDialog progressDialog;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getResources().getString(R.string.Login));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ca = new ClientAndroidInterface(this);
        progressDialog = new ProgressDialog(this);
        officerCode = ca.getOfficerCode();
        page = getIntent().getIntExtra("Page",0);
        initViews();
        canSave();
        setupListenners();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = txtLoginName.getText().toString();
                String password = txtPassword.getText().toString();

                boolean hasInternet = ca.CheckInternetAvailable();
                if(!hasInternet){
                    ca.ShowDialog(getResources().getString(R.string.NoInternet));
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    boolean loggedIn = ca.LoginJI(username, password);
                    if(loggedIn){
                        if(page == 0){
                            Intent intent = new Intent(LoginActivity.this, SyncActivity.class);
                            startActivity(intent);
                        } else if (page == 1) {
                            Intent intent = new Intent(LoginActivity.this, SearchActivity.class);
                            startActivity(intent);
                        } else if (page == 2) {
                            Intent intent = new Intent(LoginActivity.this, Enrolment.class);
                            startActivity(intent);
                        } else if (page == 4) {
                            ca.launchActivity("Reports");
                        } else if (page == 5) {
                            ca.launchActivity("Enquire");
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        progressBar.setVisibility(View.GONE);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        ca.ShowDialog(getResources().getString(R.string.LoginFail));
                    }
                }

            }
        });
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
        progressBar = findViewById(R.id.loginProgressBar);

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
    }
}