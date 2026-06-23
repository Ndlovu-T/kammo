import { router } from "expo-router";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { ArrowRight, CreditCard, Package, Shield, Star, Truck } from "lucide-react-native";
import { Screen } from "../../src/components/ui";
import { colors } from "../../src/theme";

const UPDATES = [
  {
    icon: Truck,
    title: "Deal in transit",
    body: "Your item is on the way. Track progress in the deal view.",
    time: "Now",
    unread: true,
  },
  {
    icon: CreditCard,
    title: "Payment secured",
    body: "Funds are locked until delivery is confirmed.",
    time: "1h",
    unread: true,
  },
  {
    icon: Shield,
    title: "Identity verified",
    body: "Your KAMMO ID is active. Trust score updated.",
    time: "Mon",
    unread: false,
  },
  {
    icon: Star,
    title: "New rating received",
    body: "A buyer left feedback on your last deal.",
    time: "Sun",
    unread: false,
  },
  {
    icon: Package,
    title: "Funds released",
    body: "Completed deal payout is now in your wallet.",
    time: "Sat",
    unread: false,
  },
];

export default function UpdatesTab() {
  return (
    <Screen style={styles.screen}>
      <View style={styles.head}>
        <Text style={styles.title}>Updates</Text>
        <Pressable>
          <Text style={styles.markAll}>Mark all read</Text>
        </Pressable>
      </View>

      {UPDATES.map((item, i) => {
        const Icon = item.icon;
        return (
          <Pressable
            key={item.title}
            style={[styles.row, item.unread && styles.rowUnread]}
            onPress={() => router.push("/(tabs)")}
          >
            <View style={styles.icon}>
              <Icon color={colors.green} size={18} />
            </View>
            <View style={styles.body}>
              <Text style={styles.rowTitle}>{item.title}</Text>
              <Text style={styles.rowBody}>{item.body}</Text>
            </View>
            <View style={styles.right}>
              <Text style={styles.time}>{item.time}</Text>
              {item.unread ? <View style={styles.unreadDot} /> : null}
            </View>
          </Pressable>
        );
      })}
    </Screen>
  );
}

const styles = StyleSheet.create({
  screen: { paddingTop: 8, paddingHorizontal: 0 },
  head: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 16,
    paddingBottom: 8,
  },
  title: { fontSize: 17, fontWeight: "700", color: colors.text },
  markAll: { fontSize: 12, fontWeight: "600", color: colors.green },
  row: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: 11,
    paddingVertical: 13,
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
  },
  rowUnread: { backgroundColor: "rgba(0,232,122,0.02)" },
  icon: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: colors.greenDim,
    alignItems: "center",
    justifyContent: "center",
  },
  body: { flex: 1 },
  rowTitle: { fontSize: 13.5, fontWeight: "700", color: colors.text },
  rowBody: { fontSize: 12, color: colors.mid, marginTop: 2, lineHeight: 17 },
  right: { alignItems: "flex-end", gap: 5 },
  time: { fontSize: 10.5, color: colors.mid },
  unreadDot: { width: 8, height: 8, borderRadius: 4, backgroundColor: colors.green },
});
