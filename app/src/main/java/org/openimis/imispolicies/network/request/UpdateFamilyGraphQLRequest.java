package org.openimis.imispolicies.network.request;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.UpdateFamilyMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.util.Mapper;
import org.openimis.imispolicies.type.FamilyAttachmentInputType;
import org.openimis.imispolicies.type.FamilyHeadInsureeInputType;
import org.openimis.imispolicies.type.PhotoInputType;
import org.openimis.imispolicies.type.UpdateFamilyMutationInput;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import android.util.Log;

public class UpdateFamilyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String update(@NonNull Family family, int officerId) throws Exception {
        Family.Member head = family.getHead();
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        Response<UpdateFamilyMutation.Data> response = makeSynchronous(new UpdateFamilyMutation(
                UpdateFamilyMutationInput.builder()
                        .uuid(family.getUuid())
                        .clientMutationId("Update family '" + family.getHeadChfId() + "'")
                        .id(family.getId())
                        .locationId(family.getLocationId())
                        .poverty(family.isPoor())
                        .familyTypeId(family.getType())
                        .address(family.getAddress())
                        .ethnicity(family.getEthnicity())
                        .confirmationNo(family.getConfirmationNumber())
                        .confirmationTypeId(family.getConfirmationType())
                        .isOffline(family.isOffline())
                        .parentId(family.getParentId() != null && family.getParentId() != 0 ? family.getParentId() : null)
                        .attachments(
                                family.getAttachments() != null ? Mapper.map(family.getAttachments(), dto -> toAttachment(dto)) : new ArrayList<>()
                        )
                        .headInsuree(
                                FamilyHeadInsureeInputType.builder()
                                        .id(head.getId())
                                        .uuid(head.getUuid())
                                        .chfId(head.getChfId())
                                        .lastName(head.getLastName())
                                        .otherNames(head.getOtherNames())
                                        .genderId(head.getGender())
                                        .dob(head.getDateOfBirth())
                                        .passport(head.getIdentificationNumber())
                                        .cardIssued(head.isCardIssued())
                                        .typeOfIdId(head.getTypeOfId())
                                        .marital(head.getMarital())
                                        .phone(head.getPhone())
                                        .email(head.getEmail())
                                        .professionId(head.getProfession() != null && head.getProfession() != 0 ? head.getProfession() : null)
                                        .educationId(head.getEducation() != null && head.getEducation() != 0 ? head.getEducation() : null)
                                        .professionalSituation(head.getProfessionalSituation())
                                        // NEW categorical fields for head insuree
                                        .residenceEnvironmentId(head.getResidenceEnvironment() != null && head.getResidenceEnvironment() != 0 ? head.getResidenceEnvironment() : null)
                                        .housingTypeId(parseIntegerSafely(head.getHousingType(), "HousingType", head.getChfId()))
                                        .mutualInsuranceCoverageId(head.getMutualInsuranceCoverage() != null && head.getMutualInsuranceCoverage() != 0 ? head.getMutualInsuranceCoverage() : null)
                                        .noDisabilityId(head.getNoDisability() != null && head.getNoDisability() != 0 ? head.getNoDisability() : null)
                                        .nonDisablingDiseaseId(parseIntegerSafely(head.getNonDisablingDisease(), "NonDisablingDisease", head.getChfId()))
                                        .incomeLevelId(head.getIncomeLevel() != null && head.getIncomeLevel() != 0 ? head.getIncomeLevel() : null)
                                        .preferredPaymentMethod(head.getPaymentMethod())
                                        .coordinates(head.getOtherHousehold())
                                        .bankCoordinates(head.getAccountDetails())
                                        .bankCoordinates(head.getAccountDetails())
                                        .residenceEnvironmentId(1)
                                        .housingTypeId(1)
                                        .mutualInsuranceCoverageId(2)
                                        .noDisabilityId(2)
                                        .nonDisablingDiseaseId(1)
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
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .updateFamily(), "update family is null")
                        .clientMutationId(), "clientMutationId is null");
    }

    private FamilyAttachmentInputType toAttachment(
            @NonNull Family.Attachment dto
    ){
        return FamilyAttachmentInputType.builder()
                .title(dto.getTitle())
                .filename(dto.getFilename())
                .mime(dto.getMime())
                .document(dto.getContent())
                .build();
    }

    /**
     * Safely parse a string to Integer with detailed logging to prevent NullPointerException
     * @param value The string value to parse
     * @param fieldName The name of the field for logging
     * @param chfId The CHFID for context in logging
     * @return Integer value or null if parsing fails or value is null/empty/zero
     */
    private Integer parseIntegerSafely(String value, String fieldName, String chfId) {
        try {
            if (value == null) {
                Log.w("UpdateFamily", "[GRAPHQL][" + chfId + "] " + fieldName + " is null, sending null to server");
                return null;
            }
            if (value.trim().isEmpty() || value.equals("0")) {
                Log.w("UpdateFamily", "[GRAPHQL][" + chfId + "] " + fieldName + " is empty or zero (" + value + "), sending null to server");
                return null;
            }
            Integer result = Integer.parseInt(value.trim());

            return result;
        } catch (NumberFormatException e) {
            Log.e("UpdateFamily", "[GRAPHQL][" + chfId + "] Failed to parse " + fieldName + " value: '" + value + "' - sending null to server", e);
            return null;
        }
    }
}
