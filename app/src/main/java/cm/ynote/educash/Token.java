package cm.ynote.educash;

import android.util.Base64;

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

public class Token {
    public void saveTokenText(String token, String validTo) {
        Global global = Global.getGlobal();
        String dir = global.getSubdirectory("Authentications");

        File tokenFile = new File(dir, "token.txt");
        File validToFile = new File(dir, "validTo.txt");

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTokenText() {
        String token = null;
        String validTo = null;
        try {
            Global global = Global.getGlobal();
            String dir = global.getSubdirectory("Authentications");

            File validToFile = new File(dir, "validTo.txt");
            if (validToFile.exists()) {
                FileInputStream fIn = new FileInputStream(validToFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                validTo = myReader.readLine();
                myReader.close();
            }

            File tokenFile = new File(dir, "token.txt");
            if (tokenFile.exists()) {
                FileInputStream fIn = new FileInputStream(tokenFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                token = myReader.readLine();
                myReader.close();
            }

            SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultIsoDatetimeFormatter();

            if (validTo != null)
                try {
                    Date expiryDate = format.parse(validTo);
                    Date now = new Date();

                    if (now.after(expiryDate)) {
                        clearToken();
                        token = null;
                    }
                } catch (ParseException | NullPointerException e) {
                    e.printStackTrace();
                }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (token == null) {
            return "";
        }
        return token;
    }

    public void clearToken() {
        saveTokenText("", "");
    }

    //How to validate JWT:
    //https://datatracker.ietf.org/doc/html/rfc7519#section-7.2
    public boolean isTokenValidJWT() {
        String token = getTokenText();
        if (token == null)
            return false;

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

        return true;
    }
}
