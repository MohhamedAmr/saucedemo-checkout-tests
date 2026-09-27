# Manual Testing – SauceDemo Checkout

**Scope:** checkout journey on https://www.saucedemo.com as `standard_user` – product list → cart → checkout information → overview → complete (including the order PDF and session handling around the flow).

**Test environment**

| | |
|---|---|
| Device | Desktop PC |
| OS | Windows 10 (64-bit) |
| Browser | Google Chrome 154.0.8037.57, viewport 1920×953 |
| Network | Home broadband, no throttling |
| User | `standard_user` |
| Date | 27 Sep 2026 |

**Contents**
1. Testing ideas and prioritization
2. Confirmed bugs (BUG-01 – BUG-07)
3. Observations / UX issues
4. Not a defect / expected behaviour

---

## 1. Testing ideas and prioritization

### How I prioritized

I ranked each idea by **business impact if it fails** and **how many customers would hit it**:

- **P1 – test first.** Anything that stops a customer from buying, charges or shows the wrong amount, or loses what they put in the cart. These are direct revenue and trust risks, and every order goes through them.
- **P2 – test next.** Alternative paths and data quality: the customer can still buy, but the order data may be wrong, or a common side path (cancel, back, refresh, remove, session timeout) behaves badly.
- **P3 – test when time allows.** Wider coverage (browsers, devices, accessibility, performance) and low-traffic features. Important for a release, but less likely to block the core purchase on this app.

If time is very limited, I would run the P1 happy path with 2+ products first, then the price calculation checks, then required-field validation – that combination covers most of the money-related risk.

### P1 – Core purchase and money

| # | Area | Testing idea | Result |
|---|---|---|---|
| 1 | Login | `standard_user` logs in and lands on Products; checkout pages redirect to login with an error when not logged in | ✅ Pass (message display: BUG-07) |
| 2 | Add to cart | Add from the product list and from the product details page; button switches to **Remove** | ✅ Pass |
| 3 | Cart badge | Badge count matches the number of items after adding/removing from list, details and cart pages; badge disappears at 0 | ✅ Pass |
| 4 | Cart contents | Cart shows exactly the added products – correct names, prices, quantity 1, in the order added | ✅ Pass |
| 5 | Price consistency | Same price for a product on list, details, cart, overview and order PDF | ✅ Pass |
| 6 | Required fields | Continue is blocked with the right message and the right field highlighted when First Name, Last Name or Zip/Postal Code is empty | ⚠️ Blocked correctly, but every field is highlighted – **BUG-06** |
| 7 | Overview calculation | Item total = sum of item prices, Total = Item total + Tax, amounts rounded and formatted to 2 decimals – with 1, 2 and 4 products | ❌ **BUG-01** |
| 8 | Complete purchase | Finish shows the confirmation page, empties the cart and clears the badge | ✅ Pass |
| 9 | Empty cart | An order can't be placed with an empty cart – via cart, browser Back, or removing items from the Overview | ❌ **BUG-02** |
| 10 | End-to-end | Full happy path with one product, two products (Fleece Jacket + Onesie) and several products | ✅ Pass (amounts see BUG-01) |

### P2 – Alternative paths and data quality

| # | Area | Testing idea | Result |
|---|---|---|---|
| 11 | Input validation | Whitespace-only values, leading/trailing spaces | ❌ **BUG-03** |
| 12 | Input validation | Non-Latin names (Arabic), apostrophes/hyphens (`O'Brien-Smith`), very long values (300 chars), special characters and script tags | ✅ Accepted, no script executed |
| 13 | Postal code | Letters, decimals, negative numbers, symbols, ZIP+4 | ⚠️ Any value accepted – OBS-02 |
| 14 | Step order | Open `/checkout-step-two.html` directly without filling the information step | ❌ **BUG-04** |
| 15 | Navigation | Cancel on Information → cart with items; Cancel on Overview → products with cart kept; Continue Shopping keeps cart | ✅ Pass |
| 16 | Navigation | Browser Back/refresh on each checkout step; Back after Finish; Back after logout | ⚠️ Refresh keeps the order; Back after logout is blocked correctly; Back after Finish → empty order (BUG-02) |
| 17 | Navigation | Open a product from the Overview page and return to checkout | ⚠️ No way back except browser Back – OBS-03 |
| 18 | Remove items | Remove from cart page, product list and product details (also while on Overview); cart, badge and totals update | ✅ Totals recalculate correctly; removing the last item → BUG-02 |
| 19 | Session/state | Cart after page refresh, logout/login | ✅ Cart kept (OBS-08) |
| 20 | Session expiry | Session expires during checkout (10 minutes after login) | ⚠️ Redirect works, cart kept, form data lost; message clipped (BUG-07), see OBS-06 |
| 21 | Reset App State | Menu → Reset App State clears cart, badge and product buttons | ❌ **BUG-05** |
| 22 | Error handling | Error banner can be closed; field highlighting clears | ✅ Pass |
| 23 | Order PDF | **Generate PDF order** contains the correct items, prices, totals and shipping details | ✅ Correct for a normal order; empty receipt after BUG-02; no shipping section after BUG-04 |

