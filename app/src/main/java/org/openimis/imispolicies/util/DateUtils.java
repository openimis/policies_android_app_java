package org.openimis.imispolicies.util;

import androidx.annotation.NonNull;

import org.openimis.imispolicies.AppInformation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = AppInformation.DateTimeInfo.getDefaultDateFormatter();

    private DateUtils() {
        throw new IllegalAccessError("This constructor is private");
    }

    @NonNull
    public static String toDateString(@NonNull Date date) {
        return DATE_FORMAT.format(date);
    }

    @NonNull
    public static Date dateFromString(@NonNull String date) throws ParseException {
        return DATE_FORMAT.parse(date);
    }

}
