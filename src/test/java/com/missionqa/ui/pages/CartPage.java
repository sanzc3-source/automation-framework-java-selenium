package com.missionqa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CartPage extends BasePage {

    private final By checkoutBtn = By.id("checkout");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public void removeItemByName(String itemName) {
        // On cart page: each cart item has name + a remove button in the same container
        By itemContainer = By.xpath("//div[@class='cart_item']//div[@class='inventory_item_name' and text()='" + itemName + "']/ancestor::div[@class='cart_item']");
        By removeBtn = By.xpath(".//button[contains(@id,'remove')]");
        waits.visible(itemContainer).findElement(removeBtn).click();
    }

    public void clickCheckout() {
        waits.visible(checkoutBtn).click();
    }
}