### P3 – Wider coverage

| # | Area | Testing idea | Result |
|---|---|---|---|
| 24 | Cross-browser | Checkout on Firefox, Safari (WebKit) and Edge | ⏸ Not executed |
| 25 | Responsive | Cart and checkout on mobile viewports (e.g. 375×812) | ⏸ Not executed |
| 26 | Accessibility | Labels on inputs, focus order, screen reader announcements for errors and cart changes | ⚠️ Partially reviewed – OBS-07 |
| 27 | Keyboard | Enter submits the information form; tab order First → Last → Zip → Continue | ⏸ Not executed |
| 28 | Performance | Page transitions and Finish under a slow network profile | ⏸ Not executed |
| 29 | UI consistency | Alignment and fonts of list headers, product content vs images | ⚠️ OBS-04, OBS-05 |

---

## 2. Confirmed bugs

All bugs below were reproduced at least twice on the environment listed above. Screenshots are in [`evidence/`](evidence).

| ID | Title | Severity | Priority |
|---|---|---|---|
| BUG-01 | Item total shown with floating-point noise | Major | High |
| BUG-02 | Order can be completed with an empty cart | Major | High |
| BUG-03 | Whitespace-only customer information accepted | Major | Medium |
| BUG-04 | Overview opens by URL, skipping the information step | Major | Medium |
| BUG-05 | Reset App State leaves stale "Remove" buttons | Minor | Low |
| BUG-06 | All checkout fields marked as errors when only one is missing | Minor | Medium |
| BUG-07 | Login error message is cut off when it wraps to three lines | Minor | Medium |

### BUG-01 – Item total on Checkout Overview is shown with floating-point noise (e.g. `$57.980000000000004`)

| | |
|---|---|
| **Severity** | Major |
| **Priority** | High |
| **Reproducibility** | Always (5/5) for the affected product combinations |
| **Environment** | Windows 10, Chrome 154.0.8037.57, desktop, broadband |

**Steps to reproduce**
1. Log in as `standard_user` / `secret_sauce`.
2. Add **Sauce Labs Fleece Jacket** ($49.99) to the cart.
3. Add **Sauce Labs Onesie** ($7.99) to the cart.
4. Open the cart and click **Checkout**.
5. Enter First Name `John`, Last Name `Doe`, Zip `12345` and click **Continue**.
6. Check the **Price Total** section.

**Expected result:** `Item total: $57.98`

**Actual result:** `Item total: $57.980000000000004`. Tax (`$4.64`) and Total (`$62.62`) are correct and formatted.

**Notes**
- Also happens with **Bike Light + Fleece Jacket** → `Item total: $59.980000000000004`.
- With an empty cart the same label shows `Item total: $0` instead of `$0.00`.
- The order PDF for the same order shows `Item total $57.98` correctly, so only the Overview page is missing the 2-decimal formatting.

**Attachment:** [`BUG-01-item-total-precision.jpg`](evidence/BUG-01-item-total-precision.jpg)

**Why Major / High:** the calculation itself is correct, but the customer sees a malformed money amount on the last screen before paying. That damages trust in the price, it happens on common product combinations, and the fix is small.

---

### BUG-02 – Order can be completed with an empty cart

| | |
|---|---|
| **Severity** | Major |
| **Priority** | High |
| **Reproducibility** | Always (4/4, three different paths) |
| **Environment** | Windows 10, Chrome 154.0.8037.57, desktop, broadband |

