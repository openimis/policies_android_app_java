package org.openimis.imispolicies.network.apollo;

import androidx.annotation.NonNull;

import com.apollographql.apollo.api.CustomTypeAdapter;
import com.apollographql.apollo.api.CustomTypeValue;

public class DecimalCustomTypeAdapter implements CustomTypeAdapter<Double> {
    @Override
    public Double decode(@NonNull CustomTypeValue<?> customTypeValue) {
         try {
             return Double.parseDouble(customTypeValue.value.toString());
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public CustomTypeValue<?> encode(Double o) {
        return new CustomTypeValue.GraphQLNumber(o);
    }
}
