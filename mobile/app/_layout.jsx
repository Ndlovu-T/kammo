import "react-native-gesture-handler";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { KammoProvider } from "../src/KammoContext";
import { ToastProvider } from "../src/components/Toast";
import ErrorToastBridge from "../src/components/ErrorToastBridge";

export default function RootLayout() {
  return (
    <KammoProvider>
      <ToastProvider>
        <ErrorToastBridge />
        <StatusBar style="light" />
        <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: "#080a09" } }}>
          <Stack.Screen name="index" />
          <Stack.Screen name="login" />
          <Stack.Screen name="register" />
          <Stack.Screen name="(tabs)" />
          <Stack.Screen name="create-deal" options={{ presentation: "modal" }} />
          <Stack.Screen name="join-deal" />
          <Stack.Screen name="confirmed" />
          <Stack.Screen name="chat/[code]" />
          <Stack.Screen name="deal/[code]" />
          <Stack.Screen name="wallet/add" options={{ presentation: "modal" }} />
          <Stack.Screen name="wallet/withdraw" options={{ presentation: "modal" }} />
        </Stack>
      </ToastProvider>
    </KammoProvider>
  );
}
