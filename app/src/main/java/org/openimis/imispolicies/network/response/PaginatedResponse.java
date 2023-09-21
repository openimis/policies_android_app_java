package org.openimis.imispolicies.network.response;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PaginatedResponse<T> {

    public static boolean hasNextLink(@NonNull JSONObject jsonObject) throws JSONException {
        if (!jsonObject.has("link")) {
            return false;
        }

        JSONArray links = jsonObject.getJSONArray("link");
        for (int i = 0; i < links.length(); i++) {
            if ("next".equals(links.getJSONObject(i).getString("relation"))) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private final List<T> value;
    private final boolean hasMore;

    public PaginatedResponse(
            @NonNull List<T> value,
            boolean hasMore
    ) {
        this.value = value;
        this.hasMore = hasMore;
    }

    @NonNull
    public List<T> getValue() {
        return value;
    }

    public boolean hasMore() {
        return hasMore;
    }
}
