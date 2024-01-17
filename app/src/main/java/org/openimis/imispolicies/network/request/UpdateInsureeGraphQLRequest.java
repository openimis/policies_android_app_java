package org.openimis.imispolicies.network.request;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.UpdateInsureeMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.PhotoInputType;
import org.openimis.imispolicies.type.UpdateInsureeMutationInput;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class UpdateInsureeGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String update(
            @NonNull Family.Member member
    ) throws Exception {
        return update(member, member.getFamilyId());
    }

    @WorkerThread
    @NonNull
    public String update(
            @NonNull Family.Member member,
            @Nullable Integer familyId
    ) throws Exception {
        Response<UpdateInsureeMutation.Data> response = makeSynchronous(new UpdateInsureeMutation(
                UpdateInsureeMutationInput.builder()
                        .id(member.getId())
                        .chfId(member.getChfId())
                        .uuid(member.getUuid())
                        .familyId(familyId)
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
                        .relationshipId(member.getRelationship())
                        .professionId(member.getProfession())
                        .educationId(member.getEducation())
                        .healthFacilityId(member.getHealthFacilityId())
                        .currentAddress(member.getCurrentAddress())
                        .currentVillageId(member.getCurrentVillage())
                        .geolocation(member.getGeolocation())
                        .photo(
                                PhotoInputType.builder()
                                        .filename(member.getPhotoPath())
                                        .officerId(Global.getGlobal().getOfficerId())
                                        .date(new Date())
                                        .photo(
                                                member.getPhotoBytes() != null ?
                                                        Base64.encodeToString(member.getPhotoBytes(), Base64.DEFAULT) :
                                                        null
                                        )
                                        .build()
                        )
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Update insuree with UUID '" + member.getUuid() + "'")
                        .build()
        ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .updateInsuree(), "updateInsuree is null")
                        .clientMutationId(), "clientMutationId is null");
    }
}
