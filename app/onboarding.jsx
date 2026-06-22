import { router } from "expo-router";
import { useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import {
  Check,
  Globe,
  Lock,
  MessageSquare,
  Shield,
  Smartphone,
  User,
} from "lucide-react-native";
import DotPager from "../src/components/DotPager";
import { Button, Screen, Subtitle, Title } from "../src/components/ui";
import { colors } from "../src/theme";

const STEPS = [
  {
    title: "Trade with strangers,\nsafely.",
    body: "Meet on Facebook, WhatsApp, or Gumtree. Initiate a KAMMO deal. Both sides are protected.",
    art: "trade",
    dot: 0,
  },
  {
    title: "Money moves only when\nyou say so.",
    body: "Buyer's payment is held via our licensed partner — not released until you confirm delivery.",
    art: "vault",
    dot: 1,
  },
  {
    title: "Works wherever\nthe deal happens.",
    body: "App or WhatsApp — start a KAMMO deal from wherever you already are.",
    art: "channels",
    dot: 2,
  },
];

function Illustration({ type }) {
  if (type === "trade") {
    return (
      <View style={styles.artInner}>
        <View style={styles.tradeRow}>
          <View style={styles.avatar}><User color={colors.grey} size={26} /></View>
          <View style={styles.lines}><View style={styles.lineL} /><View style={styles.lineS} /></View>
        </View>
        <View style={styles.connector} />
        <View style={styles.secured}>
          <Shield color={colors.green} size={18} />
          <Text style={styles.securedText}>KAMMO Secured</Text>
        </View>
        <View style={styles.connector} />
        <View style={styles.tradeRow}>
          <View style={styles.avatar}><User color={colors.grey} size={26} /></View>
          <View style={styles.lines}><View style={styles.lineL} /><View style={styles.lineS} /></View>
        </View>
      </View>
    );
  }

  if (type === "vault") {
    return (
      <View style={[styles.artInner, { width: "85%" }]}>
        <View style={styles.vaultRow}><Text style={styles.vaultLbl}>Buyer pays</Text><Text style={styles.vaultVal}>R 8,500</Text></View>
        <View style={styles.vaultMid}>
          <View style={styles.vaultLine} />
          <Lock color={colors.green} size={10} />
          <Text style={styles.vaultHeld}>HELD SECURELY</Text>
          <View style={styles.vaultLine} />
        </View>
        <View style={styles.vaultVault}>
          <Shield color={colors.green} size={14} />
          <Text style={styles.vaultVaultTxt}>KAMMO PARTNER VAULT</Text>
        </View>
        <View style={styles.vaultMid}>
          <View style={[styles.vaultLine, { backgroundColor: colors.surface3 }]} />
          <Text style={styles.vaultOnConf}>ON CONFIRMATION</Text>
          <View style={[styles.vaultLine, { backgroundColor: colors.surface3 }]} />
        </View>
        <View style={styles.vaultRow}>
          <Text style={styles.vaultLbl}>Seller receives</Text>
          <View style={styles.vaultRecv}>
            <Text style={styles.vaultRecvVal}>R 8,500</Text>
            <Check color={colors.green} size={14} />
          </View>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.artInner}>
      <Text style={styles.channelsLbl}>Works everywhere you trade</Text>
      <View style={styles.channels}>
        {[
          { Icon: MessageSquare, lbl: "WhatsApp", clr: colors.waGreen },
          { Icon: Globe, lbl: "Facebook", clr: "#5B8DEF" },
          { Icon: Smartphone, lbl: "KAMMO App", clr: colors.grey },
        ].map(({ Icon, lbl, clr }) => (
          <View key={lbl} style={styles.channel}>
            <View style={styles.channelIco}><Icon color={clr} size={26} /></View>
            <Text style={[styles.channelLbl, { color: clr }]}>{lbl}</Text>
          </View>
        ))}
      </View>
      <View style={styles.linkBox}>
        <Text style={styles.linkTitle}>One deal link. Anywhere.</Text>
        <Text style={styles.linkUrl}>kammo.co.za/deal/AB42XK</Text>
      </View>
    </View>
  );
}

export default function OnboardingScreen() {
  const [step, setStep] = useState(0);
  const current = STEPS[step];
  const isLast = step === STEPS.length - 1;

  function next() {
    if (isLast) router.replace("/register");
    else setStep((s) => s + 1);
  }

  return (
    <Screen scroll={false} style={styles.screen}>
      <View style={styles.flex}>
        <View style={styles.art}>
          <Illustration type={current.art} />
        </View>
        <View style={styles.copy}>
          <Title style={styles.title}>{current.title}</Title>
          <Subtitle style={styles.body}>{current.body}</Subtitle>
        </View>
      </View>
      <View style={styles.footer}>
        <Button title={isLast ? "Create My Account" : "Next"} onPress={next} />
        {!isLast ? (
          <>
            <DotPager total={3} active={current.dot} />
            <Pressable onPress={() => router.replace("/register")}>
              <Text style={styles.skip}>Skip</Text>
            </Pressable>
          </>
        ) : null}
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  screen: { flexGrow: 1, justifyContent: "space-between", paddingBottom: 32 },
  flex: { flex: 1, justifyContent: "center", gap: 28 },
  art: {
    height: 260,
    backgroundColor: colors.surface2,
    borderRadius: 28,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: "center",
    justifyContent: "center",
    overflow: "hidden",
  },
  artInner: { alignItems: "center", gap: 16 },
  tradeRow: { flexDirection: "row", alignItems: "center", gap: 24 },
  avatar: {
    width: 60,
    height: 60,
    borderRadius: 16,
    backgroundColor: colors.surface3,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: "center",
    justifyContent: "center",
  },
  lines: { gap: 8 },
  lineL: { width: 80, height: 4, borderRadius: 4, backgroundColor: colors.surface3 },
  lineS: { width: 56, height: 4, borderRadius: 4, backgroundColor: colors.surface2 },
  connector: { width: 1, height: 20, backgroundColor: "rgba(0,194,120,0.3)" },
  secured: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.25)",
    borderRadius: 14,
    paddingVertical: 12,
    paddingHorizontal: 20,
  },
  securedText: { color: colors.green, fontSize: 14, fontWeight: "700" },
  vaultRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    width: "100%",
    backgroundColor: colors.surface3,
    borderRadius: 12,
    padding: 12,
  },
  vaultLbl: { color: colors.grey, fontSize: 12 },
  vaultVal: { color: colors.white, fontSize: 15, fontWeight: "700" },
  vaultMid: { flexDirection: "row", alignItems: "center", gap: 8, width: "100%", paddingHorizontal: 8 },
  vaultLine: { flex: 1, height: 2, backgroundColor: colors.green },
  vaultHeld: { color: colors.green, fontSize: 10, fontWeight: "700" },
  vaultVault: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 8,
    width: "100%",
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.2)",
    borderRadius: 12,
    padding: 12,
  },
  vaultVaultTxt: { color: colors.green, fontSize: 11, fontWeight: "700", letterSpacing: 0.5 },
  vaultOnConf: { color: colors.grey, fontSize: 10, fontWeight: "600" },
  vaultRecv: { flexDirection: "row", alignItems: "center", gap: 6 },
  vaultRecvVal: { color: colors.green, fontSize: 15, fontWeight: "700" },
  channelsLbl: { color: colors.grey, fontSize: 13, fontWeight: "600", textTransform: "uppercase", letterSpacing: 0.8 },
  channels: { flexDirection: "row", gap: 24 },
  channel: { alignItems: "center", gap: 6 },
  channelIco: {
    width: 56,
    height: 56,
    borderRadius: 16,
    backgroundColor: colors.surface3,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: "center",
    justifyContent: "center",
  },
  channelLbl: { fontSize: 11, fontWeight: "700" },
  linkBox: {
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.2)",
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 20,
    alignItems: "center",
  },
  linkTitle: { color: colors.green, fontSize: 13, fontWeight: "700" },
  linkUrl: { color: "rgba(0,194,120,0.7)", fontSize: 11, marginTop: 4, fontFamily: "monospace" },
  copy: { alignItems: "center", gap: 12 },
  title: { textAlign: "center", fontSize: 26, lineHeight: 32 },
  body: { textAlign: "center", fontSize: 15 },
  footer: { gap: 12 },
  skip: { color: colors.grey, textAlign: "center", fontSize: 13, fontWeight: "600" },
});
