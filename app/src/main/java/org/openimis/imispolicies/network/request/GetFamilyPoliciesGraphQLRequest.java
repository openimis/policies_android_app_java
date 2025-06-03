package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetPoliciesQuery;

import java.util.List;

public class GetFamilyPoliciesGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public List<GetPoliciesQuery.Edge> get (String familyUuid) throws Exception {
        return makeSynchronous(new GetPoliciesQuery(familyUuid)).getData().policiesByFamily().edges();
    }
}
