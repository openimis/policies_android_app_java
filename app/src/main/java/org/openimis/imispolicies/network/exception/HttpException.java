package org.openimis.imispolicies.network.exception;

import androidx.annotation.Nullable;

public class HttpException extends RuntimeException {

    private final int code;

    @Nullable
    private final String body;

    public HttpException(
            int code,
            @Nullable String message,
            @Nullable String body,
            @Nullable Throwable cause
    ) {
        super("HTTP " + code + " - " + message+": "+body, cause);
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public String getBody() {
        return body;
    }
}
