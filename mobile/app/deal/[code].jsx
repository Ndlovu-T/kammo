import { router, useLocalSearchParams } from "expo-router";
import { useEffect, useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import { Lock, Shield, Truck } from "lucide-react-native";
import DealTimeline from "../../src/components/DealTimeline";
import StatusPill from "../../src/components/StatusPill";
import { Button, Card, ErrorText, Field, Screen, Subtitle } from "../../src/components/ui";
import { openCheckout, referenceFromCallbackUrl } from "../../src/checkout";
import { getDealActions, userDealRole } from "../../src/dealActions";
import { useKammo } from "../../src/KammoContext";
import { formatRand, statusLabel } from "../../src/utils";
import { colors } from "../../src/theme";

const API_FN = {
  acceptAsSeller: (api, t, code) => api.acceptAsSeller(t, code),
  acceptAsBuyer: (ctx) => ctx.acceptAsBuyer(),
  markReadyForCollection: (api, t, code) => api.markReadyForCollection(t, code),
  markInTransit: (api, t, code) => api.markInTransit(t, code),
  confirmDelivery: (api, t, code) => api.confirmDelivery(t, code),
  raiseDispute: (api, t, code) => api.raiseDispute(t, code),
};

export default function DealScreen() {
  const { code } = useLocalSearchParams();
  const ctx = useKammo();
  const {
    selectedDeal,
    selectDeal,
    dealAction,
    acceptAsBuyer,
    kammoApi,
    token,
    rateDeal,
    userPhone,
    busy,
    error,
  } = ctx;
  const [rating, setRating] = useState("5");
  const [comment, setComment] = useState("");
  const [otpStep, setOtpStep] = useState(false);
  const [otpCode, setOtpCode] = useState("");
  const [payBusy, setPayBusy] = useState(false);
  const [payError, setPayError] = useState("");

  useEffect(() => {
    if (code) selectDeal(String(code)).catch(() => {});
  }, [code]);

  const deal = selectedDeal;
  const role = userDealRole(deal, userPhone);
  const actions = getDealActions(deal, userPhone).filter(
    (a) => !(a.key === "pay" && otpStep)
  );

  async function startPayment() {
    setPayError("");
    setPayBusy(true);
    try {
      await kammoApi.requestPaymentOtp(token, deal.dealCode);
      setOtpStep(true);
    } catch (e) {
      setPayError(e.message || "Failed to send OTP");
    } finally {
      setPayBusy(false);
    }
  }

  async function submitOtp() {
    setPayError("");
    setPayBusy(true);
    try {
      await kammoApi.verifyPaymentOtp(token, deal.dealCode, otpCode);
      const updated = await dealAction((t, c) => kammoApi.markPaymentSecured(t, c));
      if (updated.pendingPaymentUrl) {
        const redirectUrl = await openCheckout(updated.pendingPaymentUrl, "kammo://deal/callback");
        const reference = referenceFromCallbackUrl(redirectUrl);
        await dealAction((t, c) => kammoApi.confirmPayment(t, c, reference));
      }
      setOtpStep(false);
      setOtpCode("");
    } catch (e) {
      setPayError(e.message || "Payment failed");
    } finally {
      setPayBusy(false);
    }
  }

  async function runAction(action) {
    if (action.api === "pay") {
      return startPayment();
    }
    try {
      if (action.api === "acceptAsBuyer") {
        await acceptAsBuyer();
      } else {
        const fn = API_FN[action.api];
        if (fn) await dealAction((t, c) => fn(kammoApi, t, c));
      }
      if (action.navigate) router.push(`${action.navigate}?code=${deal.dealCode}`);
    } catch {}
  }

  if (!deal) {
    return (
      <Screen>
        <Subtitle>Loading deal from backend...</Subtitle>
        <ErrorText>{error}</ErrorText>
      </Screen>
    );
  }

  const nextHint =
    role === "buyer" ? deal.nextBuyerAction : deal.nextSellerAction;

  return (
    <Screen>
      <View style={styles.hero}>
        <View style={styles.heroIcon}><Lock color={colors.green} size={26} /></View>
        <Text style={styles.heroStatus}>{statusLabel(deal.status)}</Text>
        <Text style={styles.heroSub}>
          {formatRand(deal.price)} · {nextHint || deal.itemName}
        </Text>
        <View style={styles.shield}>
          <Shield color={colors.green} size={12} />
          <Text style={styles.shieldText}>Protected by KAMMO · You are the {role}</Text>
        </View>
      </View>

      <View style={styles.stats}>
        {[
          ["Amount", formatRand(deal.price), colors.green],
          ["Total", formatRand(deal.totalToPay), colors.white],
          ["Deal ID", deal.dealReference || deal.dealCode, colors.white],
        ].map(([lbl, val, clr]) => (
          <View key={lbl} style={styles.statBox}>
            <Text style={styles.statLbl}>{lbl}</Text>
            <Text style={[styles.statVal, { color: clr }, lbl === "Deal ID" && styles.mono]}>{val}</Text>
          </View>
        ))}
      </View>

      {deal.waybillNumber ? (
        <Card style={styles.track}>
          <View style={styles.trackHead}>
            <Text style={styles.trackTitle}>Courier tracking</Text>
            <StatusPill status={deal.status} small />
          </View>
          <View style={styles.trackRow}>
            <View style={styles.trackIco}><Truck color={colors.amber} size={18} /></View>
            <View>
              <Text style={styles.trackName}>The Courier Guy</Text>
              <Text style={styles.waybill}>{deal.waybillNumber}</Text>
            </View>
          </View>
        </Card>
      ) : null}

      <Text style={styles.section}>Progress</Text>
      <DealTimeline trackerStep={deal.trackerStep} />

      <Button
        title="Open Chat"
        variant="outline"
        onPress={() => router.push(`/chat/${deal.dealCode}`)}
      />

      {actions.length > 0 ? (
        <>
          <Text style={styles.section}>Actions</Text>
          {actions.map((action) => (
            <Button
              key={action.key}
              title={action.title}
              variant={action.variant || (action.key === "confirm" ? "green" : "outline")}
              loading={busy || payBusy}
              onPress={() => runAction(action)}
            />
          ))}
        </>
      ) : deal.status === "COMPLETED" ? (
        <Button title="Rate this deal" onPress={() => router.push("/confirmed")} />
      ) : null}

      {otpStep ? (
        <>
          <Text style={styles.section}>Enter the code we sent you</Text>
          <Field
            label="6-digit code"
            value={otpCode}
            onChangeText={setOtpCode}
            keyboardType="numeric"
            maxLength={6}
            placeholder="000000"
          />
          <ErrorText>{payError}</ErrorText>
          <Button title="Confirm & Pay" loading={payBusy || busy} onPress={submitOtp} />
          <Button
            title="Cancel"
            variant="outline"
            onPress={() => {
              setOtpStep(false);
              setOtpCode("");
              setPayError("");
            }}
          />
        </>
      ) : null}

      {deal.status === "COMPLETED" ? (
        <>
          <Text style={styles.section}>Rate deal</Text>
          <Field label="Score 1-5" value={rating} onChangeText={setRating} keyboardType="numeric" />
          <Field label="Comment" value={comment} onChangeText={setComment} />
          <Button
            title="Submit Rating"
            variant="outline"
            loading={busy}
            onPress={() => rateDeal(Number(rating), comment).catch(() => {})}
          />
        </>
      ) : null}

      <ErrorText>{error}</ErrorText>
      <Button title="Back" variant="outline" onPress={() => router.back()} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  hero: {
    backgroundColor: "#0A1F18",
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.25)",
    borderRadius: 20,
    padding: 20,
    alignItems: "center",
    marginBottom: 12,
  },
  heroIcon: {
    width: 56,
    height: 56,
    borderRadius: 16,
    backgroundColor: colors.greenDim,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 12,
  },
  heroStatus: { color: colors.white, fontSize: 20, fontWeight: "700" },
  heroSub: { color: colors.grey, fontSize: 13, marginTop: 6, textAlign: "center", lineHeight: 18 },
  shield: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    marginTop: 14,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.2)",
    borderRadius: 999,
    paddingVertical: 7,
    paddingHorizontal: 14,
  },
  shieldText: { color: colors.green, fontSize: 12, fontWeight: "700" },
  stats: { flexDirection: "row", gap: 10, marginBottom: 12 },
  statBox: {
    flex: 1,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 14,
    padding: 14,
    alignItems: "center",
  },
  statLbl: { color: colors.grey, fontSize: 10, textTransform: "uppercase", letterSpacing: 0.5, marginBottom: 4 },
  statVal: { fontSize: 14, fontWeight: "700", textAlign: "center" },
  mono: { fontFamily: "monospace", fontSize: 10 },
  track: { marginBottom: 8 },
  trackHead: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 12 },
  trackTitle: { color: colors.white, fontSize: 15, fontWeight: "700" },
  trackRow: { flexDirection: "row", alignItems: "center", gap: 12, backgroundColor: colors.surface2, borderRadius: 14, padding: 12 },
  trackIco: {
    width: 40,
    height: 40,
    borderRadius: 10,
    backgroundColor: colors.surface3,
    alignItems: "center",
    justifyContent: "center",
  },
  trackName: { color: colors.white, fontSize: 13, fontWeight: "700" },
  waybill: { color: colors.grey, fontSize: 11, fontFamily: "monospace", marginTop: 2 },
  section: { color: colors.white, fontSize: 16, fontWeight: "700", marginTop: 12, marginBottom: 4 },
});
