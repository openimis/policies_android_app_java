package org.openimis.imispolicies;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ToRestApi {
    public static class UploadStatus {
        public static final int NO_RESPONSE = -1;
        public static final int REJECTED = 0;
        public static final int ACCEPTED = 1;
        public static final int ERROR = 2;
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

    public static final String FUNCTION_PREFIX = "api/";


    private final Token token;
    private final String uri;
    private final String apiVersion;

    public ToRestApi() {
        token = Global.getGlobal().getJWTToken();
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
            if (responseCode >= 400) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String errorPayload = EntityUtils.toString(entity);
                    Log.e("HTTP_POST", "error payload" + errorPayload);
                }
            }
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
            if (object != null && responseCode >= 400) {
                Log.e("HTTP_POST", object.toString());
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String errorPayload = EntityUtils.toString(entity);
                    Log.e("HTTP_POST", "error payload" + errorPayload);
                }
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
            if (response != null && response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                token.clearToken();
            }
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return content;
    }

    private void checkToken(HttpResponse response) {
        if (response != null && response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            token.clearToken();
        }
    }

    private String buildTokenHeader() {
        String tokenText = token.getTokenText();
        if (tokenText != null) {
            return String.format("bearer %s", tokenText.trim());
        }
        return "";
    }
}
