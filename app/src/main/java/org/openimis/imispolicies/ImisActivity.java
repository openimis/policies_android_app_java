package org.openimis.imispolicies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public abstract class ImisActivity extends AppCompatActivity {
    private static final String BASE_LOG_TAG = "IMISACTIVITY";
    protected ProgressDialog progressDialog;
    protected ActionBar actionBar;
    protected Global global;


    protected AlertDialog showDialog(String msg, DialogInterface.OnClickListener okCallback, DialogInterface.OnClickListener cancelCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false);

        if (okCallback != null) {
            builder.setPositiveButton(R.string.Ok, okCallback);
        } else {
            builder.setPositiveButton(R.string.Ok, ((dialog, which) -> dialog.cancel()));
        }

        if (cancelCallback != null) {
            builder.setNegativeButton(R.string.Cancel, cancelCallback);
        }

        return builder.show();
    }

    protected AlertDialog showDialog(String msg, DialogInterface.OnClickListener okCallback) {
        return showDialog(msg, okCallback, null);
    }

    protected AlertDialog showDialog(String msg) {
        return showDialog(msg, null, null);
    }

}
