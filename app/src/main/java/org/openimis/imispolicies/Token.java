package org.openimis.imispolicies;

import android.util.Base64;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Hiren on 15/02/2019.
 */
@Deprecated
public class Token {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX", Locale.US);

    public void saveTokenText(String token, String validTo, String eoCode) {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");

        File tokenFile = new File(dir, "token.txt");
        File validToFile = new File(dir, "validTo.txt");
        File eoCodeFile = new File(dir, "eoCode.txt");

        try {
            if (tokenFile.exists()) {
                tokenFile.delete();
            }
            if (tokenFile.createNewFile()) {
                FileOutputStream fOut = new FileOutputStream(tokenFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(token);
                myOutWriter.close();
                fOut.close();
            }

            if (validToFile.exists()) {
                validToFile.delete();
            }
            if (validToFile.createNewFile()) {
                FileOutputStream fOut = new FileOutputStream(validToFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(validTo);
                myOutWriter.close();
                fOut.close();
            }
            if (eoCodeFile.exists()) {
                eoCodeFile.delete();
            }
            if (eoCodeFile.createNewFile()) {
                FileOutputStream fOut = new FileOutputStream(eoCodeFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(eoCode);
                myOutWriter.close();
                fOut.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public String getToken() {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");
        File tokenFile = new File(dir, "token.txt");
        if (tokenFile.exists()) {
            try (FileInputStream fIn = new FileInputStream(tokenFile)) {

                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                String token = myReader.readLine();
                myReader.close();
                return token;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    public Date getValidity() {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");
        File validToFile = new File(dir, "validTo.txt");
        if (validToFile.exists()) {
            try (FileInputStream fIn = new FileInputStream(validToFile)) {
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                String validTo = myReader.readLine();
                myReader.close();
                if (validTo == null || "".equals(validTo)) {
                    return null;
                }
                return getValidity(validTo);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    public static Date getValidity(@Nullable String date) throws ParseException {
        if (date == null) {
            return null;
        }
        return format.parse(date);
    }

    @Nullable
    public String getOfficerCode() {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");

        File eoCodeFile = new File(dir, "eoCode.txt");
        if (eoCodeFile.exists()) {
            try (FileInputStream fIn = new FileInputStream(eoCodeFile)) {
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                String eoCode = myReader.readLine();
                myReader.close();
                return eoCode;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void clearToken() {
        saveTokenText("", "", "");
    }

    //How to validate JWT:
    //https://datatracker.ietf.org/doc/html/rfc7519#section-7.2
    public boolean isTokenValidJWT() {
        String token = getToken();
        if (StringUtils.isEmpty(token)) {
            return false;
        }

        String eoCode = getOfficerCode();
        if (eoCode == null) {
            return false;
        }

        int indexOfFirstDot = token.indexOf('.');
        if (indexOfFirstDot == -1)
            return false;

        String tokenHeader = token.substring(0, indexOfFirstDot);
        try {
            JSONObject headerObject = new JSONObject(new String(Base64.decode(tokenHeader, Base64.DEFAULT)));
            if (!"JWT".equals(headerObject.getString("typ")))
                return false;
        } catch (JSONException e) {
            return false;
        }

        Date expiryDate = getValidity();
        Date now = new Date();
        return !now.after(expiryDate);
    }
}
