package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ProductDetailsPage {

    private final Locator name;
    private final Locator price;
    private final Locator addToCartButton;

    public ProductDetailsPage(Page page) {
        name = page.getByTestId("inventory-item-name");
        price = page.getByTestId("inventory-item-price");
        addToCartButton = page.getByTestId("add-to-cart");
    }

    public Locator name() {
        return name;
    }

    public Locator price() {
        return price;
    }

    public Locator addToCartButton() {
        return addToCartButton;
    }
}
