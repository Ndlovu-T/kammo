import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { colors, spacing } from "../theme";

export function Screen({ children, scroll = true, style, refreshControl, bg = "bg" }) {
  const content = scroll ? (
    <ScrollView
      contentContainerStyle={[styles.scroll, style]}
      keyboardShouldPersistTaps="handled"
      refreshControl={refreshControl}
    >
      {children}
    </ScrollView>
  ) : (
    <View style={[styles.body, style]}>{children}</View>
  );

  return (
    <SafeAreaView style={[styles.safe, bg === "bg1" ? styles.bg1 : styles.bg]} edges={["top", "left", "right"]}>
      {content}
    </SafeAreaView>
  );
}

export function Title({ children, style }) {
  return <Text style={[styles.title, style]}>{children}</Text>;
}

export function Subtitle({ children, style }) {
  return <Text style={[styles.subtitle, style]}>{children}</Text>;
}

export function Card({ children, onPress, style }) {
  const Wrapper = onPress ? Pressable : View;
  return (
    <Wrapper style={[styles.card, style]} onPress={onPress}>
      {children}
    </Wrapper>
  );
}

export function Field({ label, ...props }) {
  return (
    <View style={styles.field}>
      {label ? <Text style={styles.label}>{label}</Text> : null}
      <TextInput placeholderTextColor={colors.dim} style={styles.input} {...props} />
    </View>
  );
}

export function Button({ title, onPress, disabled, variant = "green", loading }) {
  const isGreen = variant === "green";
  return (
    <Pressable
      style={[
        styles.button,
        variant === "ghost" && styles.buttonGhost,
        variant === "outline" && styles.buttonOutline,
        variant === "red" && styles.buttonRed,
        (disabled || loading) && styles.buttonDisabled,
      ]}
      onPress={onPress}
      disabled={disabled || loading}
    >
      {loading ? (
        <ActivityIndicator color={isGreen ? colors.ink : colors.text} />
      ) : (
        <Text
          style={[
            styles.buttonText,
            variant === "outline" && styles.buttonTextOutline,
            variant === "ghost" && styles.buttonTextGhost,
            variant === "red" && styles.buttonTextRed,
          ]}
        >
          {title}
        </Text>
      )}
    </Pressable>
  );
}

export function ErrorText({ children }) {
  if (!children) return null;
  return <Text style={styles.error}>{children}</Text>;
}

export function SectionHeader({ title, action, onAction, accent }) {
  return (
    <View style={styles.sec}>
      <Text style={[styles.secTitle, accent && { color: accent }]}>{title}</Text>
      {action ? (
        <Pressable onPress={onAction}>
          <Text style={styles.secMore}>{action}</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

export function InfoBox({ title, children, danger }) {
  return (
    <View style={[styles.ibox, danger && styles.iboxDanger]}>
      {title ? <Text style={[styles.iboxTitle, danger && styles.iboxTitleDanger]}>{title}</Text> : null}
      <Text style={styles.iboxBody}>{children}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1 },
  bg: { backgroundColor: colors.bg },
  bg1: { backgroundColor: colors.ink2 },
  body: { flex: 1, padding: spacing.lg },
  scroll: { padding: 16, paddingBottom: 100, gap: spacing.md },
  title: {
    color: colors.text,
    fontSize: 19,
    fontWeight: "800",
    letterSpacing: -0.5,
    marginBottom: 4,
  },
  subtitle: { color: colors.mid, fontSize: 12.5, lineHeight: 20 },
  card: {
    backgroundColor: colors.ink3,
    borderColor: colors.line2,
    borderWidth: 1,
    borderRadius: 18,
    padding: spacing.md,
    gap: 8,
  },
  field: { gap: 5, marginBottom: 13 },
  label: {
    color: colors.mid,
    fontSize: 10,
    fontWeight: "600",
    letterSpacing: 0.8,
    textTransform: "uppercase",
  },
  input: {
    backgroundColor: colors.ink3,
    borderColor: colors.line2,
    borderWidth: 1,
    borderRadius: 12,
    color: colors.text,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 14,
  },
  button: {
    backgroundColor: colors.green,
    borderRadius: 13,
    paddingVertical: 14,
    alignItems: "center",
    shadowColor: colors.green,
    shadowOpacity: 0.25,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 4 },
    elevation: 3,
  },
  buttonGhost: {
    backgroundColor: "transparent",
    borderWidth: 1.5,
    borderColor: colors.line2,
    shadowOpacity: 0,
    elevation: 0,
  },
  buttonOutline: {
    backgroundColor: "transparent",
    borderWidth: 1.5,
    borderColor: colors.line2,
    shadowOpacity: 0,
    elevation: 0,
  },
  buttonRed: {
    backgroundColor: colors.redDim,
    borderWidth: 1.5,
    borderColor: "rgba(255,77,94,0.25)",
    shadowOpacity: 0,
    elevation: 0,
  },
  buttonDisabled: { opacity: 0.6 },
  buttonText: { color: colors.ink, fontSize: 14, fontWeight: "700", letterSpacing: -0.2 },
  buttonTextGhost: { color: colors.text },
  buttonTextOutline: { color: colors.text },
  buttonTextRed: { color: colors.red },
  error: { color: colors.red, fontSize: 13, fontWeight: "600" },
  sec: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 2,
    paddingTop: 18,
    paddingBottom: 8,
  },
  secTitle: {
    fontSize: 10,
    fontWeight: "600",
    color: colors.mid,
    letterSpacing: 1,
    textTransform: "uppercase",
  },
  secMore: { fontSize: 12, fontWeight: "600", color: colors.green },
  ibox: {
    backgroundColor: colors.ink4,
    borderWidth: 1,
    borderColor: colors.line2,
    borderLeftWidth: 3,
    borderLeftColor: colors.green,
    borderRadius: 10,
    padding: 11,
  },
  iboxDanger: { borderLeftColor: colors.red },
  iboxTitle: { fontSize: 12, fontWeight: "700", color: colors.green, marginBottom: 4 },
  iboxTitleDanger: { color: colors.red },
  iboxBody: { fontSize: 12, color: colors.mid, lineHeight: 19 },
});
