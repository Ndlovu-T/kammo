import { StyleSheet, Text, View } from "react-native";
import { Card } from "./ui";
import StatusPill from "./StatusPill";
import { formatRand, timeAgo } from "../utils";
import { colors } from "../theme";

export default function DealCard({ deal, onPress, subtitle }) {
  return (
    <Card onPress={onPress}>
      <Text style={styles.name}>{deal.itemName}</Text>
      <View style={styles.row}>
        <Text style={styles.ref}>{deal.dealReference || deal.dealCode}</Text>
        {subtitle ? <Text style={styles.sub}>{subtitle}</Text> : null}
      </View>
      <View style={styles.footer}>
        <StatusPill status={deal.status} small />
        <View style={styles.right}>
          <Text style={styles.price}>{formatRand(deal.price)}</Text>
          <Text style={styles.time}>{timeAgo(deal.updatedAt || deal.createdAt)}</Text>
        </View>
      </View>
    </Card>
  );
}

const styles = StyleSheet.create({
  name: { color: colors.white, fontSize: 15, fontWeight: "700" },
  row: { flexDirection: "row", alignItems: "center", gap: 8, marginTop: 4 },
  ref: { color: colors.green, fontSize: 11, fontFamily: "monospace" },
  sub: { color: colors.grey, fontSize: 11 },
  footer: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-end",
    marginTop: 10,
  },
  right: { alignItems: "flex-end" },
  price: { color: colors.white, fontSize: 16, fontWeight: "800" },
  time: { color: colors.grey, fontSize: 10, marginTop: 2 },
});
