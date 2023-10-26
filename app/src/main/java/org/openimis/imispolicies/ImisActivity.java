package org.openimis.imispolicies;


import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public abstract class ImisActivity extends AppCompatActivity {

    protected AlertDialog showDialog(
            @NonNull String msg,
            @Nullable DialogInterface.OnClickListener okCallback,
            @Nullable DialogInterface.OnClickListener cancelCallback
    ) {
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

    protected AlertDialog showDialog(@NonNull String msg, @Nullable DialogInterface.OnClickListener okCallback) {
        return showDialog(msg, okCallback, null);
    }

    protected AlertDialog showDialog(@NonNull String msg) {
        return showDialog(msg, null);
    }

}
