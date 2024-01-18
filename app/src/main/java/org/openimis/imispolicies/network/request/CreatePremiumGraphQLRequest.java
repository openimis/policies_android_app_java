package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.CreatePremiumMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.CreatePremiumMutationInput;

import java.util.Objects;
import java.util.UUID;

public class CreatePremiumGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String create(@NonNull Family.Policy.Premium premium, @NonNull String policyUuid) throws Exception {
        Response<CreatePremiumMutation.Data> response = makeSynchronous(new CreatePremiumMutation(
                CreatePremiumMutationInput.builder()
                        .policyUuid(policyUuid)
                        .amount(premium.getAmount())
                        .receipt(premium.getReceipt())
                        .payDate(premium.getPayDate())
                        .payType(premium.getPayType())
                        .isOffline(premium.isOffline())
                        .isPhotoFee(premium.isPhotoFee())
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Create premium with for policy with UUID '" + policyUuid+ "'")
                        .build()
        ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .createPremium(), "createPremium is null")
                        .clientMutationId(), "clientMutationId is null");
    }
}
