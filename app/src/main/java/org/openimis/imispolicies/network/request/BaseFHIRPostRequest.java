package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.network.exception.HttpException;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public abstract class BaseFHIRPostRequest<T, U> extends BaseFHIRRequest {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    BaseFHIRPostRequest(@NonNull String endpoint) {
        super(endpoint);
    }

    @WorkerThread
    @NonNull
    public U post(T object) throws Exception {
        return post(object, null);
    }

    @WorkerThread
    @NonNull
    public U post(T object, @Nullable Map<String, String> queryParameters) throws Exception {
        Request.Builder builder = getRequestBuilder(queryParameters);
        JSONObject entity = toJson(object);
        builder.post(RequestBody.create(entity.toString(), JSON));
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
    abstract protected U fromJson(@NonNull JSONObject object) throws Exception;

    @NonNull
    abstract protected JSONObject toJson(T object) throws JSONException;
}
