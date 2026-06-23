import { router, useFocusEffect } from "expo-router";
import { useCallback } from "react";
import { Pressable, RefreshControl, StyleSheet, Text, View } from "react-native";
import { Bell, Plus } from "lucide-react-native";
import ActiveDealCard from "../../src/components/ActiveDealCard";
import TrustIdentityCard from "../../src/components/TrustIdentityCard";
import WalletCard from "../../src/components/WalletCard";
import WalletTxRow from "../../src/components/WalletTxRow";
import { Card, Screen, SectionHeader } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { timeAgo } from "../../src/utils";
import { colors } from "../../src/theme";

export default function HomeTab() {
  const { deals, displayName, profile, selectDeal, refreshSession, busy, wallet } = useKammo();
  const activeDeals = deals.filter((d) => d.status !== "COMPLETED");
  const completed = deals.filter((d) => d.status === "COMPLETED").slice(0, 3);
  const trustScore = profile?.trustScore ?? 96;

  useFocusEffect(
    useCallback(() => {
      refreshSession().catch(() => {});
    }, [refreshSession])
  );

  async function openDeal(deal) {
    await selectDeal(deal.dealCode);
    router.push(`/deal/${deal.dealCode}`);
  }

  return (
    <Screen
      style={styles.screen}
      refreshControl={
        <RefreshControl refreshing={busy} onRefresh={() => refreshSession().catch(() => {})} tintColor={colors.green} />
      }
    >
      <View style={styles.topGlow} />
      <View style={styles.header}>
        <Text style={styles.logo}>
          K<Text style={styles.logoEm}>AMM</Text>O<Text style={styles.logoDot}>.</Text>
        </Text>
        <Pressable style={styles.iconBtn} onPress={() => router.push("/(tabs)/updates")}>
          <Bell color={colors.text} size={17} />
          <View style={styles.dot} />
        </Pressable>
      </View>

      <TrustIdentityCard
        displayName={displayName}
        kammoId={profile?.kammoId}
        trustScore={trustScore}
        dealCount={profile?.dealCount ?? deals.length}
      />

      <WalletCard wallet={wallet} />

      <SectionHeader
        title={`Active Deals${activeDeals.length ? ` · ${activeDeals.length}` : ""}`}
        action={activeDeals.length ? "View all" : undefined}
        onAction={() => router.push("/(tabs)/deals")}
      />

      {activeDeals.length === 0 ? (
        <Text style={styles.empty}>No active deals — start one below.</Text>
      ) : (
        <View style={styles.list}>
          {activeDeals.slice(0, 3).map((deal) => (
            <ActiveDealCard key={deal.dealCode} deal={deal} onPress={() => openDeal(deal)} />
          ))}
        </View>
      )}

      <SectionHeader title="Recent Activity" action="View History" onAction={() => router.push("/(tabs)/wallet")} />

      {completed.length === 0 ? (
        <Text style={styles.empty}>Completed deals will appear here.</Text>
      ) : (
        <Card style={styles.txList}>
          {completed.map((deal, i) => (
            <WalletTxRow
              key={deal.dealCode}
              tx={{
                id: deal.dealCode,
                type: "credit",
                title: deal.itemName,
                sub: `${deal.dealReference || `KM-${deal.dealCode}`} · ${timeAgo(deal.updatedAt || deal.createdAt)}`,
                amount: deal.price,
                when: timeAgo(deal.updatedAt || deal.createdAt),
              }}
              last={i === completed.length - 1}
            />
          ))}
        </Card>
      )}

      <Pressable style={styles.fabBtn} onPress={() => router.push("/create-deal")}>
        <Plus color={colors.ink} size={20} strokeWidth={2.5} />
        <Text style={styles.fabTxt}>START NEW DEAL</Text>
      </Pressable>
    </Screen>
  );
}

const styles = StyleSheet.create({
  screen: { paddingTop: 8 },
  topGlow: {
    position: "absolute",
    top: 0,
    left: 0,
    right: 0,
    height: 180,
    backgroundColor: "rgba(0,232,122,0.04)",
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 16,
  },
  logo: { fontSize: 24, fontWeight: "400", color: colors.text, letterSpacing: -0.5 },
  logoEm: { fontStyle: "italic" },
  logoDot: { color: colors.green },
  iconBtn: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: colors.ink3,
    borderWidth: 1,
    borderColor: colors.line2,
    alignItems: "center",
    justifyContent: "center",
  },
  dot: {
    position: "absolute",
    top: 5,
    right: 5,
    width: 7,
    height: 7,
    borderRadius: 4,
    backgroundColor: colors.red,
    borderWidth: 1.5,
    borderColor: colors.ink3,
  },
  list: { gap: 9 },
  empty: { color: colors.mid, fontSize: 13, textAlign: "center", paddingVertical: 12 },
  txList: { paddingHorizontal: 16, paddingVertical: 4 },
  fabBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 9,
    paddingVertical: 16,
    borderRadius: 15,
    backgroundColor: colors.green,
    marginTop: 8,
    shadowColor: colors.green,
    shadowOpacity: 0.25,
    shadowRadius: 12,
    elevation: 4,
  },
  fabTxt: { color: colors.ink, fontSize: 15, fontWeight: "800", letterSpacing: -0.3 },
});
