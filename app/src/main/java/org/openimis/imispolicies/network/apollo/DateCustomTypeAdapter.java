package org.openimis.imispolicies.network.apollo;

import androidx.annotation.NonNull;

import com.apollographql.apollo.api.CustomTypeAdapter;
import com.apollographql.apollo.api.CustomTypeValue;

import org.openimis.imispolicies.util.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class DateCustomTypeAdapter implements CustomTypeAdapter<Date> {
    @Override
    public Date decode(@NonNull CustomTypeValue<?> customTypeValue) {
         try {
             return DateUtils.dateFromString(customTypeValue.value.toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public CustomTypeValue<?> encode(Date o) {
        return new CustomTypeValue.GraphQLString(DateUtils.toDateString(o));
    }
}
