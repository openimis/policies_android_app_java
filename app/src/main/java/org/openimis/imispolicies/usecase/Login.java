package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.json.JSONObject;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.ToRestApi;
import org.openimis.imispolicies.Token;
import org.openimis.imispolicies.network.dto.LoginDto;
import org.openimis.imispolicies.network.dto.TokenDto;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.request.LoginRequest;
import org.openimis.imispolicies.repository.LoginRepository;

import java.net.HttpURLConnection;
import java.util.Date;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.util.EntityUtils;

public class Login {

    @NonNull
    private final LoginRequest request;
    @NonNull
    private final LoginRepository repository;
    @NonNull
    private final ToRestApi toRestApi;

    public Login() {
        this(Global.getGlobal().getLoginRepository(), new LoginRequest(), new ToRestApi());
    }

    public Login(
            @NonNull LoginRepository loginRepository,
            @NonNull LoginRequest request,
            @NonNull ToRestApi toRestApi
            ) {
        this.request = request;
        this.repository = loginRepository;
        this.toRestApi = toRestApi;
    }

    @WorkerThread
    public void execute(@NonNull String username, @NonNull String password) throws Exception {
        String officerCode =  Global.getGlobal().getOfficerCode();
        if (officerCode == null) {
            throw new IllegalStateException("OfficerCode should not be null on login");
        }
        try {
            TokenDto token = request.post(new LoginDto(username.trim(), password));
            repository.saveFhirToken(token.getToken(), new Date(token.getExpiresOn()), officerCode);
            //token = loginToRestApi(username, password);
            repository.saveRestToken(token.getToken(), new Date(token.getExpiresOn()), officerCode);
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
