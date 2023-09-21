package org.openimis.imispolicies;

import android.content.Context;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import org.json.JSONObject;
import org.openimis.imispolicies.repository.LoginRepository;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.util.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ToRestApi {

    public static class RenewalStatus {
        public static final int ACCEPTED = 3001;
        public static final int ALREADY_ACCEPTED = 3002;
        public static final int REJECTED = 3003;
        public static final int DUPLICATE_RECEIPT = 3004;
        public static final int GRACE_PERIOD_EXPIRED = 3005;
        public static final int CONTROL_NUMBER_ERROR = 3006;
        public static final int UNEXPECTED_EXCEPTION = 3999;
    }

    public static class Headers {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT = "Accept";
        public static final String AUTHORIZATION = "Authorization";
        public static final String API_VERSION = "api-version";
    }

    public static class MimeTypes {
        public static final String APPLICATION_JSON = "application/json";
    }

    public static final String FUNCTION_PREFIX = "rest/api/";


    private final LoginRepository repository;
    private final String uri;
    private final String apiVersion;

    public ToRestApi() {
        repository = Global.getGlobal().getLoginRepository();
        uri = AppInformation.DomainInfo.getDomain() + FUNCTION_PREFIX;
        apiVersion = AppInformation.DomainInfo.getApiVersion();
    }

    public HttpResponse getFromRestApi(String functionName, boolean addToken) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri + functionName);
        httpGet.setHeader(Headers.CONTENT_TYPE, MimeTypes.APPLICATION_JSON);
        httpGet.setHeader(Headers.ACCEPT, MimeTypes.APPLICATION_JSON);
        httpGet.setHeader(Headers.API_VERSION, apiVersion);
        if (addToken) {
            httpGet.setHeader(Headers.AUTHORIZATION, buildTokenHeader());
        }

        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (addToken) {
                checkToken(response);
            }
            int responseCode = response.getStatusLine().getStatusCode();
            Log.i("HTTP_GET", uri + functionName + " - " + responseCode);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HttpResponse postToRestApi(JSONObject object, String functionName, boolean addToken) {
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(uri + functionName);
        httpPost.setHeader(Headers.CONTENT_TYPE, MimeTypes.APPLICATION_JSON);
        httpPost.setHeader(Headers.ACCEPT, MimeTypes.APPLICATION_JSON);
        httpPost.setHeader(Headers.API_VERSION, apiVersion);
        if (addToken) {
            httpPost.setHeader(Headers.AUTHORIZATION, buildTokenHeader());
        }

        try {
            if (object != null) {
                StringEntity postingString = new StringEntity(object.toString());
                httpPost.setEntity(postingString);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (addToken) {
                checkToken(response);
            }

            int responseCode = response.getStatusLine().getStatusCode();
            Log.i("HTTP_POST", uri + functionName + " - " + responseCode);
            if (object != null && responseCode >= 400 && !functionName.equals("login")) {
                String body = object.toString();
                if (body.length() > 1000) {
                    body = body.substring(0, 1000);
                }
                Log.e("HTTP_POST", "Body: " + body);
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public HttpResponse postToRestApi(final JSONObject object, final String functionName) {
        return postToRestApi(object, functionName, false);
    }

    public String postObjectToRestApiObject(final JSONObject object, final String functionName) {
        return getContent(postToRestApi(object, functionName, false));
    }

    public HttpResponse postToRestApiToken(final JSONObject object, final String functionName) {
        return postToRestApi(object, functionName, true);
    }

    public String getObjectFromRestApi(final String functionName) {
        return getContent(getFromRestApi(functionName, false));
    }

    public String getObjectFromRestApiToken(final String functionName) {
        return getContent(getFromRestApi(functionName, true));
    }

    public HttpResponse getFromRestApiToken(final String functionName) {
        return getFromRestApi(functionName, true);
    }

    public HttpResponse getFromRestApi(final String functionName) {
        return getFromRestApi(functionName, false);
    }

    public HttpResponse deleteFromRestApiToken(final String functionName) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(uri + functionName);
        httpDelete.setHeader(Headers.CONTENT_TYPE, MimeTypes.APPLICATION_JSON);
        httpDelete.setHeader(Headers.AUTHORIZATION, buildTokenHeader());
        httpDelete.setHeader(Headers.ACCEPT, MimeTypes.APPLICATION_JSON);
        httpDelete.setHeader(Headers.API_VERSION, apiVersion);

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpDelete);
            checkToken(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public String getContent(HttpResponse response) {
        if (response == null)
            return null;

        String content = null;
        HttpEntity respEntity = response.getEntity();

        if (respEntity != null) {
            try {
                content = EntityUtils.toString(respEntity);
                if (content != null && content.length() > 0
                        && response.getStatusLine().getStatusCode() >= 400) {
                    Log.e("HTTP", "Error " + response.getStatusLine().getStatusCode() + ", response: " + content);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return content;
    }

    private void checkToken(HttpResponse response) {
        if (response != null && response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            repository.saveRestToken(null, null, null);
            MainActivity.SetLoggedIn();
        }
    }

    private String buildTokenHeader() {
        String tokenText = repository.getRestToken();
        if (!StringUtils.isEmpty(tokenText)) {
            return String.format("bearer %s", tokenText.trim());
        }
        return "";
    }

    public String getHttpError(Context context, int httpResponseCode, String httpReason) {
        if (httpResponseCode == HttpURLConnection.HTTP_OK || httpResponseCode == HttpURLConnection.HTTP_CREATED) {
            return null;
        } else if (httpResponseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            return context.getResources().getString(R.string.NotFound);
        } else if (httpResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return context.getResources().getString(R.string.Unauthorized);
        } else if (httpResponseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            return context.getResources().getString(R.string.Forbidden);
        } else {
            return context.getResources().getString(R.string.HttpResponse, httpResponseCode, httpReason);
        }
    }
}
