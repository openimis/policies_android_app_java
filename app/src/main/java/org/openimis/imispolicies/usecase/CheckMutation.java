package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.CheckMutationQuery;
import org.openimis.imispolicies.network.request.CheckMutationGraphQLRequest;

import java.util.concurrent.TimeoutException;

public class CheckMutation {

    private static final long DEFAULT_TIMEOUT = 30_000L;
    private static final long DEFAULT_DELAY = 500L;
    private static final int STATUS_RECEIVED = 0;
    private static final int STATUS_ERROR = 1;
    private static final int STATUS_SUCCESS = 2;

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
    @NonNull
    public CheckMutationQuery.Node execute(@NonNull String uuid, @NonNull String message) throws Exception {
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
            throw new IllegalStateException(message + ": " + node.error());
        }
        return node;
    }
}
