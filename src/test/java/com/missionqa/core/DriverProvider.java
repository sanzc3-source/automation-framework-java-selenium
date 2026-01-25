package com.missionqa.core;

import org.openqa.selenium.WebDriver;

/**
 * Thread-safe WebDriver storage for Cucumber scenarios.
 * Hooks should call DriverProvider.set(driver) in @Before("@ui")
 * and DriverProvider.remove() in @After("@ui").
 */
public final class DriverProvider {

    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();

    private DriverProvider() {
        // utility class
    }

    public static void set(WebDriver driver) {
        TL_DRIVER.set(driver);
    }

    public static WebDriver get() {
        WebDriver driver = TL_DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is null. Did Hooks initialize the driver?");
        }
        return driver;
    }

    public static void remove() {
        TL_DRIVER.remove();
    }
}
