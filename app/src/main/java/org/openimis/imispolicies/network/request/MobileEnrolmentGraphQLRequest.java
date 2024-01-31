package org.openimis.imispolicies.network.request;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.MobileEnrolmentMutation;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.type.FamilyEnrollmentGQLType;
import org.openimis.imispolicies.type.FamilyHeadInsureeInputType;
import org.openimis.imispolicies.type.InsureeEnrollmentGQLType;
import org.openimis.imispolicies.type.MobileEnrollmentMutationInput;
import org.openimis.imispolicies.type.PhotoInputType;
import org.openimis.imispolicies.type.PolicyEnrollmentGQLType;
import org.openimis.imispolicies.type.PremiumEnrollmentGQLType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MobileEnrolmentGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public String execute(@NonNull Family family, List<Family.Policy> policies) throws Exception {
        Family.Member head = family.getHead();
        Response<MobileEnrolmentMutation.Data> response = makeSynchronous(
                new MobileEnrolmentMutation(MobileEnrollmentMutationInput.builder()
                        .clientMutationId(UUID.randomUUID().toString())
                        .clientMutationLabel("Mobile enrolment for family '" + family.getUuid() + "'")
                        .family(FamilyEnrollmentGQLType.builder()
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
                                .headInsuree(
                                        FamilyHeadInsureeInputType.builder()
                                                .chfId(head.getChfId())
                                                .lastName(head.getLastName())
                                                .otherNames(head.getOtherNames())
                                                .genderId(head.getGender())
                                                .dob(head.getDateOfBirth())
                                                .cardIssued(head.isCardIssued())
                                                .build()
                                )
                                .build())
                        .insurees(getInsurees(family))
                        .policies(getPolicies(policies))
                        .premiums(getPremiums(policies))
                        .build()
                ));
        return Objects.requireNonNull(
                Objects.requireNonNull(
                                Objects.requireNonNull(response.getData(), "data is null")
                                        .mobileEnrollment(), "mobileEnrollment is null")
                        .clientMutationId(), "clientMutationId is null");
    }

    private List<InsureeEnrollmentGQLType> getInsurees(Family family) {
        List<InsureeEnrollmentGQLType> insurees = new ArrayList<>();
        for (Family.Member member : family.getMembers()) {
            if (member.isHead()) {
                continue;
            }
            insurees.add(InsureeEnrollmentGQLType.builder()
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
                    .build());
        }
        return insurees;
    }

    private List<PolicyEnrollmentGQLType> getPolicies(List<Family.Policy> familyPolicies) {
        List<PolicyEnrollmentGQLType> policies = new ArrayList<>();
        for (Family.Policy policy : familyPolicies) {
            policies.add(PolicyEnrollmentGQLType.builder()
                    .uuidInput(Input.optional(policy.getUuid()))
                    .mobileId(policy.getId())
                    .familyId(policy.getFamilyId())
                    .enrollDate(policy.getEnrollDate())
                    .startDate(policy.getStartDate())
                    .expiryDate(policy.getExpiryDate())
                    .value(policy.getValue())
                    .productId(policy.getProductId())
                    .officerId(policy.getOfficerId())
                    .build());
        }
        return policies;
    }

    private List<PremiumEnrollmentGQLType> getPremiums(List<Family.Policy> policies) {
        List<PremiumEnrollmentGQLType> premiums = new ArrayList<>();
        for (Family.Policy policy : policies) {
            for (Family.Policy.Premium premium : policy.getPremiums()) {
                premiums.add(PremiumEnrollmentGQLType.builder()
                        .policyId(premium.getPolicyId())
                        .policyUuid(premium.getPolicyUuid() != null ? premium.getPolicyUuid() : String.valueOf(premium.getPolicyId()))
                        .amount(premium.getAmount())
                        .receipt(premium.getReceipt())
                        .payDate(premium.getPayDate())
                        .payType(premium.getPayType())
                        .isOffline(premium.isOffline())
                        .isPhotoFee(premium.isPhotoFee())
                        .build());
            }
        }
        return premiums;
    }
}
