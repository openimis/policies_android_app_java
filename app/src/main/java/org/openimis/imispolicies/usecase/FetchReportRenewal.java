package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.domain.entity.Report;

import java.util.Date;

public class FetchReportRenewal {

    public FetchReportRenewal(){
    }

    @WorkerThread
    @NonNull
    public Report.Renewal execute(
            @Nullable Date fromDate,
            @Nullable Date toDate
        ) throws Exception {
        // This information is normally found in the table `tblFromPhone` but is not exposed.
        // There is no information we can retrieve that I could find to fill this.
        return new Report.Renewal(
                /* renewalSent = */ -1,
                /* renewalAccepted = */ -1
        );
    }
}
