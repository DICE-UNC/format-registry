package edu.drexel.acin.sf.util;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 1/20/12
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigReader {
    public static final String CONFIG_FILENAME = "irods.properties";
    public static final String CONFIG_DIRECTORY_NAME = ".format_registry";

    private static final File CONFIG_DIRECTORY = new File(System.getProperty("user.home"), CONFIG_DIRECTORY_NAME);
    private static final File CONFIG_FILE = new File(CONFIG_DIRECTORY, CONFIG_FILENAME);

    public static final String RODS_USERNAME_KEY = "rods.user";
    public static final String RODS_PASSWORD_KEY = "rods.pass";
    public static final String RODS_ZONE_KEY = "rods.zone";
    public static final String RODS_RESOURCE_KEY = "rods.resource";
    public static final String RODS_HOST_KEY = "rods.host";
    public static final String RODS_PORT_KEY = "rods.port";
    public static final String RODS_HOMEDIR_KEY = "rods.home";
    public static final String COLLECTION_KEY = "formatregistry.home";
    public static final String HTTP_PORT_KEY = "http.port";
    public static final String LOCAL_PATH_KEY = "filestore.path";
    public static final String DATABASE_URL_KEY = "db.url";
    public static final String DATABASE_USERNAME_KEY = "db.user";
    public static final String DATABASE_PASSWORD_KEY = "db.pass";

    public static final List<String> REQUIRED_KEYS = Collections.unmodifiableList(Arrays.asList(
            RODS_USERNAME_KEY,
            RODS_PASSWORD_KEY,
            RODS_ZONE_KEY,
            RODS_RESOURCE_KEY,
            RODS_HOST_KEY,
            RODS_PORT_KEY,
            RODS_HOMEDIR_KEY,
            COLLECTION_KEY,
            HTTP_PORT_KEY,
            LOCAL_PATH_KEY,
            DATABASE_URL_KEY,
            DATABASE_USERNAME_KEY,
            DATABASE_PASSWORD_KEY
    ));

    public static Properties getProperties() throws IOException {
        if (!CONFIG_DIRECTORY.exists() && !CONFIG_DIRECTORY.mkdirs()) {
            throw new IOException("Unable to create config directory: " + CONFIG_DIRECTORY.getPath());
        }
        if (!CONFIG_DIRECTORY.isDirectory()) {
            throw new IOException("Non-directory in place of config directory: " + CONFIG_DIRECTORY.getPath());
        }
        if (!CONFIG_FILE.exists()) {
            writeEmptyConfig();
            throw new IOException("Config file not found. Empty file written to: " + CONFIG_FILE.getPath());
        }
        final Properties props = new Properties();
        props.load(new FileInputStream(CONFIG_FILE));

        for (String key : REQUIRED_KEYS) {
            if (!props.containsKey(key)) {
                throw new IOException("Missing required property " + key + " in " + CONFIG_FILE.getPath());
            }
        }
        return props;
    }

    private static void writeEmptyConfig() throws IOException {
        try (Writer w = new FileWriter(CONFIG_FILE)) {
            for (String key : REQUIRED_KEYS) {
                w.write(key);
                w.write('=');
                w.write('\n');
            }
            w.flush();
        }
    }
}
