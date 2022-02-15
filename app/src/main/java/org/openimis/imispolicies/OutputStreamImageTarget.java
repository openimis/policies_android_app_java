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

    public OutputStreamImageTarget(OutputStream outputStream, int imageQuality) {
        this.outputStream = outputStream;
        this.imageQuality = imageQuality;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, outputStream);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.e(LOG_TAG, "Loading image failed");
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
