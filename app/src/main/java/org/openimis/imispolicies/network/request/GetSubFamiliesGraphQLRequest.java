package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;

import org.openimis.imispolicies.GetFamilyQuery;
import org.openimis.imispolicies.GetSubFamiliesQuery;

import java.util.List;

public class GetSubFamiliesGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public List<GetSubFamiliesQuery.Edge> get(String parentUuid) throws Exception {
        return makeSynchronous(new GetSubFamiliesQuery(Input.fromNullable(parentUuid))).getData().families().edges();
    }
}
