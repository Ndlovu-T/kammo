import AsyncStorage from "@react-native-async-storage/async-storage";
import { formatRand, timeAgo } from "./utils";

const WALLET_KEY = "kammo_wallet_balance";

export async function loadWalletBalance() {
  const raw = await AsyncStorage.getItem(WALLET_KEY);
  if (raw == null) return 0;
  const n = Number(raw);
  return Number.isFinite(n) ? n : 0;
}

export async function saveWalletBalance(amount) {
  await AsyncStorage.setItem(WALLET_KEY, String(Math.max(0, amount)));
}

const LEDGER_LABELS = {
  HOLD: { title: "Funds locked", type: "lock" },
  LOCK: { title: "Funds locked", type: "lock" },
  RELEASE: { title: "Funds released", type: "credit" },
  UNLOCK: { title: "Funds released", type: "credit" },
  PAYOUT: { title: "Payout sent", type: "debit" },
  REFUND: { title: "Refunded", type: "debit" },
  REVERSAL: { title: "Refunded", type: "debit" },
};

export function buildTransactionsFromLedger(entries = []) {
  return entries
    .map((entry) => {
      const label = LEDGER_LABELS[entry.type] || { title: entry.type, type: "debit" };
      return {
        id: entry.id,
        type: label.type,
        title: label.title,
        sub: entry.reason,
        amount: Math.abs(Number(entry.amount) || 0),
        when: timeAgo(entry.createdAt),
        sortAt: new Date(entry.createdAt).getTime(),
      };
    })
    .sort((a, b) => b.sortAt - a.sortAt)
    .slice(0, 12);
}

export function computeLocked(deals = [], dashboard) {
  if (dashboard?.activeDealValue != null) return Number(dashboard.activeDealValue) || 0;
  const lockedStatuses = [
    "PAYMENT_SECURED",
    "AWAITING_COLLECTION",
    "IN_TRANSIT",
    "DELIVERED",
    "AWAITING_BUYER_PAYMENT",
    "BUYER_ACCEPTED",
    "SELLER_ACCEPTED",
  ];
  return deals
    .filter((d) => lockedStatuses.includes(d.status))
    .reduce((sum, d) => sum + Number(d.price || 0), 0);
}

export function buildTransactions(deals = []) {
  const rows = [];

  deals.forEach((deal) => {
    const ref = deal.dealReference || `KM-${deal.dealCode}`;
    const when = timeAgo(deal.updatedAt || deal.createdAt);
    const price = Number(deal.price || 0);

    if (deal.status === "COMPLETED") {
      rows.push({
        id: `${deal.dealCode}-release`,
        type: "credit",
        title: "Funds released",
        sub: `${ref} · ${deal.itemName}`,
        amount: price,
        when,
        sortAt: new Date(deal.updatedAt || deal.createdAt).getTime(),
      });
    }

    if (["PAYMENT_SECURED", "IN_TRANSIT", "AWAITING_COLLECTION", "DELIVERED"].includes(deal.status)) {
      rows.push({
        id: `${deal.dealCode}-lock`,
        type: "lock",
        title: "Funds locked",
        sub: `${ref} · ${deal.itemName}`,
        amount: price,
        when,
        sortAt: new Date(deal.updatedAt || deal.createdAt).getTime(),
      });
    }
  });

  return rows.sort((a, b) => b.sortAt - a.sortAt).slice(0, 12);
}

export function buildWalletSnapshot({ wallet, profile, dashboard, deals }) {
  const locked = computeLocked(deals, dashboard);
  const lifetime = Number(profile?.volume ?? 0);
  const activeCount = dashboard?.activeDealCount ?? deals.filter((d) => d.status !== "COMPLETED").length;
  const ledgerEntries = wallet?.recentEntries ?? [];

  return {
    available: Number(wallet?.balance ?? 0),
    locked,
    lifetime,
    activeCount,
    dealCount: profile?.dealCount ?? deals.length,
    transactions: ledgerEntries.length > 0 ? buildTransactionsFromLedger(ledgerEntries) : buildTransactions(deals),
  };
}

export function formatWalletAmount(amount) {
  const n = Number(amount) || 0;
  const whole = Math.floor(n);
  const cents = Math.round((n - whole) * 100);
  return { whole, cents: String(cents).padStart(2, "0"), display: formatRand(n) };
}
