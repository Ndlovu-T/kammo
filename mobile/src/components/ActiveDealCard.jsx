import { Pressable, StyleSheet, Text, View } from "react-native";
import { Clock } from "lucide-react-native";
import StatusPill from "./StatusPill";
import { formatRand } from "../utils";
import { colors } from "../theme";

const STEPS = ["Created", "Paid", "Shipped", "Delivered", "Inspect"];

// Tracker step comes from the backend's DealStatus -> TrackerStep mapping
// (see backend DealStatus.toTrackerStep()).
const TRACKER_STEP_ORDER = ["INITIATED", "PAID", "TRANSIT", "INSPECT", "RELEASED"];

function stepIndex(trackerStep) {
  const idx = TRACKER_STEP_ORDER.indexOf(trackerStep);
  return idx === -1 ? 0 : idx + 1;
}

function ProgressSteps({ trackerStep, status }) {
  const current = stepIndex(trackerStep);
  const labels = status === "AWAITING_BUYER_PAYMENT" ? ["Created", "Pay", "Ship", "Transit", "Done"] : STEPS;

  return (
    <View style={styles.steps}>
      {labels.map((lbl, i) => {
        const done = i < current;
        const now = i === current;
        return (
          <View key={lbl} style={styles.stepGroup}>
            {i > 0 ? <View style={[styles.line, done && styles.lineDone, i === current && styles.linePart]} /> : null}
            <View style={styles.stepCol}>
              <View style={[styles.dot, done && styles.dotDone, now && styles.dotNow]} />
              <Text style={[styles.stepLbl, done && styles.stepLblDone, now && styles.stepLblNow]}>{lbl}</Text>
            </View>
          </View>
        );
      })}
    </View>
  );
}

export default function ActiveDealCard({ deal, onPress, subtitle }) {
  const ref = deal.dealReference || `KM-${deal.dealCode}`;
  const inspecting = ["DELIVERED", "IN_TRANSIT"].includes(deal.status);

  return (
    <Pressable style={styles.card} onPress={onPress}>
      <View style={styles.head}>
        <View style={styles.left}>
          <View style={styles.icon}>
            <Text style={styles.iconTxt}>{deal.itemName?.slice(0, 1) || "?"}</Text>
          </View>
          <View>
            <Text style={styles.ref}>{ref}</Text>
            <Text style={styles.item}>{deal.itemName}</Text>
            {subtitle ? <Text style={styles.sub}>{subtitle}</Text> : null}
          </View>
        </View>
        <View style={styles.right}>
          <Text style={styles.price}>{formatRand(deal.price)}</Text>
          <StatusPill status={deal.status} small />
        </View>
      </View>
      <ProgressSteps trackerStep={deal.trackerStep} status={deal.status} />
      {inspecting ? (
        <View style={styles.timer}>
          <Clock color={colors.amber} size={14} />
          <Text style={styles.timerLbl}>Auto-release in</Text>
          <Text style={styles.timerVal}>48h window</Text>
        </View>
      ) : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.ink3,
    borderWidth: 1,
    borderColor: colors.line2,
    borderRadius: 18,
    padding: 15,
    gap: 12,
  },
  head: { flexDirection: "row", justifyContent: "space-between", alignItems: "flex-start" },
  left: { flexDirection: "row", gap: 10, flex: 1 },
  icon: {
    width: 40,
    height: 40,
    borderRadius: 11,
    backgroundColor: colors.ink4,
    alignItems: "center",
    justifyContent: "center",
  },
  iconTxt: { color: colors.green, fontSize: 16, fontWeight: "800" },
  ref: { fontSize: 9, color: colors.green, letterSpacing: 0.8, marginBottom: 2 },
  item: { fontSize: 14, fontWeight: "800", color: colors.text, letterSpacing: -0.3 },
  sub: { fontSize: 11, color: colors.mid, marginTop: 2 },
  right: { alignItems: "flex-end", gap: 4 },
  price: { fontSize: 16, fontWeight: "800", color: colors.green },
  steps: { flexDirection: "row", alignItems: "flex-start" },
  stepGroup: { flex: 1, flexDirection: "row", alignItems: "flex-start" },
  stepCol: { flex: 1, alignItems: "center", gap: 4 },
  dot: { width: 8, height: 8, borderRadius: 4, backgroundColor: colors.line },
  dotDone: { backgroundColor: colors.green },
  dotNow: {
    backgroundColor: colors.ink,
    borderWidth: 2,
    borderColor: colors.green,
  },
  stepLbl: { fontSize: 8, color: colors.mid, textAlign: "center" },
  stepLblDone: { color: colors.green },
  stepLblNow: { color: colors.green },
  line: { position: "absolute", left: -4, top: 3, right: "50%", height: 2, backgroundColor: colors.line, marginBottom: 14 },
  lineDone: { backgroundColor: colors.green },
  linePart: { backgroundColor: colors.green, opacity: 0.5 },
  timer: {
    flexDirection: "row",
    alignItems: "center",
    gap: 7,
    padding: 9,
    backgroundColor: colors.amberDim,
    borderWidth: 1,
    borderColor: "rgba(255,167,38,0.2)",
    borderRadius: 11,
  },
  timerLbl: { flex: 1, fontSize: 11, color: colors.mid },
  timerVal: { fontSize: 13, fontWeight: "800", color: colors.amber },
});
