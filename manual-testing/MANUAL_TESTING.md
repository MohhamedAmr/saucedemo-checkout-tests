# Manual Testing - SauceDemo Checkout

**Scope:** checkout flow with `standard_user` (products > cart > checkout information > overview > complete).

**Environment (all bugs below):** Windows 10 64-bit, Chrome 154.0.8037.57, desktop 1920x953, home broadband (no throttling).

## 1. Test ideas

Priority is based on business impact: anything that blocks the purchase or affects money comes first, then data quality and alternative paths, then wider coverage.

**P1 - Critical**
- Login with `standard_user`, and checkout pages can't be opened without login
- Add to cart from the product list and from the product details page
- Cart badge count after adding and removing items
- Cart shows the right products, prices and quantity
- Same price on product list, details, cart and overview
- Required fields on checkout information (First Name, Last Name, Zip/Postal Code)
- Overview calculation: item total, tax, total and number format
- Finish: complete page is shown and the cart is cleared
- Checkout with an empty cart
- Full purchase with one, two and several products

**P2 - High**
- Spaces only, special characters, Arabic names and long values in checkout fields
- Postal code formats
- Opening checkout pages directly by URL (skipping steps)
- Cancel, Continue Shopping, browser Back and refresh during checkout
- Removing items from the cart, product list and product details page
- Cart after refresh and after logout/login
- Session expiry during checkout
- Reset App State from the menu
- Error messages and field highlighting
- Content of the order PDF

**P3 - Medium**
- Other browsers (Firefox, Safari, Edge) and mobile screen size
- Keyboard navigation and accessibility
- Slow network
- UI alignment and product content

P1 is what every customer goes through, so a failure there means lost sales or wrong amounts. P2 covers mistakes and side paths that can create bad orders. P3 matters before a release but is less likely to block a purchase.

Not covered in this round: other browsers, mobile, keyboard and slow network.

## 2. Bugs

| ID | Title | Severity | Priority |
|---|---|---|---|
| BUG-01 | Item total shows a long decimal value | Major | High |
| BUG-02 | Order can be completed with an empty cart | Major | High |
| BUG-03 | Checkout accepts spaces only in all fields | Major | Medium |
| BUG-04 | Overview page can be opened directly, skipping checkout information | Major | Medium |
| BUG-05 | Reset App State keeps "Remove" buttons | Minor | Low |
| BUG-06 | All fields turn red when only one field is missing | Minor | Medium |
| BUG-07 | Login error message is cut off after session expiry | Minor | Medium |

Screenshots are in the [evidence](evidence) folder.

### BUG-01: Item total on Overview shows a long decimal value ($57.980000000000004)
**Severity:** Major | **Priority:** High

Steps:
1. Log in as `standard_user`.
2. Add Sauce Labs Fleece Jacket and Sauce Labs Onesie to the cart.
3. Open the cart, click Checkout, fill First Name, Last Name and Zip, click Continue.

Expected: `Item total: $57.98`

Actual: `Item total: $57.980000000000004`. Tax and Total are correct. Same with Bike Light + Fleece Jacket (`$59.980000000000004`). With an empty cart it shows `$0` instead of `$0.00`.

Attachment: [BUG-01-item-total-precision.jpg](evidence/BUG-01-item-total-precision.jpg)

### BUG-02: Order can be completed with an empty cart
**Severity:** Major | **Priority:** High

Steps:
1. Log in as `standard_user` and open the cart while it is empty.
2. Click Checkout, fill the form and click Continue.
3. Click Finish.

Expected: Checkout can't be completed with an empty cart.

Actual: The order is completed ("Thank you for your order!") with a $0.00 total, and the order PDF has no items.

Also happens when:
- On the Overview page you open the only product, click Remove, press browser Back and click Finish.
- After finishing an order you press browser Back and click Finish again.

Attachments: [empty cart](evidence/BUG-02-empty-cart-checkout-enabled.jpg), [overview](evidence/BUG-02-empty-order-overview.jpg), [overview after remove + Back](evidence/BUG-02-empty-overview-after-remove-and-back.jpg), [complete](evidence/BUG-02-empty-order-complete.jpg)

