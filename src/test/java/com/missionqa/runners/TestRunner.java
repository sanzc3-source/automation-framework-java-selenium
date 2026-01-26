package com.missionqa.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.missionqa.hooks",
                "com.missionqa.ui.steps",
                "com.missionqa.api.steps"
        },
        plugin = {
                "pretty",
                "html:target/cucumber-reports.html",
                "json:target/cucumber.json"
        },
        tags = "@ui"
)
public class TestRunner extends AbstractTestNGCucumberTests {
}
