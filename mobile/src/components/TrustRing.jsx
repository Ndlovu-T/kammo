import { View, Text, StyleSheet } from "react-native";
import Svg, { Circle } from "react-native-svg";
import { colors } from "../theme";

export default function TrustRing({ score = 96, size = 56, stroke = 4 }) {
  const r = (size - stroke) / 2 - 2;
  const cx = size / 2;
  const circ = 2 * Math.PI * r;
  const offset = circ * (1 - Math.min(100, Math.max(0, score)) / 100);

  return (
    <View style={{ width: size, height: size }}>
      <Svg width={size} height={size} style={{ transform: [{ rotate: "-90deg" }] }}>
        <Circle cx={cx} cy={cx} r={r} stroke={colors.ink4} strokeWidth={stroke} fill="none" />
        <Circle
          cx={cx}
          cy={cx}
          r={r}
          stroke={colors.green}
          strokeWidth={stroke}
          fill="none"
          strokeLinecap="round"
          strokeDasharray={`${circ} ${circ}`}
          strokeDashoffset={offset}
        />
      </Svg>
      <View style={styles.center}>
        <Text style={[styles.val, size > 70 && styles.valLg]}>{Math.round(score)}</Text>
        {size > 70 ? <Text style={styles.sub}>/ 100</Text> : null}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  center: {
    ...StyleSheet.absoluteFillObject,
    alignItems: "center",
    justifyContent: "center",
  },
  val: { fontSize: 13, fontWeight: "700", color: colors.text, lineHeight: 15 },
  valLg: { fontSize: 22, lineHeight: 24 },
  sub: { fontSize: 7, color: colors.mid, letterSpacing: 1 },
});
