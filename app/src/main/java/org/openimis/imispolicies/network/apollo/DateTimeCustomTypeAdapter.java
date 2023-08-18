package org.openimis.imispolicies.network.apollo;

import androidx.annotation.NonNull;

import com.apollographql.apollo.api.CustomTypeAdapter;
import com.apollographql.apollo.api.CustomTypeValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeCustomTypeAdapter implements CustomTypeAdapter<Date> {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);

    @Override
    public Date decode(@NonNull CustomTypeValue<?> customTypeValue) {
        try {
            return dateFormat.parse(removeMicros(customTypeValue.value.toString()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private String removeMicros(@NonNull String date) {
        return date.substring(0, date.lastIndexOf('.') + 4);
    }

    @NonNull
    @Override
    public CustomTypeValue<?> encode(Date o) {
        return new CustomTypeValue.GraphQLString(dateFormat.format(o));
    }
}
