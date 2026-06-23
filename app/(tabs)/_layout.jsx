import { Redirect, Tabs, router } from "expo-router";
import { Bell, LayoutGrid, Plus, User, Wallet } from "lucide-react-native";
import { Pressable, StyleSheet, View } from "react-native";
import { useKammo } from "../../src/KammoContext";
import { colors } from "../../src/theme";

function CreateFab() {
  return (
    <Pressable style={styles.fab} onPress={() => router.push("/create-deal")}>
      <Plus color={colors.ink} size={24} strokeWidth={2.5} />
    </Pressable>
  );
}

export default function TabsLayout() {
  const { ready, token } = useKammo();
  if (ready && !token) return <Redirect href="/" />;

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: styles.tabBar,
        tabBarActiveTintColor: colors.green,
        tabBarInactiveTintColor: colors.mid,
        tabBarLabelStyle: styles.tabLbl,
      }}
    >
      <Tabs.Screen
        name="index"
        options={{ title: "Deals", tabBarIcon: ({ color, size }) => <LayoutGrid color={color} size={size} /> }}
      />
      <Tabs.Screen name="wallet" options={{ title: "Wallet", tabBarIcon: ({ color, size }) => <Wallet color={color} size={size} /> }} />
      <Tabs.Screen
        name="create"
        options={{
          title: "New Deal",
          tabBarButton: () => <CreateFab />,
        }}
        listeners={{ tabPress: (e) => { e.preventDefault(); router.push("/create-deal"); } }}
      />
      <Tabs.Screen name="updates" options={{ title: "Updates", tabBarIcon: ({ color, size }) => <Bell color={color} size={size} /> }} />
      <Tabs.Screen name="profile" options={{ title: "Profile", tabBarIcon: ({ color, size }) => <User color={color} size={size} /> }} />
      <Tabs.Screen name="deals" options={{ href: null }} />
      <Tabs.Screen name="market" options={{ href: null }} />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  tabBar: {
    backgroundColor: colors.ink2,
    borderTopColor: colors.line,
    height: 80,
    paddingBottom: 18,
    paddingTop: 6,
  },
  tabLbl: { fontSize: 9.5, fontWeight: "600", letterSpacing: 0.3 },
  fab: {
    top: -10,
    width: 48,
    height: 48,
    borderRadius: 15,
    backgroundColor: colors.green,
    alignItems: "center",
    justifyContent: "center",
    shadowColor: colors.green,
    shadowOpacity: 0.35,
    shadowRadius: 10,
    elevation: 4,
  },
});
