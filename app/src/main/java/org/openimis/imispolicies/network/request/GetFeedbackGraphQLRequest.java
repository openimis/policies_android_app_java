package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imispolicies.GetFeedbacksQuery;
import org.openimis.imispolicies.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.List;

public class GetFeedbackGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public List<GetFeedbacksQuery.Edge> get(@NonNull String officerCode) throws Exception {
        List<GetFeedbacksQuery.Edge> edges = makeSynchronous(new GetFeedbacksQuery(
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
