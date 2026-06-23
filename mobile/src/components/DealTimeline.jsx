import { StyleSheet, Text, View } from "react-native";
import { Check } from "lucide-react-native";
import { colors } from "../theme";

const STEPS = [
  { key: "CREATED", title: "Deal created", desc: "Link shared with other party" },
  { key: "PAYMENT", title: "Payment secured", desc: "Funds held by payment partner" },
  { key: "TRANSIT", title: "In transit", desc: "Item shipped or ready for collection" },
  { key: "DELIVERED", title: "Delivered", desc: "Buyer confirms receipt" },
  { key: "COMPLETED", title: "Completed", desc: "Funds released to seller" },
];

function stepState(status, index) {
  const order = ["CREATED", "AWAITING_BUYER_PAYMENT", "BUYER_ACCEPTED", "SELLER_ACCEPTED", "PAYMENT_SECURED", "AWAITING_COLLECTION", "IN_TRANSIT", "DELIVERED", "COMPLETED"];
  const idx = order.indexOf(status);
  const thresholds = [0, 2, 5, 7, 8];
  const t = thresholds[index];
  if (idx >= t + 1 || status === "COMPLETED") return "done";
  if (idx >= t) return "live";
  return "wait";
}

export default function DealTimeline({ status }) {
  return (
    <View style={styles.wrap}>
      {STEPS.map((step, i) => {
        const state = stepState(status, i);
        const isLast = i === STEPS.length - 1;
        return (
          <View key={step.key} style={styles.step}>
            <View style={styles.left}>
              <View style={[styles.dot, state === "done" && styles.dotDone, state === "live" && styles.dotLive]}>
                {state === "done" ? <Check color="#000" size={12} /> : <Text style={styles.dotNum}>{i + 1}</Text>}
              </View>
              {!isLast ? (
                <View style={[styles.line, state === "done" && styles.lineDone]} />
              ) : null}
            </View>
            <View style={styles.body}>
              <Text style={styles.title}>{step.title}</Text>
              <Text style={styles.desc}>{step.desc}</Text>
            </View>
          </View>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { gap: 0 },
  step: { flexDirection: "row", gap: 14 },
  left: { alignItems: "center", width: 28 },
  dot: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: colors.surface2,
    borderWidth: 2,
    borderColor: colors.border,
    alignItems: "center",
    justifyContent: "center",
  },
  dotDone: { backgroundColor: colors.green, borderColor: colors.green },
  dotLive: { borderColor: colors.green, backgroundColor: colors.surface3 },
  dotNum: { color: colors.grey2, fontSize: 11, fontWeight: "700" },
  line: { width: 2, flex: 1, minHeight: 28, marginVertical: 4, backgroundColor: colors.border },
  lineDone: { backgroundColor: colors.green },
  body: { flex: 1, paddingBottom: 20 },
  title: { color: colors.white, fontSize: 14, fontWeight: "700" },
  desc: { color: colors.grey, fontSize: 12, marginTop: 4, lineHeight: 17 },
});