**Steps to reproduce (path A – empty cart)**
1. Log in as `standard_user`.
2. Make sure the cart is empty and open the cart page.
3. Click **Checkout** (the button is enabled).
4. Enter First Name `John`, Last Name `Doe`, Zip `12345` and click **Continue**.
5. Overview shows no items, `Item total: $0`, `Total: $0.00`. Click **Finish**.

**Path B – removing the item from the Overview page**
1. Add **Sauce Labs Fleece Jacket** only and go through checkout to the **Overview** page.
2. Click the product name – its details page opens.
3. Click **Remove** (badge disappears).
4. Press browser **Back** – the Overview now shows no items and $0.00, **Finish** is still enabled.
5. Click **Finish**.

**Path C – browser Back after a completed order**
1. Complete a normal order.
2. On the Complete page press browser **Back** – an empty Overview is shown with Finish enabled.
3. Click **Finish**.

**Expected result:** Checkout/Finish is blocked while the cart is empty (disabled button or a message). An order with no items can't be placed.

**Actual result:** "Thank you for your order!" is shown for an order with no products. **Generate PDF order** then produces a receipt with an empty item list and `Item total $0.00 / Tax $0.00 / Total $0.00`.

**Note:** when the Overview still has items left (e.g. removing one of two products in path B), the list and totals are recalculated correctly – the problem is only that nothing stops an order once the cart is empty.

**Attachments:** [`BUG-02-empty-cart-checkout-enabled.jpg`](evidence/BUG-02-empty-cart-checkout-enabled.jpg), [`BUG-02-empty-order-overview.jpg`](evidence/BUG-02-empty-order-overview.jpg), [`BUG-02-empty-overview-after-remove-and-back.jpg`](evidence/BUG-02-empty-overview-after-remove-and-back.jpg), [`BUG-02-empty-order-complete.jpg`](evidence/BUG-02-empty-order-complete.jpg)

**Why Major / High:** in a real store this creates empty/zero-value orders and receipts that reach fulfilment, reporting and payment. It's a broken business rule on the main flow, reachable in three ordinary ways.

---

### BUG-03 – Checkout information accepts whitespace-only First Name, Last Name and Zip/Postal Code

| | |
|---|---|
| **Severity** | Major |
| **Priority** | Medium |
| **Reproducibility** | Always (2/2) |
| **Environment** | Windows 10, Chrome 154.0.8037.57, desktop, broadband |

**Steps to reproduce**
1. Log in as `standard_user` and add any product to the cart.
2. Open the cart and click **Checkout**.
3. Type 3 spaces in First Name, Last Name and Zip/Postal Code.
4. Click **Continue**.

**Expected result:** Same validation as an empty field ("Error: First Name is required", etc.). Values should be trimmed before validation.

**Actual result:** Validation passes and the Overview page opens; the order can be finished without any real customer name or postal code.

**Attachments:** [`BUG-03-whitespace-only-fields.jpg`](evidence/BUG-03-whitespace-only-fields.jpg), [`BUG-03-whitespace-accepted-overview.jpg`](evidence/BUG-03-whitespace-accepted-overview.jpg)

**Why Major / Medium:** it produces orders that can't be shipped or matched to a customer, but a normal customer is unlikely to do this by accident, so it can follow BUG-01/02.

---

### BUG-04 – Checkout Overview can be opened by URL, skipping the information step

| | |
|---|---|
| **Severity** | Major |
| **Priority** | Medium |
| **Reproducibility** | Always (3/3) |
| **Environment** | Windows 10, Chrome 154.0.8037.57, desktop, broadband |

**Steps to reproduce**
1. Log in as `standard_user` and add **Sauce Labs Onesie** to the cart.
2. Without clicking Checkout, enter `https://www.saucedemo.com/checkout-step-two.html` in the address bar.
3. Click **Finish**, then **Generate PDF order**.

**Expected result:** User is redirected to the information step (or cart) because no customer information was provided.

**Actual result:** Overview is shown with the cart items and totals, and Finish completes the order without any customer information. The generated receipt has **no "Ship to" section at all**.

**Attachment:** not needed – reproducible from the URL alone.

