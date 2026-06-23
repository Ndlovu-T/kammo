import { Redirect, router } from "expo-router";
import { useEffect } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { ArrowRight, CreditCard, Shield, Truck } from "lucide-react-native";
import { Button, Screen } from "../src/components/ui";
import { useKammo } from "../src/KammoContext";
import { colors } from "../src/theme";

const PILLARS = [
  { icon: CreditCard, label: "ID\nVerified" },
  { icon: Shield, label: "Funds\nProtected" },
  { icon: Truck, label: "Courier\nSecured" },
];

export default function WelcomeScreen() {
  const { ready, token } = useKammo();

  useEffect(() => {
    if (ready && token) router.replace("/(tabs)");
  }, [ready, token]);

  if (!ready) {
    return (
      <Screen scroll={false} style={styles.center}>
        <Text style={styles.logo}>
          K<Text style={styles.logoEm}>AMM</Text>O<Text style={styles.logoDot}>.</Text>
        </Text>
      </Screen>
    );
  }

  if (token) return <Redirect href="/(tabs)" />;

  return (
    <Screen scroll={false} style={styles.wrap}>
      <View style={styles.glow1} />
      <View style={styles.glow2} />

      <View style={styles.top}>
        <Text style={styles.logo}>
          K<Text style={styles.logoEm}>AMM</Text>O<Text style={styles.logoDot}>.</Text>
        </Text>
        <Pressable onPress={() => router.push("/login")}>
          <Text style={styles.skip}>Skip</Text>
        </Pressable>
      </View>

      <View style={styles.body}>
        <View style={styles.badge}>
          <View style={styles.badgeDot} />
          <Text style={styles.badgeTxt}>DEAL PROTECTION PLATFORM</Text>
        </View>

        <Text style={styles.headline}>
          Trust <Text style={styles.struck}>Strangers</Text>.{"\n"}
          <Text style={styles.safe}>Safely.</Text>
        </Text>

        <Text style={styles.sub}>
          Buy and sell with confidence.{"\n"}KAMMO protects your money until the item is delivered.
        </Text>

        <View style={styles.demo}>
          <View style={styles.flow}>
            {["BUYER", "KAMMO", "SELLER"].map((step, i) => (
              <View key={step} style={styles.flowRow}>
                {i > 0 ? <Text style={styles.flowArrow}>→</Text> : null}
                <View style={[styles.flowStep, step === "KAMMO" && styles.flowStepOn]}>
                  <Text style={[styles.flowLbl, step === "KAMMO" && styles.flowLblOn]}>{step}</Text>
                </View>
              </View>
            ))}
          </View>
          <View style={styles.bubbles}>
            <View style={[styles.bubble, styles.bubbleIn]}>
              <Text style={styles.bubbleTxt}>"Can I pay safely?"</Text>
            </View>
            <View style={[styles.bubble, styles.bubbleBot]}>
              <Text style={styles.bubbleBotTxt}>
                <Text style={styles.botBold}>KAMMO</Text> · Funds locked until delivery is confirmed.
              </Text>
            </View>
            <View style={[styles.bubble, styles.bubbleOut]}>
              <Text style={styles.bubbleTxt}>"Great, I'll ship today." ✓</Text>
            </View>
          </View>
        </View>
      </View>

      <View style={styles.foot}>
        <View style={styles.pillars}>
          {PILLARS.map(({ icon: Icon, label }) => (
            <View key={label} style={styles.pillar}>
              <View style={styles.pillarIco}>
                <Icon color={colors.green} size={14} />
              </View>
              <Text style={styles.pillarLbl}>{label}</Text>
            </View>
          ))}
        </View>

        <Button title="Create Account →" onPress={() => router.push("/register")} />
        <Button title="Sign In" variant="outline" onPress={() => router.push("/login")} />
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  wrap: { flexGrow: 1, justifyContent: "space-between", paddingVertical: 14, overflow: "hidden" },
  center: { flexGrow: 1, justifyContent: "center", alignItems: "center" },
  glow1: {
    position: "absolute",
    top: -40,
    left: -60,
    width: 360,
    height: 360,
    borderRadius: 180,
    backgroundColor: "rgba(0,232,122,0.1)",
  },
  glow2: {
    position: "absolute",
    bottom: 80,
    right: -80,
    width: 300,
    height: 300,
    borderRadius: 150,
    backgroundColor: "rgba(79,163,255,0.05)",
  },
  top: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 8,
  },
  logo: { fontSize: 26, color: colors.text, letterSpacing: -0.5 },
  logoEm: { fontStyle: "italic" },
  logoDot: { color: colors.green },
  skip: { fontSize: 13, color: colors.mid, fontWeight: "500" },
  body: { flex: 1, justifyContent: "center", paddingHorizontal: 10, gap: 16 },
  badge: {
    flexDirection: "row",
    alignItems: "center",
    alignSelf: "flex-start",
    gap: 7,
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: 100,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.2)",
  },
  badgeDot: { width: 6, height: 6, borderRadius: 3, backgroundColor: colors.green },
  badgeTxt: { fontSize: 10, fontWeight: "600", color: colors.green, letterSpacing: 0.4 },
  headline: { fontSize: 44, lineHeight: 44, color: colors.text, letterSpacing: -2 },
  struck: { color: colors.mid, textDecorationLine: "line-through", textDecorationColor: colors.red },
  safe: { fontStyle: "italic", color: colors.green },
  sub: { fontSize: 14, color: colors.mid, lineHeight: 22, maxWidth: 300 },
  demo: {
    backgroundColor: colors.ink2,
    borderWidth: 1,
    borderColor: colors.line2,
    borderRadius: 20,
    padding: 18,
  },
  flow: { flexDirection: "row", alignItems: "center", justifyContent: "center", marginBottom: 12, gap: 4 },
  flowRow: { flexDirection: "row", alignItems: "center", gap: 4 },
  flowArrow: { color: colors.dim, fontSize: 11 },
  flowStep: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
    backgroundColor: colors.ink3,
    borderWidth: 1,
    borderColor: colors.line,
  },
  flowStepOn: { backgroundColor: colors.greenDim, borderColor: "rgba(0,232,122,0.18)" },
  flowLbl: { fontSize: 8, color: colors.mid, fontWeight: "600" },
  flowLblOn: { color: colors.green },
  bubbles: { gap: 6 },
  bubble: { maxWidth: "88%", paddingHorizontal: 13, paddingVertical: 9, borderRadius: 13 },
  bubbleIn: { alignSelf: "flex-start", backgroundColor: colors.ink3, borderWidth: 1, borderColor: colors.line },
  bubbleBot: {
    alignSelf: "center",
    backgroundColor: "rgba(0,232,122,0.05)",
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.2)",
    maxWidth: "94%",
  },
  bubbleOut: { alignSelf: "flex-end", backgroundColor: "#1a2b20" },
  bubbleTxt: { fontSize: 12, color: colors.text, lineHeight: 17 },
  bubbleBotTxt: { fontSize: 11, color: colors.mid, textAlign: "center", lineHeight: 16 },
  botBold: { color: colors.green, fontWeight: "700" },
  foot: { gap: 8, paddingHorizontal: 10 },
  pillars: { flexDirection: "row", gap: 8, marginBottom: 12 },
  pillar: {
    flex: 1,
    alignItems: "center",
    gap: 5,
    paddingVertical: 10,
    backgroundColor: colors.ink3,
    borderWidth: 1,
    borderColor: colors.line2,
    borderRadius: 14,
  },
  pillarIco: {
    width: 28,
    height: 28,
    borderRadius: 8,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.18)",
    alignItems: "center",
    justifyContent: "center",
  },
  pillarLbl: { fontSize: 8.5, color: colors.mid, textAlign: "center", lineHeight: 12 },
});
