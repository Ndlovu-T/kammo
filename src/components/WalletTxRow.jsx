import { Pressable, StyleSheet, Text, View } from "react-native";
import { ArrowDownLeft, ArrowUpRight, Lock, Plus } from "lucide-react-native";
import { formatWalletAmount } from "../wallet";
import { colors } from "../theme";

const ICONS = {
  credit: { Icon: ArrowDownLeft, bg: colors.greenDim, color: colors.green },
  lock: { Icon: Lock, bg: colors.amberDim, color: colors.amber },
  debit: { Icon: ArrowUpRight, bg: colors.bg2, color: colors.mid },
  topup: { Icon: Plus, bg: colors.greenDim, color: colors.green },
};

export default function WalletTxRow({ tx, last }) {
  const cfg = ICONS[tx.type] || ICONS.debit;
  const { Icon } = cfg;
  const isCredit = tx.type === "credit" || tx.type === "topup";
  const amt = formatWalletAmount(tx.amount).display;

  return (
    <View style={[styles.row, last && styles.last]}>
      <View style={[styles.icon, { backgroundColor: cfg.bg }]}>
        <Icon color={cfg.color} size={17} />
      </View>
      <View style={styles.body}>
        <Text style={styles.title}>{tx.title}</Text>
        <Text style={styles.sub} numberOfLines={1}>
          {tx.sub}
        </Text>
      </View>
      <View style={styles.right}>
        <Text style={[styles.amt, isCredit ? styles.credit : tx.type === "lock" ? styles.lock : styles.debit]}>
          {isCredit ? "+" : "−"}
          {amt}
        </Text>
        <Text style={styles.when}>{tx.when}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
  },
  last: { borderBottomWidth: 0 },
  icon: {
    width: 38,
    height: 38,
    borderRadius: 11,
    alignItems: "center",
    justifyContent: "center",
  },
  body: { flex: 1, minWidth: 0 },
  title: { fontSize: 14, fontWeight: "600", color: colors.text },
  sub: { fontSize: 11, color: colors.mid, marginTop: 1 },
  right: { alignItems: "flex-end" },
  amt: { fontSize: 14, fontWeight: "700" },
  credit: { color: colors.green },
  lock: { color: colors.amber },
  debit: { color: colors.text },
  when: { fontSize: 10, color: colors.mid, marginTop: 1 },
});
