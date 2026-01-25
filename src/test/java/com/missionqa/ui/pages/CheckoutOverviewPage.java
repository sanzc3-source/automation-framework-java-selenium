package com.missionqa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CheckoutOverviewPage extends BasePage {

    private final By itemPrices = By.cssSelector(".inventory_item_price");
    private final By summaryItemTotal = By.cssSelector(".summary_subtotal_label");
    private final By summaryTax = By.cssSelector(".summary_tax_label");

    public CheckoutOverviewPage(WebDriver driver) {
        super(driver);
    }

    public BigDecimal sumOfItemPrices() {
        List<org.openqa.selenium.WebElement> prices = driver.findElements(itemPrices);
        BigDecimal sum = BigDecimal.ZERO;

        for (org.openqa.selenium.WebElement el : prices) {
            sum = sum.add(parseMoney(el.getText()));
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal displayedItemTotal() {
        // Example: "Item total: $103.96"
        String txt = waits.visible(summaryItemTotal).getText();
        return parseMoney(extractDollars(txt));
    }

    public BigDecimal displayedTax() {
        // Example: "Tax: $8.32"
        String txt = waits.visible(summaryTax).getText();
        return parseMoney(extractDollars(txt));
    }

    public BigDecimal taxRatePercentRoundedToWhole() {
        BigDecimal itemTotal = displayedItemTotal();
        BigDecimal tax = displayedTax();

        if (itemTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = tax
                .divide(itemTotal, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        // We expect 8% (whole number). Round normally.
        return rate.setScale(0, RoundingMode.HALF_UP);
    }

    private String extractDollars(String txt) {
        // returns "$103.96" portion
        int idx = txt.indexOf('$');
        return (idx >= 0) ? txt.substring(idx) : txt;
    }

    private BigDecimal parseMoney(String money) {
        // "$103.96" -> 103.96
        String cleaned = money.replace("$", "").trim();
        return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
    }
}
