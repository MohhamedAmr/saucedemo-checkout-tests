# SauceDemo Checkout – QA Assessment

Manual testing and UI automation for the checkout journey on [SauceDemo](https://www.saucedemo.com) using `standard_user`.

- **Manual testing** – testing ideas, prioritization, bug reports, observations and not-a-defect notes: [`manual-testing/MANUAL_TESTING.md`](manual-testing/MANUAL_TESTING.md)
- **Automation** – Java end-to-end test for the checkout journey (this README)

## What the automated test covers

`CheckoutTest.standardUserCanCompleteCheckout`:

1. Logs in as `standard_user` and checks the Products page is shown
2. Adds **Sauce Labs Fleece Jacket** from the product list
3. Opens **Sauce Labs Onesie** details, checks name and price, adds it to the cart
4. Checks the cart badge shows **2**
5. Opens the cart and checks product names, prices and quantities
6. Goes to checkout and fills First Name, Last Name and Zip/Postal Code with data from the JSONPlaceholder API
7. On the Overview page checks the products, prices, item total and that Total = Item total + Tax
8. Clicks Finish and checks the Complete page (URL, title, confirmation message, cart badge cleared)

## Tech stack

- Java 17, Maven
- [Playwright for Java](https://playwright.dev/java/) – browser automation and assertions
- TestNG – test runner
- RestAssured – JSONPlaceholder API call
- Page Object Model for the UI

## Prerequisites

- JDK 17 or newer
- Maven 3.8 or newer (or run from IntelliJ IDEA, which includes Maven)
- Internet access to `www.saucedemo.com` and `jsonplaceholder.typicode.com`

## Installation

```bash
git clone <repository-url>
cd saucedemo-checkout-tests
mvn clean test-compile
```

Maven downloads the dependencies. On the first run Playwright for Java also downloads its browsers automatically (one-time, a few hundred MB).

## Running the tests

```bash
mvn test                      # uses testng.xml (headless by default)
mvn test -Dheadless=false     # with a visible browser, without editing testng.xml
```

From IntelliJ: open the folder (IntelliJ detects `pom.xml`), set the Project SDK to JDK 17 or newer, click **Reload All Maven Projects**, then right-click `testng.xml` → **Run**.

Results: console output and the TestNG reports in `target/surefire-reports/` (`index.html`, `emailable-report.html`). For a failed test a Playwright trace (every step with screenshots and DOM snapshots) is saved in `target/traces/<test-name>.zip` – drag it into https://trace.playwright.dev to view it.

## Known failure – BUG-01

The run currently finishes with **1 failed test** because of a real application bug, not a test issue:

```
Item total on Overview expected [Item total: $57.98] but found [Item total: $57.980000000000004]
```

SauceDemo shows the item total without rounding for this product combination (see BUG-01 in the manual testing report). The item total check is a soft assertion, so the test still completes the order and verifies the Complete page before reporting the failure. When the bug is fixed the test passes without any change.

## Test data from JSONPlaceholder

Checkout details are not hardcoded. `api/UsersApi.java`:

1. Calls `GET https://jsonplaceholder.typicode.com/users` with RestAssured and expects status 200. Network errors (connection reset, timeout – 10 s) are retried up to 3 times, 2 s apart; a wrong status code fails immediately
2. Fails with a clear message if the list is empty
3. Picks a random user from the list
4. Maps the user to checkout data:
   - `name` → First Name / Last Name. A title like `Mrs.` is removed, the first word becomes the first name and the rest the last name (`Mrs. Dennis Schulist` → `Dennis` / `Schulist`)
   - `address.zipcode` → Zip/Postal Code, used as returned (e.g. `92998-3874`)

The selected customer is printed in the console and the TestNG report, so a failing run can be repeated with the same data.

## Configuration

| Setting | Where | Default | |
|---|---|---|---|
| `headless` | `<parameter name="headless">` in `testng.xml`, or `-Dheadless=...` (overrides the XML) | `true` | `false` shows the browser |
| `baseUrl` | `-DbaseUrl=...`, read in `config/TestConfig.java` | `https://www.saucedemo.com` | e.g. `mvn test -DbaseUrl=https://staging.example.com` |

Other settings:
- Locators use SauceDemo's `data-test` attributes (`setTestIdAttribute("data-test")` in `BaseTest`), so pages use `getByTestId(...)`.
- `testng.xml` runs every class in `com.saucedemo.tests`, in parallel by class.
- The `standard_user` / `secret_sauce` credentials are the public demo credentials shown on the SauceDemo login page.

## Project structure

```
├── manual-testing/                   # manual testing deliverable (Markdown + screenshots, not code)
│   ├── MANUAL_TESTING.md
│   └── evidence/
├── src/main/java/com/saucedemo/       # framework code – reusable, no test logic
│   ├── pages/                         # page objects – locators and page actions only
│   │   ├── Header.java                # page title, cart link and badge (shared)
│   │   ├── CartItemList.java          # item list used by both Cart and Overview
│   │   └── LoginPage, InventoryPage, ProductDetailsPage, CartPage,
│   │       CheckoutInformationPage, CheckoutOverviewPage, CheckoutCompletePage
│   ├── api/UsersApi.java              # JSONPlaceholder call + mapping to checkout data
│   ├── data/                          # Product, Products, User, Customer
│   ├── config/TestConfig.java         # baseUrl
│   └── utils/Price.java               # price formatting / parsing
├── src/test/java/com/saucedemo/       # tests
│   ├── base/BaseTest.java             # browser setup/teardown, trace on failure
│   └── tests/CheckoutTest.java        # the end-to-end checkout test
├── testng.xml
└── pom.xml
```

Framework code (`src/main/java`) describes the application and the test data; test code (`src/test/java`) holds the test lifecycle and all assertions. To add a scenario, create a new class in `tests/` that extends `BaseTest` and reuse the page objects.
