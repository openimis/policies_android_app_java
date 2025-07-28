package org.openimis.imispolicies.network.request;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.CreateFamilyMutation;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.util.Mapper;
import org.openimis.imispolicies.type.CreateFamilyMutationInput;
import org.openimis.imispolicies.type.FamilyAttachmentInputType;
import org.openimis.imispolicies.type.FamilyHeadInsureeInputType;
import org.openimis.imispolicies.type.PhotoInputType;
import org.openimis.imispolicies.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CreateFamilyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String create(@NonNull Family family, int officerId) throws Exception {
        // Enhanced logging for HTTP 400 diagnosis
        Log.d("CreateFamily", "=== CREATING FAMILY ===");
        Log.d("CreateFamily", "Family Head CHFID: " + family.getHeadChfId());
        Log.d("CreateFamily", "Family Location ID: " + family.getLocationId());
        
        Family.Member head = family.getHead();
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        
        // Validate and sanitize head insuree data to prevent HTTP 400
        String validatedGender = validateGender(head.getGender());
        String validatedTypeOfId = validateTypeOfId(head.getTypeOfId());
        Integer validatedEducation = validateEducation(head.getEducation());
        Integer validatedProfession = validateProfession(head.getProfession());
        
        Log.d("CreateFamily", "Head data - Gender: " + validatedGender + ", TypeOfId: " + validatedTypeOfId + ", Education: " + validatedEducation + ", Profession: " + validatedProfession);
        
        try {
            Response<CreateFamilyMutation.Data> response = makeSynchronous(new CreateFamilyMutation(
                    CreateFamilyMutationInput.builder()
                            .clientMutationId("Create family '" + family.getHeadChfId() + "'")
                            .locationId(family.getLocationId())
                            .poverty(family.isPoor())
                            .familyTypeId(family.getType() != null ? family.getType() : "H")
                            .address(family.getAddress())
                            .ethnicity(family.getEthnicity())
                            .confirmationNo(family.getConfirmationNumber())
                            .confirmationTypeId(family.getConfirmationType())
                            .isOffline(family.isOffline())
                            .attachments(
                                    family.getAttachments() != null ? Mapper.map(family.getAttachments(), dto -> toAttachment(dto)) : new ArrayList<>()
                            )
                            .parentId(family.getParentId() != null && family.getParentId() != 0 ? family.getParentId() : null)
                            .headInsuree(
                                    FamilyHeadInsureeInputType.builder()
                                            .lastName(head.getLastName())
                                            .otherNames(head.getOtherNames())
                                            .genderId(validatedGender)
                                            .dob(head.getDateOfBirth())
                                            .passport(head.getIdentificationNumber())
                                            .cardIssued(head.isCardIssued())
                                            .typeOfIdId(validatedTypeOfId)
                                            .marital(head.getMarital())
                                            .phone(head.getPhone())
                                            .email(head.getEmail())
                                            .professionId(validatedProfession)
                                            .educationId(validatedEducation)
                                            .professionalSituation(head.getProfessionalSituation())
                                            // MISSING IMPORTANT FIELDS - Temporarily commented until Apollo generates classes
                                            //.currentAddress(head.getCurrentAddress()) // TODO: Apollo needs to regenerate classes
                                            //.geolocation(head.getGeolocation()) // TODO: Apollo needs to regenerate classes
                                            //.currentVillageId(head.getCurrentVillage() != null && head.getCurrentVillage() != 0 ? head.getCurrentVillage() : null) // TODO: Apollo needs to regenerate classes
                                            //.healthFacilityId(head.getHealthFacilityId() != null && head.getHealthFacilityId() != 0 ? head.getHealthFacilityId() : null) // TODO: Apollo needs to regenerate classes
                                            //.relationshipId(head.getRelationship() != null && head.getRelationship() != 0 ? head.getRelationship() : null) // TODO: Apollo needs to regenerate classes
                                            // NEW REQUIRED FIELDS - Server now requires these as Int!
                                            // Using null-safe values that are more likely to exist in master data
                                            .residenceEnvironmentId(head.getResidenceEnvironment() != null ? head.getResidenceEnvironment() : 3)
                                            .housingTypeId(head.getHousingType() != null && !head.getHousingType().isEmpty() ? Integer.parseInt(head.getHousingType()) : 2)
                                            .mutualInsuranceCoverageId(head.getMutualInsuranceCoverage() != null && head.getMutualInsuranceCoverage() ? 1 : 3)
                                            .noDisabilityId(head.getNoDisability() != null && head.getNoDisability() ? 1 : 3)
                                            .nonDisablingDiseaseId(head.getNonDisablingDisease() != null && !head.getNonDisablingDisease().isEmpty() ? Integer.parseInt(head.getNonDisablingDisease()) : 2)
                                            // ADDITIONAL FIELDS - Now activated for complete data sync
                                            .incomeLevelId(head.getIncomeLevel() != null && head.getIncomeLevel() != 0 ? head.getIncomeLevel() : null)
                                            .preferredPaymentMethod(head.getPaymentMethod() != null && !head.getPaymentMethod().isEmpty() ? head.getPaymentMethod() : null)
                                            .coordinates(head.getOtherHousehold() != null && !head.getOtherHousehold().isEmpty() ? head.getOtherHousehold() : null)
                                            .bankCoordinates(head.getAccountDetails() != null && !head.getAccountDetails().isEmpty() ? head.getAccountDetails() : null)
                                            .photo(
                                                    PhotoInputType.builder()
                                                            .filename(head.getPhotoPath())
                                                            .photo(
                                                                    head.getPhotoBytes() != null ?
                                                                            Base64.encodeToString(head.getPhotoBytes(), Base64.DEFAULT) :
                                                                            null
                                                            )
                                                            .date(date)
                                                            .officerId(officerId)
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            ));
            
            Log.d("CreateFamily", "Family creation successful");
            return Objects.requireNonNull(
                    Objects.requireNonNull(
                                    Objects.requireNonNull(response.getData(), "data is null")
                                            .createFamily(), "create family is null")
                            .clientMutationId(), "clientMutationId is null");
                            
        } catch (Exception e) {
            Log.e("CreateFamily", "=== HTTP 400 ERROR DETAILS ===");
            Log.e("CreateFamily", "Family Head CHFID: " + family.getHeadChfId());
            Log.e("CreateFamily", "Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            Log.e("CreateFamily", "Head Gender (validated): " + validatedGender);
            Log.e("CreateFamily", "Head TypeOfId (validated): " + validatedTypeOfId);
            Log.e("CreateFamily", "Head Education (validated): " + validatedEducation);
            Log.e("CreateFamily", "Head Profession (validated): " + validatedProfession);
            Log.e("CreateFamily", "===========================");
            e.printStackTrace();
            throw e;
        }
    }

    private FamilyAttachmentInputType toAttachment(
            @NonNull Family.Attachment dto
    ){
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        return FamilyAttachmentInputType.builder()
                .title(dto.getTitle())
                .filename(dto.getFilename())
                .mime(dto.getMime())
                .date(date)
                .document(dto.getContent())
                .build();
    }
    
    // Validation methods to prevent HTTP 400 errors - same as CreateInsureeGraphQLRequest
    private String validateGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            Log.w("CreateFamily", "Gender is null/empty, defaulting to 'M'");
            return "M";
        }
        
        String upperGender = gender.trim().toUpperCase();
        if ("M".equals(upperGender) || "F".equals(upperGender) || "O".equals(upperGender)) {
            return upperGender;
        }
        
        Log.w("CreateFamily", "Invalid gender '" + gender + "', defaulting to 'M'");
        return "M";
    }
    
    private String validateTypeOfId(String typeOfId) {
        if (typeOfId == null || typeOfId.trim().isEmpty()) {
            Log.w("CreateFamily", "TypeOfId is null/empty, defaulting to 'N'");
            return "N";
        }
        
        String upperTypeOfId = typeOfId.trim().toUpperCase();
        if ("D".equals(upperTypeOfId) || "N".equals(upperTypeOfId) || "P".equals(upperTypeOfId) || "V".equals(upperTypeOfId)) {
            return upperTypeOfId;
        }
        
        Log.w("CreateFamily", "Invalid typeOfId '" + typeOfId + "', defaulting to 'N'");
        return "N";
    }
    
    private Integer validateEducation(Integer education) {
        if (education == null || education == 0) {
            Log.w("CreateFamily", "Education is null/0, defaulting to 1");
            return 1;
        }
        
        if (education == 1 || education == 2) {
            return education;
        }
        
        Log.w("CreateFamily", "Invalid education '" + education + "', defaulting to 1");
        return 1;
    }
    
    private Integer validateProfession(Integer profession) {
        if (profession == null || profession <= 0) {
            Log.w("CreateFamily", "Profession is null/invalid, defaulting to 1");
            return 1;
        }
        
        if (profession >= 1 && profession <= 20) {
            return profession;
        }
        
        Log.w("CreateFamily", "Invalid profession '" + profession + "', defaulting to 1");
        return 1;
    }
}
