import { normalizePhone } from "./utils.js";

/** Who the logged-in user is on this deal (buyer or seller). */
export function userDealRole(deal, userPhone) {
  if (!deal || !userPhone) return "unknown";
  const phone = normalizePhone(userPhone);
  const other = normalizePhone(deal.otherPartyPhoneNumber || "");

  if (deal.ownerRole === "BUYER") {
    return other === phone ? "seller" : "buyer";
  }
  if (deal.ownerRole === "SELLER") {
    return other === phone ? "buyer" : "seller";
  }
  return "unknown";
}

/** Backend-driven actions available for the current user on this deal. */
export function getDealActions(deal, userPhone) {
  if (!deal) return [];
  const role = userDealRole(deal, userPhone);
  const s = deal.status;
  const actions = [];

  if (role === "seller" && s === "CREATED") {
    actions.push({ key: "acceptSeller", title: "Accept as Seller", api: "acceptAsSeller" });
  }
  if (role === "buyer" && s === "AWAITING_BUYER_PAYMENT" && deal.ownerRole === "SELLER") {
    actions.push({ key: "acceptBuyer", title: "Accept Deal as Buyer", api: "acceptAsBuyer" });
  }
  if (
    ["AWAITING_BUYER_PAYMENT", "BUYER_ACCEPTED", "SELLER_ACCEPTED", "CREATED"].includes(s)
  ) {
    actions.push({
      key: "payment",
      title: "Mark Payment Secured (test)",
      api: "markPaymentSecured",
      variant: "outline",
    });
  }
  if (role === "seller" && s === "PAYMENT_SECURED") {
    actions.push({
      key: "ready",
      title: "Mark Ready for Collection",
      api: "markReadyForCollection",
      variant: "outline",
    });
  }
  if (["AWAITING_COLLECTION", "PAYMENT_SECURED"].includes(s)) {
    actions.push({
      key: "transit",
      title: "Mark In Transit",
      api: "markInTransit",
      variant: "outline",
    });
  }
  if (role === "buyer" && ["IN_TRANSIT", "DELIVERED", "AWAITING_COLLECTION"].includes(s)) {
    actions.push({
      key: "confirm",
      title: "Confirm — I Received It",
      api: "confirmDelivery",
      navigate: "/confirmed",
    });
  }
  if (!["COMPLETED", "DISPUTED"].includes(s)) {
    actions.push({
      key: "dispute",
      title: "Raise Dispute",
      api: "raiseDispute",
      variant: "red",
    });
  }
  return actions;
}

function addressIsUsable(address) {
  if (!address) return false;
  return Boolean(
    address.line1?.trim() &&
      address.city?.trim() &&
      address.province?.trim() &&
      address.postalCode?.trim() &&
      address.contactName?.trim() &&
      address.contactPhone?.trim()
  );
}

export function validateCreateDraft(draft) {
  if (!draft.itemName?.trim()) return "Item name is required";
  if (!draft.price || Number(draft.price) < 1) return "Price must be at least R 1";
  if (!draft.description?.trim()) return "Description is required";
  if (!draft.otherPhone?.trim()) return "Other party phone is required";
  if (!normalizePhone(draft.otherPhone)) return "Enter a valid SA mobile number";

  if (draft.delivery === "courier") {
    if (!addressIsUsable(draft.collectionAddress)) return "Add a complete collection address";
    if (!addressIsUsable(draft.deliveryAddress)) return "Add a complete delivery address";
  }
  if (draft.delivery === "locker") {
    if (!draft.collectionLocker) return "Choose a drop-off locker";
    if (!draft.deliveryLocker) return "Choose a pickup locker";
  }
  return "";
}

export function dealProgressPct(status) {
  const order = [
    "CREATED",
    "AWAITING_BUYER_PAYMENT",
    "BUYER_ACCEPTED",
    "SELLER_ACCEPTED",
    "PAYMENT_SECURED",
    "AWAITING_COLLECTION",
    "IN_TRANSIT",
    "DELIVERED",
    "COMPLETED",
  ];
  const idx = order.indexOf(status);
  if (idx < 0) return status === "DISPUTED" ? 0 : 10;
  return Math.round(((idx + 1) / order.length) * 100);
}
