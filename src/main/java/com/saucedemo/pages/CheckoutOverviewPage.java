package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class CheckoutOverviewPage {

    private final CartItemList items;
    private final Locator itemTotal;
    private final Locator tax;
    private final Locator total;
    private final Locator finishButton;

    public CheckoutOverviewPage(Page page) {
        items = new CartItemList(page);
        itemTotal = page.getByTestId("subtotal-label");
        tax = page.getByTestId("tax-label");
        total = page.getByTestId("total-label");
        finishButton = page.getByTestId("finish");
    }

    public CartItemList items() {
        return items;
    }

    public Locator itemTotal() {
        return itemTotal;
    }

    public Locator tax() {
        return tax;
    }

    public Locator total() {
        return total;
    }

    public Locator finishButton() {
        return finishButton;
    }
}
