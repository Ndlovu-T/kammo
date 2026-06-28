import { createContext, useCallback, useContext, useRef, useState } from "react";
import { Animated, StyleSheet, Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { colors } from "../theme";

const ToastContext = createContext(null);

const VARIANT_STYLES = {
  error: { borderLeftColor: colors.red, icon: "⚠️" },
  success: { borderLeftColor: colors.green, icon: "✓" },
  info: { borderLeftColor: colors.blue, icon: "ℹ️" },
};

export function ToastProvider({ children }) {
  const [toast, setToast] = useState(null);
  const opacity = useRef(new Animated.Value(0)).current;
  const hideTimer = useRef(null);

  const hideToast = useCallback(() => {
    Animated.timing(opacity, { toValue: 0, duration: 200, useNativeDriver: true }).start(() => {
      setToast(null);
    });
  }, [opacity]);

  const showToast = useCallback(
    (message, variant = "error", duration = 4000) => {
      if (!message) return;
      if (hideTimer.current) clearTimeout(hideTimer.current);
      setToast({ message, variant });
      opacity.setValue(0);
      Animated.timing(opacity, { toValue: 1, duration: 200, useNativeDriver: true }).start();
      hideTimer.current = setTimeout(hideToast, duration);
    },
    [hideToast, opacity]
  );

  const style = toast ? VARIANT_STYLES[toast.variant] || VARIANT_STYLES.info : null;

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      {toast ? (
        <SafeAreaView style={styles.wrap} pointerEvents="none">
          <Animated.View style={[styles.toast, { borderLeftColor: style.borderLeftColor, opacity }]}>
            <Text style={styles.icon}>{style.icon}</Text>
            <Text style={styles.message}>{toast.message}</Text>
          </Animated.View>
        </SafeAreaView>
      ) : null}
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error("useToast must be used within a ToastProvider");
  return ctx;
}

const styles = StyleSheet.create({
  wrap: {
    position: "absolute",
    top: 0,
    left: 0,
    right: 0,
    alignItems: "center",
    paddingHorizontal: 16,
  },
  toast: {
    marginTop: 8,
    maxWidth: 480,
    width: "100%",
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    backgroundColor: colors.ink4,
    borderWidth: 1,
    borderColor: colors.line2,
    borderLeftWidth: 4,
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 14,
    shadowColor: "#000",
    shadowOpacity: 0.3,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 4 },
    elevation: 6,
  },
  icon: { fontSize: 15 },
  message: { flex: 1, color: colors.text, fontSize: 13, fontWeight: "600", lineHeight: 18 },
});
