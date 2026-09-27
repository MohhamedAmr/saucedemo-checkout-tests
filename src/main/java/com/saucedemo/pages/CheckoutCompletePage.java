package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class CheckoutCompletePage {

    private final Locator header;
    private final Locator message;

    public CheckoutCompletePage(Page page) {
        header = page.getByTestId("complete-header");
        message = page.getByTestId("complete-text");
    }

    public Locator header() {
        return header;
    }

    public Locator message() {
        return message;
    }
}
