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

public class CreatePremiumGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public CreatePremiumMutation.Data create(@NonNull Family.Policy.Premium premium) throws Exception {
        Response<CreatePremiumMutation.Data> response = makeSynchronous(new CreatePremiumMutation(
                CreatePremiumMutationInput.builder()
                        .id(premium.getId())
                        .amount(premium.getAmount())
                        .receipt(premium.getReceipt())
                        .payDate(premium.getPayDate())
                        .payType(premium.getPayType())
                        .isOffline(premium.isOffline())
                        .isPhotoFee(premium.isPhotoFee())
                        .build()
        ));
        return Objects.requireNonNull(response.getData());
    }
}
