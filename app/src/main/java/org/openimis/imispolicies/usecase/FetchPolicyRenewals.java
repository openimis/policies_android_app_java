package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetPolicyRenewalsQuery;
import org.openimis.imispolicies.GetRenewalsQuery;
import org.openimis.imispolicies.domain.entity.PolicyRenewal;
import org.openimis.imispolicies.domain.utils.IdUtils;
import org.openimis.imispolicies.network.request.GetPolicyRenewalsGraphQLRequest;
import org.openimis.imispolicies.network.util.Mapper;

import java.util.List;
import java.util.Objects;

public class FetchPolicyRenewals {

    @NonNull
    private final GetPolicyRenewalsGraphQLRequest getPolicyRenewalsGraphQLRequest;

    public FetchPolicyRenewals() {
        this(new GetPolicyRenewalsGraphQLRequest());
    }

    public FetchPolicyRenewals(@NonNull GetPolicyRenewalsGraphQLRequest getPolicyRenewalsGraphQLRequest) {
        this.getPolicyRenewalsGraphQLRequest = getPolicyRenewalsGraphQLRequest;
    }

    @NonNull
    @WorkerThread
    public List<PolicyRenewal> execute(String officerCode) throws Exception {
        List<GetPolicyRenewalsQuery.Edge> edges = getPolicyRenewalsGraphQLRequest.get(officerCode);
        return Mapper.map(edges, this::toRenewal);
    }

    @NonNull
    private PolicyRenewal toRenewal(@NonNull GetPolicyRenewalsQuery.Edge edge) {
        GetPolicyRenewalsQuery.Node node = Objects.requireNonNull(edge.node());
        return new PolicyRenewal(
                /* id = */ IdUtils.getIdFromGraphQLString(node.id()),
                /* uuid = */ node.uuid(),
                /* policyId = */ IdUtils.getIdFromGraphQLString(node.id()),
                /* officerId = */ IdUtils.getIdFromGraphQLString(node.officer().id()),
                /* officerCode = */ node.officer().code(),
                /* chfId = */ node.family().headInsuree().chfId(),
                /* lastName = */ node.family().headInsuree().lastName(),
                /* otherNames = */ node.family().headInsuree().otherNames(),
                /* productCode = */ node.product().code(),
                /* productName = */ node.product().name(),
                /* villageName = */ node.family().headInsuree().currentVillage() != null ? node.family().headInsuree().currentVillage().name() : null,
                /* renewalPromptDate = */ node.startDate(),
                /* phone = */ node.family().headInsuree().phone()
        );
    }
}
