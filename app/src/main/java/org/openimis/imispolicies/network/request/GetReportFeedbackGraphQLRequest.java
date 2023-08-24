package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.GetReportFeedbackQuery;
import org.openimis.imispolicies.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Objects;

public class GetReportFeedbackGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public GetReportFeedbackQuery.Data get(
            @Nullable Date fromDate,
            @Nullable Date toDate
    ) throws Exception {
        Response<GetReportFeedbackQuery.Data> response = makeSynchronous(new GetReportFeedbackQuery(
                Input.fromNullable(fromDate),
                Input.fromNullable(toDate)
        ));
        if (response.hasErrors()) {
            String details = response.getErrors().get(0).getMessage();
            if (details.equals("User not authorized for this operation")) {
                throw new HttpException(
                        HttpURLConnection.HTTP_UNAUTHORIZED,
                        details,
                        null,
                        null
                );
            }
             throw new RuntimeException(response.toString());
        }
        return Objects.requireNonNull(response.getData());
    }
}
