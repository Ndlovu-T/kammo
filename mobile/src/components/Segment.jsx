import { Pressable, StyleSheet, Text, View } from "react-native";
import { colors } from "../theme";

export default function Segment({ options, value, onChange }) {
  return (
    <View style={styles.seg}>
      {options.map((opt) => {
        const on = value === opt.id;
        return (
          <Pressable key={opt.id} style={[styles.btn, on && styles.btnOn]} onPress={() => onChange(opt.id)}>
            <Text style={[styles.lbl, on && styles.lblOn]}>{opt.label}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  seg: {
    flexDirection: "row",
    padding: 4,
    borderRadius: 14,
    backgroundColor: colors.bg2,
    gap: 4,
  },
  btn: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 10,
    alignItems: "center",
  },
  btnOn: {
    backgroundColor: colors.bg,
    shadowColor: "#000",
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 1,
  },
  lbl: { fontSize: 13, fontWeight: "600", color: colors.dim },
  lblOn: { color: colors.text },
});
