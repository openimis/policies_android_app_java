package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.CreateFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.CreateInsureeGraphQLRequest;
import org.openimis.imispolicies.network.request.UpdateInsureeGraphQLRequest;

import java.net.HttpURLConnection;

public class UpdateFamily {

    @NonNull
    private final FetchFamily fetchFamily;
    @NonNull
    private final CreateFamilyGraphQLRequest createFamilyGraphQLRequest;
    @NonNull
    private final CreateInsureeGraphQLRequest createInsureeGraphQLRequest;
    @NonNull
    private final UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest;
    @NonNull
    private final CheckMutation checkMutation;

    public UpdateFamily() {
        this(
                new FetchFamily(),
                new CreateFamilyGraphQLRequest(),
                new CreateInsureeGraphQLRequest(),
                new UpdateInsureeGraphQLRequest(),
                new CheckMutation()
        );
    }

    public UpdateFamily(
            @NonNull FetchFamily fetchFamily,
            @NonNull CreateFamilyGraphQLRequest createFamilyGraphQLRequest,
            @NonNull CreateInsureeGraphQLRequest createInsureeGraphQLRequest,
            @NonNull UpdateInsureeGraphQLRequest updateInsureeGraphQLRequest,
            @NonNull CheckMutation checkMutation
    ) {
        this.fetchFamily = fetchFamily;
        this.createFamilyGraphQLRequest = createFamilyGraphQLRequest;
        this.createInsureeGraphQLRequest = createInsureeGraphQLRequest;
        this.updateInsureeGraphQLRequest = updateInsureeGraphQLRequest;
        this.checkMutation = checkMutation;
    }

    @WorkerThread
    public void execute(@NonNull Family family) throws Exception {
        Family existingFamily = null;
        try {
            existingFamily = fetchFamily.execute(family.getHeadChfId());
        } catch (HttpException e) {
            if (e.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
                throw e;
            }
        }
        if (existingFamily == null) {
            checkMutation.execute(createFamilyGraphQLRequest.create(family), "Error while creating family '" + family.getUuid() + "'");
            for (Family.Member member : family.getMembers()) {
                if (member.isHead()) {
                    continue;
                }
                insertOrUpdateInsuree(member);
            }
        } else {
            outer:
            for (Family.Member existingMember : existingFamily.getMembers()) {
                for (Family.Member member : family.getMembers()) {
                    if (member.getChfId().equals(existingMember.getChfId())) {
                        continue outer;
                    }
                }
                removeMemberFromFamily(existingMember);
            }
            for (Family.Member member : family.getMembers()) {
                insertOrUpdateInsuree(member);
            }
        }
    }

    @WorkerThread
    private void insertOrUpdateInsuree(@NonNull Family.Member member) throws Exception {
        try {
            checkMutation.execute(updateInsureeGraphQLRequest.update(member), "Error while updating family member '"+member+"'");
        } catch (Exception e) {
            checkMutation.execute(createInsureeGraphQLRequest.create(member), "Error while inserting or updating family member '"+member+"'");
        }
    }

    @WorkerThread
    private void removeMemberFromFamily(@NonNull Family.Member member) throws Exception {
        checkMutation.execute(updateInsureeGraphQLRequest.update(member, null), "Error while removing family member '"+member+"'");
    }
}
