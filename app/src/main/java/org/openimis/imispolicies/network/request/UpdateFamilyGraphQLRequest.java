package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.UpdateFamilyMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.FamilyHeadInsureeInputType;
import org.openimis.imispolicies.type.UpdateFamilyMutationInput;

import java.util.Objects;
import java.util.UUID;

public class UpdateFamilyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String update(@NonNull Family family) throws Exception {
        Family.Member head = family.getHead();
        Response<UpdateFamilyMutation.Data> response = makeSynchronous(new UpdateFamilyMutation(
                UpdateFamilyMutationInput.builder()
                        .id(family.getId())
                        .uuid(family.getUuid())
                        .locationId(family.getLocationId())
                        .poverty(family.isPoor())
                        .familyTypeId(family.getType())
                        .address(family.getAddress())
                        .ethnicity(family.getEthnicity())
                        .confirmationNo(family.getConfirmationNumber())
                        .confirmationTypeId(family.getConfirmationType())
                        .isOffline(family.isOffline())
                        .headInsuree(
                                FamilyHeadInsureeInputType.builder()
                                        .id(head.getId())
                                        .chfId(head.getChfId())
                                        .lastName(head.getLastName())
                                        .otherNames(head.getOtherNames())
                                        .genderId(head.getGender())
                                        .dob(head.getDateOfBirth())
                                        .build()
                        )
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Update family with UUID '"+family.getUuid()+"'")
                        .build()
        ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .updateFamily(), "updateFamily is null")
                        .clientMutationId(), "clientMutationId is null");
    }
}
