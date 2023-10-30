package org.openimis.imispolicies.network.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.network.request.BaseFHIRGetPaginatedRequest;
import org.openimis.imispolicies.network.response.PaginatedResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PaginatedResponseUtils {

    private PaginatedResponseUtils() {
        throw new IllegalAccessError("This constructor is private");
    }

    @WorkerThread
    @NonNull
    public static <T> List<T> downloadAll(@NonNull BaseFHIRGetPaginatedRequest<T> request) throws Exception {
        return downloadAll(request::get);
    }
    @WorkerThread
    @NonNull
    public static <T> List<T> downloadAll(@NonNull RequestExecutor<T> executor) throws Exception {
        return downloadAll(executor, null);
    }

    @NonNull
    @WorkerThread
    public static <T, U> List<U> downloadAll(
            @NonNull RequestExecutor<T> executor,
            @Nullable Mapper.Transformer<T, U> transformer
    ) throws Exception {
        int page = 0;
        boolean hasMore;
        List<U> list = new ArrayList<>();
        Mapper<T, U> mapper = transformer != null ? new Mapper<>(transformer) : null;
        do {
            PaginatedResponse<T> response = executor.download(page++);
            if (mapper != null) {
                list.addAll(mapper.map(response.getValue()));
            } else {
                list.addAll((Collection<? extends U>) response.getValue());
            }
            hasMore = response.hasMore();
        } while(hasMore);
        return list;
    }

    public interface RequestExecutor<T> {

        @NonNull
        @WorkerThread
        PaginatedResponse<T> download(int page) throws Exception;
    }
}
