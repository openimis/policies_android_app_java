package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.domain.entity.PendingFeedback;
import org.openimis.imispolicies.network.exception.HttpException;

import java.net.HttpURLConnection;
import java.util.Map;

public class PostNewFeedbackRequest extends BaseFHIRPostRequest<PendingFeedback, Void> {

    public PostNewFeedbackRequest() {
        super("Communication");
    }

    @NonNull
    @Override
    public Void post(PendingFeedback object, @Nullable Map<String, String> queryParameters) throws Exception {
        try {
            return super.post(object, queryParameters);
        } catch (HttpException e) {
            if (e.getCode() == 500 && e.getBody() != null) {
                try {
                    // Error handling is not super good. The backend returns 500 even
                    // when it should not. We are trying our best to detect what the real issue is.
                    JSONObject errorBody = new JSONObject(e.getBody());
                    String details = errorBody.getJSONArray("issue")
                            .getJSONObject(0)
                            .getJSONObject("details")
                            .getString("text");
                    if (details.equals("Feedback exists for this claim")) {
                        throw new HttpException(
                                HttpURLConnection.HTTP_CONFLICT,
                                details,
                                e.getBody(),
                                e
                        );
                    }
                    if (details.contains("validation error")) {
                        throw new HttpException(
                                HttpURLConnection.HTTP_BAD_REQUEST,
                                "One or more field is missing",
                                e.getBody(),
                                e
                        );
                    }
                    if (details.equals("'NoneType' object has no attribute 'rsplit'")) {
                        throw new HttpException(
                                HttpURLConnection.HTTP_BAD_REQUEST,
                                "Claim reference doesn't match Claim/<UUID>",
                                e.getBody(),
                                e
                        );
                    }
                    if (details.equals("about is required") || details.equals("payload must have 5 elements")) {
                        throw new HttpException(
                                HttpURLConnection.HTTP_BAD_REQUEST,
                                details,
                                e.getBody(),
                                e
                        );
                    }

                } catch (JSONException ignored) {
                }
            }
            throw e;
        }
    }

    @NonNull
    @Override
    protected Void fromJson(@NonNull JSONObject object) {
        return null;
    }

    @NonNull
    @Override
    protected JSONObject toJson(PendingFeedback object) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resourceType", "Communication");
        jsonObject.put("status", "completed");
        JSONObject about = new JSONObject();
        about.put("reference", "Claim/" + object.getClaimUUID());
        jsonObject.put("about", wrapInArray(about));
        jsonObject.put("subject", code("Patient", object.getChfId()));
        JSONArray payload = new JSONArray();
        payload.put(answer("CareRendered", object.wasCareRendered()));
        payload.put(answer("PaymentAsked", object.wasPaymentAsked()));
        payload.put(answer("DrugPrescribed", object.wasDrugPrescribed()));
        payload.put(answer("DrugReceived", object.wasDrugReceived()));
        // There is a typo in the JSON for "Assessment", we are evaluating if this should be fixed
        payload.put(answer("Asessment", String.valueOf(object.getAssessment())));
        jsonObject.put("payload", payload);
        return jsonObject;
    }

    protected JSONObject answer(@NonNull String type, boolean value) throws JSONException {
        return answer(type, value ? "yes" : "no");
    }

    protected JSONObject answer(@NonNull String type, @NonNull String value) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject extension = new JSONObject();
        extension.put("url", "https://openimis.github.io/openimis_fhir_r4_ig/StructureDefinition/communication-payload-type");
        JSONObject concept = new JSONObject();
        JSONObject coding = new JSONObject();
        coding.put("system", "https://openimis.github.io/openimis_fhir_r4_ig/CodeSystem/feedback-payload");
        coding.put("code", type);
        concept.put("coding", wrapInArray(coding));
        extension.put("valueCodeableConcept", concept);
        jsonObject.put("extension", wrapInArray(extension));
        jsonObject.put("contentString", value);
        return jsonObject;
    }

    @NonNull
    private JSONObject code(@NonNull String type, @NonNull String code) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", type);
        JSONObject identifier = new JSONObject();
        identifier.put("value", code);
        JSONObject jsonType = new JSONObject();
        JSONObject coding = new JSONObject();
        coding.put("system", "https://openimis.github.io/openimis_fhir_r4_ig/CodeSystem/openimis-identifiers");
        coding.put("code", "Code");
        jsonType.put("coding", wrapInArray(coding));
        identifier.put("type", jsonType);
        json.put("identifier", identifier);
        return json;
    }

    @NonNull
    private JSONArray wrapInArray(@NonNull JSONObject jsonObject) {
        JSONArray array = new JSONArray();
        array.put(jsonObject);
        return array;
    }
}
