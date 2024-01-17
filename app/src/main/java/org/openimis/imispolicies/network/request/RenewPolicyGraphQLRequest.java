package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.RenewPolicyMutation;
import org.openimis.imispolicies.type.RenewPolicyMutationInput;

import java.util.Objects;
import java.util.UUID;

public class RenewPolicyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String execute(@NonNull String uuid) throws Exception {
        Response<RenewPolicyMutation.Data> response = makeSynchronous(new RenewPolicyMutation(RenewPolicyMutationInput.builder()
                .uuid(uuid)
                .clientMutationId(UUID.randomUUID().toString())
                .clientMutationLabel("Renew policy with UUID '" + uuid + "'")
                .build()));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .renewPolicy(), "renewPolicy is null")
                        .clientMutationId(), "clientMutationId is null");
    }
}
