package org.openimis.imispolicies.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import org.openimis.imispolicies.R;

public class AndroidUtils {
    public static ProgressDialog showProgressDialog(Context context, int titleResId, int messageResId) {
        return ProgressDialog.show(
                context,
                context.getResources().getString(titleResId),
                context.getResources().getString(messageResId)
        );
    }

    public static void showToast(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static AlertDialog showDialog(Context context, int messageResId) {
        return new AlertDialog.Builder(context)
                .setMessage(messageResId)
                .setCancelable(false)
                .setPositiveButton(R.string.Ok, (dialogInterface, i) -> {
                }).show();
    }

    public static AlertDialog showDialog(Context context, CharSequence message) {
        return new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.Ok, (dialogInterface, i) -> {
                }).show();
    }

    public static AlertDialog showConfirmDialog(Context context, int messageResId, DialogInterface.OnClickListener onPositive) {
        return new AlertDialog.Builder(context)
                .setMessage(messageResId)
                .setCancelable(false)
                .setPositiveButton(R.string.Ok, onPositive)
                .setNegativeButton(R.string.Cancel, (dialogInterface, i) -> {
                }).show();
    }
}
