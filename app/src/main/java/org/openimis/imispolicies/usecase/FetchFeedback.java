package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetFeedbackRequestsQuery;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.domain.entity.FeedbackRequest;
import org.openimis.imispolicies.network.request.GetFeedbackRequestsGraphQLRequest;
import org.openimis.imispolicies.network.util.Mapper;

import java.util.List;
import java.util.Objects;

public class FetchFeedback {

    @NonNull
    private final Global global;
    @NonNull
    private final GetFeedbackRequestsGraphQLRequest getFeedbackRequestsGraphQLRequest;

    public FetchFeedback() {
        this(Global.getGlobal(), new GetFeedbackRequestsGraphQLRequest());
    }

    public FetchFeedback(
            @NonNull Global global,
            @NonNull GetFeedbackRequestsGraphQLRequest getFeedbackRequestsGraphQLRequest
    ) {
        this.getFeedbackRequestsGraphQLRequest = getFeedbackRequestsGraphQLRequest;
        this.global = global;
    }

    @WorkerThread
    public List<FeedbackRequest> execute() throws Exception {
        return execute(global.requireOfficerCode());
    }

    @WorkerThread
    public List<FeedbackRequest> execute(@NonNull String officerCode) throws Exception {
        List<GetFeedbackRequestsQuery.Edge> nodes = getFeedbackRequestsGraphQLRequest.get(officerCode);
        return Mapper.map(nodes, this::toFeedback);
    }

    @NonNull
    private FeedbackRequest toFeedback(@NonNull GetFeedbackRequestsQuery.Edge edge) {
        GetFeedbackRequestsQuery.Node node = Objects.requireNonNull(edge.node());
        return new FeedbackRequest(
                /* chfId = */ Objects.requireNonNull(node.insuree().chfId()),
                /* officeId = */ -1, // This is not returned by GraphQL
                /* officerCode = */ Objects.requireNonNull(Objects.requireNonNull(node.admin()).code()),
                /* lastName = */ node.insuree().lastName(),
                /* otherNames = */ node.insuree().otherNames(),
                /* hfCode = */ node.healthFacility().code(),
                /* hfName = */ node.healthFacility().name(),
                /* claimCode = */ node.code(),
                /* claimUUID = */ node.uuid(),
                /* promptDate = */ Objects.requireNonNull(
                Objects.requireNonNull(node.insuree().claimSet().edges().get(0).node()).dateTo()
        ),
                /* fromDate = */ node.dateFrom(),
                /* toDate = */ node.dateTo(),
                /* phone = */ Objects.requireNonNull(node.admin()).phone()
        );
    }
}
