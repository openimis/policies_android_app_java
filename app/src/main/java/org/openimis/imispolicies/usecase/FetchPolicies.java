package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openimis.imispolicies.GetPoliciesQuery;
import org.openimis.imispolicies.network.request.GetFamilyPoliciesGraphQLRequest;
import org.openimis.imispolicies.util.DateUtils;

import java.util.List;
import java.util.Objects;

public class FetchPolicies {

    @NonNull
    private final GetFamilyPoliciesGraphQLRequest getFamilyPoliciesGraphQLRequest;

    public FetchPolicies(){ this(new GetFamilyPoliciesGraphQLRequest());}

    public FetchPolicies(@NonNull GetFamilyPoliciesGraphQLRequest getFamilyPoliciesGraphQLRequest){
        this.getFamilyPoliciesGraphQLRequest = getFamilyPoliciesGraphQLRequest;
    }

    @WorkerThread
    @NonNull
    public JSONArray execute (@NonNull String familyUuid) throws Exception {
        List<GetPoliciesQuery.Edge> edges = getFamilyPoliciesGraphQLRequest.get(familyUuid);
        JSONArray array = new JSONArray();
        for(GetPoliciesQuery.Edge edge : edges){
            GetPoliciesQuery.Node node = Objects.requireNonNull(edge.node());
            JSONObject policyObject = new JSONObject();
            policyObject.put("PolicyId",node.policyId());
            policyObject.put("PolicyUuid",node.policyUuid());
            policyObject.put("EnrollDate",node.enrollDate() != null ? DateUtils.toDateString(Objects.requireNonNull(node.enrollDate())) : null);
            policyObject.put("StartDate",node.startDate() != null ? DateUtils.toDateString(Objects.requireNonNull(node.startDate())) : null);
            policyObject.put("EffectiveDate", node.effectiveDate() != null ? DateUtils.toDateString(Objects.requireNonNull(node.effectiveDate())): null);
            policyObject.put("ExpiryDate", node.expiryDate() != null ? DateUtils.toDateString(Objects.requireNonNull(node.expiryDate())): null);
            policyObject.put("SigningDate",node.signatureDate() != null ? DateUtils.toDateString(Objects.requireNonNull(node.signatureDate())): null);
            policyObject.put("PolicyStatus",node.status());
            policyObject.put("PolicyValue",node.policyValue());
            policyObject.put("ProductCode",node.productCode());
            policyObject.put("ContributionPlanCode",node.contributionPlanCode());
            policyObject.put("OfficerCode",node.officerCode());
            policyObject.put("IsOffline",false);
            policyObject.put("Periodicity",node.periodicity());
            policyObject.put("PaymentDay",node.paymentDay());
            array.put(policyObject);
        }
        return array;
    }
}
