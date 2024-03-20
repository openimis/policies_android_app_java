package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.RenewPolicyMutation;
import org.openimis.imispolicies.domain.entity.PolicyRenewalRequest;
import org.openimis.imispolicies.type.MobilePolicyRenewalAndPremiumInput;

import java.util.Objects;
import java.util.UUID;

public class RenewPolicyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String execute(PolicyRenewalRequest request) throws Exception {
        Response<RenewPolicyMutation.Data> response = makeSynchronous(new RenewPolicyMutation(
                MobilePolicyRenewalAndPremiumInput.builder()
                        .renewalId(request.getRenewalId())
                        .renewalDate(request.getDate())
                        .officerId(request.getOfficerId())
                        .receipt(request.getReceiptNumber())
                        .payType(request.getPayType())
                        .amount(request.getAmount())
                        .payerId(request.getPayerId())
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Renew policy with renewalId '" + request.getRenewalId() + "'")
                        .build()
        ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .mobilePolicyRenewalAndPremium(), "mobilePolicyRenewalAndPremium is null")
                        .clientMutationId(), "clientMutationId is null");
    }
}
