package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

// Page title and cart icon, shown on every page after login.
public class Header {

    private final Locator pageTitle;
    private final Locator cartLink;
    private final Locator cartBadge;

    public Header(Page page) {
        pageTitle = page.getByTestId("title");
        cartLink = page.getByTestId("shopping-cart-link");
        cartBadge = page.getByTestId("shopping-cart-badge");
    }

    public Locator pageTitle() {
        return pageTitle;
    }

    public Locator cartLink() {
        return cartLink;
    }

    public Locator cartBadge() {
        return cartBadge;
    }
}
