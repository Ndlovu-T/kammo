import { router } from "expo-router";
import { useState } from "react";
import {
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import {
  ArrowLeft,
  Banknote,
  Check,
  CheckCircle,
  Link2,
  Lock,
  MessageSquare,
  MoreVertical,
  Paperclip,
  Phone,
  Send,
  Shield,
} from "lucide-react-native";
import { Button } from "../src/components/ui";
import { useKammo } from "../src/KammoContext";
import { formatRand } from "../src/utils";
import { colors } from "../src/theme";

function WaHeader({ tab, onTab }) {
  return (
    <View style={styles.header}>
      <View style={styles.headerTop}>
        <Pressable style={styles.backBtn} onPress={() => router.back()}>
          <ArrowLeft color="#E9EDEF" size={18} />
        </Pressable>
        <View style={styles.botAvatar}>
          <Shield color="#000" size={18} />
        </View>
        <View style={styles.headerInfo}>
          <Text style={styles.botName}>KAMMO Bot</Text>
          <View style={styles.botStatus}>
            <View style={styles.onlineDot} />
            <Text style={styles.botSub}>Active · Official Partner</Text>
          </View>
        </View>
        <Phone color="rgba(233,237,239,0.6)" size={18} />
        <MoreVertical color="rgba(233,237,239,0.6)" size={18} />
      </View>
      <View style={styles.tabs}>
        {[["seller", "SELLER FLOW"], ["buyer", "BUYER FLOW"]].map(([id, lbl]) => (
          <Pressable key={id} style={styles.tab} onPress={() => onTab(id)}>
            <Text style={[styles.tabTxt, tab === id && styles.tabTxtOn]}>{lbl}</Text>
            {tab === id ? <View style={styles.tabLine} /> : null}
          </Pressable>
        ))}
      </View>
    </View>
  );
}

function DatePill({ label }) {
  return (
    <View style={styles.dateWrap}>
      <Text style={styles.datePill}>{label}</Text>
    </View>
  );
}

function WaBubble({ out, time, highlight, children }) {
  return (
    <View style={[styles.msgRow, out && styles.msgRowOut]}>
      <View style={[styles.bubble, out && styles.bubbleOut, highlight && styles.bubbleHi]}>
        {children}
        <View style={styles.timeRow}>
          <Text style={styles.time}>{time}</Text>
          {out ? (
            <View style={styles.checks}>
              <Check color="rgba(233,237,239,0.5)" size={9} />
              <Check color="rgba(233,237,239,0.5)" size={9} style={{ marginLeft: -4 }} />
            </View>
          ) : null}
        </View>
      </View>
    </View>
  );
}

function BotCard({ header, rows, btn }) {
  return (
    <View style={styles.botCard}>
      <View style={styles.botCardHead}>
        <Lock color="#000" size={13} />
        <Text style={styles.botCardHeadTxt}>{header}</Text>
      </View>
      <View style={styles.botCardBody}>
        {rows.map(([k, v, green]) => (
          <View key={k} style={styles.botRow}>
            <Text style={styles.botKey}>{k}</Text>
            <Text style={[styles.botVal, green && styles.botValGreen]}>{v}</Text>
          </View>
        ))}
        {btn ? (
          <View style={styles.botBtn}>
            <Link2 color={colors.green} size={13} />
            <Text style={styles.botBtnTxt}>{btn}</Text>
          </View>
        ) : null}
      </View>
    </View>
  );
}

function OptBtn({ green, children, onPress }) {
  return (
    <Pressable
      style={[styles.optBtn, green ? styles.optGreen : styles.optRed]}
      onPress={onPress}
    >
      <Text style={[styles.optTxt, { color: green ? colors.waGreen : colors.red }]}>{children}</Text>
    </Pressable>
  );
}

function SellerFlow() {
  return (
    <>
      <DatePill label="TODAY" />
      <WaBubble time="09:01">
        <Text style={styles.msgTxt}>
          Hi! I'm KAMMO Bot — your secure trade assistant.{"\n\n"}
          I help buyers and sellers trade safely without the risk of scams or non-delivery.{"\n\n"}
          What do you need help with?
        </Text>
      </WaBubble>
      <WaBubble time="09:01">
        <View style={styles.optCol}>
          <OptBtn green>I'm selling something</OptBtn>
          <OptBtn green>I'm buying something</OptBtn>
          <OptBtn green>Check my deal status</OptBtn>
        </View>
      </WaBubble>
      <WaBubble out time="09:02">
        <Text style={styles.msgTxt}>I'm selling something</Text>
      </WaBubble>
      <WaBubble time="09:02">
        <Text style={styles.msgTxt}>What are you selling? Please describe the item.</Text>
      </WaBubble>
      <WaBubble out time="09:02">
        <Text style={styles.msgTxt}>iPhone 14 Pro Max 256GB Deep Purple</Text>
      </WaBubble>
      <WaBubble time="09:03">
        <Text style={styles.msgTxt}>What is your asking price? (numbers only, e.g. 8500)</Text>
      </WaBubble>
      <WaBubble out time="09:03">
        <Text style={styles.msgTxt}>8500</Text>
      </WaBubble>
      <WaBubble time="09:04">
        <Text style={styles.msgTxt}>Deal created. Here is your secure KAMMO deal card:</Text>
        <BotCard
          header="KAMMO DEAL KM-AB42XK"
          rows={[
            ["Item", "iPhone 14 Pro Max 256GB"],
            ["Price", "R 8,500", true],
            ["Seller ID", "DT-90134"],
            ["Protection", "KAMMO Secured", true],
          ]}
          btn="kammo.co.za/deal/AB42XK"
        />
        <Text style={[styles.msgTxt, { marginTop: 8 }]}>
          Share this link with your buyer via WhatsApp, Facebook, or SMS.
        </Text>
      </WaBubble>
      <DatePill label="30 MINUTES LATER" />
      <WaBubble time="09:34" highlight>
        <Text style={styles.hiTitle}>
          <CheckCircle color={colors.waGreen} size={14} /> Payment received
        </Text>
        <Text style={styles.msgTxt}>
          {"\n"}Buyer paid R 8,705 (R 8,500 + KAMMO fee + courier). Funds are held securely.{"\n\n"}
          Your KAMMO waybill is ready:
        </Text>
        <BotCard
          header="THE COURIER GUY — Pre-paid Waybill"
          rows={[
            ["Waybill", "TCG-887-44221"],
            ["Ship To", "Johannesburg, GP"],
            ["Cost", "R 0 (paid by buyer)", true],
            ["Drop-off", "Any PostNet / TCG point"],
          ]}
          btn="Download and Print Waybill"
        />
        <Text style={[styles.msgTxt, { marginTop: 8 }]}>
          You have 24 hours to drop off. The buyer will confirm receipt.
        </Text>
      </WaBubble>
      <DatePill label="NEXT DAY" />
      <WaBubble time="14:22" highlight>
        <Text style={styles.hiTitle}>
          <Banknote color={colors.waGreen} size={15} /> Funds released
        </Text>
        <Text style={styles.msgTxt}>
          {"\n"}Buyer confirmed delivery and is satisfied.{"\n\n"}
          R 8,500 sent to your account via PayShap.{"\n"}
          Allow 1–2 hours for transfer.{"\n\n"}
          Buyer rating: ★★★★★ 5 / 5
        </Text>
      </WaBubble>
    </>
  );
}

function BuyerFlow() {
  return (
    <>
      <DatePill label="BUYER RECEIVES DEAL LINK" />
      <WaBubble time="10:14">
        <Text style={styles.msgTxt}>
          Someone shared a KAMMO deal link with you. Here are the verified deal details:
        </Text>
        <BotCard
          header="KAMMO DEAL KM-AB42XK"
          rows={[
            ["Item", "iPhone 14 Pro Max 256GB"],
            ["Price", "R 8,500", true],
            ["Seller ID", "DT-90134"],
            ["Trust Score", "98% · 4.9 stars", true],
            ["Condition", "Excellent, battery 97%"],
            ["Delivery", "Courier (tracked)"],
            ["Inspection", "24 hours after delivery"],
          ]}
        />
      </WaBubble>
      <WaBubble time="10:14">
        <Text style={styles.msgTxt}>
          Your payment is held by our licensed partner — not released to the seller until you confirm delivery.{"\n\n"}
          Do you want to proceed?
        </Text>
        <View style={[styles.optCol, { marginTop: 8 }]}>
          <OptBtn green>Yes, pay securely</OptBtn>
          <OptBtn>Cancel</OptBtn>
        </View>
      </WaBubble>
      <WaBubble out time="10:15">
        <Text style={styles.msgTxt}>Yes, pay securely</Text>
      </WaBubble>
      <WaBubble time="10:15">
        <Text style={styles.msgTxt}>Choose your payment method:</Text>
        <View style={[styles.optCol, { marginTop: 8 }]}>
          {[["PayShap — Instant", "Most SA banks supported"], ["Card — Visa / Mastercard", ""], ["EFT — 2 to 4 hours", ""]].map(([t, s]) => (
            <View key={t} style={styles.payOpt}>
              <Text style={styles.payTitle}>{t}</Text>
              {s ? <Text style={styles.paySub}>{s}</Text> : null}
            </View>
          ))}
        </View>
      </WaBubble>
      <WaBubble out time="10:16">
        <Text style={styles.msgTxt}>PayShap</Text>
      </WaBubble>
      <WaBubble time="10:16" highlight>
        <Text style={styles.hiTitle}>
          <CheckCircle color={colors.waGreen} size={14} /> Payment confirmed
        </Text>
        <Text style={styles.msgTxt}>{"\n"}R 8,705 secured. Deal KM-AB42XK is now active.</Text>
        <View style={styles.steps}>
          {["Payment secured", "Seller notified to ship", "Courier collection and tracking", "Delivery and your inspection window", "You confirm — seller gets paid"].map((s, i) => (
            <View key={i} style={styles.stepRow}>
              <View style={styles.stepNum}>
                {i === 0 ? <Check color={colors.green} size={10} /> : <Text style={styles.stepNumTxt}>{i + 1}</Text>}
              </View>
              <Text style={styles.stepTxt}>{s}</Text>
            </View>
          ))}
        </View>
      </WaBubble>
      <DatePill label="DELIVERY DAY" />
      <WaBubble time="14:38">
        <Text style={styles.msgTxt}>
          Your package is out for delivery. Waybill TCG-887-44221.{"\n\n"}
          Once it arrives, inspect within 24 hours, then reply below:
        </Text>
        <View style={[styles.optCol, { marginTop: 8 }]}>
          <OptBtn green onPress={() => router.push("/confirmed")}>
            Received and satisfied — release payment
          </OptBtn>
          <OptBtn>Issue with the item — raise dispute</OptBtn>
        </View>
      </WaBubble>
    </>
  );
}

export default function WhatsAppScreen() {
  const [tab, setTab] = useState("seller");
  const { deals, selectDeal } = useKammo();
  const latestDeal = deals[0];

  return (
    <SafeAreaView style={styles.safe} edges={["top"]}>
      <WaHeader tab={tab} onTab={setTab} />
      {latestDeal ? (
        <View style={styles.liveBanner}>
          <Text style={styles.liveTxt}>
            Live deal on backend: {latestDeal.dealReference} · {formatRand(latestDeal.price)}
          </Text>
          <Button
            title="Open"
            onPress={async () => {
              await selectDeal(latestDeal.dealCode);
              router.push(`/deal/${latestDeal.dealCode}`);
            }}
          />
        </View>
      ) : null}
      <ScrollView style={styles.bg} contentContainerStyle={styles.bgContent}>
        {tab === "seller" ? <SellerFlow /> : <BuyerFlow />}
      </ScrollView>
      <View style={styles.inputBar}>
        <MessageSquare color="rgba(233,237,239,0.4)" size={22} />
        <TextInput
          style={styles.input}
          placeholder="Type a message..."
          placeholderTextColor="rgba(233,237,239,0.4)"
          editable={false}
        />
        <Paperclip color="rgba(233,237,239,0.4)" size={22} />
        <View style={styles.send}>
          <Send color="#fff" size={18} />
        </View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: "#0C1214" },
  header: { backgroundColor: "#1F2C34" },
  headerTop: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  backBtn: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: "rgba(255,255,255,0.08)",
    alignItems: "center",
    justifyContent: "center",
  },
  botAvatar: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: colors.waGreen,
    alignItems: "center",
    justifyContent: "center",
  },
  headerInfo: { flex: 1 },
  botName: { color: "#E9EDEF", fontSize: 15, fontWeight: "700" },
  botStatus: { flexDirection: "row", alignItems: "center", gap: 4, marginTop: 2 },
  onlineDot: { width: 5, height: 5, borderRadius: 3, backgroundColor: colors.waGreen },
  botSub: { color: "rgba(233,237,239,0.5)", fontSize: 11 },
  tabs: { flexDirection: "row", borderTopWidth: 1, borderTopColor: "rgba(255,255,255,0.06)" },
  tab: { flex: 1, alignItems: "center", paddingVertical: 10 },
  tabTxt: { fontSize: 12, fontWeight: "700", color: "rgba(233,237,239,0.4)" },
  tabTxtOn: { color: colors.waGreen },
  tabLine: { position: "absolute", bottom: 0, left: 0, right: 0, height: 2, backgroundColor: colors.waGreen },
  bg: { flex: 1, backgroundColor: "#0D1418" },
  bgContent: { padding: 12, gap: 10, paddingBottom: 24 },
  dateWrap: { alignItems: "center", marginVertical: 4 },
  datePill: {
    backgroundColor: "rgba(17,27,33,0.8)",
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 4,
    fontSize: 11,
    color: "#8796A1",
  },
  msgRow: { flexDirection: "row" },
  msgRowOut: { flexDirection: "row-reverse" },
  bubble: {
    maxWidth: "82%",
    backgroundColor: "#1F2C34",
    borderRadius: 10,
    borderTopLeftRadius: 0,
    paddingHorizontal: 14,
    paddingTop: 9,
    paddingBottom: 6,
  },
  bubbleOut: {
    backgroundColor: "#005C4B",
    borderTopLeftRadius: 10,
    borderTopRightRadius: 0,
  },
  bubbleHi: {
    backgroundColor: "#0D2E1F",
    borderWidth: 1,
    borderColor: "rgba(37,211,102,0.3)",
  },
  msgTxt: { color: "#E9EDEF", fontSize: 13, lineHeight: 19 },
  hiTitle: { color: colors.waGreen, fontWeight: "700", fontSize: 13 },
  timeRow: { flexDirection: "row", alignItems: "center", justifyContent: "flex-end", gap: 3, marginTop: 3 },
  time: { color: "rgba(233,237,239,0.5)", fontSize: 10 },
  checks: { flexDirection: "row" },
  botCard: {
    marginTop: 8,
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.3)",
    borderRadius: 12,
    overflow: "hidden",
    backgroundColor: colors.surface,
  },
  botCardHead: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    backgroundColor: colors.green,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  botCardHeadTxt: { color: "#000", fontSize: 13, fontWeight: "800" },
  botCardBody: { padding: 12, gap: 8 },
  botRow: { flexDirection: "row", justifyContent: "space-between" },
  botKey: { color: colors.grey, fontSize: 12 },
  botVal: { color: colors.white, fontSize: 12, fontWeight: "600" },
  botValGreen: { color: colors.green },
  botBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 6,
    marginTop: 6,
    backgroundColor: colors.greenDim,
    borderWidth: 1,
    borderColor: "rgba(0,194,120,0.25)",
    borderRadius: 8,
    padding: 10,
  },
  botBtnTxt: { color: colors.green, fontSize: 13, fontWeight: "700" },
  optCol: { gap: 8 },
  optBtn: { borderRadius: 8, paddingVertical: 10, paddingHorizontal: 14 },
  optGreen: { backgroundColor: "rgba(37,211,102,0.12)", borderWidth: 1, borderColor: "rgba(37,211,102,0.25)" },
  optRed: { backgroundColor: "rgba(255,69,96,0.08)", borderWidth: 1, borderColor: "rgba(255,69,96,0.15)" },
  optTxt: { fontWeight: "700", fontSize: 13 },
  payOpt: {
    backgroundColor: "rgba(37,211,102,0.12)",
    borderWidth: 1,
    borderColor: "rgba(37,211,102,0.25)",
    borderRadius: 8,
    padding: 10,
  },
  payTitle: { color: colors.waGreen, fontWeight: "700", fontSize: 13 },
  paySub: { color: "rgba(233,237,239,0.5)", fontSize: 11, marginTop: 2 },
  steps: {
    backgroundColor: "#182229",
    borderRadius: 12,
    padding: 14,
    marginTop: 8,
    gap: 4,
  },
  stepRow: { flexDirection: "row", alignItems: "center", gap: 10, paddingVertical: 4 },
  stepNum: {
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: colors.greenDim,
    alignItems: "center",
    justifyContent: "center",
  },
  stepNumTxt: { color: colors.green, fontSize: 10, fontWeight: "800" },
  stepTxt: { color: "#E9EDEF", fontSize: 12, flex: 1 },
  inputBar: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    backgroundColor: "#1F2C34",
    paddingHorizontal: 12,
    paddingVertical: 10,
    paddingBottom: 28,
  },
  input: {
    flex: 1,
    backgroundColor: "#2A3942",
    borderRadius: 22,
    paddingHorizontal: 16,
    paddingVertical: 10,
    color: "#E9EDEF",
    fontSize: 14,
  },
  send: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.waGreen,
    alignItems: "center",
    justifyContent: "center",
  },
  liveBanner: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    backgroundColor: colors.greenDim,
    borderBottomWidth: 1,
    borderBottomColor: "rgba(0,194,120,0.2)",
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  liveTxt: { flex: 1, color: colors.green, fontSize: 12, fontWeight: "600" },
});
