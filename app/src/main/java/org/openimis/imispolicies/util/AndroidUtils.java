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

    public static AlertDialog showDialog(Context context, CharSequence title, CharSequence message, boolean isCancelable, CharSequence positiveLabel, DialogInterface.OnClickListener onPositive, CharSequence neutralLabel, DialogInterface.OnClickListener onNeutral, CharSequence negativeLabel, DialogInterface.OnClickListener onNegative) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);
        builder.setCancelable(isCancelable);
        if (positiveLabel != null) builder.setPositiveButton(positiveLabel, onPositive);
        if (neutralLabel != null) builder.setPositiveButton(neutralLabel, onNeutral);
        if (negativeLabel != null) builder.setNegativeButton(negativeLabel, onNegative);
        return builder.show();
    }

    public static AlertDialog showDialog(Context context, int messageResId) {
        return showDialog(context, null, context.getResources().getString(messageResId), false, context.getResources().getString(R.string.Ok), null, null, null, null, null);
    }

    public static AlertDialog showDialog(Context context, CharSequence message) {
        return showDialog(context, null, message, false, context.getResources().getString(R.string.Ok), null, null, null, null, null);
    }

    public static AlertDialog showDialog(Context context, CharSequence title, CharSequence message) {
        return showDialog(context, title, message, false, context.getResources().getString(R.string.Ok), null, null, null, null, null);
    }

    public static AlertDialog showConfirmDialog(Context context, int messageResId, DialogInterface.OnClickListener onPositive) {
        return showDialog(context, null, context.getResources().getString(messageResId), false, context.getResources().getString(R.string.Ok), onPositive, null, null, context.getResources().getString(R.string.Cancel), null);
    }

    public static AlertDialog showDialog(Context context, String message, String positiveLabel, DialogInterface.OnClickListener onPositive) {
        return showDialog(context, null, message, false, positiveLabel, onPositive, null, null, context.getResources().getString(R.string.Cancel), null);
    }
}
