package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.request.CreatePolicyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreatePremiumGraphQLRequest;

import java.util.List;

public class CreatePolicy {

    @NonNull
    private final CreatePolicyGraphQLRequest createPolicyGraphQLRequest;
    @NonNull
    private final CreatePremiumGraphQLRequest createPremiumGraphQLRequest;
    @NonNull
    private final CheckMutation checkMutation;

    public CreatePolicy() {
        this(new CreatePolicyGraphQLRequest(), new CreatePremiumGraphQLRequest(), new CheckMutation());
    }

    public CreatePolicy(
            @NonNull CreatePolicyGraphQLRequest createPolicyGraphQLRequest,
            @NonNull CreatePremiumGraphQLRequest createPremiumGraphQLRequest,
            @NonNull CheckMutation checkMutation
    ) {
        this.createPolicyGraphQLRequest = createPolicyGraphQLRequest;
        this.createPremiumGraphQLRequest = createPremiumGraphQLRequest;
        this.checkMutation = checkMutation;
    }

    @WorkerThread
    public void execute(List<Family.Policy> policies) throws Exception {
        for (Family.Policy policy : policies) {
            checkMutation.execute(createPolicyGraphQLRequest.create(policy), "Error while creating policy '" + policy.getUuid() + "'");
            for (Family.Policy.Premium premium : policy.getPremiums()) {
                checkMutation.execute(createPremiumGraphQLRequest.create(premium), "Error while creating premium '" + premium.getId() + "' for policy '" + premium.getPolicyUuid() + "'");
            }
        }
    }
}
