package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.openimis.imispolicies.util.AndroidUtils;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText txtSearchInsuranceNumber;
    MaterialButton btnSearch;
    ClientAndroidInterface ca;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle(R.string.Search);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ca = new ClientAndroidInterface(this);
        initViews();
        canSearch();
        txtSearchInsuranceNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                canSearch();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hasInternet = ca.CheckInternetAvailable();
                if(!hasInternet){
                    AndroidUtils.showDialog(SearchActivity.this, getResources().getString(R.string.NoInternet));
                } else {
                    progressBar = findViewById(R.id.loadingProgressBar);
                    String InsuranceNumber = txtSearchInsuranceNumber.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    new Thread(() -> {
                        int result = ca.ModifyFamily(InsuranceNumber);
                        runOnUiThread(()->{
                            if ( result == 1) {
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(SearchActivity.this, Enrolment.class);
                                startActivity(intent);
                                finish();
                            } else {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }).start();
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
        btnSearch = findViewById(R.id.btnSearch);
        txtSearchInsuranceNumber = findViewById(R.id.txtSearchInsuranceNumber);
    }

    private void canSearch (){
        if(txtSearchInsuranceNumber.getText().toString().isEmpty()){
            btnSearch.setEnabled(false);
        } else {
            btnSearch.setEnabled(true);
        }
    }
}