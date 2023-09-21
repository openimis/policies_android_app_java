package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.network.request.RenewPolicyGraphQLRequest;

public class RenewPolicy {

    @NonNull
    private final RenewPolicyGraphQLRequest renewPolicyGraphQLRequest;

    public RenewPolicy() {
        this(new RenewPolicyGraphQLRequest());
    }

    public RenewPolicy(@NonNull RenewPolicyGraphQLRequest renewPolicyGraphQLRequest) {
        this.renewPolicyGraphQLRequest = renewPolicyGraphQLRequest;
    }

    @WorkerThread
    public void execute(@NonNull String uuid) throws Exception {
        renewPolicyGraphQLRequest.execute(uuid);
    }
}
