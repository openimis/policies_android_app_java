package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetReportFeedbackQuery;
import org.openimis.imispolicies.domain.entity.Report;
import org.openimis.imispolicies.network.request.GetReportFeedbackGraphQLRequest;

import java.util.Date;
import java.util.Objects;

public class FetchReportFeedback {

    @NonNull
    private final GetReportFeedbackGraphQLRequest getReportFeedbackGraphQLRequest;

    public FetchReportFeedback(){
        this(new GetReportFeedbackGraphQLRequest());
    }

    public FetchReportFeedback(@NonNull GetReportFeedbackGraphQLRequest getReportFeedbackGraphQLRequest) {
        this.getReportFeedbackGraphQLRequest = getReportFeedbackGraphQLRequest;
    }

    @WorkerThread
    @NonNull
    public Report.Feedback execute(
            @Nullable Date fromDate,
            @Nullable Date toDate
        ) throws Exception {
        // This information is normally found in the table `tblFromPhone` but is not exposed.
        // This is a best effort to provide some information.
        GetReportFeedbackQuery.Data data = getReportFeedbackGraphQLRequest.get(fromDate, toDate);
        return new Report.Feedback(
                /* feedbackSent = */ -1,
                /* feedbackAccepted = */ Objects.requireNonNullElse(data.delivered().totalCount(), -1)
        );
    }
}
