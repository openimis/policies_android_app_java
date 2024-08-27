package org.openimis.imispolicies.network.request;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.CreateFamilyMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.CreateFamilyMutationInput;
import org.openimis.imispolicies.type.FamilyHeadInsureeInputType;
import org.openimis.imispolicies.type.PhotoInputType;
import org.openimis.imispolicies.util.DateUtils;

import java.util.Objects;

public class CreateFamilyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public CreateFamilyMutation.Data create(@NonNull Family family) throws Exception {
        Family.Member head = family.getHead();
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        Response<CreateFamilyMutation.Data> response = makeSynchronous(new CreateFamilyMutation(
                CreateFamilyMutationInput.builder()
                        .locationId(22)
                        .poverty(family.isPoor())
                        .familyTypeId(family.getType())
                        .address(family.getAddress())
                        .ethnicity(family.getEthnicity())
                        .confirmationNo(family.getConfirmationNumber())
                        .confirmationTypeId(family.getConfirmationType())
                        .isOffline(family.isOffline())
                        .headInsuree(
                                FamilyHeadInsureeInputType.builder()
                                        .lastName(head.getLastName())
                                        .otherNames(head.getOtherNames())
                                        .genderId(head.getGender())
                                        .dob(head.getDateOfBirth())
                                        .passport(head.getIdentificationNumber())
                                        .cardIssued(false)
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
                                                        .officerId(1)
                                                        .build()
                                        )
                                        .build()
                        )
                        .build()
        ));
        return Objects.requireNonNull(response.getData());
    }
}
