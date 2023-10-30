package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.network.exception.HttpException;

import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public abstract class BaseGetBinaryRequest extends BaseRequest {

    BaseGetBinaryRequest(@NonNull String endpoint) {
        super(endpoint);
    }

    @WorkerThread
    @NonNull
    public byte[] get() throws Exception {
        return get(null);
    }

    @WorkerThread
    @NonNull
    public byte[] get(@Nullable Map<String, String> queryParameters) throws Exception {
        Request.Builder builder = getRequestBuilder(queryParameters);
        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            ResponseBody body = response.body();
            if (response.isSuccessful() && body != null) {
                return body.bytes();
            } else {
                String responseBody = null;
                if (body != null) {
                    responseBody = body.string();
                }
                throw new HttpException(response.code(), response.message(), responseBody, null);
            }
        }
    }

}
