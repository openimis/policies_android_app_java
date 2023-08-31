package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.openimis.imispolicies.GetRenewalsQuery;
import org.openimis.imispolicies.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Objects;

public class GetPolicyRenewalsGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    @NonNull
    public List<GetRenewalsQuery.Edge> get() throws Exception {
        Response<GetRenewalsQuery.Data> response = makeSynchronous(new GetRenewalsQuery());
        GetRenewalsQuery.Data data = response.getData();
        if (data == null || data.policyRenewals() == null) {
            throw new HttpException(HttpURLConnection.HTTP_NOT_FOUND, "No renewals found", null, null);
        }
        return Objects.requireNonNull(data.policyRenewals()).edges();
    }
}
