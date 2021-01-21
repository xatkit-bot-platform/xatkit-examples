package com.xatkit.example;

import fr.inria.atlanmod.commons.log.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

public class PropertiesManager {

    private static PropertiesManager propertiesManager;
    private final Properties properties = new Properties();
    private final String propertiesDefaultFileName = "bot-private.properties";

    private PropertiesManager() throws IOException {
        readProperties();
    }

    public static PropertiesManager get() throws IOException {
        Log.debug("Returning PropertiesManager");
        if (propertiesManager == null) {
            Log.debug("PropertiesManager not found, creating new PropertiesManager.");
            propertiesManager = new PropertiesManager();
        }
        return propertiesManager;
    }

    private void readProperties() throws IOException {
        readProperties(propertiesDefaultFileName);
    }

    private void readProperties(String fileName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

        if (inputStream == null) {
            String msg = MessageFormat.format("{0} not found.",fileName);
            Log.error(msg);
            throw new FileNotFoundException(msg);
        }

        properties.load(inputStream);
    }

    public String getValue(String key) {
        return properties.getProperty(key);
    }

}
