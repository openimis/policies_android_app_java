package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.DeleteRenewalMutation;
import org.openimis.imispolicies.type.DeletePolicyRenewalsMutationInput;

import java.util.List;

public class DeletePolicyRenewalGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    public void delete(@NonNull String uuid) throws Exception {
        makeSynchronous(new DeleteRenewalMutation(
                DeletePolicyRenewalsMutationInput.builder().uuids(List.of(uuid)).build()
        ));
    }
}
