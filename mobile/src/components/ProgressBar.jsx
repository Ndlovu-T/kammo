import { StyleSheet, View } from "react-native";
import { colors } from "../theme";

export default function ProgressBar({ progress = 0 }) {
  return (
    <View style={styles.track}>
      <View style={[styles.fill, { width: `${Math.min(100, Math.max(0, progress))}%` }]} />
    </View>
  );
}

const styles = StyleSheet.create({
  track: {
    height: 3,
    borderRadius: 99,
    backgroundColor: colors.bg3,
    overflow: "hidden",
  },
  fill: {
    height: "100%",
    borderRadius: 99,
    backgroundColor: colors.green,
  },
});
