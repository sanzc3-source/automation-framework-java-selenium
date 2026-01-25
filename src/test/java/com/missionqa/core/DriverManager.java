package com.missionqa.core;

import com.missionqa.config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public final class DriverManager {

    private DriverManager() {}

    public static WebDriver createDriver() {
        String browser = TestConfig.getProperty("browser").toLowerCase();

        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver();

            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();

            case "chromeheadless":
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--headless=new");
                return new ChromeDriver(opts);

            default:
                throw new IllegalArgumentException("Unsupported browser in config.properties: " + browser);
        }
    }

    public static void quitDriver(WebDriver driver) {
        if (driver == null) return;
        try {
            driver.quit();
        } catch (Exception ignored) {
        }
    }
}
