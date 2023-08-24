package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetFeedbacksQuery;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.domain.entity.FeedbackRequest;
import org.openimis.imispolicies.network.request.GetFeedbackGraphQLRequest;
import org.openimis.imispolicies.network.util.Mapper;

import java.util.List;
import java.util.Objects;

public class FetchFeedback {

    @NonNull
    private final GetFeedbackGraphQLRequest getFeedbackGraphQLRequest;

    public FetchFeedback() {
        this(new GetFeedbackGraphQLRequest());
    }

    public FetchFeedback(@NonNull GetFeedbackGraphQLRequest getFeedbackGraphQLRequest) {
        this.getFeedbackGraphQLRequest = getFeedbackGraphQLRequest;
    }

    @WorkerThread
    public List<FeedbackRequest> execute() throws Exception {
        String officerCode = Global.getGlobal().getOfficerCode();
        if (officerCode == null) {
            throw new IllegalStateException("OfficerCode is null");
        }
        return execute(officerCode);
    }

    @WorkerThread
    public List<FeedbackRequest> execute(@NonNull String officerCode) throws Exception {
        List<GetFeedbacksQuery.Edge> nodes = getFeedbackGraphQLRequest.get(officerCode);
        return Mapper.map(nodes, this::toFeedback);
    }

    @NonNull
    private FeedbackRequest toFeedback(@NonNull GetFeedbacksQuery.Edge edge) {
        GetFeedbacksQuery.Node node = Objects.requireNonNull(edge.node());
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
