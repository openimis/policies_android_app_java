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
            String dir = Environment.getExternalStorageDirectory() + File.separator + "IMIS/Authentications/";
            //create folder
            File folder = new File(dir); //folder name
            folder.mkdirs();

            //create file
            File file = new File(dir, "token.txt");
            try {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(token);
                myOutWriter.close();
                fOut.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String getTokenText(){
        String aBuffer = "";
        try {
            String dir = Environment.getExternalStorageDirectory() + File.separator + "IMIS/Authentications/";
            File myFile = new File("/"+dir+"/token.txt");
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            aBuffer = myReader.readLine();
            myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aBuffer;
    }
}
