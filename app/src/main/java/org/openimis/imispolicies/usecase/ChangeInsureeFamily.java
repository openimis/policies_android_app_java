package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.network.request.ChangeInsureeGraphQLRequest;

public class ChangeInsureeFamily {

    @NonNull
    private final ChangeInsureeGraphQLRequest changeInsureeGraphQLRequest;

    @NonNull
    private final CheckMutation checkMutation;

    public ChangeInsureeFamily () {
        this(
                new ChangeInsureeGraphQLRequest(),
                new CheckMutation()
        );
    }

    public ChangeInsureeFamily(
            @NonNull ChangeInsureeGraphQLRequest changeInsureeGraphQLRequest,
            @NonNull CheckMutation checkMutation
    ){
        this.changeInsureeGraphQLRequest = changeInsureeGraphQLRequest;
        this.checkMutation = checkMutation;
    }

    @WorkerThread
    public void execute (@NonNull String subFamilyUuid, @NonNull String headInsureeUuid) throws Exception {
        checkMutation.execute(
                changeInsureeGraphQLRequest.execute(subFamilyUuid, headInsureeUuid),
                "Erreur lors du déplacement du chef de ménage"
        );
    }
}
