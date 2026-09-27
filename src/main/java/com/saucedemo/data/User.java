package com.saucedemo.data;

public record User(String username, String password) {

    // Public demo credentials listed on the SauceDemo login page.
    public static final User STANDARD_USER = new User("standard_user", "secret_sauce");
}
