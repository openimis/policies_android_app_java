package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imispolicies.GetInsureeInquireQuery;
import org.openimis.imispolicies.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.List;


public class GetInsureeInquireGraphQLRequest extends BaseGraphQLRequest {

    @NonNull
    @WorkerThread
    public GetInsureeInquireQuery.Node get(
            @NonNull String chfId
    ) throws Exception {
        List<GetInsureeInquireQuery.Edge> edges = makeSynchronous(new GetInsureeInquireQuery(
                Input.fromNullable(chfId)
        )).getData().insurees().edges();
        if (edges.isEmpty()) {
            throw new HttpException(
                    /* code = */ HttpURLConnection.HTTP_NOT_FOUND,
                    /* message = */ "Insuree with id '" + chfId + "' was not found",
                    /* body = */ null,
                    /* cause = */ null
            );
        }
        return edges.get(0).node();
    }
}
