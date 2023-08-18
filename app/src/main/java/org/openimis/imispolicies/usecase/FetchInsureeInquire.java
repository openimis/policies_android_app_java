package org.openimis.imispolicies.usecase;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetInsureeInquireQuery;
import org.openimis.imispolicies.domain.entity.Insuree;
import org.openimis.imispolicies.domain.entity.Policy;
import org.openimis.imispolicies.network.request.GetInsureeInquireGraphQLRequest;
import org.openimis.imispolicies.network.util.Mapper;

import java.util.List;
import java.util.Objects;

public class FetchInsureeInquire {

    @NonNull
    private final GetInsureeInquireGraphQLRequest request;

    public FetchInsureeInquire() {
        this(new GetInsureeInquireGraphQLRequest());
    }

    public FetchInsureeInquire(@NonNull GetInsureeInquireGraphQLRequest request) {
        this.request = request;
    }

    @NonNull
    @WorkerThread
    public Insuree execute(@NonNull String chfId) throws Exception {
        GetInsureeInquireQuery.Node node = request.get(chfId);
        return new Insuree(
                /* chfId = */ Objects.requireNonNull(node.chfId()),
                /* name = */ node.lastName() + " " + node.otherNames(),
                /* dateOfBirth = */ node.dob(),
                /* gender = */ node.gender() != null ? node.gender().gender() : null,
                /* photoPath = */ getPhotoPath(node.photos()),
                /* photo = */ getPhotoBytes(node.photos()),
                /* policies = */ Mapper.map(node.insureePolicies().edges(), this::toPolicy)
        );
    }

    @Nullable
    private String getPhotoPath(@NonNull List<GetInsureeInquireQuery.Photo> photos) {
        for (GetInsureeInquireQuery.Photo photo : photos) {
            String filename = photo.filename();
            if (filename != null) {
                String folder = photo.folder();
                if (folder != null) {
                    folder = folder.replace('\\', '/');
                    if (!folder.endsWith("/")) {
                        folder += "/";
                    }
                    return folder + filename;
                }
                return filename;
            }
        }

        return null;
    }

    @Nullable
    private byte[] getPhotoBytes(@NonNull List<GetInsureeInquireQuery.Photo> photos) {
        for (GetInsureeInquireQuery.Photo photo : photos) {
            String photoBase64 = photo.photo();
            if (photoBase64 != null) {
                return Base64.decode(photoBase64, Base64.DEFAULT);
            }
        }

        return null;
    }

    @NonNull
    private Policy toPolicy(@NonNull GetInsureeInquireQuery.Edge1 edge) {
        GetInsureeInquireQuery.Policy policy = Objects.requireNonNull(edge.node()).policy();
        GetInsureeInquireQuery.Product product = policy.product();
        return new Policy(
                /* code = */ product.code(),
                /* name = */ product.name(),
                /* value = */ policy.value(),
                /* expiryDate = */ policy.expiryDate(),
                /* status = */ intAsStatus(policy.status()),
                /* deductibleType = */ product.deductible(),
                /* deductibleIp = */ product.deductibleIp(),
                /* deductibleOp = */ product.deductibleOp(),
                /* ceilingIp = */ product.ceilingIp(),
                /* ceilingOp = */ product.ceilingOp(),
                /* antenatalAmountLeft = */ product.maxAmountAntenatal(),
                /* consultationAmountLeft = */ product.maxAmountConsultation(),
                /* deliveryAmountLeft = */ product.maxAmountDelivery(),
                /* hospitalizationAmountLeft = */ product.maxAmountHospitalization(),
                /* surgeryAmountLeft = */ product.maxAmountSurgery(),
                /* totalAdmissionsLeft = */ product.maxNoHospitalization(),
                /* totalAntenatalLeft = */ product.maxNoAntenatal(),
                /* totalConsultationsLeft = */ product.maxNoConsultation(),
                /* totalDeliveriesLeft = */ product.maxNoDelivery(),
                /* totalSurgeriesLeft = */ product.maxNoSurgery(),
                /* totalVisitsLeft = */ product.maxNoVisits()
        );
    }

    /**
     * <a href="https://github.com/openimis/database_ms_sqlserver/blob/main/sql/stored_procedures/uspAPIGetCoverage.sql#L48C3-L48C101">Values for the status in the stored procedure</a>
     */
    @NonNull
    private Policy.Status intAsStatus(@Nullable Integer integer) {
        if (integer == null) {
            return Policy.Status.EXPIRED;
        }
        switch (integer) {
            case 1: return Policy.Status.IDLE;
            case 2: return Policy.Status.ACTIVE;
            case 4: return Policy.Status.SUSPENDED;
            //case 8: return Policy.Status.EXPIRED; <-- Same as default
            case 16: return Policy.Status.READY;
            default:
                return Policy.Status.EXPIRED;
        }
    }
}
