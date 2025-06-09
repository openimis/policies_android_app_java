package org.openimis.imispolicies.network.request;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.UpdateInsureeMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.PhotoInputType;
import org.openimis.imispolicies.type.UpdateInsureeMutationInput;

import java.util.Objects;
import java.util.UUID;

public class UpdateInsureeGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String update(
            @NonNull Family.Member member,
            int officerId
        ) throws Exception {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        Response<UpdateInsureeMutation.Data> response = makeSynchronous(new UpdateInsureeMutation(
                UpdateInsureeMutationInput.builder()
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Update insuree '" + member.getChfId() + "'")
                        .uuid(member.getUuid())
                        .chfId(member.getChfId())
                        .familyId(member.getFamilyId())
                        .head(member.isHead())
                        .passport(member.getIdentificationNumber())
                        .typeOfIdId(member.getTypeOfId())
                        .lastName(member.getLastName())
                        .otherNames(member.getOtherNames())
                        .dob(member.getDateOfBirth())
                        .genderId(member.getGender())
                        .marital(member.getMarital())
                        .phone(member.getPhone())
                        .email(member.getEmail())
                        .cardIssued(member.isCardIssued())
                        .relationshipId(member.getRelationship() != null && member.getRelationship() != 0 ? member.getRelationship() : null)
                        .professionId(member.getProfession() != null && member.getProfession() != 0 ? member.getProfession() : null)
                        .educationId(member.getEducation() != null && member.getEducation() != 0 ? member.getEducation() : null)
                        .healthFacilityId(member.getHealthFacilityId() != null && member.getHealthFacilityId() != 0 ? member.getHealthFacilityId() : null)
                        .currentAddress(member.getCurrentAddress())
                        .currentVillageId(member.getCurrentVillage() != null && member.getCurrentVillage() != 0 ? member.getCurrentVillage() : null)
                        .geolocation(member.getGeolocation())
                        .incomeLevelId(member.getIncomeLevel())
                        .preferredPaymentMethod(member.getPaymentMethod())
                        .professionalSituation(member.getProfessionalSituation())
                        .photo(
                                PhotoInputType.builder()
                                        .filename(member.getPhotoPath())
                                        .photo(
                                                member.getPhotoBytes() != null ?
                                                        Base64.encodeToString(member.getPhotoBytes(), Base64.DEFAULT) :
                                                        null
                                        )
                                        .officerId(officerId)
                                        .date(date)
                                        .build()
                        )
                        .build()
        ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .updateInsuree(), "update insuree is null")
                        .clientMutationId(), "clientMutationId is null");
    }
}
