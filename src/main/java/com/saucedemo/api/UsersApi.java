package com.saucedemo.api;

import com.saucedemo.data.Customer;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;

import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

public final class UsersApi {

    private static final String USERS_URL = "https://jsonplaceholder.typicode.com/users";
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000;
    private static final RestAssuredConfig TIMEOUTS = RestAssuredConfig.config().httpClient(
            HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", 10000)
                    .setParam("http.socket.timeout", 10000));

    private UsersApi() {
    }

    public static Customer getRandomCustomer() {
        JsonPath users = fetchUsers();

        int userCount = users.getList("$").size();
        if (userCount == 0) {
            throw new IllegalStateException("JSONPlaceholder returned no users");
        }

        int index = ThreadLocalRandom.current().nextInt(userCount);
        return toCustomer(
                users.getString("[" + index + "].name"),
                users.getString("[" + index + "].address.zipcode"));
    }

    // Retries only network errors (connection reset, timeout). A wrong status code fails immediately.
    private static JsonPath fetchUsers() {
        Exception lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return given().config(TIMEOUTS)
                        .when().get(USERS_URL)
                        .then().statusCode(200)
                        .extract().jsonPath();
            } catch (Exception e) {
                lastError = e;
                if (attempt < MAX_ATTEMPTS) {
                    waitBeforeRetry();
                }
            }
        }
        throw new IllegalStateException("Could not reach " + USERS_URL + " after " + MAX_ATTEMPTS
                + " attempts: " + lastError, lastError);
    }

    private static void waitBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // The API only has a full name (some with a title, e.g. "Mrs. Dennis Schulist"),
    // so the title is dropped, the first word is the first name and the rest is the last name.
    private static Customer toCustomer(String fullName, String zipCode) {
        String[] nameParts = fullName.replaceFirst("^(Mr|Mrs|Ms|Miss|Dr)\\.\\s+", "").split(" ", 2);
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        return new Customer(nameParts[0], lastName, zipCode);
    }
}
