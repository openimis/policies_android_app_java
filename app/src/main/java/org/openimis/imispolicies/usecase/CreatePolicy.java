package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.CheckMutationQuery;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.request.CreatePolicyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreatePremiumGraphQLRequest;

import java.util.List;
import java.util.Objects;

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
            CheckMutationQuery.Node node = checkMutation.execute(createPolicyGraphQLRequest.create(policy), "Error while creating policy '" + policy.getUuid() + "'");
            String policyUuid = Objects.requireNonNull(node.policies().get(0).policy().uuid(), "checkMutation didn't return a policy Uuid");
            for (Family.Policy.Premium premium : policy.getPremiums()) {
                checkMutation.execute(createPremiumGraphQLRequest.create(premium, policyUuid), "Error while creating premium '" + premium.getId() + "' for policy '" + policyUuid + "'");
            }
        }
    }
}
