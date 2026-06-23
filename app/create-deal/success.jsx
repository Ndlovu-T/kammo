import { router } from "expo-router";
import { useState } from "react";
import { Share, StyleSheet, Text, View } from "react-native";
import { Check, MessageSquare, Share2 } from "lucide-react-native";
import { Button, Card, Screen } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { formatRand } from "../../src/utils";
import { colors } from "../../src/theme";

export default function CreateDealSuccess() {
  const { selectedDeal } = useKammo();
  const [copied, setCopied] = useState(false);

  if (!selectedDeal) {
    return (
      <Screen bg="bg">
        <Text style={styles.empty}>No deal found. Go back and try again.</Text>
        <Button title="Start over" onPress={() => router.replace("/create-deal")} />
      </Screen>
    );
  }

  const link = selectedDeal.dealLink || `https://kammo.co.za/deal/${selectedDeal.dealCode}`;
  const displayLink = link.replace(/^https?:\/\//, "");
  const ref = selectedDeal.dealReference || `KM-${selectedDeal.dealCode}`;

  async function copyLink() {
    try {
      await Share.share({ message: link });
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {}
  }

  return (
    <Screen bg="bg1" style={{ padding: 0 }}>
      <View style={styles.hero}>
        <View style={styles.checkWrap}>
          <Check color="#000" size={26} />
        </View>
        <View>
          <Text style={styles.liveTag}>Deal is Live</Text>
          <Text style={styles.ref}>{ref}</Text>
        </View>
      </View>

      <View style={styles.tags}>
        <Text style={styles.tag}>{selectedDeal.itemName}</Text>
        <Text style={styles.tag}>{formatRand(selectedDeal.price)}</Text>
        <Text style={styles.tag}>
          {selectedDeal.deliveryMethod === "MEETUP" ? "Meetup" : "Courier"} · {selectedDeal.inspectionWindowHours || 24}h inspect
        </Text>
      </View>

      <View style={styles.body}>
        <Card style={styles.linkCard}>
          <Text style={styles.cap}>Secure Deal Link</Text>
          <Text style={styles.link} selectable>
            {displayLink}
          </Text>
          <View style={styles.linkActions}>
            <Button title={copied ? "Copied!" : "Copy"} variant="ghost" onPress={copyLink} />
            <View style={styles.shareRow}>
              <MessageSquare color={colors.waGreen} size={18} />
              <Share2 color={colors.mid} size={18} />
            </View>
          </View>
        </Card>

        <Button title="Track This Deal" onPress={() => router.replace(`/deal/${selectedDeal.dealCode}`)} />
        <Button title="Back to Home" variant="ghost" onPress={() => router.replace("/(tabs)")} />
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  hero: {
    backgroundColor: "#002d16",
    padding: 22,
    flexDirection: "row",
    alignItems: "center",
    gap: 14,
  },
  checkWrap: {
    width: 52,
    height: 52,
    borderRadius: 16,
    backgroundColor: colors.green,
    alignItems: "center",
    justifyContent: "center",
  },
  liveTag: {
    fontSize: 11,
    fontWeight: "700",
    letterSpacing: 0.8,
    textTransform: "uppercase",
    color: "rgba(0,217,126,0.6)",
    marginBottom: 3,
  },
  ref: { fontSize: 22, fontWeight: "900", color: "#fff", letterSpacing: -0.8 },
  tags: { flexDirection: "row", flexWrap: "wrap", gap: 7, padding: 18, backgroundColor: "#001a0d" },
  tag: {
    backgroundColor: "rgba(0,217,126,0.12)",
    borderWidth: 1,
    borderColor: "rgba(0,217,126,0.2)",
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 5,
    fontSize: 11,
    fontWeight: "600",
    color: "rgba(0,217,126,0.9)",
  },
  body: { padding: 18, gap: 12 },
  linkCard: { gap: 10 },
  cap: {
    fontSize: 11,
    fontWeight: "600",
    letterSpacing: 0.5,
    textTransform: "uppercase",
    color: colors.mid,
  },
  link: { fontSize: 14, fontWeight: "600", color: colors.green, fontFamily: "monospace" },
  linkActions: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginTop: 4 },
  shareRow: { flexDirection: "row", gap: 16, paddingRight: 8 },
  empty: { color: colors.mid, textAlign: "center", marginBottom: 16 },
});
