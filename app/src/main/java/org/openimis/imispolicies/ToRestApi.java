package org.openimis.imispolicies;

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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

public class ToRestApi {
    public static class UploadStatus
    {
        public static final int NO_RESPONSE = -1;
        public static final int REJECTED = 0;
        public static final int ACCEPTED = 1;
        public static final int ERROR = 2;
    }

    Token tokenl = new Token();
    private String uri = AppInformation.DomainInfo.getDomain()+"api/";

    // Post without Token, returned response
    public HttpResponse postToRestApi(final JSONObject object, final String functionName) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri+functionName);
        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            httpPost.setHeader("Content-type", "application/json");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    // Post without Token, returned object
    public String postObjectToRestApiObject(final JSONObject object, final String functionName) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri + functionName);
        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            httpPost.setHeader("Content-type", "application/json");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getContent(response);
    }

    // Post with Token JWT, returned response
    public HttpResponse postToRestApiToken(final JSONObject object, final String functionName) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri+functionName);
        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "bearer "+tokenl.getTokenText());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            if (response != null && response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                tokenl.clearToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    // Get without Token, returned object
    public String getObjectFromRestApi(final String functionName) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri + functionName);
        httpGet.setHeader("Content-type", "application/json");

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getContent(response);
    }

    // Get with Token JWT, returned object
    public String getObjectFromRestApiToken(final String functionName) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri+functionName);
        httpGet.setHeader("Content-type", "application/json");
        httpGet.setHeader("Authorization", "bearer "+tokenl.getTokenText());

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            if (response != null && response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                tokenl.clearToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getContent(response);
    }

    public String deleteFromRestApiToken(final String functionName) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(uri+functionName);
        httpDelete.setHeader("Content-type", "application/json");
        httpDelete.setHeader("Authorization", "bearer "+tokenl.getTokenText());

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpDelete);
            if (response != null && response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                tokenl.clearToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getContent(response);
    }

    public String getContent(HttpResponse response)
    {
        if(response==null)
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
