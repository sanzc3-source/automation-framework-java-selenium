package com.missionqa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckoutInfoPage extends BasePage {

    private final By firstName = By.id("first-name");
    private final By lastName = By.id("last-name");
    private final By postalCode = By.id("postal-code");
    private final By continueBtn = By.id("continue");

    public CheckoutInfoPage(WebDriver driver) {
        super(driver);
    }

    public void enterCheckoutInfo(String first, String last, String zip) {
        waits.visible(firstName).clear();
        waits.visible(firstName).sendKeys(first);

        waits.visible(lastName).clear();
        waits.visible(lastName).sendKeys(last);

        waits.visible(postalCode).clear();
        waits.visible(postalCode).sendKeys(zip);
    }

    public void clickContinue() {
        waits.visible(continueBtn).click();
    }
}
