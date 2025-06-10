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

public class UpdateFamilyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String update(@NonNull Family family, int officerId) throws Exception {
        Family.Member head = family.getHead();
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        Response<UpdateFamilyMutation.Data> response = makeSynchronous(new UpdateFamilyMutation(
                UpdateFamilyMutationInput.builder()
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Update family '" + family.getHeadChfId() + "'")
                        .id(family.getId())
                        .uuid(family.getUuid())
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
                                        .incomeLevelId(head.getIncomeLevel())
                                        .preferredPaymentMethod(head.getPaymentMethod())
                                        .coordinates(head.getOtherHousehold())
                                        .bankCoordinates(head.getAccountDetails())
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
}
