package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.network.request.RenewPolicyGraphQLRequest;

public class RenewPolicy {

    @NonNull
    private final RenewPolicyGraphQLRequest renewPolicyGraphQLRequest;
    private final CheckMutation checkMutation;

    public RenewPolicy() {
        this(new RenewPolicyGraphQLRequest(), new CheckMutation());
    }

    public RenewPolicy(
            @NonNull RenewPolicyGraphQLRequest renewPolicyGraphQLRequest,
            @NonNull CheckMutation checkMutation
    ) {
        this.renewPolicyGraphQLRequest = renewPolicyGraphQLRequest;
        this.checkMutation = checkMutation;
    }

    @WorkerThread
    public void execute(@NonNull String uuid) throws Exception {
        checkMutation.execute(renewPolicyGraphQLRequest.execute(uuid), "Error while renewing policy '" + uuid + "'");
    }
}
