import { Pressable, StyleSheet, Text, View } from "react-native";
import { router } from "expo-router";
import { formatRand } from "../utils";
import { formatWalletAmount } from "../wallet";
import { colors } from "../theme";

export default function WalletCard({ wallet, compact }) {
  const { whole, cents } = formatWalletAmount(wallet?.available ?? 0);

  return (
    <Pressable
      style={[styles.card, compact && styles.compact]}
      onPress={() => router.push("/(tabs)/wallet")}
    >
      <View style={styles.glow} />
      <View style={styles.inner}>
        <View style={styles.top}>
          <View>
            <Text style={styles.label}>WALLET</Text>
            <Text style={styles.balance}>
              R {whole.toLocaleString("en-ZA")}
              <Text style={styles.cents}>.{cents}</Text>
            </Text>
            <Text style={styles.meta}>
              {formatRand(wallet?.locked ?? 0)} locked · {formatRand(wallet?.lifetime ?? 0)} lifetime
            </Text>
          </View>
          <View style={styles.actions}>
            <Pressable style={styles.addBtn} onPress={() => router.push("/wallet/add")}>
              <Text style={styles.addTxt}>Add</Text>
            </Pressable>
            <Pressable style={styles.withBtn} onPress={() => router.push("/wallet/withdraw")}>
              <Text style={styles.withTxt}>Withdraw</Text>
            </Pressable>
          </View>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 18,
    backgroundColor: "#0c1f14",
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.2)",
    overflow: "hidden",
    marginVertical: 8,
  },
  compact: { marginVertical: 4 },
  glow: {
    position: "absolute",
    top: -50,
    right: -50,
    width: 160,
    height: 160,
    borderRadius: 80,
    backgroundColor: "rgba(0,232,122,0.1)",
  },
  inner: { padding: 18 },
  top: { flexDirection: "row", justifyContent: "space-between", alignItems: "flex-start", gap: 12 },
  label: {
    fontSize: 10,
    fontWeight: "700",
    color: "rgba(0,232,122,0.6)",
    letterSpacing: 1.4,
    marginBottom: 5,
  },
  balance: {
    fontSize: 28,
    fontWeight: "800",
    color: "#fff",
    letterSpacing: -1.5,
  },
  cents: { fontSize: 16 },
  meta: { fontSize: 11, color: colors.dim, marginTop: 5 },
  actions: { gap: 7 },
  addBtn: {
    paddingHorizontal: 14,
    paddingVertical: 9,
    borderRadius: 10,
    backgroundColor: colors.green,
  },
  addTxt: { fontSize: 12, fontWeight: "700", color: colors.ink },
  withBtn: {
    paddingHorizontal: 14,
    paddingVertical: 9,
    borderRadius: 10,
    backgroundColor: "rgba(255,255,255,0.07)",
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.08)",
  },
  withTxt: { fontSize: 12, fontWeight: "600", color: "#ebebe5" },
});
