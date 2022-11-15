package org.openimis.imispolicies.util;

import android.support.annotation.NonNull;

import org.openimis.imispolicies.tools.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StreamUtils {
    private static final int buffSize = 8192;
    private static final String LOG_TAG = "STREAMUTIL";

    /**
     * Only use this method if you can be sure the content of the input stream
     * can be read, is a UTF-8 text and will fit in memory (as String).
     *
     * @param is input stream to be read
     * @return content of the input stream or null if IO exception occurs
     */
    public static String readInputStreamAsUTF8String(@NonNull InputStream is) {
        try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder stringBuilder = null;
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                if (stringBuilder != null) {
                    stringBuilder.append("\n");
                    stringBuilder.append(inputStr);
                } else {
                    stringBuilder = new StringBuilder(inputStr);
                }
            }
            return stringBuilder != null ? stringBuilder.toString() : null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while reading input stream", e);
        }

        return null;
    }

    /**
     * Copy input stream to output stream using a byte buffer (8kb by default)
     *
     * @param is source stream
     * @param os target stream
     * @throws IOException thrown on read/write error
     */
    public static void bufferedStreamCopy(@NonNull InputStream is, @NonNull OutputStream os) throws IOException {
        byte[] buffer = new byte[buffSize];

        int read;
        while ((read = is.read(buffer)) >= 0) {
            os.write(buffer, 0, read);
        }
    }
}
