package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONObject;
import org.openimis.imispolicies.network.exception.HttpException;

import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public abstract class BaseFHIRGetRequest<T> extends BaseFHIRRequest {

    BaseFHIRGetRequest(@NonNull String endpoint) {
        super(endpoint);
    }

    @WorkerThread
    @NonNull
    public T get() throws Exception {
        return get(null);
    }

    @WorkerThread
    @NonNull
    public T get(@Nullable Map<String, String> queryParameters) throws Exception {
        Request.Builder builder = getRequestBuilder(queryParameters);
        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            ResponseBody body = response.body();
            String bodyString = body != null ? body.string() : null;
            if (response.isSuccessful()) {
                if (bodyString == null) {
                    throw new RuntimeException("Call was successful but body was null");
                }
                return fromJson(new JSONObject(bodyString));
            } else {
                throw new HttpException(response.code(), response.message(), bodyString, null);
            }
        }
    }

    @NonNull
    abstract protected T fromJson(@NonNull JSONObject object) throws Exception;
}
