package com.saucedemo.config;

public final class TestConfig {

    public static final String BASE_URL = System.getProperty("baseUrl", "https://www.saucedemo.com");

    private TestConfig() {
    }
}
