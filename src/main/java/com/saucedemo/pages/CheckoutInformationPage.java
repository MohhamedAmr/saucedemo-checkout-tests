package com.saucedemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.saucedemo.data.Customer;

public class CheckoutInformationPage {

    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator postalCodeInput;
    private final Locator continueButton;

    public CheckoutInformationPage(Page page) {
        firstNameInput = page.getByTestId("firstName");
        lastNameInput = page.getByTestId("lastName");
        postalCodeInput = page.getByTestId("postalCode");
        continueButton = page.getByTestId("continue");
    }

    public void fillCustomerInfo(Customer customer) {
        firstNameInput.fill(customer.firstName());
        lastNameInput.fill(customer.lastName());
        postalCodeInput.fill(customer.postalCode());
    }

    public Locator continueButton() {
        return continueButton;
    }
}
