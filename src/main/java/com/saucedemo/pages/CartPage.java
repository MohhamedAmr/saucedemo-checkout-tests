package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class CartPage {

    private final CartItemList items;
    private final Locator checkoutButton;

    public CartPage(Page page) {
        items = new CartItemList(page);
        checkoutButton = page.getByTestId("checkout");
    }

    public CartItemList items() {
        return items;
    }

    public Locator checkoutButton() {
        return checkoutButton;
    }
}
