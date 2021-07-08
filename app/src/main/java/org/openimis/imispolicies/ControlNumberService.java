package org.openimis.imispolicies;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ControlNumberService extends IntentService {

    private static final String ACTION_REQUEST_CN = "ControlNumberService.ACTION_REQUEST_CN";
    private static final String ACTION_GET_ASSIGNED_CN = "ControlNumberService.ACTION_GET_ASSIGNED_CN";

    private static final String FIELD_PAYLOAD = "FIELD_PAYLOAD";
    private static final String FIELD_PAYMENT_DETAILS = "FIELD_PAYMENT_DETAILS";

    public static final String ACTION_REQUEST_SUCCESS = "ControlNumberService.ACTION_REQUEST_SUCCESS";
    public static final String ACTION_REQUEST_ERROR = "ControlNumberService.ACTION_REQUEST_ERROR";

    public static final String FIELD_ERROR_MESSAGE = "FIELD_ERROR_MESSAGE";

    private ToRestApi toRestApi;
    private ClientAndroidInterface ca;

    public ControlNumberService() {
        super("ControlNumberService");
    }

    public static void requestControlNumber(Context context, final JSONObject order, final JSONArray paymentDetails) {
        Intent intent = new Intent(context, ControlNumberService.class);
        intent.setAction(ACTION_REQUEST_CN);
        intent.putExtra(FIELD_PAYLOAD, order.toString());
        intent.putExtra(FIELD_PAYMENT_DETAILS, paymentDetails.toString());
        context.startService(intent);
    }

    public static void getAssignedControlNumber(Context context, final JSONArray order) {
        Intent intent = new Intent(context, ControlNumberService.class);
        intent.setAction(ACTION_GET_ASSIGNED_CN);
        intent.putExtra(FIELD_PAYLOAD, order.toString());
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        toRestApi = new ToRestApi();
        ca = new ClientAndroidInterface(this);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REQUEST_CN.equals(action)) {
                final String order = intent.getStringExtra(FIELD_PAYLOAD);
                final String details = intent.getStringExtra(FIELD_PAYMENT_DETAILS);
                handleActionRequestCN(order, details);
            } else if (ACTION_GET_ASSIGNED_CN.equals(action)) {
                final String order = intent.getStringExtra(FIELD_PAYLOAD);
                handleActionGetAssignedCN(order);
            }
        }
    }

    private void handleActionRequestCN(final String order, final String details) {
        String errorMessage;
        try {
            JSONObject orderJson = new JSONObject(order);
            JSONArray detailsJson = new JSONArray(details);

            HttpEntity respEntity;
            HttpResponse response = toRestApi.postToRestApiToken(orderJson, "GetControlNumber");
            int responseCode = response.getStatusLine().getStatusCode();
            respEntity = response.getEntity();
            String content = EntityUtils.toString(respEntity);

            JSONObject responseContent = new JSONObject(content);
            errorMessage = getErrorMessage(responseCode, responseContent);

            if ("".equals(errorMessage)) {
                String internalIdentifier = responseContent.getString("internal_identifier");
                String controlNumber = responseContent.getString("control_number");
                int id = insertControlNumber(orderJson, controlNumber, internalIdentifier);
                updateRecordedPoliciesAfterRequest(detailsJson, id);

                broadcastSuccess();
            } else {
                broadcastError(errorMessage);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            broadcastError(getResources().getString(R.string.SomethingWrongServer));
        }
    }

    private void handleActionGetAssignedCN(final String order) {
        String errorMessage;
        try {
            JSONObject orderJson = new JSONObject().put("requests", new JSONArray(order));

            HttpEntity respEntity;
            HttpResponse response = toRestApi.postToRestApiToken(orderJson, "GetAssignedControlNumbers");
            int responseCode = response.getStatusLine().getStatusCode();
            respEntity = response.getEntity();
            String content = EntityUtils.toString(respEntity);

            JSONObject responseContent = new JSONObject(content);
            errorMessage = getErrorMessage(responseCode, responseContent);
            if ("".equals(errorMessage)) {
                String assignedControlNumbers = responseContent.getString("assigned_control_numbers");
                updateAssignedControlNumbers(assignedControlNumbers);

                broadcastSuccess();
            } else {
                broadcastError(errorMessage);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            broadcastError(getResources().getString(R.string.SomethingWrongServer));
        }
    }

    private void broadcastSuccess() {
        Intent successIntent = new Intent(ACTION_REQUEST_SUCCESS);
        sendBroadcast(successIntent);
    }

    private void broadcastError(String errorMessage) {
        Intent errorIntent = new Intent(ACTION_REQUEST_ERROR);
        errorIntent.putExtra(FIELD_ERROR_MESSAGE, errorMessage);
        sendBroadcast(errorIntent);
    }

    private String getErrorMessage(int responseCode, JSONObject responseContent) throws JSONException {
        String errorMessage;
        if ("true".equals(responseContent.getString("error_occured"))) {
            errorMessage = responseContent.getString("error_message");
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            errorMessage = getResources().getString(R.string.has_no_rights);
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            errorMessage = getResources().getString(R.string.SomethingWrongServer);
        } else if (responseCode >= 400) { // for compatibility, but should not be needed
            errorMessage = getResources().getString(R.string.SomethingWrongServer);
        } else {
            errorMessage = "";
        }
        return errorMessage;
    }

    private int insertControlNumber(JSONObject order, String controlNumber, String internalIdentifier) throws JSONException {
        return ca.insertControlNumber(
                order.getString("amount_to_be_paid"),
                order.getString("amount_to_be_paid"), //TODO no separation between calculated and confirmed amount for now
                controlNumber,
                internalIdentifier,
                order.getString("type_of_payment"),
                order.getString("SmsRequired"));
    }

    private void updateRecordedPoliciesAfterRequest(JSONArray paymentDetails, int ControlNumberId) throws JSONException {
        JSONObject ob;
        for (int j = 0; j < paymentDetails.length(); j++) {
            ob = paymentDetails.getJSONObject(j);
            int Id = Integer.parseInt(ob.getString("Id"));
            ca.updateRecordedPolicy(Id, ControlNumberId);
        }
    }

    private void updateAssignedControlNumbers(String assignedControlNumbers) throws JSONException {
        JSONObject ob;
        JSONArray arr = new JSONArray(assignedControlNumbers);
        for (int j = 0; j < arr.length(); j++) {
            ob = arr.getJSONObject(j);
            String internalIdentifier = ob.getString("internal_identifier");
            String controlNumber = ob.getString("control_number");
            ca.assignControlNumber(internalIdentifier, controlNumber);
        }
    }
}