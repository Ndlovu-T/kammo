import { router } from "expo-router";
import { StyleSheet, Text, View } from "react-native";
import { Link2, Lock, Package } from "lucide-react-native";
import ProgressBar from "../../src/components/ProgressBar";
import WizardHeader from "../../src/components/WizardHeader";
import { Button, Card, ErrorText, Screen, Subtitle, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { formatRand } from "../../src/utils";
import { colors } from "../../src/theme";

export default function CreateDealReview() {
  const { createDraft, submitCreateDeal, busy, error } = useKammo();
  const price = Number(createDraft.price) || 0;
  const fee = price * 0.01;
  const courier = createDraft.delivery !== "meetup" ? 120 : 0;
  const total = price + fee + courier;
  const deliveryLabel =
    createDraft.delivery === "locker" ? "PUDO Locker" : createDraft.delivery === "meetup" ? "Meetup" : "Courier";

  async function onCreate() {
    try {
      await submitCreateDeal();
      router.replace("/create-deal/success");
    } catch {}
  }

  return (
    <Screen>
      <WizardHeader title="Review Deal" step={3} progress={75} backTo="/create-deal/details" />
      <Title style={styles.head}>Confirm the details</Title>
      <Subtitle style={styles.sub}>Both parties agree to these terms when payment is made.</Subtitle>

      <View style={styles.dealCard}>
        <View style={styles.dealHeader}>
          <Text style={styles.dealBrand}>KAMMO DEAL</Text>
          <View style={styles.dealLine} />
          <Text style={styles.dealNew}>NEW</Text>
        </View>
        <Text style={styles.dealLbl}>Item</Text>
        <Text style={styles.dealItem}>{createDraft.itemName || "—"}</Text>
        <View style={styles.dealRow}>
          <View>
            <Text style={styles.dealLbl}>Agreed price</Text>
            <Text style={styles.dealPrice}>{formatRand(price)}</Text>
          </View>
          <View style={styles.dealRight}>
            <Text style={styles.dealLbl}>Delivery</Text>
            <View style={styles.delRow}>
              <Package color={colors.white} size={13} />
              <Text style={styles.delTxt}>{deliveryLabel}</Text>
            </View>
          </View>
        </View>
        <View style={styles.dealFooter}>
          <Text style={styles.dealMeta}>Inspection: {createDraft.inspectionHours}h after delivery</Text>
          <Text style={styles.dealMeta}>KAMMO Fee: {formatRand(fee)}</Text>
        </View>
      </View>

      <Card>
        <Text style={styles.feeTitle}>Fee breakdown</Text>
        {[["Item price", formatRand(price)], ["KAMMO fee (1%)", formatRand(fee)], ["Delivery (est.)", formatRand(courier)]].map(([k, v]) => (
          <View key={k} style={styles.feeRow}>
            <Text style={styles.feeKey}>{k}</Text>
            <Text style={styles.feeVal}>{v}</Text>
          </View>
        ))}
        <View style={styles.feeDivider} />
        <View style={styles.feeRow}>
          <Text style={styles.feeTotal}>Total to pay</Text>
          <Text style={styles.feeTotalVal}>{formatRand(total)}</Text>
        </View>
      </Card>

      <View style={styles.alert}>
        <Lock color={colors.green} size={14} />
        <Text style={styles.alertText}>
          Funds held by our licensed payment partner. Released only after you confirm delivery.
        </Text>
      </View>

      <ErrorText>{error}</ErrorText>
      <Button title="Create Deal Link" onPress={onCreate} loading={busy} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  head: { fontSize: 24, marginTop: 8 },
  sub: { marginBottom: 8 },
  dealCard: {
    backgroundColor: "#0C1F18",
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.25)",
    borderRadius: 20,
    padding: 20,
    gap: 8,
    marginBottom: 4,
  },
  dealHeader: { flexDirection: "row", alignItems: "center", gap: 10, marginBottom: 8 },
  dealBrand: { color: colors.green, fontSize: 13, fontWeight: "800", letterSpacing: 1 },
  dealLine: { flex: 1, height: 1, backgroundColor: "rgba(0,194,120,0.2)" },
  dealNew: {
    color: colors.green,
    fontSize: 11,
    fontWeight: "700",
    fontFamily: "monospace",
    backgroundColor: colors.surface3,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
  },
  dealLbl: { color: "rgba(0,194,120,0.6)", fontSize: 11, textTransform: "uppercase", letterSpacing: 0.5 },
  dealItem: { color: colors.white, fontSize: 15, fontWeight: "700" },
  dealRow: { flexDirection: "row", justifyContent: "space-between", marginTop: 8 },
  dealPrice: { color: colors.white, fontSize: 28, fontWeight: "800", marginTop: 2 },
  dealRight: { alignItems: "flex-end" },
  delRow: { flexDirection: "row", alignItems: "center", gap: 5, marginTop: 2 },
  delTxt: { color: colors.white, fontSize: 14, fontWeight: "600" },
  dealFooter: {
    flexDirection: "row",
    justifyContent: "space-between",
    borderTopWidth: 1,
    borderTopColor: "rgba(0,194,120,0.15)",
    paddingTop: 12,
    marginTop: 8,
  },
  dealMeta: { color: "rgba(0,194,120,0.6)", fontSize: 12 },
  feeTitle: {
    color: colors.grey,
    fontSize: 11,
    fontWeight: "700",
    textTransform: "uppercase",
    letterSpacing: 0.6,
    marginBottom: 8,
  },
  feeRow: { flexDirection: "row", justifyContent: "space-between", marginBottom: 10 },
  feeKey: { color: colors.grey, fontSize: 14 },
  feeVal: { color: colors.white, fontSize: 14, fontWeight: "600" },
  feeDivider: { height: 1, backgroundColor: colors.border, marginBottom: 10 },
  feeTotal: { color: colors.white, fontSize: 15, fontWeight: "700" },
  feeTotalVal: { color: colors.green, fontSize: 18, fontWeight: "800" },
  alert: {
    flexDirection: "row",
    gap: 10,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.15)",
    borderRadius: 14,
    padding: 14,
  },
  alertText: { flex: 1, color: colors.green, fontSize: 12, lineHeight: 18 },
});
