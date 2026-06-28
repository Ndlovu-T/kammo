import * as WebBrowser from "expo-web-browser";

/**
 * Opens a Paystack checkout URL in an in-app browser sheet and waits for the redirect back to
 * `callbackUrl` (a `kammo://...` deep link configured as the `callback_url` on the backend's
 * Paystack initialize call). Throws if the buyer cancels or dismisses the sheet before paying.
 */
export async function openCheckout(checkoutUrl, callbackUrl) {
  const result = await WebBrowser.openAuthSessionAsync(checkoutUrl, callbackUrl);
  if (result.type !== "success") {
    throw new Error("Checkout was cancelled");
  }
  return result.url;
}

/** Pulls the Paystack `reference` query param off the redirect URL the checkout sheet returns. */
export function referenceFromCallbackUrl(url) {
  const match = url?.match(/[?&]reference=([^&]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}
