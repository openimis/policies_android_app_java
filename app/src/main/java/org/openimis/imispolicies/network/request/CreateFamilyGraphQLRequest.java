package org.openimis.imispolicies.network.request;

import android.util.Base64;

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
        Family.Member head = family.getHead();
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        Response<CreateFamilyMutation.Data> response = makeSynchronous(new CreateFamilyMutation(
                CreateFamilyMutationInput.builder()
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Create family '" + family.getHeadChfId() + "'")
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
                                        .genderId(head.getGender())
                                        .dob(head.getDateOfBirth())
                                        .passport(head.getIdentificationNumber())
                                        .cardIssued(head.isCardIssued())
                                        .typeOfIdId(head.getTypeOfId())
                                        .marital(head.getMarital())
                                        .phone(head.getPhone())
                                        .email(head.getEmail())
                                        .professionId(head.getProfession())
                                        .educationId(head.getEducation() == 0 ? null:head.getEducation())
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
                                        .createFamily(), "create family is null")
                        .clientMutationId(), "clientMutationId is null");
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
}
