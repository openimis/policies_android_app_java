package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.CreateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreateInsureeGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateInsureeGraphQLRequest;

import java.net.HttpURLConnection;

public class UpdateFamily {

    @NonNull
    private final FetchFamily fetchFamily;
    @NonNull
    private final CreateFamilyGraphQLRequest createFamilyGraphQLRequest;
    @NonNull
    private final UpdateFamilyGraphQLRequest updateFamilyGraphQLRequest;
    @NonNull
    private final CreateInsureeGraphQLRequest createInsureeGraphQLRequest;
    @NonNull
    private final UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest;

    public UpdateFamily() {
        this(
                new FetchFamily(),
                new CreateFamilyGraphQLRequest(),
                new UpdateFamilyGraphQLRequest(),
                new CreateInsureeGraphQLRequest(),
                new UpdateInsureeGraphQLRequest()
        );
    }

    public UpdateFamily(
            @NonNull FetchFamily fetchFamily,
            @NonNull CreateFamilyGraphQLRequest createFamilyGraphQLRequest,
            @NonNull UpdateFamilyGraphQLRequest updateFamilyGraphQLRequest,
            @NonNull CreateInsureeGraphQLRequest createInsureeGraphQLRequest,
            @NonNull UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest
    ) {
        this.fetchFamily = fetchFamily;
        this.createFamilyGraphQLRequest = createFamilyGraphQLRequest;
        this.updateFamilyGraphQLRequest = updateFamilyGraphQLRequest;
        this.createInsureeGraphQLRequest = createInsureeGraphQLRequest;
        this.updateInsureeGraphQLRequest = updateInsureeGraphQLRequest;
    }

    @WorkerThread
    public void execute(@NonNull Family family, @NonNull String insureeCHFID) throws Exception {
        Family existingFamily = null;
        try {
            existingFamily = fetchFamily.execute(insureeCHFID);
        } catch (HttpException e) {
            if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw e;
            }
        }
        if (existingFamily == null) {
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
        }
        for (Family.Member member : family.getMembers()) {
            insertOrUpdateInsuree(member, insureeCHFID);
        }
    }

    @WorkerThread
    private void insertOrUpdateInsuree(@NonNull Family.Member member, @Nullable String insureeCHFID ) throws Exception {
        Family existingFamily = null;
        try {
            existingFamily = fetchFamily.execute(insureeCHFID);
        } catch (HttpException e) {
            if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw e;
            }
        }
        if(existingFamily != null){
            try {
                createInsureeGraphQLRequest.create(member, existingFamily.getId());
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
