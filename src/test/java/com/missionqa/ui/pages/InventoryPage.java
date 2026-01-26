package com.missionqa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
        String nameXpath = String.format(
                "//div[contains(@class,'inventory_item_name') and normalize-space(.)='%s']",
                itemName
        );

        // Find the item name element based on exact visible text
        By itemNameBy = By.xpath(nameXpath);
        WebElement nameEl = waits.visible(itemNameBy);

        // Climb to the inventory item container
        WebElement itemContainer = nameEl.findElement(
                By.xpath("./ancestor::div[contains(@class,'inventory_item')]")
        );

        // Click the Add to cart button within that container
        WebElement addBtn = itemContainer.findElement(
                By.xpath(".//button[contains(normalize-space(.),'Add to cart')]")
        );
        addBtn.click();
    }

    public void addItems(List<String> items) {
        for (String item : items) {
            addItemByName(item);
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
