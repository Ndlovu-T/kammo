import { Linking, Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { router } from "expo-router";
import { useState } from "react";
import { MessageCircle } from "lucide-react-native";
import { Button, ErrorText, Field, Screen } from "../src/components/ui";
import { useKammo } from "../src/KammoContext";
import { colors } from "../src/theme";

export default function LoginScreen() {
  const { login, busy, error } = useKammo();
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");

  async function onSubmit() {
    try {
      await login({ phone, password });
      router.replace("/(tabs)");
    } catch {}
  }

  return (
    <Screen scroll={false} style={styles.wrap}>
      <Text style={styles.mark}>
        KA<Text style={styles.markG}>MM</Text>O<Text style={styles.markG}>.</Text>
      </Text>
      <Text style={styles.sub}>Secure the deal — every time.</Text>

      <Field label="Phone or KAMMO ID" value={phone} onChangeText={setPhone} placeholder="+27 8X XXX XXXX or KAM-XXXXXX" keyboardType="phone-pad" />
      <Field label="Password" value={password} onChangeText={setPassword} secureTextEntry placeholder="••••••••" />

      <Pressable>
        <Text style={styles.forgot}>Forgot password?</Text>
      </Pressable>

      <ErrorText>{error}</ErrorText>
      <Button title="Sign in" onPress={onSubmit} loading={busy} />

      <View style={styles.orLine}>
        <View style={styles.orRule} />
        <Text style={styles.orTxt}>or</Text>
        <View style={styles.orRule} />
      </View>

      <Pressable
        style={styles.waBtn}
        onPress={() => Linking.openURL("https://wa.me/27834523688?text=Hi+KAMMO%2C+I+want+to+sign+in+or+create+an+account.")}
      >
        <MessageCircle color={colors.text} size={18} />
        <Text style={styles.waTxt}>Continue with WhatsApp</Text>
      </Pressable>

      <Text style={styles.footer}>
        New here?{" "}
        <Text style={styles.link} onPress={() => router.push("/register")}>
          Create account
        </Text>
      </Text>
    </Screen>
  );
}

const styles = StyleSheet.create({
  wrap: { flexGrow: 1, paddingHorizontal: 22, paddingTop: 32, paddingBottom: 24, justifyContent: "flex-start" },
  mark: { fontSize: 46, color: colors.text, letterSpacing: -1, lineHeight: 48, marginBottom: 2 },
  markG: { color: colors.green },
  sub: { fontSize: 14, color: colors.mid, marginBottom: 32 },
  forgot: { textAlign: "right", fontSize: 12, color: colors.green, fontWeight: "600", marginBottom: 22 },
  orLine: { flexDirection: "row", alignItems: "center", marginVertical: 18, gap: 10 },
  orRule: { flex: 1, height: 1, backgroundColor: colors.line },
  orTxt: { fontSize: 12, color: colors.dim },
  waBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 8,
    paddingVertical: 14,
    borderRadius: 13,
    borderWidth: 1.5,
    borderColor: colors.line2,
  },
  waTxt: { fontSize: 14, fontWeight: "700", color: colors.text },
  footer: { textAlign: "center", fontSize: 13, color: colors.mid, marginTop: 20 },
  link: { color: colors.green, fontWeight: "600" },
});
