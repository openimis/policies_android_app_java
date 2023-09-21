package org.openimis.imispolicies.network.dto;

import androidx.annotation.NonNull;

public class LoginDto {

    @NonNull
    private final String username;
    @NonNull
    private final String password;

    public LoginDto(@NonNull String username, @NonNull String password) {
        this.username = username;
        this.password = password;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }
}
