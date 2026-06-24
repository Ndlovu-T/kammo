import { router } from "expo-router";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { MapPin, Package, PackageSearch, Users } from "lucide-react-native";
import AddressForm from "../../src/components/AddressForm";
import PhoneField from "../../src/components/PhoneField";
import SelectCard from "../../src/components/SelectCard";
import WizardHeader from "../../src/components/WizardHeader";
import { Button, ErrorText, Screen, Subtitle, Title } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { colors } from "../../src/theme";

function LockerRow({ label, locker, onPress }) {
  return (
    <Pressable style={styles.lockerRow} onPress={onPress}>
      <View style={styles.lockerIcon}>
        <MapPin color={locker ? colors.green : colors.mid} size={18} />
      </View>
      <View style={styles.lockerBody}>
        <Text style={styles.lockerLabel}>{label}</Text>
        {locker ? (
          <Text style={styles.lockerName}>{locker.name}</Text>
        ) : (
          <Text style={styles.lockerPlaceholder}>Tap to choose a locker</Text>
        )}
      </View>
      <Text style={styles.lockerAction}>{locker ? "Change" : "Choose"}</Text>
    </Pressable>
  );
}

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
          icon={<PackageSearch color={createDraft.delivery === "locker" ? colors.green : colors.mid} size={18} />}
          title="PUDO Locker"
          subtitle="Drop off and collect from a nearby locker"
          selected={createDraft.delivery === "locker"}
          onPress={() => setCreateDraft((p) => ({ ...p, delivery: "locker" }))}
        />
        <SelectCard
          icon={<Users color={createDraft.delivery === "meetup" ? colors.green : colors.mid} size={18} />}
          title="Meet In Person"
          subtitle="Agree on a safe public location"
          selected={createDraft.delivery === "meetup"}
          onPress={() => setCreateDraft((p) => ({ ...p, delivery: "meetup" }))}
        />
      </View>

      {createDraft.delivery === "courier" ? (
        <>
          <AddressForm
            label="Collection address"
            value={createDraft.collectionAddress}
            onChange={(v) => setCreateDraft((p) => ({ ...p, collectionAddress: v }))}
          />
          <AddressForm
            label="Delivery address"
            value={createDraft.deliveryAddress}
            onChange={(v) => setCreateDraft((p) => ({ ...p, deliveryAddress: v }))}
          />
        </>
      ) : null}

      {createDraft.delivery === "locker" ? (
        <View style={styles.lockerList}>
          <LockerRow
            label="Drop-off locker"
            locker={createDraft.collectionLocker}
            onPress={() => router.push("/create-deal/locker?which=collection")}
          />
          <LockerRow
            label="Pickup locker"
            locker={createDraft.deliveryLocker}
            onPress={() => router.push("/create-deal/locker?which=delivery")}
          />
        </View>
      ) : null}

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
  lockerList: { gap: 8, marginBottom: 8 },
  lockerRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 14,
    padding: 14,
    borderRadius: 16,
    backgroundColor: colors.bg1,
    borderWidth: 1.5,
    borderColor: colors.line2,
  },
  lockerIcon: { flexShrink: 0 },
  lockerBody: { flex: 1 },
  lockerLabel: {
    color: colors.mid,
    fontSize: 10,
    fontWeight: "600",
    letterSpacing: 0.5,
    textTransform: "uppercase",
  },
  lockerName: { color: colors.text, fontSize: 14, fontWeight: "600", marginTop: 2 },
  lockerPlaceholder: { color: colors.dim, fontSize: 13, marginTop: 2 },
  lockerAction: { color: colors.green, fontSize: 12, fontWeight: "700" },
});
