package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;


import org.openimis.imispolicies.CreatePolicyMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.CreatePolicyMutationInput;

import java.util.Objects;

public class CreatePolicyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public CreatePolicyMutation.Data create(@NonNull Family.Policy policy) throws Exception {
        Response<CreatePolicyMutation.Data> response = makeSynchronous(new CreatePolicyMutation(
                CreatePolicyMutationInput.builder()
                        .familyId(policy.getFamilyId())
                        .enrollDate(policy.getEnrollDate())
                        .startDate(policy.getStartDate())
                        .expiryDate(policy.getExpiryDate())
                        .value(policy.getValue())
                        .productId(policy.getProductId())
                        .officerId(policy.getOfficerId())
                        .build()
        ));
        return Objects.requireNonNull(response.getData());
    }
}
