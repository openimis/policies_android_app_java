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
            CreateInsureeMutation mutation = new CreateInsureeMutation(
                    CreateInsureeMutationInput.builder()
                            .clientMutationId(UUID.randomUUID().toString())
                            .familyId(familyId)
                            .chfId(member.getChfId())
                            .passport(member.getIdentificationNumber())
                            .typeOfIdId(member.getTypeOfId() != null && !member.getTypeOfId().isEmpty() ? member.getTypeOfId() : null)
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
                            // Champs optionnels - envoyer les valeurs sélectionnées par l'utilisateur ou null
                            .residenceEnvironmentId(member.getResidenceEnvironment() != null && member.getResidenceEnvironment() != 0 ? member.getResidenceEnvironment() : null)
                            .housingTypeId(member.getHousingType() != null && !member.getHousingType().equals("0") && !member.getHousingType().isEmpty() ? Integer.parseInt(member.getHousingType()) : null)
                            .mutualInsuranceCoverageId(member.getMutualInsuranceCoverage() != null ? (member.getMutualInsuranceCoverage() ? 1 : 0) : null)
                            .noDisabilityId(member.getNoDisability() != null ? (member.getNoDisability() ? 1 : 0) : null)
                            .nonDisablingDiseaseId(member.getNonDisablingDisease() != null && !member.getNonDisablingDisease().equals("0") && !member.getNonDisablingDisease().isEmpty() ? Integer.parseInt(member.getNonDisablingDisease()) : null)
                            .incomeLevelId(member.getIncomeLevel() != null && member.getIncomeLevel() != 0 ? member.getIncomeLevel() : null)
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
            );
            
            Response<CreateInsureeMutation.Data> response = makeSynchronous(mutation);

            return Objects.requireNonNull(
                Objects.requireNonNull(
                    Objects.requireNonNull(response.getData(), "data is null")
                        .createInsuree(), "create insuree is null")
                    .clientMutationId(), "client mutation id is null");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}