package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetSnapshotPoliciesQuery;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.domain.entity.SnapshotPolicies;
import org.openimis.imispolicies.network.request.GetSnapshotPoliciesGraphQLRequest;

import java.util.Date;

public class FetchSnapshotPolicies {

    @NonNull
    private final Global global;
    @NonNull
    private final GetSnapshotPoliciesGraphQLRequest getSnapshotPoliciesGraphQLRequest;

    public FetchSnapshotPolicies() {
        this(Global.getGlobal(), new GetSnapshotPoliciesGraphQLRequest());
    }


    public FetchSnapshotPolicies(
            @NonNull Global global,
            @NonNull GetSnapshotPoliciesGraphQLRequest getSnapshotPoliciesGraphQLRequest
    ) {
        this.getSnapshotPoliciesGraphQLRequest = getSnapshotPoliciesGraphQLRequest;
        this.global = global;
    }

    @NonNull
    @WorkerThread
    public SnapshotPolicies execute(
            @Nullable Date date
    ) throws Exception {
        return execute(global.requireOfficerCode(), date);
    }

    @NonNull
    @WorkerThread
    public SnapshotPolicies execute(
            @NonNull String officerCode,
            @Nullable Date date
    ) throws Exception {
        GetSnapshotPoliciesQuery.Data data = getSnapshotPoliciesGraphQLRequest.get(officerCode, date);
        return new SnapshotPolicies(
                /* active = */ data.active().totalCount(),
                /* expired = */ data.expired().totalCount(),
                /* idle = */ data.idle().totalCount(),
                /* suspended  = */ data.suspended().totalCount()
        );
    }
}
