import { router } from "expo-router";
import { StyleSheet, Text, TextInput, View } from "react-native";
import WizardHeader from "../../src/components/WizardHeader";
import { Button, Field, Screen, Subtitle, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { colors } from "../../src/theme";

export default function CreateDealStep1() {
  const { createDraft, setCreateDraft } = useKammo();

  return (
    <Screen bg="bg">
      <WizardHeader title="New Deal" step={1} total={3} progress={33} backTo="/(tabs)" onBack={() => router.back()} />
      <Title style={styles.head}>Deal details</Title>
      <Subtitle style={styles.sub}>Be specific — this protects both parties.</Subtitle>

      <Field
        label="Deal Title"
        placeholder="e.g. iPhone 14 Pro Max 256GB"
        value={createDraft.itemName}
        onChangeText={(v) => setCreateDraft((p) => ({ ...p, itemName: v }))}
      />

      <View style={styles.field}>
        <Text style={styles.label}>Amount (ZAR)</Text>
        <View style={styles.currency}>
          <Text style={styles.cur}>R</Text>
          <TextInput
            style={styles.priceInput}
            placeholder="0.00"
            placeholderTextColor={colors.dim}
            keyboardType="numeric"
            value={String(createDraft.price)}
            onChangeText={(v) => setCreateDraft((p) => ({ ...p, price: v }))}
          />
        </View>
      </View>

      <Field
        label="Description"
        placeholder="Condition, what's included, any relevant details..."
        value={createDraft.description}
        onChangeText={(v) => setCreateDraft((p) => ({ ...p, description: v }))}
        multiline
        numberOfLines={3}
        style={{ minHeight: 90, textAlignVertical: "top" }}
      />

      <Button title="Continue" onPress={() => router.push("/create-deal/details")} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  head: { fontSize: 24, marginTop: 4 },
  sub: { marginBottom: 8 },
  field: { gap: 7 },
  label: {
    color: colors.mid,
    fontSize: 11,
    fontWeight: "600",
    letterSpacing: 0.5,
    textTransform: "uppercase",
  },
  currency: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: colors.surface2,
    borderWidth: 1.5,
    borderColor: colors.line2,
    borderRadius: 16,
  },
  cur: { paddingLeft: 18, color: colors.mid, fontSize: 16, fontWeight: "700" },
  priceInput: {
    flex: 1,
    color: colors.text,
    fontSize: 15,
    paddingHorizontal: 8,
    paddingVertical: 16,
  },
});
