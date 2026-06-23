import { router } from "expo-router";
import { Share, Pressable, StyleSheet, Text, View } from "react-native";
import { CreditCard, Lock, LogOut, MessageCircle, Shield, UserCheck } from "lucide-react-native";
import { useState } from "react";
import TrustRing from "../../src/components/TrustRing";
import { Screen } from "../../src/components/ui";
import { useKammo } from "../../src/KammoContext";
import { colors } from "../../src/theme";

function MenuRow({ icon, title, sub, onPress, danger, trailing }) {
  return (
    <Pressable style={styles.mrow} onPress={onPress}>
      <View style={[styles.mico, danger && styles.micoDanger]}>{icon}</View>
      <View style={styles.mbody}>
        <Text style={[styles.mtitle, danger && styles.mtitleDanger]}>{title}</Text>
        {sub ? <Text style={styles.msub}>{sub}</Text> : null}
      </View>
      {trailing}
    </Pressable>
  );
}

export default function ProfileTab() {
  const { profile, displayName, logout } = useKammo();
  const [copied, setCopied] = useState(false);
  const trustScore = profile?.trustScore ?? 96;
  const kammoId = profile?.kammoId?.startsWith("KAM-")
    ? profile.kammoId
    : profile?.kammoId
      ? `KAM-${profile.kammoId}`
      : "KAM-------";

  async function copyId() {
    try {
      await Share.share({ message: kammoId });
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {}
  }

  async function signOut() {
    await logout();
    router.replace("/");
  }

  return (
    <Screen style={styles.screen}>
      <View style={styles.banner}>
        <TrustRing score={trustScore} size={86} stroke={6} />
        <View style={styles.band}>
          <Text style={styles.bandTxt}>TRUSTED</Text>
        </View>
        <Text style={styles.name}>{displayName}</Text>
        <Text style={styles.id}>{kammoId}</Text>
        <View style={styles.chips}>
          {["ID", "Phone", "Face"].map((c) => (
            <View key={c} style={styles.chip}>
              <Text style={styles.chipTxt}>✓ {c}</Text>
            </View>
          ))}
          <View style={styles.chipDeal}>
            <Text style={styles.chipDealTxt}>Tier 2 FICA</Text>
          </View>
        </View>
        <Text style={styles.rating}>★★★★★ 4.9 · {profile?.dealCount ?? 0} deals</Text>
      </View>

      <View style={styles.idCard}>
        <View style={styles.idGlow} />
        <Text style={styles.idLbl}>YOUR KAMMO ID</Text>
        <Text style={styles.idBig}>{kammoId}</Text>
        <Text style={styles.idSub}>{displayName} · Share instead of your number</Text>
        <View style={styles.idActions}>
          <Pressable style={styles.copyBtn} onPress={copyId}>
            <Text style={styles.copyTxt}>Copy ID</Text>
          </Pressable>
        </View>
        {copied ? <Text style={styles.copied}>Copied to clipboard!</Text> : null}
      </View>

      <View style={styles.trustCard}>
        <TrustRing score={trustScore} />
        <View style={styles.trustBody}>
          <View style={styles.trustHead}>
            <Text style={styles.trustTitle}>Trust Score</Text>
            <View style={styles.trustBadge}>
              <Text style={styles.trustBadgeTxt}>TRUSTED</Text>
            </View>
          </View>
          <View style={styles.bar}>
            <View style={[styles.barFill, { width: `${trustScore}%` }]} />
          </View>
          <Text style={styles.trustSub}>Identity · Behaviour · No penalties</Text>
        </View>
      </View>

      <View style={styles.menu}>
        <MenuRow
          icon={<CreditCard color={colors.green} size={17} />}
          title="Wallet & Payments"
          sub="Balance · History · Bank accounts"
          onPress={() => router.push("/(tabs)/wallet")}
        />
        <MenuRow
          icon={<Lock color={colors.green} size={17} />}
          title="Security"
          sub="Password · 2FA · Biometrics"
        />
        <MenuRow
          icon={<UserCheck color={colors.green} size={17} />}
          title="Identity (FICA / KYC)"
          sub="ID · Passport · Driver's License"
          trailing={<Text style={styles.checkMark}>✓</Text>}
        />
        <MenuRow
          icon={<MessageCircle color={colors.green} size={17} />}
          title="WhatsApp Bot"
          sub="Linked · Manage notifications"
          trailing={<Text style={styles.liveDot}>●</Text>}
        />
        <MenuRow
          icon={<Shield color={colors.green} size={17} />}
          title="How KAMMO Works"
        />
        <MenuRow
          icon={<LogOut color={colors.red} size={17} />}
          title="Sign out"
          onPress={signOut}
          danger
        />
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  screen: { paddingHorizontal: 0, paddingTop: 0 },
  banner: {
    backgroundColor: colors.ink2,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
    paddingVertical: 22,
    paddingHorizontal: 16,
    alignItems: "center",
  },
  band: {
    marginTop: 4,
    marginBottom: 12,
    paddingHorizontal: 12,
    paddingVertical: 3,
    borderRadius: 20,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.25)",
  },
  bandTxt: { fontSize: 10, fontWeight: "700", color: colors.green, letterSpacing: 0.8 },
  name: { fontSize: 19, fontWeight: "800", color: colors.text, letterSpacing: -0.5 },
  id: { fontSize: 13, color: colors.green, letterSpacing: 1, marginTop: 2, marginBottom: 10 },
  chips: { flexDirection: "row", flexWrap: "wrap", gap: 6, justifyContent: "center", marginBottom: 10 },
  chip: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 20,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.2)",
  },
  chipTxt: { fontSize: 10.5, color: colors.green, fontWeight: "600" },
  chipDeal: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 20,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.2)",
  },
  chipDealTxt: { fontSize: 10.5, color: colors.green, fontWeight: "600" },
  rating: { fontSize: 12, color: colors.mid },
  idCard: {
    marginHorizontal: 16,
    marginTop: 12,
    backgroundColor: colors.walletBg,
    borderWidth: 1.5,
    borderColor: "rgba(0,232,122,0.28)",
    borderRadius: 20,
    padding: 20,
    overflow: "hidden",
  },
  idGlow: {
    position: "absolute",
    top: -40,
    right: -40,
    width: 140,
    height: 140,
    borderRadius: 70,
    backgroundColor: "rgba(0,232,122,0.1)",
  },
  idLbl: { fontSize: 9.5, color: "rgba(0,232,122,0.6)", letterSpacing: 1.5, marginBottom: 6 },
  idBig: { fontSize: 28, fontWeight: "800", color: colors.text, letterSpacing: 1.2, marginBottom: 4 },
  idSub: { fontSize: 13, color: colors.mid, marginBottom: 14 },
  idActions: { flexDirection: "row", gap: 8 },
  copyBtn: {
    paddingHorizontal: 16,
    paddingVertical: 9,
    borderRadius: 10,
    backgroundColor: colors.green,
  },
  copyTxt: { fontSize: 12, fontWeight: "700", color: colors.ink },
  copied: { fontSize: 10, color: colors.green, marginTop: 8 },
  trustCard: {
    marginHorizontal: 16,
    marginTop: 10,
    backgroundColor: colors.ink3,
    borderWidth: 1,
    borderColor: colors.line2,
    borderRadius: 18,
    padding: 16,
    flexDirection: "row",
    gap: 14,
    alignItems: "center",
  },
  trustBody: { flex: 1 },
  trustHead: { flexDirection: "row", alignItems: "center", gap: 8, marginBottom: 5 },
  trustTitle: { fontSize: 15, fontWeight: "800", color: colors.text },
  trustBadge: {
    paddingHorizontal: 9,
    paddingVertical: 2,
    borderRadius: 20,
    backgroundColor: colors.greenGlow,
    borderWidth: 1,
    borderColor: "rgba(0,232,122,0.25)",
  },
  trustBadgeTxt: { fontSize: 10, fontWeight: "700", color: colors.green },
  bar: { height: 4, backgroundColor: colors.ink4, borderRadius: 2, overflow: "hidden", marginBottom: 6 },
  barFill: { height: "100%", backgroundColor: colors.green, borderRadius: 2 },
  trustSub: { fontSize: 11, color: colors.mid },
  menu: { marginTop: 10 },
  mrow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    paddingVertical: 14,
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
  },
  mico: {
    width: 36,
    height: 36,
    borderRadius: 10,
    backgroundColor: colors.ink3,
    alignItems: "center",
    justifyContent: "center",
  },
  micoDanger: { backgroundColor: colors.redDim },
  mbody: { flex: 1 },
  mtitle: { fontSize: 14, fontWeight: "600", color: colors.text },
  mtitleDanger: { color: colors.red },
  msub: { fontSize: 11, color: colors.mid, marginTop: 1 },
  checkMark: { color: colors.green, fontSize: 16 },
  liveDot: { color: colors.green, fontSize: 10 },
});
