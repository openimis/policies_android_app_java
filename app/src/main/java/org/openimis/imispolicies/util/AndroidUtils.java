package org.openimis.imispolicies.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openimis.imispolicies.R;

public class AndroidUtils {
    public static ProgressDialog showProgressDialog(
            @NonNull Context context,
            int titleResId,
            int messageResId
    ) {
        return ProgressDialog.show(
                context,
                context.getResources().getString(titleResId),
                context.getResources().getString(messageResId)
        );
    }

    public static void showToast(@NonNull Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(@NonNull Context context, @NonNull CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static AlertDialog showDialog(
            @NonNull Context context,
            @Nullable CharSequence title,
            @Nullable CharSequence message,
            boolean isCancelable,
            @Nullable CharSequence positiveLabel,
            @Nullable DialogInterface.OnClickListener onPositive,
            @Nullable CharSequence neutralLabel,
            @Nullable DialogInterface.OnClickListener onNeutral,
            @Nullable CharSequence negativeLabel,
            @Nullable DialogInterface.OnClickListener onNegative
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);
        builder.setCancelable(isCancelable);
        if (positiveLabel != null) builder.setPositiveButton(positiveLabel, onPositive);
        if (neutralLabel != null) builder.setPositiveButton(neutralLabel, onNeutral);
        if (negativeLabel != null) builder.setNegativeButton(negativeLabel, onNegative);
        return builder.show();
    }

    public static AlertDialog showDialog(@NonNull Context context, int messageResId) {
        return showDialog(context, null, context.getResources().getString(messageResId), false, context.getResources().getString(R.string.Ok), null, null, null, null, null);
    }

    public static AlertDialog showDialog(@NonNull Context context, @NonNull CharSequence message) {
        return showDialog(context, null, message, false, context.getResources().getString(R.string.Ok), null, null, null, null, null);
    }

    public static AlertDialog showDialog(@NonNull Context context, @NonNull CharSequence title, @NonNull CharSequence message) {
        return showDialog(context, title, message, false, context.getResources().getString(R.string.Ok), null, null, null, null, null);
    }

    public static AlertDialog showConfirmDialog(@NonNull Context context, int messageResId, @NonNull DialogInterface.OnClickListener onPositive) {
        return showDialog(context, null, context.getResources().getString(messageResId), false, context.getResources().getString(R.string.Ok), onPositive, null, null, context.getResources().getString(R.string.Cancel), null);
    }

    public static AlertDialog showDialog(@NonNull Context context, @NonNull String message, @NonNull String positiveLabel, @NonNull DialogInterface.OnClickListener onPositive) {
        return showDialog(context, null, message, false, positiveLabel, onPositive, null, null, context.getResources().getString(R.string.Cancel), null);
    }
}
