package tz.co.exact.imis;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Hiren on 14/02/2019.
 */

public class ToRestApi {
    Token tokenl = new Token();
    private String uri = AppInformation.DomainInfo.getDomain()+"/restapi/";

    //Post
    public HttpResponse postToRestApi(final JSONObject object, final String functionName) {
        final String[] content = {null};
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri+functionName);
        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            httpPost.setHeader("Content-type", "application/json");
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }

        //Send Request Here
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
    public String postObjectToRestApiObject(final JSONObject object, final String functionName) {
        final String[] content = {null};
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri+functionName);
        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            httpPost.setHeader("Content-type", "application/json");
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }

        //Send Request Here
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity respEntity = response.getEntity();
        if (respEntity != null) {
            final String[] code = {null};
            // EntityUtils to get the response content

            try {
                content[0] = EntityUtils.toString(respEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content[0];
    }
    public HttpResponse postToRestApiToken(final JSONObject object, final String functionName) {
        //final String[] content = {null};
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri+functionName);
        try {
            StringEntity postingString = new StringEntity(object.toString());
            httpPost.setEntity(postingString);
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "bearer "+tokenl.getTokenText());
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }

        //Send Request Here
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

}
