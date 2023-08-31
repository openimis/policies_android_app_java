package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.ToRestApi;
import org.openimis.imispolicies.domain.PolicyRenewal;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.DeletePolicyRenewalGraphQLRequest;

import java.net.HttpURLConnection;
import java.util.List;

public class DeletePolicyRenewal {

    @NonNull
    private final FetchPolicyRenewals fetchPolicyRenewals;
    @NonNull
    private final DeletePolicyRenewalGraphQLRequest deletePolicyRenewalGraphQLRequest;

    public DeletePolicyRenewal() {
        this(
                new FetchPolicyRenewals(),
                new DeletePolicyRenewalGraphQLRequest()
        );
    }

    public DeletePolicyRenewal(
            @NonNull FetchPolicyRenewals fetchPolicyRenewals,
            @NonNull DeletePolicyRenewalGraphQLRequest deletePolicyRenewalGraphQLRequest
    ) {
        this.fetchPolicyRenewals = fetchPolicyRenewals;
        this.deletePolicyRenewalGraphQLRequest = deletePolicyRenewalGraphQLRequest;
    }

    @WorkerThread
    public int execute(int id) throws Exception {
        List<PolicyRenewal> renewals = fetchPolicyRenewals.execute();
        for (PolicyRenewal renewal : renewals) {
            if (renewal.getId() == id) {
                return execute(renewal.getUuid());
            }
        }
        throw new HttpException(HttpURLConnection.HTTP_NOT_FOUND, "Renewal with id '"+id+"' doesn't exists", null, null);
    }
    @WorkerThread
    public int execute(@NonNull String uuid) throws Exception {
        deletePolicyRenewalGraphQLRequest.delete(uuid);
        return ToRestApi.RenewalStatus.ACCEPTED;
    }
}
