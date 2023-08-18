package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.network.dto.LoginDto;
import org.openimis.imispolicies.network.dto.TokenDto;
import org.openimis.imispolicies.network.request.LoginRequest;

import java.util.concurrent.TimeUnit;

public class Login {

    @NonNull
    private final LoginRequest request;
    @NonNull
    private final Global global;

    public Login(
            @NonNull LoginRequest request,
            @NonNull Global global
    ) {
        this.request = request;
        this.global = global;
    }

    public Login() {
        this(new LoginRequest(), Global.getGlobal());
    }

    @WorkerThread
    public void execute(@NonNull String username, String password) {
        try {
            TokenDto token = request.post(new LoginDto(username.trim(), password));
            global.getJWTToken().saveTokenText(
                    token.getToken(),
                    TimeUnit.SECONDS.toMillis(token.getExpiresOn()),
                    global.getOfficerCode()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
