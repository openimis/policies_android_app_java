package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imispolicies.GetFamilyIdQuery;
import org.openimis.imispolicies.GetFamilyQuery;
import org.openimis.imispolicies.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;

public class GetFamilyIdGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public GetFamilyIdQuery.Node get() throws Exception {
        List<GetFamilyIdQuery.Edge> edges = makeSynchronous(new GetFamilyIdQuery()).getData().families().edges();
        if (edges.isEmpty()) {
            throw new HttpException(
                    /* code = */ HttpURLConnection.HTTP_NOT_FOUND,
                    /* message = */ "No family found",
                    /* body = */ null,
                    /* cause = */ null
            );
        }
        return Objects.requireNonNull(edges.get(0).node());
    }

}
