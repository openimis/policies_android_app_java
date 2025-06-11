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
    @NonNull
    private final CheckMutation checkMutation;

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
                new UpdatePolicyGraphQLRequest(),
                new CheckMutation()
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
            @NonNull UpdatePolicyGraphQLRequest updatePolicyGraphQLRequest,
            @NonNull CheckMutation checkMutation
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
        this.checkMutation = checkMutation;
    }

    @WorkerThread
    public void execute(
            @NonNull Family family,
            @NonNull String insureeCHFID,
            int officerId
    ) throws Exception {
        int familyId = 0;
        try {
            fetchFamily.execute(insureeCHFID, "");
            checkMutation.execute(
                    updateFamilyGraphQLRequest.update(family, officerId),
                    "Érreur lors de la mise à jour de la famille '" + family.getHeadChfId() + "'"
            );
            familyId = family.getId();
        } catch (HttpException e) {
            if (e.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                checkMutation.execute(
                        createFamilyGraphQLRequest.create(family, officerId),
                        "Érreur lors de la création de la famille '" + family.getHeadChfId() + "'"
                );
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
            checkMutation.execute(
                    updateInsureeGraphQLRequest.update(member, officerId),
                    "Érreur lors de la mise à jour de l'assuré '" + member.getChfId() + "'"
            );

        } catch (HttpException e) {
            if (e.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                if(familyId != 0 && !member.isHead()){
                    checkMutation.execute(
                            createInsureeGraphQLRequest.create(member, familyId, officerId),
                            "Érreur lors de la création de l'assuré '" + member.getChfId() + "'"
                    );
                }
            }
        }
    }

    @WorkerThread
    private void insertOrUpdatePolicy (@NonNull Family.Policy policy, int familyId) throws Exception{
        if (policy.getUuid().isEmpty() || policy.getUuid().equals("0")){
            checkMutation.execute(
                    createPolicyGraphQLRequest.create(policy, familyId),
                    "Érreur lors de la création de la police '" + policy.getUuid() + "'"
            );

            for (Family.Policy.Premium premium : policy.getPremiums()) {
                checkMutation.execute(
                        createPremiumGraphQLRequest.create(premium),
                        "Érreur lors de la création de la cotisation '" + policy.getUuid() + "'"
                );
            }
        } else {
            updatePolicyGraphQLRequest.update(policy, familyId);
            for (Family.Policy.Premium premium : policy.getPremiums()) {
                checkMutation.execute(
                        createPremiumGraphQLRequest.create(premium),
                        "Érreur lors de la création de la cotisation '" + policy.getUuid() + "'"
                );
            }
        }
    }

    @WorkerThread
    private void removeMemberFromFamily(@NonNull Family.Member member, int officerId) throws Exception {
        updateInsureeGraphQLRequest.update(member, officerId);
    }
}
