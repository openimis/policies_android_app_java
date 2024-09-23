package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.ToRestApi;
import org.openimis.imispolicies.domain.entity.PolicyRenewalRequest;
import org.openimis.imispolicies.network.request.RenewPolicyGraphQLRequest;

public class RenewPolicy {

    @NonNull
    private final RenewPolicyGraphQLRequest renewPolicyGraphQLRequest;
    private final CheckMutation checkMutation;
    @NonNull
    private final DeletePolicyRenewal deletePolicyRenewal;

    public RenewPolicy() {
        this(new RenewPolicyGraphQLRequest(), new CheckMutation(), new DeletePolicyRenewal());
    }

    public RenewPolicy(
            @NonNull RenewPolicyGraphQLRequest renewPolicyGraphQLRequest,
            @NonNull CheckMutation checkMutation,
            @NonNull DeletePolicyRenewal deletePolicyRenewal
    ) {
        this.renewPolicyGraphQLRequest = renewPolicyGraphQLRequest;
        this.checkMutation = checkMutation;
        this.deletePolicyRenewal = deletePolicyRenewal;
    }

    @WorkerThread
    public int execute(@NonNull PolicyRenewalRequest request) throws Exception {
        if (request.isDiscontinued()) {
            return deletePolicyRenewal.execute(request.getRenewalId());
        }
        checkMutation.execute(renewPolicyGraphQLRequest.execute(request), "Error while renewing policy '" + request.getRenewalId() + "'");
        return ToRestApi.RenewalStatus.ACCEPTED;
    }
}
