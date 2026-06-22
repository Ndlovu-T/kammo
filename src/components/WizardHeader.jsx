import { router } from "expo-router";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { X } from "lucide-react-native";
import ProgressBar from "./ProgressBar";
import { colors } from "../theme";

export default function WizardHeader({ title, step, total = 3, progress, backTo, onBack }) {
  return (
    <View style={styles.wrap}>
      <View style={styles.row}>
        <Pressable
          style={styles.back}
          onPress={() => (onBack ? onBack() : backTo ? router.replace(backTo) : router.back())}
        >
          <X color={colors.mid} size={20} />
        </Pressable>
        <View style={styles.titles}>
          <Text style={styles.title}>{title}</Text>
          {step ? <Text style={styles.step}>Step {step} of {total}</Text> : null}
        </View>
      </View>
      {progress != null ? (
        <View style={styles.bar}>
          <ProgressBar progress={progress} />
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { marginBottom: 8, gap: 12 },
  row: { flexDirection: "row", alignItems: "center", gap: 12 },
  back: {
    width: 44,
    height: 44,
    borderRadius: 13,
    backgroundColor: colors.bg1,
    borderWidth: 1.5,
    borderColor: colors.line,
    alignItems: "center",
    justifyContent: "center",
  },
  titles: { flex: 1 },
  title: { color: colors.text, fontSize: 18, fontWeight: "700", letterSpacing: -0.4 },
  step: { color: colors.mid, fontSize: 11, marginTop: 1 },
  bar: { paddingHorizontal: 2 },
});
