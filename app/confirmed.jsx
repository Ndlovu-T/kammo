import { router, useLocalSearchParams } from "expo-router";
import { useEffect, useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import { CheckCircle, TrendingUp } from "lucide-react-native";
import Logo from "../src/components/Logo";
import StarRating from "../src/components/StarRating";
import { Button, Card, ErrorText, Field, Screen, Subtitle, Title } from "../src/components/ui";
import { useKammo } from "../src/KammoContext";
import { formatRand } from "../src/utils";
import { colors } from "../src/theme";

export default function ConfirmedScreen() {
  const { code } = useLocalSearchParams();
  const { selectedDeal, selectDeal, rateDeal, profile, busy, error } = useKammo();
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState("");

  useEffect(() => {
    const dealCode = code || selectedDeal?.dealCode;
    if (dealCode) selectDeal(String(dealCode)).catch(() => {});
  }, [code]);

  const deal = selectedDeal;
  const dealRef = deal?.dealReference || deal?.dealCode;
  const amount = deal?.price ?? 0;
  const itemName = deal?.itemName || "—";

  async function submit() {
    if (!rating || !deal?.dealCode) return;
    try {
      await rateDeal(rating, comment);
      router.replace("/(tabs)");
    } catch {}
  }

  if (!deal) {
    return (
      <Screen>
        <Subtitle>Loading deal from backend...</Subtitle>
        <Button title="Back to Home" variant="outline" onPress={() => router.replace("/(tabs)")} />
      </Screen>
    );
  }

  return (
    <Screen style={styles.screen}>
      <View style={styles.center}>
        <Logo size="md" />
        <View style={styles.iconWrap}>
          <CheckCircle color={colors.green} size={46} />
        </View>
        <Title style={styles.title}>Deal Complete</Title>
        <Subtitle style={styles.sub}>
          Funds of {formatRand(amount)} released to seller via PayShap.
        </Subtitle>
        <Text style={styles.badge}>{dealRef}</Text>
      </View>

      <Card style={styles.rateCard}>
        <Text style={styles.rateTitle}>Rate your experience</Text>
        <Subtitle style={styles.rateSub}>Ratings build trust and make KAMMO safer for everyone.</Subtitle>
        <View style={styles.sellerRow}>
          <Text style={styles.sellerAvatar}>
            {(deal.otherPartyPhoneNumber || "??").slice(-2)}
          </Text>
          <View>
            <Text style={styles.sellerName}>Other party · {deal.otherPartyPhoneNumber}</Text>
            <Text style={styles.sellerItem}>{itemName}</Text>
          </View>
        </View>
        <StarRating value={rating} onChange={setRating} />
        <Field
          placeholder="Leave a comment (optional)..."
          value={comment}
          onChangeText={setComment}
          multiline
        />
        <ErrorText>{error}</ErrorText>
        <Button
          title={busy ? "Submitting..." : "Submit Rating"}
          onPress={submit}
          disabled={!rating || busy}
          loading={busy}
        />
      </Card>

      <View style={styles.trustBoost}>
        <TrendingUp color={colors.green} size={18} />
        <View>
          <Text style={styles.boostTitle}>Trust score updated</Text>
          <Text style={styles.boostSub}>
            Your profile: {profile?.trustScore ?? 98}% · {profile?.rating ?? 4.9} / 5
          </Text>
        </View>
      </View>

      <Button title="Back to Home" variant="outline" onPress={() => router.replace("/(tabs)")} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  screen: { paddingTop: 8 },
  center: { alignItems: "center", gap: 12, marginBottom: 16 },
  iconWrap: {
    width: 90,
    height: 90,
    borderRadius: 28,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.3)",
    alignItems: "center",
    justifyContent: "center",
  },
  title: { fontSize: 28, textAlign: "center" },
  sub: { textAlign: "center", fontSize: 14 },
  badge: {
    color: colors.green,
    fontFamily: "monospace",
    fontSize: 12,
    fontWeight: "700",
    backgroundColor: colors.surface3,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  rateCard: { gap: 12 },
  rateTitle: { color: colors.white, fontSize: 16, fontWeight: "700" },
  rateSub: { marginBottom: 4, fontSize: 12 },
  sellerRow: { flexDirection: "row", alignItems: "center", gap: 12 },
  sellerAvatar: {
    width: 44,
    height: 44,
    borderRadius: 12,
    backgroundColor: colors.green,
    color: "#000",
    fontSize: 16,
    fontWeight: "800",
    textAlign: "center",
    lineHeight: 44,
  },
  sellerName: { color: colors.white, fontSize: 15, fontWeight: "700" },
  sellerItem: { color: colors.grey, fontSize: 12, marginTop: 2 },
  trustBoost: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.2)",
    borderRadius: 16,
    padding: 16,
    marginVertical: 8,
  },
  boostTitle: { color: colors.green, fontSize: 13, fontWeight: "700" },
  boostSub: { color: "rgba(0,194,120,0.7)", fontSize: 12, marginTop: 2 },
});
