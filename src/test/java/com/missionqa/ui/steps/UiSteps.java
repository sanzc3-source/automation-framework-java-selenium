package com.missionqa.ui.steps;

import com.missionqa.core.DriverProvider;
import com.missionqa.ui.pages.*;
import cucumber.api.DataTable;
import cucumber.api.java.en.*;

import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

public class UiSteps {

    private WebDriver driver() {
        return DriverProvider.get();
    }

    private LoginPage loginPage() { return new LoginPage(driver()); }
    private InventoryPage inventoryPage() { return new InventoryPage(driver()); }
    private CartPage cartPage() { return new CartPage(driver()); }
    private CheckoutInfoPage checkoutInfoPage() { return new CheckoutInfoPage(driver()); }
    private CheckoutOverviewPage checkoutOverviewPage() { return new CheckoutOverviewPage(driver()); }

    @Given("^I am on the login page$")
    public void iAmOnTheLoginPage() {
        // Hooks already navigated to baseUrl, so this can be a no-op or assert.
        // Keep it simple:
        assertTrue(driver().getCurrentUrl().contains("saucedemo"), "Not on SauceDemo site");
    }

    @And("^I login via UI as \"([^\"]*)\" with password \"([^\"]*)\"$")
    public void iLoginViaUI(String username, String password) {
        loginPage().login(username, password);
    }

    @And("^I should be on the inventory page$")
    public void iShouldBeOnInventory() {
        assertTrue(inventoryPage().isLoaded(), "Inventory page did not load");
    }

    @When("^I add these items to the cart$")
    public void iAddTheseItems(DataTable dt) {
        List<String> items = dt.asList(String.class).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !"item".equalsIgnoreCase(s))
                .collect(Collectors.toList());

        inventoryPage().addItems(items);
    }

    @Then("^the cart badge count should be (\\d+)$")
    public void cartBadgeCountShouldBe(int expected) {
        assertEquals(inventoryPage().cartCount(), expected, "Cart badge count mismatch");
    }

    @When("^I open the cart$")
    public void iOpenCart() {
        inventoryPage().openCart();
    }

    @When("^I remove item \"([^\"]*)\"$")
    public void iRemoveItem(String itemName) {
        cartPage().removeItemByName(itemName);
    }

    @When("^I proceed to checkout$")
    public void iProceedToCheckout() {
        cartPage().clickCheckout();
    }

    @When("^I enter checkout info first \"([^\"]*)\" last \"([^\"]*)\" zip \"([^\"]*)\"$")
    public void iEnterCheckoutInfo(String first, String last, String zip) {
        checkoutInfoPage().enterCheckoutInfo(first, last, zip);
    }

    @When("^I continue to the overview page$")
    public void iContinueToOverview() {
        checkoutInfoPage().clickContinue();
    }

    @Then("^the item total should equal the sum of item prices$")
    public void itemTotalMatchesSum() {
        assertEquals(
                checkoutOverviewPage().displayedItemTotal(),
                checkoutOverviewPage().sumOfItemPrices(),
                "Displayed item total does not match sum of item prices"
        );
    }

    @Then("^the tax rate should be (\\d+) percent$")
    public void taxRateShouldBe(int expectedPercent) {
        assertEquals(
                checkoutOverviewPage().taxRatePercentRoundedToWhole().intValue(),
                expectedPercent,
                "Tax rate percent mismatch"
        );
    }
}
