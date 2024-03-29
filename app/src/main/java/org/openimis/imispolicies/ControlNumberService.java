package org.openimis.imispolicies;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;

import org.intellij.lang.annotations.Language;
import org.openimis.imispolicies.tools.Log;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.util.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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

    private static class ControlNumberStatus {
        public static final int SUCCESS = 4001;
        public static final int NO_CN_AVAILABLE = 4002;
        public static final int THRESHOLD_REACHED = 4003;
        public static final int ERROR = 4999;
    }

    public static Map<Integer, Integer> ControlNumberStatusMapping;

    static {
        ControlNumberStatusMapping = new HashMap<>();
        ControlNumberStatusMapping.put(ControlNumberStatus.NO_CN_AVAILABLE, R.string.NoCNReceived);
        ControlNumberStatusMapping.put(ControlNumberStatus.THRESHOLD_REACHED, R.string.CNThresholdReached);
        ControlNumberStatusMapping.put(ControlNumberStatus.ERROR, R.string.SomethingWrongServer);
    }

    private Global global;
    private ToRestApi toRestApi;
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
        sqlHandler = new SQLHandler(this);
        global = (Global) getApplicationContext();

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
            object.put("availableControlNumbers", sqlHandler.getFreeCNCount(global.getOfficerCode(), productCode));

            HttpResponse response = toRestApi.postToRestApiToken(object, "GetControlNumbersForEO");
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity respEntity = response.getEntity();
            String content = EntityUtils.toString(respEntity);
            JSONObject responseContent = new JSONObject(content);

            errorMessage = getErrorMessage(responseCode, responseContent);
            if (StringUtils.isEmpty(errorMessage)) {
                JSONArray controlNumbers = responseContent.getJSONArray("controlNumbers");
                insertBulkControlNumbers(controlNumbers);
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

        if (responseContent != null) {
            if (responseContent.has("header")) {
                JSONObject responseHeader = responseContent.getJSONObject("header");
                int status = responseHeader.getInt("error");
                if (status != ControlNumberStatus.SUCCESS) {
                    errorMessage = getBulkCnErrorMessage(status);
                } else if (responseContent.getJSONArray("controlNumbers").length() == 0) {
                    errorMessage = getResources().getString(R.string.NoCNReceived);
                }
            } else if (responseContent.has("error_occured")) {
                if (responseContent.getBoolean("error_occured")) {
                    errorMessage = responseContent.getString("error_message");
                }
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

    private String getBulkCnErrorMessage(int status) {
        Integer errorMessageId = ControlNumberStatusMapping.get(status);
        if (errorMessageId != null) {
            return getResources().getString(errorMessageId);
        } else {
            Log.e(LOG_TAG, String.format("Unknown response code from the server: %d", status));
            return getResources().getString(R.string.SomethingWrongServer);
        }
    }

    private int insertControlNumber(JSONObject order, String controlNumber, String internalIdentifier) throws JSONException {
        return insertControlNumber(
                order.getString("amount_to_be_paid"),
                order.getString("amount_to_be_paid"), //TODO no separation between calculated and confirmed amount for now
                controlNumber,
                internalIdentifier,
                order.getString("type_of_payment"),
                order.getString("SmsRequired"));
    }

    private int insertControlNumber(String amountCalculated, String amountConfirmed, String control_number, String InternalIdentifier, String PaymentType, String SmsRequired) {
        ContentValues values = new ContentValues();
        values.put("AmountCalculated", String.valueOf(amountCalculated));
        values.put("AmountConfirmed", String.valueOf(amountConfirmed));
        values.put("ControlNumber", String.valueOf(control_number));
        values.put("InternalIdentifier", String.valueOf(InternalIdentifier));
        values.put("PaymentType", String.valueOf(PaymentType));
        values.put("SmsRequired", String.valueOf(SmsRequired));

        sqlHandler.insertData("tblControlNumber", values);

        return getMaxId();
    }

    public int getMaxId() {
        int id = 0;
        @Language("SQL")
        String Query = "SELECT Max(Id) As Id FROM tblControlNumber";
        JSONArray ID = sqlHandler.getResult(Query, null);
        try {
            JSONObject JmaxIdOb = ID.getJSONObject(0);
            id = JmaxIdOb.getInt("Id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    private void updateRecordedPoliciesAfterRequest(JSONArray paymentDetails, int ControlNumberId) throws JSONException {
        JSONObject ob;
        for (int j = 0; j < paymentDetails.length(); j++) {
            ob = paymentDetails.getJSONObject(j);
            int Id = Integer.parseInt(ob.getString("Id"));
            updateRecordedPolicy(Id, ControlNumberId);
        }
    }

    public void updateRecordedPolicy(int Id, int ControlNumberId) {
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());
        ContentValues values = new ContentValues();
        values.put("ControlNumberId", ControlNumberId);
        values.put("ControlRequestDate", d);
        try {
            sqlHandler.updateData("tblRecordedPolicies", values, "Id = ?", new String[]{String.valueOf(Id)});
        } catch (UserException e) {
            e.printStackTrace();
        }
    }

    private void updateAssignedControlNumbers(String assignedControlNumbers) throws JSONException {
        JSONObject ob;
        JSONArray arr = new JSONArray(assignedControlNumbers);
        for (int j = 0; j < arr.length(); j++) {
            ob = arr.getJSONObject(j);
            String internalIdentifier = ob.getString("internal_identifier");
            String controlNumber = ob.getString("control_number");
            assignControlNumber(internalIdentifier, controlNumber);
        }
    }

    private void assignControlNumber(String InternalIdentifier, String ControlNumber) {
        ContentValues values = new ContentValues();
        values.put("ControlNumber", ControlNumber);
        try {
            sqlHandler.updateData("tblControlNumber", values, "InternalIdentifier = ?", new String[]{String.valueOf(InternalIdentifier)});
        } catch (UserException e) {
            e.printStackTrace();
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
