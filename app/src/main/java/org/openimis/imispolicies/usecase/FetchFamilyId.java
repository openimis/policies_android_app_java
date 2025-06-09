package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetFamilyIdQuery;
import org.openimis.imispolicies.GetFamilyQuery;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.domain.utils.IdUtils;
import org.openimis.imispolicies.network.request.GetFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.GetFamilyIdGraphQLRequest;
import org.openimis.imispolicies.network.util.Mapper;

import java.util.Objects;

public class FetchFamilyId {

    @NonNull
    private final GetFamilyIdGraphQLRequest getFamilyIdGraphQLRequest;

    public FetchFamilyId() {
        this(new GetFamilyIdGraphQLRequest());
    }

    public FetchFamilyId(@NonNull GetFamilyIdGraphQLRequest getFamilyIdGraphQLRequest) {
        this.getFamilyIdGraphQLRequest = getFamilyIdGraphQLRequest;
    }

    @WorkerThread
    @NonNull
    public Family execute() throws Exception {
        GetFamilyIdQuery.Node node = getFamilyIdGraphQLRequest.get();
        return new Family(
                /* chfid = */ node.headInsuree().chfId(),
                /* id = */ IdUtils.getIdFromGraphQLString(node.id()),
                /* uuid = */ node.uuid(),
                null,
                null,
                false,
                /* type = */ null,
                /* address = */ null,
                /* ethnicity = */ null,
                null,
                null,
                true,
                null,
                null,
                /* insurees = */ null,
                null,
                null
                );
    }
}
