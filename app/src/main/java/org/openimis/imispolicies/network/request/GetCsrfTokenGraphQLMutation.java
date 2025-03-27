package org.openimis.imispolicies.network.request;

import android.media.session.MediaSession;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.internal.QueryDocumentMinifier;

import org.json.JSONObject;
import org.openimis.imispolicies.BuildConfig;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.ToRestApi;
import org.openimis.imispolicies.tools.Log;

import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetCsrfTokenGraphQLMutation extends BaseGraphQLRequest {

    private static final String URI = BuildConfig.API_BASE_URL + "api/graphql";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    protected Global global;


    @WorkerThread
    @NonNull
    public Response get(@NonNull String jwtToken) throws Exception {

        String QUERY_DOCUMENT = QueryDocumentMinifier.minify(
                "mutation {"
                        + "  getCsrfToken {"
                        + " csrfToken"
                        + " } "
                        + " } "
        );

        JSONObject json = new JSONObject();
        json.put("query", QUERY_DOCUMENT);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient httpClient = builder.build();
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(URI)
                .addHeader("Authorization","bearer " + jwtToken)
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();
        int responseCode = response.code();

        Log.i("HTTP_POST", URI + " - " + responseCode);
        Log.i("GetCsrfToken", QUERY_DOCUMENT);

        String responsePhrase = Objects.requireNonNull(response.body()).string();
        Log.i("RESPONSE", String.format("response: %d %s", responseCode, responsePhrase));

        return response;

    }
}
