import { router } from "expo-router";
import { useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import StatusPill from "../src/components/StatusPill";
import { Button, Card, ErrorText, Field, Screen, Subtitle, Title } from "../src/components/ui";
import { useKammo } from "../src/KammoContext";
import { formatRand, statusLabel } from "../src/utils";
import { colors } from "../src/theme";

function extractDealCode(raw) {
  const value = String(raw || "").trim();
  const fromUrl = value.match(/deal\/([A-Za-z0-9]+)/i);
  if (fromUrl) return fromUrl[1];
  const fromRef = value.match(/KM-([A-Za-z0-9]+)/i);
  if (fromRef) return fromRef[1];
  return value.replace(/^KM-/i, "");
}

export default function JoinDealScreen() {
  const { token, loadDeal, acceptAsBuyer, busy, error, clearError } = useKammo();
  const [input, setInput] = useState("");
  const [deal, setDeal] = useState(null);

  async function lookup() {
    clearError();
    const code = extractDealCode(input);
    if (!code) return;
    try {
      const loaded = await loadDeal(code, { withMessages: false });
      setDeal(loaded);
    } catch {
      setDeal(null);
    }
  }

  async function accept() {
    try {
      await acceptAsBuyer();
      router.replace(`/deal/${deal.dealCode}`);
    } catch {}
  }

  function openDeal() {
    router.replace(`/deal/${deal.dealCode}`);
  }

  if (!token) {
    return (
      <Screen>
        <Title>Join a deal</Title>
        <Subtitle>Sign in to accept or manage a deal from a link or QR code.</Subtitle>
        <Button title="Sign In" onPress={() => router.push("/login")} />
      </Screen>
    );
  }

  return (
    <Screen>
      <Title>Join a deal</Title>
      <Subtitle>Enter a deal code or paste a KAMMO link from WhatsApp / SMS.</Subtitle>

      <Field
        label="Deal code or link"
        placeholder="AB42XK or kammo.co.za/deal/AB42XK"
        value={input}
        onChangeText={setInput}
        autoCapitalize="characters"
      />
      <Button title="Load from backend" onPress={lookup} loading={busy} />
      <ErrorText>{error}</ErrorText>

      {deal ? (
        <Card style={styles.dealCard}>
          <StatusPill status={deal.status} />
          <Text style={styles.itemName}>{deal.itemName}</Text>
          <Text style={styles.ref}>{deal.dealReference || deal.dealCode}</Text>
          <Text style={styles.price}>{formatRand(deal.price)}</Text>
          <Text style={styles.meta}>Total to pay: {formatRand(deal.totalToPay)}</Text>
          <Text style={styles.meta}>Status: {statusLabel(deal.status)}</Text>
          {deal.nextBuyerAction ? (
            <Text style={styles.hint}>{deal.nextBuyerAction}</Text>
          ) : null}
          {deal.status === "AWAITING_BUYER_PAYMENT" && deal.ownerRole === "SELLER" ? (
            <Button title="Accept as Buyer" onPress={accept} loading={busy} />
          ) : (
            <Button title="Open Deal" onPress={openDeal} />
          )}
        </Card>
      ) : null}

      <Button title="Scan QR instead" variant="outline" onPress={() => router.push("/scan-deal")} />
      <Button title="Back" variant="outline" onPress={() => router.back()} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  dealCard: { gap: 8, marginTop: 8 },
  itemName: { color: colors.white, fontSize: 18, fontWeight: "700" },
  ref: { color: colors.green, fontFamily: "monospace", fontSize: 12 },
  price: { color: colors.green, fontSize: 24, fontWeight: "800" },
  meta: { color: colors.grey, fontSize: 13 },
  hint: { color: colors.grey, fontSize: 12, lineHeight: 18, marginTop: 4 },
});
