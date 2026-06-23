import { Pressable, StyleSheet, Text, View } from "react-native";
import { Check } from "lucide-react-native";
import { colors } from "../theme";

export default function SelectCard({ icon, title, subtitle, selected, onPress }) {
  return (
    <Pressable style={[styles.card, selected && styles.cardSelected]} onPress={onPress}>
      <View style={styles.icon}>{icon}</View>
      <View style={styles.body}>
        <Text style={[styles.title, selected && styles.titleSelected]}>{title}</Text>
        {subtitle ? <Text style={[styles.sub, selected && styles.subSelected]}>{subtitle}</Text> : null}
      </View>
      {selected ? (
        <View style={styles.chk}>
          <Check color={colors.green} size={12} />
        </View>
      ) : (
        <View style={styles.radio} />
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    alignItems: "center",
    gap: 14,
    padding: 14,
    borderRadius: 16,
    backgroundColor: colors.bg1,
    borderWidth: 1.5,
    borderColor: colors.line2,
  },
  cardSelected: {
    backgroundColor: colors.greenDim,
    borderColor: colors.green,
  },
  icon: { flexShrink: 0 },
  body: { flex: 1 },
  title: { color: colors.text, fontSize: 14, fontWeight: "600" },
  titleSelected: { color: colors.green },
  sub: { color: colors.dim, fontSize: 12, marginTop: 2 },
  subSelected: { color: "rgba(0,138,68,0.7)" },
  radio: {
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: colors.dim,
  },
  chk: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: colors.green,
    alignItems: "center",
    justifyContent: "center",
  },
});
