package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.request.MobileEnrolmentGraphQLRequest;

import java.util.List;

public class UpdateFamily {

    @NonNull
    private final CheckMutation checkMutation;
    @NonNull
    private final MobileEnrolmentGraphQLRequest mobileEnrolmentGraphQLRequest;

    public UpdateFamily() {
        this(new CheckMutation(), new MobileEnrolmentGraphQLRequest());
    }

    public UpdateFamily(
            @NonNull CheckMutation checkMutation,
            @NonNull MobileEnrolmentGraphQLRequest mobileEnrolmentGraphQLRequest
    ) {
        this.checkMutation = checkMutation;
        this.mobileEnrolmentGraphQLRequest = mobileEnrolmentGraphQLRequest;
    }

    @WorkerThread
    public void execute(@NonNull Family family, @NonNull List<Family.Policy> policies) throws Exception {
        checkMutation.execute(mobileEnrolmentGraphQLRequest.execute(family, policies), "Error while calling mobile enrolment for family '" + family.getId() + "'");
    }
}
