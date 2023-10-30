package org.openimis.imispolicies.util;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

public class TextViewUtils {

    private TextViewUtils() {
        throw new IllegalAccessError("This constructor is private");
    }

    public static void setDate(@NonNull TextView textView, @Nullable Date date) {
        if (date == null) {
            textView.setText(null);
        } else {
            textView.setText(DateUtils.toDateString(date));
        }
    }
}
