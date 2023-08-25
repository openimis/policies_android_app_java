package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetReportFeedbackQuery;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.domain.entity.Report;
import org.openimis.imispolicies.network.request.GetReportFeedbackGraphQLRequest;

import java.util.Date;
import java.util.Objects;

public class FetchReportFeedback {

    @NonNull
    private final Global global;
    @NonNull
    private final GetReportFeedbackGraphQLRequest getReportFeedbackGraphQLRequest;

    public FetchReportFeedback(){
        this(Global.getGlobal(), new GetReportFeedbackGraphQLRequest());
    }

    public FetchReportFeedback(
            @NonNull Global global,
            @NonNull GetReportFeedbackGraphQLRequest getReportFeedbackGraphQLRequest
    ) {
        this.global = global;
        this.getReportFeedbackGraphQLRequest = getReportFeedbackGraphQLRequest;
    }



    @WorkerThread
    @NonNull
    public Report.Feedback execute(
            @Nullable Date fromDate,
            @Nullable Date toDate
        ) throws Exception {
        return execute(global.requireOfficerCode(), fromDate, toDate);
    }

    @WorkerThread
    @NonNull
    public Report.Feedback execute(
            @NonNull String officerCode,
            @Nullable Date fromDate,
            @Nullable Date toDate
    ) throws Exception {
        // This information is normally found in the table `tblFromPhone` but is not exposed.
        // This is a best effort to provide some information.
        GetReportFeedbackQuery.Data data = getReportFeedbackGraphQLRequest.get(officerCode, fromDate, toDate);
        return new Report.Feedback(
                /* feedbackSent = */ -1,
                /* feedbackAccepted = */ Objects.requireNonNullElse(data.delivered().totalCount(), -1)
        );
    }
}
