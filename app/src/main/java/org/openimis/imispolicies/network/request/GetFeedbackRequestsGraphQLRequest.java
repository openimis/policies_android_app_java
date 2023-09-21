package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imispolicies.GetFeedbackRequestsQuery;
import org.openimis.imispolicies.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.List;

public class GetFeedbackRequestsGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public List<GetFeedbackRequestsQuery.Edge> get(@NonNull String officerCode) throws Exception {
        List<GetFeedbackRequestsQuery.Edge> edges = makeSynchronous(new GetFeedbackRequestsQuery(
                Input.fromNullable(officerCode)
        )).getData().claims().edges();
        if (edges.isEmpty()) {
            throw new HttpException(
                    /* code = */ HttpURLConnection.HTTP_NOT_FOUND,
                    /* message = */ "No feedbacks found",
                    /* body = */ null,
                    /* cause = */ null
            );
        }
        return edges;
    }
}
