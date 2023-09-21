package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.GetSnapshotPoliciesQuery;

import java.util.Date;
import java.util.Objects;

public class GetSnapshotPoliciesGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public GetSnapshotPoliciesQuery.Data get(
            @Nullable String officerCode,
            @Nullable Date date
    ) throws Exception {
        Response<GetSnapshotPoliciesQuery.Data> response = makeSynchronous(new GetSnapshotPoliciesQuery(
                Input.fromNullable(officerCode),
                Input.fromNullable(date)
        ));
        return Objects.requireNonNull(response.getData());
    }
}
