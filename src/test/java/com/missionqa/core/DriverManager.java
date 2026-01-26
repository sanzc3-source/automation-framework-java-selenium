package com.missionqa.core;

import com.missionqa.config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.HashMap;
import java.util.Map;

public final class DriverManager {

    private DriverManager() {}

    public static WebDriver createDriver() {
        String browser = TestConfig.getProperty("browser").toLowerCase();

        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver(buildChromeOptions(false));

            case "chromeheadless":
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver(buildChromeOptions(true));

            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();

            default:
                throw new IllegalArgumentException("Unsupported browser in config.properties: " + browser);
        }
    }

    private static ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }

        // Fresh profile each run
        String tmpProfile = System.getProperty("java.io.tmpdir")
                + "/missionqa-chrome-" + System.currentTimeMillis();
        options.addArguments("--user-data-dir=" + tmpProfile);

        // Reduce Chrome UI noise
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");

        // KEY: disable password leak detection popup
        options.addArguments("--disable-features=PasswordLeakDetection");

        // Disable password manager + credential prompts + leak detection
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false); // <- important
        options.setExperimentalOption("prefs", prefs);

        return options;
    }

    public static void quitDriver(WebDriver driver) {
        if (driver == null) return;
        try {
            driver.quit();
        } catch (Exception ignored) {
        }
    }
}
