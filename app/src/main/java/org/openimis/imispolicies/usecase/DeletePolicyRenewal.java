package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.network.request.DeletePolicyRenewalGraphQLRequest;

public class DeletePolicyRenewal {

    @NonNull
    private final DeletePolicyRenewalGraphQLRequest deletePolicyRenewalGraphQLRequest;

    public DeletePolicyRenewal() {
        this(new DeletePolicyRenewalGraphQLRequest());
    }

    public DeletePolicyRenewal(@NonNull DeletePolicyRenewalGraphQLRequest deletePolicyRenewalGraphQLRequest) {
        this.deletePolicyRenewalGraphQLRequest = deletePolicyRenewalGraphQLRequest;
    }

    @WorkerThread
    public void execute(@NonNull String uuid) throws Exception {
        deletePolicyRenewalGraphQLRequest.delete(uuid);
    }
}
