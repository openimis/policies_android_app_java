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

    private final Token token;
    private final String uri;
    private final String apiVersion;

    public ToRestApi() {
        token = Global.getGlobal().getJWTToken();
        uri = AppInformation.DomainInfo.getDomain() + "api/";
        apiVersion = AppInformation.DomainInfo.getApiVersion();
    }

    public HttpResponse getFromRestApi(String functionName, boolean addToken) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri + functionName);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader("accept", "application/json");
        httpGet.setHeader("api-version", apiVersion);
        if (addToken) {
            httpGet.setHeader("Authorization", "bearer " + token.getTokenText().trim());
        }

        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (addToken && response != null && response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                token.clearToken();
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
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("accept", "application/json");
        httpPost.setHeader("api-version", apiVersion);
        if (addToken) {
            httpPost.setHeader("Authorization", "bearer " + token.getTokenText().trim());
        }

        try {
            if (object != null) {
                StringEntity postingString = new StringEntity(object.toString());
                httpPost.setEntity(postingString);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (addToken && response != null && response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                token.clearToken();
            }
            int responseCode = response.getStatusLine().getStatusCode();
            Log.i("HTTP_POST", uri + functionName + " - " + responseCode);
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

    public String deleteFromRestApiToken(final String functionName) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(uri + functionName);
        httpDelete.setHeader("Content-type", "application/json");
        httpDelete.setHeader("Authorization", "bearer " + token.getTokenText());
        httpDelete.setHeader("accept", "application/json");
        httpDelete.setHeader("api-version", apiVersion);

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpDelete);
            if (response != null && response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                token.clearToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getContent(response);
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
}
