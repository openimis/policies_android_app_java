package org.openimis.imispolicies.domain.utils;

import android.util.Base64;

import androidx.annotation.Nullable;

public class PhotoUtils {

    private PhotoUtils() throws IllegalAccessException {
        throw new IllegalAccessException("This constructor should not be accessed");
    }

    @Nullable
    public static String getPhotoPath(@Nullable String folder, @Nullable String fileName) {
        if (fileName != null) {
            if (folder != null) {
                folder = folder.replace('\\', '/');
                if (!folder.endsWith("/")) {
                    folder += "/";
                }
                return folder + fileName;
            }
            return fileName;
        }
        return null;
    }

    @Nullable
    public static byte[] getPhotoBytes(@Nullable String photoBase64) {
        if (photoBase64 == null) {
            return null;
        }
        return Base64.decode(photoBase64, Base64.DEFAULT);
    }
}
