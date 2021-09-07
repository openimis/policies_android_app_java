package org.openimis.imispolicies;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ControlNumberService extends IntentService {
    private static final String LOG_TAG = "CNSERVICE";

    private static final String ACTION_REQUEST_CN = "ControlNumberService.ACTION_REQUEST_CN";
    private static final String ACTION_GET_ASSIGNED_CN = "ControlNumberService.ACTION_GET_ASSIGNED_CN";
    private static final String ACTION_FETCH_BULK_CN = "ControlNumberService.FETCH_BULK_CN";

    private static final String FIELD_PRODUCT_CODE = "FIELD_PRODUCT_CODE";
    private static final String FIELD_PAYLOAD = "FIELD_PAYLOAD";
    private static final String FIELD_PAYMENT_DETAILS = "FIELD_PAYMENT_DETAILS";

    public static final String ACTION_REQUEST_SUCCESS = "ControlNumberService.ACTION_REQUEST_SUCCESS";
    public static final String ACTION_REQUEST_ERROR = "ControlNumberService.ACTION_REQUEST_ERROR";

    public static final String FIELD_ERROR_MESSAGE = "FIELD_ERROR_MESSAGE";

    private ToRestApi toRestApi;
    private ClientAndroidInterface ca;
    private SQLHandler sqlHandler;

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

    public static void fetchBulkControlNumbers(Context context, String productCode) {
        Intent intent = new Intent(context, ControlNumberService.class);
        intent.setAction(ACTION_FETCH_BULK_CN);
        intent.putExtra(FIELD_PRODUCT_CODE, productCode);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        toRestApi = new ToRestApi();
        ca = new ClientAndroidInterface(this);
        sqlHandler = new SQLHandler(this);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REQUEST_CN.equals(action)) {
                final String order = intent.getStringExtra(FIELD_PAYLOAD);
                final String details = intent.getStringExtra(FIELD_PAYMENT_DETAILS);
                handleActionRequestCN(order, details);
            } else if (ACTION_GET_ASSIGNED_CN.equals(action)) {
                final String order = intent.getStringExtra(FIELD_PAYLOAD);
                handleActionGetAssignedCN(order);
            } else if (ACTION_FETCH_BULK_CN.equals(action)) {
                final String productCode = intent.getStringExtra(FIELD_PRODUCT_CODE);
                handleActionFetchBulkCN(productCode);
            }
        }
    }

    private void handleActionFetchBulkCN(String productCode) {
        String errorMessage;
        try {
            JSONObject object = new JSONObject();
            object.put("productCode", productCode);

            HttpEntity respEntity;
            HttpResponse response = toRestApi.postToRestApiToken(object, "GetControlNumbersForEO");
            int responseCode = response.getStatusLine().getStatusCode();
            respEntity = response.getEntity();
            String content = EntityUtils.toString(respEntity);
            JSONArray responseContent = new JSONArray(content);

            errorMessage = getErrorMessage(responseCode, null);
            if ("".equals(errorMessage)) {
                insertBulkControlNumbers(responseContent);
                broadcastSuccess();
            } else {
                broadcastError(errorMessage);
            }
        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, "Error during bulk CN requesting", e);
            broadcastError(getResources().getString(R.string.SomethingWrongServer));
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
            if (StringUtils.isEmpty(errorMessage)) {
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
        Log.i(LOG_TAG, String.format("ControlNumberService finished with %s", ACTION_REQUEST_SUCCESS));
    }

    private void broadcastError(String errorMessage) {
        Intent errorIntent = new Intent(ACTION_REQUEST_ERROR);
        errorIntent.putExtra(FIELD_ERROR_MESSAGE, errorMessage);
        sendBroadcast(errorIntent);
        Log.i(LOG_TAG, String.format("ControlNumberService finished with %s, error message: %s", ACTION_REQUEST_ERROR, errorMessage));
    }

    private String getErrorMessage(int responseCode, JSONObject responseContent) throws JSONException {
        String errorMessage = "";

        if (responseContent != null && responseContent.has("error_occured")) {
            if (responseContent.getBoolean("error_occured")) {
                errorMessage = responseContent.getString("error_message");
            }
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            errorMessage = getResources().getString(R.string.has_no_rights);
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            errorMessage = getResources().getString(R.string.SomethingWrongServer);
        } else if (responseCode >= 400) { // for compatibility, but should not be needed
            errorMessage = getResources().getString(R.string.SomethingWrongServer);
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

    private void insertBulkControlNumbers(JSONArray controlNumbers) throws JSONException {
        if (controlNumbers == null || controlNumbers.length() == 0) {
            Log.i(LOG_TAG, "0 numbers fetched");
        } else {
            Log.i(LOG_TAG, String.format("%d numbers fetched", controlNumbers.length()));
            for (int i = 0; i < controlNumbers.length(); i++) {
                ContentValues cv = new ContentValues();
                JSONObject object = controlNumbers.getJSONObject(i);

                cv.put("Id", object.getString("controlNumberId"));
                cv.put("BillId", object.getString("billId"));
                cv.put("ProductCode", object.getString("productCode"));
                cv.put("OfficerCode", object.getString("officerCode"));
                cv.put("ControlNUmber", object.getString("controlNumber"));
                cv.put("Amount", object.getString("amount"));

                sqlHandler.insertData(SQLHandler.tblBulkControlNumbers, cv);
            }
        }
    }
}
