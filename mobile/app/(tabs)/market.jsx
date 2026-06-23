import { router, useFocusEffect } from "expo-router";
import { useCallback, useEffect, useState } from "react";
import { RefreshControl, StyleSheet, Text, View } from "react-native";
import { Lock } from "lucide-react-native";
import BackendBanner from "../../src/components/BackendBanner";
import { Card, Field, Screen, Subtitle, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { formatRand } from "../../src/utils";
import { colors } from "../../src/theme";

const CATEGORIES = ["", "Electronics", "Fashion", "Gaming"];

export default function MarketTab() {
  const {
    listings,
    loadListings,
    openListing,
    backendOk,
    sessionError,
    checkBackend,
    busy,
  } = useKammo();
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");

  useEffect(() => {
    loadListings({ category, search }).catch(() => {});
  }, [category, search]);

  useFocusEffect(
    useCallback(() => {
      loadListings({ category, search }).catch(() => {});
    }, [category, search, loadListings])
  );

  return (
    <Screen
      refreshControl={
        <RefreshControl
          refreshing={busy}
          onRefresh={() => loadListings({ category, search }).catch(() => {})}
          tintColor={colors.green}
        />
      }
    >
      <BackendBanner
        ok={backendOk}
        error={sessionError}
        onRetry={async () => {
          await checkBackend();
          await loadListings({ category, search }).catch(() => {});
        }}
      />
      <Title>Marketplace</Title>
      <Subtitle>All listings are KAMMO-secured · synced from backend</Subtitle>
      <Field placeholder="Search listings..." value={search} onChangeText={setSearch} />

      <View style={styles.cats}>
        {CATEGORIES.map((c) => (
          <Text
            key={c || "all"}
            style={[styles.cat, category === c && styles.catOn]}
            onPress={() => setCategory(c)}
          >
            {c || "All"}
          </Text>
        ))}
      </View>

      {listings.length === 0 ? (
        <Subtitle>No listings — pull down to refresh or check backend is running.</Subtitle>
      ) : (
        listings.map((item) => (
          <Card
            key={item.id}
            onPress={async () => {
              await openListing(item.id);
              router.push(`/product/${item.id}`);
            }}
          >
            <View style={styles.row}>
              <Lock size={14} color={colors.green} />
              <Text style={styles.secured}>SECURED</Text>
            </View>
            <Text style={styles.name}>{item.name}</Text>
            <Text style={styles.price}>{formatRand(item.price)}</Text>
            <Text style={styles.meta}>{item.sellerUsername} · {item.location}</Text>
          </Card>
        ))
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  cats: { flexDirection: "row", flexWrap: "wrap", gap: 8, marginBottom: 8 },
  cat: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: colors.surface2,
    borderWidth: 1,
    borderColor: colors.border,
    color: colors.grey,
    fontSize: 12,
    fontWeight: "600",
    overflow: "hidden",
  },
  catOn: { backgroundColor: colors.green, color: "#000", borderColor: colors.green },
  row: { flexDirection: "row", alignItems: "center", gap: 4, marginBottom: 6 },
  secured: { color: colors.green, fontSize: 9, fontWeight: "800" },
  name: { color: colors.white, fontSize: 15, fontWeight: "700" },
  price: { color: colors.green, fontSize: 18, fontWeight: "800", marginTop: 4 },
  meta: { color: colors.grey, fontSize: 12, marginTop: 4 },
});
