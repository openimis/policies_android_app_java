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
                        .clientMutationId("Create insuree '" + member.getChfId() + "'")
                        .familyId(familyId)
                        .head(member.isHead())
                        .lastName(member.getLastName())
                        .otherNames(member.getOtherNames())
                        .dob(member.getDateOfBirth())
                        .genderId(member.getGender())
                        .passport(member.getIdentificationNumber())
                        .typeOfIdId(member.getTypeOfId() != null && !member.getTypeOfId().trim().isEmpty() ? Integer.parseInt(member.getTypeOfId()) : null)
                        .marital(member.getMarital())
                        .phone(member.getPhone())
                        .email(member.getEmail())
                        .cardIssued(member.isCardIssued())
                        .relationshipId(member.getRelationship() != null && member.getRelationship() != 0 ? member.getRelationship() : null)
                        .professionId(member.getProfession() != null && member.getProfession() != 0 ? member.getProfession() : null)
                        .educationId(member.getEducation() != null && member.getEducation() != 0 ? member.getEducation() : null)
                        .healthFacilityId(member.getHealthFacilityId() != null && member.getHealthFacilityId() != 0 ? member.getHealthFacilityId() : null)
                        .professionalSituation(member.getProfessionalSituation())
                        // .incomeLevelId(member.getIncomeLevel() != null && member.getIncomeLevel() != 0 ? member.getIncomeLevel() : null) // Server GraphQL schema not yet updated
                        // .preferredPaymentMethod(member.getPaymentMethod() != null && !member.getPaymentMethod().trim().isEmpty() ? member.getPaymentMethod() : null) // Server GraphQL schema not yet updated
                        // .coordinates(member.getOtherHousehold() != null && !member.getOtherHousehold().trim().isEmpty() ? member.getOtherHousehold() : null) // Server GraphQL schema not yet updated
                        // .bankCoordinates(member.getAccountDetails() != null && !member.getAccountDetails().trim().isEmpty() ? member.getAccountDetails() : null) // Server GraphQL schema not yet updated
                        // .residenceEnvironmentId(member.getResidenceEnvironment() != null ? member.getResidenceEnvironment() : null) // Server GraphQL schema not yet updated
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
