import { router, useFocusEffect } from "expo-router";
import { useCallback } from "react";
import { Pressable, RefreshControl, StyleSheet, Text, View } from "react-native";
import { ArrowDownLeft, Lock, Plus, TrendingUp } from "lucide-react-native";
import WalletTxRow from "../../src/components/WalletTxRow";
import { Card, Screen, SectionHeader, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { formatWalletAmount } from "../../src/wallet";
import { colors } from "../../src/theme";

export default function WalletTab() {
  const { wallet, refreshSession, busy } = useKammo();
  const { whole, cents } = formatWalletAmount(wallet.available);

  useFocusEffect(
    useCallback(() => {
      refreshSession().catch(() => {});
    }, [refreshSession])
  );

  return (
    <Screen
      refreshControl={
        <RefreshControl refreshing={busy} onRefresh={() => refreshSession().catch(() => {})} tintColor={colors.green} />
      }
    >
      <Title style={styles.pageTitle}>Wallet</Title>

      <View style={styles.hero}>
        <View style={styles.glow} />
        <Text style={styles.heroLabel}>AVAILABLE BALANCE</Text>
        <Text style={styles.heroBal}>
          R {whole.toLocaleString("en-ZA")}
          <Text style={styles.heroCents}>.{cents}</Text>
        </Text>
        <View style={styles.heroActions}>
          <Pressable style={styles.addBtn} onPress={() => router.push("/wallet/add")}>
            <Plus color="#fff" size={15} />
            <Text style={styles.addTxt}>Add Funds</Text>
          </Pressable>
          <Pressable style={styles.withBtn} onPress={() => router.push("/wallet/withdraw")}>
            <ArrowDownLeft color="#ebebe5" size={15} />
            <Text style={styles.withTxt}>Withdraw</Text>
          </Pressable>
        </View>
      </View>

      <View style={styles.tiles}>
        <View style={styles.tile}>
          <View style={styles.tileHead}>
            <Lock color={colors.amber} size={14} />
            <Text style={[styles.tileLbl, { color: colors.amber }]}>LOCKED</Text>
          </View>
          <Text style={styles.tileVal}>{formatWalletAmount(wallet.locked).display}</Text>
          <Text style={styles.tileSub}>In {wallet.activeCount} active deal{wallet.activeCount === 1 ? "" : "s"}</Text>
        </View>
        <View style={styles.tile}>
          <View style={styles.tileHead}>
            <TrendingUp color={colors.green} size={14} />
            <Text style={[styles.tileLbl, { color: colors.green }]}>LIFETIME</Text>
          </View>
          <Text style={styles.tileVal}>{formatWalletAmount(wallet.lifetime).display}</Text>
          <Text style={styles.tileSub}>{wallet.dealCount} deals processed</Text>
        </View>
      </View>

      <SectionHeader title="Transactions" />

      {wallet.transactions.length === 0 ? (
        <Text style={styles.empty}>No transactions yet.</Text>
      ) : (
        <Card style={styles.list}>
          {wallet.transactions.map((tx, i) => (
            <WalletTxRow key={tx.id} tx={tx} last={i === wallet.transactions.length - 1} />
          ))}
        </Card>
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  pageTitle: { fontSize: 24, marginBottom: 8 },
  hero: {
    borderRadius: 20,
    backgroundColor: "#0c1f14",
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.25)",
    padding: 22,
    overflow: "hidden",
    marginBottom: 12,
  },
  glow: {
    position: "absolute",
    top: -50,
    right: -50,
    width: 170,
    height: 170,
    borderRadius: 85,
    backgroundColor: "rgba(0,232,122,0.12)",
  },
  heroLabel: {
    fontSize: 10,
    fontWeight: "700",
    color: "rgba(0,232,122,0.6)",
    letterSpacing: 1.5,
    marginBottom: 6,
  },
  heroBal: { fontSize: 34, fontWeight: "800", color: "#fff", letterSpacing: -1.5 },
  heroCents: { fontSize: 18 },
  heroActions: { flexDirection: "row", gap: 10, marginTop: 16 },
  addBtn: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 6,
    paddingVertical: 12,
    borderRadius: 12,
    backgroundColor: colors.green,
  },
  addTxt: { color: colors.ink, fontSize: 13, fontWeight: "700" },
  withBtn: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 6,
    paddingVertical: 12,
    borderRadius: 12,
    backgroundColor: "rgba(255,255,255,0.06)",
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.1)",
  },
  withTxt: { color: "#ebebe5", fontSize: 13, fontWeight: "600" },
  tiles: { flexDirection: "row", gap: 10, marginBottom: 8 },
  tile: {
    flex: 1,
    backgroundColor: colors.bg,
    borderWidth: 1.5,
    borderColor: colors.line2,
    borderRadius: 14,
    padding: 14,
  },
  tileHead: { flexDirection: "row", alignItems: "center", gap: 6, marginBottom: 6 },
  tileLbl: { fontSize: 9, fontWeight: "700", letterSpacing: 0.8 },
  tileVal: { fontSize: 18, fontWeight: "800", color: colors.text },
  tileSub: { fontSize: 11, color: colors.mid, marginTop: 2 },
  list: { paddingHorizontal: 16, paddingVertical: 4 },
  empty: { color: colors.mid, textAlign: "center", paddingVertical: 24 },
});
