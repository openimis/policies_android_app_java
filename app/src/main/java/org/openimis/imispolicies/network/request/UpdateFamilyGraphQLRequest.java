package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.UpdateFamilyMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.FamilyHeadInsureeInputType;
import org.openimis.imispolicies.type.UpdateFamilyMutationInput;

import java.util.Objects;

public class UpdateFamilyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public UpdateFamilyMutation.Data update(@NonNull Family family) throws Exception {
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
                                        .id(family.getHead().getId())
                                        .chfId(family.getHead().getChfId())
                                        .build()
                        )
                        .build()
        ));
        return Objects.requireNonNull(response.getData());
    }
}
