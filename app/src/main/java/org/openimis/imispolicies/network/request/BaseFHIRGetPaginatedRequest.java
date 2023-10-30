package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.json.JSONObject;
import org.openimis.imispolicies.network.response.PaginatedResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseFHIRGetPaginatedRequest<T> extends BaseFHIRGetRequest<PaginatedResponse<T>> {
    BaseFHIRGetPaginatedRequest(@NonNull String endpoint) {
        super(endpoint);
    }

    @WorkerThread
    @NonNull
    public PaginatedResponse<T> get(int page) throws Exception {
        return get(new HashMap<>(), page);
    }

    @WorkerThread
    @NonNull
    public PaginatedResponse<T> get(@NonNull Map<String, String> queryParameters, int page) throws Exception {
        queryParameters.put("page-offset", String.valueOf(page + 1));
        return get(queryParameters);
    }

    @NonNull
    @Override
    protected final PaginatedResponse<T> fromJson(@NonNull JSONObject object) throws Exception {
        return new PaginatedResponse<>(
                getValueFromJson(object),
                PaginatedResponse.hasNextLink(object)
        );
    }

    @NonNull
    protected abstract List<T> getValueFromJson(@NonNull JSONObject object) throws Exception;
}
