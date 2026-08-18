package org.openimis.imispolicies;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class SyncActivity extends AppCompatActivity {

    ClientAndroidInterface ca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        setTitle(getResources().getString(R.string.Sync));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ca = new ClientAndroidInterface(this);

        initButton(R.id.btnUploadEnrolment);
        initButton(R.id.btnUploadRenewals);
        initButton(R.id.btnUploadFeedBack);
        initButton(R.id.btnEnrollmentXML);
        initButton(R.id.btnCreateRenewalXML);
        initButton(R.id.btnCreateFeedbackXML);
        initButton(R.id.btnDownloadMaster);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initButton(int id) {
        MaterialButton btn = findViewById(id);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id = view.getId();

                    if (id == R.id.btnUploadEnrolment) {
                        if(!ca.isLoggedIn()){
                            Intent intent = new Intent(SyncActivity.this, LoginActivity.class);
                            intent.putExtra("Page", 0);
                            startActivity(intent);
                        } else {
                            try {
                                ca.uploadEnrolment();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else if (id == R.id.btnUploadRenewals) {
                        if (!ca.isLoggedIn()) {
                            Intent intent = new Intent(SyncActivity.this, LoginActivity.class);
                            intent.putExtra("Page", 0);
                            startActivity(intent);
                        } else {
                            ca.uploadRenewals();
                        }
                    } else if (id == R.id.btnUploadFeedBack) {
                        if (!ca.isLoggedIn()) {
                            Intent intent = new Intent(SyncActivity.this, LoginActivity.class);
                            intent.putExtra("Page", 0);
                            startActivity(intent);
                        } else {
                            ca.uploadFeedbacks();
                        }
                    } else if (id == R.id.btnEnrollmentXML) {
                        ca.CreateEnrolmentXML();
                    } else if (id == R.id.btnCreateRenewalXML) {
                        ca.CreateRenewalExport();
                    } else if (id == R.id.btnCreateFeedbackXML) {
                        ca.CreateFeedbackExport();
                    } else if (id == R.id.btnDownloadMaster) {
                        String res = ca.checkNet();
                        if (res.equals("false")) {
                            ca.getLocalData();
                        } else {
                            ca.downloadMasterData();
                        }
                    }
                }
            });
        }
    }
}