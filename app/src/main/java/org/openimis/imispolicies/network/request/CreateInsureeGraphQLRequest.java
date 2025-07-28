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
        // Enhanced logging for HTTP 400 diagnosis
        Log.d("CreateInsuree", "=== STARTING INSUREE CREATION ===");
        Log.d("CreateInsuree", "CHFID: " + member.getChfId());
        Log.d("CreateInsuree", "FamilyId: " + familyId);
        Log.d("CreateInsuree", "OfficerId: " + officerId);
        Log.d("CreateInsuree", "LastName: " + member.getLastName());
        Log.d("CreateInsuree", "OtherNames: " + member.getOtherNames());
        Log.d("CreateInsuree", "Gender: " + member.getGender());
        Log.d("CreateInsuree", "TypeOfId: " + member.getTypeOfId());
        Log.d("CreateInsuree", "IdentificationNumber: " + member.getIdentificationNumber());
        Log.d("CreateInsuree", "IsHead: " + member.isHead());
        
        // Validate critical fields
        if (member.getChfId() == null || member.getChfId().isEmpty()) {
            Log.e("CreateInsuree", "ERROR: CHFID is null or empty");
            throw new IllegalArgumentException("CHFID cannot be null or empty");
        }
        if (member.getLastName() == null || member.getLastName().isEmpty()) {
            Log.e("CreateInsuree", "ERROR: LastName is null or empty");
            throw new IllegalArgumentException("LastName cannot be null or empty");
        }
        if (member.getOtherNames() == null || member.getOtherNames().isEmpty()) {
            Log.e("CreateInsuree", "ERROR: OtherNames is null or empty");
            throw new IllegalArgumentException("OtherNames cannot be null or empty");
        }
        if (familyId <= 0) {
            Log.e("CreateInsuree", "ERROR: FamilyId is invalid: " + familyId);
            throw new IllegalArgumentException("FamilyId must be positive");
        }
        
        // Validate and normalize server-expected data formats
        String validatedGender = validateGender(member.getGender());
        String validatedTypeOfId = validateTypeOfId(member.getTypeOfId());
        Integer validatedEducation = validateEducation(member.getEducation());
        Integer validatedProfession = validateProfession(member.getProfession());
        Integer validatedRelationship = validateRelationship(member.getRelationship());
        
        Log.d("CreateInsuree", "Validated Gender: " + validatedGender);
        Log.d("CreateInsuree", "Validated TypeOfId: " + validatedTypeOfId);
        Log.d("CreateInsuree", "Validated Education: " + validatedEducation);
        Log.d("CreateInsuree", "Validated Profession: " + validatedProfession);
        Log.d("CreateInsuree", "Validated Relationship: " + validatedRelationship);
        
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        Log.d("CreateInsuree", "Date: " + date);
        Log.d("CreateInsuree", "Building GraphQL mutation...");
        
        try {
            Response<CreateInsureeMutation.Data> response = makeSynchronous(new CreateInsureeMutation(
                    CreateInsureeMutationInput.builder()
                            .clientMutationId("Create insuree '" + member.getChfId() + "'")
                            .familyId(familyId)
                            .head(member.isHead())
                            .passport(member.getIdentificationNumber())
                            .typeOfIdId(validatedTypeOfId)
                            .lastName(member.getLastName())
                            .otherNames(member.getOtherNames())
                            .dob(member.getDateOfBirth())
                            .genderId(validatedGender)
                            .marital(member.getMarital())
                            .phone(member.getPhone())
                            .email(member.getEmail())
                            .cardIssued(member.isCardIssued())
                            .relationshipId(validatedRelationship)
                            .professionId(validatedProfession)
                            .educationId(validatedEducation)
                            .healthFacilityId(member.getHealthFacilityId() == 0 ? null : member.getHealthFacilityId())
                            .professionalSituation(member.getProfessionalSituation())
                            // MISSING IMPORTANT FIELDS - Temporarily commented until Apollo generates classes
                            //.currentAddress(member.getCurrentAddress()) // TODO: Apollo needs to regenerate classes
                            //.geolocation(member.getGeolocation()) // TODO: Apollo needs to regenerate classes
                            //.currentVillageId(member.getCurrentVillage() != null && member.getCurrentVillage() != 0 ? member.getCurrentVillage() : null) // TODO: Apollo needs to regenerate classes
                            
                            // NEW REQUIRED FIELDS - Temporarily commented until Apollo generates classes
                            //.residenceEnvironmentId(member.getResidenceEnvironment() != null && member.getResidenceEnvironment() != 0 ? member.getResidenceEnvironment() : 3) // TODO: Apollo needs to regenerate classes
                            //.housingTypeId(member.getHousingType() != null && !member.getHousingType().isEmpty() ? Integer.parseInt(member.getHousingType()) : 2) // TODO: Apollo needs to regenerate classes
                            //.mutualInsuranceCoverageId(member.getMutualInsuranceCoverage() != null && member.getMutualInsuranceCoverage() ? 1 : 3) // TODO: Apollo needs to regenerate classes
                            //.noDisabilityId(member.getNoDisability() != null && member.getNoDisability() ? 1 : 3) // TODO: Apollo needs to regenerate classes
                            //.nonDisablingDiseaseId(member.getNonDisablingDisease() != null && !member.getNonDisablingDisease().isEmpty() ? Integer.parseInt(member.getNonDisablingDisease()) : 2) // TODO: Apollo needs to regenerate classes
                            
                            // Fields not yet supported by server GraphQL schema - commented to prevent HTTP 400
                            .incomeLevelId(member.getIncomeLevel() == 0 ? null : member.getIncomeLevel()) // Server GraphQL schema not yet updated
                            .preferredPaymentMethod(member.getPaymentMethod() == null || member.getPaymentMethod().isEmpty() ? null : member.getPaymentMethod()) // Server GraphQL schema not yet updated
                            .coordinates(member.getOtherHousehold() == null || member.getOtherHousehold().isEmpty() ? null : member.getOtherHousehold()) // Server GraphQL schema not yet updated
                            .bankCoordinates(member.getAccountDetails() == null || member.getAccountDetails().isEmpty() ? null : member.getAccountDetails()) // Server GraphQL schema not yet updated
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
            
            Log.d("CreateInsuree", "GraphQL mutation successful!");
            return Objects.requireNonNull(
                    Objects.requireNonNull(
                                    Objects.requireNonNull(response.getData(), "data is null")
                                            .createInsuree(), "create insuree is null")
                            .clientMutationId(), "clientMutationId is null");
                            
        } catch (Exception e) {
            Log.e("CreateInsuree", "=== HTTP 400 ERROR DETAILS ===");
            Log.e("CreateInsuree", "Error Type: " + e.getClass().getSimpleName());
            Log.e("CreateInsuree", "Error Message: " + e.getMessage());
            Log.e("CreateInsuree", "CHFID: " + member.getChfId());
            Log.e("CreateInsuree", "FamilyId: " + familyId);
            Log.e("CreateInsuree", "LastName: " + member.getLastName());
            Log.e("CreateInsuree", "OtherNames: " + member.getOtherNames());
            Log.e("CreateInsuree", "Gender: " + member.getGender());
            Log.e("CreateInsuree", "TypeOfId: " + member.getTypeOfId());
            Log.e("CreateInsuree", "IdentificationNumber: " + member.getIdentificationNumber());
            Log.e("CreateInsuree", "DOB: " + member.getDateOfBirth());
            Log.e("CreateInsuree", "Relationship: " + member.getRelationship());
            Log.e("CreateInsuree", "Profession: " + member.getProfession());
            Log.e("CreateInsuree", "Education: " + member.getEducation());
            Log.e("CreateInsuree", "HealthFacilityId: " + member.getHealthFacilityId());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Validate gender according to server data: "M", "F", "O"
     */
    private String validateGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            Log.w("CreateInsuree", "Gender is null/empty, defaulting to 'M'");
            return "M"; // Default to Male
        }
        
        String upperGender = gender.toUpperCase();
        if ("M".equals(upperGender) || "F".equals(upperGender) || "O".equals(upperGender)) {
            return upperGender;
        }
        
        Log.w("CreateInsuree", "Invalid gender '" + gender + "', defaulting to 'M'");
        return "M"; // Default to Male for invalid values
    }
    
    /**
     * Validate type of ID according to server data: "D", "N", "P", "V"
     */
    private String validateTypeOfId(String typeOfId) {
        if (typeOfId == null || typeOfId.isEmpty()) {
            Log.w("CreateInsuree", "TypeOfId is null/empty, defaulting to 'N'");
            return "N"; // Default to National ID
        }
        
        String upperTypeOfId = typeOfId.toUpperCase();
        if ("D".equals(upperTypeOfId) || "N".equals(upperTypeOfId) || "P".equals(upperTypeOfId) || "V".equals(upperTypeOfId)) {
            return upperTypeOfId;
        }
        
        Log.w("CreateInsuree", "Invalid typeOfId '" + typeOfId + "', defaulting to 'N'");
        return "N"; // Default to National ID for invalid values
    }
    
    /**
     * Validate education according to server data: 1 or 2
     */
    private Integer validateEducation(Integer education) {
        if (education == null || education == 0) {
            Log.w("CreateInsuree", "Education is null/0, defaulting to 1");
            return 1; // Default to Normal school curriculum
        }
        
        if (education == 1 || education == 2) {
            return education;
        }
        
        Log.w("CreateInsuree", "Invalid education '" + education + "', defaulting to 1");
        return 1; // Default to Normal school curriculum
    }
    
    /**
     * Validate profession according to server data: 1-20
     */
    private Integer validateProfession(Integer profession) {
        if (profession == null || profession == 0) {
            Log.w("CreateInsuree", "Profession is null/0, defaulting to 1");
            return 1; // Default to Without profession
        }
        
        if (profession >= 1 && profession <= 20) {
            return profession;
        }
        
        Log.w("CreateInsuree", "Invalid profession '" + profession + "', defaulting to 1");
        return 1; // Default to Without profession
    }
    
    /**
     * Validate relationship according to server data: 1, 2, 3, 21-25
     */
    private Integer validateRelationship(Integer relationship) {
        if (relationship == null || relationship == 0) {
            Log.w("CreateInsuree", "Relationship is null/0, defaulting to 1");
            return 1; // Default to Head of family
        }
        
        if (relationship == 1 || relationship == 2 || relationship == 3 || (relationship >= 21 && relationship <= 25)) {
            return relationship;
        }
        
        Log.w("CreateInsuree", "Invalid relationship '" + relationship + "', defaulting to 1");
        return 1; // Default to Head of family
    }
}
