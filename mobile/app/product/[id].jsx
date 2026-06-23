import { router, useLocalSearchParams } from "expo-router";
import { useEffect } from "react";
import { StyleSheet, Text, View } from "react-native";
import { Lock, MapPin } from "lucide-react-native";
import Logo from "../../src/components/Logo";
import { Button, Card, Screen, Subtitle, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { formatRand } from "../../src/utils";
import { colors } from "../../src/theme";

export default function ProductScreen() {
  const { id } = useLocalSearchParams();
  const { selectedListing, openListing, setCreateDraft, busy } = useKammo();

  useEffect(() => {
    if (id) openListing(String(id)).catch(() => {});
  }, [id]);

  const p = selectedListing;

  if (!p) {
    return (
      <Screen>
        <Subtitle>Loading listing...</Subtitle>
      </Screen>
    );
  }

  function buySecurely() {
    setCreateDraft((draft) => ({
      ...draft,
      role: "buyer",
      itemName: p.name,
      price: String(p.price),
      description: p.description || "",
    }));
    router.push("/create-deal");
  }

  return (
    <Screen>
      <Logo size="sm" />
      <View style={styles.badgeRow}>
        <Lock size={12} color={colors.green} />
        <Text style={styles.badge}>KAMMO SECURED</Text>
      </View>
      <Title>{p.name}</Title>
      <Text style={styles.price}>{formatRand(p.price)}</Text>
      <View style={styles.pills}>
        <View style={styles.pillRow}>
          <MapPin size={10} color={colors.grey} />
          <Text style={styles.pill}>{p.location}</Text>
        </View>
        <Text style={styles.pill}>Seller {p.sellerUsername}</Text>
        <Text style={styles.pill}>★ {p.sellerRating}</Text>
      </View>
      <Card>
        <Text style={styles.desc}>{p.description}</Text>
      </Card>
      <Button title={`Buy Securely — ${formatRand(p.price)}`} onPress={buySecurely} loading={busy} />
      <Button title="Back to Market" variant="outline" onPress={() => router.back()} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  badgeRow: { flexDirection: "row", alignItems: "center", gap: 6, marginBottom: 8 },
  badge: { color: colors.green, fontSize: 10, fontWeight: "800", letterSpacing: 0.5 },
  price: { color: colors.green, fontSize: 28, fontWeight: "800", marginVertical: 8 },
  pills: { flexDirection: "row", flexWrap: "wrap", gap: 8, marginBottom: 12, alignItems: "center" },
  pillRow: { flexDirection: "row", alignItems: "center", gap: 4 },
  pill: { color: colors.grey, fontSize: 12 },
  desc: { color: colors.grey, lineHeight: 22, fontSize: 14 },
});
