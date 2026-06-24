export function normalizePhone(local) {
  const digits = String(local || "").replace(/\D/g, "");
  if (!digits) return "";
  if (digits.startsWith("27")) return digits;
  if (digits.startsWith("0")) return `27${digits.slice(1)}`;
  return `27${digits}`;
}

export function formatRand(amount) {
  const n = Number(amount);
  if (Number.isNaN(n)) return "R 0";
  return `R ${n.toLocaleString("en-ZA", { maximumFractionDigits: 0 })}`;
}

export function deliveryToApi(ui) {
  if (ui === "meetup") return "MEETUP";
  if (ui === "locker") return "PUDO_LOCKER";
  return "COURIER";
}

export const emptyAddress = {
  line1: "",
  line2: "",
  city: "",
  province: "",
  postalCode: "",
  contactName: "",
  contactPhone: "",
  contactEmail: "",
};

export function lockerToAddressPayload(locker) {
  if (!locker) return null;
  return {
    line1: locker.name,
    line2: locker.line1,
    city: locker.city,
    province: locker.province,
    postalCode: locker.postalCode,
    lockerTerminalId: locker.id,
  };
}

export function addressToPayload(address) {
  if (!address) return null;
  const trimmed = {
    line1: address.line1?.trim() || "",
    line2: address.line2?.trim() || "",
    city: address.city?.trim() || "",
    province: address.province?.trim() || "",
    postalCode: address.postalCode?.trim() || "",
    contactName: address.contactName?.trim() || "",
    contactPhone: normalizePhone(address.contactPhone) || "",
    contactEmail: address.contactEmail?.trim() || "",
  };
  const hasContent = Object.values(trimmed).some(Boolean);
  return hasContent ? trimmed : null;
}

export function statusLabel(status) {
  const map = {
    CREATED: "Created",
    AWAITING_BUYER_PAYMENT: "Awaiting Payment",
    BUYER_ACCEPTED: "Buyer Accepted",
    SELLER_ACCEPTED: "Seller Accepted",
    PAYMENT_SECURED: "Payment Secured",
    AWAITING_COLLECTION: "Awaiting Collection",
    IN_TRANSIT: "In Transit",
    DELIVERED: "Delivered",
    COMPLETED: "Completed",
    DISPUTED: "Disputed",
  };
  return map[status] || status;
}

export function statusPillType(status) {
  if (status === "PAYMENT_SECURED") return "green";
  if (status === "IN_TRANSIT" || status === "AWAITING_COLLECTION") return "amber";
  if (status === "DISPUTED") return "red";
  if (status === "COMPLETED") return "grey";
  return "amber";
}

export function timeAgo(iso) {
  if (!iso) return "";
  const diff = Date.now() - new Date(iso).getTime();
  const hrs = Math.floor(diff / 3600000);
  if (hrs < 1) return "Just now";
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days === 1) return "Yesterday";
  return `${days}d ago`;
}

export const defaultCreateDraft = {
  role: "buyer",
  itemName: "",
  price: "",
  description: "",
  otherPhone: "",
  delivery: "courier",
  inspectionHours: 24,
  collectionAddress: { ...emptyAddress },
  deliveryAddress: { ...emptyAddress },
  collectionLocker: null,
  deliveryLocker: null,
};
