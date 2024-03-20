package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.DeleteRenewalMutation;
import org.openimis.imispolicies.type.DeletePolicyRenewalsMutationInput;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DeletePolicyRenewalGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String delete(@NonNull String uuid) throws Exception {
        Response<DeleteRenewalMutation.Data> response = makeSynchronous(new DeleteRenewalMutation(
                DeletePolicyRenewalsMutationInput.builder()
                        .uuids(List.of(uuid))
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Delete policy renewal with UUID '" + uuid + "'")
                        .build()
        ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                        Objects.requireNonNull(response.getData(), "data is null")
                                .deletePolicyRenewals(), "deletePolicyRenewals is null"
                ).clientMutationId(), "clientMutationId is null");
    }
}
