package com.missionqa.hooks;

import com.missionqa.config.TestConfig;
import com.missionqa.core.DriverManager;
import com.missionqa.core.DriverProvider;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Hook {

    private static final int WAIT_SEC = 20;

    @Before("@ui")
    public void beforeUi() {
        WebDriver driver = DriverManager.createDriver();
        DriverProvider.set(driver);

        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(WAIT_SEC, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(WAIT_SEC, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(WAIT_SEC, TimeUnit.SECONDS);

        driver.get(TestConfig.getProperty("ui.baseUrl"));
    }

    @After("@ui")
    public void afterUi(Scenario scenario) {
        WebDriver driver = null;
        try {
            driver = DriverProvider.get();
        } catch (Exception ignored) {}

        // Screenshot only on failure
        if (driver != null && scenario != null && scenario.isFailed()) {
            try {
                String browser = TestConfig.getProperty("browser");
                String screenshotDir = TestConfig.getProperty("screenshot.dir");

                String fileName = scenario.getName().replace(" ", "")
                        + new Timestamp(new Date().getTime()).toString().replaceAll("[^a-zA-Z0-9]", "")
                        + "_" + browser + ".png";

                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                FileUtils.copyFile(src, new File(screenshotDir + File.separator + fileName));
            } catch (Exception ignored) {}
        }

        DriverManager.quitDriver(driver);
        DriverProvider.remove();
    }

    @Before("@api")
    public void beforeApi() {
        // API setup lives here later (tokens, headers, etc.)
    }

    @After("@api")
    public void afterApi() {
        // API cleanup if needed later
    }
}
