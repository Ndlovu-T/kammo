import { Pressable, StyleSheet, Text, View } from "react-native";
import { RefreshCw } from "lucide-react-native";
import { apiBaseUrl } from "../api";
import { colors } from "../theme";

export default function BackendBanner({ ok, error, onRetry }) {
  if (ok !== false && !error) return null;

  return (
    <Pressable style={styles.banner} onPress={onRetry}>
      <View style={styles.row}>
        <RefreshCw color={colors.amber} size={14} />
        <View style={styles.body}>
          <Text style={styles.title}>
            {ok === false ? "Backend offline" : "Sync issue"}
          </Text>
          <Text style={styles.sub} numberOfLines={2}>
            {error || `Cannot reach ${apiBaseUrl}`}
          </Text>
        </View>
        <Text style={styles.retry}>Retry</Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  banner: {
    backgroundColor: "rgba(245,158,11,0.12)",
    borderWidth: 1,
    borderColor: "rgba(245,158,11,0.25)",
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
  },
  row: { flexDirection: "row", alignItems: "center", gap: 10 },
  body: { flex: 1 },
  title: { color: colors.amber, fontSize: 13, fontWeight: "700" },
  sub: { color: colors.grey, fontSize: 11, marginTop: 2 },
  retry: { color: colors.amber, fontSize: 12, fontWeight: "700" },
});
