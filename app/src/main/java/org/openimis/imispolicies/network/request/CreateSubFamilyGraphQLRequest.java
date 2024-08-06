package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.CreateFamilyMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.CreateFamilyMutationInput;
import org.openimis.imispolicies.type.FamilyHeadInsureeInputType;

import java.util.Objects;

public class CreateSubFamilyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public CreateFamilyMutation.Data create(@NonNull Family family) throws Exception {
        Family.Member head = family.getHead();
        Response<CreateFamilyMutation.Data> response = makeSynchronous(new CreateFamilyMutation(
                CreateFamilyMutationInput.builder()
                        .locationId(22)
                        .poverty(family.isPoor())
                        .familyTypeId(family.getType())
                        .address(family.getAddress())
                        .ethnicity(family.getEthnicity())
                        .confirmationNo(family.getConfirmationNumber())
                        .confirmationTypeId(family.getConfirmationType())
                        .isOffline(family.isOffline())
                        .parentId(family.getParentId())
                        .headInsuree(
                                FamilyHeadInsureeInputType.builder()
                                        .lastName(head.getLastName())
                                        .otherNames(head.getOtherNames())
                                        .genderId(head.getGender())
                                        .dob(head.getDateOfBirth())
                                        .passport(head.getIdentificationNumber())
                                        .cardIssued(false)
                                        .build()
                        )
                        .build()
        ));
        return Objects.requireNonNull(response.getData());
    }
}
