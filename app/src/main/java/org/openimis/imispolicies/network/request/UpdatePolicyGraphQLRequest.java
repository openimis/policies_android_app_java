package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.UpdatePolicyMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.UpdatePolicyMutationInput;

import java.util.Objects;
import java.util.UUID;

public class UpdatePolicyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public UpdatePolicyMutation.Data update(@NonNull Family.Policy policy, int familyId) throws Exception {
        Response<UpdatePolicyMutation.Data> response = makeSynchronous(new UpdatePolicyMutation(
                UpdatePolicyMutationInput.builder()
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationId("Update policy '" + policy.getUuid() + "'")
                        .uuid(policy.getUuid())
                        .familyId(familyId)
                        .enrollDate(policy.getEnrollDate())
                        .startDate(policy.getStartDate())
                        .expiryDate(policy.getExpiryDate())
                        .value(policy.getValue() != null ? String.valueOf(policy.getValue()) : null)
                        .productId(policy.getProductId())
                        .officerId(policy.getOfficerId())
                        .contributionPlanId(policy.getContributionPlanId())
                        .paymentDay(policy.getPaymentDay() != null && !policy.getPaymentDay().isEmpty() ? Integer.parseInt(policy.getPaymentDay()) : null)
                        .signatureDate(policy.getSigningDate())
                        .periodicity(policy.getPeriodicity())
                        .build()
        ));
        return Objects.requireNonNull(response.getData());
    }
}
