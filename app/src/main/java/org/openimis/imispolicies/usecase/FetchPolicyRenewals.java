package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetRenewalsQuery;
import org.openimis.imispolicies.domain.PolicyRenewal;
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
    public List<PolicyRenewal> execute() throws Exception {
        List<GetRenewalsQuery.Edge> edges = getPolicyRenewalsGraphQLRequest.get();
        return Mapper.map(edges, this::toRenewal);
    }

    @NonNull
    private PolicyRenewal toRenewal(@NonNull GetRenewalsQuery.Edge edge) {
        GetRenewalsQuery.Node node = Objects.requireNonNull(edge.node());
        return new PolicyRenewal(
                /* id = */ node.id() != null ? IdUtils.getIdFromGraphQLString(node.id()) : 0,
                /* uuid = */ node.uuid(),
                /* policyId = */ node.policy() != null && node.policy().id() != null ? IdUtils.getIdFromGraphQLString(node.policy().id()) : 0,
                /* officerId = */ node.policy() != null && node.policy().officer() != null && node.policy().officer().id() != null ? IdUtils.getIdFromGraphQLString(node.policy().officer().id()) : 0,
                /* officerCode = */ node.policy().officer().code(),
                /* chfId = */ node.insuree().chfId(),
                /* lastName = */ node.insuree().lastName(),
                /* otherNames = */ node.insuree().otherNames(),
                /* productCode = */ node.policy().product().code(),
                /* productName = */ node.policy().product().name(),
                /* villageName = */ node.insuree().currentVillage() != null ? node.insuree().currentVillage().name() : null,
                /* renewalPromptDate = */ node.renewalPromptDate(),
                /* phone = */ node.insuree().phone()
        );
    }
}
