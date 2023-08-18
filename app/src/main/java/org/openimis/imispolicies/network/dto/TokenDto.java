package org.openimis.imispolicies.network.dto;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenDto {

    @NonNull
    public static TokenDto fromJson(@NonNull JSONObject object) throws JSONException {
        return new TokenDto(object.getString("token"), object.getLong("exp"));
    }

    @NonNull
    private final String token;
    private final long expiresOn;

    public TokenDto(
            @NonNull String token,
            long expiresOn
    ){
        this.token = token;
        this.expiresOn = expiresOn;
    }

    @NonNull
    public String getToken() {
        return token;
    }

    public long getExpiresOn() {
        return expiresOn;
    }
}
