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
        try {
            Response<CreateInsureeMutation.Data> response = makeSynchronous(new CreateInsureeMutation(
                    CreateInsureeMutationInput.builder()
                            .clientMutationId("Create insuree '" + member.getChfId() + "'")
                            .familyId(familyId)
                            .chfId(member.getChfId())
                            .passport(member.getIdentificationNumber())
                            .typeOfIdId(member.getTypeOfId() != null && !member.getTypeOfId().isEmpty() ? Integer.parseInt(member.getTypeOfId()) : null)
                            .lastName(member.getLastName())
                            .otherNames(member.getOtherNames())
                            .dob(member.getDateOfBirth())
                            .genderId(member.getGender())
                            .marital(member.getMarital())
                            .phone(member.getPhone())
                            .email(member.getEmail())
                            .cardIssued(member.isCardIssued())
                            .relationshipId(member.getRelationship())
                            .professionId(member.getProfession())
                            .educationId(member.getEducation() == 0 ? null : member.getEducation())
                            .healthFacilityId(member.getHealthFacilityId() == 0 ? null : member.getHealthFacilityId())
                            .professionalSituation(member.getProfessionalSituation())
                            .currentAddress(member.getCurrentAddress())
                            .geolocation(member.getGeolocation())
                            .currentVillageId(member.getCurrentVillage() != null && member.getCurrentVillage() != 0 ? member.getCurrentVillage() : null)
                            // NEW REQUIRED FIELDS - Temporarily commented until server supports CreateInsureeMutationInput
                            //.residenceEnvironmentId(member.getResidenceEnvironment() != null && member.getResidenceEnvironment() != 0 ? member.getResidenceEnvironment() : 3)
                            //.housingTypeId(member.getHousingType() != null && !member.getHousingType().isEmpty() ? Integer.parseInt(member.getHousingType()) : 2)
                            //.mutualInsuranceCoverageId(member.getMutualInsuranceCoverage() != null && member.getMutualInsuranceCoverage() ? 1 : 3)
                            //.noDisabilityId(member.getNoDisability() != null && member.getNoDisability() ? 1 : 3)
                            //.nonDisablingDiseaseId(member.getNonDisablingDisease() != null && !member.getNonDisablingDisease().isEmpty() ? Integer.parseInt(member.getNonDisablingDisease()) : 2)
                            // Fields now supported by server GraphQL schema - activated for data transmission
                            .incomeLevelId(member.getIncomeLevel() != null && member.getIncomeLevel() != 0 ? member.getIncomeLevel() : null)
                            .preferredPaymentMethod(member.getPaymentMethod() != null && !member.getPaymentMethod().isEmpty() ? member.getPaymentMethod() : null)
                            .coordinates(member.getOtherHousehold() != null && !member.getOtherHousehold().isEmpty() ? member.getOtherHousehold() : null)
                            .bankCoordinates(member.getAccountDetails() != null && !member.getAccountDetails().isEmpty() ? member.getAccountDetails() : null)
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
                            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
