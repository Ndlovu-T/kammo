import { router, useFocusEffect } from "expo-router";
import { useCallback, useMemo, useState } from "react";
import { RefreshControl, StyleSheet, Text, View } from "react-native";
import DealRow from "../../src/components/DealRow";
import Segment from "../../src/components/Segment";
import { Card, Screen, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { colors } from "../../src/theme";

export default function DealsTab() {
  const { deals, selectDeal, refreshSession, busy } = useKammo();
  const [tab, setTab] = useState("all");

  useFocusEffect(
    useCallback(() => {
      refreshSession().catch(() => {});
    }, [refreshSession])
  );

  const filtered = useMemo(() => {
    if (tab === "active") return deals.filter((d) => d.status !== "COMPLETED" && d.status !== "DISPUTED");
    if (tab === "done") return deals.filter((d) => d.status === "COMPLETED");
    return deals;
  }, [deals, tab]);

  const activeCount = deals.filter((d) => d.status !== "COMPLETED").length;
  const doneCount = deals.filter((d) => d.status === "COMPLETED").length;

  return (
    <Screen
      refreshControl={
        <RefreshControl refreshing={busy} onRefresh={() => refreshSession().catch(() => {})} tintColor={colors.green} />
      }
    >
      <Title style={styles.pageTitle}>Deals</Title>

      <Segment
        value={tab}
        onChange={setTab}
        options={[
          { id: "all", label: "All" },
          { id: "active", label: `Active (${activeCount})` },
          { id: "done", label: `Completed (${doneCount})` },
        ]}
      />

      {filtered.length === 0 ? (
        <Text style={styles.empty}>No deals in this tab.</Text>
      ) : (
        <Card style={styles.list}>
          {filtered.map((deal, i) => (
            <DealRow
              key={deal.dealCode}
              deal={deal}
              subtitle={deal.ownerRole === "BUYER" ? "Buying" : "Selling"}
              last={i === filtered.length - 1}
              onPress={async () => {
                await selectDeal(deal.dealCode);
                router.push(`/deal/${deal.dealCode}`);
              }}
            />
          ))}
        </Card>
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  pageTitle: { fontSize: 24, marginBottom: 4 },
  list: { padding: 0, overflow: "hidden", marginTop: 4 },
  empty: { color: colors.mid, fontSize: 14, textAlign: "center", paddingVertical: 32 },
});
