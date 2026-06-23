import { Pressable, StyleSheet, View } from "react-native";
import { Star } from "lucide-react-native";
import { colors } from "../theme";

export default function StarRating({ value, onChange, size = 34 }) {
  return (
    <View style={styles.row}>
      {[1, 2, 3, 4, 5].map((n) => (
        <Pressable key={n} onPress={() => onChange(n)} hitSlop={8}>
          <Star
            size={size}
            color={value >= n ? colors.amber : colors.grey2}
            fill={value >= n ? colors.amber : "transparent"}
            style={{ opacity: value >= n ? 1 : 0.35 }}
          />
        </Pressable>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: "row", justifyContent: "center", gap: 8 },
});
