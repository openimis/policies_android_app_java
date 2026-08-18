package org.openimis.imispolicies;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    MaterialButton btn_change_rar_pwd, btn_rar_pass_default, btn_export_logs, btn_clear_logs;
    ClientAndroidInterface ca;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.action_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ca = new ClientAndroidInterface(this);
        initViews();
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
        btn_change_rar_pwd = findViewById(R.id.btn_change_rar_pwd);
        btn_rar_pass_default = findViewById(R.id.btn_rar_pass_default);
        btn_export_logs = findViewById(R.id.btn_export_logs);
        btn_clear_logs = findViewById(R.id.btn_clear_logs);

        if(ca.isLoggingEnabled()){
            btn_export_logs.setVisibility(View.VISIBLE);
            btn_clear_logs.setVisibility(View.VISIBLE);
        }else{
            btn_export_logs.setVisibility(View.GONE);
            btn_clear_logs.setVisibility(View.GONE);
        }
    }

    private void setupListenners() {
        btn_change_rar_pwd.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.ChangeRarPassword));

            final EditText input = new EditText(this);
            input.setHint(getResources().getString(R.string.EnterRarPassword));
            builder.setView(input);

            builder.setPositiveButton(getResources().getString(R.string.Save), (dialogInterface, which) -> {
               
            });

            builder.setNegativeButton(getResources().getString(R.string.Cancel), (dialogInterface, which) -> {
                dialogInterface.cancel();
            });

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
                String text = input.getText().toString().trim();
                if (text.isEmpty()) {
                    Toast.makeText(this, getResources().getString(R.string.EnterRarPassword), Toast.LENGTH_SHORT).show();
                } else {
                    ca.SaveRarPassword(text);
                    ca.ShowDialog(getResources().getString(R.string.PasswordChanged));
                    dialog.dismiss();
                }
            });

        });

        btn_rar_pass_default.setOnClickListener(v -> {
            try {
                ca.BackToDefaultRarPassword();
                ca.ShowDialog(getResources().getString(R.string.ResetRarPwd));
            } catch (Exception e) {
                ca.ShowDialog(e.getMessage());
            }
        });

        btn_export_logs.setOnClickListener(v -> {
            ca.exportLogs();
        });

        btn_clear_logs.setOnClickListener(v -> {
            ca.clearLogs();
        });
    }
}