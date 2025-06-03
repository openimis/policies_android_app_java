package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.CreateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreateInsureeGraphQLRequest;
import org.openimis.imispolicies.network.request.CreatePolicyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreatePremiumGraphQLRequest;
import org.openimis.imispolicies.network.request.CreateSubFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateInsureeGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdatePolicyGraphQLRequest;
import org.openimis.imispolicies.tools.Log;

import java.net.HttpURLConnection;
import java.util.Objects;

public class UpdateFamily {

    @NonNull
    private final CreateFamilyGraphQLRequest createFamilyGraphQLRequest;
    @NonNull
    private final UpdateFamilyGraphQLRequest updateFamilyGraphQLRequest;
    @NonNull
    private final CreateInsureeGraphQLRequest createInsureeGraphQLRequest;
    @NonNull
    private final UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest;
    @NonNull
    private final CreateSubFamilyGraphQLRequest createSubFamilyGraphQLRequest;
    @NonNull
    private final FetchInsureeInquire fetchInsureeInquire;
    @NonNull
    private final FetchFamily fetchFamily;
    @NonNull
    private final FetchFamilyId fetchFamilyId;
    @NonNull
    private final CreatePolicyGraphQLRequest createPolicyGraphQLRequest;
    @NonNull
    private final CreatePremiumGraphQLRequest createPremiumGraphQLRequest;
    @NonNull
    private final UpdatePolicyGraphQLRequest updatePolicyGraphQLRequest;

    public UpdateFamily() {
        this(
                new CreateFamilyGraphQLRequest(),
                new UpdateFamilyGraphQLRequest(),
                new CreateInsureeGraphQLRequest(),
                new UpdateInsureeGraphQLRequest(),
                new CreateSubFamilyGraphQLRequest(),
                new FetchInsureeInquire(),
                new FetchFamily(),
                new FetchFamilyId(),
                new CreatePolicyGraphQLRequest(),
                new CreatePremiumGraphQLRequest(),
                new UpdatePolicyGraphQLRequest()
        );
    }

    public UpdateFamily(
            @NonNull CreateFamilyGraphQLRequest createFamilyGraphQLRequest,
            @NonNull UpdateFamilyGraphQLRequest updateFamilyGraphQLRequest,
            @NonNull CreateInsureeGraphQLRequest createInsureeGraphQLRequest,
            @NonNull UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest,
            @NonNull CreateSubFamilyGraphQLRequest createSubFamilyGraphQLRequest,
            @NonNull FetchInsureeInquire fetchInsureeInquire,
            @NonNull FetchFamily fetchFamily,
            @NonNull FetchFamilyId fetchFamilyId,
            @NonNull CreatePolicyGraphQLRequest createPolicyGraphQLRequest,
            @NonNull CreatePremiumGraphQLRequest createPremiumGraphQLRequest,
            @NonNull UpdatePolicyGraphQLRequest updatePolicyGraphQLRequest
    ) {
        this.createFamilyGraphQLRequest = createFamilyGraphQLRequest;
        this.updateFamilyGraphQLRequest = updateFamilyGraphQLRequest;
        this.createInsureeGraphQLRequest = createInsureeGraphQLRequest;
        this.updateInsureeGraphQLRequest = updateInsureeGraphQLRequest;
        this.createSubFamilyGraphQLRequest = createSubFamilyGraphQLRequest;
        this.fetchInsureeInquire = fetchInsureeInquire;
        this.fetchFamily = fetchFamily;
        this.fetchFamilyId = fetchFamilyId;
        this.createPolicyGraphQLRequest = createPolicyGraphQLRequest;
        this.createPremiumGraphQLRequest = createPremiumGraphQLRequest;
        this.updatePolicyGraphQLRequest = updatePolicyGraphQLRequest;
    }

    @WorkerThread
    public void execute(
            @NonNull Family family,
            @NonNull String insureeCHFID,
            int officerId
    ) throws Exception {
        int familyId = 0;
        try {
            fetchFamily.execute(insureeCHFID);
            updateFamilyGraphQLRequest.update(family, officerId);
            familyId = family.getId();
        } catch (HttpException e) {
            if (e.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                if(family.getParentId() != null && family.getParentId() != 0){
                    createSubFamilyGraphQLRequest.create(family, officerId);
                }else{
                    createFamilyGraphQLRequest.create(family, officerId);
                }
                try{
                    Family existingFamily = fetchFamilyId.execute();
                    familyId = existingFamily.getId();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            } else {
                throw e;
            }
        }

        for (Family.Member member : family.getMembers()) {
            insertOrUpdateInsuree(member, officerId, familyId);
        }

        for (Family.Policy policy : Objects.requireNonNull(family.getPolicies())){
            insertOrUpdatePolicy(policy, familyId);
        }

        /*if (existingFamily == null) {
            createFamilyGraphQLRequest.create(family);
        } else {
            updateFamilyGraphQLRequest.update(family);
            outer:
            for (Family.Member existingMember : existingFamily.getMembers()) {
                for (Family.Member member: family.getMembers()) {
                    if (member.getChfId().equals(existingMember.getChfId())) {
                        continue outer;
                    }
                }
                removeMemberFromFamily(existingMember);
            }
        }*/
    }

    @WorkerThread
    private void insertOrUpdateInsuree(@NonNull Family.Member member, int officerId, int familyId ) throws Exception {
        try {
            fetchInsureeInquire.execute(member.getChfId());
            updateInsureeGraphQLRequest.update(member);
        } catch (HttpException e) {
            if (e.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                if(familyId != 0 && !member.isHead()){
                    createInsureeGraphQLRequest.create(member, familyId, officerId);
                }
            } else {
                throw e;
            }
        }
    }

    @WorkerThread
    private void insertOrUpdatePolicy (@NonNull Family.Policy policy, int familyId) throws Exception{
        if (policy.getUuid().isEmpty()){
            createPolicyGraphQLRequest.create(policy, familyId);
            for (Family.Policy.Premium premium : policy.getPremiums()) {
                createPremiumGraphQLRequest.create(premium);
            }
        }else{
            updatePolicyGraphQLRequest.update(policy, familyId);
            for (Family.Policy.Premium premium : policy.getPremiums()) {
                createPremiumGraphQLRequest.create(premium);
            }
        }
    }

    @WorkerThread
    private void removeMemberFromFamily(@NonNull Family.Member member) throws Exception {
        updateInsureeGraphQLRequest.update(member);
    }
}
