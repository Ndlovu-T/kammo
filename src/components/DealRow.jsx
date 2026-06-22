import { Pressable, StyleSheet, Text, View } from "react-native";
import { ChevronRight } from "lucide-react-native";
import StatusPill from "./StatusPill";
import { formatRand, timeAgo } from "../utils";
import { colors } from "../theme";

function initials(name = "") {
  const parts = name.trim().split(/\s+/);
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
  return name.slice(0, 2).toUpperCase() || "??";
}

export function KmBadge({ id }) {
  const ref = id?.startsWith("KM-") ? id : `KM-${id || "----"}`;
  return (
    <View style={styles.km}>
      <Text style={styles.kmText}>{ref}</Text>
    </View>
  );
}

export default function DealRow({ deal, subtitle, onPress, last }) {
  const done = deal.status === "COMPLETED";
  return (
    <Pressable style={[styles.row, last && styles.rowLast]} onPress={onPress}>
      <View style={styles.ava}>
        <Text style={styles.avaText}>{initials(deal.itemName)}</Text>
      </View>
      <View style={styles.body}>
        <Text style={styles.name} numberOfLines={1}>
          {deal.itemName}
        </Text>
        <View style={styles.meta}>
          <KmBadge id={deal.dealReference || deal.dealCode} />
          {subtitle ? <Text style={styles.sub}>{subtitle}</Text> : null}
        </View>
        <StatusPill status={deal.status} small />
      </View>
      <View style={styles.right}>
        <Text style={[styles.price, done && styles.priceDim]}>{formatRand(deal.price)}</Text>
        <Text style={styles.time}>{timeAgo(deal.updatedAt || deal.createdAt)}</Text>
        <ChevronRight color={colors.dim} size={14} style={{ marginTop: 4 }} />
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    paddingVertical: 14,
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
  },
  rowLast: { borderBottomWidth: 0 },
  ava: {
    width: 44,
    height: 44,
    borderRadius: 13,
    backgroundColor: colors.green,
    alignItems: "center",
    justifyContent: "center",
  },
  avaText: { color: "#fff", fontSize: 14, fontWeight: "800" },
  body: { flex: 1, minWidth: 0, gap: 4 },
  name: { fontSize: 14, fontWeight: "600", color: colors.text },
  meta: { flexDirection: "row", alignItems: "center", gap: 6 },
  sub: { fontSize: 11, color: colors.mid },
  right: { alignItems: "flex-end" },
  price: { fontSize: 14, fontWeight: "700", color: colors.text },
  priceDim: { color: colors.mid },
  time: { fontSize: 11, color: colors.mid, marginTop: 2 },
  km: {
    backgroundColor: colors.greenDim,
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 3,
  },
  kmText: { fontSize: 10, fontWeight: "700", color: colors.green, letterSpacing: 0.3 },
});
