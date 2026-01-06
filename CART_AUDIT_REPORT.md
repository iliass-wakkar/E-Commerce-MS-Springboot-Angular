# Cart Functionality Audit

## 1. Executive Summary
The shopping cart implementation is functional but basic. It relies on `localStorage` for persistence, which is standard for this type of application. However, it lacks critical validation features (Stock Limits) and has some usability issues.

## 2. 🚨 Critical Issues (Must Fix)

### A. No Stock Validation in Cart
*   **Issue**: Users can increase the quantity of an item indefinitely using the `+` button.
*   **Impact**: A user can add 100 items when only 5 are in stock. They will only find out it's impossible when they try to "Checkout", resulting in a 400 Bad Request error from the backend.
*   **Fix**: Update `updateCartItemQuantity` in `OrdersComponent` to check `product.stockQuantity`.

### B. Price Staleness
*   **Issue**: The cart stores the *entire* product object, including the price at the time it was added.
*   **Impact**: If an admin changes the price of a product, the user's cart will still show the old price.
*   **Fix**: When loading the cart or before checkout, re-fetch the latest prices from the backend. (Optional for now, but good to know).

## 3. ⚠️ Usability Issues

### A. Read-Only Quantity Input
*   **Issue**: The quantity input field is `readonly`.
*   **Impact**: To buy 50 items, a user must click the `+` button 49 times.
*   **Fix**: Make the input editable and listen for `change` events.

### B. Missing Image Fallback
*   **Issue**: `<img [src]="item.product.imageUrl">` assumes the URL is always valid.
*   **Impact**: Broken image icon if the product has no image.
*   **Fix**: Add a fallback like `|| 'assets/placeholder.png'`.

## 4. Code Quality

### ✅ Reactive State
*   **Status**: **Good**.
*   **Details**: Uses `BehaviorSubject` correctly to update the UI whenever the cart changes.

### ✅ Persistence
*   **Status**: **Good**.
*   **Details**: Cart survives page reloads thanks to `localStorage`.

## 5. Recommended Action Plan

1.  **Fix Stock Limit**: Modify `OrdersComponent.ts` to prevent increasing quantity beyond stock.
2.  **Fix Image Fallback**: Update `orders.component.html`.
