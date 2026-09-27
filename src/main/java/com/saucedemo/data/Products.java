package com.saucedemo.data;

import java.math.BigDecimal;

public final class Products {

    public static final Product FLEECE_JACKET = new Product("Sauce Labs Fleece Jacket", new BigDecimal("49.99"));
    public static final Product ONESIE = new Product("Sauce Labs Onesie", new BigDecimal("7.99"));

    private Products() {
    }
}
