package com.missionqa.core;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class SessionInjector {
    public static void setLocalStorage(WebDriver driver, Map<String, String> kv) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        kv.forEach((k, v) -> js.executeScript("window.localStorage.setItem(arguments[0], arguments[1]);", k, v));
    }
}
