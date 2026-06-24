import { router } from "expo-router";
import { useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { ArrowLeft } from "lucide-react-native";
import { Button, ErrorText, Field, InfoBox, Screen, Subtitle, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { colors } from "../../src/theme";

export default function BankAccountScreen() {
  const { bankAccount, saveBankAccount, busy, error } = useKammo();
  const [bankCode, setBankCode] = useState(bankAccount?.bankCode || "");
  const [bankAccountNumber, setBankAccountNumber] = useState("");
  const [bankAccountName, setBankAccountName] = useState(bankAccount?.bankAccountName || "");
  const [localError, setLocalError] = useState("");

  async function onSave() {
    setLocalError("");
    if (!/^\d{3,10}$/.test(bankCode.trim())) {
      setLocalError("Bank code must be 3-10 digits (the numeric Paystack bank code)");
      return;
    }
    if (!/^\d{5,20}$/.test(bankAccountNumber.trim())) {
      setLocalError("Account number must be numeric");
      return;
    }
    if (!bankAccountName.trim()) {
      setLocalError("Account holder name is required");
      return;
    }
    try {
      await saveBankAccount({ bankCode, bankAccountNumber, bankAccountName });
      router.back();
    } catch {}
  }

  return (
    <Screen bg="bg1">
      <View style={styles.topbar}>
        <Pressable style={styles.back} onPress={() => router.back()}>
          <ArrowLeft color={colors.green} size={22} />
        </Pressable>
        <Text style={styles.topTitle}>Bank Account</Text>
      </View>

      <Title>{bankAccount?.configured ? "Update your bank account" : "Add your bank account"}</Title>
      <Subtitle>Used for withdrawing your KAMMO Wallet balance.</Subtitle>

      {bankAccount?.configured ? (
        <View style={styles.current}>
          <Text style={styles.currentTitle}>{bankAccount.bankAccountName}</Text>
          <Text style={styles.currentSub}>
            Bank code {bankAccount.bankCode} · {bankAccount.maskedAccountNumber}
          </Text>
        </View>
      ) : null}

      <Field
        label="Bank code"
        placeholder="e.g. 632005"
        value={bankCode}
        onChangeText={setBankCode}
        keyboardType="numeric"
      />
      <Field
        label="Account number"
        placeholder="Account number"
        value={bankAccountNumber}
        onChangeText={setBankAccountNumber}
        keyboardType="numeric"
      />
      <Field
        label="Account holder name"
        placeholder="Full name on the account"
        value={bankAccountName}
        onChangeText={setBankAccountName}
      />

      <InfoBox title="Why we ask">
        The bank code is the numeric code your bank uses with our payment partner (Paystack). Check your
        bank's website or app if you're not sure.
      </InfoBox>

      <ErrorText>{localError || error}</ErrorText>
      <Button title="Save bank account" onPress={onSave} loading={busy} />
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
  current: {
    backgroundColor: colors.greenDim,
    borderWidth: 1.5,
    borderColor: colors.green,
    borderRadius: 14,
    padding: 14,
    marginBottom: 12,
  },
  currentTitle: { fontSize: 13.5, fontWeight: "700", color: colors.text },
  currentSub: { fontSize: 11, color: colors.mid, marginTop: 2 },
});
