package org.openimis.imispolicies.network.okhttp;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.openimis.imispolicies.MainActivity;
import org.openimis.imispolicies.repository.LoginRepository;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthorizationInterceptor implements Interceptor {
    private static final String USER_AGENT = "mobile_app";

    @NonNull
    private final LoginRepository repository;

    public AuthorizationInterceptor(@NonNull LoginRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = repository.getFhirToken();
        String csrfToken = repository.getCsrfToken();
        if (token == null) {
            return chain.proceed(chain.request());
        }
        Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("Authorization", "bearer " + token.trim());
        builder.addHeader("User-Agent", USER_AGENT);
        Response response = chain.proceed(builder.build());
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            repository.saveFhirToken(null, null, null);
            MainActivity.SetLoggedIn();
        }
        return response;
    }
}
