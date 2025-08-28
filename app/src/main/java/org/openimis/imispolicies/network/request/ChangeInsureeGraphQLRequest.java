package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.ChangeInsureeFamilyMutation;
import org.openimis.imispolicies.type.ChangeInsureeFamilyMutationInput;

import java.util.Objects;
import java.util.UUID;

public class ChangeInsureeGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String execute(@NonNull String subFamilyUuid, @NonNull String headInsureeUuid) throws Exception {
        Response<ChangeInsureeFamilyMutation.Data> response = makeSynchronous(
                new ChangeInsureeFamilyMutation(
                        ChangeInsureeFamilyMutationInput.builder()
                                .clientMutationId(UUID.randomUUID().toString())
                                .clientMutationLabel("Mobile: Déplacement du chef dans la sous-famille")
                                .familyUuid(subFamilyUuid)
                                .insureeUuid(headInsureeUuid)
                                .cancelPolicies(true)
                                .build()
                )
        );

        return Objects.requireNonNull(
                Objects.requireNonNull(
                        Objects.requireNonNull(response.getData(), "Data is null")
                                .changeInsureeFamily(), "change insuree family is null")
                        .clientMutationId(), "clientMutationId is null"
        );
    }
}
