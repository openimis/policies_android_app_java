package org.openimis.imispolicies.usecase;

import androidx.annotation.NonNull;

import org.apache.commons.io.IOUtils;
import org.openimis.imispolicies.network.exception.HttpException;
import org.openimis.imispolicies.network.exception.UserNotAuthenticatedException;
import org.openimis.imispolicies.network.request.GetMasterDataExportRequest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FetchMasterData {

    private static final String MASTER_DATA_FILE_NAME = "masterdata.";
    @NonNull
    private final GetMasterDataExportRequest getMasterDataExportRequest;

    public FetchMasterData() {
        this(new GetMasterDataExportRequest());
    }

    public FetchMasterData(@NonNull GetMasterDataExportRequest getMasterDataExportRequest) {
        this.getMasterDataExportRequest = getMasterDataExportRequest;
    }

    @NonNull
    public String execute() throws Exception {
        try (ZipInputStream zipFile = new ZipInputStream(new ByteArrayInputStream(getMasterDataExportRequest.get()))) {
            ZipEntry entry;
            while ((entry = zipFile.getNextEntry()) != null) {
                // Currently, the name of the file is "MasterData.txt" but the code is a little bit
                // more permissive in case someone wants to "fix" that into 'masterdata.json'.
                if (entry.getName().toLowerCase(Locale.ENGLISH).startsWith(MASTER_DATA_FILE_NAME)) {
                    return IOUtils.toString(zipFile, StandardCharsets.UTF_8);
                }
            }
            throw new IllegalArgumentException("The file '" + MASTER_DATA_FILE_NAME + "' could not be found in the zip file.");
        } catch (HttpException e) {
            // By default, there is no authentication or permissions needed to download the master
            // data but it's possible to put some restrictions in the configuration.
            // Therefore, it's possible the backend would return a 403 (though it should return a
            // 401) when trying to download the zip.
            if (e.getCode() == 401 || e.getCode() == 403) {
                throw new UserNotAuthenticatedException("Backend return '"+e.getCode()+"' while trying to download master data.", e);
            }
            else throw e;
        }
    }
}
