import { StyleSheet, Text, TextInput, View } from "react-native";
import { colors } from "../theme";

export default function PhoneField({ label, value, onChangeText, ...props }) {
  return (
    <View style={styles.field}>
      {label ? <Text style={styles.label}>{label}</Text> : null}
      <View style={styles.row}>
        <View style={styles.prefix}>
          <Text style={styles.prefixText}>+27</Text>
        </View>
        <TextInput
          style={styles.input}
          placeholderTextColor={colors.dim}
          value={value}
          onChangeText={onChangeText}
          keyboardType="phone-pad"
          placeholder="82 000 0000"
          {...props}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  field: { gap: 7 },
  label: {
    color: colors.mid,
    fontSize: 11,
    fontWeight: "600",
    letterSpacing: 0.5,
    textTransform: "uppercase",
  },
  row: { flexDirection: "row" },
  prefix: {
    backgroundColor: colors.ink3,
    borderWidth: 1,
    borderColor: colors.line2,
    borderRightWidth: 0,
    borderTopLeftRadius: 12,
    borderBottomLeftRadius: 12,
    paddingHorizontal: 14,
    justifyContent: "center",
  },
  prefixText: { color: colors.mid, fontWeight: "600", fontSize: 14 },
  input: {
    flex: 1,
    backgroundColor: colors.ink3,
    borderWidth: 1,
    borderColor: colors.line2,
    borderTopRightRadius: 12,
    borderBottomRightRadius: 12,
    color: colors.text,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 14,
  },
});
