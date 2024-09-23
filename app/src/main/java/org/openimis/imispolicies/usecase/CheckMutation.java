package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openimis.imispolicies.CheckMutationQuery;
import org.openimis.imispolicies.network.request.CheckMutationGraphQLRequest;

import java.util.concurrent.TimeoutException;

public class CheckMutation {

    private static final long DEFAULT_TIMEOUT = 30_000L;
    private static final long DEFAULT_DELAY = 500L;
    private static final int STATUS_RECEIVED = 0;
    private static final int STATUS_ERROR = 1;

    private final long timeOutMs;
    private final long delayMs;
    @NonNull
    private final CheckMutationGraphQLRequest request;

    public CheckMutation() {
        this(DEFAULT_TIMEOUT, DEFAULT_DELAY);
    }

    public CheckMutation(long timeOutMs, long delayMs) {
        this(timeOutMs, delayMs, new CheckMutationGraphQLRequest());
    }

    public CheckMutation(long timeOutMs, long delayMs, @NonNull CheckMutationGraphQLRequest request) {
        this.timeOutMs = timeOutMs;
        this.delayMs = delayMs;
        this.request = request;
    }

    @WorkerThread
    public void execute(@NonNull String uuid, @NonNull String message) throws Exception {
        long start = System.currentTimeMillis();
        CheckMutationQuery.Node node = null;
        Integer status;
        do {
            if (node != null) {
                Thread.sleep(delayMs);
            }
            node = request.execute(uuid);
            status = node.status();
            if (System.currentTimeMillis() >= start + timeOutMs) {
                throw new TimeoutException("Could not retrieve the mutation status of '" + uuid + "' within " + timeOutMs + "ms");
            }
        } while (status == null || status == STATUS_RECEIVED);

        if (status == STATUS_ERROR) {
            throw new IllegalStateException(message + ":\n" + getErrorDetail(node.error()));
        }
    }

    private String getErrorDetail(String error) {
        try {
            JSONArray array = new JSONArray(error);
            StringBuilder builder = new StringBuilder();
            for (int i=0; i < array.length();i++) {
                JSONObject object = array.getJSONObject(i);
                if (builder.length() != 0) {
                    builder.append("\n");
                }
                builder.append(" - ");
                builder.append(object.getString("detail"));
            }
            if (builder.length() != 0) {
                return builder.toString();
            }
        } catch (Exception ignored) {
            //
        }
        return error;
    }
}
