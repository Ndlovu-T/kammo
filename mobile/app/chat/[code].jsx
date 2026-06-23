import { router, useLocalSearchParams } from "expo-router";
import { useEffect, useRef, useState } from "react";
import { FlatList, KeyboardAvoidingView, Platform, StyleSheet, Text, View } from "react-native";
import { useKammo } from "../../src/KammoContext";
import { formatRand, statusLabel } from "../../src/utils";
import { Button, ErrorText, Field, Screen, Subtitle, Title } from "../../src/components/ui";
import { colors } from "../../src/theme";

export default function ChatScreen() {
  const { code } = useLocalSearchParams();
  const { selectedDeal, selectDeal, messages, sendMessage, refreshMessages, busy, error } = useKammo();
  const [text, setText] = useState("");
  const listRef = useRef(null);

  useEffect(() => {
    if (code) selectDeal(String(code)).catch(() => {});
  }, [code]);

  useEffect(() => {
    if (!code) return;
    const id = setInterval(() => refreshMessages().catch(() => {}), 5000);
    return () => clearInterval(id);
  }, [code, refreshMessages]);

  async function send() {
    if (!text.trim()) return;
    try {
      await sendMessage(text.trim());
      setText("");
    } catch {}
  }

  const deal = selectedDeal;

  return (
    <Screen scroll={false} style={{ padding: 0 }}>
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
        keyboardVerticalOffset={80}
      >
        <View style={styles.header}>
          <Title style={styles.title}>Deal chat</Title>
          {deal ? (
            <Subtitle style={styles.sub}>
              {deal.itemName} · {formatRand(deal.price)} · {statusLabel(deal.status)}
            </Subtitle>
          ) : (
            <Subtitle style={styles.sub}>Loading from backend...</Subtitle>
          )}
        </View>

        <FlatList
          ref={listRef}
          data={messages}
          keyExtractor={(m) => String(m.id)}
          contentContainerStyle={styles.list}
          onContentSizeChange={() => listRef.current?.scrollToEnd()}
          ListEmptyComponent={<Text style={styles.empty}>No messages yet — send the first one.</Text>}
          renderItem={({ item }) => (
            <View style={styles.bubble}>
              <Text style={styles.body}>{item.body}</Text>
              <Text style={styles.time}>
                {item.createdAt ? new Date(item.createdAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : ""}
              </Text>
            </View>
          )}
        />

        <ErrorText>{error}</ErrorText>
        <View style={styles.inputRow}>
          <Field placeholder="Message..." value={text} onChangeText={setText} />
          <Button title="Send" onPress={send} loading={busy} />
        </View>
        <Button title="Deal details" variant="outline" onPress={() => router.back()} />
      </KeyboardAvoidingView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, padding: 16, gap: 10 },
  header: { marginBottom: 8 },
  title: { marginBottom: 4 },
  sub: { fontSize: 13 },
  list: { paddingVertical: 8, gap: 8, flexGrow: 1 },
  empty: { color: colors.grey, textAlign: "center", marginTop: 40 },
  bubble: {
    alignSelf: "flex-start",
    maxWidth: "85%",
    backgroundColor: colors.surface2,
    borderRadius: 12,
    borderTopLeftRadius: 4,
    padding: 12,
    borderWidth: 1,
    borderColor: colors.border,
  },
  body: { color: colors.white, fontSize: 14, lineHeight: 20 },
  time: { color: colors.grey, fontSize: 10, marginTop: 4, textAlign: "right" },
  inputRow: { gap: 8 },
});
