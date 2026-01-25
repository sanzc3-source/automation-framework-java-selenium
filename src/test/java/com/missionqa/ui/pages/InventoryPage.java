package com.missionqa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class InventoryPage extends BasePage {

    private final By title = By.cssSelector(".title");
    private final By cartBadge = By.cssSelector(".shopping_cart_badge");
    private final By cartLink = By.cssSelector(".shopping_cart_link");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return waits.visible(title).getText().contains("Products");
    }

    public void addItemByName(String itemName) {
        By itemContainer = By.xpath("//div[@class='inventory_item']//div[@class='inventory_item_name' and text()='" + itemName + "']/ancestor::div[@class='inventory_item']");
        By addBtn = By.xpath(".//button[contains(@id,'add-to-cart')]");
        waits.visible(itemContainer).findElement(addBtn).click();
    }

    public void addItems(List<String> itemNames) {
        for (String name : itemNames) {
            addItemByName(name);
        }
    }

    public int cartCount() {
        if (driver.findElements(cartBadge).isEmpty()) return 0;
        return Integer.parseInt(driver.findElement(cartBadge).getText());
    }

    public void openCart() {
        waits.visible(cartLink).click();
    }
}
