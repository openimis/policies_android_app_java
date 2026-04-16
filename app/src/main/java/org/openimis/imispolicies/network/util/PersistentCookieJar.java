package org.openimis.imispolicies.network.util;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

public class PersistentCookieJar implements CookieJar {

    private final SharedPreferences prefs;

    public PersistentCookieJar(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, List<Cookie> cookies) {

        for (Cookie cookie : cookies) {
            if ("openimis_session".equals(cookie.name())) {

                prefs.edit()
                        .putString("session_value", cookie.value())
                        .putLong("session_expiry", cookie.expiresAt())
                        .apply();
            }
        }
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {

        String value = prefs.getString("session_value", null);
        long expiry = prefs.getLong("session_expiry", -1);

        if (value == null || expiry == -1) {
            return new ArrayList<>();
        }

        Cookie cookie = new Cookie.Builder()
                .name("openimis_session")
                .value(value)
                .domain(url.host())
                .path("/")
                .expiresAt(expiry)
                .build();

        List<Cookie> cookies = new ArrayList<>();
        cookies.add(cookie);
        return cookies;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}