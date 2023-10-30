package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.GetCumulativePoliciesQuery;

import java.util.Date;
import java.util.Objects;

public class GetCumulativePoliciesGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public GetCumulativePoliciesQuery.Data get(
            @Nullable String officerCode,
            @Nullable Date from,
            @Nullable Date to
    ) throws Exception {
        Response<GetCumulativePoliciesQuery.Data> response = makeSynchronous(new GetCumulativePoliciesQuery(
                Input.fromNullable(officerCode),
                Input.fromNullable(from),
                Input.fromNullable(to)
        ));
        return Objects.requireNonNull(response.getData());
    }
}
