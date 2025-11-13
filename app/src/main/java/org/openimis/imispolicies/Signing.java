package org.openimis.imispolicies;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.usecase.Login;

public class Signing extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private Button btnLogin;
    private TextView tvError;
    protected ProgressDialog progressDialog;
    private CircularProgressIndicator cp;
    private int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signing);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.Login));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        page = intent.getIntExtra("page", 0);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnSignIn);
        tvError = findViewById(R.id.tvLoginError);
        cp = findViewById(R.id.cp_load_login);
        tvError.setEnabled(false);
        cp.setIndeterminate(false);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFormValid()){
                    etLogin.setBackground(getDrawable(R.drawable.edit_text));
                    etPassword.setBackground(getDrawable(R.drawable.edit_text));
                    tvError.setText("");
                    String username = etLogin.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    cp.setIndeterminate(true);
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            try{
                                new Login().execute(username, password);
                                runOnUiThread(() -> {
                                    MainActivity.SetLoggedIn();
                                    if(page == 5){
                                        Intent intent = new Intent(Signing.this , Enquire.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        finish();
                                    }
                                });
                            } catch (Exception e){
                                e.printStackTrace();
                                runOnUiThread(()->{
                                    cp.setIndeterminate(false);
                                    tvError.setText(getResources().getString(R.string.LoginFail));
                                });
                            }
                        }
                    };
                    thread.start();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean isFormValid(){
        if(etLogin.getText().toString().trim().isEmpty() || etPassword.getText().toString().trim().isEmpty()){
            tvError.setText(getResources().getString(R.string.fieldRequired));
            if(etLogin.getText().toString().trim().isEmpty()){
                etLogin.setBackground(getDrawable(R.drawable.edit_text_error));
            }else{
                etLogin.setBackground(getDrawable(R.drawable.edit_text));
            }
            if(etPassword.getText().toString().trim().isEmpty()) {
                etPassword.setBackground(getDrawable(R.drawable.edit_text_error));
            }else{
                etPassword.setBackground(getDrawable(R.drawable.edit_text));
            }
            return false;
        }
        return true;
    }
}