import { Pressable, StyleSheet, Text, View } from "react-native";
import { ChevronRight, Check } from "lucide-react-native";
import { router } from "expo-router";
import TrustRing from "./TrustRing";
import { colors } from "../theme";

function VerifyChip({ label }) {
  return (
    <View style={styles.chip}>
      <Check color={colors.green} size={11} />
      <Text style={styles.chipTxt}>{label}</Text>
    </View>
  );
}

export default function TrustIdentityCard({ displayName, kammoId, trustScore = 96, dealCount = 0 }) {
  const id = kammoId?.startsWith("KAM-") ? kammoId : kammoId ? `KAM-${kammoId}` : "KAM-------";

  return (
    <Pressable style={styles.card} onPress={() => router.push("/(tabs)/profile")}>
      <View style={styles.glow} />
      <View style={styles.row}>
        <TrustRing score={trustScore} />
        <View style={styles.body}>
          <Text style={styles.name}>{displayName}</Text>
          <Text style={styles.id}>{id}</Text>
          <View style={styles.chips}>
            <VerifyChip label="ID" />
            <VerifyChip label="Phone" />
            <VerifyChip label="Face" />
            {dealCount > 0 ? (
              <View style={styles.dealChip}>
                <Text style={styles.dealChipTxt}>{dealCount} deals</Text>
              </View>
            ) : null}
          </View>
        </View>
        <ChevronRight color={colors.mid} size={18} />
      </View>
      <View style={styles.footer}>
        <Text style={styles.footerLbl}>TRUST SCORE</Text>
        <View style={styles.barWrap}>
          <View style={styles.bar}>
            <View style={[styles.barFill, { width: `${trustScore}%` }]} />
          </View>
          <Text style={styles.scoreTxt}>
            {trustScore}
            <Text style={styles.scoreSub}>/100</Text>
          </Text>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.ink3,
    borderWidth: 1,
    borderColor: colors.line2,
    borderRadius: 20,
    padding: 18,
    overflow: "hidden",
    marginBottom: 12,
  },
  glow: {
    position: "absolute",
    top: -30,
    right: -30,
    width: 120,
    height: 120,
    borderRadius: 60,
    backgroundColor: "rgba(0,232,122,0.09)",
  },
  row: { flexDirection: "row", alignItems: "center", gap: 14 },
  body: { flex: 1 },
  name: { fontSize: 17, fontWeight: "800", color: colors.text, letterSpacing: -0.4, lineHeight: 20 },
  id: { fontSize: 11, color: colors.green, letterSpacing: 0.8, marginVertical: 3 },
  chips: { flexDirection: "row", flexWrap: "wrap", gap: 5, marginTop: 4 },
  chip: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 20,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.2)",
  },
  chipTxt: { fontSize: 10.5, color: colors.green, fontWeight: "600" },
  dealChip: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 20,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.2)",
  },
  dealChipTxt: { fontSize: 10.5, color: colors.green, fontWeight: "600" },
  footer: {
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: colors.line,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  footerLbl: { fontSize: 10, color: colors.mid, letterSpacing: 0.8 },
  barWrap: { flexDirection: "row", alignItems: "center", gap: 8 },
  bar: { width: 120, height: 4, backgroundColor: colors.ink4, borderRadius: 2, overflow: "hidden" },
  barFill: { height: "100%", backgroundColor: colors.green, borderRadius: 2 },
  scoreTxt: { fontSize: 12, fontWeight: "700", color: colors.green },
  scoreSub: { fontSize: 10, color: colors.mid },
});
