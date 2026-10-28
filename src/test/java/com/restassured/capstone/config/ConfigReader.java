package com.restassured.capstone.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader - Property Reader utility (Assignment 2).
 *
 * <p>Loads the environment specific property file (qa.properties / uat.properties)
 * from the classpath folder {@code config/}. The environment is chosen from the
 * command line, so the environment can be switched without changing any code:</p>
 *
 * <pre>
 *   mvn test -Denv=qa
 *   mvn test -Denv=uat
 * </pre>
 *
 * <p>If {@code -Denv} is not supplied, "qa" is used as the default.</p>
 */
public final class ConfigReader {

    /** Default environment used when -Denv is not provided. */
    private static final String DEFAULT_ENV = "qa";

    /** Name of the environment currently in use (qa / uat). */
    private static final String ENV = System.getProperty("env", DEFAULT_ENV).toLowerCase();

    /** Holds all key/value pairs of the loaded property file. */
    private static final Properties PROPERTIES = new Properties();

    // Static block: runs once, when the class is first used.
    static {
        String fileName = "config/" + ENV + ".properties";
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new IllegalStateException("Property file not found on classpath: " + fileName
                        + ". Valid values for -Denv are 'qa' or 'uat'.");
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read property file: " + fileName, e);
        }
    }

    // Private constructor: this is a utility class, nobody should create objects of it.
    private ConfigReader() {
    }

    /** @return the name of the active environment, e.g. "qa". */
    public static String getEnvironment() {
        return ENV;
    }

    /**
     * Reads any property value from the active property file.
     *
     * @param key property name, e.g. "baseUrl"
     * @return the trimmed value
     * @throws IllegalArgumentException if the key does not exist
     */
    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Key '" + key + "' not found in " + ENV + ".properties");
        }
        return value.trim();
    }

    /** @return the base URL of the application (read from the property file). */
    public static String getBaseUrl() {
        return get("baseUrl");
    }

    /** @return the common API path prefix, e.g. "public/v2". */
    public static String getBasePath() {
        return get("basePath");
    }

    /** @return the maximum allowed response time in milliseconds. */
    public static long getMaxResponseTime() {
        return Long.parseLong(get("maxResponseTime"));
    }

    /**
     * Returns the bearer token used for authentication.
     *
     * <p>Lookup order (first non-empty wins):</p>
     * <ol>
     *   <li>JVM argument      : {@code -Dtoken=xxxx}</li>
     *   <li>Environment var   : {@code GOREST_TOKEN}</li>
     *   <li>Property file     : {@code token=xxxx}</li>
     * </ol>
     * This keeps the secret out of source control.
     */
    public static String getToken() {
        String token = System.getProperty("token"); // looks token in command -Dtoken=xxxx
        if (isBlank(token)) {
            token = System.getenv("GOREST_TOKEN"); // looks for env variable
        }
        if (isBlank(token)) {
            token = PROPERTIES.getProperty("token"); // looks in properties file
        }
        if (isBlank(token)) {
            throw new IllegalStateException("No API token supplied. Run with -Dtoken=YOUR_TOKEN "
                    + "or set the GOREST_TOKEN environment variable "
                    + "(free token: https://gorest.co.in/consumer/login).");
        }
        return token.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
