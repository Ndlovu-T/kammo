import { router } from "expo-router";
import { useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import { CameraView, useCameraPermissions } from "expo-camera";
import { Button, Screen, Subtitle, Title } from "../src/components/ui";
import { useKammo } from "../src/KammoContext";
import { colors } from "../src/theme";

function extractDealCode(raw) {
  const value = String(raw || "").trim();
  const fromUrl = value.match(/deal\/([A-Za-z0-9]+)/i);
  if (fromUrl) return fromUrl[1];
  const fromRef = value.match(/KM-([A-Za-z0-9]+)/i);
  if (fromRef) return fromRef[1];
  return value.replace(/^KM-/i, "");
}

export default function ScanDealScreen() {
  const { selectDeal } = useKammo();
  const [permission, requestPermission] = useCameraPermissions();
  const [scanned, setScanned] = useState(false);

  async function handleScan({ data }) {
    if (scanned) return;
    setScanned(true);
    const code = extractDealCode(data);
    try {
      await selectDeal(code);
      router.replace(`/deal/${code}`);
    } catch {
      setScanned(false);
    }
  }

  if (!permission) {
    return (
      <Screen>
        <Subtitle>Loading camera...</Subtitle>
      </Screen>
    );
  }

  if (!permission.granted) {
    return (
      <Screen>
        <Title>Scan a deal QR</Title>
        <Subtitle>KAMMO needs camera access to scan deal QR codes and links.</Subtitle>
        <Button title="Allow Camera" onPress={requestPermission} />
        <Button title="Back" variant="outline" onPress={() => router.back()} />
      </Screen>
    );
  }

  return (
    <View style={styles.root}>
      <CameraView
        style={styles.camera}
        facing="back"
        barcodeScannerSettings={{ barcodeTypes: ["qr"] }}
        onBarcodeScanned={handleScan}
      />
      <View style={styles.overlay}>
        <Text style={styles.hint}>Point at a KAMMO deal QR code</Text>
        <View style={styles.frame} />
        <Button title="Cancel" variant="outline" onPress={() => router.back()} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: colors.black },
  camera: { flex: 1 },
  overlay: {
    position: "absolute",
    bottom: 0,
    left: 0,
    right: 0,
    padding: 24,
    paddingBottom: 40,
    gap: 16,
    backgroundColor: "rgba(0,0,0,0.55)",
  },
  hint: { color: colors.white, textAlign: "center", fontSize: 15, fontWeight: "600" },
  frame: {
    alignSelf: "center",
    width: 220,
    height: 220,
    borderWidth: 2,
    borderColor: colors.green,
    borderRadius: 16,
  },
});
