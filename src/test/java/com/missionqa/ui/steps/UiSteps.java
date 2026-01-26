package com.missionqa.ui.steps;

import com.missionqa.core.DriverProvider;
import com.missionqa.ui.pages.*;
import io.cucumber.java.en.*;

import org.openqa.selenium.WebDriver;

import java.util.Arrays;
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

        // -----------------------------
        // Background steps
        // -----------------------------

        @Given("I am on the login page")
        public void iAmOnTheLoginPage() {
                // Hook already navigates to base URL. We just sanity-check.
                assertTrue(driver().getCurrentUrl().contains("saucedemo"), "Not on SauceDemo site");
        }

        @Given("I login via UI as {string} with password {string}")
        public void iLoginViaUI(String username, String password) {
                loginPage().login(username, password);
        }

        @Given("I should be on the inventory page")
        public void iShouldBeOnTheInventoryPage() {
                assertTrue(inventoryPage().isLoaded(), "Inventory page did not load");
        }

        // -----------------------------
        // Scenario steps
        // -----------------------------

        // IMPORTANT:
        // We support ONLY "When ..." for this step to avoid duplicate-step conflicts.
        @When("I add these items to the cart {string}")
        public void iAddTheseItemsToTheCart(String items) {
                // Supports BOTH:
                // 1) literal "\n" sequences in the Examples table
                // 2) actual newline characters in the runtime string
                List<String> itemList = Arrays.stream(items.split("\\\\n|\\r?\\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

                inventoryPage().addItems(itemList);
        }

        @Then("the cart badge count should be {int}")
        public void theCartBadgeCountShouldBe(int expected) {
                assertEquals(inventoryPage().cartCount(), expected, "Cart badge count mismatch");
        }

        @When("I open the cart")
        public void iOpenTheCart() {
                inventoryPage().openCart();
        }

        @When("I remove item {string}")
        public void iRemoveItem(String itemName) {
                cartPage().removeItemByName(itemName);
        }

        @When("I proceed to checkout")
        public void iProceedToCheckout() {
                cartPage().clickCheckout();
        }

        @When("I enter checkout info first {string} last {string} zip {string}")
        public void iEnterCheckoutInfo(String first, String last, String zip) {
                checkoutInfoPage().enterCheckoutInfo(first, last, zip);
        }

        @When("I continue to the overview page")
        public void iContinueToTheOverviewPage() {
                checkoutInfoPage().clickContinue();
        }

        @Then("the item total should equal the sum of item prices")
        public void itemTotalShouldEqualSumOfPrices() {
                assertEquals(
                        checkoutOverviewPage().displayedItemTotal(),
                        checkoutOverviewPage().sumOfItemPrices(),
                        "Displayed item total does not match sum of item prices"
                );
        }

        @Then("the tax rate should be {int} percent")
        public void taxRateShouldBe(int expectedPercent) {
                assertEquals(
                        checkoutOverviewPage().taxRatePercentRoundedToWhole().intValue(),
                        expectedPercent,
                        "Tax rate percent mismatch"
                );
        }
}