### BUG-03: Checkout information accepts spaces only
**Severity:** Major | **Priority:** Medium

Steps:
1. Log in, add any product, open the cart and click Checkout.
2. Enter only spaces in First Name, Last Name and Zip/Postal Code.
3. Click Continue.

Expected: Same error as an empty field (e.g. "First Name is required").

Actual: The Overview page opens and the order can be finished without a name or postal code.

Attachments: [form](evidence/BUG-03-whitespace-only-fields.jpg), [overview](evidence/BUG-03-whitespace-accepted-overview.jpg)

### BUG-04: Overview page can be opened by URL without checkout information
**Severity:** Major | **Priority:** Medium

Steps:
1. Log in and add Sauce Labs Onesie to the cart.
2. Open `https://www.saucedemo.com/checkout-step-two.html` directly.
3. Click Finish.

Expected: Redirect to the checkout information page.

Actual: The Overview page opens and the order is completed without customer information. The order PDF has no shipping details.

### BUG-05: Reset App State keeps "Remove" buttons on the product list
**Severity:** Minor | **Priority:** Low

Steps:
1. Log in and add Fleece Jacket and Onesie to the cart.
2. Open the menu and click Reset App State, then close the menu.

Expected: Cart is empty and both products show "Add to cart".

Actual: Cart and badge are cleared, but both products still show "Remove" until the page is refreshed.

Attachment: [BUG-05-reset-state-stale-remove-buttons.jpg](evidence/BUG-05-reset-state-stale-remove-buttons.jpg)

### BUG-06: All checkout fields turn red when only one field is missing
**Severity:** Minor | **Priority:** Medium

Steps:
1. Log in, add any product, open the cart and click Checkout.
2. Enter First Name `John`, leave Last Name empty, enter Zip `12345`.
3. Click Continue.

Expected: Only Last Name is marked as invalid.

Actual: The message says "Last Name is required" (correct), but all three fields are marked red with an error icon, including the filled ones.

Attachment: [BUG-06-valid-fields-marked-as-errors.jpg](evidence/BUG-06-valid-fields-marked-as-errors.jpg)

### BUG-07: Login error message is cut off after session expiry
**Severity:** Minor | **Priority:** Medium

Steps:
1. Log in, add a product and open Checkout: Your Information.
2. Wait until the session expires (about 10 minutes after login) and click Continue.

(Faster: while logged out, open `https://www.saucedemo.com/inventory.html` directly.)

Expected: Login page shows a readable message.

Actual: The message "You can only access '/checkout-step-two.html' when you are logged in." wraps to three lines and the first and last lines are cut off. Shorter messages (e.g. for `/cart.html`) display fine.

Attachments: [login page](evidence/BUG-07-login-error-after-session-expiry.jpg), [zoom](evidence/BUG-07-login-error-clipped-zoom.png)

## 3. Observations (not logged as bugs)

These need a product decision or are UX points; the flow still works.

- **OBS-01** Only one validation message is shown at a time (all fields empty > only "First Name is required").
- **OBS-02** Postal code accepts any value (letters, `-123`, `12.5`). Letters can be valid (UK, Canada), so the allowed format needs to be defined first.
- **OBS-03** A product opened from the Overview page only has "Back to products"; there is no way back to checkout except browser Back.
- **OBS-04** In the cart/overview list, the "QTY" header is not aligned with the quantity box and uses a different font than "Description". [screenshot](evidence/OBS-cart-qty-header-alignment.png)
- **OBS-05** "Test.allTheThings() T-Shirt (Red)" shows an orange sweatshirt image. [screenshot](evidence/OBS-product-name-vs-image.jpg)
- **OBS-06** The session ends 10 minutes after login even if the user is active, and the message doesn't say the session expired. Checkout information entered is lost.
- **OBS-07** Inputs use placeholders instead of labels, and adding/removing items is not announced to screen readers.
- **OBS-08** The cart is kept after logout (stored in the browser).
- **OBS-09** Quantity can't be changed in the cart.
- **OBS-10** Tax (8%) is calculated correctly but the rate isn't shown.
