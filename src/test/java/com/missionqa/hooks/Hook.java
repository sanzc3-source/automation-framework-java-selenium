package com.missionqa.hooks;

import com.missionqa.config.TestConfig;
import com.missionqa.core.DriverManager;
import com.missionqa.core.DriverProvider;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.time.Duration;

public class Hook {

    private static final int WAIT_SEC = 20;

    @Before("@ui")
    public void beforeUi() {
        WebDriver driver = DriverManager.createDriver();
        DriverProvider.set(driver);

        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(WAIT_SEC));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(WAIT_SEC));
        // driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(WAIT_SEC)); // remove for baseline stability

        driver.get(TestConfig.getProperty("ui.baseUrl"));
    }

    @After("@ui")
    public void afterUi(Scenario scenario) {
        WebDriver driver = null;
        try {
            driver = DriverProvider.get();
        } catch (Exception ignored) {}

        if (driver != null && scenario != null && scenario.isFailed()) {
            try {
                String browser = TestConfig.getProperty("browser");
                String screenshotDir = TestConfig.getProperty("screenshot.dir");

                File dir = new File(screenshotDir);
                if (!dir.exists()) dir.mkdirs();

                String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9-_]", "_");
                String fileName = safeName + "_" + browser + "_" + System.currentTimeMillis() + ".png";

                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                FileUtils.copyFile(src, new File(dir, fileName));
            } catch (Exception ignored) {}
        }

        DriverManager.quitDriver(driver);
        DriverProvider.remove();
    }

    @Before("@api")
    public void beforeApi() {
        // API setup later if needed
    }

    @After("@api")
    public void afterApi() {
        // API cleanup later if needed
    }
}
