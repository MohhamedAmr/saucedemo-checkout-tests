package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class InventoryPage {

    private final Page page;
    private final Locator items;

    public InventoryPage(Page page) {
        this.page = page;
        items = page.getByTestId("inventory-item");
    }

    public void addToCart(String productName) {
        item(productName)
                .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add to cart"))
                .click();
    }

    public void openProductDetails(String productName) {
        item(productName).getByTestId("inventory-item-name").click();
    }

    private Locator item(String productName) {
        return items.filter(new Locator.FilterOptions()
                .setHas(page.getByText(productName, new Page.GetByTextOptions().setExact(true))));
    }
}
