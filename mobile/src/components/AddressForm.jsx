import { StyleSheet, Text, View } from "react-native";
import PhoneField from "./PhoneField";
import { Field } from "./ui";
import { colors } from "../theme";

export default function AddressForm({ label, value, onChange }) {
  function set(key, v) {
    onChange({ ...value, [key]: v });
  }

  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>{label}</Text>
      <Field
        placeholder="Street address"
        value={value.line1}
        onChangeText={(v) => set("line1", v)}
      />
      <Field
        placeholder="Apartment, suite, unit (optional)"
        value={value.line2}
        onChangeText={(v) => set("line2", v)}
      />
      <View style={styles.row}>
        <View style={styles.half}>
          <Field placeholder="City" value={value.city} onChangeText={(v) => set("city", v)} />
        </View>
        <View style={styles.half}>
          <Field placeholder="Province" value={value.province} onChangeText={(v) => set("province", v)} />
        </View>
      </View>
      <Field
        placeholder="Postal code"
        value={value.postalCode}
        onChangeText={(v) => set("postalCode", v)}
        keyboardType="numeric"
      />
      <Field
        placeholder="Contact name"
        value={value.contactName}
        onChangeText={(v) => set("contactName", v)}
      />
      <PhoneField value={value.contactPhone} onChangeText={(v) => set("contactPhone", v)} />
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { gap: 8, marginBottom: 8 },
  label: {
    color: colors.mid,
    fontSize: 11,
    fontWeight: "600",
    letterSpacing: 0.5,
    textTransform: "uppercase",
    marginBottom: 2,
  },
  row: { flexDirection: "row", gap: 10 },
  half: { flex: 1 },
});