**Why Major / Medium:** same impact as BUG-03 (orders without customer data), but it needs deliberate URL manipulation, so fewer real users would hit it.

---

### BUG-05 – "Reset App State" empties the cart but products still show "Remove"

| | |
|---|---|
| **Severity** | Minor |
| **Priority** | Low |
| **Reproducibility** | Always (2/2) |
| **Environment** | Windows 10, Chrome 154.0.8037.57, desktop, broadband |

**Steps to reproduce**
1. Log in as `standard_user`.
2. Add **Sauce Labs Fleece Jacket** and **Sauce Labs Onesie** to the cart (badge shows 2).
3. Open the menu (☰) and click **Reset App State**.
4. Close the menu and look at the product list.

**Expected result:** Cart is empty, badge is hidden and both products show **Add to cart**.

**Actual result:** Cart is empty and badge is hidden, but both products still show **Remove** until the page is refreshed. Clicking the stale **Remove** only switches the button back to **Add to cart**.

**Attachment:** [`BUG-05-reset-state-stale-remove-buttons.jpg`](evidence/BUG-05-reset-state-stale-remove-buttons.jpg)

**Why Minor / Low:** the cart data is correct; only the button labels are out of sync, and a refresh fixes it.

---

### BUG-06 – All checkout fields are marked as errors when only one field is missing

| | |
|---|---|
| **Severity** | Minor |
| **Priority** | Medium |
| **Reproducibility** | Always (3/3) |
| **Environment** | Windows 10, Chrome 154.0.8037.57, desktop, broadband |

**Steps to reproduce**
1. Log in as `standard_user`, add any product, open the cart and click **Checkout**.
2. Enter First Name `John`, leave Last Name empty, enter Zip `12345`.
3. Click **Continue**.

**Expected result:** Only **Last Name** is highlighted as invalid; the filled fields keep their normal style.

**Actual result:** The message correctly says "Error: Last Name is required", but **all three fields** get the red underline and the red ✖ icon, including `John` and `12345`. Same when only First Name or only Zip is missing.

**Attachment:** [`BUG-06-valid-fields-marked-as-errors.jpg`](evidence/BUG-06-valid-fields-marked-as-errors.jpg)

**Why Minor / Medium:** the order can still be completed and the text message names the right field, so impact is limited (Minor). But it's on the checkout form every customer sees when they make a mistake, it points them at fields that are already correct, and it's a cheap fix – so it's worth scheduling soon (Medium).

---

### BUG-07 – Login error message is cut off when it wraps to three lines (e.g. after session expiry)

| | |
|---|---|
| **Severity** | Minor |
| **Priority** | Medium |
| **Reproducibility** | Always (3/3) |
| **Environment** | Windows 10, Chrome 154.0.8037.57, desktop, broadband |

**Steps to reproduce**
1. Log in as `standard_user`, add a product and go to **Checkout: Your Information**; fill the three fields.
2. Wait until the session expires (the session cookie expires 10 minutes after login).
3. Click **Continue**.

Quicker way to see the same message: while logged out, open `https://www.saucedemo.com/inventory.html` directly.

**Expected result:** The user is redirected to Login with a fully readable message explaining they need to log in again.

**Actual result:** Redirect works, but the message `Epic sadface: You can only access '/checkout-step-two.html' when you are logged in.` wraps to three lines inside a banner with a fixed height (45 px for 60 px of text). The first and last lines are cut off, so it reads like a corrupted message. Two-line messages (e.g. for `/cart.html`) display fine.

**Attachments:** [`BUG-07-login-error-after-session-expiry.jpg`](evidence/BUG-07-login-error-after-session-expiry.jpg), [`BUG-07-login-error-clipped-zoom.png`](evidence/BUG-07-login-error-clipped-zoom.png)

**Why Minor / Medium:** the redirect itself works and the user can log in again (Minor). But this is the only explanation a customer gets when the session times out mid-checkout, it happens on the most-used pages (products and checkout), and the fix is a CSS change (Medium).

---

## 3. Observations / UX issues

Valid points worth raising with the product owner, but not functional defects: either there's no requirement to compare against, or the flow still works correctly.

