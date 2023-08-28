package org.openimis.imispolicies.network.okhttp;

import androidx.annotation.NonNull;

import org.openimis.imispolicies.MainActivity;
import org.openimis.imispolicies.repository.LoginRepository;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthorizationInterceptor implements Interceptor {

    @NonNull
    private final LoginRepository repository;

    public AuthorizationInterceptor(@NonNull LoginRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = repository.getFhirToken();
        if (token != null) {
            Request.Builder builder = chain.request().newBuilder();
            builder.addHeader("Authorization", "bearer " + token.trim());
            Response response = chain.proceed(builder.build());
            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                repository.saveFhirToken(null, null, null);
                MainActivity.SetLoggedIn();
            }
            return response;
        }
        return chain.proceed(chain.request());
    }
}
