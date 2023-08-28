package org.openimis.imispolicies.domain.utils;

import android.util.Base64;

import androidx.annotation.NonNull;

public class IdUtils {

    private IdUtils() throws IllegalAccessException {
        throw new IllegalAccessException("This constructor should not be accessed");
    }

    /**
     * https://github.com/graphql-python/graphene-sqlalchemy/issues/126
     */
    public static int getIdFromGraphQLString(@NonNull String id) {
        return Integer.parseInt(new String(Base64.decode(id, Base64.NO_WRAP)).split(":", 2)[1]);
    }
}
