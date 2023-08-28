package org.openimis.imispolicies.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.Token;

import java.util.Date;

public class LoginRepository {
    private static final String PREFS_NAME = "LoginRepository";
    private static final String HAS_MIGRATED = "has_migrated";
    private static final String REST_TOKEN = "rest_token";
    private static final String REST_VALIDITY = "rest_validity";
    private static final String REST_OFFICER_CODE = "rest_officer_code";
    private static final String FHIR_TOKEN = "fhir_token";
    private static final String FHIR_VALIDITY = "fhir_validity";
    private static final String FHIR_OFFICER_CODE = "fhir_officer_code";

    private final SharedPreferences prefs;

    public LoginRepository(@NonNull Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(HAS_MIGRATED, false)) {
            migrateOldTokens();
        }
    }

    @SuppressWarnings("deprecation")
    private void migrateOldTokens() {
        SharedPreferences.Editor editor = prefs.edit();
        Token token = new Token();
        if (token.isTokenValidJWT()) {
            editor.putString(REST_TOKEN, token.getToken());
            Date validity = token.getValidity();
            if (validity != null) {
                editor.putLong(REST_VALIDITY, validity.getTime());
            }
            String officerCode = token.getOfficerCode();
            if (officerCode != null) {
                editor.putString(REST_OFFICER_CODE, officerCode);
            }
        }
        editor.putBoolean(HAS_MIGRATED, true);
        editor.apply();
    }

    @Nullable
    public String getRestToken() {
        return getToken(REST_TOKEN, REST_VALIDITY, REST_OFFICER_CODE);
    }

    @Nullable
    public String getFhirToken() {
        return getToken(FHIR_TOKEN, FHIR_VALIDITY, FHIR_OFFICER_CODE);
    }

    /**
     * Logic taken from [Token.java]
     */
    @Nullable
    private String getToken(
            @NonNull String tokenKey,
            @NonNull String validityKey,
            @NonNull String officerCodeKey
    ) {
        String eoCode = prefs.getString(officerCodeKey, null);
        if(Global.getGlobal().getOfficerCode() == null || !Global.getGlobal().getOfficerCode().equals(eoCode)) {
            return null;
        }

        String token = prefs.getString(tokenKey, null);
        if (token == null) {
            return null;
        }

        int indexOfFirstDot = token.indexOf('.');
        if (indexOfFirstDot == -1) {
            return null;
        }

        String tokenHeader = token.substring(0, indexOfFirstDot);
        try {
            JSONObject headerObject = new JSONObject(new String(Base64.decode(tokenHeader, Base64.DEFAULT)));
            if (!"JWT".equals(headerObject.getString("typ"))) {
                return null;
            }
        } catch (JSONException e) {
            return null;
        }

        Date expiryDate = getValidity(validityKey);
        if (expiryDate == null) {
            return null;
        }

        Date now = new Date();
        if (now.after(expiryDate)) {
            return null;
        }
        return token;
    }

    @Nullable
    private Date getValidity(@NonNull String validityKey) {
        long validTo = prefs.getLong(validityKey, -1);
        if (validTo == -1) {
            return null;
        }
        return new Date(validTo);
    }

    public void saveRestToken(@Nullable String token, @Nullable Date validity, @Nullable String officerCode) {
        saveToken(REST_TOKEN, REST_VALIDITY, REST_OFFICER_CODE, token, validity, officerCode);
    }

    public void saveFhirToken(@Nullable String token, @Nullable Date validity, @Nullable String officerCode) {
        saveToken(FHIR_TOKEN, FHIR_VALIDITY, FHIR_OFFICER_CODE, token, validity, officerCode);
    }

    private void saveToken(
            @NonNull String tokenKey, @NonNull String validityKey, @NonNull String officerCodeKey,
            @Nullable String token, @Nullable Date validity, @Nullable String officerCode
    ) {
        SharedPreferences.Editor editor = prefs.edit();
        if (StringUtils.isEmpty(token)) {
            editor.remove(tokenKey);
        } else {
            editor.putString(tokenKey, token);
        }
        if (validity == null) {
            editor.remove(validityKey);
        } else {
            editor.putLong(validityKey, validity.getTime());
        }
        if (officerCode == null) {
            editor.remove(officerCodeKey);
        } else {
            editor.putString(officerCodeKey, officerCode);
        }
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getFhirToken() != null && getRestToken() != null;
    }

    public void logout() {
        saveFhirToken(null, null, null);
        saveRestToken(null, null, null);
    }
}
