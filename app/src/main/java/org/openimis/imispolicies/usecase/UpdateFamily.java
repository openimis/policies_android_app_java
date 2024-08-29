package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.CreateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreateInsureeGraphQLRequest;
import org.openimis.imispolicies.network.request.CreateSubFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateInsureeGraphQLRequest;
import org.openimis.imispolicies.tools.Log;

import java.net.HttpURLConnection;

public class UpdateFamily {

    @NonNull
    private final FetchFamilyId fetchFamilyId;
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

    public UpdateFamily() {
        this(
                new FetchFamilyId(),
                new CreateFamilyGraphQLRequest(),
                new UpdateFamilyGraphQLRequest(),
                new CreateInsureeGraphQLRequest(),
                new UpdateInsureeGraphQLRequest(),
                new CreateSubFamilyGraphQLRequest()
        );
    }

    public UpdateFamily(
            @NonNull FetchFamilyId fetchFamilyId,
            @NonNull CreateFamilyGraphQLRequest createFamilyGraphQLRequest,
            @NonNull UpdateFamilyGraphQLRequest updateFamilyGraphQLRequest,
            @NonNull CreateInsureeGraphQLRequest createInsureeGraphQLRequest,
            @NonNull UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest,
            @NonNull CreateSubFamilyGraphQLRequest createSubFamilyGraphQLRequest
    ) {
        this.fetchFamilyId = fetchFamilyId;
        this.createFamilyGraphQLRequest = createFamilyGraphQLRequest;
        this.updateFamilyGraphQLRequest = updateFamilyGraphQLRequest;
        this.createInsureeGraphQLRequest = createInsureeGraphQLRequest;
        this.updateInsureeGraphQLRequest = updateInsureeGraphQLRequest;
        this.createSubFamilyGraphQLRequest = createSubFamilyGraphQLRequest;
    }

    @WorkerThread
    public void execute(@NonNull Family family, @NonNull String insureeCHFID, @NonNull int officerId) throws Exception {
        /*try {
            //existingFamily = fetchFamily.execute();
        } catch (HttpException e) {
            if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw e;
            }
        }*/
        if(family.getParentId() != null && family.getParentId() != 0){
            createSubFamilyGraphQLRequest.create(family, officerId);
        }else{
            createFamilyGraphQLRequest.create(family, officerId);
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
        for (Family.Member member : family.getMembers()) {
            insertOrUpdateInsuree(member, insureeCHFID, officerId);
        }
    }

    @WorkerThread
    private void insertOrUpdateInsuree(@NonNull Family.Member member, @Nullable String insureeCHFID, @NonNull int officerId ) throws Exception {
        Family existingFamily = null;
        try {
            existingFamily = fetchFamilyId.execute();
        } catch (HttpException e) {
            if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw e;
            }
        }
        Log.e("isHOF",String.valueOf(member.isHead()));
        if(existingFamily != null && member.isHead() == false){
                try {
                    createInsureeGraphQLRequest.create(member, existingFamily.getId(), officerId);
                } catch (Exception e) {
                    updateInsureeGraphQLRequest.update(member, existingFamily.getId());
                }
        }
    }

    @WorkerThread
    private void removeMemberFromFamily(@NonNull Family.Member member) throws Exception {
        updateInsureeGraphQLRequest.update(member, null);
    }
}
