package org.openimis.imispolicies.network.request;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.CreateInsureeMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.CreateInsureeMutationInput;
import org.openimis.imispolicies.type.PhotoInputType;
import org.openimis.imispolicies.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class CreateInsureeGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String create(@NonNull Family.Member member, int familyId, int officerId) throws Exception {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        Response<CreateInsureeMutation.Data> response = makeSynchronous(new CreateInsureeMutation(
                CreateInsureeMutationInput.builder()
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Create insuree '" + member.getChfId() + "'")
                        .familyId(familyId)
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
                        .relationshipId(member.getRelationship() == 0 ? null : member.getRelationship())
                        .professionId(member.getProfession())
                        .educationId(member.getEducation() == 0 ? null : member.getEducation())
                        .healthFacilityId(member.getHealthFacilityId() == 0 ? null : member.getHealthFacilityId())
                        .professionalSituation(member.getProfessionalSituation())
                        .incomeLevelId(member.getIncomeLevel())
                        .preferredPaymentMethod(member.getPaymentMethod())
                        .coordinates(member.getOtherHousehold())
                        .bankCoordinates(member.getAccountDetails())
                        .photo(
                                PhotoInputType.builder()
                                        .filename(member.getPhotoPath())
                                        .photo(
                                                member.getPhotoBytes() != null ?
                                                        Base64.encodeToString(member.getPhotoBytes(), Base64.DEFAULT) :
                                                        null
                                        )
                                        .date(date)
                                        .officerId(officerId)
                                        .build()
                        )
                        .build()
        ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .createInsuree(), "create insuree is null")
                        .clientMutationId(), "clientMutationId is null");
    }
}
