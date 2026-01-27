package com.missionqa.core;

import com.missionqa.config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class DriverManager {

    private static final boolean USE_WDM =
            Boolean.parseBoolean(System.getProperty("useWdm", "false"));

    private DriverManager() {}

    public static WebDriver createDriver() {
        String browser = System.getenv("BROWSER");
        if (browser == null || browser.isBlank()) {
            browser = System.getProperty("browser");
        }
        if (browser == null || browser.isBlank()) {
            browser = TestConfig.getProperty("browser");
        }
        browser = browser.toLowerCase();

        String remoteUrl = System.getenv("SELENIUM_REMOTE_URL");

        // If running in Docker with Selenium container, use RemoteWebDriver
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            return createRemoteDriver(browser, remoteUrl);
        }

        // Local execution (Selenium Manager by default)
        switch (browser) {
            case "chrome":
                setupIfNeeded("chrome");
                return new ChromeDriver(buildChromeOptions(false));

            case "chromeheadless":
                setupIfNeeded("chrome");
                return new ChromeDriver(buildChromeOptions(true));

            case "edge":
                setupIfNeeded("edge");
                return new EdgeDriver(new EdgeOptions());

            case "firefox":
                setupIfNeeded("firefox");
                return new FirefoxDriver(buildFirefoxOptions(false));

            case "firefoxheadless":
                setupIfNeeded("firefox");
                return new FirefoxDriver(buildFirefoxOptions(true));

            default:
                throw new IllegalArgumentException("Unsupported browser in config.properties: " + browser);
        }
    }

    private static WebDriver createRemoteDriver(String browser, String remoteUrl) {
        try {
            URL gridUrl = new URL(remoteUrl);

            switch (browser) {
                case "chrome":
                case "chromeheadless":
                    boolean chromeHeadless = browser.equals("chromeheadless");
                    return new RemoteWebDriver(gridUrl, buildChromeOptions(chromeHeadless));

                case "edge":
                    return new RemoteWebDriver(gridUrl, new EdgeOptions());

                case "firefox":
                case "firefoxheadless":
                    boolean ffHeadless = browser.equals("firefoxheadless");
                    return new RemoteWebDriver(gridUrl, buildFirefoxOptions(ffHeadless));

                default:
                    throw new IllegalArgumentException("Unsupported browser in config.properties: " + browser);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RemoteWebDriver using SELENIUM_REMOTE_URL=" + remoteUrl, e);
        }
    }

    private static void setupIfNeeded(String browser) {
        if (!USE_WDM) return;

        System.setProperty("wdm.avoidHttpClient", "true");

        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                break;
            default:
                // no-op
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

        // Reduce noise + stability
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-extensions");
        options.addArguments("--window-size=1920,1080");

        // Container-safe flags
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-setuid-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // Disable password leak detection popup
        options.addArguments("--disable-features=PasswordLeakDetection");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);

        return options;
    }

    private static FirefoxOptions buildFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }

    public static void quitDriver(WebDriver driver) {
        if (driver == null) return;
        try {
            driver.quit();
        } catch (Exception ignored) {}
    }
}
