import { Pressable, StyleSheet, Text, View } from "react-native";
import { ChevronRight } from "lucide-react-native";
import { colors } from "../theme";

export default function MenuRow({ icon, label, right, red, onPress }) {
  return (
    <Pressable style={styles.row} onPress={onPress}>
      <View style={styles.ico}>{icon}</View>
      <Text style={[styles.txt, red && styles.red]}>{label}</Text>
      <View style={styles.right}>{right ?? <ChevronRight color={red ? colors.red : colors.grey2} size={18} />}</View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    gap: 14,
    paddingVertical: 16,
    paddingHorizontal: 4,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  ico: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: colors.surface2,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: "center",
    justifyContent: "center",
  },
  txt: { flex: 1, color: colors.white, fontSize: 14, fontWeight: "500" },
  red: { color: colors.red },
  right: { marginLeft: "auto" },
});
