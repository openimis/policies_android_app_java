package org.openimis.imispolicies;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.openimis.imispolicies.tools.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.OutputStream;

public class OutputStreamImageTarget implements Target {
    private static final String LOG_TAG = "OSIMAGETARGET";
    private final OutputStream outputStream;
    private final int imageQuality;
    private final Runnable onSuccess;

    public OutputStreamImageTarget(OutputStream outputStream, int imageQuality) {
        this.outputStream = outputStream;
        this.imageQuality = imageQuality;
        this.onSuccess = () -> {
        };
    }

    public OutputStreamImageTarget(OutputStream outputStream, int imageQuality, Runnable onSuccess) {
        this.outputStream = outputStream;
        this.imageQuality = imageQuality;
        this.onSuccess = onSuccess;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if (bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, outputStream)) {
            onSuccess.run();
        } else {
            Log.e(LOG_TAG, "Compressing image failed");
        }
    }

    @Override
    public void onBitmapFailed(Exception exception, Drawable errorDrawable) {
        Log.e(LOG_TAG, "Loading image failed", exception);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
