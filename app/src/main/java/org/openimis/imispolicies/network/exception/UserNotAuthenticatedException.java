package org.openimis.imispolicies.network.exception;

import androidx.annotation.Nullable;

public class UserNotAuthenticatedException extends IllegalStateException {

    public UserNotAuthenticatedException(@Nullable Throwable cause) {
        this(null, cause);
    }

    public UserNotAuthenticatedException(@Nullable String message) {
        this(message, null);
    }

    public UserNotAuthenticatedException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
