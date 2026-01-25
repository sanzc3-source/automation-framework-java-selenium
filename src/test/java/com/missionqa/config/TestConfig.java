package com.missionqa.config;

import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = TestConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new IllegalStateException("config.properties not found in src/test/resources");
            }
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private TestConfig() {}

    public static String getProperty(String key) {
        String val = props.getProperty(key);
        if (val == null) {
            throw new IllegalArgumentException("Missing config key in config.properties: " + key);
        }
        return val.trim();
    }

    // Optional convenience helpers (safe)
    public static String uiBaseUrl() { return getProperty("ui.baseUrl"); }
    public static String browser() { return getProperty("browser"); }
    public static String screenshotDir() { return getProperty("screenshot.dir"); }
    public static String apiBaseUrl() { return getProperty("api.baseUrl"); }
}
