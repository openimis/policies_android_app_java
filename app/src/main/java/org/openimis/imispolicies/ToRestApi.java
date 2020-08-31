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

import android.view.View;

import com.exact.general.General;

public class ToRestApi {
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
        final String[] content = {null};
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

        HttpEntity respEntity = response.getEntity();
        if (respEntity != null) {
            try {
                content[0] = EntityUtils.toString(respEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content[0];
    }

    // Post without Token, returned object
    public String postObjectToRestApiObjectToken(final JSONObject object, final String functionName) {
        final String[] content = {null};
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpEntity respEntity = response.getEntity();
        if (respEntity != null) {
            try {
                content[0] = EntityUtils.toString(respEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content[0];
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    // Get without Token, returned object
    public String getObjectFromRestApi(final String functionName) {
        final String[] content = {null};
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri+functionName);
        httpGet.setHeader("Content-type", "application/json");

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpEntity respEntity = response.getEntity();
        if (respEntity != null) {
            try {
                content[0] = EntityUtils.toString(respEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content[0];
    }

    // Get with Token JWT, returned object
    public String getObjectFromRestApiToken(final String functionName) {
        String content = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri+functionName);
        httpGet.setHeader("Content-type", "application/json");
        httpGet.setHeader("Authorization", "bearer "+tokenl.getTokenText());

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public String deleteFromRestApiToken(final String functionName) {
        String content = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(uri+functionName);
        httpDelete.setHeader("Content-type", "application/json");
        httpDelete.setHeader("Authorization", "bearer "+tokenl.getTokenText());

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
