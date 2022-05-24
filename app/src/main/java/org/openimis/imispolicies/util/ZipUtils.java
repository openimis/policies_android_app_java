package org.openimis.imispolicies.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.openimis.imispolicies.tools.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ZipUtils {
    private static final String LOG_TAG = "ZIPUTILS";

    public static File zipDirectories(File outputFile, String zipPassword, File... directories) {
        ArrayList<File> filesToAdd = new ArrayList<>();
        for (File directory : directories) {
            if (!directory.exists() || !directory.isDirectory()) {
                Log.w(LOG_TAG, "Provided file is not a directory: " + directory);
                continue;
            }

            File[] files = directory.listFiles();
            if (files == null) {
                Log.w(LOG_TAG, "Reading a directory filed: " + directory);
                continue;
            }

            Collections.addAll(filesToAdd, files);
        }

        return zipFiles(filesToAdd, outputFile, zipPassword);
    }

    public static File zipFiles(ArrayList<File> filesToAdd, File destinationFile, String password) {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            if (password.length() > 0) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
                parameters.setPassword(password);
            }

            ZipFile zipFile = new ZipFile(destinationFile);
            zipFile.addFiles(filesToAdd, parameters);
            return zipFile.getFile();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while compressing", e);
        }
        return null;
    }

    public static void zipPath(String targetPath, String destinationFilePath, String password) {
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            if (password.length() > 0) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
                parameters.setPassword(password);
            }

            ZipFile zipFile = new ZipFile(destinationFilePath);

            File targetFile = new File(targetPath);
            if (targetFile.exists()) {
                zipFile.addFile(targetFile, parameters);
            } else if (targetFile.isDirectory()) {
                zipFile.addFolder(targetFile, parameters);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while compressing", e);
        }
    }

    public static void unzipFile(File targetZipFile, File destinationFolder, String password) {
        try {
            ZipFile zipFile = new ZipFile(targetZipFile);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(destinationFolder.getPath());

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while decompressing", e);
        }
    }

    public static void unzipPath(String targetZipFilePath, String destinationFolderPath, String password) {
        try {
            ZipFile zipFile = new ZipFile(targetZipFilePath);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(destinationFolderPath);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while decompressing", e);
        }
    }
}
