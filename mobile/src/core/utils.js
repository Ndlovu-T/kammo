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
  return ui === "meetup" ? "MEETUP" : "COURIER";
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
};
