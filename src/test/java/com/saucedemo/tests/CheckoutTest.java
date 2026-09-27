package com.saucedemo.tests;

import com.saucedemo.api.UsersApi;
import com.saucedemo.base.BaseTest;
import com.saucedemo.data.Customer;
import com.saucedemo.data.Product;
import com.saucedemo.pages.CartItemList;
import com.saucedemo.pages.CartPage;
import com.saucedemo.pages.CheckoutCompletePage;
import com.saucedemo.pages.CheckoutInformationPage;
import com.saucedemo.pages.CheckoutOverviewPage;
import com.saucedemo.pages.Header;
import com.saucedemo.pages.InventoryPage;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.pages.ProductDetailsPage;
import com.saucedemo.utils.Price;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.saucedemo.data.Products.FLEECE_JACKET;
import static com.saucedemo.data.Products.ONESIE;
import static com.saucedemo.data.User.STANDARD_USER;
import static org.testng.Assert.assertEquals;

public class CheckoutTest extends BaseTest {

    @Test(description = "Standard user buys a Fleece Jacket and a Onesie with customer data from JSONPlaceholder")
    public void standardUserCanCompleteCheckout() {
        Header header = new Header(page);
        LoginPage loginPage = new LoginPage(page);
        InventoryPage inventoryPage = new InventoryPage(page);
        ProductDetailsPage productDetailsPage = new ProductDetailsPage(page);
        CartPage cartPage = new CartPage(page);
        CheckoutInformationPage checkoutInformationPage = new CheckoutInformationPage(page);
        CheckoutOverviewPage checkoutOverviewPage = new CheckoutOverviewPage(page);
        CheckoutCompletePage checkoutCompletePage = new CheckoutCompletePage(page);
        SoftAssert softAssert = new SoftAssert();
        List<Product> orderedProducts = List.of(FLEECE_JACKET, ONESIE);

        // Get customer data from JSONPlaceholder
        Customer customer = UsersApi.getRandomCustomer();
        Reporter.log("Customer from JSONPlaceholder: " + customer, true);

        // Log in as standard user
        loginPage.open();
        loginPage.login(STANDARD_USER.username(), STANDARD_USER.password());
        assertThat(page).hasURL(Pattern.compile(".*/inventory\\.html"));
        assertThat(header.pageTitle()).hasText("Products");

        // Add Fleece Jacket from the product list
        inventoryPage.addToCart(FLEECE_JACKET.name());
        assertThat(header.cartBadge()).hasText("1");

        // Add Onesie from its product details page
        inventoryPage.openProductDetails(ONESIE.name());
        assertThat(page).hasURL(Pattern.compile(".*/inventory-item\\.html.*"));
        assertThat(productDetailsPage.name()).hasText(ONESIE.name());
        assertThat(productDetailsPage.price()).hasText(Price.format(ONESIE.price()));
        productDetailsPage.addToCartButton().click();

        // Cart badge shows 2 products
        assertThat(header.cartBadge()).hasText("2");

        // Cart shows the added products with correct names and prices
        header.cartLink().click();
        assertThat(header.pageTitle()).hasText("Your Cart");
        assertItems(cartPage.items(), orderedProducts);

        // Enter customer information
        cartPage.checkoutButton().click();
        assertThat(header.pageTitle()).hasText("Checkout: Your Information");
        checkoutInformationPage.fillCustomerInfo(customer);
        checkoutInformationPage.continueButton().click();

        // Overview shows the order items and totals
        assertThat(header.pageTitle()).hasText("Checkout: Overview");
        assertItems(checkoutOverviewPage.items(), orderedProducts);

        BigDecimal itemTotal = orderedProducts.stream()
                .map(Product::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Soft assertion: currently fails because of BUG-01 (shown as "$57.980000000000004").
        softAssert.assertEquals(checkoutOverviewPage.itemTotal().innerText(),
                "Item total: " + Price.format(itemTotal), "Item total on Overview");

        BigDecimal tax = Price.parse(checkoutOverviewPage.tax().innerText());
        BigDecimal total = Price.parse(checkoutOverviewPage.total().innerText());
        assertEquals(total, itemTotal.add(tax), "Total should equal item total + tax");

        // Finish the order and see the Complete page
        checkoutOverviewPage.finishButton().click();
        assertThat(page).hasURL(Pattern.compile(".*/checkout-complete\\.html"));
        assertThat(header.pageTitle()).hasText("Checkout: Complete!");
        assertThat(checkoutCompletePage.header()).hasText("Thank you for your order!");
        assertThat(checkoutCompletePage.message()).containsText("Your order has been dispatched");
        assertThat(header.cartBadge()).isHidden();

        softAssert.assertAll();
    }

    private void assertItems(CartItemList items, List<Product> expected) {
        assertThat(items.names()).hasText(expected.stream().map(Product::name).toArray(String[]::new));
        assertThat(items.prices()).hasText(expected.stream().map(p -> Price.format(p.price())).toArray(String[]::new));
        assertThat(items.quantities()).hasText(expected.stream().map(p -> "1").toArray(String[]::new));
    }
}
