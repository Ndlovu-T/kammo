import { router } from "expo-router";
import { StyleSheet, Text, View } from "react-native";
import { Package, Users } from "lucide-react-native";
import PhoneField from "../../src/components/PhoneField";
import SelectCard from "../../src/components/SelectCard";
import WizardHeader from "../../src/components/WizardHeader";
import { Button, ErrorText, Screen, Subtitle, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { colors } from "../../src/theme";

export default function CreateDealStep2() {
  const { createDraft, setCreateDraft, submitCreateDeal, busy, error } = useKammo();

  async function onCreate() {
    try {
      await submitCreateDeal();
      router.replace("/create-deal/success");
    } catch {}
  }

  return (
    <Screen bg="bg">
      <WizardHeader title="New Deal" step={2} total={3} progress={66} />
      <Title style={styles.head}>Delivery &amp; counterparty</Title>
      <Subtitle style={styles.sub}>How will the item be transferred?</Subtitle>

      <Text style={styles.sectionLabel}>Delivery Method</Text>
      <View style={styles.list}>
        <SelectCard
          icon={<Package color={createDraft.delivery === "courier" ? colors.green : colors.mid} size={18} />}
          title="Courier"
          subtitle="KAMMO generates the waybill"
          selected={createDraft.delivery === "courier"}
          onPress={() => setCreateDraft((p) => ({ ...p, delivery: "courier" }))}
        />
        <SelectCard
          icon={<Users color={createDraft.delivery === "meetup" ? colors.green : colors.mid} size={18} />}
          title="Meet In Person"
          subtitle="Agree on a safe public location"
          selected={createDraft.delivery === "meetup"}
          onPress={() => setCreateDraft((p) => ({ ...p, delivery: "meetup" }))}
        />
      </View>

      <PhoneField
        label="Other Party's Phone Number"
        value={createDraft.otherPhone}
        onChangeText={(v) => setCreateDraft((p) => ({ ...p, otherPhone: v }))}
      />
      <Text style={styles.hint}>They'll receive a secure deal link via SMS. No app required.</Text>

      <ErrorText>{error}</ErrorText>
      <Button title="Create Deal" onPress={onCreate} loading={busy} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  head: { fontSize: 24, marginTop: 4 },
  sub: { marginBottom: 8 },
  sectionLabel: {
    color: colors.mid,
    fontSize: 11,
    fontWeight: "600",
    letterSpacing: 0.5,
    textTransform: "uppercase",
    marginTop: 4,
  },
  list: { gap: 8, marginVertical: 8 },
  hint: { color: colors.mid, fontSize: 12, marginTop: -4, marginBottom: 8 },
});
