package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetCumulativePoliciesQuery;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.domain.entity.CumulativePolicies;
import org.openimis.imispolicies.network.request.GetCumulativePoliciesGraphQLRequest;

import java.util.Date;

public class FetchCumulativePolicies {

    @NonNull
    private final Global global;
    @NonNull
    private final GetCumulativePoliciesGraphQLRequest getCumulativePoliciesGraphQLRequest;

    public FetchCumulativePolicies() {
        this(Global.getGlobal(), new GetCumulativePoliciesGraphQLRequest());
    }


    public FetchCumulativePolicies(
            @NonNull Global global,
            @NonNull GetCumulativePoliciesGraphQLRequest getCumulativePoliciesGraphQLRequest
    ) {
        this.global = global;
        this.getCumulativePoliciesGraphQLRequest = getCumulativePoliciesGraphQLRequest;
    }

    @NonNull
    @WorkerThread
    public CumulativePolicies execute(
            @Nullable Date from,
            @Nullable Date to
    ) throws Exception {
        return execute(global.requireOfficerCode(), from, to);
    }
    
    @NonNull
    @WorkerThread
    public CumulativePolicies execute(
            @NonNull String officerCode,
            @Nullable Date from,
            @Nullable Date to
    ) throws Exception {
        GetCumulativePoliciesQuery.Data data = getCumulativePoliciesGraphQLRequest.get(officerCode, from, to);
        return new CumulativePolicies(
                /* newPolicies = */ data.new_().totalCount(),
                /* renewedPolicies = */ data.expired().totalCount(),
                /* expiredPolicies = */ data.expired().totalCount(),
                /* suspendedPolicies  = */ data.suspended().totalCount(),
                /* collectedContributions  = */ -1.0
        );
    }
}
