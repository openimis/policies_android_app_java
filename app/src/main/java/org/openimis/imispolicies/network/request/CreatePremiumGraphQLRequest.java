package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.CreatePolicyMutation;
import org.openimis.imispolicies.CreatePremiumMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.CreatePolicyMutationInput;
import org.openimis.imispolicies.type.CreatePremiumMutationInput;

import java.util.Objects;
import java.util.UUID;

public class CreatePremiumGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String create(@NonNull Family.Policy.Premium premium) throws Exception {
        Response<CreatePremiumMutation.Data> response = makeSynchronous(new CreatePremiumMutation(
                CreatePremiumMutationInput.builder()
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Create contribution '" + premium.getPolicyUuid() + "'")
                        .policyUuid(premium.getPolicyUuid())
                        .amount(premium.getAmount())
                        .receipt(premium.getReceipt())
                        .payDate(premium.getPayDate())
                        .payType(premium.getPayType())
                        .isOffline(premium.isOffline())
                        .isPhotoFee(premium.isPhotoFee())
                        .build()
        ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .createPremium(), "mobileEnrollment is null")
                        .clientMutationId(), "clientMutationId is null");
    }
}
