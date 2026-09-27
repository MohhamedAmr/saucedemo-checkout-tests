package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

// Item list shared by the Cart and Checkout Overview pages (same markup on both).
public class CartItemList {

    private final Locator names;
    private final Locator prices;
    private final Locator quantities;

    public CartItemList(Page page) {
        names = page.getByTestId("inventory-item-name");
        prices = page.getByTestId("inventory-item-price");
        quantities = page.getByTestId("item-quantity");
    }

    public Locator names() {
        return names;
    }

    public Locator prices() {
        return prices;
    }

    public Locator quantities() {
        return quantities;
    }
}