| ID | Observation | Why it's an observation, not a bug |
|---|---|---|
| OBS-01 | **Validation shows one error at a time.** With all three fields empty only "First Name is required" is shown; the user finds the next missing field on the next click. | Sequential validation is a common, working pattern and each message is correct. Showing all missing fields at once would be a UX improvement. (The wrong *highlighting* is a separate real bug – BUG-06.) |
| OBS-02 | **Zip/Postal Code has no format validation.** `SAAdda`, `12.5`, `-123`, `0`, symbols and 300-character values are accepted. | No requirement defines the allowed format. Letters alone can't be rejected – UK (`SW1A 1AA`) and Canadian (`K1A 0B1`) postcodes contain letters – and there's no country field to validate against. Needs a product decision on supported countries; then it becomes testable. |
| OBS-03 | **No way back to checkout from a product opened on the Overview page.** Product names on the Overview are links; the details page only offers **Back to products**. Returning to the cart means entering the customer information again; only browser Back returns to the Overview. | Nothing breaks and the order data stays correct – it's a navigation/UX gap. It does make BUG-02 path B easy to reach. |
| OBS-04 | **Cart/Overview list header alignment.** `QTY` is left-aligned and sits about 20 px left of the centred quantity box below it; `QTY` uses the monospace font while `Description` uses the regular font. | Cosmetic, no functional impact, and there is no design spec to compare against. Worth a UI polish ticket. Evidence: [`OBS-cart-qty-header-alignment.png`](evidence/OBS-cart-qty-header-alignment.png) |
| OBS-05 | **Product image doesn't match the name.** "Test.allTheThings() T-Shirt (Red)" shows an orange long-sleeve sweatshirt. | Catalog content, not application logic. The name itself is intentional demo data (see section 4), but a name/image mismatch in a real store would cause returns. Evidence: [`OBS-product-name-vs-image.jpg`](evidence/OBS-product-name-vs-image.jpg) |
| OBS-06 | **Session ends 10 minutes after login, even while the user is active**, and the message doesn't say the session expired – it shows an internal page path. The cart is kept, but the checkout information typed in is lost. | The redirect is correct security behaviour; timeout length and wording are product decisions. The clipped text is logged separately as BUG-07. |
| OBS-07 | **Accessibility:** inputs use placeholders instead of labels (fields look empty once filled with spaces – see BUG-03), and adding/removing items isn't announced to screen readers (no live region). | Needs an accessibility review against WCAG; not a functional failure. |
| OBS-08 | **Cart survives logout** (kept in browser local storage). | Fine for a single user; on a shared device the next user sees the previous user's cart. Product decision. |
| OBS-09 | **Quantity can't be changed** in the cart; each product can be added only once. | Design limitation of the demo, not broken behaviour. |
| OBS-10 | **Tax rate isn't shown.** Tax is 8% of the item total in every case checked, but the page doesn't say so. | Calculation is correct; transparency suggestion. |

---

## 4. Not a defect / expected behaviour

| Reported behaviour | Verified behaviour | Why it's not a defect |
|---|---|---|
| No confirmation dialog when removing an item from the cart | Item disappears immediately, badge updates, the item can be added again from the product list | Removing a low-cost, easily reversible item without a confirmation is the normal e-commerce pattern; a dialog would add friction. No requirement asks for one. |
| No success message after Add / Remove | Button text switches Add to cart ⇄ Remove, the badge count changes, the cart row disappears | The UI does give immediate visual feedback. A toast is a design choice, not a missing function. (Screen-reader feedback is covered in OBS-07.) |
| Backpack description starts with `carry.allTheThings()` | Same text on list, details and cart | Intentional developer-humour copy – the whole catalog uses it ("A red light isn't the desired state in testing…", "junior automation engineer…"). It's Sauce Labs' demo content, rendered correctly. |
| Product named `Test.allTheThings() T-Shirt (Red)` | Name is consistent everywhere, including the order PDF | Intentional demo test data, same style as above. (The image mismatch is noted as OBS-05.) |
| Overview allows finishing after the product is removed via its details page and browser Back | Reproduced | Not a separate bug – same root cause as **BUG-02** (no empty-cart check before Finish). Added to BUG-02 as path B. |
| Empty order can still generate a PDF | Reproduced – receipt with no items and $0.00 | Consequence of **BUG-02**; once an empty order can't be completed, an empty receipt can't be generated. Added to BUG-02 as impact. |
