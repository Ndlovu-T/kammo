import { StyleSheet, Text, View } from "react-native";
import { statusLabel, statusPillType } from "../utils";
import { colors } from "../theme";

const pillColors = {
  green: { bg: colors.greenDim, text: colors.green },
  amber: { bg: colors.amberDim, text: colors.amber },
  red: { bg: colors.redDim, text: colors.red },
  grey: { bg: colors.bg2, text: colors.mid },
  blue: { bg: colors.blueDim, text: colors.blue },
};

export default function StatusPill({ status, small }) {
  let type = statusPillType(status);
  if (status === "IN_TRANSIT") type = "blue";
  const c = pillColors[type] || pillColors.grey;
  return (
    <View style={[styles.pill, small && styles.small, { backgroundColor: c.bg }]}>
      <Text style={[styles.text, small && styles.smallText, { color: c.text }]}>{statusLabel(status)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  pill: {
    alignSelf: "flex-start",
    paddingHorizontal: 11,
    paddingVertical: 5,
    borderRadius: 999,
  },
  small: { paddingHorizontal: 8, paddingVertical: 3 },
  text: { fontSize: 11, fontWeight: "600" },
  smallText: { fontSize: 10 },
});
