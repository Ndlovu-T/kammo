import { router } from "expo-router";
import { useState } from "react";
import { Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { ArrowLeft } from "lucide-react-native";
import PhoneField from "../src/components/PhoneField";
import { Button, ErrorText, Field, InfoBox, Screen } from "../src/components/ui";
import { useKammo } from "../src/KammoContext";
import { colors } from "../src/theme";

export default function RegisterScreen() {
  const { register, busy, error } = useKammo();
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [idNumber, setIdNumber] = useState("");

  async function onSubmit() {
    try {
      await register({ phone, email, password });
      router.replace("/(tabs)");
    } catch {}
  }

  return (
    <Screen bg="bg1">
      <View style={styles.topbar}>
        <Pressable style={styles.back} onPress={() => router.back()}>
          <ArrowLeft color={colors.green} size={22} />
        </Pressable>
        <Text style={styles.topTitle}>Create Account</Text>
      </View>

      <Field label="Full name" value={name} onChangeText={setName} placeholder="Your legal name" />
      <PhoneField label="Phone number" value={phone} onChangeText={setPhone} />
      <Field label="Email" value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" placeholder="you@email.com" />
      <Field label="Password" value={password} onChangeText={setPassword} secureTextEntry placeholder="At least 8 characters" />
      <Field label="SA ID / Passport" value={idNumber} onChangeText={setIdNumber} placeholder="For identity verification" />

      <InfoBox title="🔒 Your KAMMO ID">
        Once verified you get a KAMMO ID (e.g. KAM-938201) — share it instead of your number. Your real number stays private.
      </InfoBox>

      <ErrorText>{error}</ErrorText>
      <Button title="Create account" onPress={onSubmit} loading={busy} />

      <Text style={styles.footer}>
        Already have an account?{" "}
        <Text style={styles.link} onPress={() => router.push("/login")}>
          Sign in
        </Text>
      </Text>
    </Screen>
  );
}

const styles = StyleSheet.create({
  topbar: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    marginBottom: 8,
    paddingBottom: 8,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
  },
  back: { padding: 4 },
  topTitle: { fontSize: 15, fontWeight: "700", color: colors.text },
  footer: { textAlign: "center", fontSize: 13, color: colors.mid, marginTop: 16 },
  link: { color: colors.green, fontWeight: "600" },
});
