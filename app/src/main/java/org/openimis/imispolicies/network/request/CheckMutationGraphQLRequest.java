package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.CheckMutationQuery;

import java.util.Objects;

public class CheckMutationGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public CheckMutationQuery.Node execute(@NonNull String uuid) throws Exception {
        Response<CheckMutationQuery.Data> response = makeSynchronous(new CheckMutationQuery(Input.fromNullable(uuid)));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                        Objects.requireNonNull(response.getData())
                                .mutationLogs()
                ).edges().get(0).node()
        );
    }
}
