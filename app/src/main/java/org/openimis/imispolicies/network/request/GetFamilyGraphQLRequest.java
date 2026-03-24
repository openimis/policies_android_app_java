package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imispolicies.GetFamilyQuery;
import org.openimis.imispolicies.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class GetFamilyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public GetFamilyQuery.Node get(String chfId, String parentUuid) throws Exception {
        List<GetFamilyQuery.Edge> edges = makeSynchronous(new GetFamilyQuery(Input.fromNullable(chfId), Input.fromNullable(parentUuid))).getData().families().edges();
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
