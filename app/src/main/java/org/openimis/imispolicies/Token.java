package org.openimis.imispolicies;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Hiren on 15/02/2019.
 */

public class Token {
    public void saveTokenText(String token) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //handle case of no SDCARD present
        } else {
            Global global = Global.getGlobal();
            String dir = global.getSubdirectory("Authentications");

            File file = new File(dir, "token.txt");
            try {
                if (file.exists()) {
                    file.delete();
                }
                if (file.createNewFile()) {
                    FileOutputStream fOut = new FileOutputStream(file);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(token);
                    myOutWriter.close();
                    fOut.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getTokenText() {
        String aBuffer = "";
        try {
            Global global = Global.getGlobal();
            String dir = global.getSubdirectory("Authentications");
            File myFile = new File(dir, "token.txt");
            if (myFile.exists()) {
                FileInputStream fIn = new FileInputStream(myFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                aBuffer = myReader.readLine();
                myReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }
}
