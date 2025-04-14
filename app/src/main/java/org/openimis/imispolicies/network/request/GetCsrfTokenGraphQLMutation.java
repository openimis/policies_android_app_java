package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetCsrfTokenMutation;
import java.util.Objects;

public class GetCsrfTokenGraphQLMutation extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String get() throws Exception {
        com.apollographql.apollo.api.Response<GetCsrfTokenMutation.Data> response = makeSynchronous(new GetCsrfTokenMutation());
        return Objects.requireNonNull(
                Objects.requireNonNull(
                        Objects.requireNonNull(response.getData(), "data is null")
                                .getCsrfToken(), "csrfToken is null"
                ).csrfToken(), "csrfToken is null");
    }
}
