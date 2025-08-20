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
import android.util.Log;

public class CreateInsureeGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String create(@NonNull Family.Member member, int familyId, int officerId) throws Exception {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        
        // Logs de contrôle pour confirmer les types Integer
        Log.d("UPLOAD", "NoDisability (int) = " + member.getNoDisability());
        Log.d("UPLOAD", "MutualInsuranceCoverage (int) = " + member.getMutualInsuranceCoverage());
        
        try {
            CreateInsureeMutation mutation = new CreateInsureeMutation(
                    CreateInsureeMutationInput.builder()
                            .clientMutationId("Create insuree '" + member.getChfId() + "'")
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
                            .residenceEnvironment(member.getResidenceEnvironment() != null && member.getResidenceEnvironment() != 0 ? member.getResidenceEnvironment() : null)
                            .housingType(parseIntegerSafely(member.getHousingType(), "HousingType", member.getChfId()))
                            .mutualInsuranceCoverage(member.getMutualInsuranceCoverage() != null && member.getMutualInsuranceCoverage() != 0 ? member.getMutualInsuranceCoverage() : null)
                            .noDisability(member.getNoDisability() != null && member.getNoDisability() != 0 ? member.getNoDisability() : null)
                            .nonDisablingDisease(parseIntegerSafely(member.getNonDisablingDisease(), "NonDisablingDisease", member.getChfId()))
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
    
    /**
     * Safely parse a string to Integer with detailed logging to prevent NullPointerException
     * @param value The string value to parse
     * @param fieldName The name of the field for logging
     * @param chfId The CHFID for context in logging
     * @return Integer value or null if parsing fails or value is null/empty
     */
    private Integer parseIntegerSafely(String value, String fieldName, String chfId) {
        try {
            if (value == null) {
                Log.w("CreateInsuree", "[GRAPHQL][" + chfId + "] " + fieldName + " is null, sending null to server");
                return null;
            }
            
            if (value.trim().isEmpty() || value.equals("0")) {
                Log.w("CreateInsuree", "[GRAPHQL][" + chfId + "] " + fieldName + " is empty or zero (" + value + "), sending null to server");
                return null;
            }
            
            Integer result = Integer.parseInt(value.trim());
            Log.d("CreateInsuree", "[GRAPHQL][" + chfId + "] " + fieldName + " = " + result);
            return result;
            
        } catch (NumberFormatException e) {
            Log.e("CreateInsuree", "[GRAPHQL][" + chfId + "] Failed to parse " + fieldName + " value: '" + value + "' - sending null to server", e);
            return null;
        }
    }
}