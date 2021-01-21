package com.xatkit.example;

import com.xatkit.plugins.messenger.platform.entity.Attachment;
import com.xatkit.plugins.messenger.platform.entity.File;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

import java.io.*;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private static FileManager fileManager;
    private Map<String, File> files;

    private FileManager() {
        this.files = new HashMap<>();
    }

    public static FileManager get() {
        Log.debug("Returning FileManager");
        if (fileManager == null) {
            Log.debug("FileManager not found, creating new FileManager.");
            fileManager = new FileManager();
        }
        return fileManager;
    }

    public File loadFileToName(Attachment.AttachmentType type, String fileName, String name) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            String msg = MessageFormat.format("{0} not found.",fileName);
            Log.error(msg);
            throw new FileNotFoundException(msg);
        }

        Tika tika = new Tika();
        String mimeType = tika.detect(inputStream);
        if (mimeType == null) mimeType = tika.detect(fileName);
        if (mimeType == null) mimeType = URLConnection.guessContentTypeFromStream(inputStream);
        if (mimeType == null) mimeType = URLConnection.guessContentTypeFromName(fileName);

        //https://stackoverflow.com/a/35465681
        java.io.File tempFile = java.io.File.createTempFile(String.valueOf(inputStream.hashCode()), FilenameUtils.getExtension(fileName));
        tempFile.deleteOnExit();

        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        fileOutputStream.close();

        File file = new File(type, tempFile, mimeType);
        files.put(name, file);
        return getFile(name);
    }

    public File getFile(String name) {
        File file = null;
        if (files.containsKey(name)) file = files.get(name);
        else Log.error("Attempted to use a file by the name \"{0}\", but it is not loaded.", name);
        return file;
    }
}
