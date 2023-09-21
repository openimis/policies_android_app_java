package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.PendingFeedback;
import org.openimis.imispolicies.network.request.PostNewFeedbackRequest;

public class PostFeedback {

    @NonNull
    private final PostNewFeedbackRequest postNewFeedbackRequest;

    public PostFeedback() {
        this(new PostNewFeedbackRequest());
    }

    public PostFeedback(@NonNull PostNewFeedbackRequest postNewFeedbackRequest) {
        this.postNewFeedbackRequest = postNewFeedbackRequest;
    }

    @WorkerThread
    public void execute(@NonNull PendingFeedback pendingFeedback) throws Exception {
        postNewFeedbackRequest.post(pendingFeedback);
    }
}
