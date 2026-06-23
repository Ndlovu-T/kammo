import { Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { ArrowLeft } from "lucide-react-native";
import { router } from "expo-router";
import { useState } from "react";
import { Button, ErrorText, Screen, Subtitle, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { colors } from "../../src/theme";

const QUICK = [500, 1000, 2500, 5000];

export default function WalletAddScreen() {
  const { addFunds, busy, error } = useKammo();
  const [amount, setAmount] = useState("1000");
  const [chip, setChip] = useState("1000");

  async function onContinue() {
    try {
      await addFunds(amount);
      router.back();
    } catch {}
  }

  return (
    <Screen bg="bg1">
      <View style={styles.topbar}>
        <Pressable style={styles.back} onPress={() => router.back()}>
          <ArrowLeft color={colors.green} size={22} />
        </Pressable>
        <Text style={styles.topTitle}>Add Funds</Text>
      </View>

      <Title>How much to add?</Title>
      <Subtitle>Top up your KAMMO Wallet for instant deal payments.</Subtitle>

      <View style={styles.amountWrap}>
        <Text style={styles.currency}>R</Text>
        <TextInput
          style={styles.amountInput}
          value={amount}
          onChangeText={(v) => { setAmount(v); setChip(""); }}
          keyboardType="numeric"
          placeholder="0"
          placeholderTextColor={colors.dim}
        />
      </View>
      <View style={styles.underline} />

      <View style={styles.chips}>
        {QUICK.map((n) => (
          <Pressable
            key={n}
            style={[styles.chip, chip === String(n) && styles.chipOn]}
            onPress={() => { setAmount(String(n)); setChip(String(n)); }}
          >
            <Text style={[styles.chipTxt, chip === String(n) && styles.chipTxtOn]}>
              R {n.toLocaleString("en-ZA")}
            </Text>
          </Pressable>
        ))}
      </View>

      <Text style={styles.label}>PAYMENT METHOD</Text>
      <View style={styles.method}>
        <Text style={styles.methodTitle}>Online Payment</Text>
        <Text style={styles.methodSub}>Card · Instant EFT · PayShap · Ozow</Text>
      </View>

      <ErrorText>{error}</ErrorText>
      <Button title="Continue to payment" onPress={onContinue} loading={busy} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  topbar: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    marginBottom: 8,
    paddingBottom: 8,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
  },
  back: { padding: 4 },
  topTitle: { fontSize: 15, fontWeight: "700", color: colors.text },
  amountWrap: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 4,
    paddingVertical: 14,
  },
  currency: { fontSize: 36, fontWeight: "800", color: colors.mid },
  amountInput: {
    fontSize: 40,
    fontWeight: "800",
    color: colors.text,
    minWidth: 120,
    textAlign: "center",
  },
  underline: {
    height: 2,
    width: 200,
    alignSelf: "center",
    backgroundColor: colors.line2,
    borderRadius: 2,
    marginBottom: 18,
  },
  chips: { flexDirection: "row", flexWrap: "wrap", gap: 8, justifyContent: "center", marginBottom: 20 },
  chip: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: colors.ink3,
    borderWidth: 1.5,
    borderColor: colors.line2,
  },
  chipOn: { backgroundColor: colors.greenGlow, borderColor: colors.green },
  chipTxt: { fontSize: 12, fontWeight: "500", color: colors.mid },
  chipTxtOn: { color: colors.green },
  label: {
    fontSize: 10,
    fontWeight: "600",
    letterSpacing: 0.8,
    color: colors.mid,
    marginBottom: 10,
    textTransform: "uppercase",
  },
  method: {
    backgroundColor: colors.greenDim,
    borderWidth: 1.5,
    borderColor: colors.green,
    borderRadius: 14,
    padding: 14,
    marginBottom: 16,
  },
  methodTitle: { fontSize: 13.5, fontWeight: "700", color: colors.text },
  methodSub: { fontSize: 11, color: colors.mid, marginTop: 2 },
});
