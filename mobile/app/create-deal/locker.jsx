import { router, useLocalSearchParams } from "expo-router";
import { useMemo, useState } from "react";
import { FlatList, StyleSheet, Text, TextInput, View } from "react-native";
import { MapPin, Search } from "lucide-react-native";
import SelectCard from "../../src/components/SelectCard";
import { Screen, Subtitle, Title } from "../../src/components/ui";
import WizardHeader from "../../src/components/WizardHeader";
import { useKammo } from "../../src/KammoContext";
import { searchLockers } from "../../src/data/lockers";
import { colors } from "../../src/theme";

export default function LockerPickerScreen() {
  const { which = "collection" } = useLocalSearchParams();
  const { createDraft, setCreateDraft } = useKammo();
  const [query, setQuery] = useState("");

  const lockers = useMemo(() => searchLockers(query), [query]);
  const draftKey = which === "delivery" ? "deliveryLocker" : "collectionLocker";
  const selectedId = createDraft[draftKey]?.id;

  function choose(locker) {
    setCreateDraft((p) => ({ ...p, [draftKey]: locker }));
    router.back();
  }

  return (
    <Screen bg="bg" scroll={false}>
      <WizardHeader
        title={which === "delivery" ? "Pickup Locker" : "Drop-off Locker"}
        progress={null}
        onBack={() => router.back()}
      />
      <Title style={styles.head}>Choose a locker</Title>
      <Subtitle style={styles.sub}>
        {which === "delivery"
          ? "Where the buyer will collect the item."
          : "Where the seller will drop the item off."}
      </Subtitle>

      <View style={styles.search}>
        <Search color={colors.dim} size={16} />
        <TextInput
          style={styles.searchInput}
          placeholder="Search by mall, suburb or city"
          placeholderTextColor={colors.dim}
          value={query}
          onChangeText={setQuery}
        />
      </View>

      <FlatList
        data={lockers}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <SelectCard
            icon={<MapPin color={selectedId === item.id ? colors.green : colors.mid} size={18} />}
            title={item.name}
            subtitle={`${item.network} · ${item.line1}, ${item.city}`}
            selected={selectedId === item.id}
            onPress={() => choose(item)}
          />
        )}
        ListEmptyComponent={<Text style={styles.empty}>No lockers match your search.</Text>}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  head: { fontSize: 22, marginTop: 4 },
  sub: { marginBottom: 12 },
  search: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    backgroundColor: colors.ink3,
    borderWidth: 1,
    borderColor: colors.line2,
    borderRadius: 12,
    paddingHorizontal: 14,
    marginBottom: 12,
  },
  searchInput: { flex: 1, color: colors.text, fontSize: 14, paddingVertical: 12 },
  list: { gap: 8, paddingBottom: 40 },
  empty: { color: colors.mid, textAlign: "center", marginTop: 24 },
});
