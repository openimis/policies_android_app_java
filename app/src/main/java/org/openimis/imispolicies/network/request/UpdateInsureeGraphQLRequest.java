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
import android.util.Log;

public class UpdateInsureeGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String update(
            @NonNull Family.Member member,
            int officerId
        ) throws Exception {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        
        // Logs de contrôle pour confirmer les types Integer
        Log.d("UPLOAD", "NoDisability (int) = " + member.getNoDisability());
        Log.d("UPLOAD", "MutualInsuranceCoverage (int) = " + member.getMutualInsuranceCoverage());
        Response<UpdateInsureeMutation.Data> response = makeSynchronous(new UpdateInsureeMutation(
                UpdateInsureeMutationInput.builder()
                        .clientMutationId("Update insuree '" + member.getChfId() + "'") 
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
                        .residenceEnvironmentId(member.getResidenceEnvironment() != null && member.getResidenceEnvironment() != 0 ? member.getResidenceEnvironment() : null)
                        .housingTypeId(parseIntegerSafely(member.getHousingType(), "HousingType", member.getChfId()))
                        .mutualInsuranceCoverageId(member.getMutualInsuranceCoverage() != null && member.getMutualInsuranceCoverage() != 0 ? member.getMutualInsuranceCoverage() : null)
                        .noDisabilityId(member.getNoDisability() != null && member.getNoDisability() != 0 ? member.getNoDisability() : null)
                        .nonDisablingDiseaseId(parseIntegerSafely(member.getNonDisablingDisease(), "NonDisablingDisease", member.getChfId()))
                        .incomeLevelId(member.getIncomeLevel() != null && member.getIncomeLevel() != 0 ? member.getIncomeLevel() : null)
                        .preferredPaymentMethod(member.getPaymentMethod())
                        .coordinates(member.getOtherHousehold())
                        .bankCoordinates(member.getAccountDetails())
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
    
    /**
     * Convertit une chaîne en entier de manière sécurisée
     * @param value La valeur à convertir
     * @param defaultValue La valeur par défaut à retourner en cas d'erreur ou si la valeur est nulle/vide
     * @return L'entier converti ou la valeur par défaut
     */
    private Integer parseIntSafe(String value, Integer defaultValue) {
        if (value == null || value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
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
                Log.w("UpdateInsuree", "[GRAPHQL][" + chfId + "] " + fieldName + " is null, sending null to server");
                return null;
            }
            
            if (value.trim().isEmpty() || value.equals("0")) {
                Log.w("UpdateInsuree", "[GRAPHQL][" + chfId + "] " + fieldName + " is empty or zero (" + value + "), sending null to server");
                return null;
            }
            
            Integer result = Integer.parseInt(value.trim());
            Log.d("UpdateInsuree", "[GRAPHQL][" + chfId + "] " + fieldName + " = " + result);
            return result;
            
        } catch (NumberFormatException e) {
            Log.e("UpdateInsuree", "[GRAPHQL][" + chfId + "] Failed to parse " + fieldName + " value: '" + value + "' - sending null to server", e);
            return null;
        }
    }
}
