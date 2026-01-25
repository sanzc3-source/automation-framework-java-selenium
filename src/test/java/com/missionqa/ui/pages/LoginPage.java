package com.missionqa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private final By username = By.id("user-name");
    private final By password = By.id("password");
    private final By loginBtn = By.id("login-button");
    private final By error = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage enterUsername(String user) {
        waits.visible(username).clear();
        waits.visible(username).sendKeys(user);
        return this;
    }

    public LoginPage enterPassword(String pass) {
        waits.visible(password).clear();
        waits.visible(password).sendKeys(pass);
        return this;
    }

    public void submit() {
        waits.visible(loginBtn).click();
    }

    public void login(String user, String pass) {
        enterUsername(user);
        enterPassword(pass);
        submit();
    }

    public boolean hasError() {
        return !driver.findElements(error).isEmpty();
    }

    public String errorText() {
        return hasError() ? driver.findElement(error).getText() : "";
    }
}
