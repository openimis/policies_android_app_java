package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.json.JSONObject;
import org.openimis.imispolicies.BuildConfig;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.ToRestApi;
import org.openimis.imispolicies.Token;
import org.openimis.imispolicies.network.dto.LoginDto;
import org.openimis.imispolicies.network.dto.TokenDto;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.GetCsrfTokenGraphQLMutation;
import org.openimis.imispolicies.network.request.LoginRequest;
import org.openimis.imispolicies.repository.LoginRepository;
import org.openimis.imispolicies.tools.Log;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Objects;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;
import okhttp3.Response;

public class Login {

    @NonNull
    private final LoginRequest request;
    @NonNull
    private final LoginRepository repository;
    @NonNull
    private final ToRestApi toRestApi;
    private final boolean isPaymentEnabled;

    public Login() {
        this(Global.getGlobal().getLoginRepository(), new LoginRequest(), new ToRestApi(), BuildConfig.IS_PAYMENT_ENABLED);
    }

    public Login(
            @NonNull LoginRepository loginRepository,
            @NonNull LoginRequest request,
            @NonNull ToRestApi toRestApi,
            boolean isPaymentEnabled
    ) {
        this.request = request;
        this.repository = loginRepository;
        this.toRestApi = toRestApi;
        this.isPaymentEnabled = isPaymentEnabled;
    }

    @WorkerThread
    public void execute(@NonNull String username, @NonNull String password) throws Exception {
        String officerCode = Global.getGlobal().getOfficerCode();
        if (officerCode == null) {
            throw new IllegalStateException("OfficerCode should not be null on login");
        }
        try {
            TokenDto token = request.post(new LoginDto(username.trim(), password));
            Response response = new GetCsrfTokenGraphQLMutation().get(token.getToken());
            String csrfToken = Objects.requireNonNull(response.body()).toString();
            Log.e("response token", response.body().toString());
            repository.saveFhirToken(token.getToken(), new Date(token.getExpiresOn()), officerCode);
            repository.saveCsrfToken(csrfToken);
            if (isPaymentEnabled) {
                token = loginToRestApi(username, password);
                repository.saveRestToken(token.getToken(), new Date(token.getExpiresOn()), officerCode);
            }
        } catch (Exception e) {
            repository.logout();
            throw e;
        }
    }

    private TokenDto loginToRestApi(@NonNull String username, @NonNull String password) throws Exception {
        JSONObject object = new JSONObject();
        object.put("userName", username);
        object.put("password", password);

        HttpResponse response = toRestApi.postToRestApi(object, "login");
        HttpEntity respEntity = response.getEntity();
        String content = EntityUtils.toString(respEntity);
        int code = response.getStatusLine().getStatusCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new HttpException(code, response.getStatusLine().getReasonPhrase(), content, null);
        }

        JSONObject ob = new JSONObject(content);
        return new TokenDto(
                /* token = */ ob.getString("access_token"),
                /* expiresOn = */ Token.getValidity(ob.getString("expires_on")).getTime()
        );
    }
}
