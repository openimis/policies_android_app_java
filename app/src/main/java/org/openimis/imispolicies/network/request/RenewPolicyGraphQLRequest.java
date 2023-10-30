package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.RenewPolicyMutation;
import org.openimis.imispolicies.type.RenewPolicyMutationInput;

public class RenewPolicyGraphQLRequest extends BaseGraphQLRequest {

    @WorkerThread
    public void execute(@NonNull String uuid) throws Exception {
        makeSynchronous(new RenewPolicyMutation(RenewPolicyMutationInput.builder()
                .uuid(uuid)
                .build()));
    }
}
