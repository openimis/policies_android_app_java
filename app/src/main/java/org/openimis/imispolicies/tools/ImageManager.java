package org.openimis.imispolicies.tools;

import android.content.ContentResolver;
import android.content.Context;

import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.util.FileUtils;

import java.io.File;

public class ImageManager {
    protected static final String LOG_TAG = "IMAGEMANAGER";

    protected final Context context;
    protected final ContentResolver contentResolver;

    public static ImageManager of(Context context) {
        return new ImageManager(context);
    }

    protected ImageManager(Context context) {
        this.context = context;
        this.contentResolver = this.context.getContentResolver();
    }

    public File getNewestInsureeImage(String insureeNumber) {
        return FileUtils.getNewestFileStartingWith(
                new File(Global.getGlobal().getImageFolder()),
                insureeNumber + "_"
        );
    }

    public File[] getInsureeImages(String insureeNumber) {
        return FileUtils.getFilesStartingWith(
                new File(Global.getGlobal().getImageFolder()),
                insureeNumber + "_"
        );
    }
}
