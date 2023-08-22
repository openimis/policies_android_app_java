package org.openimis.imispolicies.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
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

    public static void showDialog(
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
        if (context instanceof Activity && Thread.currentThread() != Looper.getMainLooper().getThread()) {
            ((Activity) context).runOnUiThread(builder::show);
        } else {
            builder.show();
        }
    }

    public static void showDialog(@NonNull Context context, int messageResId) {
        showDialog(context, null, context.getResources().getString(messageResId), false, context.getResources().getString(R.string.Ok), null, null, null, null, null);
    }

    public static void showDialog(@NonNull Context context, @NonNull CharSequence message) {
        showDialog(context, null, message, false, context.getResources().getString(R.string.Ok), null, null, null, null, null);
    }

    public static void showDialog(@NonNull Context context, @NonNull CharSequence title, @NonNull CharSequence message) {
        showDialog(context, title, message, false, context.getResources().getString(R.string.Ok), null, null, null, null, null);
    }

    public static void showConfirmDialog(@NonNull Context context, int messageResId, @NonNull DialogInterface.OnClickListener onPositive) {
        showDialog(context, null, context.getResources().getString(messageResId), false, context.getResources().getString(R.string.Ok), onPositive, null, null, context.getResources().getString(R.string.Cancel), null);
    }

    public static void showDialog(@NonNull Context context, @NonNull String message, @NonNull String positiveLabel, @NonNull DialogInterface.OnClickListener onPositive) {
        showDialog(context, null, message, false, positiveLabel, onPositive, null, null, context.getResources().getString(R.string.Cancel), null);
    }
}
